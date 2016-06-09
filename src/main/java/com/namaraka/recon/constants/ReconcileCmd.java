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
public enum ReconcileCmd implements Constants{

    START("START"),
    CANCEL("CANCEL"),
    PAUSE("PAUSE"),
    RESUME("RESUME");

    private final String reconFileStatusValue;

    ReconcileCmd(String reconStatusValue) {
        this.reconFileStatusValue = reconStatusValue;
    }

    @Override
    public String getValue() {
        return this.reconFileStatusValue;
    }

    /**
     * 
     * @param reconFileVal
     * @return 
     */
    public static ReconcileCmd convertToEnum(String reconFileVal) throws IllegalArgumentException {

        if (reconFileVal != null) {
            
            for (ReconcileCmd reconcileCmdEnum : ReconcileCmd.values()) {
                if (reconFileVal.equalsIgnoreCase(reconcileCmdEnum.getValue())) {
                    return reconcileCmdEnum;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + reconFileVal + " found");
        //return null;
    }

}
