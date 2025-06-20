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
            // 由于项目结构非标准，资源文件未被正确复制到类路径，
            // 我们回退到使用文件系统路径加载。这在IDE中可以工作，但可移植性较差。
            String projectDir = System.getProperty("user.dir");
            String qrCodePath = projectDir + "/resources/qrcodes/" + paymentMethod + ".png";
            File qrCodeFile = new File(qrCodePath);

            if (qrCodeFile.exists()) {
                try {
                    Image qrCodeImage = new Image(qrCodeFile.toURI().toString());
                    qrCodeImageView.setImage(qrCodeImage);
                    qrCodeImageView.setVisible(true);
                } catch (Exception e) {
                    qrCodeImageView.setImage(null);
                    qrCodeImageView.setVisible(false);
                    System.err.println("加载二维码图片时出错: " + e.getMessage());
                }
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
