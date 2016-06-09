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
public enum ReportType implements Constants{

    PEGASUS("PEGASUS"),
    GOOGLE("GOOGLE"),
    EQUINOX_SPECIAL("EQUINOX SPECIAL"),
    ELMA("ELMA"),
    EQUINOX("EQUINOX"),
    DEFAULT("NORMAL");
    
    private final String reportType;
    private static final Logger logger = LoggerFactory.getLogger(ReportType.class);

    ReportType(String reportType) {
        this.reportType = reportType;
    }

    @Override
    public String getValue() {
        return this.reportType;
    }

    public static ReportType convertToEnum(String reportTypeVal) {

        if (reportTypeVal != null) {
            
            for (ReportType reportTypeEnum : ReportType.values()) {
                if (reportTypeVal.equalsIgnoreCase(reportTypeEnum.getValue())) {
                    
                    System.out.println("report type returned: " + reportTypeVal);
                    return reportTypeEnum;
                }
            }
        }
        
        logger.warn("No constant with text " + reportTypeVal + " found");
        return DEFAULT;        
        //throw new IllegalArgumentException("No constant with text " + reportTypeVal + " found");
        //return null;
    }

}
