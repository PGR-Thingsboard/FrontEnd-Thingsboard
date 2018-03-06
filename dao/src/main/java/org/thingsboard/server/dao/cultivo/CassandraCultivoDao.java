/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.cultivo;

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
import org.thingsboard.server.common.data.cultivo.Cultivo;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.EntitySubtypeEntity;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_BY_TENANT_AND_NAME_VIEW_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_CUSTOMER_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_NAME_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ID_PROPERTY;
import org.thingsboard.server.dao.model.nosql.CultivoEntity;
import org.thingsboard.server.dao.nosql.CassandraAbstractSearchTextDao;
import org.thingsboard.server.dao.util.NoSqlDao;

/**
 *
 * @author German Lopez
 */
@Component
@Slf4j
@NoSqlDao
public class CassandraCultivoDao extends CassandraAbstractSearchTextDao<CultivoEntity, Cultivo> implements CultivoDao {

    @Override
    protected Class<CultivoEntity> getColumnFamilyClass() {
        return CultivoEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CULTIVO_COLUMN_FAMILY_NAME;
    }

    @Override
    public Cultivo save(Cultivo domain) {
        Cultivo savedCultivo = super.save(domain);
        EntitySubtype entitySubtype = new EntitySubtype(savedCultivo.getTenantId(), EntityType.CULTIVO, savedCultivo.getType());
        EntitySubtypeEntity entitySubtypeEntity = new EntitySubtypeEntity(entitySubtype);
        Statement saveStatement = cluster.getMapper(EntitySubtypeEntity.class).saveQuery(entitySubtypeEntity);
        executeWrite(saveStatement);
        return savedCultivo;
    }

    @Override
    public List<Cultivo> findCultivosByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find cultivos by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<CultivoEntity> cultivoEntities = findPageWithTextSearch(CULTIVO_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Collections.singletonList(eq(CULTIVO_TENANT_ID_PROPERTY, tenantId)), pageLink);

        log.trace("Found cultivos [{}] by tenantId [{}] and pageLink [{}]", cultivoEntities, tenantId, pageLink);
        return DaoUtil.convertDataList(cultivoEntities);
    }

    @Override
    public List<Cultivo> findCultivosByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        log.debug("Try to find cultivos by tenantId [{}], type [{}] and pageLink [{}]", tenantId, type, pageLink);
        List<CultivoEntity> cultivoEntities = findPageWithTextSearch(CULTIVO_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(CULTIVO_TYPE_PROPERTY, type),
                        eq(CULTIVO_TENANT_ID_PROPERTY, tenantId)), pageLink);
        log.trace("Found cultivos [{}] by tenantId [{}], type [{}] and pageLink [{}]", cultivoEntities, tenantId, type, pageLink);
        return DaoUtil.convertDataList(cultivoEntities);
    }

    public ListenableFuture<List<Cultivo>> findCultivosByTenantIdAndIdsAsync(UUID tenantId, List<UUID> cultivoIds) {
        log.debug("Try to find cultivos by tenantId [{}] and cultivo Ids [{}]", tenantId, cultivoIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(CULTIVO_TENANT_ID_PROPERTY, tenantId));
        query.and(in(ID_PROPERTY, cultivoIds));
        return findListByStatementAsync(query);
    }

    @Override
    public List<Cultivo> findCultivosByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        log.debug("Try to find cultivos by tenantId [{}], customerId[{}] and pageLink [{}]", tenantId, customerId, pageLink);
        List<CultivoEntity> cultivoEntities = findPageWithTextSearch(CULTIVO_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(CULTIVO_CUSTOMER_ID_PROPERTY, customerId),
                        eq(CULTIVO_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found cultivos [{}] by tenantId [{}], customerId [{}] and pageLink [{}]", cultivoEntities, tenantId, customerId, pageLink);
        return DaoUtil.convertDataList(cultivoEntities);
    }

    @Override
    public List<Cultivo> findCultivosByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        log.debug("Try to find cultivos by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", tenantId, customerId, type, pageLink);
        List<CultivoEntity> cultivoEntities = findPageWithTextSearch(CULTIVO_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(CULTIVO_TYPE_PROPERTY, type),
                        eq(CULTIVO_CUSTOMER_ID_PROPERTY, customerId),
                        eq(CULTIVO_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found cultivos [{}] by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", cultivoEntities, tenantId, customerId, type, pageLink);
        return DaoUtil.convertDataList(cultivoEntities);
    }

    @Override
    public ListenableFuture<List<Cultivo>> findCultivosByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> cultivoIds) {
        log.debug("Try to find cultivos by tenantId [{}], customerId [{}] and cultivo Ids [{}]", tenantId, customerId, cultivoIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(CULTIVO_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(CULTIVO_CUSTOMER_ID_PROPERTY, customerId));
        query.and(in(ID_PROPERTY, cultivoIds));
        return findListByStatementAsync(query);
    }

    @Override
    public Optional<Cultivo> findCultivosByTenantIdAndName(UUID tenantId, String cultivoName) {
        Select select = select().from(CULTIVO_BY_TENANT_AND_NAME_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(CULTIVO_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(CULTIVO_NAME_PROPERTY, cultivoName));
        CultivoEntity cultivoEntity = (CultivoEntity) findOneByStatement(query);
        return Optional.ofNullable(DaoUtil.getData(cultivoEntity));
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantCultivoTypesAsync(UUID tenantId) {
        Select select = select().from(ENTITY_SUBTYPE_COLUMN_FAMILY_NAME);
        Select.Where query = select.where();
        query.and(eq(ENTITY_SUBTYPE_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY, EntityType.CULTIVO));
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

}

