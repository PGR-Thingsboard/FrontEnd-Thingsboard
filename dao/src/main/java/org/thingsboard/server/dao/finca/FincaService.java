/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.finca;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.finca.Finca;
import org.thingsboard.server.common.data.finca.FincaSearchQuery;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.FincaId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;

/**
 *
 * @author German Lopez
 */
public interface FincaService {

    Finca findFincaById(FincaId fincaId);

    ListenableFuture<Finca> findFincaByIdAsync(FincaId fincaId);

    Optional<Finca> findFincaByTenantIdAndName(TenantId tenantId, String name);

    Finca saveFinca(Finca finca);

    Finca assignFincaToCustomer(FincaId fincaId, CustomerId customerId);

    Finca unassignFincaFromCustomer(FincaId fincaId);

    void deleteFinca(FincaId fincaId);

    TextPageData<Finca> findFincasByTenantId(TenantId tenantId, TextPageLink pageLink);

    TextPageData<Finca> findFincasByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink);

    ListenableFuture<List<Finca>> findFincasByTenantIdAndIdsAsync(TenantId tenantId, List<FincaId> fincaIds);

    void deleteFincasByTenantId(TenantId tenantId);

    TextPageData<Finca> findFincasByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink);

    TextPageData<Finca> findFincasByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink);

    ListenableFuture<List<Finca>> findFincasByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<FincaId> fincaIds);

    void unassignCustomerFincas(TenantId tenantId, CustomerId customerId);

    ListenableFuture<List<Finca>> findFincasByQuery(FincaSearchQuery query);

    ListenableFuture<List<EntitySubtype>> findFincaTypesByTenantId(TenantId tenantId);
}
