package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import model.Cargo;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class NewOrderController {

    // Flight Information
    @FXML private TextField flightNumberField;
    @FXML private DatePicker flightDatePicker;
    @FXML private TextField departureAirportField;
    @FXML private TextField arrivalAirportField;
    @FXML private TextField maxPayloadField;

    // Customer Information
    @FXML private ComboBox<String> customerTypeComboBox;
    @FXML private TextField customerIdField;
    @FXML private TextField customerNameField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerAddressField;

    // Cargo Information
    @FXML private TableView<Cargo> cargoTableView;
    @FXML private TableColumn<Cargo, String> cargoNameColumn;
    @FXML private TableColumn<Cargo, String> cargoTypeColumn;
    @FXML private TableColumn<Cargo, Double> cargoWeightColumn;
    @FXML private TableColumn<Cargo, Double> cargoVolumeColumn;
    @FXML private TableColumn<Cargo, Integer> cargoQuantityColumn;

    // Sender Information
    @FXML private TextField senderNameField;
    @FXML private TextField senderPhoneField;
    @FXML private TextField senderAddressField;

    // Recipient Information
    @FXML private TextField recipientNameField;
    @FXML private TextField recipientPhoneField;
    @FXML private TextField recipientAddressField;

    // Order Actions
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private DatePicker orderDatePicker;

    @FXML
    public void initialize() {
        // Initialize Customer Type ComboBox
        customerTypeComboBox.setItems(FXCollections.observableArrayList("Individual", "Corporate"));

        // Initialize Payment Method ComboBox
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("支付宝支付", "微信支付", "现金支付"));

        // Set default dates to today
        flightDatePicker.setValue(LocalDate.now());
        orderDatePicker.setValue(LocalDate.now());

        // Configure Cargo Table
        cargoNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cargoTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        cargoWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        cargoVolumeColumn.setCellValueFactory(new PropertyValueFactory<>("volume"));
        cargoQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    @FXML
    private void handleAddCargo() {
        try {
            // Load the fxml file for the dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/AddCargoDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Create the dialog.
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("添加新货物");

            // Get the controller.
            AddCargoDialogController controller = loader.getController();

            // Show the dialog and wait for a result.
            Optional<ButtonType> result = dialog.showAndWait();
            result.ifPresent(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    try {
                        controller.processResult();
                        Cargo newCargo = controller.getNewCargo();
                        if (newCargo != null) {
                            cargoTableView.getItems().add(newCargo);
                        }
                    } catch (NumberFormatException e) {
                        // Handle case where user enters invalid number format
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("输入错误");
                        alert.setHeaderText("无效的数字格式");
                        alert.setContentText("请确保数量、重量和尺寸字段中只包含有效的数字。\n" + e.getMessage());
                        alert.showAndWait();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifyCargo() {
        Cargo selectedCargo = cargoTableView.getSelectionModel().getSelectedItem();
        if (selectedCargo != null) {
            try {
                // Load the fxml file for the dialog.
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/view/AddCargoDialog.fxml"));
                DialogPane dialogPane = loader.load();

                // Get the controller and set the cargo to be modified.
                AddCargoDialogController controller = loader.getController();
                controller.setCargo(selectedCargo);

                // Create the dialog.
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("修改货物信息");

                // Show the dialog and wait for a result.
                Optional<ButtonType> result = dialog.showAndWait();
                result.ifPresent(buttonType -> {
                    if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                        controller.updateCargo(selectedCargo);
                        cargoTableView.refresh();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Nothing selected.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("没有选中任何内容");
            alert.setHeaderText("没有选中货物");
            alert.setContentText("请在表格中选择要修改的货物。");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDeleteCargo() {
        Cargo selectedCargo = cargoTableView.getSelectionModel().getSelectedItem();
        if (selectedCargo != null) {
            cargoTableView.getItems().remove(selectedCargo);
        } else {
            // Nothing selected.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("没有选中任何内容");
            alert.setHeaderText("没有选中货物");
            alert.setContentText("请在表格中选择要删除的货物。");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCreateOrder() {
        ObservableList<Cargo> cargoItems = cargoTableView.getItems();
        if (cargoItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("没有货物");
            alert.setHeaderText("无法创建订单");
            alert.setContentText("请至少添加一件货物后再创建订单。");
            alert.showAndWait();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("================ 订单详情 ================\n");
        sb.append("创建时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        // Flight Information
        sb.append("--- 航班信息 ---\n");
        sb.append("航班号: ").append(flightNumberField.getText()).append("\n");
        sb.append("起飞机场: ").append(departureAirportField.getText()).append("\n");
        sb.append("到达机场: ").append(arrivalAirportField.getText()).append("\n");
        sb.append("航班日期: ").append(flightDatePicker.getValue()).append("\n");
        sb.append("最大载重: ").append(maxPayloadField.getText()).append(" kg\n\n");

        // Customer Information
        sb.append("--- 客户信息 ---\n");
        sb.append("客户类型: ").append(customerTypeComboBox.getValue()).append("\n");
        sb.append("客户ID: ").append(customerIdField.getText()).append("\n");
        sb.append("客户姓名: ").append(customerNameField.getText()).append("\n");
        sb.append("联系电话: ").append(customerPhoneField.getText()).append("\n");
        sb.append("客户地址: ").append(customerAddressField.getText()).append("\n\n");

        // Sender Information
        sb.append("--- 发件人信息 ---\n");
        sb.append("姓名: ").append(senderNameField.getText()).append("\n");
        sb.append("电话: ").append(senderPhoneField.getText()).append("\n");
        sb.append("地址: ").append(senderAddressField.getText()).append("\n\n");

        // Recipient Information
        sb.append("--- 收件人信息 ---\n");
        sb.append("姓名: ").append(recipientNameField.getText()).append("\n");
        sb.append("电话: ").append(recipientPhoneField.getText()).append("\n");
        sb.append("地址: ").append(recipientAddressField.getText()).append("\n\n");

        // Cargo Information
        sb.append("--- 货物清单 ---\n");
        sb.append(String.format("%-15s %-15s %-10s %-10s %-10s %-10s\n", "ID", "名称", "类型", "数量", "重量(kg)", "体积(cm³)"));
        sb.append("------------------------------------------------------------------------\n");
        for (Cargo cargo : cargoItems) {
            sb.append(String.format("%-15s %-15s %-10s %-10d %-10.2f %-10.2f\n",
                    cargo.getId(), cargo.getName(), cargo.getType(), cargo.getQuantity(), cargo.getWeight(), cargo.getVolume()));
        }
        sb.append("\n");

        // Order Information
        sb.append("--- 订单信息 ---\n");
        sb.append("支付方式: ").append(paymentMethodComboBox.getValue()).append("\n");
        sb.append("下单日期: ").append(orderDatePicker.getValue()).append("\n");
        sb.append("============================================\n");

        String fileName = "order_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            writer.write(sb.toString());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("订单创建成功");
            alert.setHeaderText(null);
            alert.setContentText("订单信息已成功保存到文件: " + fileName);
            alert.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("保存失败");
            alert.setHeaderText("无法保存订单文件");
            alert.setContentText("错误信息: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }    
}
