/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.finca;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.finca.Finca;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.Dao;

/**
 *
 * @author German Lopez
 */
public interface FincaDao extends Dao<Finca> {

    /**
     * Save or update finca object
     *
     * @param finca the finca object
     * @return saved finca object
     */
    Finca save(Finca finca);

    /**
     * Find fincas by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of finca objects
     */
    List<Finca> findFincasByTenantId(UUID tenantId, TextPageLink pageLink);

    /**
     * Find fincas by tenantId, type and page link.
     *
     * @param tenantId the tenantId
     * @param type the type
     * @param pageLink the page link
     * @return the list of finca objects
     */
    List<Finca> findFincasByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink);

    /**
     * Find fincas by tenantId and fincas Ids.
     *
     * @param tenantId the tenantId
     * @param fincaIds the finca Ids
     * @return the list of finca objects
     */
    ListenableFuture<List<Finca>> findFincasByTenantIdAndIdsAsync(UUID tenantId, List<UUID> fincaIds);

    /**
     * Find fincas by tenantId, customerId and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param pageLink the page link
     * @return the list of finca objects
     */
    List<Finca> findFincasByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink);

    /**
     * Find fincas by tenantId, customerId, type and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param type the type
     * @param pageLink the page link
     * @return the list of finca objects
     */
    List<Finca> findFincasByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink);

    /**
     * Find fincas by tenantId, customerId and fincas Ids.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param fincaIds the finca Ids
     * @return the list of finca objects
     */
    ListenableFuture<List<Finca>> findFincasByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> fincaIds);

    /**
     * Find fincas by tenantId and finca name.
     *
     * @param tenantId the tenantId
     * @param name the finca name
     * @return the optional finca object
     */
    Optional<Finca> findFincasByTenantIdAndName(UUID tenantId, String name);

    /**
     * Find tenants finca types.
     *
     * @return the list of tenant finca type objects
     */
    ListenableFuture<List<EntitySubtype>> findTenantFincaTypesAsync(UUID tenantId);

}

