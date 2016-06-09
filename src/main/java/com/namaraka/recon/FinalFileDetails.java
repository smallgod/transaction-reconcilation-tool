/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.model.v1_0.BaseModel;
import com.namaraka.recon.utilities.Auditable;
import com.namaraka.recon.utilities.DBMSXMLObject;
import com.namaraka.recon.utilities.Processeable;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 *
 * @author smallGod
 */
@Entity
@DynamicUpdate(value = true)
@SelectBeforeUpdate(value = true)
@Table(name = "final_reconfile_details")
public class FinalFileDetails extends BaseModel implements Auditable, Processeable, Serializable {

    private static final long serialVersionUID = -6516148922518667952L;

    private String reportDisplayName;
    private String filePath;
    private String fileID;//generate an ID for this file
    private long reconGroupID; //generated ID shared with the other files in group - binds it to it's sister files that made it
    private boolean isExceptionsOnlyFile; //whether we include only exceptions or all txns    
    private String ownerID; //owner - whoever initiated the recon
    private ReconStatus reconProgress;
    
    public FinalFileDetails() {
    }

    public String getReportDisplayName() {
        return reportDisplayName.trim();
    }

    public void setReportDisplayName(String reportDisplayName) {
        this.reportDisplayName = reportDisplayName;
    }

    public String getFilePath() {
        return filePath.trim();
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getReconGroupID() {
        return reconGroupID;
    }

    public void setReconGroupID(long reconGroupID) {
        this.reconGroupID = reconGroupID;
    }

    public boolean isIsExceptionsOnlyFile() {
        return isExceptionsOnlyFile;
    }

    public void setIsExceptionsOnlyFile(boolean isExceptionsOnlyFile) {
        this.isExceptionsOnlyFile = isExceptionsOnlyFile;
    }

    public String getOwnerID() {
        return ownerID.trim();
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public FinalFileDetails getFileDetails(String fileID) {
        return this;
    }

    @Override
    public String getUsername() {
        return ownerID;
    }

    @Override
    public DBMSXMLObject getXMLObject() {
        return this;
    }

    public ReconStatus getReconProgress() {
        return reconProgress;
    }

    public void setReconProgress(ReconStatus reconProgress) {
        this.reconProgress = reconProgress;
    }
}
