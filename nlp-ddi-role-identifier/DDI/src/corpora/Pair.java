//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.27 at 06:34:22 PM CET 
//


package corpora;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "pair")
public class Pair {

    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "e2", required = true)
    protected String e2;
    @XmlAttribute(name = "e1", required = true)
    protected String e1;
    @XmlAttribute(name = "ddi")
    protected String ddi;
    @XmlAttribute(name = "interaction")
    protected String interaction;
    @XmlAttribute(name = "type")
    protected String type;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the e2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getE2() {
        return e2;
    }

    /**
     * Sets the value of the e2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setE2(String value) {
        this.e2 = value;
    }

    /**
     * Gets the value of the e1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getE1() {
        return e1;
    }

    /**
     * Sets the value of the e1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setE1(String value) {
        this.e1 = value;
    }

    /**
     * Gets the value of the ddi property.
     * 
     */
   public String getDdi() {
        return ddi;
    }

    /**
     * Gets the value of the ddi property.
     * 
     */
   public String getInteraction() {
        return interaction;
    }
    /**
     * Sets the value of the ddi property.
     * 
     */
    public void setDdi(String value) {
        this.ddi = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
