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
    private java.lang.String BirtingurSoap_address = "https://www.arionbanki.is/Netbanki4/StandardServices/Birtingur.asmx";

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

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap getBirtingurSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BirtingurSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBirtingurSoap(endpoint);
    }

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap getBirtingurSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
        	is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoapStub _stub = new is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoapStub(portAddress, this);
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


    // Use to get a proxy class for BirtingurSoap12
    private java.lang.String BirtingurSoap12_address = "https://www.arionbanki.is/Netbanki4/StandardServices/Birtingur.asmx";

    public java.lang.String getBirtingurSoap12Address() {
        return BirtingurSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BirtingurSoap12WSDDServiceName = "BirtingurSoap12";

    public java.lang.String getBirtingurSoap12WSDDServiceName() {
        return BirtingurSoap12WSDDServiceName;
    }

    public void setBirtingurSoap12WSDDServiceName(java.lang.String name) {
        BirtingurSoap12WSDDServiceName = name;
    }

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap getBirtingurSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BirtingurSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBirtingurSoap12(endpoint);
    }

    public is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap getBirtingurSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
        	is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap12Stub _stub = new is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap12Stub(portAddress, this);
            _stub.setPortName(getBirtingurSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBirtingurSoap12EndpointAddress(java.lang.String address) {
        BirtingurSoap12_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     * This service has multiple ports for a given interface;
     * the proxy implementation returned may be indeterminate.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap.class.isAssignableFrom(serviceEndpointInterface)) {
            	is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoapStub _stub = new is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoapStub(new java.net.URL(BirtingurSoap_address), this);
                _stub.setPortName(getBirtingurSoapWSDDServiceName());
                return _stub;
            }
            if (is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap.class.isAssignableFrom(serviceEndpointInterface)) {
            	is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap12Stub _stub = new is.idega.idegaweb.egov.citizen.wsclient.arion.BirtingurSoap12Stub(new java.net.URL(BirtingurSoap12_address), this);
                _stub.setPortName(getBirtingurSoap12WSDDServiceName());
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
        else if ("BirtingurSoap12".equals(inputPortName)) {
            return getBirtingurSoap12();
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
            ports.add(new javax.xml.namespace.QName("http://www.kbbanki.is/Netbanki/Services/Birtingur", "BirtingurSoap12"));
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
if ("BirtingurSoap12".equals(portName)) {
            setBirtingurSoap12EndpointAddress(address);
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
