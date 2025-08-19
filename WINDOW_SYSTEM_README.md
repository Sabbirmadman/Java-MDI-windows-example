# Generic JavaFX Window Management System

A reusable, generic window management system for JavaFX applications that provides internal windows with full window management capabilities.

## Features

✅ **Click-to-Focus**: Click anywhere on a window (including inputs, text areas) to bring it to front  
✅ **Drag & Drop**: Drag windows by title bar  
✅ **Resize**: Resize from any edge or corner  
✅ **Minimize/Maximize**: Standard window controls  
✅ **Grid Layout**: Organize windows in a grid  
✅ **Window Arrangements**: Cascade, Tile Horizontally, Tile Vertically  
✅ **Taskbar Integration**: Bottom taskbar with window buttons  
✅ **Icon Support**: Icons in title bar and taskbar  
✅ **Duplicate Prevention**: One window per type  
✅ **Generic/Reusable**: Easy integration into any JavaFX project  

## Quick Start

### 1. Copy the Window System Files

Copy these files to your project:
- `windowing/WindowSystem.java`
- `windowing/WindowBuilder.java` 
- `windowing/InternalWindowPane.java`

### 2. Basic Setup

```java
import org.yourpackage.windowing.*;

// Create containers
Pane freeFormContainer = new Pane();
GridPane gridContainer = new GridPane(); 
HBox taskbarContainer = new HBox();

// Initialize window system
WindowSystem windowSystem = new WindowSystem(
    freeFormContainer, 
    gridContainer, 
    taskbarContainer
);

// Add to your scene
BorderPane root = new BorderPane();
StackPane center = new StackPane(freeFormContainer, gridContainer);
root.setCenter(center);
root.setBottom(taskbarContainer);
```

### 3. Create Windows

```java
// Simple window
windowSystem.addWindow(
    WindowBuilder.create("My Window")
        .content(new Label("Hello World"))
);

// Advanced window with icon and positioning
Image icon = new Image("path/to/icon.png");
TextArea textArea = new TextArea();

windowSystem.addWindow(
    WindowBuilder.create("Text Editor")
        .windowType("text-editor")    // Prevents duplicates
        .icon(icon)                   // 16x16 icon
        .content(textArea)
        .size(400, 300)
        .position(100, 50)
);
```

## WindowBuilder API

### Methods

- `WindowBuilder.create(String title)` - Create new builder
- `.windowType(String type)` - Set window type for duplicate prevention
- `.icon(Image icon)` - Set 16x16 icon for title bar and taskbar
- `.content(Node content)` - Set window content
- `.size(double width, double height)` - Set window size
- `.position(double x, double y)` - Set window position

### Example

```java
windowSystem.addWindow(
    WindowBuilder.create("Calculator")
        .windowType("calculator")
        .icon(calculatorIcon)
        .content(calculatorLayout)
        .size(250, 300)
        .position(200, 100)
);
```

## WindowSystem API

### Core Methods

```java
// Window management
boolean addWindow(WindowBuilder builder)  // Returns false if duplicate
void removeWindow(InternalWindowPane window)
void bringWindowToFront(InternalWindowPane window)

// Layout modes
void toggleGridMode()
void setGridSize(int rows, int cols)
void arrangeWindows(WindowArrangement arrangement)

// Getters
boolean isGridMode()
List<InternalWindowPane> getWindows()
```

### Window Arrangements

```java
windowSystem.arrangeWindows(WindowSystem.WindowArrangement.CASCADE);
windowSystem.arrangeWindows(WindowSystem.WindowArrangement.TILE_HORIZONTAL);
windowSystem.arrangeWindows(WindowSystem.WindowArrangement.TILE_VERTICAL);
```

## Adding Icons

### Method 1: Load from File

```java
Image icon = new Image("file:icons/text-editor.png");
// or
Image icon = new Image(getClass().getResourceAsStream("/icons/text-editor.png"));
```

### Method 2: Create Programmatically

```java
// Simple text-based icon
private Image createIcon(String text, Color color) {
    Canvas canvas = new Canvas(16, 16);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    
    // Background
    gc.setFill(Color.WHITE);
    gc.fillRect(0, 0, 16, 16);
    
    // Border
    gc.setStroke(color);
    gc.strokeRect(0, 0, 16, 16);
    
    // Text
    gc.setFill(color);
    gc.setFont(Font.font(10));
    gc.fillText(text, 3, 12);
    
    WritableImage image = new WritableImage(16, 16);
    canvas.snapshot(null, image);
    return image;
}
```

### Method 3: Use Font Icons

```java
// Using FontAwesome or other icon fonts
Label iconLabel = new Label("\uf15c"); // FontAwesome file icon
iconLabel.setFont(Font.loadFont("fontawesome.ttf", 12));
```

## Integration Examples

### Basic Application Structure

```java
public class MyApplication extends Application {
    private WindowSystem windowSystem;
    
    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        
        // Setup containers
        Pane freeFormPane = new Pane();
        GridPane gridPane = new GridPane();
        HBox taskbar = new HBox();
        
        StackPane center = new StackPane(freeFormPane, gridPane);
        root.setCenter(center);
        root.setBottom(taskbar);
        
        // Initialize window system
        windowSystem = new WindowSystem(freeFormPane, gridPane, taskbar);
        
        // Create menu bar
        root.setTop(createMenuBar());
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu windowMenu = new Menu("Window");
        
        MenuItem textEditor = new MenuItem("Text Editor");
        textEditor.setOnAction(e -> createTextEditor());
        
        windowMenu.getItems().add(textEditor);
        menuBar.getMenus().add(windowMenu);
        return menuBar;
    }
    
    private void createTextEditor() {
        TextArea textArea = new TextArea();
        Image icon = new Image("text-editor-icon.png");
        
        windowSystem.addWindow(
            WindowBuilder.create("Text Editor")
                .windowType("text-editor")
                .icon(icon)
                .content(textArea)
                .size(500, 400)
        );
    }
}
```

### Menu Integration

```java
private MenuBar createMenuBar() {
    MenuBar menuBar = new MenuBar();
    
    // Window menu
    Menu windowMenu = new Menu("Window");
    MenuItem calculator = new MenuItem("Calculator");
    calculator.setOnAction(e -> windowSystem.addWindow(
        WindowBuilder.create("Calculator")
            .windowType("calculator")
            .content(createCalculatorContent())
    ));
    
    // Layout menu
    Menu layoutMenu = new Menu("Layout");
    CheckMenuItem gridMode = new CheckMenuItem("Grid Mode");
    gridMode.setOnAction(e -> windowSystem.toggleGridMode());
    
    layoutMenu.getItems().add(gridMode);
    windowMenu.getItems().add(calculator);
    menuBar.getMenus().addAll(windowMenu, layoutMenu);
    
    return menuBar;
}
```

## Styling

### Container Styling

```java
// Free-form background
freeFormContainer.setStyle("-fx-background-color: #f0f0f0;");

// Grid background (different color to distinguish modes)
gridContainer.setStyle("-fx-background-color: #e8f5e8;");

// Taskbar styling
taskbar.setStyle("-fx-background-color: #333333;");
taskbar.setPrefHeight(40);
```

### Window Styling

The windows automatically style themselves, but you can customize:

```java
// In InternalWindowPane.java, modify updateFocusAppearance()
private void updateFocusAppearance() {
    if (hasFocus) {
        titleBar.setStyle("-fx-background-color: #4A90E2;"); // Your color
    } else {
        titleBar.setStyle("-fx-background-color: #d3d3d3;"); // Your color
    }
}
```

## Best Practices

### 1. Window Types
Use meaningful window types to prevent duplicates:
```java
.windowType("text-editor")
.windowType("calculator") 
.windowType("image-viewer")
```

### 2. Icon Sizes
Always use 16x16 icons for best appearance in title bars and taskbar.

### 3. Content Layout
Structure your window content properly:
```java
VBox content = new VBox(10);
content.setPadding(new Insets(10));
content.getChildren().addAll(mainArea, buttonBar);
```

### 4. Resource Management
Load icons efficiently:
```java
// Cache icons
private static final Map<String, Image> iconCache = new HashMap<>();

private Image getIcon(String name) {
    return iconCache.computeIfAbsent(name, 
        k -> new Image(getClass().getResourceAsStream("/icons/" + k + ".png"))
    );
}
```

## Migration from Existing Code

If you have an existing window system, here's how to migrate:

### Before (Old System)
```java
InternalWindow window = new InternalWindow("Title");
window.setContent(content);
windowManager.addWindow(window);
```

### After (Generic System)
```java
windowSystem.addWindow(
    WindowBuilder.create("Title")
        .windowType("my-window")
        .content(content)
);
```

## Troubleshooting

### Issue: Click-to-focus not working
**Solution**: The system uses `addEventFilter` on `MouseEvent.MOUSE_PRESSED`. Make sure you're not consuming these events elsewhere.

### Issue: Icons not showing
**Solution**: Verify icon path and size (should be 16x16). Check console for loading errors.

### Issue: Windows not positioning correctly in grid
**Solution**: Ensure grid constraints are set up properly and windows are not manually positioned when in grid mode.

## Complete Example

See `GenericSystemExample.java` for a complete working example with:
- Text editor with icon
- Calculator with custom layout  
- Image viewer placeholder
- Menu integration
- Icon creation methods

## License

This window system is designed to be integrated into your projects. Modify as needed for your requirements.