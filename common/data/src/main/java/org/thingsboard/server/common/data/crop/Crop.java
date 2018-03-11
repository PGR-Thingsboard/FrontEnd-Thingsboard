/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.common.data.crop;

import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.HasName;
import org.thingsboard.server.common.data.SearchTextBasedWithAdditionalInfo;
import org.thingsboard.server.common.data.id.CropId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;

/**
 *
 * @author German Lopez
 */
@EqualsAndHashCode(callSuper = true)
public class Crop extends SearchTextBasedWithAdditionalInfo<CropId> implements HasName {

    private static final long serialVersionUID = 2807343040519543363L;

    private TenantId tenantId;
    private CustomerId customerId;
    private String name;
    private String type;
    private String nameFarm;

    public Crop() {
        super();
    }

    public Crop(CropId id) {
        super(id);
    }
    
    public Crop(Crop crop){
        super(crop);
        this.tenantId = crop.getTenantId();
        this.customerId = crop.getCustomerId();
        this.name = crop.getName();
        this.type = crop.getType();
        this.nameFarm=crop.getNameFarm();
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
        builder.append("Crop [tenantId=");
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
        builder.append(", nameFarm=");
        builder.append(nameFarm);
        builder.append("]");
        return builder.toString();
    }

    /**
     * @return the farm
     */
    public String getNameFarm() {
        return nameFarm;
    }

    /**
     * @param farm the farm to set
     */
    public void setNameFarm(String nameFarm) {
        this.nameFarm = nameFarm;
    }

}
