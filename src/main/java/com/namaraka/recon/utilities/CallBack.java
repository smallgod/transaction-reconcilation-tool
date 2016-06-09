/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.ReportDetails;
import org.hibernate.StatelessSession;

/**
 *
 * @author smallgod
 */
public interface CallBack {

    public void execute(Object data);

    public int readRecordsFromFile(ReportDetails reportFileDetails, StatelessSession tempSession, long currentNumberOfRecordsInDB) throws MyCustomException;

    public int processLinkFileRecords(ReportDetails reportFileDetails, StatelessSession tempSession) throws MyCustomException;

    public int readRecordsFromDB(ReportDetails reportFileDetails, StatelessSession tempSession, long currentNumberOfRecordsInDB) throws MyCustomException;

}
