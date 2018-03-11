/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.farm;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.farm.Farm;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.Dao;
import org.thingsboard.server.dao.model.nosql.FarmEntity;

/**
 *
 * @author German Lopez
 */
public interface FarmDao extends Dao<Farm> {

    /**
     * Save or update farm object
     *
     * @param farm the farm object
     * @return saved farm object
     */
    Farm save(Farm farm);
    
    /**
     * Return all farms
     */
    ListenableFuture<List<FarmEntity>> allFarms();

    /**
     * Find farms by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of farm objects
     */
    List<Farm> findFarmsByTenantId(UUID tenantId, TextPageLink pageLink);

    /**
     * Find farms by tenantId, type and page link.
     *
     * @param tenantId the tenantId
     * @param type the type
     * @param pageLink the page link
     * @return the list of farm objects
     */
    List<Farm> findFarmsByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink);

    /**
     * Find farms by tenantId and farms Ids.
     *
     * @param tenantId the tenantId
     * @param farmIds the farm Ids
     * @return the list of farm objects
     */
    ListenableFuture<List<Farm>> findFarmsByTenantIdAndIdsAsync(UUID tenantId, List<UUID> farmIds);

    /**
     * Find farms by tenantId, customerId and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param pageLink the page link
     * @return the list of farm objects
     */
    List<Farm> findFarmsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink);

    /**
     * Find farms by tenantId, customerId, type and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param type the type
     * @param pageLink the page link
     * @return the list of farm objects
     */
    List<Farm> findFarmsByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink);

    /**
     * Find farms by tenantId, customerId and farms Ids.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param farmIds the farm Ids
     * @return the list of farm objects
     */
    ListenableFuture<List<Farm>> findFarmsByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> farmIds);

    /**
     * Find farms by tenantId and farm name.
     *
     * @param tenantId the tenantId
     * @param name the farm name
     * @return the optional farm object
     */
    Optional<Farm> findFarmsByTenantIdAndName(UUID tenantId, String name);

    /**
     * Find tenants farm types.
     *
     * @return the list of tenant farm type objects
     */
    ListenableFuture<List<EntitySubtype>> findTenantFarmTypesAsync(UUID tenantId);

}

