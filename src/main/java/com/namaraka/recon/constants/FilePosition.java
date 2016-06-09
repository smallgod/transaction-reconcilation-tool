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
public enum FilePosition  implements Constants{

    FIRST_FILE("FIRST_FILE"),
    SUBSEQUENT_FILE("SUBSEQUENT_FILE"),
    MASTER_FILE("MASTER_FILE");

    private final String filePositionValue;

    FilePosition(String filePositionValue) {
        this.filePositionValue = filePositionValue;
    }

    public String getValue() {
        return this.filePositionValue;
    }

    /**
     * 
     * @param filePositionVal
     * @return 
     */
    public static FilePosition convertToEnum(String filePositionVal) throws IllegalArgumentException {

        if (filePositionVal != null) {
            
            for (FilePosition filePositionValText : FilePosition.values()) {
                if (filePositionVal.equalsIgnoreCase(filePositionValText.getValue())) {
                    return filePositionValText;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + filePositionVal + " found");
        //return null;
    }

}
