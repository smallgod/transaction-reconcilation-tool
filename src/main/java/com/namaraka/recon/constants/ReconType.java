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
public enum ReconType implements Constants {   
    
    CENTENARY_SPECIAL("CENTENARY_SPECIAL"),
    NORMAL("NORMAL");

    private final String reconType;
    private static final Logger logger = LoggerFactory.getLogger(ReconType.class);

    ReconType(String reconType) {
        this.reconType = reconType;
    }

    public String getValue() {
        return this.reconType;
    }

    public static ReconType convertToEnum(String reconTypeVal) {
       
        if (reconTypeVal != null) {
            
            for (ReconType reconTypeEnum : ReconType.values()) {
                if (reconTypeVal.equalsIgnoreCase(reconTypeEnum.getValue())) {
                    return reconTypeEnum;
                }
            }            
        }
        logger.warn("No constant with text " + reconTypeVal + " found");
        throw new IllegalArgumentException("No constant with text " + reconTypeVal + " found");
        //return null;
    }
}