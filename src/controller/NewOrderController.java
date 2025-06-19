package controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import model.Cargo;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class NewOrderController {

    //<editor-fold desc="FXML Fields">
    // Flight Info
    @FXML private TextField flightNumberField;
    @FXML private TextField departureAirportField;
    @FXML private TextField arrivalAirportField;
    @FXML private DatePicker flightDatePicker;
    @FXML private TextField maxWeightField;

    // Customer Info
    @FXML private ComboBox<String> customerTypeComboBox;
    @FXML private TextField customerIdField;
    @FXML private TextField customerNameField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerAddressField;

    // Cargo Info
    @FXML private TableView<Cargo> cargoTableView;
    @FXML private TableColumn<Cargo, String> cargoNameCol;
    @FXML private TableColumn<Cargo, String> cargoTypeCol;
    @FXML private TableColumn<Cargo, Number> cargoWeightCol;
    @FXML private TableColumn<Cargo, Number> cargoVolumeCol;
    @FXML private TableColumn<Cargo, Number> cargoQuantityCol;

    // Sender Info
    @FXML private TextField senderNameField;
    @FXML private TextField senderPhoneField;
    @FXML private TextField senderAddressField;

    // Recipient Info
    @FXML private TextField recipientNameField;
    @FXML private TextField recipientPhoneField;
    @FXML private TextField recipientAddressField;

    // Order Actions
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private DatePicker orderDatePicker;
    @FXML private Button createOrderButton;
    //</editor-fold>

    private final ObservableList<Cargo> cargoList = FXCollections.observableArrayList();
    private String orderFilePath = null; // 用于存储正在编辑的订单文件的路径

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        orderDatePicker.setValue(LocalDate.now());
        flightDatePicker.setValue(LocalDate.now());
    }

    private void setupTableColumns() {
        cargoNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        cargoTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        cargoWeightCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getWeight()));
        cargoVolumeCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getVolume()));
        cargoQuantityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()));
        cargoTableView.setItems(cargoList);
    }

    private void setupComboBoxes() {
        customerTypeComboBox.setItems(FXCollections.observableArrayList("个人客户", "企业客户"));
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("支付宝支付", "微信支付", "现金支付"));
    }

    /**
     * 从文件加载订单数据到UI界面进行修改
     * @param filePath 订单文件的路径
     */
    public void loadOrderFromFile(String filePath) {
        this.orderFilePath = filePath;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            String section = "";
            cargoList.clear();

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("---") && line.trim().endsWith("---")) {
                    section = line.trim();
                    if (section.contains("货物清单")) {
                        reader.readLine(); // 跳过表头
                        reader.readLine(); // 跳过分隔符
                    }
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                switch (section) {
                    case "--- 航班信息 ---":
                        if (line.contains("航班号:")) flightNumberField.setText(parseValue(line));
                        else if (line.contains("起飞机场:")) departureAirportField.setText(parseValue(line));
                        else if (line.contains("抵达机场:")) arrivalAirportField.setText(parseValue(line));
                        else if (line.contains("航班日期:")) flightDatePicker.setValue(LocalDate.parse(parseValue(line), dateFormatter));
                        else if (line.contains("最大载重(kg):")) maxWeightField.setText(parseValue(line));
                        break;
                    case "--- 客户信息 ---":
                        if (line.contains("客户类型:")) customerTypeComboBox.setValue(parseValue(line));
                        else if (line.contains("客户ID:")) customerIdField.setText(parseValue(line));
                        else if (line.contains("姓名:")) customerNameField.setText(parseValue(line));
                        else if (line.contains("电话:")) customerPhoneField.setText(parseValue(line));
                        else if (line.contains("地址:")) customerAddressField.setText(parseValue(line));
                        break;
                    case "--- 发件人信息 ---":
                        if (line.contains("姓名:")) senderNameField.setText(parseValue(line));
                        else if (line.contains("电话:")) senderPhoneField.setText(parseValue(line));
                        else if (line.contains("地址:")) senderAddressField.setText(parseValue(line));
                        break;
                    case "--- 收件人信息 ---":
                        if (line.contains("姓名:")) recipientNameField.setText(parseValue(line));
                        else if (line.contains("电话:")) recipientPhoneField.setText(parseValue(line));
                        else if (line.contains("地址:")) recipientAddressField.setText(parseValue(line));
                        break;
                    case "--- 货物清单 ---":
                        String[] parts = line.trim().split("\\s{2,}");
                        if (parts.length >= 8) {
                            // 顺序: ID, 名称, 类型, 数量, 重量, 长度, 宽度, 高度
                            cargoList.add(new Cargo(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), Double.parseDouble(parts[6]), Double.parseDouble(parts[5]), Double.parseDouble(parts[7]), Double.parseDouble(parts[4])));
                        }
                        break;
                    case "--- 订单信息 ---":
                        if (line.contains("支付方式:")) paymentMethodComboBox.setValue(parseValue(line));
                        else if (line.contains("下单日期:")) orderDatePicker.setValue(LocalDate.parse(parseValue(line), dateFormatter));
                        break;
                }
            }
            cargoTableView.setItems(cargoList);
            createOrderButton.setText("完成修改");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "加载失败", "读取或解析订单文件失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCargo() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/AddCargoDialog.fxml"));
            DialogPane dialogPane = loader.load();

            AddCargoDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("添加货物");
            dialog.initOwner(cargoTableView.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                controller.processResult();
                Cargo newCargo = controller.getNewCargo();
                if (newCargo != null) {
                    cargoList.add(newCargo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "加载错误", "无法打开添加货物对话框。\n" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "程序错误", "发生意外错误：\n" + e.getMessage());
        }
    }

    @FXML
    private void handleEditCargo() {
        // TODO: Implement cargo editing logic
        showAlert(Alert.AlertType.INFORMATION, "功能未实现", "编辑货物功能正在开发中。");
    }

    @FXML
    private void handleDeleteCargo() {
        Cargo selectedCargo = cargoTableView.getSelectionModel().getSelectedItem();
        if (selectedCargo != null) {
            cargoList.remove(selectedCargo);
        } else {
            showAlert(Alert.AlertType.WARNING, "未选择", "请在表格中选择要删除的货物。");
        }
    }

    @FXML
    private void handleCreateOrder() {
        if (!isInputValid()) {
            return;
        }

        String finalFilePath;
        String orderNumber;
        File ordersDir = new File("orders");

        if (orderFilePath != null) {
            finalFilePath = orderFilePath;
            orderNumber = finalFilePath.substring(finalFilePath.indexOf('_') + 1, finalFilePath.lastIndexOf('.'));
        } else {
            if (!ordersDir.exists()) {
                ordersDir.mkdirs();
            }
            orderNumber = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(java.time.LocalDateTime.now());
            finalFilePath = new File(ordersDir, "order_" + orderNumber + ".txt").getPath();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(finalFilePath), StandardCharsets.UTF_8))) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            writer.write("--- 航班信息 ---\n");
            writer.write("航班号: " + flightNumberField.getText() + "\n");
            writer.write("起飞机场: " + departureAirportField.getText() + "\n");
            writer.write("抵达机场: " + arrivalAirportField.getText() + "\n");
            writer.write("航班日期: " + flightDatePicker.getValue().format(dateFormatter) + "\n");
            writer.write("最大载重(kg): " + maxWeightField.getText() + "\n\n");

            writer.write("--- 客户信息 ---\n");
            writer.write("客户类型: " + customerTypeComboBox.getValue() + "\n");
            writer.write("客户ID: " + customerIdField.getText() + "\n");
            writer.write("姓名: " + customerNameField.getText() + "\n");
            writer.write("电话: " + customerPhoneField.getText() + "\n");
            writer.write("地址: " + customerAddressField.getText() + "\n\n");

            writer.write("--- 发件人信息 ---\n");
            writer.write("姓名: " + senderNameField.getText() + "\n");
            writer.write("电话: " + senderPhoneField.getText() + "\n");
            writer.write("地址: " + senderAddressField.getText() + "\n\n");

            writer.write("--- 收件人信息 ---\n");
            writer.write("姓名: " + recipientNameField.getText() + "\n");
            writer.write("电话: " + recipientPhoneField.getText() + "\n");
            writer.write("地址: " + recipientAddressField.getText() + "\n\n");

            writer.write("--- 货物清单 ---\n");
            writer.write(String.format("%-20s %-15s %-10s %-8s %-10s %-10s %-10s %-10s %-12s\n",
                    "ID", "名称", "类型", "数量", "重量(kg)", "长(cm)", "宽(cm)", "高(cm)", "体积(cm³)"));
            
            char[] separator = new char[115];
            java.util.Arrays.fill(separator, '-');
            writer.write(new String(separator) + "\n");

            for (Cargo cargo : cargoList) {
                writer.write(String.format("%-20s %-15s %-10s %-8d %-10.2f %-10.2f %-10.2f %-10.2f %-12.2f\n",
                        cargo.getId(), cargo.getName(), cargo.getType(), cargo.getQuantity(),
                        cargo.getWeight(), cargo.getLength(), cargo.getWidth(), cargo.getHeight(), cargo.getVolume()));
            }
            writer.write("\n");

            writer.write("--- 订单信息 ---\n");
            writer.write("订单号: " + orderNumber + "\n");
            writer.write("下单日期: " + orderDatePicker.getValue().format(dateFormatter) + "\n");
            writer.write("支付方式: " + paymentMethodComboBox.getValue() + "\n");

            showAlert(Alert.AlertType.INFORMATION, "操作成功", (orderFilePath == null ? "订单创建成功！" : "订单修改成功！") + "\n订单号: " + orderNumber);
            clearFields();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "保存失败", "创建或修改订单文件失败: " + e.getMessage());
        }
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (flightNumberField.getText() == null || flightNumberField.getText().trim().isEmpty()) errorMessage.append("航班号不能为空。\n");
        if (customerNameField.getText() == null || customerNameField.getText().trim().isEmpty()) errorMessage.append("客户姓名不能为空。\n");
        if (senderNameField.getText() == null || senderNameField.getText().trim().isEmpty()) errorMessage.append("发件人姓名不能为空。\n");
        if (recipientNameField.getText() == null || recipientNameField.getText().trim().isEmpty()) errorMessage.append("收件人姓名不能为空。\n");
        if (customerTypeComboBox.getValue() == null) errorMessage.append("请选择客户类型。\n");
        if (paymentMethodComboBox.getValue() == null) errorMessage.append("请选择支付方式。\n");
        if (orderDatePicker.getValue() == null) errorMessage.append("请选择订单日期。\n");
        if (flightDatePicker.getValue() == null) errorMessage.append("请选择航班日期。\n");
        if (cargoList.isEmpty()) errorMessage.append("货物列表不能为空。\n");

        // Validate numeric fields
        try {
            if (maxWeightField.getText() != null && !maxWeightField.getText().trim().isEmpty()) {
                Double.parseDouble(maxWeightField.getText());
            }
        } catch (NumberFormatException e) {
            errorMessage.append("最大载重必须是有效的数字。\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(Alert.AlertType.WARNING, "信息不完整或格式错误", errorMessage.toString());
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        flightNumberField.clear();
        departureAirportField.clear();
        arrivalAirportField.clear();
        flightDatePicker.setValue(LocalDate.now());
        maxWeightField.clear();
        customerTypeComboBox.getSelectionModel().clearSelection();
        customerTypeComboBox.setPromptText("客户类型");
        customerIdField.clear();
        customerNameField.clear();
        customerPhoneField.clear();
        customerAddressField.clear();
        senderNameField.clear();
        senderPhoneField.clear();
        senderAddressField.clear();
        recipientNameField.clear();
        recipientPhoneField.clear();
        recipientAddressField.clear();
        paymentMethodComboBox.getSelectionModel().clearSelection();
        paymentMethodComboBox.setPromptText("支付方式");
        orderDatePicker.setValue(LocalDate.now());
        cargoList.clear();

        // 重置修改状态
        orderFilePath = null;
        createOrderButton.setText("创建订单");
    }

    /**
     * 从冒号分隔的行中解析值
     * @param line 待解析的行
     * @return 冒号后的值
     */
    private String parseValue(String line) {
        String[] parts = line.split(":", 2);
        if (parts.length > 1) {
            return parts[1].trim();
        }
        return "";
    }
}
