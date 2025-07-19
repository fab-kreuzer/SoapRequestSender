package de.fabkreuzer.soaprequestsender.service;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.model.iface.Operation;

import java.util.List;

public class WsdlService {

    private WsdlInterface currentWsdl;

    public List<Operation> getOperations(String url)  {
        try {
            WsdlProject project = new WsdlProject();
            WsdlInterface[] wsdls = WsdlImporter.importWsdl(project, url);
            currentWsdl = wsdls[0];

            return currentWsdl.getOperationList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getServiceName() {
        return currentWsdl != null ? currentWsdl.getName() : "";
    }

    public String generateSampleRequest(Operation operation) {
        try {
            if (operation instanceof WsdlOperation) {
                WsdlOperation wsdlOperation = (WsdlOperation) operation;
                WsdlRequest request = wsdlOperation.addNewRequest("Sample Request");
                // Generate a default request based on the operation's schema
                String requestContent = wsdlOperation.createRequest(true);
                request.setRequestContent(requestContent);
                return requestContent;
            }
            return "Unable to generate sample request for this operation type.";
        } catch (Exception e) {
            return "Error generating sample request: " + e.getMessage();
        }
    }
}
