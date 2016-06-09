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
public enum ReconGlobalDetailKeys {

    NUM_RECORDS(1),
    IS_MASTER(2),
    MASTER_FILE_EFFORT(3);

    private final int globalDetailKeyValue;

    ReconGlobalDetailKeys(int globalDetailKeyVal) {
        this.globalDetailKeyValue = globalDetailKeyVal;
    }

    public double getProcessingEffortValue() {
        return this.globalDetailKeyValue;
    }

    /**
     *
     * @param globalDetKey
     * @return
     */
    public static ReconGlobalDetailKeys convertToEnum(int globalDetKey) throws IllegalArgumentException {

        if (globalDetKey > 0) {

            for (ReconGlobalDetailKeys reconGlobalKeyVal : ReconGlobalDetailKeys.values()) {
                if (globalDetKey == reconGlobalKeyVal.getProcessingEffortValue()) {
                    return reconGlobalKeyVal;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + globalDetKey + " found");
        //return null;
    }
}
