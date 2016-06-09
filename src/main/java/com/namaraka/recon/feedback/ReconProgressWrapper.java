/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.feedback;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author smallgod
 */
public class ReconProgressWrapper {
    
    @SerializedName(value="reconprogress")
    private ReconProgress reconProgress;

    public ReconProgress getReconProgress() {
        return reconProgress;
    }

    public void setReconProgress(ReconProgress reconProgress) {
        this.reconProgress = reconProgress;
    }
    
}
