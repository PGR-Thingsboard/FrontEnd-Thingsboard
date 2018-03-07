/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.common.data.cultivo;

import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.HasName;
import org.thingsboard.server.common.data.SearchTextBasedWithAdditionalInfo;
import org.thingsboard.server.common.data.id.CultivoId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;

/**
 *
 * @author German Lopez
 */
@EqualsAndHashCode(callSuper = true)
public class Cultivo extends SearchTextBasedWithAdditionalInfo<CultivoId> implements HasName {

    private static final long serialVersionUID = 2807343040519543363L;

    private TenantId tenantId;
    private CustomerId customerId;
    private String name;
    private String type;
    private String nameFinca;

    public Cultivo() {
        super();
    }

    public Cultivo(CultivoId id) {
        super(id);
    }
    
    public Cultivo(Cultivo cultivo){
        super(cultivo);
        this.tenantId = cultivo.getTenantId();
        this.customerId = cultivo.getCustomerId();
        this.name = cultivo.getName();
        this.type = cultivo.getType();
        this.nameFinca=cultivo.getNameFinca();
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
        builder.append("Cultivo [tenantId=");
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
        builder.append(", nameFinca=");
        builder.append(nameFinca);
        builder.append("]");
        return builder.toString();
    }

    /**
     * @return the finca
     */
    public String getNameFinca() {
        return nameFinca;
    }

    /**
     * @param finca the finca to set
     */
    public void setNameFinca(String nameFinca) {
        this.nameFinca = nameFinca;
    }

}
