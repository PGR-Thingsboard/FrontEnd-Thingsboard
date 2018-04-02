/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.parcel;

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
import org.thingsboard.server.common.data.parcel.Parcel;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.EntitySubtypeEntity;
import static org.thingsboard.server.dao.model.ModelConstants.ALL_PARCELS;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_BY_TENANT_AND_NAME_VIEW_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_CUSTOMER_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_NAME_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.PARCEL_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ID_PROPERTY;
import org.thingsboard.server.dao.model.nosql.ParcelEntity;
import org.thingsboard.server.dao.nosql.CassandraAbstractSearchTextDao;
import org.thingsboard.server.dao.util.NoSqlDao;

/**
 *
 * @author German Lopez
 */
@Component
@Slf4j
@NoSqlDao
public class CassandraParcelDao extends CassandraAbstractSearchTextDao<ParcelEntity, Parcel> implements ParcelDao {

    @Override
    protected Class<ParcelEntity> getColumnFamilyClass() {
        return ParcelEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return PARCEL_COLUMN_FAMILY_NAME;
    }

    @Override
    public Parcel save(Parcel domain) {
        Parcel savedParcel = super.save(domain);
        EntitySubtype entitySubtype = new EntitySubtype(savedParcel.getTenantId(), EntityType.PARCEL, savedParcel.getType());
        EntitySubtypeEntity entitySubtypeEntity = new EntitySubtypeEntity(entitySubtype);
        Statement saveStatement = cluster.getMapper(EntitySubtypeEntity.class).saveQuery(entitySubtypeEntity);
        executeWrite(saveStatement);
        return savedParcel;
    }

    @Override
    public List<Parcel> findParcelsByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find parcels by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<ParcelEntity> parcelEntities = findPageWithTextSearch(PARCEL_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Collections.singletonList(eq(PARCEL_TENANT_ID_PROPERTY, tenantId)), pageLink);

        log.trace("Found parcels [{}] by tenantId [{}] and pageLink [{}]", parcelEntities, tenantId, pageLink);
        return DaoUtil.convertDataList(parcelEntities);
    }

    @Override
    public List<Parcel> findParcelsByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        log.debug("Try to find parcels by tenantId [{}], type [{}] and pageLink [{}]", tenantId, type, pageLink);
        List<ParcelEntity> parcelEntities = findPageWithTextSearch(PARCEL_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(PARCEL_TYPE_PROPERTY, type),
                        eq(PARCEL_TENANT_ID_PROPERTY, tenantId)), pageLink);
        log.trace("Found parcels [{}] by tenantId [{}], type [{}] and pageLink [{}]", parcelEntities, tenantId, type, pageLink);
        return DaoUtil.convertDataList(parcelEntities);
    }

    public ListenableFuture<List<Parcel>> findParcelsByTenantIdAndIdsAsync(UUID tenantId, List<UUID> parcelIds) {
        log.debug("Try to find parcels by tenantId [{}] and parcel Ids [{}]", tenantId, parcelIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(PARCEL_TENANT_ID_PROPERTY, tenantId));
        query.and(in(ID_PROPERTY, parcelIds));
        return findListByStatementAsync(query);
    }

    @Override
    public List<Parcel> findParcelsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        log.debug("Try to find parcels by tenantId [{}], customerId[{}] and pageLink [{}]", tenantId, customerId, pageLink);
        List<ParcelEntity> parcelEntities = findPageWithTextSearch(PARCEL_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(PARCEL_CUSTOMER_ID_PROPERTY, customerId),
                        eq(PARCEL_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found parcels [{}] by tenantId [{}], customerId [{}] and pageLink [{}]", parcelEntities, tenantId, customerId, pageLink);
        return DaoUtil.convertDataList(parcelEntities);
    }

    @Override
    public List<Parcel> findParcelsByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        log.debug("Try to find parcels by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", tenantId, customerId, type, pageLink);
        List<ParcelEntity> parcelEntities = findPageWithTextSearch(PARCEL_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(PARCEL_TYPE_PROPERTY, type),
                        eq(PARCEL_CUSTOMER_ID_PROPERTY, customerId),
                        eq(PARCEL_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found parcels [{}] by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", parcelEntities, tenantId, customerId, type, pageLink);
        return DaoUtil.convertDataList(parcelEntities);
    }

    @Override
    public ListenableFuture<List<Parcel>> findParcelsByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> parcelIds) {
        log.debug("Try to find parcels by tenantId [{}], customerId [{}] and parcel Ids [{}]", tenantId, customerId, parcelIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(PARCEL_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(PARCEL_CUSTOMER_ID_PROPERTY, customerId));
        query.and(in(ID_PROPERTY, parcelIds));
        return findListByStatementAsync(query);
    }

    @Override
    public Optional<Parcel> findParcelsByTenantIdAndName(UUID tenantId, String parcelName) {
        Select select = select().from(PARCEL_BY_TENANT_AND_NAME_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(PARCEL_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(PARCEL_NAME_PROPERTY, parcelName));
        ParcelEntity parcelEntity = (ParcelEntity) findOneByStatement(query);
        return Optional.ofNullable(DaoUtil.getData(parcelEntity));
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantParcelTypesAsync(UUID tenantId) {
        Select select = select().from(ENTITY_SUBTYPE_COLUMN_FAMILY_NAME);
        Select.Where query = select.where();
        query.and(eq(ENTITY_SUBTYPE_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY, EntityType.PARCEL));
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
    public ListenableFuture<List<ParcelEntity>> findParcelsByFarmId(String farmId) {
        Select select = select().from(PARCEL_COLUMN_FAMILY_NAME);
        Select.Where query = select.where();
        query.and(eq(PARCEL_COLUMN_FAMILY_NAME, farmId));
        ResultSetFuture resultSetFuture = getSession().executeAsync(query);
        return Futures.transform(resultSetFuture, new Function<ResultSet, List<ParcelEntity>>() {
            @Nullable
            @Override
            public List<ParcelEntity> apply(@Nullable ResultSet resultSet) {
                Result<ParcelEntity> result = cluster.getMapper(ParcelEntity.class).map(resultSet);
                if (result != null) {
                    List<ParcelEntity> parcels = new ArrayList<>();
                    result.all().forEach((parcel) ->
                            parcels.add(parcel)
                    );
                    return parcels;
                } else {
                    return Collections.emptyList();
                }
            }
        });
    }
    
    @Override
    public ListenableFuture<List<ParcelEntity>> allParcels() {
        Select select = select().from(ALL_PARCELS);
        Select.Where query = select.where();
        ResultSetFuture resultSetFuture = getSession().executeAsync(query);
        return Futures.transform(resultSetFuture, new Function<ResultSet, List<ParcelEntity>>() {
            @Nullable
            @Override
            public List<ParcelEntity> apply(@Nullable ResultSet resultSet) {
                Result<ParcelEntity> result = cluster.getMapper(ParcelEntity.class).map(resultSet);
                if (result != null) {
                    List<ParcelEntity> parcels = new ArrayList<>();
                    result.all().forEach((parcel) ->
                            parcels.add(parcel)
                    );
                    return parcels;
                } else {
                    return Collections.emptyList();
                }
            }
        });
    }
    
    

}

