/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.IF;

import com.namaraka.recon.constants.CenteServiceType;
import java.util.ArrayList;

/**
 *
 * @author smallgod
 */
public interface TxnRecordIF {

    public String getIDValue();
            
    public void setReportFileKeyID(String id);

    public void setReconGroupID(String reconID);

    public void setCenteServiceType(CenteServiceType serviceType);

    public void setAmount(String amountColumnValue);

    public void setTransactionDescription(String descriptionColumnValue);

    public void setIDValue(String IDColumnValue);

    public void setSpecialIDValue(String specialIDValue);

    public void setLinkIDValue(String linkIDColumnValue);

    public void setTransactionStatuses(String fileStatusValue);

    public void setTempTransStatusHolder(String statusColumnValue);

    public void setFileRecordsDetails(String fileRecordsDetails);

    public void setIsRecordExistinMasterFile(boolean TRUE);
    
    public void setCellValues(ArrayList<String> cellValues);
    
}
