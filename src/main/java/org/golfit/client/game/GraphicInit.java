//package org.golfit.client.game;
//
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.scene.Scene;
//import javafx.scene.layout.Pane;
//import javafx.stage.Stage;
//
///**
// * Standalone JavaFX application that initializes the game setting.
// */
//public class GraphicInit extends Application {
//
//    @Override
//    public void start(Stage primaryStage) {
//        GameView gameView = new GameView();
//        Pane canvas = gameView.getRoot();
//
//        if (org.golfit.Main.currentClient != null) {
//            org.golfit.Main.currentClient.getMovementHandler().setGamePane(canvas);
//
//            // Connect to server only AFTER pane is ready to avoid missing initial spawn packets
//            String username = org.golfit.Main.currentClient.clientApi.getUsername();
//            if (username != null) {
//                try {
//                    org.golfit.Main.currentClient.clientApi.connect(username);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//
//        Scene scene = new Scene(canvas, 800, 600);
//        primaryStage.setTitle("GolfIt - Sultans of Swing");
//        primaryStage.setScene(scene);
//
//        primaryStage.setOnCloseRequest(e -> Platform.exit());
//
//        primaryStage.show();
//        primaryStage.toFront();
//        primaryStage.requestFocus();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}