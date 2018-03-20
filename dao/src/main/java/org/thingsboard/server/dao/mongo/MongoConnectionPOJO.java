/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.mongo;

import com.mongodb.client.MongoCollection;

/**
 *
 * @author Carlos Ramirez
 */
public abstract class MongoConnectionPOJO<T> extends MongoConnection{
    public abstract MongoCollection<T> getCollectionDependClass();
}
