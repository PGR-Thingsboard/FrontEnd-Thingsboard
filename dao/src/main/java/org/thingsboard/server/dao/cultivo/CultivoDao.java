/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.cultivo;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.cultivo.Cultivo;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.Dao;

/**
 *
 * @German Lopez
 */
public interface CultivoDao extends Dao<Cultivo> {

    /**
     * Save or update cultivo object
     *
     * @param cultivo the cultivo object
     * @return saved cultivo object
     */
    Cultivo save(Cultivo cultivo);

    /**
     * Find cultivos by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of cultivo objects
     */
    List<Cultivo> findCultivosByTenantId(UUID tenantId, TextPageLink pageLink);

    /**
     * Find cultivos by tenantId, type and page link.
     *
     * @param tenantId the tenantId
     * @param type the type
     * @param pageLink the page link
     * @return the list of cultivo objects
     */
    List<Cultivo> findCultivosByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink);

    /**
     * Find cultivos by tenantId and cultivos Ids.
     *
     * @param tenantId the tenantId
     * @param cultivoIds the cultivo Ids
     * @return the list of cultivo objects
     */
    ListenableFuture<List<Cultivo>> findCultivosByTenantIdAndIdsAsync(UUID tenantId, List<UUID> cultivoIds);

    /**
     * Find cultivos by tenantId, customerId and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param pageLink the page link
     * @return the list of cultivo objects
     */
    List<Cultivo> findCultivosByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink);

    /**
     * Find cultivos by tenantId, customerId, type and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param type the type
     * @param pageLink the page link
     * @return the list of cultivo objects
     */
    List<Cultivo> findCultivosByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink);

    /**
     * Find cultivos by tenantId, customerId and cultivos Ids.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param cultivoIds the cultivo Ids
     * @return the list of cultivo objects
     */
    ListenableFuture<List<Cultivo>> findCultivosByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> cultivoIds);

    /**
     * Find cultivos by tenantId and cultivo name.
     *
     * @param tenantId the tenantId
     * @param name the cultivo name
     * @return the optional cultivo object
     */
    Optional<Cultivo> findCultivosByTenantIdAndName(UUID tenantId, String name);

    /**
     * Find tenants cultivo types.
     *
     * @return the list of tenant cultivo type objects
     */
    ListenableFuture<List<EntitySubtype>> findTenantCultivoTypesAsync(UUID tenantId);

}