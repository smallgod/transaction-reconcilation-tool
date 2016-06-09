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
public enum DateTimeZones  implements Constants{

    KAMPALA("Africa/Kampala");

    private final String dateTimeZoneValue;

    DateTimeZones(String dateTimeZone) {
        this.dateTimeZoneValue = dateTimeZone;
    }

    @Override
    public String getValue() {
        return this.dateTimeZoneValue;
    }

    /**
     *
     * @param dateTimeZoneVal
     * @return
     */
    public static DateTimeZones convertToEnum(String dateTimeZoneVal) throws IllegalArgumentException {

        if (dateTimeZoneVal != null) {

            for (DateTimeZones dateTimeZone : DateTimeZones.values()) {
                if (dateTimeZoneVal.equalsIgnoreCase(dateTimeZone.getValue())) {
                    return dateTimeZone;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + dateTimeZoneVal + " found");
        //return null;
    }

}
