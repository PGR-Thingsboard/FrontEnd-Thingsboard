/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.mongo;

import org.thingsboard.server.common.data.SpatialCrop;
import org.thingsboard.server.common.data.SpatialDevice;
import org.thingsboard.server.common.data.SpatialFarm;

/**
 *
 * @author Carlos Ramirez
 */
public class MongoDBSpatial extends MongoConnection implements SpatialIndexes {

    private final MongoDBSpatialCrop mongodbcrop;
    private final MongoDBSpatialFarm mongodbFarm;
    private final MongoDBSpatialDevice mongodbDevice;

    public MongoDBSpatial() {
        mongodbcrop = new MongoDBSpatialCrop();
        mongodbFarm = new MongoDBSpatialFarm();
        mongodbDevice = new MongoDBSpatialDevice();
    }

    public MongoDBSpatialCrop getMongodbcrop() {
        return mongodbcrop;
    }

    public MongoDBSpatialFarm getMongodbFarm() {
        return mongodbFarm;
    }

    public MongoDBSpatialDevice getMongodbDevice() {
        return mongodbDevice;
    }

    @Override
    public SpatialFarm findFarmsByDeviceId(String device_id) throws MongoDBException {
        try {
            SpatialDevice sdt = mongodbDevice.findById(device_id);
            SpatialCrop sct = mongodbcrop.findById(sdt.getDevice_Crop_FK());
            return mongodbFarm.findById(sct.getCrop_Farm_FK());
        } catch (NullPointerException ex) {
            throw new MongoDBException("It wasn´t posible to load the farm associated with device!!");
        }
    }

    @Override
    public SpatialCrop findCropsByDeviceId(String device_id) throws MongoDBException {
        try {
            SpatialDevice sdt = mongodbDevice.findById(device_id);
            return mongodbcrop.findById(sdt.getDevice_Crop_FK());
        } catch (NullPointerException ex) {
            throw new MongoDBException("It wasn´t posible to load the crop associated with device!!");
        }
    }

    @Override
    public SpatialDevice getCoordenatesByDeviceId(String device_id) throws MongoDBException {
        return mongodbDevice.findById(device_id);
    }

}
