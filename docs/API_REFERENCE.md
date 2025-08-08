# API 参考

本文档涵盖项目中所有公开类、接口与其公共方法，并提供简要示例。

- 运行入口：`Main`
- 模型层：`model` 包
- 工具类：`util` 包
- 控制器：`controller` 包（FXML 事件处理的公开方法）
- 视图：见 `docs/VIEWS.md`

---

## Main

- `class Main extends javafx.application.Application`
  - `void start(Stage primaryStage)`：应用启动，根据 `SessionUtil.isLoggedIn()` 决定加载 `Main.fxml` 或 `Login.fxml`。
  - `static void main(String[] args)`：应用入口。

示例：
```java
Application.launch(Main.class, args);
```

---

## util 包

### SessionUtil
- `static void createSession(String username)`：创建会话文件 `session.txt`。
- `static void clearSession()`：清除会话。
- `static boolean isLoggedIn()`：判断是否存在会话。
- `static String getLoggedInUser()`：返回当前登录用户名（可能为 null）。

示例：
```java
if (!SessionUtil.isLoggedIn()) {
    SessionUtil.createSession("admin");
}
```

### UserUtil
- `static void saveUser(AdministratorUser user)`：将用户追加保存到 `AdministratorUser.txt`。
- `static AdministratorUser findUser(String username, String password)`：按用户名与密码查找用户，找不到返回 `null`。

---

## model 包

### Address
- 字段：`name`
- 方法：`getName()`, `setName(String)`

### Person (abstract)
- 字段：`name`, `phoneNumber`, `address`
- 方法：`getName()/setName`, `getPhoneNumber()/setPhoneNumber`, `getAddress()/setAddress`

### Sender extends Person / Recipient extends Person
- 默认构造与带参构造

### Customer (abstract) extends Person
- 字段：`ID`
- 抽象方法：`double getDiscount()`, `void setDiscount(double)`

### Individual extends Customer
- 默认折扣：0.9

### Corporate extends Customer
- 默认折扣：0.8

### AdministratorUser
- 字段：`username`, `password`, `email`
- 方法：`getUsername()/setUsername`, `getPassword()/setPassword`, `getEmail()/setEmail`, `toString()`（用于文件持久化，格式：`username,password,email`）

### Flight
- 字段：`flightNumber`, `departureCity`, `arrivalCity`, `flightDate`, `maxWeight`
- Getter/Setter 全部公开

### Cargo
- 属性（JavaFX Property）：`id`, `name`, `type`, `quantity`, `width`, `length`, `height`, `weight`, `volume (只读)`
- 方法：标准 Getter/Setter，`double getChargeableWeight()` 返回计费重量（与体积重比较取大，体积重=体积/6000）

示例：
```java
Cargo c = new Cargo("C001","电脑","Normal",1, 50, 60, 40, 10);
double billable = c.getChargeableWeight();
```

### Order
- 字段：`orderNumber`, `orderDate`, `customerType`, `customer`, `sender`, `recipient`, `flight`, `cargoItems`, `paymentMethod`, `status`, `freight`, `paymentStatus`, `selected (JavaFX)`
- 方法：标准 Getter/Setter，`addCargo(Cargo)`

### OrderSummary
- JavaFX TableView 行模型
- 字段：`orderNumber`, `orderDate`, `status`, `freight`, `sender`, `recipient`, `filePath`, `selected`, `paymentStatus`
- 方法：`xxxProperty()`，`setSelected(boolean)`, `setPaymentStatus(String)`

### 支付
- `interface Payment { void processPayment(double amount); }`
- `class AlipayPayment implements Payment`
- `class WeChatPayment implements Payment`
- `class CashPayment implements Payment`

### 运价计算
- `interface RateCalculator { double calculateRate(double weight); }`
- `class NormalRateCalculator implements RateCalculator`
- `class ExpediteRateCalculator implements RateCalculator`
- `class DangerousRateCalculator implements RateCalculator`

规则（示例）：
```java
new NormalRateCalculator().calculateRate(55); // 25.0
new ExpediteRateCalculator().calculateRate(10); // 60.0
```

### Agent
- 构造：`Agent(String cargoType)` 或 `Agent(RateCalculator)`
- `double calculateTotalWeight(Order order)`：合计订单计费重量。
- `double calculateTotalFreight(Order order)`：按客户折扣与费率求合计运费。
- `double calculateSingleCargoFreight(Cargo cargo)`：单件货物运费（不含客户折扣）。
- `String getPayment(Payment payment)`：支付方式中文名。
- `void Print(Customer, Flight, Order, Sender, Recipient, Payment)`：打印订单摘要到控制台（演示用）。

示例：
```java
Agent agent = new Agent("Normal");
double freight = agent.calculateSingleCargoFreight(cargo);
```

---

## controller 包（FXML 公开交互）

> 控制器方法多由 FXML 的 `onAction` 触发。以下列出关键公开交互方法与职责，供二次开发调用或测试时使用。

### LoginController
- 依赖 FXML 字段：`usernameField`, `passwordField`, `errorLabel`, 注册相关输入框
- `void setPrimaryStage(Stage)`：设置主舞台，登录成功后切换场景用。
- `@FXML void handleLogin()`：验证用户，创建会话，跳转主界面。
- `@FXML void handleRegister()`：注册新管理员用户。
- `@FXML void showRegister()` / `@FXML void showLogin()`：在登录/注册界面间切换。

### MainController
- `@FXML void initialize()`：默认加载订单管理视图。
- `@FXML void handleCargo()`：加载 `NewOrder.fxml` 到主内容区。
- `@FXML void handleOrderManagement()`：加载 `OrderManagement.fxml`。
- `@FXML void handleLogisticsManagement()`：加载 `LogisticsManagement.fxml`。
- `@FXML void handleLogout()`：清理会话并返回登录页。

### NewOrderController
- `@FXML void initialize()`：初始化表格与下拉选项。
- `void loadOrderFromFile(String filePath)`：将订单文件载入表单以编辑。
- `@FXML void handleAddCargo()` / `handleEditCargo()` / `handleDeleteCargo()`：维护货物列表。
- `@FXML void handleCreateOrder()`：创建或更新订单文件。

示例：
```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NewOrder.fxml"));
Parent root = loader.load();
NewOrderController c = loader.getController();
c.loadOrderFromFile(path);
```

### OrderManagementController
- `@FXML void initialize()`：加载订单、初始化筛选/排序与行双击详情。
- `@FXML void handleModifyOrder()`：在主内容区打开选中订单的编辑界面。
- `@FXML void handleDeleteOrder()`：删除选中文件并刷新列表。
- `@FXML void handlePayment()`：对选中订单逐个执行支付流程（弹出支付对话框）。
- `@FXML void refreshTable()`：重新加载订单文件。

扩展点：`parseOrderFile(...)`、`loadFullOrderFromFile(...)` 可作为读取 .txt 订单格式的参考实现。

### OrderDetailController
- `void setOrder(Order order)`：将完整订单渲染到详情界面。

### AddCargoDialogController
- `void processResult()`：从对话框输入构造 `Cargo`。
- `void setCargo(Cargo cargo)`：向对话框回填数据用于编辑。
- `void updateCargo(Cargo cargo)`：把修改应用回原对象。

### PaymentDialogController
- `void setOrder(OrderSummary order, String paymentMethod)`：设置支付信息并展示二维码。

---

## 常见模式与注意事项
- JavaFX Property 与标准 Getter/Setter 并存；在 TableView/Binding 场景优先使用 Property 接口。
- 订单文件为纯文本分区格式，解析时注意跳过表头与分隔线；新增字段建议按 `键: 值` 追加。
- 支付二维码图片通过文件系统路径加载，适配 IDE 运行。若要打包，请将资源复制到类路径并用 `getResource` 加载。