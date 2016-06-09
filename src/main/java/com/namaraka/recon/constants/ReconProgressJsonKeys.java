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
public enum ReconProgressJsonKeys implements Constants {

    RECON_GROUP_ID("recongroupid");

    private final String reconProgJsonKey;

    ReconProgressJsonKeys(String reconProgJsonKey) {
        this.reconProgJsonKey = reconProgJsonKey;
    }

    @Override
    public String getValue() {
        return this.reconProgJsonKey;
    }

    public static ReconProgressJsonKeys convertToEnum(String reconProgJsonkey) {

        if (reconProgJsonkey != null) {
            
            for (ReconProgressJsonKeys fileExtEnum : ReconProgressJsonKeys.values()) {
                if (reconProgJsonkey.equalsIgnoreCase(fileExtEnum.getValue())) {
                    return fileExtEnum;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + reconProgJsonkey + " found");
        //return null;
    }

}
