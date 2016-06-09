/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.IF;

import com.namaraka.recon.exceptiontype.MyCustomException;

/**
 *
 * @author smallgod
 */
public interface FileProcessingObserver {
    
   //method to tell the observer to startRecon, used by subject
    public void startRecon() throws MyCustomException;
     
    //attach with subject to observe
    public void setSubject(FileProcessingObserved subject);
    
    
}
