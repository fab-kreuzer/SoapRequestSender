package de.fabkreuzer.soaprequestsender.model;

import java.io.Serializable;

/**
 * A serializable wrapper for SOAP request content.
 * This class stores the request content and a name.
 */
public class RequestWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String content;
    
    public RequestWrapper() {
    }
    
    public RequestWrapper(String name, String content) {
        this.name = name;
        this.content = content;
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
    
    @Override
    public String toString() {
        return name;
    }
}