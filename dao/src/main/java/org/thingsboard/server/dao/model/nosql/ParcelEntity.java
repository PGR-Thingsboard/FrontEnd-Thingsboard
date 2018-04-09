/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.thingsboard.server.common.data.crop.Crop;
import org.thingsboard.server.common.data.farm.Area;
import org.thingsboard.server.common.data.parcel.Parcel;
import org.thingsboard.server.common.data.id.ParcelId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.JsonCodec;

import static org.thingsboard.server.dao.model.ModelConstants.*;

/**
 *
 * @author German Lopez
 */
@Table(name = PARCEL_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public final class ParcelEntity implements SearchTextEntity<Parcel> {


    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @PartitionKey(value = 1)
    @Column(name = PARCEL_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @PartitionKey(value = 2)
    @Column(name = PARCEL_CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @PartitionKey(value = 3)
    @Column(name = PARCEL_TYPE_PROPERTY)
    private String type;

    @Column(name = PARCEL_NAME_PROPERTY)
    private String name;
    
    @Column(name = PARCEL_FARMID_PROPERTY)
    private String farmId;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @com.datastax.driver.mapping.annotations.Column(name = PARCEL_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    @Column(name = PARCEL_CROP)
    private String crop;

    @Column(name = PARCEL_CROPS_HISTORY)
    private String cropsHistory;

    @Column(name = PARCEL_TOTAL_AREA)
    private String totalArea;

    public ParcelEntity() {
        super();
    }

    public ParcelEntity(Parcel parcel) {
        if (parcel.getId() != null) {
            this.id = parcel.getId().getId();
        }
        if (parcel.getTenantId() != null) {
            this.tenantId = parcel.getTenantId().getId();
        }
        if (parcel.getCustomerId() != null) {
            this.customerId = parcel.getCustomerId().getId();
        }
        this.name = parcel.getName();
        this.type = parcel.getType();
        this.farmId = parcel.getFarmId();
        this.additionalInfo = parcel.getAdditionalInfo();
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.crop = mapper.writeValueAsString(parcel.getCrop());
            this.cropsHistory = mapper.writeValueAsString(parcel.getCropsHistory());
            this.totalArea = mapper.writeValueAsString(parcel.getTotalArea());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
    public Parcel toData() {
        Parcel parcel = new Parcel(new ParcelId(id));
        parcel.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            parcel.setTenantId(new TenantId(tenantId));
        }
        if (customerId != null) {
            parcel.setCustomerId(new CustomerId(customerId));
        }
        parcel.setName(name);
        parcel.setType(getType());
        parcel.setFarmId(farmId);
        parcel.setAdditionalInfo(additionalInfo);
        try {
            ObjectMapper mapper = new ObjectMapper();
            parcel.setCrop(mapper.readValue(crop, Crop.class));
            parcel.setCropsHistory(mapper.readValue(cropsHistory, mapper.getTypeFactory().constructParametricType(List.class, Crop.class)));
            parcel.setTotalArea(mapper.readValue(totalArea, Area.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parcel;
    }


    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public String getCropsHistory() {
        return cropsHistory;
    }

    public void setCropsHistory(String cropsHistory) {
        this.cropsHistory = cropsHistory;
    }

    public String getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(String totalArea) {
        this.totalArea = totalArea;
    }
}
