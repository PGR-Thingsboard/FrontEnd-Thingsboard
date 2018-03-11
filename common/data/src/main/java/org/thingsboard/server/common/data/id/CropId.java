/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import org.thingsboard.server.common.data.EntityType;

/**
 *
 * @author German Lopez
 */
public class CropId extends UUIDBased implements EntityId {

    private static final long serialVersionUID = 1L;

    @JsonCreator
    public CropId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static CropId fromString(String cropId) {
        return new CropId(UUID.fromString(cropId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.CROP;
    }
}