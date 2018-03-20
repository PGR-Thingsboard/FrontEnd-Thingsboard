/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.mongo;

/**
 *
 * @author Carlos Ramirez
 */
public class MongoDBException extends Exception{

    public MongoDBException(String message) {
        super(message);
    }

    public MongoDBException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
