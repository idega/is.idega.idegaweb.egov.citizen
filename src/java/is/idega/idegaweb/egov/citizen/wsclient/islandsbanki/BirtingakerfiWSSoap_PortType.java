/**
 * BirtingakerfiWSSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package is.idega.idegaweb.egov.citizen.wsclient.islandsbanki;

public interface BirtingakerfiWSSoap_PortType extends java.rmi.Remote {

    /**
     * Sendir Base64 enkóðaða skrá inn í birtingakerfi
     */
    public void sendaSkra(java.lang.String skraarnafn, java.lang.String innihald, java.lang.String kennitala) throws java.rmi.RemoteException;
}
