/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.cultivo;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.cultivo.Cultivo;
import org.thingsboard.server.common.data.cultivo.CultivoSearchQuery;
import org.thingsboard.server.common.data.id.CultivoId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;

/**
 *
 * @author German Lopez
 */
public interface CultivoService {

    Cultivo findCultivoById(CultivoId cultivoId);

    ListenableFuture<Cultivo> findCultivoByIdAsync(CultivoId cultivoId);

    Optional<Cultivo> findCultivoByTenantIdAndName(TenantId tenantId, String name);

    Cultivo saveCultivo(Cultivo cultivo);

    Cultivo assignCultivoToCustomer(CultivoId cultivoId, CustomerId customerId);

    Cultivo unassignCultivoFromCustomer(CultivoId cultivoId);

    void deleteCultivo(CultivoId cultivoId);

    TextPageData<Cultivo> findCultivosByTenantId(TenantId tenantId, TextPageLink pageLink);

    TextPageData<Cultivo> findCultivosByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink);

    ListenableFuture<List<Cultivo>> findCultivosByTenantIdAndIdsAsync(TenantId tenantId, List<CultivoId> cultivoIds);

    void deleteCultivosByTenantId(TenantId tenantId);

    TextPageData<Cultivo> findCultivosByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink);

    TextPageData<Cultivo> findCultivosByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink);

    ListenableFuture<List<Cultivo>> findCultivosByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<CultivoId> cultivoIds);

    void unassignCustomerCultivos(TenantId tenantId, CustomerId customerId);

    ListenableFuture<List<Cultivo>> findCultivosByQuery(CultivoSearchQuery query);

    ListenableFuture<List<EntitySubtype>> findCultivoTypesByTenantId(TenantId tenantId);
}