/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.crop;

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
import org.thingsboard.server.common.data.crop.Crop;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.EntitySubtypeEntity;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_BY_TENANT_AND_NAME_VIEW_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_CUSTOMER_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_NAME_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_SUBTYPE_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ID_PROPERTY;
import org.thingsboard.server.dao.model.nosql.CropEntity;
import org.thingsboard.server.dao.nosql.CassandraAbstractSearchTextDao;
import org.thingsboard.server.dao.util.NoSqlDao;

/**
 *
 * @author German Lopez
 */
@Component
@Slf4j
@NoSqlDao
public class CassandraCropDao extends CassandraAbstractSearchTextDao<CropEntity, Crop> implements CropDao {

    @Override
    protected Class<CropEntity> getColumnFamilyClass() {
        return CropEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CROP_COLUMN_FAMILY_NAME;
    }

    @Override
    public Crop save(Crop domain) {
        Crop savedCrop = super.save(domain);
        EntitySubtype entitySubtype = new EntitySubtype(savedCrop.getTenantId(), EntityType.CROP, savedCrop.getType());
        EntitySubtypeEntity entitySubtypeEntity = new EntitySubtypeEntity(entitySubtype);
        Statement saveStatement = cluster.getMapper(EntitySubtypeEntity.class).saveQuery(entitySubtypeEntity);
        executeWrite(saveStatement);
        return savedCrop;
    }

    @Override
    public List<Crop> findCropsByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find crops by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<CropEntity> cropEntities = findPageWithTextSearch(CROP_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Collections.singletonList(eq(CROP_TENANT_ID_PROPERTY, tenantId)), pageLink);

        log.trace("Found crops [{}] by tenantId [{}] and pageLink [{}]", cropEntities, tenantId, pageLink);
        return DaoUtil.convertDataList(cropEntities);
    }

    @Override
    public List<Crop> findCropsByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        log.debug("Try to find crops by tenantId [{}], type [{}] and pageLink [{}]", tenantId, type, pageLink);
        List<CropEntity> cropEntities = findPageWithTextSearch(CROP_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(CROP_TYPE_PROPERTY, type),
                        eq(CROP_TENANT_ID_PROPERTY, tenantId)), pageLink);
        log.trace("Found crops [{}] by tenantId [{}], type [{}] and pageLink [{}]", cropEntities, tenantId, type, pageLink);
        return DaoUtil.convertDataList(cropEntities);
    }

    public ListenableFuture<List<Crop>> findCropsByTenantIdAndIdsAsync(UUID tenantId, List<UUID> cropIds) {
        log.debug("Try to find crops by tenantId [{}] and crop Ids [{}]", tenantId, cropIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(CROP_TENANT_ID_PROPERTY, tenantId));
        query.and(in(ID_PROPERTY, cropIds));
        return findListByStatementAsync(query);
    }

    @Override
    public List<Crop> findCropsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        log.debug("Try to find crops by tenantId [{}], customerId[{}] and pageLink [{}]", tenantId, customerId, pageLink);
        List<CropEntity> cropEntities = findPageWithTextSearch(CROP_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(CROP_CUSTOMER_ID_PROPERTY, customerId),
                        eq(CROP_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found crops [{}] by tenantId [{}], customerId [{}] and pageLink [{}]", cropEntities, tenantId, customerId, pageLink);
        return DaoUtil.convertDataList(cropEntities);
    }

    @Override
    public List<Crop> findCropsByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        log.debug("Try to find crops by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", tenantId, customerId, type, pageLink);
        List<CropEntity> cropEntities = findPageWithTextSearch(CROP_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(CROP_TYPE_PROPERTY, type),
                        eq(CROP_CUSTOMER_ID_PROPERTY, customerId),
                        eq(CROP_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found crops [{}] by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", cropEntities, tenantId, customerId, type, pageLink);
        return DaoUtil.convertDataList(cropEntities);
    }

    @Override
    public ListenableFuture<List<Crop>> findCropsByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> cropIds) {
        log.debug("Try to find crops by tenantId [{}], customerId [{}] and crop Ids [{}]", tenantId, customerId, cropIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(CROP_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(CROP_CUSTOMER_ID_PROPERTY, customerId));
        query.and(in(ID_PROPERTY, cropIds));
        return findListByStatementAsync(query);
    }

    @Override
    public Optional<Crop> findCropsByTenantIdAndName(UUID tenantId, String cropName) {
        Select select = select().from(CROP_BY_TENANT_AND_NAME_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(CROP_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(CROP_NAME_PROPERTY, cropName));
        CropEntity cropEntity = (CropEntity) findOneByStatement(query);
        return Optional.ofNullable(DaoUtil.getData(cropEntity));
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantCropTypesAsync(UUID tenantId) {
        Select select = select().from(ENTITY_SUBTYPE_COLUMN_FAMILY_NAME);
        Select.Where query = select.where();
        query.and(eq(ENTITY_SUBTYPE_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY, EntityType.CROP));
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

