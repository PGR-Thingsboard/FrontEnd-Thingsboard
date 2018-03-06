/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.finca;

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
import org.thingsboard.server.common.data.finca.Finca;
import org.thingsboard.server.common.data.finca.FincaSearchQuery;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.FincaId;
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
public class BaseFincaService extends AbstractEntityService implements FincaService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";
    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_FINCA_ID = "Incorrect fincaId ";
    @Autowired
    private FincaDao fincaDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;

    @Override
    public Finca findFincaById(FincaId fincaId) {
        log.trace("Executing findFincaById [{}]", fincaId);
        validateId(fincaId, INCORRECT_FINCA_ID + fincaId);
        return fincaDao.findById(fincaId.getId());
    }

    @Override
    public ListenableFuture<Finca> findFincaByIdAsync(FincaId fincaId) {
        log.trace("Executing findFincaById [{}]", fincaId);
        validateId(fincaId, INCORRECT_FINCA_ID + fincaId);
        return fincaDao.findByIdAsync(fincaId.getId());
    }

    @Override
    public Optional<Finca> findFincaByTenantIdAndName(TenantId tenantId, String name) {
        log.trace("Executing findFincaByTenantIdAndName [{}][{}]", tenantId, name);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return fincaDao.findFincasByTenantIdAndName(tenantId.getId(), name);
    }

    @Override
    public Finca saveFinca(Finca finca) {
        log.trace("Executing saveFinca [{}]", finca);
        fincaValidator.validate(finca);
        return fincaDao.save(finca);
    }

    @Override
    public Finca assignFincaToCustomer(FincaId fincaId, CustomerId customerId) {
        Finca finca = findFincaById(fincaId);
        finca.setCustomerId(customerId);
        return saveFinca(finca);
    }

    @Override
    public Finca unassignFincaFromCustomer(FincaId fincaId) {
        Finca finca = findFincaById(fincaId);
        finca.setCustomerId(null);
        return saveFinca(finca);
    }

    @Override
    public void deleteFinca(FincaId fincaId) {
        log.trace("Executing deleteFinca [{}]", fincaId);
        validateId(fincaId, INCORRECT_FINCA_ID + fincaId);
        deleteEntityRelations(fincaId);
        fincaDao.removeById(fincaId.getId());
    }

    @Override
    public TextPageData<Finca> findFincasByTenantId(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findFincasByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Finca> fincas = fincaDao.findFincasByTenantId(tenantId.getId(), pageLink);
        return new TextPageData<>(fincas, pageLink);
    }

    @Override
    public TextPageData<Finca> findFincasByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink) {
        log.trace("Executing findFincasByTenantIdAndType, tenantId [{}], type [{}], pageLink [{}]", tenantId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Finca> fincas = fincaDao.findFincasByTenantIdAndType(tenantId.getId(), type, pageLink);
        return new TextPageData<>(fincas, pageLink);
    }

    @Override
    public ListenableFuture<List<Finca>> findFincasByTenantIdAndIdsAsync(TenantId tenantId, List<FincaId> fincaIds) {
        log.trace("Executing findFincasByTenantIdAndIdsAsync, tenantId [{}], fincaIds [{}]", tenantId, fincaIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateIds(fincaIds, "Incorrect fincaIds " + fincaIds);
        return fincaDao.findFincasByTenantIdAndIdsAsync(tenantId.getId(), toUUIDs(fincaIds));
    }

    @Override
    public void deleteFincasByTenantId(TenantId tenantId) {
        log.trace("Executing deleteFincasByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantFincasRemover.removeEntities(tenantId);
    }

    @Override
    public TextPageData<Finca> findFincasByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink) {
        log.trace("Executing findFincasByTenantIdAndCustomerId, tenantId [{}], customerId [{}], pageLink [{}]", tenantId, customerId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Finca> fincas = fincaDao.findFincasByTenantIdAndCustomerId(tenantId.getId(), customerId.getId(), pageLink);
        return new TextPageData<>(fincas, pageLink);
    }

    @Override
    public TextPageData<Finca> findFincasByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink) {
        log.trace("Executing findFincasByTenantIdAndCustomerIdAndType, tenantId [{}], customerId [{}], type [{}], pageLink [{}]", tenantId, customerId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Finca> fincas = fincaDao.findFincasByTenantIdAndCustomerIdAndType(tenantId.getId(), customerId.getId(), type, pageLink);
        return new TextPageData<>(fincas, pageLink);
    }

    @Override
    public ListenableFuture<List<Finca>> findFincasByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<FincaId> fincaIds) {
        log.trace("Executing findFincasByTenantIdAndCustomerIdAndIdsAsync, tenantId [{}], customerId [{}], fincaIds [{}]", tenantId, customerId, fincaIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateIds(fincaIds, "Incorrect fincaIds " + fincaIds);
        return fincaDao.findFincasByTenantIdAndCustomerIdAndIdsAsync(tenantId.getId(), customerId.getId(), toUUIDs(fincaIds));
    }

    @Override
    public void unassignCustomerFincas(TenantId tenantId, CustomerId customerId) {
        log.trace("Executing unassignCustomerFincas, tenantId [{}], customerId [{}]", tenantId, customerId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        new CustomerFincasUnassigner(tenantId).removeEntities(customerId);
    }

    @Override
    public ListenableFuture<List<Finca>> findFincasByQuery(FincaSearchQuery query) {
        ListenableFuture<List<EntityRelation>> relations = relationService.findByQuery(query.toEntitySearchQuery());
        ListenableFuture<List<Finca>> fincas = Futures.transform(relations, (AsyncFunction<List<EntityRelation>, List<Finca>>) relations1 -> {
            EntitySearchDirection direction = query.toEntitySearchQuery().getParameters().getDirection();
            List<ListenableFuture<Finca>> futures = new ArrayList<>();
            for (EntityRelation relation : relations1) {
                EntityId entityId = direction == EntitySearchDirection.FROM ? relation.getTo() : relation.getFrom();
                if (entityId.getEntityType() == EntityType.FINCA) {
                    futures.add(findFincaByIdAsync(new FincaId(entityId.getId())));
                }
            }
            return Futures.successfulAsList(futures);
        });
        fincas = Futures.transform(fincas, (Function<List<Finca>, List<Finca>>)fincaList ->
            fincaList == null ? Collections.emptyList() : fincaList.stream().filter(finca -> query.getFincaTypes().contains(finca.getType())).collect(Collectors.toList())
        );
        return fincas;
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findFincaTypesByTenantId(TenantId tenantId) {
        log.trace("Executing findFincaTypesByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        ListenableFuture<List<EntitySubtype>> tenantFincaTypes = fincaDao.findTenantFincaTypesAsync(tenantId.getId());
        return Futures.transform(tenantFincaTypes,
                (Function<List<EntitySubtype>, List<EntitySubtype>>) fincaTypes -> {
                    fincaTypes.sort(Comparator.comparing(EntitySubtype::getType));
                    return fincaTypes;
                });
    }

    private DataValidator<Finca> fincaValidator =
            new DataValidator<Finca>() {

                @Override
                protected void validateCreate(Finca finca) {
                    fincaDao.findFincasByTenantIdAndName(finca.getTenantId().getId(), finca.getName()).ifPresent(
                            d -> {
                                throw new DataValidationException("Finca with such name already exists!");
                            }
                    );
                }

                @Override
                protected void validateUpdate(Finca finca) {
                    fincaDao.findFincasByTenantIdAndName(finca.getTenantId().getId(), finca.getName()).ifPresent(
                            d -> {
                                if (!d.getId().equals(finca.getId())) {
                                    throw new DataValidationException("Finca with such name already exists!");
                                }
                            }
                    );
                }

                @Override
                protected void validateDataImpl(Finca finca) {
                    finca.setType("Finca");
                    if (StringUtils.isEmpty(finca.getType())) {
                        throw new DataValidationException("Finca type should be specified!");
                    }
                    if (StringUtils.isEmpty(finca.getName())) {
                        throw new DataValidationException("Finca name should be specified!");
                    }
                    if (finca.getTenantId() == null) {
                        throw new DataValidationException("Finca should be assigned to tenant!");
                    } else {
                        Tenant tenant = tenantDao.findById(finca.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Finca is referencing to non-existent tenant!");
                        }
                    }
                    if (finca.getCustomerId() == null) {
                        finca.setCustomerId(new CustomerId(NULL_UUID));
                    } else if (!finca.getCustomerId().getId().equals(NULL_UUID)) {
                        Customer customer = customerDao.findById(finca.getCustomerId().getId());
                        if (customer == null) {
                            throw new DataValidationException("Can't assign finca to non-existent customer!");
                        }
                        if (!customer.getTenantId().equals(finca.getTenantId())) {
                            throw new DataValidationException("Can't assign finca to customer from different tenant!");
                        }
                    }
                }
            };

    private PaginatedRemover<TenantId, Finca> tenantFincasRemover =
            new PaginatedRemover<TenantId, Finca>() {

                @Override
                protected List<Finca> findEntities(TenantId id, TextPageLink pageLink) {
                    return fincaDao.findFincasByTenantId(id.getId(), pageLink);
                }

                @Override
                protected void removeEntity(Finca entity) {
                    deleteFinca(new FincaId(entity.getId().getId()));
                }
            };

    class CustomerFincasUnassigner extends PaginatedRemover<CustomerId, Finca> {

        private TenantId tenantId;

        CustomerFincasUnassigner(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        protected List<Finca> findEntities(CustomerId id, TextPageLink pageLink) {
            return fincaDao.findFincasByTenantIdAndCustomerId(tenantId.getId(), id.getId(), pageLink);
        }

        @Override
        protected void removeEntity(Finca entity) {
            unassignFincaFromCustomer(new FincaId(entity.getId().getId()));
        }
    }
}
