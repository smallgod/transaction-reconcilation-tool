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
public enum FileReconProgressEnum implements Constants {

    NEW("NEW"),
    EDITED("EDITED"),
    INPROGRESS("INPROGRESS"),
    DELETED("DELETED"),
    COMPLETED("COMPLETED"),
    UNKNOWN("UNKOWN");

    private final String reconProgressValue;

    FileReconProgressEnum(String reconProgressValue) {
        this.reconProgressValue = reconProgressValue;
    }

    public String getValue() {
        return this.reconProgressValue;
    }

    /**
     * 
     * @param reconFileVal
     * @return 
     */
    public static FileReconProgressEnum convertToEnum(String reconFileVal) throws IllegalArgumentException {

        if (reconFileVal != null) {
            
            for (FileReconProgressEnum reconFileValueText : FileReconProgressEnum.values()) {
                if (reconFileVal.equalsIgnoreCase(reconFileValueText.getValue())) {
                    return reconFileValueText;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + reconFileVal + " found");
        //return null;
    }
}
