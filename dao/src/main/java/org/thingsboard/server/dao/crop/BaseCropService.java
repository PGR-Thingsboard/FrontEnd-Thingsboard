/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.crop;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.crop.Crop;
import org.thingsboard.server.common.data.crop.CropSearchQuery;
import org.thingsboard.server.common.data.id.CropId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.FarmId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.EntitySearchDirection;
import static org.thingsboard.server.dao.DaoUtil.toUUIDs;
import org.thingsboard.server.dao.customer.CustomerDao;
import org.thingsboard.server.dao.entity.AbstractEntityService;
import org.thingsboard.server.dao.exception.DataValidationException;
import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID;
import org.thingsboard.server.dao.model.nosql.CropEntity;
import org.thingsboard.server.dao.service.DataValidator;
import org.thingsboard.server.dao.service.PaginatedRemover;
import static org.thingsboard.server.dao.service.Validator.validateId;
import static org.thingsboard.server.dao.service.Validator.validateIds;
import static org.thingsboard.server.dao.service.Validator.validatePageLink;
import static org.thingsboard.server.dao.service.Validator.validateString;
import org.thingsboard.server.dao.tenant.TenantDao;

/**
 *
 * @author German Lopez
 */
@Service
@Slf4j
public class BaseCropService extends AbstractEntityService implements CropService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";
    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_CROP_ID = "Incorrect cropId ";
    @Autowired
    private CropDao cropDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;

    @Override
    public Crop findCropById(CropId cropId) {
        log.trace("Executing findCropById [{}]", cropId);
        validateId(cropId, INCORRECT_CROP_ID + cropId);
        return cropDao.findById(cropId.getId());
    }

    @Override
    public ListenableFuture<Crop> findCropByIdAsync(CropId cropId) {
        log.trace("Executing findCropById [{}]", cropId);
        validateId(cropId, INCORRECT_CROP_ID + cropId);
        return cropDao.findByIdAsync(cropId.getId());
    }

    @Override
    public Optional<Crop> findCropByTenantIdAndName(TenantId tenantId, String name) {
        log.trace("Executing findCropByTenantIdAndName [{}][{}]", tenantId, name);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return cropDao.findCropsByTenantIdAndName(tenantId.getId(), name);
    }

    @Override
    public Crop saveCrop(Crop crop) {
        log.trace("Executing saveCrop [{}]", crop);
        cropValidator.validate(crop);
        return cropDao.save(crop);
    }

    @Override
    public Crop assignCropToCustomer(CropId cropId, CustomerId customerId) {
        Crop crop = findCropById(cropId);
        crop.setCustomerId(customerId);
        return saveCrop(crop);
    }

    @Override
    public Crop unassignCropFromCustomer(CropId cropId) {
        Crop crop = findCropById(cropId);
        crop.setCustomerId(null);
        return saveCrop(crop);
    }

    @Override
    public void deleteCrop(CropId cropId) {
        log.trace("Executing deleteCrop [{}]", cropId);
        validateId(cropId, INCORRECT_CROP_ID + cropId);
        deleteEntityRelations(cropId);
        cropDao.removeById(cropId.getId());
    }

    @Override
    public TextPageData<Crop> findCropsByTenantId(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findCropsByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Crop> crops = cropDao.findCropsByTenantId(tenantId.getId(), pageLink);
        return new TextPageData<>(crops, pageLink);
    }

    @Override
    public TextPageData<Crop> findCropsByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink) {
        log.trace("Executing findCropsByTenantIdAndType, tenantId [{}], type [{}], pageLink [{}]", tenantId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Crop> crops = cropDao.findCropsByTenantIdAndType(tenantId.getId(), type, pageLink);
        return new TextPageData<>(crops, pageLink);
    }

    @Override
    public ListenableFuture<List<Crop>> findCropsByTenantIdAndIdsAsync(TenantId tenantId, List<CropId> cropIds) {
        log.trace("Executing findCropsByTenantIdAndIdsAsync, tenantId [{}], cropIds [{}]", tenantId, cropIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateIds(cropIds, "Incorrect cropIds " + cropIds);
        return cropDao.findCropsByTenantIdAndIdsAsync(tenantId.getId(), toUUIDs(cropIds));
    }

    @Override
    public void deleteCropsByTenantId(TenantId tenantId) {
        log.trace("Executing deleteCropsByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantCropsRemover.removeEntities(tenantId);
    }

    @Override
    public TextPageData<Crop> findCropsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink) {
        log.trace("Executing findCropsByTenantIdAndCustomerId, tenantId [{}], customerId [{}], pageLink [{}]", tenantId, customerId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Crop> crops = cropDao.findCropsByTenantIdAndCustomerId(tenantId.getId(), customerId.getId(), pageLink);
        return new TextPageData<>(crops, pageLink);
    }

    @Override
    public TextPageData<Crop> findCropsByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink) {
        log.trace("Executing findCropsByTenantIdAndCustomerIdAndType, tenantId [{}], customerId [{}], type [{}], pageLink [{}]", tenantId, customerId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Crop> crops = cropDao.findCropsByTenantIdAndCustomerIdAndType(tenantId.getId(), customerId.getId(), type, pageLink);
        return new TextPageData<>(crops, pageLink);
    }

    @Override
    public ListenableFuture<List<Crop>> findCropsByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<CropId> cropIds) {
        log.trace("Executing findCropsByTenantIdAndCustomerIdAndIdsAsync, tenantId [{}], customerId [{}], cropIds [{}]", tenantId, customerId, cropIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateIds(cropIds, "Incorrect cropIds " + cropIds);
        return cropDao.findCropsByTenantIdAndCustomerIdAndIdsAsync(tenantId.getId(), customerId.getId(), toUUIDs(cropIds));
    }

    @Override
    public void unassignCustomerCrops(TenantId tenantId, CustomerId customerId) {
        log.trace("Executing unassignCustomerCrops, tenantId [{}], customerId [{}]", tenantId, customerId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        new CustomerCropsUnassigner(tenantId).removeEntities(customerId);
    }

    @Override
    public ListenableFuture<List<Crop>> findCropsByQuery(CropSearchQuery query) {
        ListenableFuture<List<EntityRelation>> relations = relationService.findByQuery(query.toEntitySearchQuery());
        ListenableFuture<List<Crop>> crops = Futures.transform(relations, (AsyncFunction<List<EntityRelation>, List<Crop>>) relations1 -> {
            EntitySearchDirection direction = query.toEntitySearchQuery().getParameters().getDirection();
            List<ListenableFuture<Crop>> futures = new ArrayList<>();
            for (EntityRelation relation : relations1) {
                EntityId entityId = direction == EntitySearchDirection.FROM ? relation.getTo() : relation.getFrom();
                if (entityId.getEntityType() == EntityType.CROP) {
                    futures.add(findCropByIdAsync(new CropId(entityId.getId())));
                }
            }
            return Futures.successfulAsList(futures);
        });
        crops = Futures.transform(crops, (Function<List<Crop>, List<Crop>>)cropList ->
            cropList == null ? Collections.emptyList() : cropList.stream().filter(crop -> query.getCropTypes().contains(crop.getType())).collect(Collectors.toList())
        );
        return crops;
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findCropTypesByTenantId(TenantId tenantId) {
        log.trace("Executing findCropTypesByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        ListenableFuture<List<EntitySubtype>> tenantCropTypes = cropDao.findTenantCropTypesAsync(tenantId.getId());
        return Futures.transform(tenantCropTypes,
                (Function<List<EntitySubtype>, List<EntitySubtype>>) cropTypes -> {
                    cropTypes.sort(Comparator.comparing(EntitySubtype::getType));
                    return cropTypes;
                });
    }

    private DataValidator<Crop> cropValidator =
            new DataValidator<Crop>() {

                @Override
                protected void validateCreate(Crop crop) {
                    cropDao.findCropsByTenantIdAndName(crop.getTenantId().getId(), crop.getName()).ifPresent(
                            d -> {
                                throw new DataValidationException("Crop with such name already exists!");
                            }
                    );
                }

                @Override
                protected void validateUpdate(Crop crop) {
                    cropDao.findCropsByTenantIdAndName(crop.getTenantId().getId(), crop.getName()).ifPresent(
                            d -> {
                                if (!d.getId().equals(crop.getId())) {
                                    throw new DataValidationException("Crop with such name already exists!");
                                }
                            }
                    );
                }

                @Override
                protected void validateDataImpl(Crop crop) {
                    crop.setType("Crop");
                    if (StringUtils.isEmpty(crop.getType())) {
                        throw new DataValidationException("Crop type should be specified!");
                    }
                    if (StringUtils.isEmpty(crop.getName())) {
                        throw new DataValidationException("Crop name should be specified!");
                    }
                    if (crop.getTenantId() == null) {
                        throw new DataValidationException("Crop should be assigned to tenant!");
                    } else {
                        Tenant tenant = tenantDao.findById(crop.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Crop is referencing to non-existent tenant!");
                        }
                    }
                    if (crop.getCustomerId() == null) {
                        crop.setCustomerId(new CustomerId(NULL_UUID));
                    } else if (!crop.getCustomerId().getId().equals(NULL_UUID)) {
                        Customer customer = customerDao.findById(crop.getCustomerId().getId());
                        if (customer == null) {
                            throw new DataValidationException("Can't assign crop to non-existent customer!");
                        }
                        if (!customer.getTenantId().equals(crop.getTenantId())) {
                            throw new DataValidationException("Can't assign crop to customer from different tenant!");
                        }
                    }
                }
            };

    private PaginatedRemover<TenantId, Crop> tenantCropsRemover =
            new PaginatedRemover<TenantId, Crop>() {

                @Override
                protected List<Crop> findEntities(TenantId id, TextPageLink pageLink) {
                    return cropDao.findCropsByTenantId(id.getId(), pageLink);
                }

                @Override
                protected void removeEntity(Crop entity) {
                    deleteCrop(new CropId(entity.getId().getId()));
                }
            };

    @Override
    public ListenableFuture<List<CropEntity>> findCropsByFarmId(String farmId) {
        return cropDao.findCropsByFarmId(farmId);
    }

    @Override
    public ListenableFuture<List<CropEntity>> allCrops() {
        return cropDao.allCrops();
    }

    class CustomerCropsUnassigner extends PaginatedRemover<CustomerId, Crop> {

        private TenantId tenantId;

        CustomerCropsUnassigner(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        protected List<Crop> findEntities(CustomerId id, TextPageLink pageLink) {
            return cropDao.findCropsByTenantIdAndCustomerId(tenantId.getId(), id.getId(), pageLink);
        }

        @Override
        protected void removeEntity(Crop entity) {
            unassignCropFromCustomer(new CropId(entity.getId().getId()));
        }
    }
}
