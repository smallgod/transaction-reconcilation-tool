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
public enum ProcessingEffort {

    FIRST_FILE_EFFORT(1),
    OTHER_FILES_EFFORT(3),
    MASTER_FILE_EFFORT(3);

    private final int processingEffortValue;

    ProcessingEffort(int processEffortValue) {
        this.processingEffortValue = processEffortValue;
    }

    public int getProcessingEffortValue() {
        return this.processingEffortValue;
    }

    /**
     * 
     * @param processEffortVal
     * @return 
     */
    public static ProcessingEffort convertToEnum(int processEffortVal) throws IllegalArgumentException {

        if (processEffortVal > 0) {
            
            for (ProcessingEffort reconFileValueText : ProcessingEffort.values()) {
                if (processEffortVal == reconFileValueText.getProcessingEffortValue()) {
                    return reconFileValueText;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + processEffortVal + " found");
        //return null;
    }

}
