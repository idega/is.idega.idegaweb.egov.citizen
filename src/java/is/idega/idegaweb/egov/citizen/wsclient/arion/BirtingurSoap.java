/**
 * BirtingurSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package is.idega.idegaweb.egov.citizen.wsclient.arion;

public interface BirtingurSoap extends java.rmi.Remote {

    /**
     * Sends a byte array representing a file to the Electronic Document
     * System of Kaupthing Bank
     */
    public is.idega.idegaweb.egov.citizen.wsclient.arion.Birtingur_main sendDocument(byte[] byteArray, java.lang.String nameOfFile) throws java.rmi.RemoteException;
}
