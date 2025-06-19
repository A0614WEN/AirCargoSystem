package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.AdministratorUser;
import util.UserUtil;
import util.SessionUtil;

import java.io.IOException;

public class LoginController {

    private Stage primaryStage;

    // Login fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // Register fields
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regEmailField;
    @FXML private TextField verificationCodeField;
    @FXML private Label registerErrorLabel;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            AdministratorUser user = UserUtil.findUser(username, password);
            if (user != null) {
                System.out.println("Login Successful!");
                SessionUtil.createSession(username);
                // Proceed to the main app scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Main.fxml"));
                Parent mainRoot = loader.load();


                Scene scene = new Scene(mainRoot);
                primaryStage.setScene(scene);
                primaryStage.setFullScreenExitHint("");
                primaryStage.setFullScreen(true);
            } else {
                errorLabel.setText("用户名或密码错误!");
                errorLabel.getStyleClass().setAll("error-label");
            }
        } catch (IOException e) {
            errorLabel.setText("登录时发生错误");
            errorLabel.getStyleClass().setAll("error-label");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        String username = regUsernameField.getText();
        String password = regPasswordField.getText();
        String email = regEmailField.getText();
        String verificationCode = verificationCodeField.getText();

        if (!"0614".equals(verificationCode)) {
            registerErrorLabel.setText("验证码错误!");
            registerErrorLabel.getStyleClass().setAll("error-label");
            return;
        }

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            registerErrorLabel.setText("所有字段均为必填项!");
            registerErrorLabel.getStyleClass().setAll("error-label");
            return;
        }

        AdministratorUser newUser = new AdministratorUser(username, password, email);
        try {
            UserUtil.saveUser(newUser);
            registerErrorLabel.setText("注册成功，请返回登录");
            registerErrorLabel.getStyleClass().setAll("success-label");
            // Clear fields after successful registration
            regUsernameField.clear();
            regPasswordField.clear();
            regEmailField.clear();
            verificationCodeField.clear();
        } catch (IOException e) {
            registerErrorLabel.setText("注册时发生错误");
            registerErrorLabel.getStyleClass().setAll("error-label");
            e.printStackTrace();
        }
    }

    @FXML
    private void showRegister() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Register.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.getScene().setRoot(root);
    }

    @FXML
    private void showLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.getScene().setRoot(root);
    }

}
