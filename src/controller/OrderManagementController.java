package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    private final ObservableList<OrderSummary> orderData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadOrderFiles();
        setupSearchFilter();
        setupSelectAllCheckBox();

        // 为表格行添加双击事件监听器
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

        // 为运费列添加自定义比较器，使其能按数字排序
        freightColumn.setComparator(Comparator.comparingDouble(s -> Double.parseDouble(s.replace(",", ""))));
    }

    private void loadOrderFiles() {
        File ordersDir = new File("orders");
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
        String orderNumber = null, orderDateStr = null, sender = null, recipient = null;
        String paymentStatus = "未支付";
        List<Cargo> cargoList = new ArrayList<>();
        String section = "";
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("--- ") && line.trim().endsWith(" ---")) {
                section = line.trim();
                if (section.contains("货物清单")) {
                    reader.readLine(); // 跳过表头
                    reader.readLine(); // 跳过分隔符
                }
                continue;
            }

            if (line.trim().isEmpty()) continue;

            if (section.equals("--- 货物清单 ---")) {
                String[] cargoParts = line.trim().split("\\s{2,}");
                if (cargoParts.length >= 8) {
                    try {
                        cargoList.add(new Cargo(cargoParts[0], cargoParts[1], cargoParts[2],
                                Integer.parseInt(cargoParts[3]), Double.parseDouble(cargoParts[6]),
                                Double.parseDouble(cargoParts[5]), Double.parseDouble(cargoParts[7]),
                                Double.parseDouble(cargoParts[4])));
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
                        if (section.equals("--- 发件人信息 ---")) sender = value;
                        else if (section.equals("--- 收件人信息 ---")) recipient = value;
                        break;
                    case "订单号": orderNumber = value; break;
                    case "下单日期": orderDateStr = value; break;
                    case "支付状态": paymentStatus = value; break;
                }
            }
        }

        if (orderDateStr == null || orderDateStr.isEmpty() || orderNumber == null || orderNumber.isEmpty()) {
            System.err.println("警告: 订单文件信息不完整，已跳过: " + filePath);
            return null;
        }

        LocalDate orderDate = LocalDate.parse(orderDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        return new OrderSummary(orderNumber, orderDateStr, determineStatus(orderDate),
                String.format("%.2f", calculateTotalFreight(cargoList)), sender, recipient, filePath, paymentStatus);
    }

    private double calculateTotalFreight(List<Cargo> cargoItems) {
        Agent agent = new Agent("Normal");
        return cargoItems.stream().mapToDouble(agent::calculateSingleCargoFreight).sum();
    }

    private String determineStatus(LocalDate orderDate) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(orderDate)) return "还未运输";
        return today.isEqual(orderDate) ? "在运输中" : "运输完毕";
    }

    private void setupSearchFilter() {
        FilteredList<OrderSummary> filteredData = new FilteredList<>(orderData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filteredData.setPredicate(order -> {
            if (newVal == null || newVal.isEmpty()) return true;
            String filter = newVal.toLowerCase();
            return order.getOrderNumber().toLowerCase().contains(filter)
                    || order.getOrderDate().toLowerCase().contains(filter)
                    || order.getStatus().toLowerCase().contains(filter)
                    || order.getFreight().toLowerCase().contains(filter)
                    || order.getSender().toLowerCase().contains(filter)
                    || order.getRecipient().toLowerCase().contains(filter);
        }));

        SortedList<OrderSummary> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(orderTableView.comparatorProperty());
        orderTableView.setItems(sortedData);
    }

    private void setupSelectAllCheckBox() {
        selectAllCheckBox.setOnAction(e -> {
            boolean isSelected = selectAllCheckBox.isSelected();
            orderTableView.getItems().forEach(item -> item.setSelected(isSelected));
        });
    }

    @FXML
    private void handleModifyOrder() {
        OrderSummary selectedOrder = getSingleSelectedOrder("修改");
        if (selectedOrder == null) return;

        if ("运输完毕".equals(selectedOrder.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "修改失败", "无法修改已完成运输的订单。");
            return;
        }

        loadNewOrderView(selectedOrder.getFilePath());
    }

    @FXML
    private void handleDeleteOrder() {
        List<OrderSummary> selectedOrders = getSelectedOrders();
        if (selectedOrders.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "操作失败", "请至少选择一个订单进行删除。");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "您确定要删除选中的 " + selectedOrders.size() + " 个订单吗？此操作不可恢复。", ButtonType.YES, ButtonType.NO);
        if (confirmation.showAndWait().filter(b -> b == ButtonType.YES).isPresent()) {
            selectedOrders.forEach(order -> {
                try {
                    Files.deleteIfExists(Paths.get(order.getFilePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "删除失败", "删除订单文件 " + order.getOrderNumber() + " 时出错。");
                }
            });
            refreshTable();
        }
    }

    @FXML
    private void handlePayment() {
        List<OrderSummary> selectedOrders = getSelectedOrders();
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

        boolean allSuccess = unpaidOrders.stream().allMatch(this::processPaymentForOrder);

        refreshTable();
        if (allSuccess) {
            showAlert(Alert.AlertType.INFORMATION, "成功", "所有选定订单的支付流程已完成。");
        } else {
            showAlert(Alert.AlertType.WARNING, "注意", "部分订单未完成支付或被取消。");
        }
    }

    private boolean processPaymentForOrder(OrderSummary summary) {
        try {
            Order fullOrder = loadFullOrderFromFile(summary.getFilePath());
            if (fullOrder == null) return false;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PaymentDialog.fxml"));
            DialogPane dialogPane = loader.load();

            PaymentDialogController controller = loader.getController();
            controller.setOrder(summary, fullOrder.getPaymentMethod());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("订单支付");

            if (dialog.showAndWait().filter(b -> b.getButtonData() == ButtonBar.ButtonData.OK_DONE).isPresent()) {
                updatePaymentStatusInFile(summary.getFilePath(), "已支付");
                summary.setPaymentStatus("已支付");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "错误", "加载支付对话框时出错: " + e.getMessage());
        }
        return false;
    }

    private void showOrderDetail(OrderSummary orderSummary) {
        try {
            Order fullOrder = loadFullOrderFromFile(orderSummary.getFilePath());
            if (fullOrder == null) return;

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
            int statusIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("支付状态:")) {
                    statusIndex = i;
                    break;
                }
            }
            if (statusIndex != -1) {
                lines.set(statusIndex, "支付状态: " + newStatus);
            } else {
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
            Sender sender = new Sender();
            Recipient recipient = new Recipient();
            Address senderAddress = new Address();
            Address recipientAddress = new Address();
            List<Cargo> cargoList = new ArrayList<>();
            String section = "";
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("--- ") && line.trim().endsWith(" ---")) {
                    section = line.trim();
                    if (section.contains("货物清单")) {
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
                            cargoList.add(new Cargo(cargoParts[0], cargoParts[1], cargoParts[2],
                                    Integer.parseInt(cargoParts[3]), Double.parseDouble(cargoParts[6]),
                                    Double.parseDouble(cargoParts[5]), Double.parseDouble(cargoParts[7]),
                                    Double.parseDouble(cargoParts[4])));
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping malformed cargo line in " + filePath + ": " + line);
                        }
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
                            if (section.equals("--- 发件人信息 ---")) sender.setName(value);
                            else if (section.equals("--- 收件人信息 ---")) recipient.setName(value);
                            break;
                        case "电话":
                            if (section.equals("--- 发件人信息 ---")) sender.setPhoneNumber(value);
                            else if (section.equals("--- 收件人信息 ---")) recipient.setPhoneNumber(value);
                            break;
                        case "地址":
                            if (section.equals("--- 发件人信息 ---")) senderAddress.setName(value);
                            else if (section.equals("--- 收件人信息 ---")) recipientAddress.setName(value);
                            break;
                    }
                }
            }

            sender.setAddress(senderAddress);
            recipient.setAddress(recipientAddress);
            order.setSender(sender);
            order.setRecipient(recipient);
            order.setCargoItems(cargoList);
            order.setFreight(calculateTotalFreight(cargoList));
            if (order.getOrderDate() != null) {
                order.setStatus(determineStatus(order.getOrderDate()));
            }
            return order;
        } catch (IOException | DateTimeParseException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "文件读取错误", "无法加载或解析订单文件: " + filePath);
            return null;
        }
    }

    private void loadNewOrderView(String filePath) {
        try {
            StackPane contentPane = (StackPane) orderTableView.getScene().lookup("#contentPane");
            if (contentPane == null) {
                showAlert(Alert.AlertType.ERROR, "程序错误", "无法找到主内容面板。");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NewOrder.fxml"));
            Parent newOrderRoot = loader.load();
            NewOrderController newOrderController = loader.getController();
            if (filePath != null) {
                newOrderController.loadOrderFromFile(filePath);
            }
            contentPane.getChildren().clear();
            contentPane.getChildren().add(newOrderRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "加载失败", "加载订单页面时出错: " + e.getMessage());
        }
    }

    private List<OrderSummary> getSelectedOrders() {
        return orderTableView.getItems().stream()
                .filter(OrderSummary::isSelected)
                .collect(Collectors.toList());
    }

    private OrderSummary getSingleSelectedOrder(String action) {
        List<OrderSummary> selectedOrders = getSelectedOrders();
        if (selectedOrders.size() != 1) {
            showAlert(Alert.AlertType.WARNING, "操作失败", "请选择一个订单进行" + action + "。" );
            return null;
        }
        return selectedOrders.get(0);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void refreshTable() {
        orderData.clear();
        loadOrderFiles();
    }
}
