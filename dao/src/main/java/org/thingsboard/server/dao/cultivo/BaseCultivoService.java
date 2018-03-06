/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.cultivo;

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
import org.thingsboard.server.common.data.cultivo.Cultivo;
import org.thingsboard.server.common.data.cultivo.CultivoSearchQuery;
import org.thingsboard.server.common.data.id.CultivoId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityId;
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
public class BaseCultivoService extends AbstractEntityService implements CultivoService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";
    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_CULTIVO_ID = "Incorrect cultivoId ";
    @Autowired
    private CultivoDao cultivoDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;

    @Override
    public Cultivo findCultivoById(CultivoId cultivoId) {
        log.trace("Executing findCultivoById [{}]", cultivoId);
        validateId(cultivoId, INCORRECT_CULTIVO_ID + cultivoId);
        return cultivoDao.findById(cultivoId.getId());
    }

    @Override
    public ListenableFuture<Cultivo> findCultivoByIdAsync(CultivoId cultivoId) {
        log.trace("Executing findCultivoById [{}]", cultivoId);
        validateId(cultivoId, INCORRECT_CULTIVO_ID + cultivoId);
        return cultivoDao.findByIdAsync(cultivoId.getId());
    }

    @Override
    public Optional<Cultivo> findCultivoByTenantIdAndName(TenantId tenantId, String name) {
        log.trace("Executing findCultivoByTenantIdAndName [{}][{}]", tenantId, name);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return cultivoDao.findCultivosByTenantIdAndName(tenantId.getId(), name);
    }

    @Override
    public Cultivo saveCultivo(Cultivo cultivo) {
        log.trace("Executing saveCultivo [{}]", cultivo);
        cultivoValidator.validate(cultivo);
        return cultivoDao.save(cultivo);
    }

    @Override
    public Cultivo assignCultivoToCustomer(CultivoId cultivoId, CustomerId customerId) {
        Cultivo cultivo = findCultivoById(cultivoId);
        cultivo.setCustomerId(customerId);
        return saveCultivo(cultivo);
    }

    @Override
    public Cultivo unassignCultivoFromCustomer(CultivoId cultivoId) {
        Cultivo cultivo = findCultivoById(cultivoId);
        cultivo.setCustomerId(null);
        return saveCultivo(cultivo);
    }

    @Override
    public void deleteCultivo(CultivoId cultivoId) {
        log.trace("Executing deleteCultivo [{}]", cultivoId);
        validateId(cultivoId, INCORRECT_CULTIVO_ID + cultivoId);
        deleteEntityRelations(cultivoId);
        cultivoDao.removeById(cultivoId.getId());
    }

    @Override
    public TextPageData<Cultivo> findCultivosByTenantId(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findCultivosByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Cultivo> cultivos = cultivoDao.findCultivosByTenantId(tenantId.getId(), pageLink);
        return new TextPageData<>(cultivos, pageLink);
    }

    @Override
    public TextPageData<Cultivo> findCultivosByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink) {
        log.trace("Executing findCultivosByTenantIdAndType, tenantId [{}], type [{}], pageLink [{}]", tenantId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Cultivo> cultivos = cultivoDao.findCultivosByTenantIdAndType(tenantId.getId(), type, pageLink);
        return new TextPageData<>(cultivos, pageLink);
    }

    @Override
    public ListenableFuture<List<Cultivo>> findCultivosByTenantIdAndIdsAsync(TenantId tenantId, List<CultivoId> cultivoIds) {
        log.trace("Executing findCultivosByTenantIdAndIdsAsync, tenantId [{}], cultivoIds [{}]", tenantId, cultivoIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateIds(cultivoIds, "Incorrect cultivoIds " + cultivoIds);
        return cultivoDao.findCultivosByTenantIdAndIdsAsync(tenantId.getId(), toUUIDs(cultivoIds));
    }

    @Override
    public void deleteCultivosByTenantId(TenantId tenantId) {
        log.trace("Executing deleteCultivosByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantCultivosRemover.removeEntities(tenantId);
    }

    @Override
    public TextPageData<Cultivo> findCultivosByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink) {
        log.trace("Executing findCultivosByTenantIdAndCustomerId, tenantId [{}], customerId [{}], pageLink [{}]", tenantId, customerId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Cultivo> cultivos = cultivoDao.findCultivosByTenantIdAndCustomerId(tenantId.getId(), customerId.getId(), pageLink);
        return new TextPageData<>(cultivos, pageLink);
    }

    @Override
    public TextPageData<Cultivo> findCultivosByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink) {
        log.trace("Executing findCultivosByTenantIdAndCustomerIdAndType, tenantId [{}], customerId [{}], type [{}], pageLink [{}]", tenantId, customerId, type, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Cultivo> cultivos = cultivoDao.findCultivosByTenantIdAndCustomerIdAndType(tenantId.getId(), customerId.getId(), type, pageLink);
        return new TextPageData<>(cultivos, pageLink);
    }

    @Override
    public ListenableFuture<List<Cultivo>> findCultivosByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<CultivoId> cultivoIds) {
        log.trace("Executing findCultivosByTenantIdAndCustomerIdAndIdsAsync, tenantId [{}], customerId [{}], cultivoIds [{}]", tenantId, customerId, cultivoIds);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateIds(cultivoIds, "Incorrect cultivoIds " + cultivoIds);
        return cultivoDao.findCultivosByTenantIdAndCustomerIdAndIdsAsync(tenantId.getId(), customerId.getId(), toUUIDs(cultivoIds));
    }

    @Override
    public void unassignCustomerCultivos(TenantId tenantId, CustomerId customerId) {
        log.trace("Executing unassignCustomerCultivos, tenantId [{}], customerId [{}]", tenantId, customerId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        new CustomerCultivosUnassigner(tenantId).removeEntities(customerId);
    }

    @Override
    public ListenableFuture<List<Cultivo>> findCultivosByQuery(CultivoSearchQuery query) {
        ListenableFuture<List<EntityRelation>> relations = relationService.findByQuery(query.toEntitySearchQuery());
        ListenableFuture<List<Cultivo>> cultivos = Futures.transform(relations, (AsyncFunction<List<EntityRelation>, List<Cultivo>>) relations1 -> {
            EntitySearchDirection direction = query.toEntitySearchQuery().getParameters().getDirection();
            List<ListenableFuture<Cultivo>> futures = new ArrayList<>();
            for (EntityRelation relation : relations1) {
                EntityId entityId = direction == EntitySearchDirection.FROM ? relation.getTo() : relation.getFrom();
                if (entityId.getEntityType() == EntityType.CULTIVO) {
                    futures.add(findCultivoByIdAsync(new CultivoId(entityId.getId())));
                }
            }
            return Futures.successfulAsList(futures);
        });
        cultivos = Futures.transform(cultivos, (Function<List<Cultivo>, List<Cultivo>>)cultivoList ->
            cultivoList == null ? Collections.emptyList() : cultivoList.stream().filter(cultivo -> query.getCultivoTypes().contains(cultivo.getType())).collect(Collectors.toList())
        );
        return cultivos;
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findCultivoTypesByTenantId(TenantId tenantId) {
        log.trace("Executing findCultivoTypesByTenantId, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        ListenableFuture<List<EntitySubtype>> tenantCultivoTypes = cultivoDao.findTenantCultivoTypesAsync(tenantId.getId());
        return Futures.transform(tenantCultivoTypes,
                (Function<List<EntitySubtype>, List<EntitySubtype>>) cultivoTypes -> {
                    cultivoTypes.sort(Comparator.comparing(EntitySubtype::getType));
                    return cultivoTypes;
                });
    }

    private DataValidator<Cultivo> cultivoValidator =
            new DataValidator<Cultivo>() {

                @Override
                protected void validateCreate(Cultivo cultivo) {
                    cultivoDao.findCultivosByTenantIdAndName(cultivo.getTenantId().getId(), cultivo.getName()).ifPresent(
                            d -> {
                                throw new DataValidationException("Cultivo with such name already exists!");
                            }
                    );
                }

                @Override
                protected void validateUpdate(Cultivo cultivo) {
                    cultivoDao.findCultivosByTenantIdAndName(cultivo.getTenantId().getId(), cultivo.getName()).ifPresent(
                            d -> {
                                if (!d.getId().equals(cultivo.getId())) {
                                    throw new DataValidationException("Cultivo with such name already exists!");
                                }
                            }
                    );
                }

                @Override
                protected void validateDataImpl(Cultivo cultivo) {
                    if (StringUtils.isEmpty(cultivo.getType())) {
                        throw new DataValidationException("Cultivo type should be specified!");
                    }
                    if (StringUtils.isEmpty(cultivo.getName())) {
                        throw new DataValidationException("Cultivo name should be specified!");
                    }
                    if (cultivo.getTenantId() == null) {
                        throw new DataValidationException("Cultivo should be assigned to tenant!");
                    } else {
                        Tenant tenant = tenantDao.findById(cultivo.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Cultivo is referencing to non-existent tenant!");
                        }
                    }
                    if (cultivo.getCustomerId() == null) {
                        cultivo.setCustomerId(new CustomerId(NULL_UUID));
                    } else if (!cultivo.getCustomerId().getId().equals(NULL_UUID)) {
                        Customer customer = customerDao.findById(cultivo.getCustomerId().getId());
                        if (customer == null) {
                            throw new DataValidationException("Can't assign cultivo to non-existent customer!");
                        }
                        if (!customer.getTenantId().equals(cultivo.getTenantId())) {
                            throw new DataValidationException("Can't assign cultivo to customer from different tenant!");
                        }
                    }
                }
            };

    private PaginatedRemover<TenantId, Cultivo> tenantCultivosRemover =
            new PaginatedRemover<TenantId, Cultivo>() {

                @Override
                protected List<Cultivo> findEntities(TenantId id, TextPageLink pageLink) {
                    return cultivoDao.findCultivosByTenantId(id.getId(), pageLink);
                }

                @Override
                protected void removeEntity(Cultivo entity) {
                    deleteCultivo(new CultivoId(entity.getId().getId()));
                }
            };

    class CustomerCultivosUnassigner extends PaginatedRemover<CustomerId, Cultivo> {

        private TenantId tenantId;

        CustomerCultivosUnassigner(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        protected List<Cultivo> findEntities(CustomerId id, TextPageLink pageLink) {
            return cultivoDao.findCultivosByTenantIdAndCustomerId(tenantId.getId(), id.getId(), pageLink);
        }

        @Override
        protected void removeEntity(Cultivo entity) {
            unassignCultivoFromCustomer(new CultivoId(entity.getId().getId()));
        }
    }
}
