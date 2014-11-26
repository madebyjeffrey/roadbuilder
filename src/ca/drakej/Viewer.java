package ca.drakej;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Created by drakej on 2014-11-25.
 */

public class Viewer extends Application {
    public static void main(String[] arguments) {
        launch(arguments);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Viewer.fxml"));
        loader.setRoot(new BorderPane());
        Parent parent = (Parent)loader.load();
        ViewerController controller = (ViewerController)loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(loader.getRoot(), 800, 600));
        primaryStage.setTitle("Viewer");
        primaryStage.show();
    }
}
