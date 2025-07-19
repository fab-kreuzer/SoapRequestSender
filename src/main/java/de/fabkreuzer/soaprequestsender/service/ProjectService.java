package de.fabkreuzer.soaprequestsender.service;

import de.fabkreuzer.soaprequestsender.model.Project;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing SOAP projects on disk.
 */
public class ProjectService {
    
    private static final String PROJECTS_DIR = "projects";
    private static final String FILE_EXTENSION = ".proj";
    
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
        Path projectFile = getProjectFilePath(project.getName());
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(projectFile.toFile()))) {
            oos.writeObject(project);
        }
    }
    
    /**
     * Load a project from disk.
     * 
     * @param projectName The name of the project to load
     * @return The loaded project
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If the class of the serialized object cannot be found
     */
    public Project loadProject(String projectName) throws IOException, ClassNotFoundException {
        Path projectFile = getProjectFilePath(projectName);
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(projectFile.toFile()))) {
            return (Project) ois.readObject();
        }
    }
    
    /**
     * Delete a project from disk.
     * 
     * @param projectName The name of the project to delete
     * @throws IOException If an I/O error occurs
     */
    public void deleteProject(String projectName) throws IOException {
        Path projectFile = getProjectFilePath(projectName);
        Files.deleteIfExists(projectFile);
    }
    
    /**
     * Get a list of all saved projects.
     * 
     * @return A list of project names
     * @throws IOException If an I/O error occurs
     */
    public List<String> getProjectNames() throws IOException {
        List<String> projectNames = new ArrayList<>();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(PROJECTS_DIR))) {
            for (Path path : stream) {
                if (path.toString().endsWith(FILE_EXTENSION)) {
                    String fileName = path.getFileName().toString();
                    // Remove the extension
                    projectNames.add(fileName.substring(0, fileName.length() - FILE_EXTENSION.length()));
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
            } catch (ClassNotFoundException e) {
                // Skip projects that can't be loaded
                System.err.println("Failed to load project: " + name);
            }
        }
        
        return projects;
    }
    
    /**
     * Get the file path for a project.
     * 
     * @param projectName The name of the project
     * @return The path to the project file
     */
    private Path getProjectFilePath(String projectName) {
        return Paths.get(PROJECTS_DIR, projectName + FILE_EXTENSION);
    }
}