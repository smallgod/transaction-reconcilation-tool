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
public enum TransactionState implements Constants {

    SUCCESS("SUCCESS", "S"),
    FAIL("FAIL", "F"),
    PENDING("PENDING", "P"),
    REVERSED("REVERSED", "R"),
    MANUAL_RECON("MANUAL_RECON", "MR"),
    MISSING_ID("MISSING_ID", "MI"),
    MISSING_STATUS("MISSING_STATUS", "MS"),
    MISSING_TXN("MISSING_TXN", "MT"),
    UNKNOWN_STATE("UNKNOWN_STATE", "U"),
    MISSING_AMOUNT("MISSING_AMOUNT", "MA");

    private final String transactionState;
    private final String shortCode;

    TransactionState(String transactionState, String shortCode) {
        this.transactionState = transactionState;
        this.shortCode = shortCode;
    }

    @Override
    public String getValue() {
        return this.transactionState;
    }
    
    public String getShortCodeValue(){
        return this.shortCode;
    }

    public static TransactionState convertToEnum(String transactionState) throws MyCustomException {

        if (transactionState != null) {

            for (TransactionState transactionStateEnum : TransactionState.values()) {
                
                if (transactionState.equalsIgnoreCase(transactionStateEnum.getValue())) {
                    return transactionStateEnum;
                } else if (transactionState.equalsIgnoreCase(transactionStateEnum.getShortCodeValue())){
                    return transactionStateEnum;
                }
            }
        }
        throw new MyCustomException("Unsupported Status Exception", ErrorCode.NOT_SUPPORTED_ERR, "Unsupported status value :: " + transactionState, ErrorCategory.CLIENT_ERR_TYPE);

    }
}
