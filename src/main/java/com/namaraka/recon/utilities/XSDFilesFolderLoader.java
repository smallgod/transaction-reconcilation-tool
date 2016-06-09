/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author smallgod
 */
public class XSDFilesFolderLoader {

    private String xsdFolderLocation;

    private XSDFilesFolderLoader() {

        loadXSDFilesfolderLoc();

    }

    private static class LoadXSDFilesSingletonHolder {

        private static final XSDFilesFolderLoader INSTANCE = new XSDFilesFolderLoader();
    }

    public static XSDFilesFolderLoader getInstance() {
        return LoadXSDFilesSingletonHolder.INSTANCE;
    }

    protected Object readResolve() {
        return getInstance();
    }

    private void loadXSDFilesfolderLoc() {
        String xsdFilesFolder = getFolderName("xsdFolderLoc");
        setXsdFolderLocation(xsdFilesFolder);
    }

    private String getFolderName(String xsdFolderLocJNDIstring) {

        InitialContext initialContext;
        Context environmentContext;
        String folderLocation;

        try {
            initialContext = new InitialContext();
            environmentContext = (Context) initialContext.lookup("java:/comp/env");
            folderLocation = (String) environmentContext.lookup(xsdFolderLocJNDIstring);
        } catch (NamingException ne) {
            System.err.println("Exiting. Failed to load XSD Files folder location - JNDI naming exception: " + ne.getMessage());
            System.exit(1);
            return null;
        }
        return folderLocation;
    }

    private void setXsdFolderLocation(String xsdFolderLocation) {
        this.xsdFolderLocation = xsdFolderLocation;
    }

    public String getXsdFolderLocation() {
        return xsdFolderLocation;
    }
}
