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
import org.thingsboard.server.common.data.farm.*;
import org.thingsboard.server.common.data.farm.Area;
import org.thingsboard.server.common.data.farm.Farm;
import org.thingsboard.server.common.data.farm.FarmDetails;
import org.thingsboard.server.common.data.farm.IrrigationSystem;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.FarmId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.JsonCodec;
import org.thingsboard.server.dao.mongo.MongoDBException;
import org.thingsboard.server.dao.mongo.MongoDBSpatial;

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

    @Column(name = FARM_TOTAL_AREA)
    private String totalArea;

    @Column(name = FARM_DASHBOARDID_PROPERTY)
    private String dashboardId;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @com.datastax.driver.mapping.annotations.Column(name = FARM_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    @Column(name= FARM_LOCATION_DESCRIPTION)
    private String locationDescription;

    @Column(name = FARM_DETAILS)
    private String farmDetails;

    @Column(name = FARM_ENVIROMENT)
    private String farmEnviroment;

    @Column(name = FARM_HOME_DETAILS)
    private String homeDetails;
    @Column(name = IRRIGATIONS_SYSTEMS)
    private  String irrigationsSystems;


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
        this.locationDescription = farm.getLocationDescription();
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.farmDetails = mapper.writeValueAsString(farm.getFarmDetails());
            this.totalArea = mapper.writeValueAsString(farm.getTotalArea());
            this.homeDetails= mapper.writeValueAsString(farm.getHomeDetails());
            this.farmEnviroment= mapper.writeValueAsString(farm.getEnviroment());
            this.irrigationsSystems = mapper.writeValueAsString(farm.getIrrigationsSystems());
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

    public String getFarmEnviroment() {
        return farmEnviroment;
    }

    public void setFarmEnviroment(String farmEnviroment) {
        this.farmEnviroment = farmEnviroment;
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
        farm.setLocationDescription(locationDescription);
        try {
            ObjectMapper mapper = new ObjectMapper();
            farm.setFarmDetails(mapper.readValue(farmDetails, FarmDetails.class));
            farm.setTotalArea(mapper.readValue(totalArea,Area.class));
            farm.setEnviroment(mapper.readValue(farmEnviroment,Enviroment.class));
            farm.setHomeDetails(mapper.readValue(homeDetails,HomeDetails.class));
            farm.setIrrigationsSystems(mapper.readValue(irrigationsSystems, mapper.getTypeFactory().constructParametricType(List.class, IrrigationSystem.class)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*MongoDBSpatial mongoService = new MongoDBSpatial();
        if(farm.getId() == null){
            System.out.println("Id: "+id.toString());
            farm.setLocation(mongoService.getMongodbFarm().findById(id.toString()).getPolygons());
        }else if(id == null){
            System.out.println("FARM.GETID: "+farm.getId().getId().toString());
            farm.setLocation(mongoService.getMongodbFarm().findById(farm.getId().getId().toString()).getPolygons());
        }*/
        return farm;
    }

    public String getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(String totalArea) {
        this.totalArea = totalArea;
    }

    public String getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(String dashboardId) {
        this.dashboardId = dashboardId;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getFarmDetails() {
        return farmDetails;
    }

    public void setFarmDetails(String farmDetails) {
        this.farmDetails = farmDetails;
    }

    public String getHomeDetails() {
        return homeDetails;
    }

    public void setHomeDetails(String homeDetails) {
        this.homeDetails = homeDetails;
    }
    public String getIrrigationsSystems() {
        return irrigationsSystems;
    }

    public void setIrrigationsSystems(String irrigationsSystems) {
        this.irrigationsSystems = irrigationsSystems;
    }

}