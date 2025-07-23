package de.fabkreuzer.soaprequestsender.ui.awt;

import com.eviware.soapui.model.iface.Operation;
import de.fabkreuzer.soaprequestsender.model.OperationWrapper;
import de.fabkreuzer.soaprequestsender.model.Project;
import de.fabkreuzer.soaprequestsender.model.RequestWrapper;
import de.fabkreuzer.soaprequestsender.ui.awt.component.XmlTextPane;
import de.fabkreuzer.soaprequestsender.ui.awt.constants.AwtConstants;
import de.fabkreuzer.soaprequestsender.ui.awt.controller.WsdlSenderController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.List;

import static de.fabkreuzer.soaprequestsender.ui.awt.constants.AwtConstants.ERROR;

/**
 * Main UI class for the SOAP Request Sender application.
 * This class has been refactored to use instance fields, the XmlTextPane component,
 * and the WsdlSenderController for business logic.
 */
public class WsdlSenderUI {

    private final WsdlSenderController controller;
    private JTree tree;
    private XmlTextPane requestArea;
    private JComboBox<String> endpointField;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private JFrame frame;

    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {
        try {
            // Set the Nimbus look and feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to the default look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            WsdlSenderUI ui = new WsdlSenderUI();
            ui.createAndShowGui();
        });
    }

    /**
     * Constructor to initialize the controller.
     */
    public WsdlSenderUI() {
        this.controller = new WsdlSenderController();
    }

    /**
     * Saves the current content of the request area to the selected request.
     */
    private void saveRequestContent() {
        TreePath path = tree.getSelectionPath();
        if (path == null) {
            return;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

        if (node != null && node.getUserObject() instanceof RequestWrapper request) {
            String content = requestArea.getXmlContent();

            // Save the request content using the controller
            controller.saveRequestContent(request, content);

            // Find the project node to save the project
            if (path.getPathCount() > 1) {
                // The project node should be at index 1 in the path (root is at 0)
                DefaultMutableTreeNode projectNode = (DefaultMutableTreeNode) path.getPathComponent(1);

                if (projectNode.getUserObject() instanceof Project project) {
                    try {
                        controller.saveProject(project);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame,

                            AwtConstants.ERROR_SAVING_PROJECT + ex.getMessage(),
                            ERROR,
                            JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Creates and shows the GUI.
     */
    /**
     * Updates the endpoint dropdown with the endpoints from the given request.
     * 
     * @param request The request to get endpoints from
     */
    private void updateEndpointComboBox(RequestWrapper request) {
        endpointField.removeAllItems();

        List<String> endpoints = request.getEndpoints();
        for (String endpoint : endpoints) {
            endpointField.addItem(endpoint);
        }

        String selectedEndpoint = request.getSelectedEndpoint();
        if (selectedEndpoint != null) {
            endpointField.setSelectedItem(selectedEndpoint);
        }
    }

    /**
     * Saves the project for the given node.
     * 
     * @param node The node to save the project for
     */
    private void saveProjectForNode(DefaultMutableTreeNode node) {
        // Find the project node
        DefaultMutableTreeNode projectNode = node;
        while (projectNode != null && !(projectNode.getUserObject() instanceof Project)) {
            projectNode = (DefaultMutableTreeNode) projectNode.getParent();
        }

        if (projectNode != null && projectNode.getUserObject() instanceof Project project) {
            try {
                controller.saveProject(project);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, 
                    AwtConstants.ERROR_SAVING_PROJECT + ex.getMessage(), 
                    ERROR,
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void createAndShowGui() {
        frame = new JFrame("SOAP Request Sender");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());

        // Create the top panel with project controls
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Project controls panel
        JPanel projectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newProjectButton = new JButton("New Project");
        JButton saveProjectButton = new JButton("Save Project");
        JButton deleteProjectButton = new JButton("Delete Project");
        JButton loadButton = new JButton("Load WSDL");

        projectPanel.add(newProjectButton);
        projectPanel.add(saveProjectButton);
        projectPanel.add(deleteProjectButton);
        projectPanel.add(loadButton);

        // Add panel to top panel
        topPanel.add(projectPanel, BorderLayout.CENTER);

        // Create the tree for projects, services, and operations
        rootNode = new DefaultMutableTreeNode("Projects");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);

        // Custom cell renderer to display operation names properly
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        tree.setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
            Component comp = renderer.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode node) {
                Object userObject = node.getUserObject();

                if (userObject instanceof Operation operation) {
                    renderer.setText(operation.getName());
                } else if (userObject instanceof Project project) {
                    renderer.setText(project.getName());
                } else if (userObject instanceof RequestWrapper request) {
                    renderer.setText(request.getName());
                }
            }

            return comp;
        });

        // Load existing projects
        try {
            List<Project> projects = controller.loadAllProjects();
            for (Project project : projects) {
                DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
                rootNode.add(projectNode);

                // Add service node if available
                if (project.getServiceName() != null && !project.getServiceName().isEmpty()) {
                    DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode(project.getServiceName());
                    projectNode.add(serviceNode);

                    // Add operations to the service node if available
                    if (project.getOperations() != null && !project.getOperations().isEmpty()) {
                        for (OperationWrapper operationWrapper : project.getOperations()) {
                            DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode(operationWrapper);
                            serviceNode.add(operationNode);

                            // Add request nodes if available
                            if (operationWrapper.getRequests() != null && !operationWrapper.getRequests().isEmpty()) {
                                for (RequestWrapper request : operationWrapper.getRequests()) {
                                    DefaultMutableTreeNode requestNode = new DefaultMutableTreeNode(request);
                                    operationNode.add(requestNode);
                                }
                            }
                        }
                    }
                }
            }
            treeModel.reload();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, 
                "Error loading projects: " + e.getMessage(), 
                ERROR,
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Create the endpoint field
        JPanel endpointPanel = new JPanel(new BorderLayout());
        JLabel endpointLabel = new JLabel("Endpoint: ");
        endpointField = new JComboBox<>();
        endpointField.setEditable(true);

        // Add action listener to save selected endpoint
        endpointField.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof RequestWrapper request) {
                String selectedEndpoint = endpointField.getSelectedItem() != null ?
                    endpointField.getSelectedItem().toString() : "";
                if (!selectedEndpoint.isEmpty()) {
                    request.setSelectedEndpoint(selectedEndpoint);
                    saveProjectForNode(node);
                }
            }
        });

        // Create a panel for the endpoint field and add button
        JPanel endpointFieldPanel = new JPanel(new BorderLayout());
        endpointFieldPanel.add(endpointField, BorderLayout.CENTER);

        // Add button to add a new endpoint
        JButton addEndpointButton = createAddEndpointButton();

        endpointFieldPanel.add(addEndpointButton, BorderLayout.EAST);

        endpointPanel.add(endpointLabel, BorderLayout.WEST);
        endpointPanel.add(endpointFieldPanel, BorderLayout.CENTER);

        // Create the request area with XML syntax highlighting
        requestArea = new XmlTextPane();

        // Add focus listener to save edited request content and reload it when focus is gained
        requestArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                saveRequestContent();
            }

            @Override
            public void focusGained(FocusEvent e) {
                // Use the current request content if available, otherwise reload from the selected request
                String currentContent = controller.getCurrentRequestContent();
                if (currentContent != null && !currentContent.isEmpty()) {
                    requestArea.setXmlContent(currentContent);
                } else {
                    // Fallback to loading from the selected request
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node != null && node.getUserObject() instanceof RequestWrapper request) {
                        requestArea.setXmlContent(request.getContent());
                    }
                }
            }
        });

        // Create a panel for the request area with the endpoint field at the top
        JPanel requestPanel = new JPanel(new BorderLayout());
        requestPanel.add(endpointPanel, BorderLayout.NORTH);
        requestPanel.add(new JScrollPane(requestArea), BorderLayout.CENTER);

        // Create a split pane with the tree on the left and request area on the right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree), requestPanel);
        splitPane.setDividerLocation(300);
        splitPane.setOneTouchExpandable(true);

        // Add components to the frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);

        // Add context menu to the tree
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem newRequestItem = new JMenuItem("New Request");
        popupMenu.add(newRequestItem);

        // Add action listener to the New Request menu item
        newRequestItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof OperationWrapper wrapper) {
                Operation operation = wrapper.getOperation();

                // Show dialog to enter request name
                String requestName = JOptionPane.showInputDialog(frame, "Enter request name:");
                if (requestName != null && !requestName.isBlank()) {
                    // Create a new request using the controller
                    RequestWrapper request = controller.createRequest(operation, requestName);

                    // Add the project's endpoint to the request
                    DefaultMutableTreeNode projectNode = (DefaultMutableTreeNode) node.getPath()[1];
                    if (projectNode.getUserObject() instanceof Project project) {
                        String endpoint = project.getWsdlUrl();
                        if (endpoint != null && !endpoint.isEmpty()) {
                            request.addEndpoint(endpoint);
                        }
                    }

                    wrapper.addRequest(request);

                    // Add the request to the tree
                    DefaultMutableTreeNode requestNode = new DefaultMutableTreeNode(request);
                    node.add(requestNode);

                    // Update the tree
                    treeModel.reload(node);

                    // Expand the operation node
                    tree.expandPath(new TreePath(node.getPath()));

                    // Select the new request
                    TreePath path = new TreePath(requestNode.getPath());
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);

                    // Save the project
                    if (projectNode.getUserObject() instanceof Project project) {
                        try {
                            controller.saveProject(project);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(frame, 
                                AwtConstants.ERROR_SAVING_PROJECT + ex.getMessage(), 
                                ERROR,
                                JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        tree.addTreeSelectionListener(e -> {
            if (e.getOldLeadSelectionPath() == null && e.getNewLeadSelectionPath() == null) {
                return;
            }

            // Save content from the old selection if it was a request
            if (e.getOldLeadSelectionPath() != null) {
                DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) e.getOldLeadSelectionPath().getLastPathComponent();
                if (oldNode != null && oldNode.getUserObject() instanceof RequestWrapper request) {
                    controller.saveRequestContent(request, requestArea.getXmlContent());
                }
            }

            // Load content from the new selection
            DefaultMutableTreeNode newNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (newNode == null || newNode.getUserObject() == null) {
                return;
            }

            Object userObject = newNode.getUserObject();
            if (userObject instanceof RequestWrapper request) {
                String content = request.getContent();
                controller.setCurrentRequestContent(content);
                requestArea.setXmlContent(content);

                // Update the endpoint dropdown with the endpoints from the request
                updateEndpointComboBox(request);
            } else if (userObject instanceof Project project) {
                endpointField.removeAllItems();
                endpointField.addItem(project.getWsdlUrl());
                controller.setCurrentRequestContent("");
                requestArea.setXmlContent("");
            } else {
                controller.setCurrentRequestContent("");
                requestArea.setXmlContent("");
            }
        });

        // Add mouse listener to the tree to show the popup menu
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(java.awt.event.MouseEvent e) {
                // Get the node at the mouse position
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    tree.setSelectionPath(path);
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node != null && node.getUserObject() instanceof OperationWrapper) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // Add action listener to the load button
        loadButton.addActionListener(e -> {
            // Get the selected node
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (selectedNode == null || !(selectedNode.getUserObject() instanceof Project selectedProject)) {
                JOptionPane.showMessageDialog(frame, "Please select a project to load WSDL.");
                return;
            }

            String url = selectedProject.getWsdlUrl();

            // Show dialog to enter or confirm URL
            url = JOptionPane.showInputDialog(frame, "Enter WSDL URL:", url != null ? url : "");
            if (url == null || url.isBlank()) {
                return;
            }

            try {
                // Clear existing service nodes
                selectedNode.removeAllChildren();

                // Update the project with the new WSDL data using the controller
                Project updatedProject = controller.updateProject(selectedProject, url);
                endpointField.removeAllItems();
                endpointField.addItem(url);
                controller.setCurrentRequestContent("");
                requestArea.setXmlContent("");

                // Create service node
                String serviceName = controller.getServiceName();
                DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode(serviceName);
                selectedNode.add(serviceNode);

                // Add operations from the updated project to the service node
                for (OperationWrapper wrapper : updatedProject.getOperations()) {
                    DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode(wrapper);
                    serviceNode.add(operationNode);

                    // Check if the operation has any requests
                    if (wrapper.getRequests().isEmpty()) {
                        // Create a default request for the operation
                        String sampleRequest = controller.generateSampleRequest(wrapper.getOperation());
                        RequestWrapper defaultRequest = new RequestWrapper("Default Request", sampleRequest);

                        // Add the project's endpoint to the request
                        defaultRequest.addEndpoint(url);
                        wrapper.addRequest(defaultRequest);

                        // Add the default request node
                        DefaultMutableTreeNode requestNode = new DefaultMutableTreeNode(defaultRequest);
                        operationNode.add(requestNode);
                    } else {
                        // Add existing request nodes
                        for (RequestWrapper request : wrapper.getRequests()) {
                            DefaultMutableTreeNode requestNode = new DefaultMutableTreeNode(request);
                            operationNode.add(requestNode);
                        }
                    }
                }

                // Save the updated project
                try {
                    controller.saveProject(selectedProject);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, 
                        AwtConstants.ERROR_SAVING_PROJECT + ex.getMessage(), 
                        ERROR,
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }

                // Update the tree and expand the service node
                treeModel.reload();
                tree.expandPath(new TreePath(serviceNode.getPath()));

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error loading WSDL: " + ex.getMessage(), 
                    ERROR,
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Add action listener for New Project button
        newProjectButton.addActionListener(e -> {
            // Create a custom dialog for project creation
            JDialog dialog = new JDialog(frame, "Create New Project", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(400, 150);
            dialog.setLocationRelativeTo(frame);

            // Create form panel
            JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel nameLabel = new JLabel("Project Name:");
            JTextField nameField = new JTextField();
            JLabel urlLabel = new JLabel("WSDL URL:");
            JTextField urlField = new JTextField();

            formPanel.add(nameLabel);
            formPanel.add(nameField);
            formPanel.add(urlLabel);
            formPanel.add(urlField);

            // Create button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancelButton = new JButton("Cancel");
            JButton createButton = new JButton("Create");

            buttonPanel.add(cancelButton);
            buttonPanel.add(createButton);

            // Add panels to dialog
            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Add action listeners to buttons
            cancelButton.addActionListener(event -> dialog.dispose());

            createButton.addActionListener(event -> {
                String projectName = nameField.getText();
                String url = urlField.getText();

                if (projectName == null || projectName.isBlank()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a project name.");
                    return;
                }

                if (url == null || url.isBlank()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a WSDL URL.");
                    return;
                }

                dialog.dispose();

                try {
                    // Create project with WSDL data using the controller
                    Project project = controller.createProject(projectName, url);

                    // Add project to tree
                    DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
                    rootNode.add(projectNode);

                    // Add service node
                    String serviceName = controller.getServiceName();
                    DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode(serviceName);
                    projectNode.add(serviceNode);

                    // Add operations from the project to the service node
                    for (OperationWrapper wrapper : project.getOperations()) {
                        DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode(wrapper);
                        serviceNode.add(operationNode);

                        // Create a default request for the operation if it doesn't have any
                        if (wrapper.getRequests().isEmpty()) {
                            // Create a default request for the operation
                            String sampleRequest = controller.generateSampleRequest(wrapper.getOperation());
                            RequestWrapper defaultRequest = new RequestWrapper("Default Request", sampleRequest);

                            // Add the project's endpoint to the request
                            defaultRequest.addEndpoint(url);
                            wrapper.addRequest(defaultRequest);

                            // Add the default request node
                            DefaultMutableTreeNode requestNode = new DefaultMutableTreeNode(defaultRequest);
                            operationNode.add(requestNode);
                        } else {
                            // Add existing request nodes
                            for (RequestWrapper request : wrapper.getRequests()) {
                                DefaultMutableTreeNode requestNode = new DefaultMutableTreeNode(request);
                                operationNode.add(requestNode);
                            }
                        }
                    }

                    treeModel.reload();

                    // Select the new project
                    TreePath path = new TreePath(projectNode.getPath());
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);

                    // Expand the service node
                    tree.expandPath(new TreePath(serviceNode.getPath()));

                    endpointField.removeAllItems();
                    endpointField.addItem(url);

                    // Try to save the project
                    try {
                        controller.saveProject(project);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, 
                            AwtConstants.ERROR_SAVING_PROJECT + ex.getMessage(), 
                            ERROR,
                            JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, 
                        "Error loading WSDL: " + ex.getMessage(), 
                        ERROR,
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });

            // Show the dialog
            dialog.setVisible(true);
        });

        // Add action listener for Save Project button
        saveProjectButton.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (selectedNode == null || !(selectedNode.getUserObject() instanceof Project project)) {
                JOptionPane.showMessageDialog(frame, "Please select a project to save.");
                return;
            }

            String url = endpointField.getSelectedItem() != null ? endpointField.getSelectedItem().toString() : "";

            // Show dialog to enter or confirm URL
            url = JOptionPane.showInputDialog(frame, "Enter WSDL URL:", url != null ? url : "");
            if (url == null || url.isBlank()) {
                return;
            }

            project.setWsdlUrl(url);
            endpointField.removeAllItems();
            endpointField.addItem(url);

            try {
                controller.saveProject(project);
                JOptionPane.showMessageDialog(frame, "Project saved successfully.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, 
                    AwtConstants.ERROR_SAVING_PROJECT + ex.getMessage(), 
                    ERROR,
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Add action listener for Delete Project button
        deleteProjectButton.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (selectedNode == null || !(selectedNode.getUserObject() instanceof Project project)) {
                JOptionPane.showMessageDialog(frame, "Please select a project to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete project '" + project.getName() + "'?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    controller.deleteProject(project.getName());
                    rootNode.remove(selectedNode);
                    treeModel.reload();
                    endpointField.removeAllItems();
                    controller.setCurrentRequestContent("");
                    requestArea.setXmlContent("");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, 
                        "Error deleting project: " + ex.getMessage(),
                        ERROR,
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        frame.setVisible(true);
    }

    private JButton createAddEndpointButton() {
        JButton addEndpointButton = new JButton("+");
        addEndpointButton.setToolTipText("Add a new endpoint");
        addEndpointButton.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof RequestWrapper request) {
                String newEndpoint = JOptionPane.showInputDialog(frame, "Enter new endpoint URL:");
                if (newEndpoint != null && !newEndpoint.isEmpty()) {
                    request.addEndpoint(newEndpoint);
                    updateEndpointComboBox(request);
                    saveProjectForNode(node);
                }
            }
        });
        return addEndpointButton;
    }
}
