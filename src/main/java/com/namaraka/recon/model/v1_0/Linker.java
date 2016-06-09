package com.namaraka.recon.model.v1_0;

import com.namaraka.recon.IF.TxnRecordIF;
import com.namaraka.recon.constants.CenteServiceType;
import com.namaraka.recon.utilities.Auditable;
import com.namaraka.recon.utilities.DBMSXMLObject;
import com.namaraka.recon.utilities.Processeable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 *
 * @author smallgod (c) Namaraka Technologies
 */
//@Entity(name = "recon_entity1")
@Entity
@DynamicUpdate(value = true)
@SelectBeforeUpdate(value = true)
@Table(name = "linker_entity1")// these table names need to be created on the fly

public class Linker extends BaseModel implements Auditable, Processeable, TxnRecordIF, Serializable {
    private static final long serialVersionUID = 5154170140182418601L;

    @Column(name = "special_id_value", nullable = true, unique = false)   // a special ID 22235 such as that extracted from a longID e.g. 22235-Apr26th2015-Pull
    private String specialIDValue;
    
    @Column(name = "id_value", nullable = true, unique = false)   // we can have the ID value not given in the file - in this case the row becomes NOK in that file
    private String IDValue;
    
    @Column(name = "link_id_value", nullable = true, unique = false)   // we can have the ID value not given in the file - in this case the row becomes NOK in that file
    private String linkIDValue;

    @Column(name = "amount")
    private String amount;
    
    @Column(name = "transaction_description", length = 1000)
    private String transactionDescription;

//    @Column(name = "id_first_inserted", nullable = false, unique = false)   
//    private String idFirstInsertedFrom; // A JSON string with the file ID and the transaction ID label name from which this ID was first inserted from e.g. " { "220":"Trans ID" }
//    
    @Column(name = "file_keyid", unique = false)   // Exception transactions - i.e. those that exist only in one report will have this field populated
    private String reportFileKeyID; //this should be treated as a foreign key from ReportDetails table - with cascade-all property

    @Column(name = "recon_group_id", nullable = false)
    private String reconGroupID; // reportFileKeyId above can also give us the reconGroupID from the reportDetails table but we nevertheless include it here - we might improve this later and remove it

    @Column(name = "transaction_statuses")
    private String transactionStatuses; // string in the JSON string form {"fileId":"statusValue", "field":statusValue"}
    @Transient
    private String tempTransStatusHolder; //temporarily holds the status for the transaction being read  in the normal string form "statusValue" 

    @Column(name = "comments")
    @Transient
    private String comments;

    @Column(name = "master_file_records", length = 3000) //might want to change this - what if the data is too long
    private String fileRecordsDetails;

    @Column(name = "marked_as_exception")
    //@Transient
    private boolean isMarkedAsException; //Dont store this in DB. Record exists and has identitical status in all reconciled files

    @Column(name = "record_inmaster_ok")
    private boolean isRecordExistinMasterFile; //whether a record exists in the master file - some may exist in other files but not master - get master file details to add from those files where they exist

    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private CenteServiceType centeServiceType;
    
    @Column(name = "cell_values", length = 5000)
    private ArrayList<String> cellValues;    
    
    private Linker(String reconIDValue, String transactionStatuses, String comments, String masterFileRecords, boolean isRecordFoundInAllFiles, boolean isRecordFoundInMasterFile) {

        this.IDValue = reconIDValue;
        this.transactionStatuses = transactionStatuses;
        this.comments = comments;
        this.fileRecordsDetails = masterFileRecords;
        this.isMarkedAsException = isRecordFoundInAllFiles;
        this.isRecordExistinMasterFile = isRecordFoundInMasterFile;
    }

    public Linker() {
        this("", "", "", "", false, false); //initialise fields
    }

    /**
     * Gets the value of the transactionStatuses property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTransactionStatuses() {
        return transactionStatuses;
    }

    /**
     * Sets the value of the transactionStatuses property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setTransactionStatuses(String value) {
        this.transactionStatuses = value;
    }

    public boolean isSetCustomermsisdn() {
        return (this.transactionStatuses != null);
    }

    /**
     * Gets the value of the comments property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the value of the comments property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setComments(String value) {
        this.comments = value;
    }

    public boolean isSetPaymentnarration() {
        return (this.comments != null);
    }

    /**
     * Gets the value of the fileRecordsDetails property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFileRecordsDetails() {
        return fileRecordsDetails;
    }

    /**
     * Sets the value of the fileRecordsDetails property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setFileRecordsDetails(String value) {
        this.fileRecordsDetails = value;
    }

    public boolean isSetCustomeridentity() {
        return (this.fileRecordsDetails != null);
    }

    // This ensures that a check is always performed when the model is saved, 
    //instead of having to manually add the check to every piece of code that saves the model.
//    @PrePersist
//    public void prepareToInsert() {//cant we do this check inside the Interceptor
//        List<Servicerequest> conflicts = find("user=?").fetch();
//        if (!conflicts.isEmpty()) {
//            throw new MyCustomException("username `" + name + "` already exists");
//        }
//    }
    public boolean isIsMarkedAsException() {
        return isMarkedAsException;
    }

    public void setIsMarkedAsException(boolean isMarkedAsException) {
        this.isMarkedAsException = isMarkedAsException;
    }

    public boolean isIsRecordExistinMasterFile() {
        return isRecordExistinMasterFile;
    }

    public void setIsRecordExistinMasterFile(boolean isRecordExistinMasterFile) {
        this.isRecordExistinMasterFile = isRecordExistinMasterFile;
    }

    @Override
    public DBMSXMLObject getXMLObject() {
        return this;
    }

    @Override
    public String getUsername() {
        return "N/A";
    }

    public String getIDValue() {
        return IDValue;
    }

    public void setIDValue(String IDValue) {
        this.IDValue = IDValue;
    }

    public String getTempTransStatusHolder() {
        return tempTransStatusHolder;
    }

    public void setTempTransStatusHolder(String tempTransStatusHolder) {
        this.tempTransStatusHolder = tempTransStatusHolder;
    }

//    public long getReportFileKeyID() {
//        return reportFileKeyID;
//    }
//
//    public void setReportFileKeyID(long reportFileKeyID) {
//        this.reportFileKeyID = reportFileKeyID;
//    }
    public String getReconGroupID() {
        return reconGroupID;
    }

    public void setReconGroupID(String reconGroupID) {
        this.reconGroupID = reconGroupID;
    }

    public String getReportFileKeyID() {
        return reportFileKeyID;
    }

    public void setReportFileKeyID(String reportFileKeyID) {
        this.reportFileKeyID = reportFileKeyID;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSpecialIDValue() {
        return specialIDValue;
    }

    public void setSpecialIDValue(String specialIDValue) {
        this.specialIDValue = specialIDValue;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public CenteServiceType getCenteServiceType() {
        return centeServiceType;
    }

    public void setCenteServiceType(CenteServiceType centeServiceType) {
        this.centeServiceType = centeServiceType;
    }

    public String getLinkIDValue() {
        return linkIDValue;
    }

    public void setLinkIDValue(String linkIDValue) {
        this.linkIDValue = linkIDValue;
    }

    public ArrayList<String> getCellValues() {
        return cellValues;
    }

    public void setCellValues(ArrayList<String> cellValues) {
        this.cellValues = cellValues;
    }
}
