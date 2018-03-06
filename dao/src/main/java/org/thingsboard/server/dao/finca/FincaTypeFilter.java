/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.finca;

import java.util.List;
import javax.annotation.Nullable;
import lombok.Data;

/**
 *
 * @author German Lopez
 */
@Data
public class FincaTypeFilter {
    @Nullable
    private String relationType;
    @Nullable
    private List<String> fincaTypes;
}
