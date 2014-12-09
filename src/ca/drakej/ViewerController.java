package ca.drakej;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Created by drakej on 2014-11-25.
 */
public class ViewerController implements Initializable {
    int numberOfGAPopulation = 10;      //number of potential solutions in each cycle of GA (could be given a dialogue box for easier customizability?)
    int generationNumber = 0;
    private Stage stage;
    private ObjectProperty<Map> map;
    private ObjectProperty<GeneticAlgorithm> ga;
    @FXML
    private Pane pane;

    public ObjectProperty<Map> mapProperty() {
        return map;
    }
    public ObjectProperty<GeneticAlgorithm> gaProperty() {
        return ga;
    }

    public Map getMap() {
        return map.get();
    }

    public void setMap(Map map) {
        this.map.setValue(map);
    }

    public GeneticAlgorithm getGa() {
        return ga.get();
    }

    public void setGa(GeneticAlgorithm ga) {
        this.ga.setValue(ga);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.map = new SimpleObjectProperty<Map>(this, "map", new Map(0, 0));
        this.ga = new SimpleObjectProperty<>(this, "ga", new GeneticAlgorithm(getMap(), 10));

        mapProperty().addListener((obs, oldValue, newValue) -> {
            updateDrawing();
        });

        gaProperty().addListener((obs, oldValue, newValue) -> {
            updateDrawing();
        });

    }

    @FXML
    public void generateClick(ActionEvent e) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GenerateDialog.fxml"));
        loader.setRoot(new BorderPane());
        Parent parent = (Parent) loader.load();
        GenerateDialogController controller = (GenerateDialogController) loader.getController();
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

    public void updateDrawing() {
        final Group group = new Group();
        group.setAutoSizeChildren(false);

        final double paneMin = Math.min(pane.getWidth(), pane.getHeight());
        final double ourMin = Math.min(getMap().getWidth(), getMap().getHeight());
        final double scale = paneMin / ourMin;

        RoadNetworkSolution rns = getGa().getBest();

        if (rns != null)
            rns.getRoadNetwork().forEach(road -> {
                //System.out.println(" updating roads");
                Line line = new Line(road.getStart().getX() * scale, road.getStart().getY() * scale,
                        road.getEnd().getX() * scale, road.getEnd().getY() * scale);
                line.setStroke(Color.BLACK);
                group.getChildren().addAll(line);
            });

        for (int i = 0; i < getMap().getCities().size(); i++) {
            Text text = new Text(getMap().getCities().get(i).getX() * scale, getMap().getCities().get(i).getY() * scale, String.valueOf(i));
            group.getChildren().addAll(text);
        }
//        getMap().getCities().forEach(city -> {
//            //System.out.println(" " + city.getX() + " " + city.getY());
//            Circle circle = new Circle(city.getX() * scale, city.getY() * scale, 4, Color.RED);
//            group.getChildren().addAll(circle);
//        });

        pane.getChildren().clear();
        pane.getChildren().add(group);
        //System.out.println(pane.getChildren().toString());


        System.out.println("Updated");
    }

    public void initializeGAClick(ActionEvent e) throws Exception {

        Map map = new Map(getMap().getWidth(), getMap().getHeight());

        map.setCities(getMap().getCities());

        setGa(new GeneticAlgorithm(map, numberOfGAPopulation));

        setMap(map);

        generationNumber = 0;

        updateDrawing();
        System.out.println("initialized GA");
    }

    public void nextGenerationOfGAClick(ActionEvent e) throws Exception {

        Map map = new Map(getMap().getWidth(), getMap().getHeight());

        map.setCities(getMap().getCities());

        map.setRoadNetworkPopulation(getMap().getRoadNetworkPopulation());

        map.updateGA();

        setMap(map);
        updateDrawing();

        generationNumber++;
        System.out.println("now at generation: " + generationNumber);
    }

}
