/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author smallgod
 */
public class ComparableIDGenerator {
    
    
    //Centenary - Utilities
    //ID + Amount + Status + TxnType
    
    //Centenary - B2C - Airtel
    //ID + Amount + Status
    
    //Centenary - B2C - MTN
    //ID + Amount + Status
    
    //Centenary - C2B - Airtel
    //ID + Amount + Status
    
    //Centenary - C2B - MTN
    //ID + Amount + Status
    
    
    
    //put this method in apropriate class    
    List<String> tempRecords = new ArrayList<>(0x2710); //10,000 records read in as CSV
    
    
    
    /**Scenario :

A folder in Linux system. I want to loop through every .xls file in a folder.

This folder typically consists of various folders, various filetypes (.sh, .pl,.csv,...).

All I want to do is loop through all files in the root and execute a program only on .xls files.

xls2csv is the program i need to run

for example:

i have 300 directories at /home/ftp_account/user1 up to user300 w/c contains .xls files in every folder,i want to convert all .xls files then move the converted files to /home/ftp_account/user1/converted

take note: converted files for user1 will go to /home/ftp_users/user1/converted

files for user2 will go to /home_ftp_users/user2/converted files for user3 will go to /home_ftp_users/user3/converted etc....
* 
* /
    /*#!/bin/bash
    for dir in /home/ftp_users/user{1..300}; 
      do
      for file in $dir/*.xls; do
        fn=$(basename ${file})
        fn=${fn%.*}
        mkdir -p $dir/converted
        xls2csv $file > $dir/converted/${fn}.csv
      done
    done*/
    
    
    
    /*for f in *.csv; 
       do ssconvert "$f" "${f%.csv}.xlsx"; done*/
    
    
/*Multimap<String, String> values = HashMultimap.create();
values.put("user1", "value1");
values.put("user2", "value2");
values.put("user3", "value3");
values.put("user1", "value4");

System.out.println(values.get("user1"));
System.out.println(values.get("user2"));
System.out.println(values.get("user3"));
Outputs:

[value4, value1]
[value2]
[value3]*/
    
    
    
}
