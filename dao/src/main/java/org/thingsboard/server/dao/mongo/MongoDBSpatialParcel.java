/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.mongo;

import static com.mongodb.client.model.Filters.eq;
import com.mongodb.Block;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.thingsboard.server.common.data.SpatialParcel;

/**
 *
 * @author Carlos Ramirez
 */
public class MongoDBSpatialParcel extends MongoConnectionPOJO<SpatialParcel> implements DaoMongo<SpatialParcel> {

    @Override
    public MongoCollection<SpatialParcel> getCollectionDependClass() {
        return this.getMongoDatabase().getCollection("Parcels", SpatialParcel.class);
    }

    @Override
    public List<SpatialParcel> find() {
        MongoCollection<SpatialParcel> farmCollection = getCollectionDependClass();
        List<SpatialParcel> resultSet = new CopyOnWriteArrayList<>();
        farmCollection.find().forEach((Block<SpatialParcel>) parcel -> {
            resultSet.add(parcel);
        });
        return resultSet;
    }

    @Override
    public SpatialParcel findById(String id) {
        return getCollectionDependClass().find(eq("_id", id)).first();
    }

    @Override
    //Revisar que poligono del lote este contenido en el poligono de la finca
    public SpatialParcel save(SpatialParcel t){
        try{
            if(this.findById(t.getId()) == null){
                getCollectionDependClass().insertOne(t);
            }else{
                getCollectionDependClass().updateOne(eq("_id",t.getId()),new Document("$set", new Document("polygons", t.getPolygons())));
            }
            return t;
        }catch(MongoWriteException ex){
            System.out.println("No fue posible agregar el parcel");
            
        }
        return null;
    }

    @Override
    public boolean removeById(String id) {
        DeleteResult deleteResult = getCollectionDependClass().deleteMany((Bson) eq("_id", id));
        return deleteResult.wasAcknowledged();
    }

}
