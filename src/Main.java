import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.SessionUtil;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String fxmlFile;
        boolean loggedIn = SessionUtil.isLoggedIn();

        if (loggedIn) {
            fxmlFile = "/view/Main.fxml";
        } else {
            fxmlFile = "/view/Login.fxml";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        if (!loggedIn) {
            LoginController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
        }

        primaryStage.setTitle("航空货物管理系统");
        primaryStage.setScene(new Scene(root));

        if (loggedIn) {
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreen(true);
        } else {
            primaryStage.setMaximized(true);
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}