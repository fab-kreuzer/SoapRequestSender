package de.fabkreuzer.soaprequestsender.service;

import de.fabkreuzer.soaprequestsender.model.OperationWrapper;
import de.fabkreuzer.soaprequestsender.model.Project;
import de.fabkreuzer.soaprequestsender.model.RequestWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Service for managing SOAP projects on disk.
 */
public class ProjectService {

    private static final Logger logger = LogManager.getLogger(ProjectService.class);
    
    private static final String PROJECTS_DIR = "projects";
    private static final String PROJECT_PROPERTIES = "project.properties";
    private static final String ENDPOINTS_FILE = "endpoints.properties";
    private static final String REQUEST_FILE = "request.xml";

    /**
     * Initialize the projects directory if it doesn't exist.
     */
    public ProjectService() {
        try {
            Path projectsPath = Paths.get(PROJECTS_DIR);
            if (!Files.exists(projectsPath)) {
                Files.createDirectory(projectsPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize projects directory", e);
        }
    }

    /**
     * Save a project to disk.
     * 
     * @param project The project to save
     * @throws IOException If an I/O error occurs
     */
    public void saveProject(Project project) throws IOException {
        // Create project directory if it doesn't exist
        Path projectDir = getProjectDirPath(project.getName());
        if (!Files.exists(projectDir)) {
            Files.createDirectories(projectDir);
        }

        // Debug logging
        logger.debug("Saving project: {}", project.getName());
        logger.debug("Operations count: {}", project.getOperations().size());
        for (OperationWrapper operation : project.getOperations()) {
            logger.debug("Operation: {}, Requests count: {}", operation.getName(), operation.getRequests().size());
            for (RequestWrapper request : operation.getRequests()) {
                logger.debug("  Request: {}, Endpoints: {}", request.getName(), request.getEndpoints().size());
            }
        }

        // Save project properties
        Properties projectProps = new Properties();
        projectProps.setProperty("name", project.getName());
        projectProps.setProperty("wsdlUrl", project.getWsdlUrl());
        projectProps.setProperty("serviceName", project.getServiceName());

        Path projectPropsPath = projectDir.resolve(PROJECT_PROPERTIES);
        try (OutputStream out = Files.newOutputStream(projectPropsPath)) {
            projectProps.store(out, "Project properties");
        }

        // Save operations and requests
        for (OperationWrapper operation : project.getOperations()) {
            saveOperation(project.getName(), operation);
        }
    }

    /**
     * Save an operation to disk.
     * 
     * @param projectName The name of the project
     * @param operation The operation to save
     * @throws IOException If an I/O error occurs
     */
    private void saveOperation(String projectName, OperationWrapper operation) throws IOException {
        // Create operation directory if it doesn't exist
        Path operationDir = getOperationDirPath(projectName, operation.getName());
        if (!Files.exists(operationDir)) {
            Files.createDirectories(operationDir);
        }

        // Save requests
        for (RequestWrapper request : operation.getRequests()) {
            saveRequest(projectName, operation.getName(), request);
        }
    }

    /**
     * Save a request to disk.
     * 
     * @param projectName The name of the project
     * @param operationName The name of the operation
     * @param request The request to save
     * @throws IOException If an I/O error occurs
     */
    private void saveRequest(String projectName, String operationName, RequestWrapper request) throws IOException {
        // Create request directory if it doesn't exist
        Path requestDir = getRequestDirPath(projectName, operationName, request.getName());
        if (!Files.exists(requestDir)) {
            Files.createDirectories(requestDir);
        }

        // Save request content
        Path requestPath = requestDir.resolve(REQUEST_FILE);
        Files.write(requestPath, request.getContent().getBytes(StandardCharsets.UTF_8));

        // Save endpoints
        Properties endpointsProps = new Properties();
        for (int i = 0; i < request.getEndpoints().size(); i++) {
            endpointsProps.setProperty("endpoint." + i, request.getEndpoints().get(i));
        }
        if (request.getSelectedEndpoint() != null) {
            endpointsProps.setProperty("selectedEndpoint", request.getSelectedEndpoint());
        }

        Path endpointsPath = requestDir.resolve(ENDPOINTS_FILE);
        try (OutputStream out = Files.newOutputStream(endpointsPath)) {
            endpointsProps.store(out, "Request endpoints");
        }
    }

    /**
     * Load a project from disk.
     * 
     * @param projectName The name of the project to load
     * @return The loaded project
     * @throws IOException If an I/O error occurs
     */
    public Project loadProject(String projectName) throws IOException {
        Path projectDir = getProjectDirPath(projectName);
        if (!Files.exists(projectDir)) {
            throw new IOException("Project directory does not exist: " + projectDir);
        }

        // Load project properties
        Properties projectProps = new Properties();
        Path projectPropsPath = projectDir.resolve(PROJECT_PROPERTIES);
        try (InputStream in = Files.newInputStream(projectPropsPath)) {
            projectProps.load(in);
        }

        String name = projectProps.getProperty("name");
        String wsdlUrl = projectProps.getProperty("wsdlUrl");
        String serviceName = projectProps.getProperty("serviceName");

        Project project = new Project(name, wsdlUrl);
        project.setServiceName(serviceName);

        // Load operations
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(projectDir, Files::isDirectory)) {
            for (Path operationDir : stream) {
                OperationWrapper operation = loadOperation(operationDir);
                if (operation != null) {
                    project.getOperations().add(operation);
                }
            }
        }

        return project;
    }

    /**
     * Load an operation from disk.
     * 
     * @param operationDir The directory containing the operation
     * @return The loaded operation
     * @throws IOException If an I/O error occurs
     */
    private OperationWrapper loadOperation(Path operationDir) throws IOException {
        String operationName = operationDir.getFileName().toString();
        OperationWrapper operation = new OperationWrapper();
        operation.setName(operationName);

        // Load requests
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(operationDir, Files::isDirectory)) {
            for (Path requestDir : stream) {
                RequestWrapper request = loadRequest(requestDir);
                if (request != null) {
                    operation.addRequest(request);
                }
            }
        }

        return operation;
    }

    /**
     * Load a request from disk.
     * 
     * @param requestDir The directory containing the request
     * @return The loaded request
     * @throws IOException If an I/O error occurs
     */
    private RequestWrapper loadRequest(Path requestDir) throws IOException {
        String requestName = requestDir.getFileName().toString();

        // Load request content
        Path requestPath = requestDir.resolve(REQUEST_FILE);
        String content = "";
        if (Files.exists(requestPath)) {
            content = new String(Files.readAllBytes(requestPath), StandardCharsets.UTF_8);
        }

        RequestWrapper request = new RequestWrapper(requestName, content);

        // Load endpoints
        Properties endpointsProps = new Properties();
        Path endpointsPath = requestDir.resolve(ENDPOINTS_FILE);
        if (Files.exists(endpointsPath)) {
            try (InputStream in = Files.newInputStream(endpointsPath)) {
                endpointsProps.load(in);
            }

            // Add endpoints
            List<String> endpoints = new ArrayList<>();
            for (int i = 0; ; i++) {
                String endpoint = endpointsProps.getProperty("endpoint." + i);
                if (endpoint == null) {
                    break;
                }
                endpoints.add(endpoint);
            }

            for (String endpoint : endpoints) {
                request.addEndpoint(endpoint);
            }

            // Set selected endpoint
            String selectedEndpoint = endpointsProps.getProperty("selectedEndpoint");
            if (selectedEndpoint != null) {
                request.setSelectedEndpoint(selectedEndpoint);
            }
        }

        return request;
    }

    /**
     * Delete a project from disk.
     * 
     * @param projectName The name of the project to delete
     * @throws IOException If an I/O error occurs
     */
    public void deleteProject(String projectName) throws IOException {
        Path projectDir = getProjectDirPath(projectName);
        if (Files.exists(projectDir)) {
            deleteDirectory(projectDir);
        }
    }

    /**
     * Recursively delete a directory.
     * 
     * @param directory The directory to delete
     * @throws IOException If an I/O error occurs
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.isDirectory(directory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (Path path : stream) {
                    deleteDirectory(path);
                }
            }
        }
        Files.delete(directory);
    }

    /**
     * Get a list of all saved projects.
     * 
     * @return A list of project names
     * @throws IOException If an I/O error occurs
     */
    public List<String> getProjectNames() throws IOException {
        List<String> projectNames = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(PROJECTS_DIR), Files::isDirectory)) {
            for (Path path : stream) {
                Path projectPropsPath = path.resolve(PROJECT_PROPERTIES);
                if (Files.exists(projectPropsPath)) {
                    projectNames.add(path.getFileName().toString());
                }
            }
        }

        return projectNames;
    }

    /**
     * Load all saved projects.
     * 
     * @return A list of all projects
     * @throws IOException If an I/O error occurs
     */
    public List<Project> loadAllProjects() throws IOException {
        List<Project> projects = new ArrayList<>();
        List<String> projectNames = getProjectNames();

        for (String name : projectNames) {
            try {
                projects.add(loadProject(name));
            } catch (IOException e) {
                // Skip projects that can't be loaded
                logger.error("Failed to load project: {}", name, e);
            }
        }

        return projects;
    }

    /**
     * Get the directory path for a project.
     * 
     * @param projectName The name of the project
     * @return The path to the project directory
     */
    private Path getProjectDirPath(String projectName) {
        return Paths.get(PROJECTS_DIR, projectName);
    }

    /**
     * Get the directory path for an operation.
     * 
     * @param projectName The name of the project
     * @param operationName The name of the operation
     * @return The path to the operation directory
     */
    private Path getOperationDirPath(String projectName, String operationName) {
        return getProjectDirPath(projectName).resolve(operationName);
    }

    /**
     * Get the directory path for a request.
     * 
     * @param projectName The name of the project
     * @param operationName The name of the operation
     * @param requestName The name of the request
     * @return The path to the request directory
     */
    private Path getRequestDirPath(String projectName, String operationName, String requestName) {
        return getOperationDirPath(projectName, operationName).resolve(requestName);
    }
}
