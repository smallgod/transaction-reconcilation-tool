/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import java.util.Map;

/**
 *
 * @author smallgod
 */
public interface TemporaryMap extends XMLObject{
    
    String getMapKey();
    String getTriggerName();
    long getDeleteInterval();
    String getDataMapKeyName();
    Map<String, TemporaryMap> getTemporaryMap();
    String getJobListerName();
}
