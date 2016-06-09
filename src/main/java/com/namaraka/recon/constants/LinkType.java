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
public enum LinkType  implements Constants{

    LINKED("LINKED"),
    LINKER("LINKER"),
    DEFAULT("NORMAL");
    
    private final String linkType;
    private static final Logger logger = LoggerFactory.getLogger(LinkType.class);

    LinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getValue() {
        return this.linkType;
    }

    public static LinkType convertToEnum(String linkTypeVal) {

        if (linkTypeVal != null) {
            
            for (LinkType linkTypeEnum : LinkType.values()) {
                if (linkTypeVal.equalsIgnoreCase(linkTypeEnum.linkType)) {
                    
                    System.out.println("report type returned: " + linkTypeVal);
                    return linkTypeEnum;
                }
            }
        }
        
        logger.warn("No constant with text " + linkTypeVal + " found");
        return DEFAULT;        
        //throw new IllegalArgumentException("No constant with text " + linkTypeVal + " found");
        //return null;
    }

}
