/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.thingsboard.server.common.data.farm.Farm;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.FarmId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.JsonCodec;

import static org.thingsboard.server.dao.model.ModelConstants.*;

/**
 *
 * @author German Lopez
 */
@Table(name = FARM_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public final class FarmEntity implements SearchTextEntity<Farm> {

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @PartitionKey(value = 1)
    @Column(name = FARM_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @PartitionKey(value = 2)
    @Column(name = FARM_CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @PartitionKey(value = 3)
    @Column(name = FARM_TYPE_PROPERTY)
    private String type;

    @Column(name = FARM_NAME_PROPERTY)
    private String name;

    @Column(name = FARM_DASHBOARDID_PROPERTY)
    private String dashboardId;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @com.datastax.driver.mapping.annotations.Column(name = FARM_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    public FarmEntity() {
        super();
    }

    public FarmEntity(Farm farm) {
        if (farm.getId() != null) {
            this.id = farm.getId().getId();
        }
        if (farm.getTenantId() != null) {
            this.tenantId = farm.getTenantId().getId();
        }
        if (farm.getCustomerId() != null) {
            this.customerId = farm.getCustomerId().getId();
        }
        this.name = farm.getName();
        this.type = farm.getType();
        this.dashboardId = farm.getDashboardId();
        this.additionalInfo = farm.getAdditionalInfo();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonNode getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(JsonNode additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String getSearchTextSource() {
        return getName();
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    @Override
    public Farm toData() {
        Farm farm = new Farm(new FarmId(id));
        farm.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            farm.setTenantId(new TenantId(tenantId));
        }
        if (customerId != null) {
            farm.setCustomerId(new CustomerId(customerId));
        }
        farm.setName(name);
        farm.setType(type);
        farm.setAdditionalInfo(additionalInfo);
        farm.setDashboardId(dashboardId);
        return farm;
    }


    public String getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(String dashboardId) {
        this.dashboardId = dashboardId;
    }
}