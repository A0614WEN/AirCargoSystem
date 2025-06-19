package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Agent;
import model.Cargo;
import model.OrderSummary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OrderManagementController {

    @FXML private TextField searchField;
    @FXML private TableView<OrderSummary> orderTableView;
    @FXML private TableColumn<OrderSummary, Boolean> selectColumn;
    @FXML private TableColumn<OrderSummary, String> orderNumberColumn;
    @FXML private TableColumn<OrderSummary, String> orderDateColumn;
    @FXML private TableColumn<OrderSummary, String> statusColumn;
    @FXML private TableColumn<OrderSummary, String> freightColumn;
    @FXML private TableColumn<OrderSummary, String> senderColumn;
    @FXML private TableColumn<OrderSummary, String> recipientColumn;
    @FXML private CheckBox selectAllCheckBox;

    private ObservableList<OrderSummary> orderData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadOrderFiles();
        setupSearchFilter();
        setupSelectAllCheckBox();
    }

    private void setupTableColumns() {
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);
        orderTableView.setEditable(true);

        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        freightColumn.setCellValueFactory(new PropertyValueFactory<>("freight"));
        senderColumn.setCellValueFactory(new PropertyValueFactory<>("sender"));
        recipientColumn.setCellValueFactory(new PropertyValueFactory<>("recipient"));
    }

    private void loadOrderFiles() {
        File dir = new File(".");
        File[] files = dir.listFiles((d, name) -> name.startsWith("order_") && name.endsWith(".txt"));
        if (files != null) {
            for (File file : files) {
                try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                    OrderSummary summary = parseOrderFile(reader, file.getPath());
                    if (summary != null) {
                        orderData.add(summary);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        orderTableView.setItems(orderData);
    }

    private OrderSummary parseOrderFile(BufferedReader reader, String filePath) throws IOException {
        List<Cargo> cargoList = new ArrayList<>();
        String content = reader.lines().collect(Collectors.joining("\n"));

        String orderNumber = extractValue(content, "订单号:\\s*(.*?)");
        String orderDateStr = extractValue(content, "下单日期:\\s*(.*?)");
        String sender = extractValue(content, "--- 发件人信息 ---\\s*姓名:\\s*(.*?)");
        String recipient = extractValue(content, "--- 收件人信息 ---\\s*姓名:\\s*(.*?)");

        if (orderDateStr == null || orderDateStr.isEmpty()) {
            System.err.println("警告: 无法从文件中解析订单日期，已跳过: " + filePath);
            return null;
        }

        if (orderNumber == null || orderNumber.isEmpty()) {
            System.err.println("警告: 无法从文件中解析订单号，已跳过: " + filePath);
            return null;
        }

        // Corrected regex to handle dimensions like "1.0*2.0*3.0"
        Pattern cargoPattern = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+([\\d.]+)\\s+([\\d.*\\*]+)");
        Matcher cargoMatcher = cargoPattern.matcher(content);
        while (cargoMatcher.find()) {
            if (cargoMatcher.group(1).equals("ID")) continue; // Skip header

            String dimensionStr = cargoMatcher.group(6);
            String[] dims = dimensionStr.split("\\*");
            double width = 0, length = 0, height = 0;
            if (dims.length == 3) {
                try {
                    width = Double.parseDouble(dims[0]);
                    length = Double.parseDouble(dims[1]);
                    height = Double.parseDouble(dims[2]);
                } catch (NumberFormatException e) {
                    System.err.println("警告: 无法解析货物尺寸，已跳过货物。文件: " + filePath);
                    continue; // Skip this cargo item
                }
            }

            cargoList.add(new Cargo(
                cargoMatcher.group(1), // id
                cargoMatcher.group(2), // name
                cargoMatcher.group(3), // type
                Integer.parseInt(cargoMatcher.group(4)), // quantity
                width,
                length,
                height,
                Double.parseDouble(cargoMatcher.group(5)) // weight
            ));
        }

        LocalDate orderDate = LocalDate.parse(orderDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        String status = determineStatus(orderDate);

        double totalFreight = calculateTotalFreight(cargoList);

        return new OrderSummary(orderNumber, orderDateStr, status, String.format("%.2f", totalFreight), sender, recipient, filePath);
    }

    private String extractValue(String content, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private double calculateTotalFreight(List<Cargo> cargoItems) {
        double totalFreight = 0;
        // This is a simplified calculation. A real implementation would need to instantiate
        // the correct RateCalculator based on cargo type and apply customer discounts.
        Agent agent = new Agent("Normal"); // Assuming normal rate for all
        for (Cargo cargo : cargoItems) {
            totalFreight += agent.calculateSingleCargoFreight(cargo);
        }
        return totalFreight;
    }

    private String determineStatus(LocalDate orderDate) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(orderDate)) {
            return "还未运输";
        } else if (today.isEqual(orderDate)) {
            return "在运输中";
        } else {
            return "运输完毕";
        }
    }

    private void setupSearchFilter() {
        FilteredList<OrderSummary> filteredData = new FilteredList<>(orderData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(order -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (order.getOrderNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getSender().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getRecipient().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        orderTableView.setItems(filteredData);
    }

    private void setupSelectAllCheckBox() {
        selectAllCheckBox.setOnAction(event -> {
            boolean newValue = selectAllCheckBox.isSelected();
            for (OrderSummary item : orderTableView.getItems()) {
                item.setSelected(newValue);
            }
        });
    }

    @FXML
    private void handleModifyOrder() {
        List<OrderSummary> selectedOrders = orderTableView.getItems().stream()
                .filter(OrderSummary::isSelected)
                .collect(Collectors.toList());

        if (selectedOrders.size() != 1) {
            showAlert(Alert.AlertType.WARNING, "操作失败", "请选择一个订单进行修改。");
            return;
        }

        OrderSummary orderToModify = selectedOrders.get(0);
        if ("运输完毕".equals(orderToModify.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "修改失败", "无法修改已完成运输的订单。");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "功能待定", "订单 " + orderToModify.getOrderNumber() + " 的修改功能正在开发中。");
    }

    @FXML
    private void handleDeleteOrder() {
        List<OrderSummary> selectedOrders = orderTableView.getItems().stream()
                .filter(OrderSummary::isSelected)
                .collect(Collectors.toList());

        if (selectedOrders.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "操作失败", "请至少选择一个订单进行删除。");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "您确定要删除选中的 " + selectedOrders.size() + " 个订单吗？此操作不可恢复。", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("确认删除");
        confirmation.setHeaderText(null);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            List<OrderSummary> successfullyDeleted = new ArrayList<>();
            for (OrderSummary order : selectedOrders) {
                try {
                    Files.deleteIfExists(Paths.get(order.getFilePath()));
                    successfullyDeleted.add(order);
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "删除失败", "删除订单文件 " + order.getOrderNumber() + " 时出错。");
                }
            }
            orderData.removeAll(successfullyDeleted);
            orderTableView.refresh();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
