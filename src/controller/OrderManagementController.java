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

import java.util.stream.Collectors;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

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
        String orderNumber = null;
        String orderDateStr = null;
        String sender = null;
        String recipient = null;
        List<Cargo> cargoList = new ArrayList<>();
        String section = "";
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("--- ") && line.trim().endsWith(" ---")) {
                section = line.trim();
                if (section.contains("货物清单")) {
                    // 跳过清单的表头和分隔符行
                    reader.readLine();
                    reader.readLine();
                }
                continue;
            }

            if (line.trim().isEmpty()) continue;

            if (section.equals("--- 货物清单 ---")) {
                String[] cargoParts = line.trim().split("\\s{2,}");
                if (cargoParts.length >= 8) {
                    try {
                        cargoList.add(new Cargo(
                            cargoParts[0], cargoParts[1], cargoParts[2],
                            Integer.parseInt(cargoParts[3]),
                            Double.parseDouble(cargoParts[6]), // width
                            Double.parseDouble(cargoParts[5]), // length
                            Double.parseDouble(cargoParts[7]), // height
                            Double.parseDouble(cargoParts[4])  // weight
                        ));
                    } catch (NumberFormatException e) {
                        System.err.println("警告: 解析货物行时数字格式错误，已跳过。文件: " + filePath + ", 行: " + line);
                    }
                }
            } else {
                String[] parts = line.split(":", 2);
                if (parts.length < 2) continue;
                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "姓名":
                        if (section.equals("--- 发件人信息 ---")) {
                            sender = value;
                        } else if (section.equals("--- 收件人信息 ---")) {
                            recipient = value;
                        }
                        break;
                    case "订单号":
                        orderNumber = value;
                        break;
                    case "下单日期":
                        orderDateStr = value;
                        break;
                }
            }
        }

        if (orderDateStr == null || orderDateStr.isEmpty()) {
            System.err.println("警告: 无法从文件中解析订单日期，已跳过: " + filePath);
            return null;
        }
        if (orderNumber == null || orderNumber.isEmpty()) {
            System.err.println("警告: 无法从文件中解析订单号，已跳过: " + filePath);
            return null;
        }

        LocalDate orderDate = LocalDate.parse(orderDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        double totalFreight = calculateTotalFreight(cargoList);
        String status = determineStatus(orderDate);

        return new OrderSummary(orderNumber, orderDateStr, status, String.format("%.2f", totalFreight), sender, recipient, filePath);
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

        try {
            // 找到主内容的StackPane
            StackPane contentPane = (StackPane) orderTableView.getScene().lookup("#contentPane");
            if (contentPane == null) {
                showAlert(Alert.AlertType.ERROR, "程序错误", "无法找到主内容面板，无法切换视图。");
                return;
            }

            // 加载新建/修改订单的FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NewOrder.fxml"));
            Parent newOrderRoot = loader.load();

            // 获取控制器并加载订单数据
            NewOrderController newOrderController = loader.getController();
            newOrderController.loadOrderFromFile(orderToModify.getFilePath());

            // 切换视图
            contentPane.getChildren().clear();
            contentPane.getChildren().add(newOrderRoot);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "加载失败", "加载订单修改页面时出错: " + e.getMessage());
        }
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
