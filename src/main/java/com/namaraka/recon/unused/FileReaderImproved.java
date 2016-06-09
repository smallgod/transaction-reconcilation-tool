/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.unused;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example shows how to use the event API for reading a file.
 */
public class FileReaderImproved implements HSSFListener {

    private SSTRecord sstrec;
    private static final Logger logger = LoggerFactory.getLogger(FileReaderImproved.class);

    /**
     * This method listens for incoming records and handles them as required.
     *
     * @param record The record that was found while reading.
     */
    @Override
    public void processRecord(Record record) {
        
                
        switch (record.getSid()) {         
            
            
            //the BOFRecord can represent either the beginning of a sheet or the workbook
            case BOFRecord.sid:
                
                System.out.println("we are hereeree");
                
                BOFRecord bof = (BOFRecord) record;
                
                if (bof.getType() == bof.TYPE_WORKBOOK) {                    
                    
                    logger.debug("Encountered workbook");
                    
                    // assigned to the class level member
                } else if (bof.getType() == bof.TYPE_WORKSHEET) {
                    logger.debug("Encountered sheet reference");
                }
                break;
                
            case BoundSheetRecord.sid:
                
                BoundSheetRecord bsr = (BoundSheetRecord) record;
                logger.debug("New sheet named: " + bsr.getSheetname());
                break;
                
            case RowRecord.sid:
                
                RowRecord rowrec = (RowRecord) record;
                System.out.println("Row found, first column at "  + rowrec.getFirstCol() + " last column at " + rowrec.getLastCol());
                
                logger.debug("Row found, first column at "  + rowrec.getFirstCol() + " last column at " + rowrec.getLastCol());
                break;
                
            case NumberRecord.sid:
                
                NumberRecord numrec = (NumberRecord) record;
                System.out.println("Cell found with value " + numrec.getValue() + " at row " + numrec.getRow() + " and column " + numrec.getColumn());
                logger.debug("Cell found with value " + numrec.getValue() + " at row " + numrec.getRow() + " and column " + numrec.getColumn());
                break;
                
            // SSTRecords store a array of unique strings used in Excel.
            case SSTRecord.sid:                
                
                sstrec = (SSTRecord) record;
                System.out.println("num unique:: " + sstrec.getNumUniqueStrings());
                                
                for (int k = 0; k < sstrec.getNumUniqueStrings(); k++) {
                    logger.debug("String table value " + k + " = " + sstrec.getString(k));
                    System.out.println("String table value " + k + " = " + sstrec.getString(k));
                    
                }
                break;
                
            case LabelSSTRecord.sid:
                
                LabelSSTRecord lrec = (LabelSSTRecord) record;
                logger.debug("String cell found with value " + sstrec.getString(lrec.getSSTIndex()));
                System.out.println("String cell found with value " + sstrec.getString(lrec.getSSTIndex()));
                break;
            
            default:
                System.out.println("Default here");
        }
    }

    /**
     * Read an excel file and spit out what we find.
     *
     * @param args Expect one argument that is the file to read.
     * @throws IOException When there is an error processing the file.
     */
    public static void main(String[] args) throws IOException {
        
        // create a new file input stream with the input file specified at the command line
        FileInputStream fin = new FileInputStream("/home/smallgod/NetBeansProjects/recontool/web/WEB-INF/reconfolder/Equinox_01-07-2015-071458.xls");
        
        // create a new org.apache.poi.poifs.filesystem.Filesystem
        POIFSFileSystem poifs = new POIFSFileSystem(fin);
        
        // get the Workbook (excel part) stream in a InputStream
        InputStream din = poifs.createDocumentInputStream("Workbook");
        
        // construct out HSSFRequest object
        HSSFRequest req = new HSSFRequest();
        
        // lazy listen for ALL records with the listener shown above
        req.addListenerForAllRecords(new FileReaderImproved());
        
        // create our event factory
        HSSFEventFactory factory = new HSSFEventFactory();
        
        // process our events based on the document input stream
        factory.processEvents(req, din);
        
        // once all the events are processed close our file input stream
        fin.close();
        
        // and our document input stream (don't want to leak these!)
        din.close();
        logger.debug("done.");
    }

}
