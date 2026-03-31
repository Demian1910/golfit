//package org.golfit.graphics;
//
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.layout.Pane;
//import javafx.stage.Stage;
//import org.golfit.server.game.ServerBall;
//
//
//import java.util.List;
//
//
//public class Window extends Application {
//
//    /**
//     * Creates a new window and initializes main pane
//     *
//     * @param stage the javaFX stage
//     */
//    @Override
//    public void start(Stage stage) {
//        Pane root = new Pane();
//        Scene scene = new Scene(root, INIT_WIDTH, INIT_HEIGHT);
//        Game game = new Game(scene);
//        root.getChildren().add((game.getRoot()));
//
//
//        // Show stage
//        stage.setTitle("Golfit");
//        stage.setScene(scene);
//        stage.show();
//
//        Course course = game.getCourse();
//
//
//        // Run once for MapData
//         HelpGridMap helpGridMap = new HelpGridMap(course);
//         // helpGridMap.updateGrids();
//        // helpGridMap.verifyIdMapping();
//
//
//
//    }
//
//
//}