package org.example.windowdemo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private Pane freeFormPane;
    private GridPane gridPane;
    private HBox taskbar;
    private WindowManager windowManager;
    private int windowCounter = 1;
    
    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);
        
        StackPane centerStack = new StackPane();
        
        freeFormPane = new Pane();
        freeFormPane.setStyle("-fx-background-color: #f0f0f0;");
        
        gridPane = new GridPane();
        gridPane.setStyle("-fx-background-color: #e8f5e8;");
        
        centerStack.getChildren().addAll(freeFormPane, gridPane);
        root.setCenter(centerStack);
        
        taskbar = new HBox(5);
        taskbar.setPadding(new Insets(5));
        taskbar.setStyle("-fx-background-color: #333333;");
        taskbar.setPrefHeight(40);
        root.setBottom(taskbar);
        
        windowManager = new WindowManager(freeFormPane, gridPane, taskbar);
        
        createInitialWindows();
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Advanced Internal Windows Demo");
        stage.setScene(scene);
        stage.show();
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        Menu windowMenu = new Menu("Window");
        
        MenuItem newTextWindow = new MenuItem("New Text Window");
        newTextWindow.setOnAction(e -> createWindowByType("text"));
        
        MenuItem newFormWindow = new MenuItem("New Form Window");
        newFormWindow.setOnAction(e -> createWindowByType("form"));
        
        MenuItem newListWindow = new MenuItem("New List Window");
        newListWindow.setOnAction(e -> createWindowByType("list"));
        
        windowMenu.getItems().addAll(newTextWindow, newFormWindow, newListWindow);
        
        Menu layoutMenu = new Menu("Layout");
        
        CheckMenuItem gridModeItem = new CheckMenuItem("Grid Mode");
        gridModeItem.setOnAction(e -> windowManager.toggleGridMode());
        
        Menu gridSizeMenu = new Menu("Grid Size");
        
        MenuItem grid2x2 = new MenuItem("2×2");
        grid2x2.setOnAction(e -> windowManager.setGridSize(2, 2));
        
        MenuItem grid3x3 = new MenuItem("3×3");
        grid3x3.setOnAction(e -> windowManager.setGridSize(3, 3));
        
        MenuItem grid2x3 = new MenuItem("2×3");
        grid2x3.setOnAction(e -> windowManager.setGridSize(2, 3));
        
        gridSizeMenu.getItems().addAll(grid2x2, grid3x3, grid2x3);
        
        Menu arrangeMenu = new Menu("Arrange");
        
        MenuItem cascade = new MenuItem("Cascade");
        cascade.setOnAction(e -> windowManager.arrangeWindows(WindowManager.WindowArrangement.CASCADE));
        
        MenuItem tileHorizontal = new MenuItem("Tile Horizontally");
        tileHorizontal.setOnAction(e -> windowManager.arrangeWindows(WindowManager.WindowArrangement.TILE_HORIZONTAL));
        
        MenuItem tileVertical = new MenuItem("Tile Vertically");
        tileVertical.setOnAction(e -> windowManager.arrangeWindows(WindowManager.WindowArrangement.TILE_VERTICAL));
        
        arrangeMenu.getItems().addAll(cascade, tileHorizontal, tileVertical);
        
        layoutMenu.getItems().addAll(gridModeItem, gridSizeMenu, arrangeMenu);
        
        menuBar.getMenus().addAll(windowMenu, layoutMenu);
        
        return menuBar;
    }
    
    private void createInitialWindows() {
        createWindowByType("text");
        createWindowByType("form");
    }
    
    private void createWindowByType(String type) {
        switch (type) {
            case "text":
                createTextWindow();
                break;
            case "form":
                createFormWindow();
                break;
            case "list":
                createListWindow();
                break;
        }
    }
    
    private void createTextWindow() {
        InternalWindow window = new InternalWindow("Text Window");
        
        TextArea textArea = new TextArea();
        textArea.setText("This is a text window. You can:\n" +
                        "• Drag the title bar to move this window\n" +
                        "• Resize by dragging the edges\n" +
                        "• Minimize using the — button\n" +
                        "• Maximize using the □ button (or double-click title)\n" +
                        "• Close using the × button\n" +
                        "• Type in this text area\n\n" +
                        "Try the Layout menu for grid mode and arrangements!");
        textArea.setPrefRowCount(10);
        
        window.setContent(textArea);
        window.setLayoutX(50);
        window.setLayoutY(50);
        
        windowManager.addWindowByType("text", window);
    }
    
    private void createFormWindow() {
        InternalWindow window = new InternalWindow("Form Window");
        
        VBox form = new VBox(10);
        form.setPadding(new Insets(10));
        
        form.getChildren().addAll(
            new Label("Name:"),
            new TextField(),
            new Label("Email:"),
            new TextField(),
            new Label("Message:"),
            new TextArea(),
            new Button("Submit")
        );
        
        window.setContent(form);
        window.setLayoutX(200);
        window.setLayoutY(100);
        
        windowManager.addWindowByType("form", window);
    }
    
    private void createListWindow() {
        InternalWindow window = new InternalWindow("List Window");
        
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        
        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(
            "Item 1", "Item 2", "Item 3", "Item 4", "Item 5",
            "Item 6", "Item 7", "Item 8", "Item 9", "Item 10"
        );
        
        Button addButton = new Button("Add Item");
        addButton.setOnAction(e -> {
            listView.getItems().add("New Item " + (listView.getItems().size() + 1));
        });
        
        container.getChildren().addAll(new Label("Sample List:"), listView, addButton);
        
        window.setContent(container);
        window.setLayoutX(100);
        window.setLayoutY(200);
        window.setPrefSize(300, 400);
        
        windowManager.addWindowByType("list", window);
    }
}
