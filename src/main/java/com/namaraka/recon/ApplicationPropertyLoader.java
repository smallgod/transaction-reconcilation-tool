/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import com.namaraka.recon.config.v1_0.Appconfig;
import com.namaraka.recon.utilities.AuditTrailInterceptor;
import com.namaraka.recon.utilities.BindXmlAndPojo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
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
public final class ApplicationPropertyLoader implements Serializable {

    private static final long serialVersionUID = -6178114706454102972L;

    private static final String APPCONFIGS_FILENAME = "appconfigs.xml";

    private SessionFactory sessionFactory;
    private Appconfig appProps;

    private String profile;
    private String jarFolder;
    private String configsFolder;
    private String xsdFolder;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationPropertyLoader.class);
    
    //work on this mess and put these statics somewhere else since they are used application wide
    public static String UPLOADS_DIR;
    public static String RECONCILED_DIR;
    public static  String TEMP_DIR;

    private ApplicationPropertyLoader() {
        loadAllApplicationConfigs(); //called only once
    }

    private static class AppPropertyLoaderSingletonHolder {

        private static final ApplicationPropertyLoader INSTANCE = new ApplicationPropertyLoader();
    }

    public static ApplicationPropertyLoader loadInstance() {
        return AppPropertyLoaderSingletonHolder.INSTANCE;
    }

    protected Object readResolve() {
        return loadInstance();
    }

    public String getConfigsFolder() {

        return configsFolder;
    }

    public String getProfile() {
        return profile;
    }

    public String getJarFolderName() {

        return jarFolder;
    }

    public String getXsdFolderName() {

        return xsdFolder;
    }

    private Appconfig loadAppConfigProperties(String filePath, String xsdFilesFolderLocation) throws NamingException, FileNotFoundException, UnsupportedEncodingException, JAXBException, SAXException {

        //String appConfigsPropsFileLoc = getConfigFileName("appConfigsPropsFile");
        Appconfig appConfigs = (Appconfig) BindXmlAndPojo.xmlFileToObject(filePath, xsdFilesFolderLocation, Appconfig.class);

        setAppProps(appConfigs);

        return appConfigs;

    }

    private void loadLog4jProperties(String filePath) throws NamingException {

        DOMConfigurator.configure(filePath); //XML configurator
        //PropertyConfigurator.configure(log4jPropsFileLoc);//Property file configurator

        logger.debug("LOG4J properties loaded!!");
    }

    private void loadHibernateProperties(String filePath) throws NamingException, HibernateException {

        File hibernatePropsFile = new File(filePath);

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

    /**
     *
     * @param jndiStringName
     * @return jndiStringValue
     * @throws NamingException
     */
    private String getConfigFileName(String jndiStringName) {

        String jndiStringValue = null;
        try {
            System.out.println(">>>>>>  Retrieving JNDI string for: " + jndiStringName + "...");

            Context initContext = new InitialContext();
            Hashtable<?, ?> ht = initContext.getEnvironment();
            Enumeration<?> keys = ht.keys();

            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                System.out.println(key.toString() + " <<<=>>> " + ht.get(key).toString());
            }

            Context initialContext = new InitialContext();
            //Context environmentContext = (Context) initialContext.lookup("java:/comp/env");
            System.out.println(">>>>>>  Looking up env var... ");

            jndiStringValue = (String) initialContext.lookup("java:/comp/env/" + jndiStringName);

            System.out.println("Retrieved JNDI value :: Name: " + jndiStringName + " Value: " + jndiStringValue);

        } catch (NamingException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jndiStringValue;
    }

    private void loadAllApplicationConfigs() {

        boolean isTest = Boolean.FALSE;
        String[] commandLineArgs;

        try {

            if (isTest) {

                profile = "development"; // this is the first commandline argument defined in the .sh file of this daemon in this case either development, production or ....
                jarFolder = "/home/smallgod/NetBeansProjects/recontool/target/recontool-1.0.jar";
                configsFolder = "/etc/configs/ug/recontool/development/";
                xsdFolder = "/etc/xsdfiles/ug/recontool/v1_0/";

                UPLOADS_DIR = "/home/smallgod/reconfolder/";
                RECONCILED_DIR = "/home/smallgod/reconfolder/";
                TEMP_DIR = "/home/smallgod/tempFiles/";

                System.out.println("Profile: " + profile);
                System.out.println("jarFolder: " + jarFolder);
                System.out.println("ConfigsFolder: " + configsFolder);

            } else {

                commandLineArgs = AppEntry.context.getArguments();

                if (null != commandLineArgs && commandLineArgs.length > 6) {

                    profile = commandLineArgs[0]; // this is the first commandline argument defined in the .sh file of this daemon in this case either development, production or ....
                    jarFolder = commandLineArgs[1];
                    configsFolder = commandLineArgs[2];
                    xsdFolder = commandLineArgs[3];

                    UPLOADS_DIR = commandLineArgs[4];
                    RECONCILED_DIR = commandLineArgs[5];
                    TEMP_DIR = commandLineArgs[6];

                    System.out.println("Profile: " + profile);
                    System.out.println("jarFolder: " + jarFolder);
                    System.out.println("ConfigsFolder: " + configsFolder);
                    
                    System.out.println("uploadsDir: " + UPLOADS_DIR);
                    System.out.println("reconcileDir: " + RECONCILED_DIR);
                    System.out.println("tempDir: " + TEMP_DIR);
                    

                } else {
                    System.err.print("Error!! Expected atleast 6 commandline args in the startup script");
                    AppEntry.context.getController().fail(">> Expected atleast 6 commandline args in the startup script");
                    System.exit(1);
                    return;
                }
            }

            String appConfigsPath = configsFolder + APPCONFIGS_FILENAME;
            Appconfig appConfigs = loadAppConfigProperties(appConfigsPath, xsdFolder);

            String hibernatePath = configsFolder + appConfigs.getConfigfiles().getHibernateprops();
            //String xsdFilePath = configsFolder + appConfigs.getConfigfiles().getXsdfoldername();
            String log4jPath = configsFolder + appConfigs.getConfigfiles().getLog4Jprops();

            loadLog4jProperties(log4jPath);
            //XSDFilesFolderLoader.getInstance();
            loadHibernateProperties(hibernatePath);

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
            hbe.printStackTrace();
            System.err.println("Exiting. Failed to load system properties - Hibernate exception: " + hbe.getMessage());
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

                    String hibernatePath = configsFolder + getAppProps().getConfigfiles().getHibernateprops();
                    loadHibernateProperties(hibernatePath);

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
