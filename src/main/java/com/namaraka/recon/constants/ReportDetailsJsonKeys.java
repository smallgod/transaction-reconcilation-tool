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
public enum ReportDetailsJsonKeys implements Constants {

    AMOUNT_COL_NAME("amount"),
    TXN_DESCRIPTION("description"),
    REPORT_TYPE("reporttype"),
    LINK_TYPE("linktype"),
    REPORT_TITLE("reportname"),
    ID_COL_NAME("idlabel"),
    LINK_ID_COL_NAME("linkidlabel"),
    STATUS_COL_NAME("status"),
    FILENAME("filename"),
    FILEPATH("filepath"), //file path minus the mount point
    PENDING_VALUE("pending"),
    SUCCESS_VALUE("success"),
    FAILED_VALUE("failed"),
    RECON_FILEID("fileid"),
    RECON_GROUPID("reconid"),
    IS_MASTER("ismaster"),
    HAS_STATUS("hasstatus"),
    //FILE_RECON_PROGRESS("filereconprogress"),
    FILE_RECON_PROGRESS("action"),
    RECON_TITLE("recontitle"),
    FILEID_FIELDSMAP("fileidfieldsmapping"),
    DEFAULT("DEFAULT");

    private final String reportDetailsJsonKey;
    private static final Logger logger = LoggerFactory.getLogger(ReportDetailsJsonKeys.class);


    ReportDetailsJsonKeys(String reportDetailsJsonKey) {
        this.reportDetailsJsonKey = reportDetailsJsonKey;
    }

    @Override
    public String getValue() {
        return this.reportDetailsJsonKey;
    }

    public static ReportDetailsJsonKeys convertToEnum(String reportDetailskey) {

        if (reportDetailskey != null) {
            
            for (ReportDetailsJsonKeys fileExtEnum : ReportDetailsJsonKeys.values()) {
                if (reportDetailskey.equalsIgnoreCase(fileExtEnum.getValue())) {
                    return fileExtEnum;
                }
            }
        }
        logger.warn("No constant with text " + reportDetailskey + " found");
        return DEFAULT;
        //throw new IllegalArgumentException("No constant with text " + reportDetailskey + " found");
        //return null;
    }

}
