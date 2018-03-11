/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.farm;

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
import org.thingsboard.server.common.data.farm.Farm;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.EntitySubtypeEntity;
import static org.thingsboard.server.dao.model.ModelConstants.ALL_FARMS;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_BY_TENANT_AND_NAME_VIEW_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_CUSTOMER_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_NAME_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FARM_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ID_PROPERTY;
import org.thingsboard.server.dao.model.nosql.FarmEntity;
import org.thingsboard.server.dao.nosql.CassandraAbstractSearchTextDao;
import org.thingsboard.server.dao.util.NoSqlDao;

/**
 *
 * @author German Lopez
 */
@Component
@Slf4j
@NoSqlDao
public class CassandraFarmDao extends CassandraAbstractSearchTextDao<FarmEntity, Farm> implements FarmDao {

    @Override
    protected Class<FarmEntity> getColumnFamilyClass() {
        return FarmEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return FARM_COLUMN_FAMILY_NAME;
    }

    @Override
    public Farm save(Farm domain) {
        Farm savedFarm = super.save(domain);
        EntitySubtype entitySubtype = new EntitySubtype(savedFarm.getTenantId(), EntityType.FARM, savedFarm.getType());
        EntitySubtypeEntity entitySubtypeEntity = new EntitySubtypeEntity(entitySubtype);
        Statement saveStatement = cluster.getMapper(EntitySubtypeEntity.class).saveQuery(entitySubtypeEntity);
        executeWrite(saveStatement);
        return savedFarm;
    }
    

    @Override
    public List<Farm> findFarmsByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find farms by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<FarmEntity> farmEntities = findPageWithTextSearch(FARM_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Collections.singletonList(eq(FARM_TENANT_ID_PROPERTY, tenantId)), pageLink);

        log.trace("Found farms [{}] by tenantId [{}] and pageLink [{}]", farmEntities, tenantId, pageLink);
        return DaoUtil.convertDataList(farmEntities);
    }

    @Override
    public List<Farm> findFarmsByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        log.debug("Try to find farms by tenantId [{}], type [{}] and pageLink [{}]", tenantId, type, pageLink);
        List<FarmEntity> farmEntities = findPageWithTextSearch(FARM_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(FARM_TYPE_PROPERTY, type),
                        eq(FARM_TENANT_ID_PROPERTY, tenantId)), pageLink);
        log.trace("Found farms [{}] by tenantId [{}], type [{}] and pageLink [{}]", farmEntities, tenantId, type, pageLink);
        return DaoUtil.convertDataList(farmEntities);
    }

    public ListenableFuture<List<Farm>> findFarmsByTenantIdAndIdsAsync(UUID tenantId, List<UUID> farmIds) {
        log.debug("Try to find farms by tenantId [{}] and farm Ids [{}]", tenantId, farmIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(FARM_TENANT_ID_PROPERTY, tenantId));
        query.and(in(ID_PROPERTY, farmIds));
        return findListByStatementAsync(query);
    }

    @Override
    public List<Farm> findFarmsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        log.debug("Try to find farms by tenantId [{}], customerId[{}] and pageLink [{}]", tenantId, customerId, pageLink);
        List<FarmEntity> farmEntities = findPageWithTextSearch(FARM_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(FARM_CUSTOMER_ID_PROPERTY, customerId),
                        eq(FARM_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found farms [{}] by tenantId [{}], customerId [{}] and pageLink [{}]", farmEntities, tenantId, customerId, pageLink);
        return DaoUtil.convertDataList(farmEntities);
    }

    @Override
    public List<Farm> findFarmsByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        log.debug("Try to find farms by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", tenantId, customerId, type, pageLink);
        List<FarmEntity> farmEntities = findPageWithTextSearch(FARM_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(FARM_TYPE_PROPERTY, type),
                        eq(FARM_CUSTOMER_ID_PROPERTY, customerId),
                        eq(FARM_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found farms [{}] by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", farmEntities, tenantId, customerId, type, pageLink);
        return DaoUtil.convertDataList(farmEntities);
    }

    @Override
    public ListenableFuture<List<Farm>> findFarmsByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> farmIds) {
        log.debug("Try to find farms by tenantId [{}], customerId [{}] and farm Ids [{}]", tenantId, customerId, farmIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(FARM_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(FARM_CUSTOMER_ID_PROPERTY, customerId));
        query.and(in(ID_PROPERTY, farmIds));
        return findListByStatementAsync(query);
    }

    @Override
    public Optional<Farm> findFarmsByTenantIdAndName(UUID tenantId, String farmName) {
        Select select = select().from(FARM_BY_TENANT_AND_NAME_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(FARM_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(FARM_NAME_PROPERTY, farmName));
        FarmEntity farmEntity = (FarmEntity) findOneByStatement(query);
        return Optional.ofNullable(DaoUtil.getData(farmEntity));
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantFarmTypesAsync(UUID tenantId) {
        Select select = select().from(ENTITY_SUBTYPE_COLUMN_FAMILY_NAME);
        Select.Where query = select.where();
        query.and(eq(ENTITY_SUBTYPE_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY, EntityType.FARM));
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
    public ListenableFuture<List<FarmEntity>> allFarms() {
        Select select = select().from(ALL_FARMS);
        Select.Where query = select.where();
        ResultSetFuture resultSetFuture = getSession().executeAsync(query);
        return Futures.transform(resultSetFuture, new Function<ResultSet, List<FarmEntity>>() {
            @Nullable
            @Override
            public List<FarmEntity> apply(@Nullable ResultSet resultSet) {
                System.out.println("Result set Farm"+resultSet.toString());
                Result<FarmEntity> result = cluster.getMapper(FarmEntity.class).map(resultSet);
                if (result != null) {
                    List<FarmEntity> farms = new ArrayList<>();
                    result.all().forEach((farm) ->
                            farms.add(farm)
                    );
                    return farms;
                } else {
                    return Collections.emptyList();
                }
            }
        });
    }

    

}

