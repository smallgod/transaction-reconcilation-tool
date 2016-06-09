/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.constants;

/**
 *
 * @author smallgod
 */
public enum StartReconJsonKeys implements Constants{

    //EXCEPTIONS_ONLY("isexceptionsonly"),
    RECON_GROUP_ID("recongroupid"),    
    REPORT_TITLE("recontitle"),
    ACTION("action"),
    IS_CALLING("iscalling"),
    CALLING_FILES("callingfiles");

    private final String startReconJsonKey;

    StartReconJsonKeys(String startReconJsonKey) {
        this.startReconJsonKey = startReconJsonKey;
    }

    @Override
    public String getValue() {
        return this.startReconJsonKey;
    }

    public static StartReconJsonKeys convertToEnum(String startReconJsonkey) {

        if (startReconJsonkey != null) {

            for (StartReconJsonKeys fileExtEnum : StartReconJsonKeys.values()) {
                if (startReconJsonkey.equalsIgnoreCase(fileExtEnum.getValue())) {
                    return fileExtEnum;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + startReconJsonkey + " found");
        //return null;
    }

}
