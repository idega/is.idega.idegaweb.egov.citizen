/**
 * Birtingur.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package is.idega.idegaweb.egov.citizen.wsclient.arion;

public interface Birtingur extends javax.xml.rpc.Service {

/**
 * A WebService for customers of the Electronic Document System of
 * Kaupthing Bank
 */
    public java.lang.String getBirtingurSoapAddress();

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap getBirtingurSoap() throws javax.xml.rpc.ServiceException;

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap getBirtingurSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getBirtingurSoap12Address();

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap getBirtingurSoap12() throws javax.xml.rpc.ServiceException;

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap getBirtingurSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
