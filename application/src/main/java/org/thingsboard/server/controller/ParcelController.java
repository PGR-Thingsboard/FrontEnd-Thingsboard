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
import org.thingsboard.server.common.data.SpatialParcel;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.parcel.Parcel;
import org.thingsboard.server.common.data.parcel.ParcelSearchQuery;
import org.thingsboard.server.common.data.id.ParcelId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.nosql.ParcelEntity;
import org.thingsboard.server.exception.ThingsboardErrorCode;
import org.thingsboard.server.exception.ThingsboardException;
import org.thingsboard.server.service.security.model.SecurityUser;

/**
 *
 * @author German Lopez
 */

@RestController
@RequestMapping("/api")
public class ParcelController extends BaseController{
    
    public static final String PARCEL_ID = "parcelId";

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/parcel/{parcelId}", method = RequestMethod.GET)
    @ResponseBody
    public Parcel getParcelById(@PathVariable(PARCEL_ID) String strParcelId) throws ThingsboardException {
        checkParameter(PARCEL_ID, strParcelId);
        try {
            ParcelId parcelId = new ParcelId(toUUID(strParcelId));
            return checkParcelId(parcelId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/parcel", method = RequestMethod.POST)
    @ResponseBody
    public Parcel saveParcel(@RequestBody Parcel parcel) throws ThingsboardException {
        try {
            parcel.setTenantId(getCurrentUser().getTenantId());
            if (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER) {
                if (parcel.getId() == null || parcel.getId().isNullUid() ||
                    parcel.getCustomerId() == null || parcel.getCustomerId().isNullUid()) {
                    throw new ThingsboardException("You don't have permission to perform this operation!",
                            ThingsboardErrorCode.PERMISSION_DENIED);
                } else {
                    checkCustomerId(parcel.getCustomerId());
                }
            }
            Parcel savedParcel  = checkNotNull(parcelService.saveParcel(parcel));
            SpatialParcel spatialParcel = new SpatialParcel(savedParcel.getId().getId().toString(),savedParcel.getFarmId(),parcel.getLocation());
            mongoService.getMongodbparcel().save(spatialParcel);
            logEntityAction(savedParcel.getId(), savedParcel,
                    savedParcel.getCustomerId(),
                    parcel.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return  savedParcel;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.PARCEL), parcel,
                    null, parcel.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/parcel/{parcelId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteParcel(@PathVariable(PARCEL_ID) String strParcelId) throws ThingsboardException {
        checkParameter(PARCEL_ID, strParcelId);
        try {
            ParcelId parcelId = new ParcelId(toUUID(strParcelId));
            Parcel parcel = checkParcelId(parcelId);
            parcelService.deleteParcel(parcelId);

            logEntityAction(parcelId, parcel,
                    parcel.getCustomerId(),
                    ActionType.DELETED, null, strParcelId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.PARCEL),
                    null,
                    null,
                    ActionType.DELETED, e, strParcelId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/parcel/{parcelId}", method = RequestMethod.POST)
    @ResponseBody
    public Parcel assignParcelToCustomer(@PathVariable("customerId") String strCustomerId,
                                       @PathVariable(PARCEL_ID) String strParcelId) throws ThingsboardException {
        checkParameter("customerId", strCustomerId);
        checkParameter(PARCEL_ID, strParcelId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);

            ParcelId parcelId = new ParcelId(toUUID(strParcelId));
            checkParcelId(parcelId);

            Parcel savedParcel = checkNotNull(parcelService.assignParcelToCustomer(parcelId, customerId));

            logEntityAction(parcelId, savedParcel,
                    savedParcel.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strParcelId, strCustomerId, customer.getName());

            return  savedParcel;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.PARCEL), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strParcelId, strCustomerId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/parcel/{parcelId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Parcel unassignParcelFromCustomer(@PathVariable(PARCEL_ID) String strParcelId) throws ThingsboardException {
        checkParameter(PARCEL_ID, strParcelId);
        try {
            ParcelId parcelId = new ParcelId(toUUID(strParcelId));
            Parcel parcel = checkParcelId(parcelId);
            if (parcel.getCustomerId() == null || parcel.getCustomerId().getId().equals(ModelConstants.NULL_UUID)) {
                throw new IncorrectParameterException("Parcel isn't assigned to any customer!");
            }

            Customer customer = checkCustomerId(parcel.getCustomerId());

            Parcel savedParcel = checkNotNull(parcelService.unassignParcelFromCustomer(parcelId));

            logEntityAction(parcelId, parcel,
                    parcel.getCustomerId(),
                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strParcelId, customer.getId().toString(), customer.getName());

            return savedParcel;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.PARCEL), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strParcelId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/parcel/{parcelId}", method = RequestMethod.POST)
    @ResponseBody
    public Parcel assignParcelToPublicCustomer(@PathVariable(PARCEL_ID) String strParcelId) throws ThingsboardException {
        checkParameter(PARCEL_ID, strParcelId);
        try {
            ParcelId parcelId = new ParcelId(toUUID(strParcelId));
            Parcel parcel = checkParcelId(parcelId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(parcel.getTenantId());
            Parcel savedParcel = checkNotNull(parcelService.assignParcelToCustomer(parcelId, publicCustomer.getId()));

            logEntityAction(parcelId, savedParcel,
                    savedParcel.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strParcelId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedParcel;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.PARCEL), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strParcelId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/parcels", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Parcel> getTenantParcels(
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length()>0) {
                return checkNotNull(parcelService.findParcelsByTenantIdAndType(tenantId, type, pageLink));
            } else {
                return checkNotNull(parcelService.findParcelsByTenantId(tenantId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/parcels", params = {"parcelName"}, method = RequestMethod.GET)
    @ResponseBody
    public Parcel getTenantParcel(
            @RequestParam String parcelName) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(parcelService.findParcelByTenantIdAndName(tenantId, parcelName));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/parcels", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Parcel> getCustomerParcels(
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
                return checkNotNull(parcelService.findParcelsByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink));
            } else {
                return checkNotNull(parcelService.findParcelsByTenantIdAndCustomerId(tenantId, customerId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/parcels", params = {"parcelIds"}, method = RequestMethod.GET)
    @ResponseBody
    public List<Parcel> getParcelsByIds(
            @RequestParam("parcelIds") String[] strParcelIds) throws ThingsboardException {
        checkArrayParameter("parcelIds", strParcelIds);
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            CustomerId customerId = user.getCustomerId();
            List<ParcelId> parcelIds = new ArrayList<>();
            for (String strParcelId : strParcelIds) {
                parcelIds.add(new ParcelId(toUUID(strParcelId)));
            }
            ListenableFuture<List<Parcel>> parcels;
            if (customerId == null || customerId.isNullUid()) {
                parcels = parcelService.findParcelsByTenantIdAndIdsAsync(tenantId, parcelIds);
            } else {
                parcels = parcelService.findParcelsByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, parcelIds);
            }
            return checkNotNull(parcels.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/parcels", method = RequestMethod.POST)
    @ResponseBody
    public List<Parcel> findByQuery(@RequestBody ParcelSearchQuery query) throws ThingsboardException {
        checkNotNull(query);
        checkNotNull(query.getParameters());
        checkNotNull(query.getParcelTypes());
        checkEntityId(query.getParameters().getEntityId());
        try {
            List<Parcel> parcels = checkNotNull(parcelService.findParcelsByQuery(query).get());
            parcels = parcels.stream().filter(parcel -> {
                try {
                    checkParcel(parcel);
                    return true;
                } catch (ThingsboardException e) {
                    return false;
                }
            }).collect(Collectors.toList());
            return parcels;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/parcel/types", method = RequestMethod.GET)
    @ResponseBody
    public List<EntitySubtype> getParcelTypes() throws ThingsboardException {
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            ListenableFuture<List<EntitySubtype>> parcelTypes = parcelService.findParcelTypesByTenantId(tenantId);
            return checkNotNull(parcelTypes.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/Allparcels", method = RequestMethod.GET)
    @ResponseBody
    public List<Parcel> getAllParcels() throws ThingsboardException {
        try {
            List<ParcelEntity> parcelTypes = parcelService.allParcels().get();
            List<Parcel> parcels = new ArrayList<>();
            for(ParcelEntity fe : parcelTypes){
                parcels.add(fe.toData());
            }
            return parcels;
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    
}
