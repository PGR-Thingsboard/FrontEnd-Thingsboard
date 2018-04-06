/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.common.data.farm;

import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.HasName;
import org.thingsboard.server.common.data.Polygon;
import org.thingsboard.server.common.data.SearchTextBasedWithAdditionalInfo;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.FarmId;
import org.thingsboard.server.common.data.id.TenantId;

/**
 *
 * @author German Lopez
 */
@EqualsAndHashCode(callSuper = true)
public class Farm extends SearchTextBasedWithAdditionalInfo<FarmId> implements HasName {

    private static final long serialVersionUID = 2807343040519543363L;

    private TenantId tenantId;
    private CustomerId customerId;
    private String name;
    private String type;
    private String dashboardId;
    private Polygon location;
    private String locationDescription;
    private FarmDetails farmDetails;
    private FarmPhotographs farmPhotographs;
    private HomeDetails homeDetails;
    private Technology technology;
    private Enviroment enviroment;
    private Area totalArea;

    public Farm() {
        super();
    }

    public Farm(FarmId id) {
        super(id);
    }

    public Farm(Farm farm) {
        super(farm);
        this.tenantId = farm.getTenantId();
        this.customerId = farm.getCustomerId();
        this.name = farm.getName();
        this.type = farm.getType();
        this.location = farm.getLocation();
        this.dashboardId = farm.getDashboardId();
        this.locationDescription = farm.getLocationDescription();
        this.farmDetails = farm.getFarmDetails();
        this.farmPhotographs = farm.getFarmPhotographs();
        this.homeDetails = farm.getHomeDetails();
        this.technology = farm.getTechnology();
        this.enviroment = farm.getEnviroment();
        this.totalArea = farm.getTotalArea();
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
    }

    public Area getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(Area totalArea) {
        this.totalArea = totalArea;
    }

    @Override

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

    @Override
    public String getSearchText() {
        return getName();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Farm [tenantId=");
        builder.append(tenantId);
        builder.append(", customerId=");
        builder.append(customerId);
        builder.append(", name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type);
        builder.append(", additionalInfo=");
        builder.append(getAdditionalInfo());
        builder.append(", createdTime=");
        builder.append(createdTime);
        builder.append(", id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }

    /**
     * @return the location
     */
    public Polygon getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Polygon location) {
        this.location = location;
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

    public FarmDetails getFarmDetails() {
        return farmDetails;
    }

    public void setFarmDetails(FarmDetails farmDetails) {
        this.farmDetails = farmDetails;
    }

    public FarmPhotographs getFarmPhotographs() {
        return farmPhotographs;
    }

    public void setFarmPhotographs(FarmPhotographs farmPhotographs) {
        this.farmPhotographs = farmPhotographs;
    }

    public HomeDetails getHomeDetails() {
        return homeDetails;
    }

    public void setHomeDetails(HomeDetails homeDetails) {
        this.homeDetails = homeDetails;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }

    public Enviroment getEnviroment() {
        return this.enviroment;
    }

    public void setEnviroment(Enviroment enviroment) {
        this.enviroment = enviroment;
    }
}
