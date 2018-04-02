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
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.Dao;
import org.thingsboard.server.dao.model.nosql.ParcelEntity;

/**
 *
 * @German Lopez
 */
public interface ParcelDao extends Dao<Parcel> {

    /**
     * Save or update parcel object
     *
     * @param parcel the parcel object
     * @return saved parcel object
     */
    Parcel save(Parcel parcel);

    /**
     * Find parcels by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of parcel objects
     */
    List<Parcel> findParcelsByTenantId(UUID tenantId, TextPageLink pageLink);

    /**
     * Find parcels by tenantId, type and page link.
     *
     * @param tenantId the tenantId
     * @param type the type
     * @param pageLink the page link
     * @return the list of parcel objects
     */
    List<Parcel> findParcelsByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink);

    /**
     * Find parcels by tenantId and parcels Ids.
     *
     * @param tenantId the tenantId
     * @param parcelIds the parcel Ids
     * @return the list of parcel objects
     */
    ListenableFuture<List<Parcel>> findParcelsByTenantIdAndIdsAsync(UUID tenantId, List<UUID> parcelIds);

    /**
     * Find parcels by tenantId, customerId and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param pageLink the page link
     * @return the list of parcel objects
     */
    List<Parcel> findParcelsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink);

    /**
     * Find parcels by tenantId, customerId, type and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param type the type
     * @param pageLink the page link
     * @return the list of parcel objects
     */
    List<Parcel> findParcelsByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink);

    /**
     * Find parcels by tenantId, customerId and parcels Ids.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param parcelIds the parcel Ids
     * @return the list of parcel objects
     */
    ListenableFuture<List<Parcel>> findParcelsByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> parcelIds);

    /**
     * Find parcels by tenantId and parcel name.
     *
     * @param tenantId the tenantId
     * @param name the parcel name
     * @return the optional parcel object
     */
    Optional<Parcel> findParcelsByTenantIdAndName(UUID tenantId, String name);

    /**
     * Find tenants parcel types.
     *
     * @return the list of tenant parcel type objects
     */
    ListenableFuture<List<EntitySubtype>> findTenantParcelTypesAsync(UUID tenantId);
    
    /**
     * Find all parcels by farm id
     * @param farmId
     * @return 
     */
    ListenableFuture<List<ParcelEntity>> findParcelsByFarmId(String farmId);
    
    /**
     * Return all parcels
     * @return 
     */
    ListenableFuture<List<ParcelEntity>> allParcels();

}