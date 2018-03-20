/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.common.data;

/**
 *
 * @author Carlos Ramirez
 */
public class SpatialCrop {

    private String id, crop_Farm_FK;
    private Polygon polygons;

    public SpatialCrop() {
    }

    public SpatialCrop(String id, String crop_Farm_FK, Polygon polygons) {
        this.id = id;
        this.crop_Farm_FK = crop_Farm_FK;
        this.polygons = polygons;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCrop_Farm_FK() {
        return crop_Farm_FK;
    }

    public void setCrop_Farm_FK(String crop_Farm_FK) {
        this.crop_Farm_FK = crop_Farm_FK;
    }

    public Polygon getPolygons() {
        return polygons;
    }

    public void setPolygons(Polygon polygons) {
        this.polygons = polygons;
    }
    
    @Override
    public String toString(){
        return ("CropId: "+id+" ,crop_Farm_FK: "+crop_Farm_FK+" ,polygon: "+polygons.getCoordinates());
    }

}
