//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.27 at 04:00:05 PM EAT 
//


package com.namaraka.recon.config.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for schedulertype complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="schedulertype">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="triggername" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="jobname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="interval" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "schedulertype", propOrder = {

})
public class Schedulertype {

    @XmlElement(required = true)
    protected String triggername;
    @XmlElement(required = true)
    protected String jobname;
    protected long interval;

    /**
     * Gets the value of the triggername property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTriggername() {
        return triggername;
    }

    /**
     * Sets the value of the triggername property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTriggername(String value) {
        this.triggername = value;
    }

    /**
     * Gets the value of the jobname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobname() {
        return jobname;
    }

    /**
     * Sets the value of the jobname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobname(String value) {
        this.jobname = value;
    }

    /**
     * Gets the value of the interval property.
     * 
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Sets the value of the interval property.
     * 
     */
    public void setInterval(long value) {
        this.interval = value;
    }

}