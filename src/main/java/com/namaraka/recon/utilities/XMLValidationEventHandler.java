package com.namaraka.recon.utilities;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLValidationEventHandler implements ValidationEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(XMLValidationEventHandler.class);

    @Override
    public boolean handleEvent(ValidationEvent event) throws RuntimeException{

        logger.debug("handle event called");
        if (event == null){
            throw new NullPointerException("Error inside handleEvent method, event is NULL");
        }
        
        int evenSeverity = event.getSeverity();
        ValidationEventLocator vel = event.getLocator();
        
        logger.debug("EVENT:             " + event.getClass());
        logger.debug("SEVERITY:          " + evenSeverity);
        logger.debug("MESSAGE:           " + event.getMessage());
        logger.debug("LINKED EXCEPTION:  " + event.getLinkedException());
        logger.debug("LOCATOR");
        logger.debug("    LINE NUMBER:   " + vel.getLineNumber());
        logger.debug("    COLUMN NUMBER: " + vel.getColumnNumber());
        logger.debug("    OFFSET:        " + vel.getOffset());
        logger.debug("    OBJECT:        " + vel.getObject());
        logger.debug("    NODE:          " + vel.getNode());
        logger.debug("    URL:           " + vel.getURL());
        
        if(evenSeverity == ValidationEvent.ERROR || evenSeverity == ValidationEvent.FATAL_ERROR){
            String error = "XML Validation error:  " + event.getMessage() + " at row: " + vel.getLineNumber() + " and column: " + vel.getColumnNumber();
            logger.error(error);
            return false;
        }else{
            return true;
        }
    }
}