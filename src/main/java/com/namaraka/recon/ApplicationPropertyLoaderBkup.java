/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import com.namaraka.recon.config.v1_0.Appconfig;
import com.namaraka.recon.utilities.AuditTrailInterceptor;
import com.namaraka.recon.utilities.XSDFilesFolderLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.ValidationException;
import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author smallgod
 */
//read: http://howtodoinjava.com/2012/10/22/singleton-design-pattern-in-java/
//and: http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
public final class ApplicationPropertyLoaderBkup implements Serializable {


    private SessionFactory sessionFactory;
    private Appconfig appProps;
    

    private static final Logger logger = LoggerFactory.getLogger(ApplicationPropertyLoader.class);

    private static final long serialVersionUID = 8041559610391707793L;

    private ApplicationPropertyLoaderBkup() {
        loadAllApplicationConfigs(); //called only once
    }

    private static class AppPropertyLoaderSingletonHolder {

        private static final ApplicationPropertyLoaderBkup INSTANCE = new ApplicationPropertyLoaderBkup();
    }

    public static ApplicationPropertyLoaderBkup getInstance() {
        return AppPropertyLoaderSingletonHolder.INSTANCE;
    }

    protected Object readResolve() {
        return getInstance();
    }

    
    private void loadAppConfigProperties() throws NamingException, FileNotFoundException, UnsupportedEncodingException, JAXBException, SAXException {

        //String appConfigsPropsFileLoc = getConfigFileName("appConfigsPropsFile");
        //Appconfig appConfigs = (Appconfig) BindXmlAndPojo.xmlFileToObject(appConfigsPropsFileLoc, Appconfig.class);

        //setAppProps(appConfigs);

        
        
        
        //test
        //String serviceRequestFile = getConfigFileName("ServiceRequest");
        //Servicerequest servRequest = (Servicerequest) BindXmlAndPojo.xmlFileToObject(serviceRequestFile, Servicerequest.class);
        //System.out.println("usernamed  : " + servRequest.getUsername());
        //System.out.println("serviceIded: " + servRequest.getServiceid());
    }

    private void loadLog4jProperties() throws NamingException {

        String log4jPropsFileLoc = getConfigFileName("log4jPropsFile");
        DOMConfigurator.configure(log4jPropsFileLoc); //XML configurator  //PropertyConfigurator.configure(log4jPropsFileLoc);//Property file configurator
    }
    

    private void loadHibernateProperties() throws NamingException, HibernateException {

        String hibernatePropsFileLoc = getConfigFileName("hibernatePropsFile");
        File hibernatePropsFile = new File(hibernatePropsFileLoc);

        String customTypesPropsFileLoc = getConfigFileName("customTypesPropsFile");

        Configuration configuration = new Configuration();
        configuration.configure(hibernatePropsFile);
        //configuration.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
        //configuration.addResource(customTypesPropsFileLoc);
        configuration.setInterceptor(new AuditTrailInterceptor());
        //configuration.setInterceptor(new InterceptorClass());

        StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();

        SessionFactory sessFactory = configuration.buildSessionFactory(serviceRegistry);

        setSessionFactory(sessFactory);

        logger.debug("opened a session factory");
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
            loadHibernateProperties();
            loadAppConfigProperties();
            
            logger.info("Yeeyyyiii Praise the LORD - props loaded - app started!");

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
        } catch (HibernateException hbe) {
            System.err.println("Exiting. Failed to load system properties - Hibernate exception: " + hbe.getMessage());
            System.exit(1);
        }  catch (SAXException saxex) {
            System.err.println("Exiting. Failed to load system properties - SAX exception: " + saxex.getMessage());
            System.exit(1);
        } catch (MarshalException exc) {
            System.err.println("Exiting. Failed to load system properties - marshal exception: " + exc.getMessage());
            System.exit(1);
        } catch (ValidationException exc) {
            System.err.println("Exiting. Failed to load system properties - validation exception: " + exc.getMessage());
            System.exit(1);
        }catch (JAXBException jbe) {
            System.err.println("Exiting. Failed to load system properties - JAXB exception: " + jbe.getMessage());
            System.exit(1);
        }catch (Exception exc) {
            System.err.println("Exiting. Failed to load system properties - General exception: " + exc.getMessage());
            System.exit(1);
        }
    }

    protected void closeHibernateSessionFactory() {

        if (getSessionFactory() != null && !getSessionFactory().isClosed()) {
            getSessionFactory().close();

            logger.debug("closed Hibernate session factory");
        }
        setSessionFactory(null);
    }

    private void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {

        synchronized (this) {
            if (sessionFactory == null || sessionFactory.isClosed()) {
                rebuildSessionFactory();
            }
        }
        return sessionFactory;
    }

    private void setAppProps(Appconfig appProps) {
        this.appProps = appProps;
    }

    public Appconfig getAppProps() {
        return appProps;
    }

    ///////////////// un-used operations ///////////////////////////////
    //might introduce thread problems (more than 1 thread accessing at the same time - but since this can only occur at 
    //context shutdown most likely it will not happen)
    private void rebuildSessionFactory() {

        synchronized (this) {
            if (sessionFactory == null || sessionFactory.isClosed()) {
                try {
                    loadHibernateProperties();
                } catch (NamingException ne) {
                    logger.error("naming exception trying to rebuild sessionFactory: " + ne.getMessage());
                } catch (HibernateException hbe) {
                    logger.error("hibernate exception trying to rebuild sessionFactory: " + hbe.getMessage());
                }
            } else {
                logger.debug("inside rebuildSessionFactory but sessionFactory is NOT null - so wont rebuild");
            }
        }
    }

    private void closeHibernateSessionFactory(ServletContextEvent servletContextEvent) {

        SessionFactory openedSessionFactory = (SessionFactory) servletContextEvent.getServletContext().getAttribute("SessionFactory");
        if (openedSessionFactory != null && !openedSessionFactory.isClosed()) {
            openedSessionFactory.close();
        }
    }

    private void setSessionFactoryAttribute(ServletContextEvent servletContextEvent) throws NullPointerException {

        if (sessionFactory == null) {
            System.out.println("newSessionFactory is null");
            throw new NullPointerException();
        }
        servletContextEvent.getServletContext().setAttribute("SessionFactory", sessionFactory);
    }

    private Properties getProperties(FileInputStream fileInputStream) throws IOException {

        Properties properties = new Properties();
        properties.load(fileInputStream);

        return properties;
    }

    private FileInputStream getConfigProperties(String propsFileJNDIstring) throws NamingException, FileNotFoundException, IOException {

        String propsFileLocation = getConfigFileName(propsFileJNDIstring);

        FileInputStream fileInputStream = new FileInputStream(propsFileLocation);

        return fileInputStream;
    }

    private String getWebAppPath(ServletConfig config) {

        ServletContext servletContext = config.getServletContext();

        return (servletContext.getRealPath("/").trim());
    }

    private void loadLog4jProperties(Properties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void loadHibernateProperties(Properties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void loadAppConfigProperties(Properties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
