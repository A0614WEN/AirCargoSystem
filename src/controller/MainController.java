package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import util.SessionUtil;
import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentPane;

    public void initialize() {
        // Show a default view when the main scene loads
        handleOrderManagement();
    }



    @FXML
    private void handleCargo() {
        try {
            System.out.println("新建订单按钮被点击");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NewOrder.fxml"));
            Parent newOrderRoot = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(newOrderRoot);
        } catch (IOException e) {
            e.printStackTrace();
            contentPane.getChildren().clear();
            Label errorLabel = new Label("错误：无法加载新建订单页面。");
            errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");
            contentPane.getChildren().add(errorLabel);
        }
    }

    @FXML
    private void handleOrderManagement() {
        try {
            System.out.println("订单管理按钮被点击");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderManagement.fxml"));
            Parent orderManagementRoot = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(orderManagementRoot);
        } catch (IOException e) {
            e.printStackTrace();
            contentPane.getChildren().clear();
            Label errorLabel = new Label("错误：无法加载订单管理页面。");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red;");
            contentPane.getChildren().add(errorLabel);
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionUtil.clearSession();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) contentPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(false);
        stage.setMaximized(true);

        LoginController controller = loader.getController();
        controller.setPrimaryStage(stage);
    }

}

