/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;

/**
 *
 * @author Carlos Ramirez
 */
public abstract class MongoConnection {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    @Autowired
    private ServerProperties serverProperties;

    public MongoConnection() {
    }

    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    public MongoClient getSession() {
        if (mongoClient == null) {
            //mongoClient = new MongoClient(new MongoClientURI(serverProperties.getMongoURI()));
            mongoClient = new MongoClient(new MongoClientURI("mongodb://192.168.0.10:27017"));
        }
        return mongoClient;
    }

    public MongoDatabase getMongoDatabase() {
        if (mongoDatabase == null) {
            org.bson.codecs.configuration.CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            //mongoDatabase = getSession().getDatabase(serverProperties.getMongoDB());
            mongoDatabase = getSession().getDatabase("prueba").withCodecRegistry(pojoCodecRegistry);
        }
        return mongoDatabase;
    }

    public MongoDatabase getMongoDatabaseByName(String databaseName) {
        if (mongoDatabase == null) {
            org.bson.codecs.configuration.CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            mongoDatabase = getSession().getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
        }
        return mongoDatabase;
    }

    public List<String> getListCollectionsNames() {
        List<String> collectionsNames = new ArrayList<>();
        for (String name : mongoDatabase.listCollectionNames()) {
            collectionsNames.add(name);
        }
        return collectionsNames;
    }

    public void dropCollection(String nameCollection) throws MongoDBException {
        if (getListCollectionsNames().contains(nameCollection)) {
            MongoCollection<org.bson.Document> collection = mongoDatabase.getCollection(nameCollection);
            collection.drop();
        } else {
            throw new MongoDBException("Collection not exist!!");
        }
    }

}
