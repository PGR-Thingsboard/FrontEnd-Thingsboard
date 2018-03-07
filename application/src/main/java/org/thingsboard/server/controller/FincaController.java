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
import org.thingsboard.server.common.data.finca.Finca;
import org.thingsboard.server.common.data.finca.FincaSearchQuery;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.FincaId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.nosql.FincaEntity;
import org.thingsboard.server.exception.ThingsboardErrorCode;
import org.thingsboard.server.exception.ThingsboardException;
import org.thingsboard.server.service.security.model.SecurityUser;

/**
 *
 * @author German Lopez
 */
@RestController
@RequestMapping("/api")
public class FincaController extends BaseController {

    public static final String FINCA_ID = "fincaId";

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/finca/{fincaId}", method = RequestMethod.GET)
    @ResponseBody
    public Finca getFincaById(@PathVariable(FINCA_ID) String strFincaId) throws ThingsboardException {
        checkParameter(FINCA_ID, strFincaId);
        try {
            FincaId fincaId = new FincaId(toUUID(strFincaId));
            return checkFincaId(fincaId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/finca", method = RequestMethod.POST)
    @ResponseBody
    public Finca saveFinca(@RequestBody Finca finca) throws ThingsboardException {
        try {
            finca.setTenantId(getCurrentUser().getTenantId());
            if (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER) {
                if (finca.getId() == null || finca.getId().isNullUid() ||
                    finca.getCustomerId() == null || finca.getCustomerId().isNullUid()) {
                    throw new ThingsboardException("You don't have permission to perform this operation!",
                            ThingsboardErrorCode.PERMISSION_DENIED);
                } else {
                    checkCustomerId(finca.getCustomerId());
                }
            }
            Finca savedFinca  = checkNotNull(fincaService.saveFinca(finca));

            logEntityAction(savedFinca.getId(), savedFinca,
                    savedFinca.getCustomerId(),
                    finca.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return  savedFinca;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.FINCA), finca,
                    null, finca.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/finca/{fincaId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteFinca(@PathVariable(FINCA_ID) String strFincaId) throws ThingsboardException {
        checkParameter(FINCA_ID, strFincaId);
        try {
            FincaId fincaId = new FincaId(toUUID(strFincaId));
            Finca finca = checkFincaId(fincaId);
            fincaService.deleteFinca(fincaId);

            logEntityAction(fincaId, finca,
                    finca.getCustomerId(),
                    ActionType.DELETED, null, strFincaId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.FINCA),
                    null,
                    null,
                    ActionType.DELETED, e, strFincaId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/finca/{fincaId}", method = RequestMethod.POST)
    @ResponseBody
    public Finca assignFincaToCustomer(@PathVariable("customerId") String strCustomerId,
                                       @PathVariable(FINCA_ID) String strFincaId) throws ThingsboardException {
        checkParameter("customerId", strCustomerId);
        checkParameter(FINCA_ID, strFincaId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);

            FincaId fincaId = new FincaId(toUUID(strFincaId));
            checkFincaId(fincaId);

            Finca savedFinca = checkNotNull(fincaService.assignFincaToCustomer(fincaId, customerId));

            logEntityAction(fincaId, savedFinca,
                    savedFinca.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strFincaId, strCustomerId, customer.getName());

            return  savedFinca;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.FINCA), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strFincaId, strCustomerId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/finca/{fincaId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Finca unassignFincaFromCustomer(@PathVariable(FINCA_ID) String strFincaId) throws ThingsboardException {
        checkParameter(FINCA_ID, strFincaId);
        try {
            FincaId fincaId = new FincaId(toUUID(strFincaId));
            Finca finca = checkFincaId(fincaId);
            if (finca.getCustomerId() == null || finca.getCustomerId().getId().equals(ModelConstants.NULL_UUID)) {
                throw new IncorrectParameterException("Finca isn't assigned to any customer!");
            }

            Customer customer = checkCustomerId(finca.getCustomerId());

            Finca savedFinca = checkNotNull(fincaService.unassignFincaFromCustomer(fincaId));

            logEntityAction(fincaId, finca,
                    finca.getCustomerId(),
                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strFincaId, customer.getId().toString(), customer.getName());

            return savedFinca;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.FINCA), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strFincaId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/finca/{fincaId}", method = RequestMethod.POST)
    @ResponseBody
    public Finca assignFincaToPublicCustomer(@PathVariable(FINCA_ID) String strFincaId) throws ThingsboardException {
        checkParameter(FINCA_ID, strFincaId);
        try {
            FincaId fincaId = new FincaId(toUUID(strFincaId));
            Finca finca = checkFincaId(fincaId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(finca.getTenantId());
            Finca savedFinca = checkNotNull(fincaService.assignFincaToCustomer(fincaId, publicCustomer.getId()));

            logEntityAction(fincaId, savedFinca,
                    savedFinca.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strFincaId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedFinca;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.FINCA), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strFincaId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/fincas", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Finca> getTenantFincas(
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length()>0) {
                return checkNotNull(fincaService.findFincasByTenantIdAndType(tenantId, type, pageLink));
            } else {
                return checkNotNull(fincaService.findFincasByTenantId(tenantId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/fincas", params = {"fincaName"}, method = RequestMethod.GET)
    @ResponseBody
    public Finca getTenantFinca(
            @RequestParam String fincaName) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(fincaService.findFincaByTenantIdAndName(tenantId, fincaName));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/fincas", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Finca> getCustomerFincas(
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
                return checkNotNull(fincaService.findFincasByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink));
            } else {
                return checkNotNull(fincaService.findFincasByTenantIdAndCustomerId(tenantId, customerId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/fincas", params = {"fincaIds"}, method = RequestMethod.GET)
    @ResponseBody
    public List<Finca> getFincasByIds(
            @RequestParam("fincaIds") String[] strFincaIds) throws ThingsboardException {
        checkArrayParameter("fincaIds", strFincaIds);
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            CustomerId customerId = user.getCustomerId();
            List<FincaId> fincaIds = new ArrayList<>();
            for (String strFincaId : strFincaIds) {
                fincaIds.add(new FincaId(toUUID(strFincaId)));
            }
            ListenableFuture<List<Finca>> fincas;
            if (customerId == null || customerId.isNullUid()) {
                fincas = fincaService.findFincasByTenantIdAndIdsAsync(tenantId, fincaIds);
            } else {
                fincas = fincaService.findFincasByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, fincaIds);
            }
            return checkNotNull(fincas.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/fincas", method = RequestMethod.POST)
    @ResponseBody
    public List<Finca> findByQuery(@RequestBody FincaSearchQuery query) throws ThingsboardException {
        checkNotNull(query);
        checkNotNull(query.getParameters());
        checkNotNull(query.getFincaTypes());
        checkEntityId(query.getParameters().getEntityId());
        try {
            List<Finca> fincas = checkNotNull(fincaService.findFincasByQuery(query).get());
            fincas = fincas.stream().filter(finca -> {
                try {
                    checkFinca(finca);
                    return true;
                } catch (ThingsboardException e) {
                    return false;
                }
            }).collect(Collectors.toList());
            return fincas;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/finca/types", method = RequestMethod.GET)
    @ResponseBody
    public List<EntitySubtype> getFincaTypes() throws ThingsboardException {
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            ListenableFuture<List<EntitySubtype>> fincaTypes = fincaService.findFincaTypesByTenantId(tenantId);
            return checkNotNull(fincaTypes.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/Allfincas", method = RequestMethod.GET)
    @ResponseBody
    public List<Finca> getAllFincas() throws ThingsboardException {
        try {
            List<FincaEntity> fincaTypes = fincaService.allFincas().get();
            List<Finca> fincas = new ArrayList<>();
            for(FincaEntity fe : fincaTypes){
                System.out.println("Finca"+fe.toData().toString());
                fincas.add(fe.toData());
            }
            return fincas;
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
