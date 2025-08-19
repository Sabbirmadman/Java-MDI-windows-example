package org.example.windowdemo.windowing;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic Window Management System
 * 
 * A reusable window management system that can be integrated into any JavaFX application.
 * Provides internal windows with drag, resize, minimize, maximize, and grid layout capabilities.
 * 
 * Usage:
 * 1. Create containers for free-form and grid layouts
 * 2. Create a taskbar container
 * 3. Initialize WindowSystem with these containers
 * 4. Create windows using WindowBuilder and add them to the system
 */
public class WindowSystem {
    private final Pane freeFormContainer;
    private final GridPane gridContainer;
    private final HBox taskbarContainer;
    private final List<InternalWindowPane> windows;
    private final Map<String, InternalWindowPane> windowsByType;
    private InternalWindowPane focusedWindow;
    private boolean gridMode = false;
    private int gridRows = 2;
    private int gridCols = 2;
    
    /**
     * Creates a new WindowSystem
     * @param freeFormContainer Pane for free-form window positioning
     * @param gridContainer GridPane for grid-based window layout
     * @param taskbarContainer HBox for window taskbar buttons
     */
    public WindowSystem(Pane freeFormContainer, GridPane gridContainer, HBox taskbarContainer) {
        this.freeFormContainer = freeFormContainer;
        this.gridContainer = gridContainer;
        this.taskbarContainer = taskbarContainer;
        this.windows = new ArrayList<>();
        this.windowsByType = new HashMap<>();
        
        setupGridContainer();
        setupContainerListeners();
    }
    
    private void setupGridContainer() {
        gridContainer.setHgap(5);
        gridContainer.setVgap(5);
        gridContainer.setVisible(false);
        updateGridConstraints();
    }
    
    private void setupContainerListeners() {
        freeFormContainer.widthProperty().addListener((obs, oldVal, newVal) -> repositionMinimizedWindows());
        freeFormContainer.heightProperty().addListener((obs, oldVal, newVal) -> repositionMinimizedWindows());
    }
    
    /**
     * Creates and adds a window to the system
     * @param builder WindowBuilder with window configuration
     * @return true if window was created, false if existing window was focused
     */
    public boolean addWindow(WindowBuilder builder) {
        if (builder.getWindowType() != null && windowsByType.containsKey(builder.getWindowType())) {
            InternalWindowPane existingWindow = windowsByType.get(builder.getWindowType());
            bringWindowToFront(existingWindow);
            if (existingWindow.isMinimized()) {
                existingWindow.toggleMinimize();
            }
            return false;
        }
        
        InternalWindowPane window = new InternalWindowPane(builder.getTitle(), this);
        
        if (builder.getIcon() != null) {
            window.setIcon(builder.getIcon());
        }
        
        if (builder.getContent() != null) {
            window.setContent(builder.getContent());
        }
        
        if (builder.getWidth() > 0 && builder.getHeight() > 0) {
            window.setPrefSize(builder.getWidth(), builder.getHeight());
        }
        
        if (builder.getX() >= 0 && builder.getY() >= 0) {
            window.setLayoutX(builder.getX());
            window.setLayoutY(builder.getY());
        }
        
        windows.add(window);
        
        if (builder.getWindowType() != null) {
            windowsByType.put(builder.getWindowType(), window);
        }
        
        if (gridMode) {
            addToGrid(window);
        } else {
            freeFormContainer.getChildren().add(window);
        }
        
        bringWindowToFront(window);
        updateTaskbar();
        
        return true;
    }
    
    public void removeWindow(InternalWindowPane window) {
        windows.remove(window);
        windowsByType.entrySet().removeIf(entry -> entry.getValue() == window);
        
        if (focusedWindow == window) {
            focusedWindow = null;
            if (!windows.isEmpty()) {
                bringWindowToFront(windows.get(windows.size() - 1));
            }
        }
        
        if (gridMode) {
            gridContainer.getChildren().remove(window);
        } else {
            freeFormContainer.getChildren().remove(window);
        }
        
        updateTaskbar();
        repositionMinimizedWindows();
    }
    
    public void bringWindowToFront(InternalWindowPane window) {
        if (focusedWindow != null) {
            focusedWindow.setFocus(false);
        }
        
        focusedWindow = window;
        window.setFocus(true);
        
        if (!gridMode) {
            Pane parent = (Pane) window.getParent();
            if (parent != null && parent.getChildren().contains(window)) {
                parent.getChildren().remove(window);
                parent.getChildren().add(window);
            }
        }
        
        updateTaskbar();
    }
    
    public void toggleGridMode() {
        gridMode = !gridMode;
        
        if (gridMode) {
            for (InternalWindowPane window : windows) {
                if (window.isMinimized()) {
                    window.toggleMinimize();
                }
            }
            switchToGridMode();
        } else {
            switchToFreeFormMode();
        }
    }
    
    public void setGridSize(int rows, int cols) {
        this.gridRows = Math.max(1, rows);
        this.gridCols = Math.max(1, cols);
        updateGridConstraints();
        
        if (gridMode) {
            switchToGridMode();
        }
    }
    
    public void arrangeWindows(WindowArrangement arrangement) {
        if (windows.isEmpty()) return;
        
        if (gridMode) {
            switchToFreeFormMode();
        }
        
        switch (arrangement) {
            case CASCADE:
                arrangeCascade();
                break;
            case TILE_HORIZONTAL:
                arrangeTileHorizontal();
                break;
            case TILE_VERTICAL:
                arrangeTileVertical();
                break;
        }
    }
    
    public void repositionMinimizedWindows() {
        if (gridMode) return;
        
        List<InternalWindowPane> minimizedWindows = new ArrayList<>();
        for (InternalWindowPane window : windows) {
            if (window.isMinimized()) {
                minimizedWindows.add(window);
            }
        }
        
        double containerWidth = freeFormContainer.getWidth();
        double containerHeight = freeFormContainer.getHeight();
        double minimizedHeight = 30;
        double windowWidth = Math.min(250, containerWidth / Math.max(1, minimizedWindows.size()));
        
        for (int i = 0; i < minimizedWindows.size(); i++) {
            InternalWindowPane window = minimizedWindows.get(i);
            window.setLayoutX(i * windowWidth);
            window.setLayoutY(containerHeight - minimizedHeight);
            window.setPrefWidth(windowWidth);
            window.toBack();
        }
    }
    
    private void switchToGridMode() {
        for (InternalWindowPane window : windows) {
            if (freeFormContainer.getChildren().contains(window)) {
                freeFormContainer.getChildren().remove(window);
            }
        }
        freeFormContainer.setVisible(false);
        
        gridContainer.getChildren().clear();
        gridContainer.setVisible(true);
        
        for (InternalWindowPane window : windows) {
            addToGrid(window);
        }
    }
    
    private void switchToFreeFormMode() {
        for (InternalWindowPane window : windows) {
            if (gridContainer.getChildren().contains(window)) {
                gridContainer.getChildren().remove(window);
            }
        }
        gridContainer.setVisible(false);
        
        freeFormContainer.setVisible(true);
        
        for (InternalWindowPane window : windows) {
            if (!freeFormContainer.getChildren().contains(window)) {
                freeFormContainer.getChildren().add(window);
            }
        }
    }
    
    private void addToGrid(InternalWindowPane window) {
        int totalCells = gridRows * gridCols;
        int windowIndex = windows.indexOf(window);
        
        if (windowIndex < totalCells) {
            int row = windowIndex / gridCols;
            int col = windowIndex % gridCols;
            
            GridPane.setRowIndex(window, row);
            GridPane.setColumnIndex(window, col);
            GridPane.setHgrow(window, javafx.scene.layout.Priority.ALWAYS);
            GridPane.setVgrow(window, javafx.scene.layout.Priority.ALWAYS);
            
            gridContainer.getChildren().add(window);
            
            window.setLayoutX(0);
            window.setLayoutY(0);
        }
    }
    
    private void updateGridConstraints() {
        gridContainer.getRowConstraints().clear();
        gridContainer.getColumnConstraints().clear();
        
        for (int i = 0; i < gridRows; i++) {
            gridContainer.getRowConstraints().add(new javafx.scene.layout.RowConstraints());
            gridContainer.getRowConstraints().get(i).setVgrow(javafx.scene.layout.Priority.ALWAYS);
        }
        
        for (int i = 0; i < gridCols; i++) {
            gridContainer.getColumnConstraints().add(new javafx.scene.layout.ColumnConstraints());
            gridContainer.getColumnConstraints().get(i).setHgrow(javafx.scene.layout.Priority.ALWAYS);
        }
    }
    
    private void updateTaskbar() {
        taskbarContainer.getChildren().clear();
        
        for (InternalWindowPane window : windows) {
            javafx.scene.control.Button taskButton = new javafx.scene.control.Button(window.getTitle());
            taskButton.setPrefWidth(120);
            taskButton.setStyle(getTaskButtonStyle(window));
            
            if (window.getIcon() != null) {
                javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView(window.getIcon());
                iconView.setFitWidth(16);
                iconView.setFitHeight(16);
                taskButton.setGraphic(iconView);
            }
            
            taskButton.setOnAction(e -> {
                if (window.isMinimized()) {
                    window.toggleMinimize();
                }
                bringWindowToFront(window);
            });
            
            taskbarContainer.getChildren().add(taskButton);
        }
    }
    
    private String getTaskButtonStyle(InternalWindowPane window) {
        if (window.hasFocus()) {
            return "-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;";
        } else if (window.isMinimized()) {
            return "-fx-background-color: #ffeb3b; -fx-text-fill: black;";
        } else if (window.isMaximized()) {
            return "-fx-background-color: #2196f3; -fx-text-fill: white;";
        } else {
            return "-fx-background-color: #4caf50; -fx-text-fill: white;";
        }
    }
    
    private void arrangeCascade() {
        double offsetX = 30;
        double offsetY = 30;
        double windowWidth = 400;
        double windowHeight = 300;
        
        for (int i = 0; i < windows.size(); i++) {
            InternalWindowPane window = windows.get(i);
            window.setLayoutX(i * offsetX);
            window.setLayoutY(i * offsetY);
            window.setPrefSize(windowWidth, windowHeight);
        }
    }
    
    private void arrangeTileHorizontal() {
        double containerWidth = freeFormContainer.getWidth();
        double windowHeight = freeFormContainer.getHeight() / windows.size();
        
        for (int i = 0; i < windows.size(); i++) {
            InternalWindowPane window = windows.get(i);
            window.setLayoutX(0);
            window.setLayoutY(i * windowHeight);
            window.setPrefSize(containerWidth, windowHeight);
        }
    }
    
    private void arrangeTileVertical() {
        double containerHeight = freeFormContainer.getHeight();
        double windowWidth = freeFormContainer.getWidth() / windows.size();
        
        for (int i = 0; i < windows.size(); i++) {
            InternalWindowPane window = windows.get(i);
            window.setLayoutX(i * windowWidth);
            window.setLayoutY(0);
            window.setPrefSize(windowWidth, containerHeight);
        }
    }
    
    // Getters
    public boolean isGridMode() { return gridMode; }
    public int getGridRows() { return gridRows; }
    public int getGridCols() { return gridCols; }
    public List<InternalWindowPane> getWindows() { return new ArrayList<>(windows); }
    
    public enum WindowArrangement {
        CASCADE, TILE_HORIZONTAL, TILE_VERTICAL
    }
}