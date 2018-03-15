/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.crop;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.crop.Crop;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.Dao;
import org.thingsboard.server.dao.model.nosql.CropEntity;

/**
 *
 * @German Lopez
 */
public interface CropDao extends Dao<Crop> {

    /**
     * Save or update crop object
     *
     * @param crop the crop object
     * @return saved crop object
     */
    Crop save(Crop crop);

    /**
     * Find crops by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of crop objects
     */
    List<Crop> findCropsByTenantId(UUID tenantId, TextPageLink pageLink);

    /**
     * Find crops by tenantId, type and page link.
     *
     * @param tenantId the tenantId
     * @param type the type
     * @param pageLink the page link
     * @return the list of crop objects
     */
    List<Crop> findCropsByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink);

    /**
     * Find crops by tenantId and crops Ids.
     *
     * @param tenantId the tenantId
     * @param cropIds the crop Ids
     * @return the list of crop objects
     */
    ListenableFuture<List<Crop>> findCropsByTenantIdAndIdsAsync(UUID tenantId, List<UUID> cropIds);

    /**
     * Find crops by tenantId, customerId and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param pageLink the page link
     * @return the list of crop objects
     */
    List<Crop> findCropsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink);

    /**
     * Find crops by tenantId, customerId, type and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param type the type
     * @param pageLink the page link
     * @return the list of crop objects
     */
    List<Crop> findCropsByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink);

    /**
     * Find crops by tenantId, customerId and crops Ids.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param cropIds the crop Ids
     * @return the list of crop objects
     */
    ListenableFuture<List<Crop>> findCropsByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> cropIds);

    /**
     * Find crops by tenantId and crop name.
     *
     * @param tenantId the tenantId
     * @param name the crop name
     * @return the optional crop object
     */
    Optional<Crop> findCropsByTenantIdAndName(UUID tenantId, String name);

    /**
     * Find tenants crop types.
     *
     * @return the list of tenant crop type objects
     */
    ListenableFuture<List<EntitySubtype>> findTenantCropTypesAsync(UUID tenantId);
    
    /**
     * Find all crops by farm id
     * @param farmId
     * @return 
     */
    ListenableFuture<List<CropEntity>> findCropsByFarmId(String farmId);
    
    /**
     * Return all crops
     * @return 
     */
    ListenableFuture<List<CropEntity>> allCrops();

}