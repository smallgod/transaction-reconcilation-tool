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
public enum ReconStatus implements Constants {

    NEW("NEW"),
    EDITED("EDITED"),
    //INVOKED("INVOKED"), //invoked by the /reconcile URL
    INPROGRESS("INPROGRESS"),
    DELETED("DELETED"),
    COMPLETED("COMPLETED"),
    UNKNOWN("UNKOWN");

    private final String reconStatus;

    ReconStatus(String reconProgressValue) {
        this.reconStatus = reconProgressValue;
    }

    @Override
    public String getValue() {
        return this.reconStatus;
    }

    /**
     * 
     * @param reconFileVal
     * @return 
     */
    public static ReconStatus convertToEnum(String reconFileVal) throws IllegalArgumentException {

        if (reconFileVal != null) {
            
            for (ReconStatus reconFileValueText : ReconStatus.values()) {
                if (reconFileVal.equalsIgnoreCase(reconFileValueText.getValue())) {
                    return reconFileValueText;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + reconFileVal + " found");
        //return null;
    }

}
