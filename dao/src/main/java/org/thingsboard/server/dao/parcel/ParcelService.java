/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.parcel;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.parcel.Parcel;
import org.thingsboard.server.common.data.parcel.ParcelSearchQuery;
import org.thingsboard.server.common.data.id.ParcelId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.FarmId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.model.nosql.ParcelEntity;

/**
 *
 * @author German Lopez
 */
public interface ParcelService {

    Parcel findParcelById(ParcelId parcelId);

    ListenableFuture<Parcel> findParcelByIdAsync(ParcelId parcelId);

    Optional<Parcel> findParcelByTenantIdAndName(TenantId tenantId, String name);

    Parcel saveParcel(Parcel parcel);

    Parcel assignParcelToCustomer(ParcelId parcelId, CustomerId customerId);

    Parcel unassignParcelFromCustomer(ParcelId parcelId);

    void deleteParcel(ParcelId parcelId);

    TextPageData<Parcel> findParcelsByTenantId(TenantId tenantId, TextPageLink pageLink);

    TextPageData<Parcel> findParcelsByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink);

    ListenableFuture<List<Parcel>> findParcelsByTenantIdAndIdsAsync(TenantId tenantId, List<ParcelId> parcelIds);

    void deleteParcelsByTenantId(TenantId tenantId);

    TextPageData<Parcel> findParcelsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink);

    TextPageData<Parcel> findParcelsByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink);

    ListenableFuture<List<Parcel>> findParcelsByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<ParcelId> parcelIds);

    void unassignCustomerParcels(TenantId tenantId, CustomerId customerId);

    ListenableFuture<List<Parcel>> findParcelsByQuery(ParcelSearchQuery query);

    ListenableFuture<List<EntitySubtype>> findParcelTypesByTenantId(TenantId tenantId);
    
    ListenableFuture<List<ParcelEntity>> findParcelsByFarmId(String farmId);
    
    ListenableFuture<List<ParcelEntity>> allParcels();
}