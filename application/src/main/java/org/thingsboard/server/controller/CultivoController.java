/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.controller;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.cultivo.Cultivo;
import org.thingsboard.server.common.data.cultivo.CultivoSearchQuery;
import org.thingsboard.server.common.data.id.CultivoId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.exception.ThingsboardErrorCode;
import org.thingsboard.server.exception.ThingsboardException;
import org.thingsboard.server.service.security.model.SecurityUser;

/**
 *
 * @author German Lopez
 */

@RestController
@RequestMapping("/api")
public class CultivoController extends BaseController{
    
    public static final String CULTIVO_ID = "cultivoId";

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/cultivo/{cultivoId}", method = RequestMethod.GET)
    @ResponseBody
    public Cultivo getCultivoById(@PathVariable(CULTIVO_ID) String strCultivoId) throws ThingsboardException {
        checkParameter(CULTIVO_ID, strCultivoId);
        try {
            CultivoId cultivoId = new CultivoId(toUUID(strCultivoId));
            return checkCultivoId(cultivoId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/cultivo", method = RequestMethod.POST)
    @ResponseBody
    public Cultivo saveCultivo(@RequestBody Cultivo cultivo) throws ThingsboardException {
        try {
            cultivo.setTenantId(getCurrentUser().getTenantId());
            if (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER) {
                if (cultivo.getId() == null || cultivo.getId().isNullUid() ||
                    cultivo.getCustomerId() == null || cultivo.getCustomerId().isNullUid()) {
                    throw new ThingsboardException("You don't have permission to perform this operation!",
                            ThingsboardErrorCode.PERMISSION_DENIED);
                } else {
                    checkCustomerId(cultivo.getCustomerId());
                }
            }
            Cultivo savedCultivo  = checkNotNull(cultivoService.saveCultivo(cultivo));

            logEntityAction(savedCultivo.getId(), savedCultivo,
                    savedCultivo.getCustomerId(),
                    cultivo.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return  savedCultivo;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.CULTIVO), cultivo,
                    null, cultivo.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/cultivo/{cultivoId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteCultivo(@PathVariable(CULTIVO_ID) String strCultivoId) throws ThingsboardException {
        checkParameter(CULTIVO_ID, strCultivoId);
        try {
            CultivoId cultivoId = new CultivoId(toUUID(strCultivoId));
            Cultivo cultivo = checkCultivoId(cultivoId);
            cultivoService.deleteCultivo(cultivoId);

            logEntityAction(cultivoId, cultivo,
                    cultivo.getCustomerId(),
                    ActionType.DELETED, null, strCultivoId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.CULTIVO),
                    null,
                    null,
                    ActionType.DELETED, e, strCultivoId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/cultivo/{cultivoId}", method = RequestMethod.POST)
    @ResponseBody
    public Cultivo assignCultivoToCustomer(@PathVariable("customerId") String strCustomerId,
                                       @PathVariable(CULTIVO_ID) String strCultivoId) throws ThingsboardException {
        checkParameter("customerId", strCustomerId);
        checkParameter(CULTIVO_ID, strCultivoId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);

            CultivoId cultivoId = new CultivoId(toUUID(strCultivoId));
            checkCultivoId(cultivoId);

            Cultivo savedCultivo = checkNotNull(cultivoService.assignCultivoToCustomer(cultivoId, customerId));

            logEntityAction(cultivoId, savedCultivo,
                    savedCultivo.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strCultivoId, strCustomerId, customer.getName());

            return  savedCultivo;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CULTIVO), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strCultivoId, strCustomerId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/cultivo/{cultivoId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Cultivo unassignCultivoFromCustomer(@PathVariable(CULTIVO_ID) String strCultivoId) throws ThingsboardException {
        checkParameter(CULTIVO_ID, strCultivoId);
        try {
            CultivoId cultivoId = new CultivoId(toUUID(strCultivoId));
            Cultivo cultivo = checkCultivoId(cultivoId);
            if (cultivo.getCustomerId() == null || cultivo.getCustomerId().getId().equals(ModelConstants.NULL_UUID)) {
                throw new IncorrectParameterException("Cultivo isn't assigned to any customer!");
            }

            Customer customer = checkCustomerId(cultivo.getCustomerId());

            Cultivo savedCultivo = checkNotNull(cultivoService.unassignCultivoFromCustomer(cultivoId));

            logEntityAction(cultivoId, cultivo,
                    cultivo.getCustomerId(),
                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strCultivoId, customer.getId().toString(), customer.getName());

            return savedCultivo;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CULTIVO), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strCultivoId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/cultivo/{cultivoId}", method = RequestMethod.POST)
    @ResponseBody
    public Cultivo assignCultivoToPublicCustomer(@PathVariable(CULTIVO_ID) String strCultivoId) throws ThingsboardException {
        checkParameter(CULTIVO_ID, strCultivoId);
        try {
            CultivoId cultivoId = new CultivoId(toUUID(strCultivoId));
            Cultivo cultivo = checkCultivoId(cultivoId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(cultivo.getTenantId());
            Cultivo savedCultivo = checkNotNull(cultivoService.assignCultivoToCustomer(cultivoId, publicCustomer.getId()));

            logEntityAction(cultivoId, savedCultivo,
                    savedCultivo.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strCultivoId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedCultivo;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CULTIVO), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strCultivoId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/cultivos", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Cultivo> getTenantCultivos(
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length()>0) {
                return checkNotNull(cultivoService.findCultivosByTenantIdAndType(tenantId, type, pageLink));
            } else {
                return checkNotNull(cultivoService.findCultivosByTenantId(tenantId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/cultivos", params = {"cultivoName"}, method = RequestMethod.GET)
    @ResponseBody
    public Cultivo getTenantCultivo(
            @RequestParam String cultivoName) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(cultivoService.findCultivoByTenantIdAndName(tenantId, cultivoName));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/cultivos", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Cultivo> getCustomerCultivos(
            @PathVariable("customerId") String strCustomerId,
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        checkParameter("customerId", strCustomerId);
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            checkCustomerId(customerId);
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length()>0) {
                return checkNotNull(cultivoService.findCultivosByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink));
            } else {
                return checkNotNull(cultivoService.findCultivosByTenantIdAndCustomerId(tenantId, customerId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/cultivos", params = {"cultivoIds"}, method = RequestMethod.GET)
    @ResponseBody
    public List<Cultivo> getCultivosByIds(
            @RequestParam("cultivoIds") String[] strCultivoIds) throws ThingsboardException {
        checkArrayParameter("cultivoIds", strCultivoIds);
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            CustomerId customerId = user.getCustomerId();
            List<CultivoId> cultivoIds = new ArrayList<>();
            for (String strCultivoId : strCultivoIds) {
                cultivoIds.add(new CultivoId(toUUID(strCultivoId)));
            }
            ListenableFuture<List<Cultivo>> cultivos;
            if (customerId == null || customerId.isNullUid()) {
                cultivos = cultivoService.findCultivosByTenantIdAndIdsAsync(tenantId, cultivoIds);
            } else {
                cultivos = cultivoService.findCultivosByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, cultivoIds);
            }
            return checkNotNull(cultivos.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/cultivos", method = RequestMethod.POST)
    @ResponseBody
    public List<Cultivo> findByQuery(@RequestBody CultivoSearchQuery query) throws ThingsboardException {
        checkNotNull(query);
        checkNotNull(query.getParameters());
        checkNotNull(query.getCultivoTypes());
        checkEntityId(query.getParameters().getEntityId());
        try {
            List<Cultivo> cultivos = checkNotNull(cultivoService.findCultivosByQuery(query).get());
            cultivos = cultivos.stream().filter(cultivo -> {
                try {
                    checkCultivo(cultivo);
                    return true;
                } catch (ThingsboardException e) {
                    return false;
                }
            }).collect(Collectors.toList());
            return cultivos;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/cultivo/types", method = RequestMethod.GET)
    @ResponseBody
    public List<EntitySubtype> getCultivoTypes() throws ThingsboardException {
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            ListenableFuture<List<EntitySubtype>> cultivoTypes = cultivoService.findCultivoTypesByTenantId(tenantId);
            return checkNotNull(cultivoTypes.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    
}
