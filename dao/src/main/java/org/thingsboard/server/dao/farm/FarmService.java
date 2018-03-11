/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.farm;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.farm.Farm;
import org.thingsboard.server.common.data.farm.FarmSearchQuery;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.FarmId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.model.nosql.FarmEntity;

/**
 *
 * @author German Lopez
 */
public interface FarmService {

    Farm findFarmById(FarmId farmId);

    ListenableFuture<Farm> findFarmByIdAsync(FarmId farmId);

    Optional<Farm> findFarmByTenantIdAndName(TenantId tenantId, String name);

    Farm saveFarm(Farm farm);
    
    ListenableFuture<List<FarmEntity>> allFarms();

    Farm assignFarmToCustomer(FarmId farmId, CustomerId customerId);

    Farm unassignFarmFromCustomer(FarmId farmId);

    void deleteFarm(FarmId farmId);

    TextPageData<Farm> findFarmsByTenantId(TenantId tenantId, TextPageLink pageLink);

    TextPageData<Farm> findFarmsByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink);

    ListenableFuture<List<Farm>> findFarmsByTenantIdAndIdsAsync(TenantId tenantId, List<FarmId> farmIds);

    void deleteFarmsByTenantId(TenantId tenantId);

    TextPageData<Farm> findFarmsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink);

    TextPageData<Farm> findFarmsByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink);

    ListenableFuture<List<Farm>> findFarmsByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<FarmId> farmIds);

    void unassignCustomerFarms(TenantId tenantId, CustomerId customerId);

    ListenableFuture<List<Farm>> findFarmsByQuery(FarmSearchQuery query);

    ListenableFuture<List<EntitySubtype>> findFarmTypesByTenantId(TenantId tenantId);
}
