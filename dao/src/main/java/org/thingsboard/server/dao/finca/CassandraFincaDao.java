/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.finca;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Result;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.finca.Finca;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.EntitySubtypeEntity;
import static org.thingsboard.server.dao.model.ModelConstants.ALL_FINCAS;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_BY_TENANT_AND_NAME_VIEW_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_CUSTOMER_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_NAME_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ID_PROPERTY;
import org.thingsboard.server.dao.model.nosql.FincaEntity;
import org.thingsboard.server.dao.nosql.CassandraAbstractSearchTextDao;
import org.thingsboard.server.dao.util.NoSqlDao;

/**
 *
 * @author German Lopez
 */
@Component
@Slf4j
@NoSqlDao
public class CassandraFincaDao extends CassandraAbstractSearchTextDao<FincaEntity, Finca> implements FincaDao {

    @Override
    protected Class<FincaEntity> getColumnFamilyClass() {
        return FincaEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return FINCA_COLUMN_FAMILY_NAME;
    }

    @Override
    public Finca save(Finca domain) {
        Finca savedFinca = super.save(domain);
        EntitySubtype entitySubtype = new EntitySubtype(savedFinca.getTenantId(), EntityType.FINCA, savedFinca.getType());
        EntitySubtypeEntity entitySubtypeEntity = new EntitySubtypeEntity(entitySubtype);
        Statement saveStatement = cluster.getMapper(EntitySubtypeEntity.class).saveQuery(entitySubtypeEntity);
        executeWrite(saveStatement);
        return savedFinca;
    }
    

    @Override
    public List<Finca> findFincasByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find fincas by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<FincaEntity> fincaEntities = findPageWithTextSearch(FINCA_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Collections.singletonList(eq(FINCA_TENANT_ID_PROPERTY, tenantId)), pageLink);

        log.trace("Found fincas [{}] by tenantId [{}] and pageLink [{}]", fincaEntities, tenantId, pageLink);
        return DaoUtil.convertDataList(fincaEntities);
    }

    @Override
    public List<Finca> findFincasByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        log.debug("Try to find fincas by tenantId [{}], type [{}] and pageLink [{}]", tenantId, type, pageLink);
        List<FincaEntity> fincaEntities = findPageWithTextSearch(FINCA_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(FINCA_TYPE_PROPERTY, type),
                        eq(FINCA_TENANT_ID_PROPERTY, tenantId)), pageLink);
        log.trace("Found fincas [{}] by tenantId [{}], type [{}] and pageLink [{}]", fincaEntities, tenantId, type, pageLink);
        return DaoUtil.convertDataList(fincaEntities);
    }

    public ListenableFuture<List<Finca>> findFincasByTenantIdAndIdsAsync(UUID tenantId, List<UUID> fincaIds) {
        log.debug("Try to find fincas by tenantId [{}] and finca Ids [{}]", tenantId, fincaIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(FINCA_TENANT_ID_PROPERTY, tenantId));
        query.and(in(ID_PROPERTY, fincaIds));
        return findListByStatementAsync(query);
    }

    @Override
    public List<Finca> findFincasByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        log.debug("Try to find fincas by tenantId [{}], customerId[{}] and pageLink [{}]", tenantId, customerId, pageLink);
        List<FincaEntity> fincaEntities = findPageWithTextSearch(FINCA_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(FINCA_CUSTOMER_ID_PROPERTY, customerId),
                        eq(FINCA_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found fincas [{}] by tenantId [{}], customerId [{}] and pageLink [{}]", fincaEntities, tenantId, customerId, pageLink);
        return DaoUtil.convertDataList(fincaEntities);
    }

    @Override
    public List<Finca> findFincasByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        log.debug("Try to find fincas by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", tenantId, customerId, type, pageLink);
        List<FincaEntity> fincaEntities = findPageWithTextSearch(FINCA_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(FINCA_TYPE_PROPERTY, type),
                        eq(FINCA_CUSTOMER_ID_PROPERTY, customerId),
                        eq(FINCA_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found fincas [{}] by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", fincaEntities, tenantId, customerId, type, pageLink);
        return DaoUtil.convertDataList(fincaEntities);
    }

    @Override
    public ListenableFuture<List<Finca>> findFincasByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> fincaIds) {
        log.debug("Try to find fincas by tenantId [{}], customerId [{}] and finca Ids [{}]", tenantId, customerId, fincaIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(FINCA_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(FINCA_CUSTOMER_ID_PROPERTY, customerId));
        query.and(in(ID_PROPERTY, fincaIds));
        return findListByStatementAsync(query);
    }

    @Override
    public Optional<Finca> findFincasByTenantIdAndName(UUID tenantId, String fincaName) {
        Select select = select().from(FINCA_BY_TENANT_AND_NAME_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(FINCA_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(FINCA_NAME_PROPERTY, fincaName));
        FincaEntity fincaEntity = (FincaEntity) findOneByStatement(query);
        return Optional.ofNullable(DaoUtil.getData(fincaEntity));
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantFincaTypesAsync(UUID tenantId) {
        Select select = select().from(ENTITY_SUBTYPE_COLUMN_FAMILY_NAME);
        Select.Where query = select.where();
        query.and(eq(ENTITY_SUBTYPE_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY, EntityType.FINCA));
        query.setConsistencyLevel(cluster.getDefaultReadConsistencyLevel());
        ResultSetFuture resultSetFuture = getSession().executeAsync(query);
        return Futures.transform(resultSetFuture, new Function<ResultSet, List<EntitySubtype>>() {
            @Nullable
            @Override
            public List<EntitySubtype> apply(@Nullable ResultSet resultSet) {
                Result<EntitySubtypeEntity> result = cluster.getMapper(EntitySubtypeEntity.class).map(resultSet);
                if (result != null) {
                    List<EntitySubtype> entitySubtypes = new ArrayList<>();
                    result.all().forEach((entitySubtypeEntity) ->
                            entitySubtypes.add(entitySubtypeEntity.toEntitySubtype())
                    );
                    return entitySubtypes;
                } else {
                    return Collections.emptyList();
                }
            }
        });
    }

    @Override
    public ListenableFuture<List<FincaEntity>> allFincas() {
        Select select = select().from(ALL_FINCAS);
        Select.Where query = select.where();
        ResultSetFuture resultSetFuture = getSession().executeAsync(query);
        return Futures.transform(resultSetFuture, new Function<ResultSet, List<FincaEntity>>() {
            @Nullable
            @Override
            public List<FincaEntity> apply(@Nullable ResultSet resultSet) {
                System.out.println("Result set Finca"+resultSet.toString());
                Result<FincaEntity> result = cluster.getMapper(FincaEntity.class).map(resultSet);
                if (result != null) {
                    List<FincaEntity> fincas = new ArrayList<>();
                    result.all().forEach((finca) ->
                            fincas.add(finca)
                    );
                    return fincas;
                } else {
                    return Collections.emptyList();
                }
            }
        });
    }

    

}

