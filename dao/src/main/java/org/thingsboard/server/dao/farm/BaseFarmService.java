/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.farm;

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
import org.thingsboard.server.common.data.farm.Farm;
import org.thingsboard.server.common.data.farm.FarmSearchQuery;
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
import org.thingsboard.server.dao.model.nosql.FarmEntity;
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
public class BaseFarmService extends AbstractEntityService implements FarmService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";
    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_FARM_ID = "Incorrect farmId ";
    @Autowired
    private FarmDao farmDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;

    @Override
    public Farm findFarmById(FarmId farmId) {
        log.trace("Executing findFarmById [{}]", farmId);
        validateId(farmId, INCORRECT_FARM_ID + farmId);
        return farmDao.findById(farmId.getId());
    }

    @Override
    public ListenableFuture<Farm> findFarmByIdAsync(FarmId farmId) {
        log.trace("Executing findFarmById [{}]", farmId);
        validateId(farmId, INCORRECT_FARM_ID + farmId);
        return farmDao.findByIdAsync(farmId.getId());
    }

    @Override
    public Optional<Farm> findFarmByTenantIdAndName(TenantId tenantId, String name) {
        log.trace("Executing findFarmByTenantIdAndName [{}][{}]", tenantId, name);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return farmDao.findFarmsByTenantIdAndName(tenantId.getId(), name);
    }

    @Override
    public Farm saveFarm(Farm farm) {
        log.trace("Executing saveFarm [{}]", farm);
        farmValidator.validate(farm);
        return farmDao.save(farm);
    }

    @Override
    public Farm assignFarmToCustomer(FarmId farmId, CustomerId customerId) {
        Farm farm = findFarmById(farmId);
        farm.setCustomerId(customerId);
        return saveFarm(farm);
    }

    @Override
    public Farm unassignFarmFromCustomer(FarmId farmId) {
        Farm farm = findFarmById(farmId);
        farm.setCustomerId(null);
        return saveFarm(farm);
    }

    @Override
    public void deleteFarm(FarmId farmId) {
        log.trace("Executing deleteFarm [{}]", farmId);
        validateId(farmId, INCORRECT_FARM_ID + farmId);
        deleteEntityRelations(farmId);
        farmDao.removeById(farmId.getId());
    }

    @Override
    public TextPageData<Farm> findFarmsByTenantId(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findFarmsByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Farm> farms = farmDao.findFarmsByTenantId(tenantId.getId(), pageLink);
        return new TextPageData<>(farms, pageLink);
    }

    @Override
    public TextPageData<Farm> findFarmsByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink) {
        log.trace("Executing findFarmsByTenantIdAndType, tenantId [{}], type [{}], pageLink [{}]", tenantId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Farm> farms = farmDao.findFarmsByTenantIdAndType(tenantId.getId(), type, pageLink);
        return new TextPageData<>(farms, pageLink);
    }

    @Override
    public ListenableFuture<List<Farm>> findFarmsByTenantIdAndIdsAsync(TenantId tenantId, List<FarmId> farmIds) {
        log.trace("Executing findFarmsByTenantIdAndIdsAsync, tenantId [{}], farmIds [{}]", tenantId, farmIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateIds(farmIds, "Incorrect farmIds " + farmIds);
        return farmDao.findFarmsByTenantIdAndIdsAsync(tenantId.getId(), toUUIDs(farmIds));
    }
    
    @Override
    public ListenableFuture<List<FarmEntity>> allFarms(){
        return farmDao.allFarms();
    }

    @Override
    public void deleteFarmsByTenantId(TenantId tenantId) {
        log.trace("Executing deleteFarmsByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantFarmsRemover.removeEntities(tenantId);
    }

    @Override
    public TextPageData<Farm> findFarmsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink) {
        log.trace("Executing findFarmsByTenantIdAndCustomerId, tenantId [{}], customerId [{}], pageLink [{}]", tenantId, customerId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Farm> farms = farmDao.findFarmsByTenantIdAndCustomerId(tenantId.getId(), customerId.getId(), pageLink);
        return new TextPageData<>(farms, pageLink);
    }

    @Override
    public TextPageData<Farm> findFarmsByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink) {
        log.trace("Executing findFarmsByTenantIdAndCustomerIdAndType, tenantId [{}], customerId [{}], type [{}], pageLink [{}]", tenantId, customerId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Farm> farms = farmDao.findFarmsByTenantIdAndCustomerIdAndType(tenantId.getId(), customerId.getId(), type, pageLink);
        return new TextPageData<>(farms, pageLink);
    }

    @Override
    public ListenableFuture<List<Farm>> findFarmsByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<FarmId> farmIds) {
        log.trace("Executing findFarmsByTenantIdAndCustomerIdAndIdsAsync, tenantId [{}], customerId [{}], farmIds [{}]", tenantId, customerId, farmIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateIds(farmIds, "Incorrect farmIds " + farmIds);
        return farmDao.findFarmsByTenantIdAndCustomerIdAndIdsAsync(tenantId.getId(), customerId.getId(), toUUIDs(farmIds));
    }

    @Override
    public void unassignCustomerFarms(TenantId tenantId, CustomerId customerId) {
        log.trace("Executing unassignCustomerFarms, tenantId [{}], customerId [{}]", tenantId, customerId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        new CustomerFarmsUnassigner(tenantId).removeEntities(customerId);
    }

    @Override
    public ListenableFuture<List<Farm>> findFarmsByQuery(FarmSearchQuery query) {
        ListenableFuture<List<EntityRelation>> relations = relationService.findByQuery(query.toEntitySearchQuery());
        ListenableFuture<List<Farm>> farms = Futures.transform(relations, (AsyncFunction<List<EntityRelation>, List<Farm>>) relations1 -> {
            EntitySearchDirection direction = query.toEntitySearchQuery().getParameters().getDirection();
            List<ListenableFuture<Farm>> futures = new ArrayList<>();
            for (EntityRelation relation : relations1) {
                EntityId entityId = direction == EntitySearchDirection.FROM ? relation.getTo() : relation.getFrom();
                if (entityId.getEntityType() == EntityType.FARM) {
                    futures.add(findFarmByIdAsync(new FarmId(entityId.getId())));
                }
            }
            return Futures.successfulAsList(futures);
        });
        farms = Futures.transform(farms, (Function<List<Farm>, List<Farm>>)farmList ->
            farmList == null ? Collections.emptyList() : farmList.stream().filter(farm -> query.getFarmTypes().contains(farm.getType())).collect(Collectors.toList())
        );
        return farms;
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findFarmTypesByTenantId(TenantId tenantId) {
        log.trace("Executing findFarmTypesByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        ListenableFuture<List<EntitySubtype>> tenantFarmTypes = farmDao.findTenantFarmTypesAsync(tenantId.getId());
        return Futures.transform(tenantFarmTypes,
                (Function<List<EntitySubtype>, List<EntitySubtype>>) farmTypes -> {
                    farmTypes.sort(Comparator.comparing(EntitySubtype::getType));
                    return farmTypes;
                });
    }

    private DataValidator<Farm> farmValidator =
            new DataValidator<Farm>() {

                @Override
                protected void validateCreate(Farm farm) {
                    farmDao.findFarmsByTenantIdAndName(farm.getTenantId().getId(), farm.getName()).ifPresent(
                            d -> {
                                throw new DataValidationException("Farm with such name already exists!");
                            }
                    );
                }

                @Override
                protected void validateUpdate(Farm farm) {
                    farmDao.findFarmsByTenantIdAndName(farm.getTenantId().getId(), farm.getName()).ifPresent(
                            d -> {
                                if (!d.getId().equals(farm.getId())) {
                                    throw new DataValidationException("Farm with such name already exists!");
                                }
                            }
                    );
                }

                @Override
                protected void validateDataImpl(Farm farm) {
                    farm.setType("Farm");
                    if (StringUtils.isEmpty(farm.getType())) {
                        throw new DataValidationException("Farm type should be specified!");
                    }
                    if (StringUtils.isEmpty(farm.getName())) {
                        throw new DataValidationException("Farm name should be specified!");
                    }
                    if (farm.getTenantId() == null) {
                        throw new DataValidationException("Farm should be assigned to tenant!");
                    } else {
                        Tenant tenant = tenantDao.findById(farm.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Farm is referencing to non-existent tenant!");
                        }
                    }
                    if (farm.getCustomerId() == null) {
                        farm.setCustomerId(new CustomerId(NULL_UUID));
                    } else if (!farm.getCustomerId().getId().equals(NULL_UUID)) {
                        Customer customer = customerDao.findById(farm.getCustomerId().getId());
                        if (customer == null) {
                            throw new DataValidationException("Can't assign farm to non-existent customer!");
                        }
                        if (!customer.getTenantId().equals(farm.getTenantId())) {
                            throw new DataValidationException("Can't assign farm to customer from different tenant!");
                        }
                    }
                }
            };

    private PaginatedRemover<TenantId, Farm> tenantFarmsRemover =
            new PaginatedRemover<TenantId, Farm>() {

                @Override
                protected List<Farm> findEntities(TenantId id, TextPageLink pageLink) {
                    return farmDao.findFarmsByTenantId(id.getId(), pageLink);
                }

                @Override
                protected void removeEntity(Farm entity) {
                    deleteFarm(new FarmId(entity.getId().getId()));
                }
            };

    class CustomerFarmsUnassigner extends PaginatedRemover<CustomerId, Farm> {

        private TenantId tenantId;

        CustomerFarmsUnassigner(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        protected List<Farm> findEntities(CustomerId id, TextPageLink pageLink) {
            return farmDao.findFarmsByTenantIdAndCustomerId(tenantId.getId(), id.getId(), pageLink);
        }

        @Override
        protected void removeEntity(Farm entity) {
            unassignFarmFromCustomer(new FarmId(entity.getId().getId()));
        }
    }
}
