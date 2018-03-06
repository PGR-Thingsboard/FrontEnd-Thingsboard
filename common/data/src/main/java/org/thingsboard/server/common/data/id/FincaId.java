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
public class FincaId extends UUIDBased implements EntityId {

    private static final long serialVersionUID = 1L;

    @JsonCreator
    public FincaId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static FincaId fromString(String fincaId) {
        return new FincaId(UUID.fromString(fincaId));
    }

    @JsonIgnore
    @Override
    public EntityType getEntityType() {
        return EntityType.FINCA;
    }
}
