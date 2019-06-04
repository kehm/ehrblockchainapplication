/* SPDX-License-Identifier: Apache-2.0 */
package com.example.hlfnetworkapplication.javafx;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class to initialize JavaFX elements
 *
 * @author kehm
 */
public class MainApp extends Application {

    /**
     * Main method to launch user interface
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("HLF Network Application");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(402);
        primaryStage.setMinWidth(592);
        primaryStage.show();
    }
}
