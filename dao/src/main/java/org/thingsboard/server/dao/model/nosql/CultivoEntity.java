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
import javax.persistence.Column;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.thingsboard.server.common.data.cultivo.Cultivo;
import org.thingsboard.server.common.data.id.CultivoId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_ADDITIONAL_INFO_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_COLUMN_FAMILY_NAME;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_CUSTOMER_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_NAME_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_TENANT_ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.CULTIVO_TYPE_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.ID_PROPERTY;
import static org.thingsboard.server.dao.model.ModelConstants.SEARCH_TEXT_PROPERTY;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.JsonCodec;

/**
 *
 * @author German Lopez
 */
@Table(name = CULTIVO_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
public final class CultivoEntity implements SearchTextEntity<Cultivo> {

    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @PartitionKey(value = 1)
    @Column(name = CULTIVO_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @PartitionKey(value = 2)
    @Column(name = CULTIVO_CUSTOMER_ID_PROPERTY)
    private UUID customerId;

    @PartitionKey(value = 3)
    @Column(name = CULTIVO_TYPE_PROPERTY)
    private String type;

    @Column(name = CULTIVO_NAME_PROPERTY)
    private String name;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @com.datastax.driver.mapping.annotations.Column(name = CULTIVO_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    public CultivoEntity() {
        super();
    }

    public CultivoEntity(Cultivo cultivo) {
        if (cultivo.getId() != null) {
            this.id = cultivo.getId().getId();
        }
        if (cultivo.getTenantId() != null) {
            this.tenantId = cultivo.getTenantId().getId();
        }
        if (cultivo.getCustomerId() != null) {
            this.customerId = cultivo.getCustomerId().getId();
        }
        this.name = cultivo.getName();
        this.type = cultivo.getType();
        this.additionalInfo = cultivo.getAdditionalInfo();
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
    public Cultivo toData() {
        Cultivo cultivo = new Cultivo(new CultivoId(id));
        cultivo.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            cultivo.setTenantId(new TenantId(tenantId));
        }
        if (customerId != null) {
            cultivo.setCustomerId(new CustomerId(customerId));
        }
        cultivo.setName(name);
        cultivo.setType(type);
        cultivo.setAdditionalInfo(additionalInfo);
        return cultivo;
    }

    

}
