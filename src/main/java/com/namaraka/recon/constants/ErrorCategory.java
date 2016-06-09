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
public enum ErrorCategory  implements Constants{

    SERVER_ERR_TYPE("SERVER_ERROR_TYPE"),
    CLIENT_ERR_TYPE("CLIENT_ERROR_TYPE"),
    EXTERNALSYSTEM_ERR_TYPE("EXTERNALSYSTEM_ERROR_TYPE");

    private final String errorCategoryValue;

    ErrorCategory(String errorCategoryValue) {
        this.errorCategoryValue = errorCategoryValue;
    }

    @Override
    public String getValue() {
        return this.errorCategoryValue;
    }
}
