package org.golfit.graphics;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


public class Window extends Application {
    public static final int screen_width = 1280;
    public static final int screen_height = 720;
    Color background_color = Color.rgb(121,213,33);

    public void start(Stage stage) {
        Rectangle rect = new Rectangle(screen_width, screen_height);
        rect.setFill(background_color);
        Group root = new Group(rect);
        Scene scene = new Scene(root, screen_width, screen_height);

        stage.setTitle("Golfit");
        stage.setScene(scene);
        stage.show();
    }
}