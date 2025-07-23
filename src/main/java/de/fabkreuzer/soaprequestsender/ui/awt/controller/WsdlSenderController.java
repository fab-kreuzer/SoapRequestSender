package de.fabkreuzer.soaprequestsender.ui.awt.controller;

import com.eviware.soapui.model.iface.Operation;
import de.fabkreuzer.soaprequestsender.model.Project;
import de.fabkreuzer.soaprequestsender.model.RequestWrapper;
import de.fabkreuzer.soaprequestsender.service.ProjectService;
import de.fabkreuzer.soaprequestsender.service.WsdlService;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

/**
 * Controller class for the WSDL Sender UI.
 * This class handles the business logic for the UI, separating it from the view.
 */
public class WsdlSenderController {
    
    private final ProjectService projectService;
    private WsdlService wsdlService;

    @Getter
    @Setter
    private String currentRequestContent;
    
    /**
     * Creates a new WsdlSenderController with the specified project service.
     */
    public WsdlSenderController() {
        this.projectService = new ProjectService();
    }
    
    /**
     * Loads all projects from disk.
     * 
     * @return A list of all projects
     * @throws IOException If an I/O error occurs
     */
    public List<Project> loadAllProjects() throws IOException {
        return projectService.loadAllProjects();
    }
    
    /**
     * Saves a project to disk.
     * 
     * @param project The project to save
     * @throws IOException If an I/O error occurs
     */
    public void saveProject(Project project) throws IOException {
        projectService.saveProject(project);
    }
    
    /**
     * Deletes a project from disk.
     * 
     * @param projectName The name of the project to delete
     * @throws IOException If an I/O error occurs
     */
    public void deleteProject(String projectName) throws IOException {
        projectService.deleteProject(projectName);
    }
    
    /**
     * Loads WSDL operations from a URL.
     * 
     * @param url The URL of the WSDL
     * @return A list of operations
     */
    public List<Operation> loadWsdl(String url) {
        wsdlService = new WsdlService();
        return wsdlService.getOperations(url);
    }
    
    /**
     * Gets the service name from the loaded WSDL.
     * 
     * @return The service name
     */
    public String getServiceName() {
        return wsdlService != null ? wsdlService.getServiceName() : "";
    }
    
    /**
     * Generates a sample request for an operation.
     * 
     * @param operation The operation
     * @return A sample request
     */
    public String generateSampleRequest(Operation operation) {
        return wsdlService != null ? wsdlService.generateSampleRequest(operation) : "";
    }

    /**
     * Creates a new project with the specified name and WSDL URL.
     * 
     * @param name The project name
     * @param url The WSDL URL
     * @return The new project
     */
    public Project createProject(String name, String url) {
        List<Operation> operations = loadWsdl(url);
        String serviceName = getServiceName();
        return new Project(name, url, serviceName, operations);
    }
    
    /**
     * Updates a project with new WSDL data.
     * 
     * @param project The project to update
     * @param url The new WSDL URL
     * @return The updated project
     */
    public Project updateProject(Project project, String url) {
        List<Operation> operations = loadWsdl(url);
        String serviceName = getServiceName();
        project.setWsdlUrl(url);
        project.setServiceName(serviceName);
        project.setOperations(operations);
        return project;
    }
    
    /**
     * Creates a new request for an operation.
     * 
     * @param operation The operation
     * @param requestName The request name
     * @return The new request
     */
    public RequestWrapper createRequest(Operation operation, String requestName) {
        String sampleRequest = generateSampleRequest(operation);
        return new RequestWrapper(requestName, sampleRequest);
    }
    
    /**
     * Saves the current request content to a request.
     * 
     * @param request The request to save to
     * @param content The content to save
     */
    public void saveRequestContent(RequestWrapper request, String content) {
        request.setContent(content);
        setCurrentRequestContent(content);
    }
}