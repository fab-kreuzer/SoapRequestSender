package de.fabkreuzer.soaprequestsender.model;

import com.eviware.soapui.model.iface.Operation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a SOAP project with a name, WSDL URL, and WSDL service information.
 * This class is used for storing project information on disk.
 */
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String wsdlUrl;
    private String serviceName;
    private List<OperationWrapper> operations = new ArrayList<>();

    public Project() {
    }

    public Project(String name, String wsdlUrl) {
        this.name = name;
        this.wsdlUrl = wsdlUrl;
    }

    public Project(String name, String wsdlUrl, String serviceName, List<Operation> operations) {
        this.name = name;
        this.wsdlUrl = wsdlUrl;
        this.serviceName = serviceName;

        // Convert Operation objects to OperationWrapper objects
        this.operations = operations.stream()
            .map(OperationWrapper::new)
            .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<OperationWrapper> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        // Convert Operation objects to OperationWrapper objects
        this.operations = operations.stream()
            .map(OperationWrapper::new)
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return name;
    }
}
