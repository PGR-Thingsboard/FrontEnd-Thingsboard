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
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.thingsboard.server.common.data.crop.Crop;
import org.thingsboard.server.common.data.id.CropId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.FarmId;
import org.thingsboard.server.common.data.id.TenantId;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_ADDITIONAL_INFO_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_CUSTOMER_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_FARMID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_NAME_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CROP_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.SEARCH_TEXT_PROPERTY;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.JsonCodec;

/**
 *
 * @author German Lopez
 */
@Table(name = CROP_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public final class CropEntity implements SearchTextEntity<Crop> {

    /**
     * @return the nameFina
     */
    public String getFarmId() {
        return farmId;
    }

    /**
     * @param nameFina the nameFina to set
     */
    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @PartitionKey(value = 1)
    @Column(name = CROP_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @PartitionKey(value = 2)
    @Column(name = CROP_CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @PartitionKey(value = 3)
    @Column(name = CROP_TYPE_PROPERTY)
    private String type;

    @Column(name = CROP_NAME_PROPERTY)
    private String name;
    
    @Column(name = CROP_FARMID_PROPERTY)
    private String farmId;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @com.datastax.driver.mapping.annotations.Column(name = CROP_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    public CropEntity() {
        super();
    }

    public CropEntity(Crop crop) {
        if (crop.getId() != null) {
            this.id = crop.getId().getId();
        }
        if (crop.getTenantId() != null) {
            this.tenantId = crop.getTenantId().getId();
        }
        if (crop.getCustomerId() != null) {
            this.customerId = crop.getCustomerId().getId();
        }
        this.name = crop.getName();
        this.type = crop.getType();
        this.farmId = crop.getFarmId();
        this.additionalInfo = crop.getAdditionalInfo();
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
    public Crop toData() {
        Crop crop = new Crop(new CropId(id));
        crop.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            crop.setTenantId(new TenantId(tenantId));
        }
        if (customerId != null) {
            crop.setCustomerId(new CustomerId(customerId));
        }
        crop.setName(name);
        crop.setType(getType());
        crop.setFarmId(farmId);
        crop.setAdditionalInfo(additionalInfo);
        return crop;
    }

    

}
