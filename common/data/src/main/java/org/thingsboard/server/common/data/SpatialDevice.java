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
public class SpatialDevice {

    private String id, device_Crop_FK;
    private Point point;

    public SpatialDevice() {
    }

    public SpatialDevice(String id, String device_Crop_FK, Point point) {
        this.id = id;
        this.device_Crop_FK = device_Crop_FK;
        this.point = point;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevice_Crop_FK() {
        return device_Crop_FK;
    }

    public void setDevice_Crop_FK(String device_Crop_FK) {
        this.device_Crop_FK = device_Crop_FK;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    @Override
    public String toString(){
        return ("DeviceId: "+id+" ,device_Crop_FK: "+device_Crop_FK+" ,Point: "+point.getCoordinates());
    }

}
