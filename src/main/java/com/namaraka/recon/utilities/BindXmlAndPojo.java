package com.namaraka.recon.utilities;

import com.namaraka.recon.config.v1_0.Appconfig;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import com.namaraka.recon.exceptiontype.MyCustomException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BindXmlAndPojo {

    /**
     * Marshall information into xml.
     *
     * @param xmlObject
     * @param classToBind
     * @return
     */
    public static String objectToXML(Object xmlObject, Class... classToBind) throws MyCustomException {

        String xmlOutput = null;
        StringWriter sw = new StringWriter();

        JAXBContext jc;

        try {
            jc = JAXBContext.newInstance(classToBind);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            //m.marshal( xmlObject, System.out );
            marshaller.marshal(xmlObject, sw);
        } catch (JAXBException e) {
             throw new MyCustomException("Error marshalling", ErrorCode.INTERNAL_ERR, "Error creating response: " + e.getMessage(), ErrorCategory.SERVER_ERR_TYPE);
        }

        xmlOutput = sw.toString();

        return xmlOutput;
    }

    /**
     * Unmarshall information into a java object
     *
     * @param xmlString
     * @param xsdFilesFolderLocation
     * @param classToBind
     * @return
     * @throws javax.xml.bind.JAXBException
     * @throws org.xml.sax.SAXException
     */
    public static Object xmlToObject(String xmlString, String xsdFilesFolderLocation, Class... classToBind) throws SAXException, JAXBException, NullPointerException, RuntimeException {

        InputSource is = new InputSource(new StringReader(xmlString));

        //retrieve xsd file
        String classXSDfile = getXSDfile(xsdFilesFolderLocation, classToBind[0]);

        //validate the XML
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File(classXSDfile));

        JAXBContext jc = JAXBContext.newInstance(classToBind);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        //uncomment to validate requests against XSD
        //unmarshaller.setSchema(schema);
        //unmarshaller.setEventHandler(new XMLValidationEventHandler());
        Object xmlObject = unmarshaller.unmarshal(is);

        return xmlObject;
    }

    /**
     * *
     *
     * @param xsdFilesFolderLocation
     * @param classToBind
     * @return xsd file path
     */
    public static String getXSDfile(String xsdFilesFolderLocation, Class classToBind) throws NullPointerException {

        String xsdFilePath = null;

//        if (classToBind  == Servicerequest.class) {
//            xsdFilePath = xsdFilesFolderLocation + "ServiceBinding/Service.xsd";
//        } else 
        if (classToBind == Appconfig.class) {
            xsdFilePath = xsdFilesFolderLocation + "appconfigs/appconfigs.xsd";
        } else {
            throw new NullPointerException("Could not find XSD file for JAXB class: " + classToBind.toString());
        }

        return xsdFilePath;
    }

    /**
     *
     * @param xmlFilePath
     * @param xsdFilesFolderLocation
     * @param classToBind
     * @return JAXB class
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws JAXBException
     * @throws javax.xml.bind.ValidationException
     * @throws org.xml.sax.SAXException
     */
    public static Object xmlFileToObject(String xmlFilePath, String xsdFilesFolderLocation, Class... classToBind) throws FileNotFoundException, UnsupportedEncodingException, SAXException, ValidationException, JAXBException, NullPointerException {

        //if schema file loc is not known - http://docs.oracle.com/javase/6/docs/api/javax/xml/validation/SchemaFactory.html#newSchema%28%29
        //SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        //Schema schema = factory.newSchema();
        //Unmarshaller unmarshaller = jc.createUnmarshaller();
        //unmarshaller.setSchema(schema);
        File file = new File(xmlFilePath);

        InputStream inputStream = new FileInputStream(file);
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");

        //retrieve xsd file
        String classXSDfile = getXSDfile(xsdFilesFolderLocation, classToBind[0]);
        
        System.out.println("XSDFile got: " + classXSDfile);
        System.out.println("FilePath   : " + file.getAbsolutePath());

        //validate the XML
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File(classXSDfile));
        
        JAXBContext jc = JAXBContext.newInstance(classToBind);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        
        unmarshaller.setSchema(schema);

        unmarshaller.setEventHandler(new XMLValidationEventHandler());
        //XMLObject xmlObject = (DBMSXMLObject) unmarshaller.unmarshal(new File(xmlFilePath)); 

        Object xmlObject = null;
        try{
            xmlObject = unmarshaller.unmarshal(is);
        }catch(JAXBException e){
            e.printStackTrace();
            System.out.println("error: " + e.getMessage());
        }

        System.out.println("done unmarshalling!!!!!!!!");

        return xmlObject;
    }

    public static <T> T xmlFileToObject1(String xmlFilePath, Class<T> classToBind) throws FileNotFoundException, UnsupportedEncodingException, JAXBException {

        File file = new File(xmlFilePath);

        InputStream inputStream = new FileInputStream(file);
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");

        JAXBContext jc = JAXBContext.newInstance(classToBind);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        T xmlObject = classToBind.cast(unmarshaller.unmarshal(is));
        //XMLObject xmlObject = (DBMSXMLObject)unmarshaller.unmarshal(file); //NOI18N

        return xmlObject;
    }
}
