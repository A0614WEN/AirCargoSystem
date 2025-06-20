package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import model.Cargo;
import model.Order;

import java.time.format.DateTimeFormatter;

public class OrderDetailController {

    @FXML
    private Label orderNumberLabel;
    @FXML
    private Label orderDateLabel;
    @FXML
    private Label customerTypeLabel;
    @FXML
    private Label paymentMethodLabel;
    @FXML
    private Label paymentStatusLabel;
    @FXML
    private Label freightLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label senderLabel;
    @FXML
    private Label recipientLabel;
    @FXML
    private TextArea cargoTextArea;

    public void setOrder(Order order) {
        if (order == null) {
            return;
        }

        orderNumberLabel.setText(order.getOrderNumber());
        orderDateLabel.setText(order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        customerTypeLabel.setText(order.getCustomerType());
        paymentMethodLabel.setText(order.getPaymentMethod());
        paymentStatusLabel.setText(order.getPaymentStatus());
        freightLabel.setText(String.format("%.2f", order.getFreight()));
        statusLabel.setText(order.getStatus());

        String senderInfo = String.format("%s (%s, %s)", 
            order.getSender().getName(), 
            order.getSender().getPhoneNumber(),
            order.getSender().getAddress().getName());
        senderLabel.setText(senderInfo);

        String recipientInfo = String.format("%s (%s, %s)", 
            order.getRecipient().getName(), 
            order.getRecipient().getPhoneNumber(),
            order.getRecipient().getAddress().getName());
        recipientLabel.setText(recipientInfo);

        StringBuilder cargoDetails = new StringBuilder();
        for (Cargo cargo : order.getCargoItems()) {
            cargoDetails.append(String.format(
                "编号: %s, 名称: %s, 类型: %s, 数量: %d, 重量: %.2fkg, 尺寸: %.2fx%.2fx%.2f cm\n",
                cargo.getId(), cargo.getName(), cargo.getType(),
                cargo.getQuantity(), cargo.getWeight(), cargo.getLength(),
                cargo.getWidth(), cargo.getHeight()
            ));
        }
        cargoTextArea.setText(cargoDetails.toString());
    }
}
