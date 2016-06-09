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
@Table(name = "equinox_entity1")// these table names need to be created on the fly
public class EquinoxDetails extends BaseModel implements Auditable, Processeable, FIleColumns, Serializable {

    private static final long serialVersionUID = 3011587131740310457L;

    @Column(name = "recon_trans_id", nullable = false)
    private String reconTransID;

    @Column(name = "original_tracer_no", nullable = false)
    private String originalTracerNo;

    @Column(name = "equinox_description")
    private String equinoxDescription;

    @Column(name = "posting_date_time")
    private String postingDateTime;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "amount")
    private String amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private CenteServiceType centeServiceType;

    @Column(name = "all_file_columns", length = 15000) //might want to change this - what if the data is too long
    private String allFileColumns;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "report_details_id", referencedColumnName = "id")
    private ReportDetails reportDetails;

    public EquinoxDetails() {
    }

    /**
     * Gets the value of the msisdn property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEquinoxDescription() {
        return equinoxDescription;
    }

    /**
     * Sets the value of the equinoxDescription property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setEquinoxDescription(String value) {
        this.equinoxDescription = value;
    }

    public boolean isSetCustomermsisdn() {
        return (this.equinoxDescription != null);
    }

    /**
     * Gets the value of the accountNumber property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the value of the accountNumber property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAccountNumber(String value) {
        this.accountNumber = value;
    }

    public boolean isSetPaymentnarration() {
        return (this.accountNumber != null);
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

    public String getPostingDateTime() {
        return postingDateTime;
    }

    public void setPostingDateTime(String postingDateTime) {
        this.postingDateTime = postingDateTime;
    }

    public String getOriginalTracerNo() {
        return originalTracerNo;
    }

    public void setOriginalTracerNo(String originalTracerNo) {
        this.originalTracerNo = originalTracerNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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

    public String getReconTransID() {
        return reconTransID;
    }

    public void setReconTransID(String reconTransID) {
        this.reconTransID = reconTransID;
    }

}
