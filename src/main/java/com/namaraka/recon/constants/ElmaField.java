/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public enum ElmaField  implements Constants{ //We need to put these values in a DB or configs file
//We need to put these values in a DB or configs file

    TRANS_REF("TX Reference"),
    DATE("Date"),
    PAY_DETAILS("Payment Details"),
    STATUS("Status"),
    //SERVICE_TYPE("Payment Details"),   //will be derived from 'Payment Details'
    AMOUNT("Amount"),
    ACCOUNT_ID("Bank Account ID"),
    DEFAULT("DEFAULT");

    private static final Logger logger = LoggerFactory.getLogger(ElmaField.class);
    private final String elmaField;    

    ElmaField(String elmaField) {
        this.elmaField = elmaField;
    }

    public String getValue() {
        return this.elmaField;
    }

    public static ElmaField convertToEnum(String elmaFieldVal) {

        if (elmaFieldVal != null) {
            
            for (ElmaField elmaFieldEnum : ElmaField.values()) {
                if (elmaFieldVal.equalsIgnoreCase(elmaFieldEnum.elmaField)) {
                    return elmaFieldEnum;
                }
            }
        }
        
        logger.warn("No constant with text " + elmaFieldVal + " found");
        return DEFAULT;
        //throw new IllegalArgumentException("No constant with text " + elmaFieldVal + " found");
        //return null;
    }

}
