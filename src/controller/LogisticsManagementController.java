package controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class LogisticsManagementController {

    @FXML
    private Pane mapPane;

    @FXML
    private Label statusLabel;

    private static final Point2D SHANGHAI_POS = new Point2D(120, 210);
    private static final Point2D LA_POS = new Point2D(620, 190);

    @FXML
    public void initialize() {
        // 1. 设置地图背景
        mapPane.setBackground(new Background(new BackgroundFill(Color.web("#B0D9E8"), null, null)));

        // 2. 绘制地图、城市和路径
        drawContinents();
        Path flightPath = drawCitiesAndPath();

        // 3. 创建飞机图标
        Group airplane = createAirplane();

        // 4. 将飞机添加到面板
        mapPane.getChildren().add(airplane);

        // 5. 创建路径动画
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(8));
        pathTransition.setPath(flightPath);
        pathTransition.setNode(airplane);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setCycleCount(1);
        pathTransition.setInterpolator(Interpolator.EASE_BOTH);

        // 6. 创建状态标签的淡入淡出动画
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), statusLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // 7. 编排整个动画序列
        SequentialTransition sequentialTransition = new SequentialTransition();
        sequentialTransition.getChildren().addAll(
                new PauseTransition(Duration.seconds(1)),
                createStatusUpdate("航班已从上海起飞", fadeIn),
                new PauseTransition(Duration.seconds(1)),
                pathTransition,
                createStatusUpdate("航班已抵达洛杉矶", fadeIn)
        );

        sequentialTransition.play();
    }

    private void drawContinents() {
        // 简化的亚洲大陆
        Polygon asia = new Polygon(
                50, 150, 150, 180, 160, 250, 80, 300, 30, 220
        );
        asia.setFill(Color.web("#A9D0A9"));
        asia.setStroke(Color.web("#7E9E7E"));

        // 简化的北美大陆
        Polygon northAmerica = new Polygon(
                580, 120, 680, 150, 690, 250, 600, 280, 570, 200
        );
        northAmerica.setFill(Color.web("#A9D0A9"));
        northAmerica.setStroke(Color.web("#7E9E7E"));

        mapPane.getChildren().addAll(asia, northAmerica);
    }

    private Path drawCitiesAndPath() {
        // 绘制城市点和标签
        drawCity("上海", SHANGHAI_POS);
        drawCity("洛杉矶", LA_POS);

        // 绘制飞行路径
        Path path = new Path();
        path.getElements().add(new MoveTo(SHANGHAI_POS.getX(), SHANGHAI_POS.getY()));
        path.getElements().add(new QuadCurveTo(375, 50, LA_POS.getX(), LA_POS.getY()));
        path.setStroke(Color.web("#FF6347", 0.8));
        path.setStrokeWidth(2.5);
        path.getStrokeDashArray().addAll(10d, 5d);
        
        mapPane.getChildren().add(path);
        
        return path;
    }

    private void drawCity(String name, Point2D position) {
        Circle cityDot = new Circle(position.getX(), position.getY(), 5, Color.web("#DB4437"));
        cityDot.setStroke(Color.WHITE);
        cityDot.setStrokeWidth(1.5);

        Label cityName = new Label(name);
        cityName.setFont(Font.font("System", FontWeight.BOLD, 14));
        cityName.setTextFill(Color.web("#333333"));
        cityName.setLayoutX(position.getX() - 20);
        cityName.setLayoutY(position.getY() + 10);

        mapPane.getChildren().addAll(cityDot, cityName);
    }

    private Group createAirplane() {
        Group airplane = new Group();
        Polygon body = new Polygon(0.0, 0.0, -20.0, 5.0, -20.0, -5.0);
        body.setFill(Color.web("#4285F4"));
        body.setStroke(Color.web("#3367D6"));
        body.setStrokeWidth(1);

        Polygon wings = new Polygon(-10.0, 0.0, -15.0, 15.0, -20.0, 15.0, -15.0, 0.0, -20.0, -15.0, -15.0, -15.0);
        wings.setFill(Color.web("#C5D7F2"));
        wings.setStroke(Color.web("#A8C4E9"));

        airplane.getChildren().addAll(wings, body);
        airplane.setScaleX(1.4);
        airplane.setScaleY(1.4);
        return airplane;
    }

    private Transition createStatusUpdate(String text, FadeTransition fadeIn) {
        return new Transition() {
            { setCycleDuration(Duration.ONE); }
            protected void interpolate(double frac) {
                statusLabel.setText(text);
                fadeIn.playFromStart();
            }
        };
    }
}
