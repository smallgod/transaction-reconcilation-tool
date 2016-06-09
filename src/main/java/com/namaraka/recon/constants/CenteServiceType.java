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
public enum CenteServiceType  implements Constants{

    PULL("PULL"),
    PUSH("PUSH"),
    DSTV("DSTV"),
    UMEME("UMEME"),
    NWSC("NWSC"),
    AIRTIME("AIRTIME");

    private final String centeServiceType;

    CenteServiceType(String centeServiceType) {
        this.centeServiceType = centeServiceType;
    }

    @Override
    public String getValue() {
        return this.centeServiceType;
    }

    public static CenteServiceType convertToEnum(String serviceTypeVal) {

        if (serviceTypeVal != null) {
            
            for (CenteServiceType serviceTypeEnum : CenteServiceType.values()) {
                if (serviceTypeVal.equalsIgnoreCase(serviceTypeEnum.centeServiceType)) {
                    return serviceTypeEnum;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + serviceTypeVal + " found");
        //return null;
    }

}
