/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.dao.mongo;

import java.util.List;

/**
 *
 * @author Carlos Ramirez
 */
public interface DaoMongo<T> {

    List<T> find();

    T findById(String id);

    T save(T t);

    boolean removeById(String id);

}
