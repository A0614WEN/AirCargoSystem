import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        primaryStage.setTitle("航空货物管理系统 - 登录");

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); 
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}