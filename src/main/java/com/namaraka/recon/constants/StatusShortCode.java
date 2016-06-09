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
public enum StatusShortCode implements Constants {

    S("successful"),
    F("failed"),
    P("pending"),
    U("unknown");

    private final String statusCodeValue;

    StatusShortCode(String statusCodeValue) {
        this.statusCodeValue = statusCodeValue;
    }

    public String getValue() {
        return this.statusCodeValue;
    }

    /**
     * 
     * @param statusCodeVal
     * @return statusShortCodeEnum
     * @throws MyCustomException 
     */
    public static StatusShortCode convertToEnum(String statusCodeVal) throws MyCustomException {

        if (statusCodeVal != null) {

            for (StatusShortCode statusShortCodeEnum : StatusShortCode.values()) {
                if (statusCodeVal.equalsIgnoreCase(statusShortCodeEnum.getValue())) {
                    return statusShortCodeEnum;
                }
            }
        }
        throw new MyCustomException("Unsupported Status Exception", ErrorCode.NOT_SUPPORTED_ERR, "Unsupported status value :: " + statusCodeVal, ErrorCategory.CLIENT_ERR_TYPE);
    }
}
