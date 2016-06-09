/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.namaraka.recon.constants.ReconcileCmd;

/**
 *
 * @author smallgod
 */
public class ReconcileRequest {

    //add Type to store this ---> {"callingfiles":{{"223":"222"}, {"220":"224"}, {"223":"224"}}    //these are file ID         
    private final ReconcileCmd action;
    private final String reconID;
    private final boolean isCalling;

    public ReconcileRequest(ReconcileCmd action, String reconID, boolean isCalling) {
        this.action = action;
        this.reconID = reconID;
        this.isCalling = isCalling;
    }
    
    public ReconcileCmd getAction() {
        return action;
    }

    public String getReconID() {
        return reconID;
    }

    public boolean isIsCalling() {
        return isCalling;
    }
}
