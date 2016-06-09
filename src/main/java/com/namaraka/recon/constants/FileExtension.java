/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.constants;

import com.namaraka.recon.exceptiontype.MyCustomException;

/**
 *
 * @author smallgod
 */
public enum FileExtension implements Constants {

    CSV("csv"),
    XLS("xls"),
    XLSX("xlsx"),
    ODS("ods"),
    TXT("txt");

    private final String fileExtensionValue;

    FileExtension(String fileExtensionValue) {
        this.fileExtensionValue = fileExtensionValue;
    }

    public String getValue() {
        return this.fileExtensionValue;
    }

    /**
     * 
     * @param fileExtValue
     * @return file extension
     * @throws MyCustomException 
     */
    public static FileExtension convertToEnum(String fileExtValue) throws MyCustomException {

        if (fileExtValue != null) {

            for (FileExtension fileExtEnum : FileExtension.values()) {
                if (fileExtValue.equalsIgnoreCase(fileExtEnum.getValue())) {
                    return fileExtEnum;
                }
            }
        }
        throw new MyCustomException("NullPointer Exception", ErrorCode.NOT_SUPPORTED_ERR, "Unsupported File Extension:: " + fileExtValue, ErrorCategory.CLIENT_ERR_TYPE);
    }
}
