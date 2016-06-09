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
public enum EquinoxField  implements Constants{ //We need to put these values in a DB or configs file
//We need to put these values in a DB or configs file
    

    TRACER_NO("origin_tracer_no"),
    DESCRIPTION("description"),
    POST_DATE("posting_dt_tm"),
    //SERVICE_TYPE("description"),   //will be derived from description
    AMOUNT("amt"),
    ACCOUNT_ID("acct_no"),
    DEFAULT("DEFAULT");

    private final String equinoxFields;
    private static final Logger logger = LoggerFactory.getLogger(EquinoxField.class);

    EquinoxField(String equinoxField) {
        this.equinoxFields = equinoxField;
    }

    @Override
    public String getValue() {
        return this.equinoxFields;
    }

    public static EquinoxField convertToEnum(String equinoxFieldVal) {
       
        if (equinoxFieldVal != null) {
            
            for (EquinoxField equinoxFieldEnum : EquinoxField.values()) {
                if (equinoxFieldVal.equalsIgnoreCase(equinoxFieldEnum.getValue())) {
                    return equinoxFieldEnum;
                }
            }
            
        }
        logger.warn("No constant with text " + equinoxFieldVal + " found");
        return DEFAULT;
        //throw new IllegalArgumentException("No constant with text " + equinoxFieldVal + " found");
        //return null;
    }

}
