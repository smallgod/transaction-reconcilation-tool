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
public interface FileProcessingObserved {
    
    //methods to register and unregister observers
    public void register(FileProcessingObserver observer);
    public void unregister(FileProcessingObserver observer);
     
    //method to notify observers of change
    public void  notifyObservers() throws MyCustomException;
     
    //method to get updates from subject
    public Object getUpdate(FileProcessingObserver observer);
    
    public void unregisterAllObservers();
    
}
