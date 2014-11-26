package ca.drakej;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Created by drakej on 2014-11-25.
 */
public class GenerateDialogController {
    @FXML
    private TextField width;

    @FXML
    private TextField height;

    @FXML
    private TextField cities;

    @FXML
    private Label error;

    public void setError(String e) {
        if (e.equals("")) {
            error.setText("");
            error.setVisible(false);
            return;
        }

        error.setText(e);
        error.setVisible(true);
    }

    public Button getGenerateButton() {
        return generateButton;
    }

    public void setGenerateButton(Button generateButton) {
        this.generateButton = generateButton;
    }

    public TextField getCities() {
        return cities;
    }

    public void setCities(TextField cities) {
        this.cities = cities;
    }

    public TextField getHeight() {

        return height;
    }

    public void setHeight(TextField height) {
        this.height = height;
    }

    public TextField getWidth() {
        return width;
    }

    public void setWidth(TextField width) {
        this.width = width;
    }

    @FXML
    private Button generateButton;

    @FXML
    public void handleGenerate(ActionEvent ae) {

    }


}
