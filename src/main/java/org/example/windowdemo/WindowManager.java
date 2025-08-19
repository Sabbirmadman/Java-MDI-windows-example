package org.example.windowdemo;

import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowManager {
    private Pane freeFormPane;
    private GridPane gridPane;
    private HBox taskbar;
    private List<InternalWindow> windows;
    private Map<String, InternalWindow> windowsByType;
    private InternalWindow focusedWindow;
    private boolean gridMode = false;
    private int gridRows = 2;
    private int gridCols = 2;
    
    public WindowManager(Pane freeFormPane, GridPane gridPane, HBox taskbar) {
        this.freeFormPane = freeFormPane;
        this.gridPane = gridPane;
        this.taskbar = taskbar;
        this.windows = new ArrayList<>();
        this.windowsByType = new HashMap<>();
        setupGridPane();
        setupContainerListeners();
    }
    
    private void setupGridPane() {
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(5));
        gridPane.setVisible(false);
        updateGridConstraints();
    }
    
    private void setupContainerListeners() {
        freeFormPane.widthProperty().addListener((obs, oldVal, newVal) -> repositionMinimizedWindows());
        freeFormPane.heightProperty().addListener((obs, oldVal, newVal) -> repositionMinimizedWindows());
    }
    
    public void addWindow(InternalWindow window) {
        windows.add(window);
        window.setParentContainer(getCurrentContainer());
        window.setWindowManager(this);
        
        if (gridMode) {
            addToGrid(window);
        } else {
            freeFormPane.getChildren().add(window);
        }
        
        bringWindowToFront(window);
        updateTaskbar();
    }
    
    public boolean addWindowByType(String windowType, InternalWindow window) {
        if (windowsByType.containsKey(windowType)) {
            InternalWindow existingWindow = windowsByType.get(windowType);
            bringWindowToFront(existingWindow);
            if (existingWindow.isMinimized()) {
                existingWindow.toggleMinimize();
            }
            return false; // Window already exists, focused instead
        }
        
        windowsByType.put(windowType, window);
        addWindow(window);
        return true; // New window created
    }
    
    public void removeWindow(InternalWindow window) {
        windows.remove(window);
        
        // Remove from type mapping
        windowsByType.entrySet().removeIf(entry -> entry.getValue() == window);
        
        if (focusedWindow == window) {
            focusedWindow = null;
            if (!windows.isEmpty()) {
                bringWindowToFront(windows.get(windows.size() - 1));
            }
        }
        
        if (gridMode) {
            gridPane.getChildren().remove(window);
        } else {
            freeFormPane.getChildren().remove(window);
        }
        
        updateTaskbar();
        repositionMinimizedWindows();
    }
    
    public void toggleGridMode() {
        gridMode = !gridMode;
        
        if (gridMode) {
            // Restore all minimized windows before switching to grid mode
            for (InternalWindow window : windows) {
                if (window.isMinimized()) {
                    window.toggleMinimize();
                }
            }
            switchToGridMode();
        } else {
            switchToFreeFormMode();
        }
    }
    
    private void switchToGridMode() {
        // Remove windows from free form pane
        for (InternalWindow window : windows) {
            if (freeFormPane.getChildren().contains(window)) {
                freeFormPane.getChildren().remove(window);
            }
        }
        freeFormPane.setVisible(false);
        
        gridPane.getChildren().clear();
        gridPane.setVisible(true);
        
        for (InternalWindow window : windows) {
            window.setParentContainer(gridPane);
            addToGrid(window);
        }
    }
    
    private void switchToFreeFormMode() {
        // Remove windows from grid pane
        for (InternalWindow window : windows) {
            if (gridPane.getChildren().contains(window)) {
                gridPane.getChildren().remove(window);
            }
        }
        gridPane.setVisible(false);
        
        freeFormPane.setVisible(true);
        
        for (InternalWindow window : windows) {
            window.setParentContainer(freeFormPane);
            if (!freeFormPane.getChildren().contains(window)) {
                freeFormPane.getChildren().add(window);
            }
        }
    }
    
    private void addToGrid(InternalWindow window) {
        int totalCells = gridRows * gridCols;
        int windowIndex = windows.indexOf(window);
        
        if (windowIndex < totalCells) {
            int row = windowIndex / gridCols;
            int col = windowIndex % gridCols;
            
            GridPane.setRowIndex(window, row);
            GridPane.setColumnIndex(window, col);
            GridPane.setHgrow(window, Priority.ALWAYS);
            GridPane.setVgrow(window, Priority.ALWAYS);
            
            gridPane.getChildren().add(window);
            
            window.setLayoutX(0);
            window.setLayoutY(0);
            window.setPrefSize(
                (gridPane.getWidth() - gridPane.getHgap() * (gridCols - 1)) / gridCols,
                (gridPane.getHeight() - gridPane.getVgap() * (gridRows - 1)) / gridRows
            );
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
    
    private void updateGridConstraints() {
        gridPane.getRowConstraints().clear();
        gridPane.getColumnConstraints().clear();
        
        for (int i = 0; i < gridRows; i++) {
            gridPane.getRowConstraints().add(new javafx.scene.layout.RowConstraints());
            gridPane.getRowConstraints().get(i).setVgrow(Priority.ALWAYS);
        }
        
        for (int i = 0; i < gridCols; i++) {
            gridPane.getColumnConstraints().add(new javafx.scene.layout.ColumnConstraints());
            gridPane.getColumnConstraints().get(i).setHgrow(Priority.ALWAYS);
        }
    }
    
    private void updateTaskbar() {
        taskbar.getChildren().clear();
        
        for (InternalWindow window : windows) {
            javafx.scene.control.Button taskButton = new javafx.scene.control.Button(window.getTitle());
            taskButton.setPrefWidth(120);
            taskButton.setStyle(getTaskButtonStyle(window));
            
            taskButton.setOnAction(e -> {
                if (window.isMinimized()) {
                    window.toggleMinimize();
                }
                bringWindowToFront(window);
            });
            
            taskbar.getChildren().add(taskButton);
        }
    }
    
    private String getTaskButtonStyle(InternalWindow window) {
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
    
    private void arrangeCascade() {
        double offsetX = 30;
        double offsetY = 30;
        double windowWidth = 400;
        double windowHeight = 300;
        
        for (int i = 0; i < windows.size(); i++) {
            InternalWindow window = windows.get(i);
            window.setLayoutX(i * offsetX);
            window.setLayoutY(i * offsetY);
            window.setPrefSize(windowWidth, windowHeight);
        }
    }
    
    private void arrangeTileHorizontal() {
        double containerWidth = freeFormPane.getWidth();
        double windowHeight = freeFormPane.getHeight() / windows.size();
        
        for (int i = 0; i < windows.size(); i++) {
            InternalWindow window = windows.get(i);
            window.setLayoutX(0);
            window.setLayoutY(i * windowHeight);
            window.setPrefSize(containerWidth, windowHeight);
        }
    }
    
    private void arrangeTileVertical() {
        double containerHeight = freeFormPane.getHeight();
        double windowWidth = freeFormPane.getWidth() / windows.size();
        
        for (int i = 0; i < windows.size(); i++) {
            InternalWindow window = windows.get(i);
            window.setLayoutX(i * windowWidth);
            window.setLayoutY(0);
            window.setPrefSize(windowWidth, containerHeight);
        }
    }
    
    public boolean isGridMode() {
        return gridMode;
    }
    
    public int getGridRows() {
        return gridRows;
    }
    
    public int getGridCols() {
        return gridCols;
    }
    
    public List<InternalWindow> getWindows() {
        return new ArrayList<>(windows);
    }
    
    public void bringWindowToFront(InternalWindow window) {
        if (focusedWindow != null) {
            focusedWindow.setFocus(false);
        }
        
        focusedWindow = window;
        window.setFocus(true);
        
        if (!gridMode) {
            // Ensure proper z-order by moving to front of parent's children
            Pane parent = (Pane) window.getParent();
            if (parent != null && parent.getChildren().contains(window)) {
                parent.getChildren().remove(window);
                parent.getChildren().add(window);
            }
        }
        
        updateTaskbar();
    }
    
    public void repositionMinimizedWindows() {
        if (gridMode) return;
        
        List<InternalWindow> minimizedWindows = new ArrayList<>();
        for (InternalWindow window : windows) {
            if (window.isMinimized()) {
                minimizedWindows.add(window);
            }
        }
        
        double containerWidth = freeFormPane.getWidth();
        double containerHeight = freeFormPane.getHeight();
        double minimizedHeight = 30;
        double windowWidth = Math.min(250, containerWidth / Math.max(1, minimizedWindows.size()));
        
        for (int i = 0; i < minimizedWindows.size(); i++) {
            InternalWindow window = minimizedWindows.get(i);
            window.setLayoutX(i * windowWidth);
            window.setLayoutY(containerHeight - minimizedHeight);
            window.setPrefWidth(windowWidth);
            window.toBack();
        }
    }
    
    private Pane getCurrentContainer() {
        return gridMode ? gridPane : freeFormPane;
    }
    
    public enum WindowArrangement {
        CASCADE, TILE_HORIZONTAL, TILE_VERTICAL
    }
}