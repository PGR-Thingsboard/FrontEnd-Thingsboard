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
import org.thingsboard.server.common.data.finca.Finca;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.FincaId;
import org.thingsboard.server.common.data.id.TenantId;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_ADDITIONAL_INFO_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_CUSTOMER_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_NAME_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.FINCA_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.SEARCH_TEXT_PROPERTY;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.JsonCodec;

/**
 *
 * @author German Lopez
 */
@Table(name = FINCA_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public final class FincaEntity implements SearchTextEntity<Finca> {

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @PartitionKey(value = 1)
    @Column(name = FINCA_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @PartitionKey(value = 2)
    @Column(name = FINCA_CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @PartitionKey(value = 3)
    @Column(name = FINCA_TYPE_PROPERTY)
    private String type;

    @Column(name = FINCA_NAME_PROPERTY)
    private String name;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @com.datastax.driver.mapping.annotations.Column(name = FINCA_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    public FincaEntity() {
        super();
    }

    public FincaEntity(Finca finca) {
        if (finca.getId() != null) {
            this.id = finca.getId().getId();
        }
        if (finca.getTenantId() != null) {
            this.tenantId = finca.getTenantId().getId();
        }
        if (finca.getCustomerId() != null) {
            this.customerId = finca.getCustomerId().getId();
        }
        this.name = finca.getName();
        this.type = finca.getType();
        this.additionalInfo = finca.getAdditionalInfo();
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
    public Finca toData() {
        Finca finca = new Finca(new FincaId(id));
        finca.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            finca.setTenantId(new TenantId(tenantId));
        }
        if (customerId != null) {
            finca.setCustomerId(new CustomerId(customerId));
        }
        finca.setName(name);
        finca.setType(type);
        finca.setAdditionalInfo(additionalInfo);
        return finca;
    }

    

}