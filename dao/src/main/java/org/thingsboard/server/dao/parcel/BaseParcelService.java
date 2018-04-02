/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.parcel;

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
import org.thingsboard.server.common.data.parcel.Parcel;
import org.thingsboard.server.common.data.parcel.ParcelSearchQuery;
import org.thingsboard.server.common.data.id.ParcelId;
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
import org.thingsboard.server.dao.model.nosql.ParcelEntity;
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
public class BaseParcelService extends AbstractEntityService implements ParcelService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";
    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_PARCEL_ID = "Incorrect parcelId ";
    @Autowired
    private ParcelDao parcelDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;

    @Override
    public Parcel findParcelById(ParcelId parcelId) {
        log.trace("Executing findParcelById [{}]", parcelId);
        validateId(parcelId, INCORRECT_PARCEL_ID + parcelId);
        return parcelDao.findById(parcelId.getId());
    }

    @Override
    public ListenableFuture<Parcel> findParcelByIdAsync(ParcelId parcelId) {
        log.trace("Executing findParcelById [{}]", parcelId);
        validateId(parcelId, INCORRECT_PARCEL_ID + parcelId);
        return parcelDao.findByIdAsync(parcelId.getId());
    }

    @Override
    public Optional<Parcel> findParcelByTenantIdAndName(TenantId tenantId, String name) {
        log.trace("Executing findParcelByTenantIdAndName [{}][{}]", tenantId, name);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return parcelDao.findParcelsByTenantIdAndName(tenantId.getId(), name);
    }

    @Override
    public Parcel saveParcel(Parcel parcel) {
        log.trace("Executing saveParcel [{}]", parcel);
        parcelValidator.validate(parcel);
        return parcelDao.save(parcel);
    }

    @Override
    public Parcel assignParcelToCustomer(ParcelId parcelId, CustomerId customerId) {
        Parcel parcel = findParcelById(parcelId);
        parcel.setCustomerId(customerId);
        return saveParcel(parcel);
    }

    @Override
    public Parcel unassignParcelFromCustomer(ParcelId parcelId) {
        Parcel parcel = findParcelById(parcelId);
        parcel.setCustomerId(null);
        return saveParcel(parcel);
    }

    @Override
    public void deleteParcel(ParcelId parcelId) {
        log.trace("Executing deleteParcel [{}]", parcelId);
        validateId(parcelId, INCORRECT_PARCEL_ID + parcelId);
        deleteEntityRelations(parcelId);
        parcelDao.removeById(parcelId.getId());
    }

    @Override
    public TextPageData<Parcel> findParcelsByTenantId(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findParcelsByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Parcel> parcels = parcelDao.findParcelsByTenantId(tenantId.getId(), pageLink);
        return new TextPageData<>(parcels, pageLink);
    }

    @Override
    public TextPageData<Parcel> findParcelsByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink) {
        log.trace("Executing findParcelsByTenantIdAndType, tenantId [{}], type [{}], pageLink [{}]", tenantId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Parcel> parcels = parcelDao.findParcelsByTenantIdAndType(tenantId.getId(), type, pageLink);
        return new TextPageData<>(parcels, pageLink);
    }

    @Override
    public ListenableFuture<List<Parcel>> findParcelsByTenantIdAndIdsAsync(TenantId tenantId, List<ParcelId> parcelIds) {
        log.trace("Executing findParcelsByTenantIdAndIdsAsync, tenantId [{}], parcelIds [{}]", tenantId, parcelIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateIds(parcelIds, "Incorrect parcelIds " + parcelIds);
        return parcelDao.findParcelsByTenantIdAndIdsAsync(tenantId.getId(), toUUIDs(parcelIds));
    }

    @Override
    public void deleteParcelsByTenantId(TenantId tenantId) {
        log.trace("Executing deleteParcelsByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantParcelsRemover.removeEntities(tenantId);
    }

    @Override
    public TextPageData<Parcel> findParcelsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink) {
        log.trace("Executing findParcelsByTenantIdAndCustomerId, tenantId [{}], customerId [{}], pageLink [{}]", tenantId, customerId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Parcel> parcels = parcelDao.findParcelsByTenantIdAndCustomerId(tenantId.getId(), customerId.getId(), pageLink);
        return new TextPageData<>(parcels, pageLink);
    }

    @Override
    public TextPageData<Parcel> findParcelsByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink) {
        log.trace("Executing findParcelsByTenantIdAndCustomerIdAndType, tenantId [{}], customerId [{}], type [{}], pageLink [{}]", tenantId, customerId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Parcel> parcels = parcelDao.findParcelsByTenantIdAndCustomerIdAndType(tenantId.getId(), customerId.getId(), type, pageLink);
        return new TextPageData<>(parcels, pageLink);
    }

    @Override
    public ListenableFuture<List<Parcel>> findParcelsByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<ParcelId> parcelIds) {
        log.trace("Executing findParcelsByTenantIdAndCustomerIdAndIdsAsync, tenantId [{}], customerId [{}], parcelIds [{}]", tenantId, customerId, parcelIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateIds(parcelIds, "Incorrect parcelIds " + parcelIds);
        return parcelDao.findParcelsByTenantIdAndCustomerIdAndIdsAsync(tenantId.getId(), customerId.getId(), toUUIDs(parcelIds));
    }

    @Override
    public void unassignCustomerParcels(TenantId tenantId, CustomerId customerId) {
        log.trace("Executing unassignCustomerParcels, tenantId [{}], customerId [{}]", tenantId, customerId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        new CustomerParcelsUnassigner(tenantId).removeEntities(customerId);
    }

    @Override
    public ListenableFuture<List<Parcel>> findParcelsByQuery(ParcelSearchQuery query) {
        ListenableFuture<List<EntityRelation>> relations = relationService.findByQuery(query.toEntitySearchQuery());
        ListenableFuture<List<Parcel>> parcels = Futures.transform(relations, (AsyncFunction<List<EntityRelation>, List<Parcel>>) relations1 -> {
            EntitySearchDirection direction = query.toEntitySearchQuery().getParameters().getDirection();
            List<ListenableFuture<Parcel>> futures = new ArrayList<>();
            for (EntityRelation relation : relations1) {
                EntityId entityId = direction == EntitySearchDirection.FROM ? relation.getTo() : relation.getFrom();
                if (entityId.getEntityType() == EntityType.PARCEL) {
                    futures.add(findParcelByIdAsync(new ParcelId(entityId.getId())));
                }
            }
            return Futures.successfulAsList(futures);
        });
        parcels = Futures.transform(parcels, (Function<List<Parcel>, List<Parcel>>)parcelList ->
            parcelList == null ? Collections.emptyList() : parcelList.stream().filter(parcel -> query.getParcelTypes().contains(parcel.getType())).collect(Collectors.toList())
        );
        return parcels;
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findParcelTypesByTenantId(TenantId tenantId) {
        log.trace("Executing findParcelTypesByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        ListenableFuture<List<EntitySubtype>> tenantParcelTypes = parcelDao.findTenantParcelTypesAsync(tenantId.getId());
        return Futures.transform(tenantParcelTypes,
                (Function<List<EntitySubtype>, List<EntitySubtype>>) parcelTypes -> {
                    parcelTypes.sort(Comparator.comparing(EntitySubtype::getType));
                    return parcelTypes;
                });
    }

    private DataValidator<Parcel> parcelValidator =
            new DataValidator<Parcel>() {

                @Override
                protected void validateCreate(Parcel parcel) {
                    parcelDao.findParcelsByTenantIdAndName(parcel.getTenantId().getId(), parcel.getName()).ifPresent(
                            d -> {
                                throw new DataValidationException("Parcel with such name already exists!");
                            }
                    );
                }

                @Override
                protected void validateUpdate(Parcel parcel) {
                    parcelDao.findParcelsByTenantIdAndName(parcel.getTenantId().getId(), parcel.getName()).ifPresent(
                            d -> {
                                if (!d.getId().equals(parcel.getId())) {
                                    throw new DataValidationException("Parcel with such name already exists!");
                                }
                            }
                    );
                }

                @Override
                protected void validateDataImpl(Parcel parcel) {
                    parcel.setType("Parcel");
                    if (StringUtils.isEmpty(parcel.getType())) {
                        throw new DataValidationException("Parcel type should be specified!");
                    }
                    if (StringUtils.isEmpty(parcel.getName())) {
                        throw new DataValidationException("Parcel name should be specified!");
                    }
                    if (parcel.getTenantId() == null) {
                        throw new DataValidationException("Parcel should be assigned to tenant!");
                    } else {
                        Tenant tenant = tenantDao.findById(parcel.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Parcel is referencing to non-existent tenant!");
                        }
                    }
                    if (parcel.getCustomerId() == null) {
                        parcel.setCustomerId(new CustomerId(NULL_UUID));
                    } else if (!parcel.getCustomerId().getId().equals(NULL_UUID)) {
                        Customer customer = customerDao.findById(parcel.getCustomerId().getId());
                        if (customer == null) {
                            throw new DataValidationException("Can't assign parcel to non-existent customer!");
                        }
                        if (!customer.getTenantId().equals(parcel.getTenantId())) {
                            throw new DataValidationException("Can't assign parcel to customer from different tenant!");
                        }
                    }
                }
            };

    private PaginatedRemover<TenantId, Parcel> tenantParcelsRemover =
            new PaginatedRemover<TenantId, Parcel>() {

                @Override
                protected List<Parcel> findEntities(TenantId id, TextPageLink pageLink) {
                    return parcelDao.findParcelsByTenantId(id.getId(), pageLink);
                }

                @Override
                protected void removeEntity(Parcel entity) {
                    deleteParcel(new ParcelId(entity.getId().getId()));
                }
            };

    @Override
    public ListenableFuture<List<ParcelEntity>> findParcelsByFarmId(String farmId) {
        return parcelDao.findParcelsByFarmId(farmId);
    }

    @Override
    public ListenableFuture<List<ParcelEntity>> allParcels() {
        return parcelDao.allParcels();
    }

    class CustomerParcelsUnassigner extends PaginatedRemover<CustomerId, Parcel> {

        private TenantId tenantId;

        CustomerParcelsUnassigner(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        protected List<Parcel> findEntities(CustomerId id, TextPageLink pageLink) {
            return parcelDao.findParcelsByTenantIdAndCustomerId(tenantId.getId(), id.getId(), pageLink);
        }

        @Override
        protected void removeEntity(Parcel entity) {
            unassignParcelFromCustomer(new ParcelId(entity.getId().getId()));
        }
    }
}
