## 航空货物管理系统

本项目是一个使用 JavaFX 构建的桌面应用，用于管理航空货物的下单、运价计算、订单管理与支付流程。项目包含清晰的分层结构：模型层（model）、控制器层（controller）、视图层（view）以及工具类（util）。

- **主要功能**
  - 订单创建、编辑、删除与浏览
  - 货物维度/重量与计费重量计算，自动计算运费
  - 支持不同计费类型（普通、加急、危险品）
  - 登录/注册与会话管理
  - 订单支付流程（支付宝/微信/现金），二维码展示
  - 简易物流跟踪演示（动画）

- **代码结构**
  - `src/model`: 领域模型（订单、货物、航班、客户、计价策略、支付策略等）
  - `src/controller`: JavaFX 控制器（登录、主界面、订单管理、新建订单、支付、物流等）
  - `src/view`: FXML 视图与样式
  - `src/util`: 工具类（用户与会话）
  - `orders/`: 订单文本文件存储目录
  - `resources/qrcodes/`: 支付二维码图片资源

- **详细文档**
  - API 参考（所有公开类与方法，含示例）：`docs/API_REFERENCE.md`
  - 使用指南（常见任务操作与代码示例）：`docs/USAGE_GUIDE.md`
  - 视图与组件说明（FXML 与控制器映射）：`docs/VIEWS.md`

## 运行与开发

- **前置条件**
  - JDK 17+
  - JavaFX 17+（若本地未集成 JavaFX，请将 JavaFX SDK 配置到 IDE 或命令行路径）

- **在 IDE 中运行**（推荐）
  1. 以项目根目录导入（例如 IntelliJ IDEA）
  2. 配置 JavaFX 库（若 IDE 未内置）
  3. 运行 `Main` 类

- **命令行运行（示例）**
  如果你单独下载了 JavaFX SDK，将其中的 `lib/` 路径配置到 `--module-path`，并开启模块：

  ```bash
  javac --release 17 -cp src -d out $(find src -name '*.java')
  java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp out Main
  ```

- **数据与资源**
  - 登录数据存储在项目根目录的 `AdministratorUser.txt`
  - 会话文件为项目根目录的 `session.txt`
  - 订单文本文件位于 `orders/` 目录（应用内创建）
  - 支付二维码图片位于 `resources/qrcodes/` 下，文件名与支付方式一致（如 `支付宝支付.png`、`微信支付.png`）

## 许可

本项目仅用于教学与演示用途。