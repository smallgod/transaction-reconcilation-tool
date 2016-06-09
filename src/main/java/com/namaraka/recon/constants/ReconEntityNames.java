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
public enum ReconEntityNames implements Constants{

    RECONCILIATION_TABLE1("recon_entity1"),
    RECONCILIATION_TABLE2("recon_entity2"),
    RECONCILIATION_TABLE3("recon_entity3"),
    RECONCILIATION_TABLE4("recon_entity4"),
    RECONCILIATION_TABLE5("recon_entity5"),
    RECONCILIATION_TABLE6("recon_entity6"),
    RECONCILIATION_TABLE7("recon_entity7"),
    RECONCILIATION_TABLE8("recon_entity8"),
    RECONCILIATION_TABLE9("recon_entity9"),
    RECONCILIATION_TABLE10("recon_entity10"),
    RECONCILIATION_TABLE11("recon_entity11"),
    RECONCILIATION_TABLE12("recon_entity12"),
    RECONCILIATION_TABLE13("recon_entity13"),
    RECONCILIATION_TABLE14("recon_entity14"),
    RECONCILIATION_TABLE15("recon_entity15"),
    RECONCILIATION_TABLE16("recon_entity16"),
    RECONCILIATION_TABLE17("recon_entity17"),
    RECONCILIATION_TABLE18("recon_entity18"),
    RECONCILIATION_TABLE19("recon_entity19"),
    RECONCILIATION_TABLE20("recon_entity20"),
    RECONCILIATION_TABLE21("recon_entity21"),
    RECONCILIATION_TABLE22("recon_entity22"),
    RECONCILIATION_TABLE23("recon_entity23"),
    RECONCILIATION_TABLE24("recon_entity24"),
    RECONCILIATION_TABLE25("recon_entity25");

    private final String reconEntityNameValue;

    ReconEntityNames(String value) {
        this.reconEntityNameValue = value;
    }

    public String getValue() {
        return this.reconEntityNameValue;
    }

    public static ReconEntityNames convertToEnum(String entityNameValue) {

        if (entityNameValue != null) {
            
            for (ReconEntityNames fileExtEnum : ReconEntityNames.values()) {
                if (entityNameValue.equalsIgnoreCase(fileExtEnum.getValue())) {
                    return fileExtEnum;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + entityNameValue + " found");
        //return null;
    }

}
