/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import com.namaraka.recon.config.v1_0.Appconfig;
import com.namaraka.recon.utilities.BindXmlAndPojo;
import com.namaraka.recon.utilities.TemporaryMap;
import com.namaraka.recon.utilities.XSDFilesFolderLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.ValidationException;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author smallgod
 */
//read: http://howtodoinjava.com/2012/10/22/singleton-design-pattern-in-java/
//and: http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
public final class ApplicationPropertyLoaderOld implements Serializable {
    private static final long serialVersionUID = -7803067555627544107L;

    private Appconfig appProps;
    private Map<String, TemporaryMap> validatedCustomerDetailsTempStorage;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationPropertyLoaderOld.class);

    private ApplicationPropertyLoaderOld() {
        loadAllApplicationConfigs(); //called only once
    }

    
    private static class AppPropertyLoaderSingletonHolder {

        private static final ApplicationPropertyLoaderOld INSTANCE = new ApplicationPropertyLoaderOld();
    }

    public static ApplicationPropertyLoaderOld getInstance() {
        return AppPropertyLoaderSingletonHolder.INSTANCE;
    }

    protected Object readResolve() {
        return getInstance();
    }

    private void loadAppConfigProperties() throws NamingException, FileNotFoundException, UnsupportedEncodingException, JAXBException, NullPointerException, ValidationException, SAXException {

        //String appConfigsPropsFileLoc = getConfigFileName("appConfigsPropsFile");
        //Appconfig appConfigs = (Appconfig) BindXmlAndPojo.xmlFileToObject(appConfigsPropsFileLoc, Appconfig.class);

        //setAppProps(appConfigs);
    }

    private void loadLog4jProperties() throws NamingException {

        String log4jPropsFileLoc = getConfigFileName("log4jPropsFile");
        DOMConfigurator.configure(log4jPropsFileLoc); //XML configurator
        //PropertyConfigurator.configure(log4jPropsFileLoc);//Property file configurator
    }

    /**
     * load all variables here that we want to be accessed throughout the
     * application
     */
    private void loadGlobalVariables() {
        validatedCustomerDetailsTempStorage = new HashMap<>();
    }

    private String getConfigFileName(String propsFileJNDIstring) throws NamingException {

        InitialContext initialContext = new InitialContext();
        Context environmentContext = (Context) initialContext.lookup("java:/comp/env");
        String propsFileLocation = (String) environmentContext.lookup(propsFileJNDIstring);

        return propsFileLocation;
    }

    private void loadAllApplicationConfigs() {

        try {

            loadLog4jProperties();
            XSDFilesFolderLoader.getInstance();
            loadAppConfigProperties();

            loadGlobalVariables();

        } catch (NullPointerException npe) {
            System.err.println("Exiting. Failed to load system properties - Null or non-existent entry: " + npe.getMessage());
            System.exit(1);
            /*try {
             fileInputStream.close();
             } catch (IOException ex) {
             System.err.println("Failed to close the properties file");
             }*/

        } catch (UnsupportedEncodingException unexc) {
            System.err.println("Exiting. Failed to load system properties - Unsupported XML Encoding exception: " + unexc.getMessage());
            System.exit(1);
        } catch (NamingException ne) {
            System.err.println("Exiting. Failed to load system properties - JNDI naming exception: " + ne.getMessage());
            System.exit(1);
        } catch (NumberFormatException ne) {
            System.err.println("Exiting. Failed to load system properties - String value found, required Integer: " + ne.getMessage());
            System.exit(1);
        } catch (FileNotFoundException ne) {
            System.err.println("Exiting. Failed to load system properties - File not found: " + ne.getMessage());
            System.exit(1);
        } catch (SAXException saxex) {
            System.err.println("Exiting. Failed to load system properties - SAX exception: " + saxex.getMessage());
            System.exit(1);
        } catch (MarshalException exc) {
            System.err.println("Exiting. Failed to load system properties - marshal exception: " + exc.getMessage());
            System.exit(1);
        } catch (ValidationException exc) {
            System.err.println("Exiting. Failed to load system properties - validation exception: " + exc.getMessage());
            System.exit(1);
        } catch (JAXBException jbe) {
            System.err.println("Exiting. Failed to load system properties - JAXB exception: " + jbe.getMessage());
            System.exit(1);
        } catch (Exception exc) {
            System.err.println("Exiting. Failed to load system properties - General exception: " + exc.getMessage());
            System.exit(1);
        }
    }

    private void setAppProps(Appconfig appProps) {
        this.appProps = appProps;
    }

    public Appconfig getAppProps() {
        return appProps;
    }

    public Map<String, TemporaryMap> getValidatedCustomerDetailsTempStorage() {
        return validatedCustomerDetailsTempStorage;
    }
}
