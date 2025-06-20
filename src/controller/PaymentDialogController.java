package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.OrderSummary;

import java.io.File;

public class PaymentDialogController {

    @FXML
    private Label paymentInfoLabel;

    @FXML
    private ImageView qrCodeImageView;

    public void setOrder(OrderSummary order, String paymentMethod) {
        String freight = order.getFreight();

        paymentInfoLabel.setText(String.format("请用 %s 支付 %s 元", paymentMethod, freight));

        if (paymentMethod != null && !"现金支付".equals(paymentMethod)) {
            // 假设二维码图片放在项目的 resources/qrcodes 目录下
            // 注意：运行时，路径可能需要调整，例如使用 getClass().getResource()
            String qrCodePath = "src/main/resources/qrcodes/" + paymentMethod + ".png";
            File qrCodeFile = new File(qrCodePath);
            if (qrCodeFile.exists()) {
                Image qrCodeImage = new Image(qrCodeFile.toURI().toString());
                qrCodeImageView.setImage(qrCodeImage);
                qrCodeImageView.setVisible(true);
            } else {
                qrCodeImageView.setImage(null);
                qrCodeImageView.setVisible(false);
                System.err.println("未找到二维码图片: " + qrCodePath);
            }
        } else {
            qrCodeImageView.setVisible(false);
        }
    }
}
