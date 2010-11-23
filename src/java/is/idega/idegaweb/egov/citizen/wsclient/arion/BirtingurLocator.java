/**
 * BirtingurLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package is.idega.idegaweb.egov.citizen.wsclient.arion;

public class BirtingurLocator extends org.apache.axis.client.Service implements is.idega.idegaweb.egov.citizen.wsclient.arion.Birtingur {

/**
 * A WebService for customers of the Electronic Document System of
 * Kaupthing Bank
 */

    public BirtingurLocator() {
    }


    public BirtingurLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public BirtingurLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BirtingurSoap
    private java.lang.String BirtingurSoap_address = "https://www.kbbanki.is/Netbanki/StandardServices/Birtingur.asmx";

    public java.lang.String getBirtingurSoapAddress() {
        return BirtingurSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BirtingurSoapWSDDServiceName = "BirtingurSoap";

    public java.lang.String getBirtingurSoapWSDDServiceName() {
        return BirtingurSoapWSDDServiceName;
    }

    public void setBirtingurSoapWSDDServiceName(java.lang.String name) {
        BirtingurSoapWSDDServiceName = name;
    }

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap_PortType getBirtingurSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BirtingurSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBirtingurSoap(endpoint);
    }

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap_PortType getBirtingurSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap_BindingStub _stub = new is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap_BindingStub(portAddress, this);
            _stub.setPortName(getBirtingurSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBirtingurSoapEndpointAddress(java.lang.String address) {
        BirtingurSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap_BindingStub _stub = new is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap_BindingStub(new java.net.URL(BirtingurSoap_address), this);
                _stub.setPortName(getBirtingurSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("BirtingurSoap".equals(inputPortName)) {
            return getBirtingurSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.kbbanki.is/Netbanki/Services/Birtingur", "Birtingur");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.kbbanki.is/Netbanki/Services/Birtingur", "BirtingurSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("BirtingurSoap".equals(portName)) {
            setBirtingurSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
