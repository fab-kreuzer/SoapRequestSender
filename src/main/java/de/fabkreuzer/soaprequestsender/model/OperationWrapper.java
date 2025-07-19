package de.fabkreuzer.soaprequestsender.model;

import com.eviware.soapui.model.iface.Operation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A serializable wrapper for Operation objects.
 * This class stores the essential information from an Operation object
 * that we need to display in the UI and generate sample requests.
 */
public class OperationWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private transient Operation operation;
    private List<RequestWrapper> requests = new ArrayList<>();

    public OperationWrapper() {
    }

    public OperationWrapper(Operation operation) {
        this.operation = operation;
        this.name = operation.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
        if (operation != null) {
            this.name = operation.getName();
        }
    }

    public List<RequestWrapper> getRequests() {
        return requests;
    }

    public void setRequests(List<RequestWrapper> requests) {
        this.requests = requests;
    }

    public void addRequest(RequestWrapper request) {
        this.requests.add(request);
    }

    @Override
    public String toString() {
        return name;
    }
}
