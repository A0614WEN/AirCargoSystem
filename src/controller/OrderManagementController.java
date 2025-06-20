package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderManagementController {

    @FXML private TextField searchField;
    @FXML private TableView<OrderSummary> orderTableView;
    @FXML private TableColumn<OrderSummary, Boolean> selectColumn;
    @FXML private TableColumn<OrderSummary, String> orderNumberColumn;
    @FXML private TableColumn<OrderSummary, String> orderDateColumn;
    @FXML private TableColumn<OrderSummary, String> statusColumn;
    @FXML private TableColumn<OrderSummary, String> paymentStatusColumn;
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
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        freightColumn.setCellValueFactory(new PropertyValueFactory<>("freight"));
        senderColumn.setCellValueFactory(new PropertyValueFactory<>("sender"));
        recipientColumn.setCellValueFactory(new PropertyValueFactory<>("recipient"));

        // Add a custom comparator for the freight column to sort it as a number
        freightColumn.setComparator(Comparator.comparingDouble(s -> Double.parseDouble(s.replace(",", ""))));
    }

    private void loadOrderFiles() {
        File ordersDir = new File("orders");
        // Ensure the directory exists before trying to list files
        if (!ordersDir.exists()) {
            ordersDir.mkdirs();
        }

        File[] files = ordersDir.listFiles((d, name) -> name.startsWith("order_") && name.endsWith(".txt"));
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
        String paymentStatus = "未支付"; // 默认设为未支付
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
                    case "支付状态":
                        paymentStatus = value;
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

        return new OrderSummary(orderNumber, orderDateStr, status, String.format("%.2f", totalFreight), sender, recipient, filePath, paymentStatus);
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
                } else if (order.getOrderDate().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getFreight().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getSender().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getRecipient().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        // Wrap the FilteredList in a SortedList.
        SortedList<OrderSummary> sortedData = new SortedList<>(filteredData);

        // Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(orderTableView.comparatorProperty());

        // Set the items in the TableView to the SortedList.
        orderTableView.setItems(sortedData);

        // 添加双击事件监听器
        orderTableView.setRowFactory(tv -> {
            TableRow<OrderSummary> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    OrderSummary clickedOrderSummary = row.getItem();
                    showOrderDetail(clickedOrderSummary);
                }
            });
            return row;
        });
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

    @FXML
    private void handlePayment() {
        List<OrderSummary> selectedOrders = orderTableView.getItems().stream()
                .filter(OrderSummary::isSelected)
                .collect(Collectors.toList());

        if (selectedOrders.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "操作失败", "请至少选择一个订单进行支付。");
            return;
        }

        List<OrderSummary> unpaidOrders = selectedOrders.stream()
                .filter(o -> !"已支付".equals(o.getPaymentStatus()))
                .collect(Collectors.toList());

        if (unpaidOrders.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "提示", "所有选中的订单都已支付。");
            return;
        }

        boolean allPaymentsSuccessful = true;
        for (OrderSummary summary : unpaidOrders) {
            try {
                Order fullOrder = loadFullOrderFromFile(summary.getFilePath());
                if (fullOrder == null) {
                    showAlert(Alert.AlertType.ERROR, "错误", "无法加载订单 " + summary.getOrderNumber() + " 的完整信息。");
                    allPaymentsSuccessful = false;
                    continue;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PaymentDialog.fxml"));
                DialogPane dialogPane = loader.load();

                PaymentDialogController controller = loader.getController();
                controller.setOrder(summary, fullOrder.getPaymentMethod());

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("订单支付");

                Optional<ButtonType> result = dialog.showAndWait();

                if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    updatePaymentStatusInFile(summary.getFilePath(), "已支付");
                    summary.setPaymentStatus("已支付");
                } else {
                    allPaymentsSuccessful = false; // 用户取消了其中一个支付
                }

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "错误", "加载支付对话框时出错: " + e.getMessage());
                allPaymentsSuccessful = false;
            }
        }

        orderTableView.refresh();
        if (allPaymentsSuccessful) {
            showAlert(Alert.AlertType.INFORMATION, "成功", "所有选定订单的支付流程已完成。");
        } else {
            showAlert(Alert.AlertType.WARNING, "注意", "部分订单未完成支付或被取消。");
        }
    }

    private void showOrderDetail(OrderSummary orderSummary) {
        try {
            Order fullOrder = loadFullOrderFromFile(orderSummary.getFilePath());
            if (fullOrder == null) {
                showAlert(Alert.AlertType.ERROR, "错误", "无法加载订单 " + orderSummary.getOrderNumber() + " 的完整信息。");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderDetail.fxml"));
            Parent root = loader.load();

            OrderDetailController controller = loader.getController();
            controller.setOrder(fullOrder);

            Stage stage = new Stage();
            stage.setTitle("订单详情 - " + orderSummary.getOrderNumber());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "错误", "加载订单详情页面时出错: " + e.getMessage());
        }
    }

    private void updatePaymentStatusInFile(String filePath, String newStatus) {
        try {
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            boolean statusUpdated = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("支付状态:")) {
                    lines.set(i, "支付状态: " + newStatus);
                    statusUpdated = true;
                    break;
                }
            }
            if (!statusUpdated) {
                lines.add(1, "支付状态: " + newStatus);
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "错误", "更新文件失败: " + e.getMessage());
        }
    }

    private Order loadFullOrderFromFile(String filePath) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            Order order = new Order();
            List<String> lines = reader.lines().collect(Collectors.toList());
            String section = "";

            for (String line : lines) {
                if (line.startsWith("---")) {
                    section = line.trim();
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                if (section.equals("--- 货物清单 ---")) {
                    try {
                        String[] cargoParts = line.trim().split("\\s{2,}");
                        if (cargoParts.length >= 8) {
                            // 尝试解析数量，如果失败，则说明是表头或无效行
                            int quantity = Integer.parseInt(cargoParts[3]);
                            double weight = Double.parseDouble(cargoParts[4]);
                            double length = Double.parseDouble(cargoParts[5]);
                            double width = Double.parseDouble(cargoParts[6]);
                            double height = Double.parseDouble(cargoParts[7]);

                            order.getCargoItems().add(new Cargo(cargoParts[0], cargoParts[1], cargoParts[2],
                                    quantity, width, length, height, weight));
                        }
                    } catch (NumberFormatException e) {
                        // 捕获到异常，说明此行不是有效的货物数据（很可能是表头），直接跳过
                        System.out.println("Skipping header or invalid line in cargo section: " + line);
                    }
                } else {
                    String[] parts = line.split(":", 2);
                    if (parts.length < 2) continue;
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    switch (key) {
                        case "订单号": order.setOrderNumber(value); break;
                        case "下单日期": order.setOrderDate(LocalDate.parse(value)); break;
                        case "客户类型": order.setCustomerType(value); break;
                        case "支付方式": order.setPaymentMethod(value); break;
                        case "支付状态": order.setPaymentStatus(value); break;
                        case "姓名":
                            if (section.contains("发件人")) order.getSender().setName(value);
                            else if (section.contains("收件人")) order.getRecipient().setName(value);
                            break;
                        case "电话":
                            if (section.contains("发件人")) order.getSender().setPhoneNumber(value);
                            else if (section.contains("收件人")) order.getRecipient().setPhoneNumber(value);
                            break;
                        case "地址":
                            if (section.contains("发件人")) order.getSender().setAddress(new Address(value));
                            else if (section.contains("收件人")) order.getRecipient().setAddress(new Address(value));
                            break;
                    }
                }
            }
            return order;
        } catch (IOException e) {
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
