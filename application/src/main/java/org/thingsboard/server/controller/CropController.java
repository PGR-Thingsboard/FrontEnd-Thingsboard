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
import org.thingsboard.server.common.data.crop.Crop;
import org.thingsboard.server.common.data.crop.CropSearchQuery;
import org.thingsboard.server.common.data.id.CropId;
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
public class CropController extends BaseController{
    
    public static final String CROP_ID = "cropId";

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/crop/{cropId}", method = RequestMethod.GET)
    @ResponseBody
    public Crop getCropById(@PathVariable(CROP_ID) String strCropId) throws ThingsboardException {
        checkParameter(CROP_ID, strCropId);
        try {
            CropId cropId = new CropId(toUUID(strCropId));
            return checkCropId(cropId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/crop", method = RequestMethod.POST)
    @ResponseBody
    public Crop saveCrop(@RequestBody Crop crop) throws ThingsboardException {
        try {
            crop.setTenantId(getCurrentUser().getTenantId());
            if (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER) {
                if (crop.getId() == null || crop.getId().isNullUid() ||
                    crop.getCustomerId() == null || crop.getCustomerId().isNullUid()) {
                    throw new ThingsboardException("You don't have permission to perform this operation!",
                            ThingsboardErrorCode.PERMISSION_DENIED);
                } else {
                    checkCustomerId(crop.getCustomerId());
                }
            }
            Crop savedCrop  = checkNotNull(cropService.saveCrop(crop));

            logEntityAction(savedCrop.getId(), savedCrop,
                    savedCrop.getCustomerId(),
                    crop.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return  savedCrop;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.CROP), crop,
                    null, crop.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/crop/{cropId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteCrop(@PathVariable(CROP_ID) String strCropId) throws ThingsboardException {
        checkParameter(CROP_ID, strCropId);
        try {
            CropId cropId = new CropId(toUUID(strCropId));
            Crop crop = checkCropId(cropId);
            cropService.deleteCrop(cropId);

            logEntityAction(cropId, crop,
                    crop.getCustomerId(),
                    ActionType.DELETED, null, strCropId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.CROP),
                    null,
                    null,
                    ActionType.DELETED, e, strCropId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/crop/{cropId}", method = RequestMethod.POST)
    @ResponseBody
    public Crop assignCropToCustomer(@PathVariable("customerId") String strCustomerId,
                                       @PathVariable(CROP_ID) String strCropId) throws ThingsboardException {
        checkParameter("customerId", strCustomerId);
        checkParameter(CROP_ID, strCropId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);

            CropId cropId = new CropId(toUUID(strCropId));
            checkCropId(cropId);

            Crop savedCrop = checkNotNull(cropService.assignCropToCustomer(cropId, customerId));

            logEntityAction(cropId, savedCrop,
                    savedCrop.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strCropId, strCustomerId, customer.getName());

            return  savedCrop;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CROP), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strCropId, strCustomerId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/crop/{cropId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Crop unassignCropFromCustomer(@PathVariable(CROP_ID) String strCropId) throws ThingsboardException {
        checkParameter(CROP_ID, strCropId);
        try {
            CropId cropId = new CropId(toUUID(strCropId));
            Crop crop = checkCropId(cropId);
            if (crop.getCustomerId() == null || crop.getCustomerId().getId().equals(ModelConstants.NULL_UUID)) {
                throw new IncorrectParameterException("Crop isn't assigned to any customer!");
            }

            Customer customer = checkCustomerId(crop.getCustomerId());

            Crop savedCrop = checkNotNull(cropService.unassignCropFromCustomer(cropId));

            logEntityAction(cropId, crop,
                    crop.getCustomerId(),
                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strCropId, customer.getId().toString(), customer.getName());

            return savedCrop;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CROP), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strCropId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/crop/{cropId}", method = RequestMethod.POST)
    @ResponseBody
    public Crop assignCropToPublicCustomer(@PathVariable(CROP_ID) String strCropId) throws ThingsboardException {
        checkParameter(CROP_ID, strCropId);
        try {
            CropId cropId = new CropId(toUUID(strCropId));
            Crop crop = checkCropId(cropId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(crop.getTenantId());
            Crop savedCrop = checkNotNull(cropService.assignCropToCustomer(cropId, publicCustomer.getId()));

            logEntityAction(cropId, savedCrop,
                    savedCrop.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strCropId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedCrop;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CROP), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strCropId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/crops", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Crop> getTenantCrops(
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length()>0) {
                return checkNotNull(cropService.findCropsByTenantIdAndType(tenantId, type, pageLink));
            } else {
                return checkNotNull(cropService.findCropsByTenantId(tenantId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/crops", params = {"cropName"}, method = RequestMethod.GET)
    @ResponseBody
    public Crop getTenantCrop(
            @RequestParam String cropName) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(cropService.findCropByTenantIdAndName(tenantId, cropName));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/crops", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Crop> getCustomerCrops(
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
                return checkNotNull(cropService.findCropsByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink));
            } else {
                return checkNotNull(cropService.findCropsByTenantIdAndCustomerId(tenantId, customerId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/crops", params = {"cropIds"}, method = RequestMethod.GET)
    @ResponseBody
    public List<Crop> getCropsByIds(
            @RequestParam("cropIds") String[] strCropIds) throws ThingsboardException {
        checkArrayParameter("cropIds", strCropIds);
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            CustomerId customerId = user.getCustomerId();
            List<CropId> cropIds = new ArrayList<>();
            for (String strCropId : strCropIds) {
                cropIds.add(new CropId(toUUID(strCropId)));
            }
            ListenableFuture<List<Crop>> crops;
            if (customerId == null || customerId.isNullUid()) {
                crops = cropService.findCropsByTenantIdAndIdsAsync(tenantId, cropIds);
            } else {
                crops = cropService.findCropsByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, cropIds);
            }
            return checkNotNull(crops.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/crops", method = RequestMethod.POST)
    @ResponseBody
    public List<Crop> findByQuery(@RequestBody CropSearchQuery query) throws ThingsboardException {
        checkNotNull(query);
        checkNotNull(query.getParameters());
        checkNotNull(query.getCropTypes());
        checkEntityId(query.getParameters().getEntityId());
        try {
            List<Crop> crops = checkNotNull(cropService.findCropsByQuery(query).get());
            crops = crops.stream().filter(crop -> {
                try {
                    checkCrop(crop);
                    return true;
                } catch (ThingsboardException e) {
                    return false;
                }
            }).collect(Collectors.toList());
            return crops;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/crop/types", method = RequestMethod.GET)
    @ResponseBody
    public List<EntitySubtype> getCropTypes() throws ThingsboardException {
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            ListenableFuture<List<EntitySubtype>> cropTypes = cropService.findCropTypesByTenantId(tenantId);
            return checkNotNull(cropTypes.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    
}
