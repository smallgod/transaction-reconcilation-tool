/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.model.v1_0;

import com.namaraka.recon.constants.FileReconProgressEnum;
import com.namaraka.recon.constants.LinkType;
import com.namaraka.recon.constants.ReportType;
import com.namaraka.recon.utilities.Auditable;
import com.namaraka.recon.utilities.DBMSXMLObject;
import com.namaraka.recon.utilities.FileProcessDeterminants;
import com.namaraka.recon.utilities.Processeable;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 *
 * @author smallGod
 */
@Entity
@DynamicUpdate(value = true)
@SelectBeforeUpdate(value = true)
@Table(name = "report_file_details", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"file_id", "file_name"})})
public class ReportDetails extends BaseModel implements Auditable, Processeable, Serializable {

    private static final long serialVersionUID = -616134449129965733L;

    
    @Column(name = "recon_file_name")
    private String finalReconFileName;  //we need to see how to get this field - not in this object
    @Column(name = "id_column_name")
    private String IDColumnName;
    @Column(name = "link_id_column_name")
    private String linkIDColumnName;
    @Column(name = "status_column_name")
    private String statusColumnName = "";
    @Column(name = "amount_column_name")
    private String amountColumnName;
    @Column(name = "date_column_name")
    private String dateColumnName;
    @Column(name = "file_name")
    private String fileName; //just the name after the last '/' (as recieved in request from UI)
    @Column(name = "recon_file_path")
    private String absoluteFilePath; //this is the complete file path with  .csv extension
    @Column(name = "pending_status_value")
    private String pendingStatusValue;
    @Column(name = "success_status_value")
    private String successStatusValue;
    @Column(name = "failed_status_value")
    private String failedStatusValue;
    @Column(name = "unknown_status_value")
    private String unknownStatusValue;
    @Column(name = "file_id")
    private String fileID;
    @Column(name = "is_master_file")
    private boolean isMaster; //whether this is the master in the group or not
    @Column(name = "owner_id")
    private String ownerID; //owner - whoever uploaded this report
    @Column(name = "to_be_reconciled")
    private boolean isToBeReconciled; //whether this file will be part of the files read into the Temp Recon DB
    @Enumerated(EnumType.STRING)
    @Column(name = "file_recon_status")
    private FileReconProgressEnum fileReconProgress; //A recon (group of files) has progress so does each of the individual files
    
    @Column(name = "recon_folder_name")
    private String reconFolderName; //created on the fly by concatenating recon name & current dateTime
    @Column(name = "recon_group_id")
    private String reconGroupID;
    @Column(name = "recon_title")
    private String reconTitle; //title to the reconciliation job
    @Column(name = "report_title", nullable = false)
    private String reportTitle;
    @Column(name = "report_type")
    @Enumerated(EnumType.STRING)
    private ReportType reportType;
    @Column(name = "link_type")
    @Enumerated(EnumType.STRING)
    private LinkType linkType;
    @Column(name = "records_number")
    private int numberOfRecords;
    @Column(name = "fileId_labels_mapping")
    private String fileIDFieldsMapping; //// JSON - { "fileID": {"idlabel", "statuslabel"} }
    @Column(name = "exceptions_file_path") //where all the NOK & UNKOWN statuses belonging to this fileID are written
    private String exceptionsFilePath;
    @Column(name = "file_header_names", length = 3000)
    private String fileHeaderNames; //with this we can get rid of "masterfilefields" column // a JSON representation of the master file fields and blank values i.e. {"Transaction ID":"", "status":""}
    @Column(name = "description_column_name", length = 1000)
    private String descriptionColumnName;
    @Column(name = "hasStatus")
    private boolean hasStatus;

    @Transient
    private int compoundedNumberOfRecords;
    
    @Transient
    private String tempEntityName; //temporarily store entity name gotten from recon details table or one created
    @Transient
    private boolean isReconcilable;
    @Transient
    private boolean isFirstFile;
    @Transient
    private FileProcessDeterminants fileProcessDeterminants;
    @Transient
    private Sheet sheet;
    @Transient
    private boolean invokedByURL;
    
    @Transient
    private String csvEquivalentFile;

    public ReportDetails() {
    }

    public String getReportTitle() {
        return reportTitle.trim();
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getIDColumnName() {
        return IDColumnName.trim();
    }

    public void setIDColumnName(String IDColumnName) {
        this.IDColumnName = IDColumnName;
    }

    public String getFileName() {
        return fileName.trim();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPendingStatusValue() {
        return pendingStatusValue.trim();
    }

    public void setPendingStatusValue(String pendingStatusValue) {
        this.pendingStatusValue = pendingStatusValue;
    }

    public String getSuccessStatusValue() {
        return successStatusValue.trim();
    }

    public void setSuccessStatusValue(String successStatusValue) {
        this.successStatusValue = successStatusValue;
    }

    public String getFailedStatusValue() {
        return failedStatusValue.trim();
    }

    public void setFailedStatusValue(String failedStatusValue) {
        this.failedStatusValue = failedStatusValue;
    }

    public String getUnknownStatusValue() {
        return unknownStatusValue.trim();
    }

    public void setUnknownStatusValue(String unknownStatusValue) {
        this.unknownStatusValue = unknownStatusValue;
    }

    public String getReconGroupID() {
        return reconGroupID;
    }

    public void setReconGroupID(String reconGroupID) {
        this.reconGroupID = reconGroupID;
    }

    public boolean isIsMaster() {
        return isMaster;
    }

    public void setIsMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    public String getOwnerID() {
        return ownerID.trim();
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getStatusColumnName() {
        return statusColumnName.trim();
    }

    public void setStatusColumnName(String statusColumnName) {
        this.statusColumnName = statusColumnName;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public ReportDetails getFileDetails(String fileID) {
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

    public boolean isIsToBeReconciled() {
        return isToBeReconciled;
    }

    public void setIsToBeReconciled(boolean isToBeReconciled) {
        this.isToBeReconciled = isToBeReconciled;
    }

    public String getTempEntityName() {
        return tempEntityName;
    }

    public void setTempEntityName(String tempEntityName) {
        this.tempEntityName = tempEntityName;
    }

    public FileReconProgressEnum getFileReconProgress() {
        return fileReconProgress;
    }

    public void setFileReconProgress(FileReconProgressEnum fileReconProgress) {
        this.fileReconProgress = fileReconProgress;
    }

    public String getFileIDFieldsMapping() {
        return fileIDFieldsMapping;
    }

    public void setFileIDFieldsMapping(String fileIDFieldsMapping) {
        this.fileIDFieldsMapping = fileIDFieldsMapping;
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

    public String getReconTitle() {
        return reconTitle;
    }

    public void setReconTitle(String reconTitle) {
        this.reconTitle = reconTitle;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public void setAbsoluteFilePath(String absoluteFilePath) {
        this.absoluteFilePath = absoluteFilePath;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public int getCompoundedNumberOfRecords() {
        return compoundedNumberOfRecords;
    }

    public void setCompoundedNumberOfRecords(int compoundedNumberOfRecords) {
        this.compoundedNumberOfRecords = compoundedNumberOfRecords;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public String getAmountColumnName() {
        return amountColumnName;
    }

    public void setAmountColumnName(String amountColumnName) {
        this.amountColumnName = amountColumnName;
    }

    public String getDateColumnName() {
        return dateColumnName;
    }

    public void setDateColumnName(String dateColumnName) {
        this.dateColumnName = dateColumnName;
    }

    public String getExceptionsFilePath() {
        return exceptionsFilePath;
    }

    public void setExceptionsFilePath(String exceptionsFilePath) {
        this.exceptionsFilePath = exceptionsFilePath;
    }

    @Override
    public void setId(long id) {
        super.setId(id);
    }

    public String getFileHeaderNames() {
        return fileHeaderNames;
    }

    public void setFileHeaderNames(String fileHeaderNames) {
        this.fileHeaderNames = fileHeaderNames;
    }

    public String getDescriptionColumnName() {
        return descriptionColumnName;
    }

    public void setDescriptionColumnName(String descriptionColumnName) {
        this.descriptionColumnName = descriptionColumnName;
    }

    public String getLinkIDColumnName() {
        return linkIDColumnName;
    }

    public void setLinkIDColumnName(String linkIDColumnName) {
        this.linkIDColumnName = linkIDColumnName;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public boolean isIsReconcilable() {
        return isReconcilable;
    }

    public void setIsReconcilable(boolean isReconcilable) {
        this.isReconcilable = isReconcilable;
    }

    public boolean isIsFirstFile() {
        return isFirstFile;
    }

    public void setIsFirstFile(boolean isFirstFile) {
        this.isFirstFile = isFirstFile;
    }

    public FileProcessDeterminants getFileProcessDeterminants() {
        return fileProcessDeterminants;
    }

    public void setFileProcessDeterminants(FileProcessDeterminants fileProcessDeterminants) {
        this.fileProcessDeterminants = fileProcessDeterminants;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public boolean isInvokedByURL() {
        return invokedByURL;
    }

    public void setInvokedByURL(boolean invokedByURL) {
        this.invokedByURL = invokedByURL;
    }

    public boolean isHasStatus() {
        return hasStatus;
    }

    public void setHasStatus(boolean hasStatus) {
        this.hasStatus = hasStatus;
    }

    public String getCsvEquivalentFile() {
        return csvEquivalentFile;
    }

    public void setCsvEquivalentFile(String csvEquivalentFile) {
        this.csvEquivalentFile = csvEquivalentFile;
    }

}
