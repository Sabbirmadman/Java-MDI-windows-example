package org.example.windowdemo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.windowdemo.windowing.WindowBuilder;
import org.example.windowdemo.windowing.WindowSystem;

/**
 * Example showing how to use the generic WindowSystem in any JavaFX application
 */
public class GenericSystemExample extends Application {
    private WindowSystem windowSystem;
    
    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        
        // Create menu bar
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);
        
        // Create containers for the window system
        StackPane centerStack = new StackPane();
        
        // Free-form container (for draggable windows)
        Pane freeFormContainer = new Pane();
        freeFormContainer.setStyle("-fx-background-color: #f0f0f0;");
        
        // Grid container (for grid layout)
        GridPane gridContainer = new GridPane();
        gridContainer.setStyle("-fx-background-color: #e8f5e8;");
        
        centerStack.getChildren().addAll(freeFormContainer, gridContainer);
        root.setCenter(centerStack);
        
        // Taskbar container
        HBox taskbarContainer = new HBox(5);
        taskbarContainer.setPadding(new Insets(5));
        taskbarContainer.setStyle("-fx-background-color: #333333;");
        taskbarContainer.setPrefHeight(40);
        root.setBottom(taskbarContainer);
        
        // Initialize the window system
        windowSystem = new WindowSystem(freeFormContainer, gridContainer, taskbarContainer);
        
        // Create some initial windows with icons
        createInitialWindows();
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Generic Window System Example");
        stage.setScene(scene);
        stage.show();
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // Window menu
        Menu windowMenu = new Menu("Window");
        
        MenuItem textWindow = new MenuItem("Text Editor");
        textWindow.setOnAction(e -> createTextWindow());
        
        MenuItem calculatorWindow = new MenuItem("Calculator");
        calculatorWindow.setOnAction(e -> createCalculatorWindow());
        
        MenuItem imageViewerWindow = new MenuItem("Image Viewer");
        imageViewerWindow.setOnAction(e -> createImageViewerWindow());
        
        windowMenu.getItems().addAll(textWindow, calculatorWindow, imageViewerWindow);
        
        // Layout menu
        Menu layoutMenu = new Menu("Layout");
        
        CheckMenuItem gridModeItem = new CheckMenuItem("Grid Mode");
        gridModeItem.setOnAction(e -> windowSystem.toggleGridMode());
        
        Menu gridSizeMenu = new Menu("Grid Size");
        MenuItem grid2x2 = new MenuItem("2×2");
        grid2x2.setOnAction(e -> windowSystem.setGridSize(2, 2));
        MenuItem grid3x3 = new MenuItem("3×3");
        grid3x3.setOnAction(e -> windowSystem.setGridSize(3, 3));
        gridSizeMenu.getItems().addAll(grid2x2, grid3x3);
        
        Menu arrangeMenu = new Menu("Arrange");
        MenuItem cascade = new MenuItem("Cascade");
        cascade.setOnAction(e -> windowSystem.arrangeWindows(WindowSystem.WindowArrangement.CASCADE));
        MenuItem tileH = new MenuItem("Tile Horizontally");
        tileH.setOnAction(e -> windowSystem.arrangeWindows(WindowSystem.WindowArrangement.TILE_HORIZONTAL));
        MenuItem tileV = new MenuItem("Tile Vertically");
        tileV.setOnAction(e -> windowSystem.arrangeWindows(WindowSystem.WindowArrangement.TILE_VERTICAL));
        arrangeMenu.getItems().addAll(cascade, tileH, tileV);
        
        layoutMenu.getItems().addAll(gridModeItem, gridSizeMenu, arrangeMenu);
        
        menuBar.getMenus().addAll(windowMenu, layoutMenu);
        return menuBar;
    }
    
    private void createInitialWindows() {
        createTextWindow();
        createCalculatorWindow();
    }
    
    private void createTextWindow() {
        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TextArea textArea = new TextArea();
        textArea.setText("This is a text editor window with an icon!\n\n" +
                        "Features:\n" +
                        "• Click anywhere in this window to focus it\n" +
                        "• Drag title bar to move\n" +
                        "• Resize from edges/corners\n" +
                        "• Minimize, maximize, close buttons\n" +
                        "• Icon in title bar and taskbar");
        textArea.setPrefRowCount(10);
        
        Button saveButton = new Button("Save");
        Button loadButton = new Button("Load");
        HBox buttonBar = new HBox(10, saveButton, loadButton);
        
        content.getChildren().addAll(textArea, buttonBar);
        
        // Load icon (you can use any 16x16 image)
        Image textIcon = createSimpleIcon("T", javafx.scene.paint.Color.BLUE);
        
        // Create window using WindowBuilder
        windowSystem.addWindow(
            WindowBuilder.create("Text Editor")
                .windowType("text-editor")
                .icon(textIcon)
                .content(content)
                .size(500, 400)
                .position(50, 50)
        );
    }
    
    private void createCalculatorWindow() {
        // Create calculator content
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        
        TextField display = new TextField("0");
        display.setStyle("-fx-font-size: 20px; -fx-alignment: center-right;");
        display.setEditable(false);
        
        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(5);
        buttonGrid.setVgap(5);
        
        String[][] buttons = {
            {"C", "±", "%", "÷"},
            {"7", "8", "9", "×"},
            {"4", "5", "6", "-"},
            {"1", "2", "3", "+"},
            {"0", ".", "=", "="}
        };
        
        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                if (row == 4 && col == 3) continue; // Skip duplicate =
                
                Button btn = new Button(buttons[row][col]);
                btn.setPrefSize(50, 40);
                btn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                
                if (row == 4 && col == 0) { // 0 button spans 2 columns
                    GridPane.setColumnSpan(btn, 2);
                }
                
                buttonGrid.add(btn, col, row);
            }
        }
        
        content.getChildren().addAll(display, buttonGrid);
        
        // Create calculator icon
        Image calcIcon = createSimpleIcon("#", javafx.scene.paint.Color.GREEN);
        
        windowSystem.addWindow(
            WindowBuilder.create("Calculator")
                .windowType("calculator")
                .icon(calcIcon)
                .content(content)
                .size(220, 300)
                .position(200, 100)
        );
    }
    
    private void createImageViewerWindow() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Placeholder image viewer
        Label placeholder = new Label("Image Viewer");
        placeholder.setStyle("-fx-font-size: 24px; -fx-alignment: center;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(placeholder);
        scrollPane.setPrefSize(400, 300);
        scrollPane.setStyle("-fx-background-color: #f0f0f0;");
        
        HBox controls = new HBox(10);
        Button openButton = new Button("Open Image");
        Button zoomInButton = new Button("Zoom In");
        Button zoomOutButton = new Button("Zoom Out");
        controls.getChildren().addAll(openButton, zoomInButton, zoomOutButton);
        
        content.getChildren().addAll(scrollPane, controls);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Create image viewer icon
        Image imageIcon = createSimpleIcon("📷", javafx.scene.paint.Color.PURPLE);
        
        windowSystem.addWindow(
            WindowBuilder.create("Image Viewer")
                .windowType("image-viewer")
                .icon(imageIcon)
                .content(content)
                .size(450, 400)
                .position(300, 150)
        );
    }
    
    /**
     * Helper method to create simple text-based icons
     * In a real application, you would load actual image files
     */
    private Image createSimpleIcon(String text, javafx.scene.paint.Color color) {
        // Create a simple 16x16 canvas with text
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(16, 16);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Background
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillRect(0, 0, 16, 16);
        
        // Border
        gc.setStroke(color);
        gc.strokeRect(0, 0, 16, 16);
        
        // Text
        gc.setFill(color);
        gc.setFont(javafx.scene.text.Font.font(10));
        gc.fillText(text, 3, 12);
        
        // Convert canvas to image
        javafx.scene.image.WritableImage image = new javafx.scene.image.WritableImage(16, 16);
        canvas.snapshot(null, image);
        
        return image;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}