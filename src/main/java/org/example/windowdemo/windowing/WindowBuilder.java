package org.example.windowdemo.windowing;

import javafx.scene.Node;
import javafx.scene.image.Image;

/**
 * Builder pattern for creating windows in the WindowSystem
 * 
 * Usage:
 * WindowBuilder.create("My Window")
 *     .windowType("text-editor")
 *     .icon(myIcon)
 *     .content(myContentNode)
 *     .size(400, 300)
 *     .position(100, 50)
 *     .build()
 */
public class WindowBuilder {
    private String title;
    private String windowType;
    private Image icon;
    private Node content;
    private double width = -1;
    private double height = -1;
    private double x = -1;
    private double y = -1;
    
    private WindowBuilder(String title) {
        this.title = title;
    }
    
    /**
     * Create a new WindowBuilder
     * @param title The window title
     * @return WindowBuilder instance
     */
    public static WindowBuilder create(String title) {
        return new WindowBuilder(title);
    }
    
    /**
     * Set window type for duplicate prevention
     * @param windowType Unique identifier for this type of window
     * @return this builder
     */
    public WindowBuilder windowType(String windowType) {
        this.windowType = windowType;
        return this;
    }
    
    /**
     * Set window icon
     * @param icon Image to display in title bar and taskbar
     * @return this builder
     */
    public WindowBuilder icon(Image icon) {
        this.icon = icon;
        return this;
    }
    
    /**
     * Set window content
     * @param content JavaFX Node to display in window body
     * @return this builder
     */
    public WindowBuilder content(Node content) {
        this.content = content;
        return this;
    }
    
    /**
     * Set window size
     * @param width Window width
     * @param height Window height
     * @return this builder
     */
    public WindowBuilder size(double width, double height) {
        this.width = width;
        this.height = height;
        return this;
    }
    
    /**
     * Set window position
     * @param x X coordinate
     * @param y Y coordinate
     * @return this builder
     */
    public WindowBuilder position(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    // Getters
    public String getTitle() { return title; }
    public String getWindowType() { return windowType; }
    public Image getIcon() { return icon; }
    public Node getContent() { return content; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getX() { return x; }
    public double getY() { return y; }
}