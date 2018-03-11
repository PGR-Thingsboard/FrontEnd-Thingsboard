/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.crop;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.crop.Crop;
import org.thingsboard.server.common.data.crop.CropSearchQuery;
import org.thingsboard.server.common.data.id.CropId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;

/**
 *
 * @author German Lopez
 */
public interface CropService {

    Crop findCropById(CropId cropId);

    ListenableFuture<Crop> findCropByIdAsync(CropId cropId);

    Optional<Crop> findCropByTenantIdAndName(TenantId tenantId, String name);

    Crop saveCrop(Crop crop);

    Crop assignCropToCustomer(CropId cropId, CustomerId customerId);

    Crop unassignCropFromCustomer(CropId cropId);

    void deleteCrop(CropId cropId);

    TextPageData<Crop> findCropsByTenantId(TenantId tenantId, TextPageLink pageLink);

    TextPageData<Crop> findCropsByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink);

    ListenableFuture<List<Crop>> findCropsByTenantIdAndIdsAsync(TenantId tenantId, List<CropId> cropIds);

    void deleteCropsByTenantId(TenantId tenantId);

    TextPageData<Crop> findCropsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink);

    TextPageData<Crop> findCropsByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink);

    ListenableFuture<List<Crop>> findCropsByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<CropId> cropIds);

    void unassignCustomerCrops(TenantId tenantId, CustomerId customerId);

    ListenableFuture<List<Crop>> findCropsByQuery(CropSearchQuery query);

    ListenableFuture<List<EntitySubtype>> findCropTypesByTenantId(TenantId tenantId);
}