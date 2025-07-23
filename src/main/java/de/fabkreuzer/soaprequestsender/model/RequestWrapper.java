package de.fabkreuzer.soaprequestsender.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for SOAP request content.
 * This class stores the request content, a name, and multiple endpoints.
 */
public class RequestWrapper {

    private String name;
    private String content;
    private List<String> endpoints = new ArrayList<>();
    private String selectedEndpoint;

    public RequestWrapper() {
    }

    public RequestWrapper(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public RequestWrapper(String name, String content, String endpoint) {
        this.name = name;
        this.content = content;
        if (endpoint != null && !endpoint.isEmpty()) {
            this.endpoints.add(endpoint);
            this.selectedEndpoint = endpoint;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    public void addEndpoint(String endpoint) {
        if (endpoint != null && !endpoint.isEmpty() && !endpoints.contains(endpoint)) {
            endpoints.add(endpoint);
            if (selectedEndpoint == null) {
                selectedEndpoint = endpoint;
            }
        }
    }

    public void removeEndpoint(String endpoint) {
        endpoints.remove(endpoint);
        if (selectedEndpoint != null && selectedEndpoint.equals(endpoint)) {
            selectedEndpoint = endpoints.isEmpty() ? null : endpoints.get(0);
        }
    }

    public String getSelectedEndpoint() {
        return selectedEndpoint;
    }

    public void setSelectedEndpoint(String selectedEndpoint) {
        if (endpoints.contains(selectedEndpoint)) {
            this.selectedEndpoint = selectedEndpoint;
        } else if (!endpoints.isEmpty()) {
            this.selectedEndpoint = endpoints.get(0);
        } else {
            this.selectedEndpoint = null;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
