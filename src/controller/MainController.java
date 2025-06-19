package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        // Show a default view when the main scene loads
        handleHome();
    }

    @FXML
    private void handleHome() {
        System.out.println("首页按钮被点击");
        contentPane.getChildren().clear();
        Label welcomeLabel = new Label("欢迎使用航空货物管理系统");
        welcomeLabel.setStyle("-fx-font-size: 24px;");
        contentPane.getChildren().add(welcomeLabel);
    }

    @FXML
    private void handleCargo() {
        System.out.println("货物管理按钮被点击");
        contentPane.getChildren().clear();
        Label cargoLabel = new Label("货物管理功能待实现");
        cargoLabel.setStyle("-fx-font-size: 24px;");
        contentPane.getChildren().add(cargoLabel);
    }

    @FXML
    private void handleRoute() {
        System.out.println("航线管理按钮被点击");
        contentPane.getChildren().clear();
        Label routeLabel = new Label("航线管理功能待实现");
        routeLabel.setStyle("-fx-font-size: 24px;");
        contentPane.getChildren().add(routeLabel);
    }

    @FXML
    private void handleTransport() {
        System.out.println("运输计划按钮被点击");
        contentPane.getChildren().clear();
        Label transportLabel = new Label("运输计划功能待实现");
        transportLabel.setStyle("-fx-font-size: 24px;");
        contentPane.getChildren().add(transportLabel);
    }

    @FXML
    private void handleUser() {
        System.out.println("用户管理按钮被点击");
        contentPane.getChildren().clear();
        Label userLabel = new Label("用户管理功能待实现");
        userLabel.setStyle("-fx-font-size: 24px;");
        contentPane.getChildren().add(userLabel);
    }
}

