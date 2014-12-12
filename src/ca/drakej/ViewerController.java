package ca.drakej;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;


import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Created by drakej on 2014-11-25.
 */
public class ViewerController implements Initializable {
    private Stage stage;

    private
    ObjectProperty<Map> map;

    public ObjectProperty<Map> mapProperty() {
        return map;
    }

    public Map getMap() {
        return map.get();
    }

    public void setMap(Map map) {
        this.map.setValue(map);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private Pane pane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.map = new SimpleObjectProperty<Map>(this, "map", new Map(0, 0));

        mapProperty().addListener((obs, oldValue, newValue) -> {
            final Group group = new Group();
            group.setAutoSizeChildren(false);

            final double paneMin = Math.min(pane.getWidth(), pane.getHeight());
            final double ourMin = Math.min(newValue.getWidth(), newValue.getHeight());
            final double scale = paneMin / ourMin;

            for (int i = 1; i < getMap().getWidth(); i++)
            {
                for (int j = 1; j < getMap().getHeight(); j++)
                {
                    Circle circle = new Circle(i * scale, j * scale, 1, Color.BLACK);
                    group.getChildren().addAll(circle);
                }
            }



            Arrays.stream(getMap().getRoads()).forEach(road -> {
                //System.out.println(" updating roads");
                Line line = new Line(road.x1 * scale, road.x2 * scale, road.x3 * scale, road.x4 * scale);
                line.setStroke(Color.BLACK);
                group.getChildren().addAll(line);
            });

            /*Arrays.stream(getMap().getIntersections()).forEach(inter -> {
                //System.out.println(" updating roads");
                Circle circle = new Circle((float)inter.getKey() * scale, (float)inter.getValue() * scale, 8, Color.PURPLE);
                group.getChildren().addAll(circle);
            });*/

            Arrays.stream(getMap().getCities()).forEach(city -> {
                //System.out.println(" " + city.getX() + " " + city.getY());
                Circle circle = new Circle(city.getX() * scale, city.getY() * scale, 4, Color.RED);
                group.getChildren().addAll(circle);
            });

            pane.getChildren().clear();
            pane.getChildren().add(group);
            //System.out.println(pane.getChildren().toString());


            //System.out.println("Updated");
        });
    }


    @FXML
    public void generateClick(ActionEvent e) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GenerateDialog.fxml"));
        loader.setRoot(new BorderPane());
        Parent parent = (Parent)loader.load();
        GenerateDialogController controller = (GenerateDialogController)loader.getController();
        Scene scene = new Scene(parent, 400, 200);

        Stage stage = new Stage();
        stage.setScene(scene);

        controller.getGenerateButton().setOnAction(ae -> {
            try {
                generateCities(Integer.parseInt(controller.getWidth().getText()),
                        Integer.parseInt(controller.getHeight().getText()),
                        Integer.parseInt(controller.getCities().getText()));
                stage.close();
                controller.setError("");
            } catch (NumberFormatException nfe) {
                controller.setError("Enter a numeric value to all fields.");
            }
        });

        stage.show();

    }

    public void generateCities(int width, int height, int cities) {
        Map map = new Map(width, height);
        map.generateCities(cities);

        setMap(map);
    }

    int numberOfGAPopulation = 10;      //number of potential solutions in each cycle of GA (could be given a dialogue box for easier customizability?)
    public void initializeGAClick(ActionEvent e) throws Exception {

        Map map = new Map(getMap().getWidth(),getMap().getHeight());

        map.setCities(getMap().getCities());

        map.initializeGA(numberOfGAPopulation);

        setMap(map);

        generationNumber = 0;

        System.out.println("initialized GA");
    }

    int generationNumber = 0;

    public void nextGenerationOfGAClick(ActionEvent e) throws Exception {

        Map map = new Map(getMap().getWidth(),getMap().getHeight());

        map.setCities(getMap().getCities());

        map.setRoadNetworkPopulation(getMap().getRoadNetworkPopulation());

        map.updateGA();

        setMap(map);

        generationNumber++;
        System.out.println("now at generation: " + generationNumber);
    }

}
