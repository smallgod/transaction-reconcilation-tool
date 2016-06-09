/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

/**
 *
 * @author smallgod
 */

import com.namaraka.recon.model.v1_0.ReconTransactionsTable;
import java.io.Serializable;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

public class InterceptorClass extends EmptyInterceptor {

    private static final long serialVersionUID = -4192072617162910240L;
    private static final Logger logger = LoggerFactory.getLogger(InterceptorClass.class);

    public InterceptorClass() {
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        logger.debug("Delete event");
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {

        logger.debug("Update Operation >> onFlushDirty event");

        if (entity instanceof ReconTransactionsTable) {

            ReconTransactionsTable sp = (ReconTransactionsTable) entity;
            return true;
        }
        return false;
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

        logger.debug("onLoad event");
        return true;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

        logger.debug("Create Operation >> onSave event");
        if (entity instanceof ReconTransactionsTable) {

            ReconTransactionsTable sp = (ReconTransactionsTable) entity;
            return true;
        }
        return false;
    }

    // called before commit into database
    @Override
    public void preFlush(Iterator entities) {
        logger.debug("preFlush operation, b4 commiting to db  >> preFlush event");
        logger.debug("postFlush: List of objects that have been flushed... ");
        int i = 0;
        while (entities.hasNext()) {
            Object entity = entities.next();

            if (entity instanceof ReconTransactionsTable) {

                ReconTransactionsTable sp = (ReconTransactionsTable) entity;
            }

            logger.debug("postFlush: " + (++i) + " : " + entity);
        }
    }

    // called after committed into database
    //This method is called after a flush has occurred and an object has been updated in memory
    @Override
    public void postFlush(Iterator entities) {

        logger.debug("postFlush operation, after commiting to db  >> postFlush event");
        logger.debug("preFlush: List of objects to flush... ");

        int i = 0;
        while (entities.hasNext()) {
            Object entity = entities.next();

            if (entity instanceof ReconTransactionsTable) {

                ReconTransactionsTable sub = (ReconTransactionsTable) entity;

            }

            logger.info("preFlush: " + (++i) + " : " + entity);
        }
    }
}
