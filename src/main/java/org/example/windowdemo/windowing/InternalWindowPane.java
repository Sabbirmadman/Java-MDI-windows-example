package org.example.windowdemo.windowing;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Generic Internal Window Pane
 * 
 * A reusable internal window implementation that can be used in any JavaFX application.
 * Supports dragging, resizing, minimizing, maximizing, and custom content.
 */
public class InternalWindowPane extends VBox {
    private static final double RESIZE_MARGIN = 8;
    private static final double MIN_WIDTH = 200;
    private static final double MIN_HEIGHT = 150;
    private static final double MINIMIZED_HEIGHT = 30;
    
    private double dragOffsetX;
    private double dragOffsetY;
    private double resizeOffsetX;
    private double resizeOffsetY;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private ResizeDirection resizeDirection = ResizeDirection.NONE;
    
    private HBox titleBar;
    private Label titleLabel;
    private ImageView iconView;
    private Button minimizeButton;
    private Button maximizeButton;
    private Button closeButton;
    private VBox contentArea;
    
    private WindowState windowState = WindowState.NORMAL;
    private double normalX, normalY, normalWidth, normalHeight;
    private WindowSystem windowSystem;
    private boolean hasFocus = false;
    private Image icon;
    
    public InternalWindowPane(String title, WindowSystem windowSystem) {
        this.windowSystem = windowSystem;
        initializeWindow(title);
        setupEventHandlers();
    }
    
    private void initializeWindow(String title) {
        setMinWidth(MIN_WIDTH);
        setMinHeight(MIN_HEIGHT);
        setPrefSize(400, 300);
        
        setBorder(new Border(new BorderStroke(
            Color.DARKGRAY, BorderStrokeStyle.SOLID, 
            new CornerRadii(5.0), BorderWidths.DEFAULT
        )));
        
        setBackground(new Background(new BackgroundFill(
            Color.WHITE, new CornerRadii(5.0), Insets.EMPTY
        )));
        
        createTitleBar(title);
        createContentArea();
        
        getChildren().addAll(titleBar, contentArea);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
    }
    
    private void createTitleBar(String title) {
        titleBar = new HBox();
        titleBar.setPadding(new Insets(8, 8, 8, 8));
        titleBar.setSpacing(10);
        
        iconView = new ImageView();
        iconView.setFitWidth(16);
        iconView.setFitHeight(16);
        iconView.setVisible(false);
        
        titleLabel = new Label(title);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        minimizeButton = new Button("—");
        minimizeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                              "-fx-font-size: 12px; -fx-font-weight: bold; " +
                              "-fx-min-width: 20px; -fx-min-height: 20px; " +
                              "-fx-max-width: 20px; -fx-max-height: 20px;");
        
        maximizeButton = new Button("□");
        maximizeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                              "-fx-font-size: 12px; -fx-font-weight: bold; " +
                              "-fx-min-width: 20px; -fx-min-height: 20px; " +
                              "-fx-max-width: 20px; -fx-max-height: 20px;");
        
        closeButton = new Button("×");
        closeButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                           "-fx-font-size: 14px; -fx-font-weight: bold; " +
                           "-fx-min-width: 20px; -fx-min-height: 20px; " +
                           "-fx-max-width: 20px; -fx-max-height: 20px;");
        
        titleBar.getChildren().addAll(iconView, titleLabel, spacer, minimizeButton, maximizeButton, closeButton);
        
        updateFocusAppearance();
    }
    
    private void createContentArea() {
        contentArea = new VBox();
        contentArea.setPadding(new Insets(10));
        contentArea.setStyle("-fx-background-color: white;");
    }
    
    private void setupEventHandlers() {
        setupDragHandlers();
        setupResizeHandlers();
        setupWindowControlHandlers();
        setupFocusHandlers();
    }
    
    private void setupDragHandlers() {
        titleBar.setOnMouseDragged(e -> {
            if (isDragging) {
                setLayoutX(e.getSceneX() - dragOffsetX);
                setLayoutY(e.getSceneY() - dragOffsetY);
            }
        });
        
        titleBar.setOnMouseReleased(e -> {
            isDragging = false;
            titleBar.setCursor(Cursor.DEFAULT);
        });
    }
    
    private void setupResizeHandlers() {
        setOnMouseMoved(e -> {
            if (!isDragging) {
                ResizeDirection direction = getResizeDirection(e.getX(), e.getY());
                updateCursor(direction);
                resizeDirection = direction;
            }
        });
        
        setOnMousePressed(e -> {
            if (resizeDirection != ResizeDirection.NONE) {
                isResizing = true;
                resizeOffsetX = e.getSceneX();
                resizeOffsetY = e.getSceneY();
            }
        });
        
        setOnMouseDragged(e -> {
            if (isResizing) {
                handleResize(e.getSceneX(), e.getSceneY());
            }
        });
        
        setOnMouseReleased(e -> {
            isResizing = false;
            setCursor(Cursor.DEFAULT);
        });
        
        setOnMouseExited(e -> {
            if (!isResizing) {
                setCursor(Cursor.DEFAULT);
            }
        });
    }
    
    private void setupWindowControlHandlers() {
        closeButton.setOnAction(e -> {
            if (windowSystem != null) {
                windowSystem.removeWindow(this);
            }
        });
        
        minimizeButton.setOnAction(e -> toggleMinimize());
        maximizeButton.setOnAction(e -> toggleMaximize());
        
        titleBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                toggleMaximize();
            }
        });
    }
    
    private void setupFocusHandlers() {
        // Add focus handler to the entire window and all its children
        addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            bringToFront();
        });
        
        titleBar.setOnMousePressed(e -> {
            bringToFront();
            if (!isResizing) {
                isDragging = true;
                dragOffsetX = e.getSceneX() - getLayoutX();
                dragOffsetY = e.getSceneY() - getLayoutY();
                titleBar.setCursor(Cursor.MOVE);
            }
        });
    }
    
    public void bringToFront() {
        if (windowSystem != null) {
            windowSystem.bringWindowToFront(this);
        } else {
            // Manual z-order management when no window system
            if (getParent() instanceof Pane) {
                Pane parent = (Pane) getParent();
                if (parent.getChildren().contains(this)) {
                    parent.getChildren().remove(this);
                    parent.getChildren().add(this);
                }
            }
            setFocus(true);
        }
    }
    
    public void setFocus(boolean focus) {
        this.hasFocus = focus;
        updateFocusAppearance();
    }
    
    private void updateFocusAppearance() {
        if (hasFocus) {
            titleBar.setStyle("-fx-background-color: #4A90E2; -fx-border-color: #2E5BA8; -fx-border-width: 1px;");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        } else {
            titleBar.setStyle("-fx-background-color: #d3d3d3; -fx-border-color: #a0a0a0; -fx-border-width: 1px;");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
        }
    }
    
    public void toggleMinimize() {
        if (windowState == WindowState.MINIMIZED) {
            restore();
        } else {
            minimize();
        }
    }
    
    private void minimize() {
        if (windowState == WindowState.NORMAL) {
            saveNormalState();
        }
        windowState = WindowState.MINIMIZED;
        contentArea.setVisible(false);
        contentArea.setManaged(false);
        setPrefHeight(MINIMIZED_HEIGHT);
        setMaxHeight(MINIMIZED_HEIGHT);
        minimizeButton.setText("□");
        
        if (windowSystem != null) {
            windowSystem.repositionMinimizedWindows();
        }
    }
    
    private void toggleMaximize() {
        if (windowState == WindowState.MAXIMIZED) {
            restore();
        } else {
            maximize();
        }
    }
    
    private void maximize() {
        if (windowState == WindowState.NORMAL) {
            saveNormalState();
        }
        windowState = WindowState.MAXIMIZED;
        contentArea.setVisible(true);
        contentArea.setManaged(true);
        setMaxHeight(Double.MAX_VALUE);
        
        if (getParent() instanceof Pane) {
            Pane parent = (Pane) getParent();
            setLayoutX(0);
            setLayoutY(0);
            setPrefSize(parent.getWidth(), parent.getHeight());
            maximizeButton.setText("❐");
        }
    }
    
    private void restore() {
        windowState = WindowState.NORMAL;
        contentArea.setVisible(true);
        contentArea.setManaged(true);
        setMaxHeight(Double.MAX_VALUE);
        
        setLayoutX(normalX);
        setLayoutY(normalY);
        setPrefSize(normalWidth, normalHeight);
        
        minimizeButton.setText("—");
        maximizeButton.setText("□");
        
        if (windowSystem != null) {
            windowSystem.repositionMinimizedWindows();
        }
    }
    
    private void saveNormalState() {
        normalX = getLayoutX();
        normalY = getLayoutY();
        normalWidth = getWidth();
        normalHeight = getHeight();
    }
    
    // Icon methods
    public void setIcon(Image icon) {
        this.icon = icon;
        if (icon != null) {
            iconView.setImage(icon);
            iconView.setVisible(true);
        } else {
            iconView.setVisible(false);
        }
    }
    
    public Image getIcon() {
        return icon;
    }
    
    // Content methods
    public void setContent(Node content) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }
    
    public VBox getContentArea() {
        return contentArea;
    }
    
    // Window state methods
    public WindowState getWindowState() {
        return windowState;
    }
    
    public boolean isMinimized() {
        return windowState == WindowState.MINIMIZED;
    }
    
    public boolean isMaximized() {
        return windowState == WindowState.MAXIMIZED;
    }
    
    public boolean hasFocus() {
        return hasFocus;
    }
    
    // Title methods
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
    
    public String getTitle() {
        return titleLabel.getText();
    }
    
    // Resize handling methods
    private ResizeDirection getResizeDirection(double x, double y) {
        boolean atLeft = x <= RESIZE_MARGIN;
        boolean atRight = x >= getWidth() - RESIZE_MARGIN;
        boolean atTop = y <= RESIZE_MARGIN;
        boolean atBottom = y >= getHeight() - RESIZE_MARGIN;
        
        if (atLeft && atTop) return ResizeDirection.NW;
        if (atRight && atTop) return ResizeDirection.NE;
        if (atLeft && atBottom) return ResizeDirection.SW;
        if (atRight && atBottom) return ResizeDirection.SE;
        if (atLeft) return ResizeDirection.W;
        if (atRight) return ResizeDirection.E;
        if (atTop) return ResizeDirection.N;
        if (atBottom) return ResizeDirection.S;
        
        return ResizeDirection.NONE;
    }
    
    private void updateCursor(ResizeDirection direction) {
        switch (direction) {
            case N:
            case S:
                setCursor(Cursor.N_RESIZE);
                break;
            case E:
            case W:
                setCursor(Cursor.E_RESIZE);
                break;
            case NE:
            case SW:
                setCursor(Cursor.NE_RESIZE);
                break;
            case NW:
            case SE:
                setCursor(Cursor.NW_RESIZE);
                break;
            default:
                setCursor(Cursor.DEFAULT);
                break;
        }
    }
    
    private void handleResize(double sceneX, double sceneY) {
        double deltaX = sceneX - resizeOffsetX;
        double deltaY = sceneY - resizeOffsetY;
        
        double newWidth = getWidth();
        double newHeight = getHeight();
        double newX = getLayoutX();
        double newY = getLayoutY();
        
        switch (resizeDirection) {
            case N:
                newHeight = Math.max(MIN_HEIGHT, getHeight() - deltaY);
                newY = getLayoutY() + (getHeight() - newHeight);
                break;
            case S:
                newHeight = Math.max(MIN_HEIGHT, getHeight() + deltaY);
                break;
            case W:
                newWidth = Math.max(MIN_WIDTH, getWidth() - deltaX);
                newX = getLayoutX() + (getWidth() - newWidth);
                break;
            case E:
                newWidth = Math.max(MIN_WIDTH, getWidth() + deltaX);
                break;
            case NW:
                newWidth = Math.max(MIN_WIDTH, getWidth() - deltaX);
                newHeight = Math.max(MIN_HEIGHT, getHeight() - deltaY);
                newX = getLayoutX() + (getWidth() - newWidth);
                newY = getLayoutY() + (getHeight() - newHeight);
                break;
            case NE:
                newWidth = Math.max(MIN_WIDTH, getWidth() + deltaX);
                newHeight = Math.max(MIN_HEIGHT, getHeight() - deltaY);
                newY = getLayoutY() + (getHeight() - newHeight);
                break;
            case SW:
                newWidth = Math.max(MIN_WIDTH, getWidth() - deltaX);
                newHeight = Math.max(MIN_HEIGHT, getHeight() + deltaY);
                newX = getLayoutX() + (getWidth() - newWidth);
                break;
            case SE:
                newWidth = Math.max(MIN_WIDTH, getWidth() + deltaX);
                newHeight = Math.max(MIN_HEIGHT, getHeight() + deltaY);
                break;
        }
        
        setPrefSize(newWidth, newHeight);
        setLayoutX(newX);
        setLayoutY(newY);
        
        resizeOffsetX = sceneX;
        resizeOffsetY = sceneY;
    }
    
    private enum ResizeDirection {
        NONE, N, S, E, W, NE, NW, SE, SW
    }
    
    public enum WindowState {
        NORMAL, MINIMIZED, MAXIMIZED
    }
}