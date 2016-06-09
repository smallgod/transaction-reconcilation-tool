package com.namaraka.recon.model.v1_0;

import com.namaraka.recon.constants.CenteServiceType;
import com.namaraka.recon.utilities.Auditable;
import com.namaraka.recon.utilities.DBMSXMLObject;
import com.namaraka.recon.utilities.FIleColumns;
import com.namaraka.recon.utilities.Processeable;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 *
 * @author smallgod (c) Namaraka Technologies
 */
//@Entity(name = "equinox_entity1")
@Entity
@DynamicUpdate(value = true)
@SelectBeforeUpdate(value = true)
@Table(name = "elma_entity1")// these table names need to be created on the fly
public class ElmaDetails extends BaseModel implements Auditable, Processeable, FIleColumns, Serializable {
    
    private static final long serialVersionUID = 8149796784175866725L;
    
    @Column(name = "recon_trans_id", nullable = false)
    private String reconTransID;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    @Column(name = "amount")
    private String amount;
    
    @Column(name = "date_created")
    private String dateCreated;
    
    @Column(name = "transaction_reference")
    private String transactionReference;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "payment_details")
    private String paymentDetails; 
    
    @Column(name = "all_file_columns", length = 5000) //might want to change this - what if the data is too long
    private String allFileColumns;
    
    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private CenteServiceType centeServiceType;
    
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "report_details_id", referencedColumnName = "id")
    private ReportDetails reportDetails;
    
    public ElmaDetails() {        
    }
    
    /**
     * Gets the value of the allFileColumns property.
     *
     * @return possible object is {@link String }
     *
     */
    @Override
    public String getAllFileColumns() {
        return allFileColumns;
    }

    /**
     * Sets the value of the allFileColumns property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAllFileColumns(String value) {
        this.allFileColumns = value;
    }

    public boolean isSetCustomeridentity() {
        return (this.allFileColumns != null);
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

    @Override
    public DBMSXMLObject getXMLObject() {
        return this;
    }

    @Override
    public String getUsername() {
        return "N/A";
    }

    public CenteServiceType getCenteServiceType() {
        return centeServiceType;
    }

    public void setCenteServiceType(CenteServiceType centeServiceType) {
        this.centeServiceType = centeServiceType;
    }

    public ReportDetails getReportDetails() {
        return reportDetails;
    }

    public void setReportDetails(ReportDetails reportDetails) {
        this.reportDetails = reportDetails;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getReconTransID() {
        return reconTransID;
    }

    public void setReconTransID(String reconTransID) {
        this.reconTransID = reconTransID;
    }
}