package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.Cargo;

public class AddCargoDialogController {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField quantityField;
    @FXML private TextField weightField;
    @FXML private TextField widthField;
    @FXML private TextField lengthField;
    @FXML private TextField heightField;

    private Cargo newCargo;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("Normal", "Expedite", "Dangerous"));
        typeComboBox.getSelectionModel().selectFirst();
    }

    public Cargo getNewCargo() {
        return newCargo;
    }

    public void processResult() {
        String id = idField.getText();
        String name = nameField.getText();
        String type = typeComboBox.getValue();
        int quantity = Integer.parseInt(quantityField.getText());
        double weight = Double.parseDouble(weightField.getText());
        double width = Double.parseDouble(widthField.getText());
        double length = Double.parseDouble(lengthField.getText());
        double height = Double.parseDouble(heightField.getText());

        newCargo = new Cargo(id, name, type, quantity, width, length, height, weight);
    }

    public void setCargo(Cargo cargo) {
        idField.setText(cargo.getId());
        nameField.setText(cargo.getName());
        typeComboBox.setValue(cargo.getType());
        quantityField.setText(String.valueOf(cargo.getQuantity()));
        weightField.setText(String.valueOf(cargo.getWeight()));
        widthField.setText(String.valueOf(cargo.getWidth()));
        lengthField.setText(String.valueOf(cargo.getLength()));
        heightField.setText(String.valueOf(cargo.getHeight()));
    }

    public void updateCargo(Cargo cargo) {
        cargo.setId(idField.getText());
        cargo.setName(nameField.getText());
        cargo.setType(typeComboBox.getValue());
        cargo.setQuantity(Integer.parseInt(quantityField.getText()));
        cargo.setWeight(Double.parseDouble(weightField.getText()));
        cargo.setWidth(Double.parseDouble(widthField.getText()));
        cargo.setLength(Double.parseDouble(lengthField.getText()));
        cargo.setHeight(Double.parseDouble(heightField.getText()));
    }
}
