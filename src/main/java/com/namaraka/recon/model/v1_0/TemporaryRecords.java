package com.namaraka.recon.model.v1_0;

import com.namaraka.recon.utilities.Auditable;
import com.namaraka.recon.utilities.DBMSXMLObject;
import com.namaraka.recon.utilities.Processeable;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

//@Entity(name = "recon_entity1")
@Entity
@DynamicUpdate(value = true)
@SelectBeforeUpdate(value = true)
@Table(name = "temporary_records")// these table names need to be created on the fly

public class TemporaryRecords extends BaseModel implements Auditable, Processeable, Serializable {

    private static final long serialVersionUID = -701373611393153817L;

    @Column(name = "generated_id", nullable = true, unique = false)
    private String generatedID;

    @Column(name = "is_failed_successful")
    private boolean isFailedOrSuccessful;

    @Column(name = "file_id")
    private String fileID;

    @Column(name = "row_details", length = 10000) //might want to change this - what if the data is too long
    private String rowDetails;

    public TemporaryRecords(String generatedID, boolean isFailedOrSuccessful, String fileID, String rowDetails) {
        this.generatedID = generatedID;
        this.isFailedOrSuccessful = isFailedOrSuccessful;
        this.fileID = fileID;
        this.rowDetails = rowDetails;
    }

    public TemporaryRecords() {
        this("", Boolean.FALSE, "", ""); //initialise fields
    }

    @Override
    public DBMSXMLObject getXMLObject() {
        return this;
    }

    @Override
    public String getUsername() {
        return "N/A";
    }

    public String getGeneratedID() {
        return generatedID;
    }

    public void setGeneratedID(String generatedID) {
        this.generatedID = generatedID;
    }

    public String getRowDetails() {
        return rowDetails;
    }

    public void setRowDetails(String rowDetails) {
        this.rowDetails = rowDetails;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public boolean isIsFailedOrSuccessful() {
        return isFailedOrSuccessful;
    }

    public void setIsFailedOrSuccessful(boolean isFailedOrSuccessful) {
        this.isFailedOrSuccessful = isFailedOrSuccessful;
    }

}
