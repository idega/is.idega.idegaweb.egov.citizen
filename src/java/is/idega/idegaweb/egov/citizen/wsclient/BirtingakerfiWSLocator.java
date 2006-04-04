/**
 * BirtingakerfiWSLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package is.idega.idegaweb.egov.citizen.wsclient;

public class BirtingakerfiWSLocator extends org.apache.axis.client.Service implements is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWS {

    public BirtingakerfiWSLocator() {
    }


    public BirtingakerfiWSLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public BirtingakerfiWSLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BirtingakerfiWSSoap
    private java.lang.String BirtingakerfiWSSoap_address = "https://ws.isb.is/adgerdirv1/birtingakerfi.asmx";

    public java.lang.String getBirtingakerfiWSSoapAddress() {
        return BirtingakerfiWSSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BirtingakerfiWSSoapWSDDServiceName = "BirtingakerfiWSSoap";

    public java.lang.String getBirtingakerfiWSSoapWSDDServiceName() {
        return BirtingakerfiWSSoapWSDDServiceName;
    }

    public void setBirtingakerfiWSSoapWSDDServiceName(java.lang.String name) {
        BirtingakerfiWSSoapWSDDServiceName = name;
    }

    public is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWSSoap_PortType getBirtingakerfiWSSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BirtingakerfiWSSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBirtingakerfiWSSoap(endpoint);
    }

    public is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWSSoap_PortType getBirtingakerfiWSSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWSSoap_BindingStub _stub = new is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWSSoap_BindingStub(portAddress, this);
            _stub.setPortName(getBirtingakerfiWSSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBirtingakerfiWSSoapEndpointAddress(java.lang.String address) {
        BirtingakerfiWSSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWSSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWSSoap_BindingStub _stub = new is.idega.idegaweb.egov.citizen.wsclient.BirtingakerfiWSSoap_BindingStub(new java.net.URL(BirtingakerfiWSSoap_address), this);
                _stub.setPortName(getBirtingakerfiWSSoapWSDDServiceName());
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
        if ("BirtingakerfiWSSoap".equals(inputPortName)) {
            return getBirtingakerfiWSSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://ws.isb.is", "BirtingakerfiWS");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://ws.isb.is", "BirtingakerfiWSSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("BirtingakerfiWSSoap".equals(portName)) {
            setBirtingakerfiWSSoapEndpointAddress(address);
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
