/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.common.data.parcel;

import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.HasName;
import org.thingsboard.server.common.data.Polygon;
import org.thingsboard.server.common.data.SearchTextBasedWithAdditionalInfo;
import org.thingsboard.server.common.data.crop.Crop;
import org.thingsboard.server.common.data.farm.Area;
import org.thingsboard.server.common.data.id.ParcelId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.List;

/**
 *
 * @author German Lopez
 */
@EqualsAndHashCode(callSuper = true)
public class Parcel extends SearchTextBasedWithAdditionalInfo<ParcelId> implements HasName {

    private static final long serialVersionUID = 2807343040519543363L;

    private TenantId tenantId;
    private CustomerId customerId;
    private String name;
    private String type;
    private String farmId;
    private Polygon location;
    private Crop crop;
    private List<Crop> cropsHistory;
    private Area totalArea;

    public Parcel() {
        super();
    }

    public Parcel(ParcelId id) {
        super(id);
    }
    
    public Parcel(Parcel parcel){
        super(parcel);
        this.tenantId = parcel.getTenantId();
        this.customerId = parcel.getCustomerId();
        this.name = parcel.getName();
        this.type = parcel.getType();
        this.farmId = parcel.getFarmId();
        this.setCrop(parcel.getCrop());
        this.cropsHistory = parcel.getCropsHistory();
        this.totalArea = parcel.getTotalArea();
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
        builder.append("Parcel [tenantId=");
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
        builder.append(", farmId=");
        builder.append(farmId);
        builder.append("]");
        return builder.toString();
    }

    /**
     * @return the farmId
     */
    public String getFarmId() {
        return farmId;
    }

    /**
     * @param farmId the farmId to set
     */
    public void setFarmId(String farmId) {
        this.farmId = farmId;
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

    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
    }

    public List<Crop> getCropsHistory() {
        return cropsHistory;
    }

    public void setCropsHistory(List<Crop> cropsHistory) {
        this.cropsHistory = cropsHistory;
    }

    public Area getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(Area totalArea) {
        this.totalArea = totalArea;
    }
}
