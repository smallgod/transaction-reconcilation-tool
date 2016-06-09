package com.namaraka.recon.model.v1_0;

import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.constants.ReconType;
import com.namaraka.recon.constants.ReconcileCmd;
import com.namaraka.recon.utilities.Auditable;
import com.namaraka.recon.utilities.DBMSXMLObject;
import com.namaraka.recon.utilities.Processeable;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 *
 * @author smallgod (c) Namaraka Technologies
 */
@Entity(name = "recon_details_entity")
@DynamicUpdate(value = true)
@SelectBeforeUpdate(value = true)
@Table(name = "recon_details_entity", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"recon_group_id"})})
public class ReconciliationDetails extends BaseModel implements Auditable, Processeable, Serializable {

    private static final long serialVersionUID = 9062757030017658399L;

    @Enumerated(EnumType.STRING)
    @Column(name = "recon_status")
    private ReconStatus reconStatus;
    
    @Column(name = "recon_name")
    private String reconName;
    
    @Column(name = "recon_group_id")
    private String reconGroupID; 
    
    @Column(name = "owner_id")
    private String ownerID;
    
    @Column(name = "file_count")
    private int noOfFilesInReconGroup;
    
    @Column(name = "recon_entityname")
    private String reconEntityName;
    
    @Column(name = "final_recon_file_name")
    private String finalReconFileName; //name of the reconciled file - dont include the full path - jst the name    
    
    @Column(name = "final_exceptions_recon_file_name")
    private String finalExceptionsReconFileName; //name of the reconciled file - dont include the full path - jst the name    
    
    @Column(name = "recon_folder_name")
    private String reconFolderName; //created on the fly by concatenating recon name & current dateTime
    
    @Column(name = "recon_type")
    @Enumerated(EnumType.STRING)    
    private ReconType reconType;
    //@Column(name = "reconcile_action")
    //@Enumerated(EnumType.STRING)
    @Transient
    private ReconcileCmd action; //whether 'NEW' 
    
    @Column(name = "is_for_calling")
    private boolean isCalling;
    
    @Column(name = "calling_file_ids")
    private String callingFiles;//The ideal solution is this --->>>  {"callingfiles":{"222-221":"", "220-213":""}    //these are file IDs
    
    @Column(name = "master_fileid")
    private String reconMasterFileID;
    
    @Column(name = "master_id_columnname")
    private String reconMasterIdColName;
    
    @Column(name = "master_file_fields", length = 3000)
    private String masterFileFields; // a JSON representation of the master file fields and blank values i.e. {"Transaction ID":"", "status":""}
    
    public ReconciliationDetails() {
    }

    @Override
    public DBMSXMLObject getXMLObject() {
        return this;
    }

    @Override
    public String getUsername() {
        return "N/A";
    }

    public ReconStatus getReconStatus() {
        return reconStatus;
    }

    public void setReconStatus(ReconStatus reconStatus) {
        this.reconStatus = reconStatus;
    }

    public String getReconName() {
        return reconName;
    }

    public void setReconName(String reconName) {
        this.reconName = reconName;
    }

    public String getReconGroupID() {
        return reconGroupID;
    }

    public void setReconGroupID(String reconGroupID) {
        this.reconGroupID = reconGroupID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public int getNoOfFilesInReconGroup() {
        return noOfFilesInReconGroup;
    }

    public void setNoOfFilesInReconGroup(int noOfFilesInReconGroup) {
        this.noOfFilesInReconGroup = noOfFilesInReconGroup;
    }

    public String getReconEntityName() {
        return reconEntityName;
    }

    public void setReconEntityName(String reconEntityName) {
        this.reconEntityName = reconEntityName;
    }

    public String getFinalReconFileName() {
        return finalReconFileName;
    }

    public void setFinalReconFileName(String finalReconFileName) {
        this.finalReconFileName = finalReconFileName;
    }

    public String getReconFolderName() {
        return reconFolderName;
    }

    public void setReconFolderName(String reconFolderName) {
        this.reconFolderName = reconFolderName;
    }

    public String getFinalExceptionsReconFileName() {
        return finalExceptionsReconFileName;
    }

    public void setFinalExceptionsReconFileName(String finalExceptionsReconFileName) {
        this.finalExceptionsReconFileName = finalExceptionsReconFileName;
    }

    public ReconType getReconType() {
        return reconType;
    }

    public void setReconType(ReconType reconType) {
        this.reconType = reconType;
    }

    public boolean isIsCalling() {
        return isCalling;
    }

    public void setIsCalling(boolean isCalling) {
        this.isCalling = isCalling;
    }

    public String getCallingFiles() {
        return callingFiles;
    }

    public void setCallingFiles(String callingFiles) {
        this.callingFiles = callingFiles;
    }

    public ReconcileCmd getAction() {
        return action;
    }

    public void setAction(ReconcileCmd action) {
        this.action = action;
    }

    public String getReconMasterFileID() {
        return reconMasterFileID;
    }

    public void setReconMasterFileID(String reconMasterFileID) {
        this.reconMasterFileID = reconMasterFileID;
    }

    public String getReconMasterIdColName() {
        return reconMasterIdColName;
    }

    public void setReconMasterIdColName(String reconMasterIdColName) {
        this.reconMasterIdColName = reconMasterIdColName;
    }

    public String getMasterFileFields() {
        return masterFileFields;
    }

    public void setMasterFileFields(String masterFileFields) {
        this.masterFileFields = masterFileFields;
    }
}
