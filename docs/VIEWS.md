# 视图与组件

本文档说明各 FXML 视图、控制器绑定关系与关键交互。

## Main.fxml
- 控制器：`controller.MainController`
- 根节点：`StackPane`
- 主要区域：
  - 左侧导航（VBox）：
    - 按钮：`订单管理` → `handleOrderManagement()`
    - 按钮：`新建订单` → `handleCargo()`
    - 按钮：`物流管理` → `handleLogisticsManagement()`
    - 按钮：`退出登录` → `handleLogout()`
  - 中部内容区：`StackPane#contentPane`，用于动态加载其它页面
- 样式：`@css/style.css`

## Login.fxml / Register.fxml
- 控制器：`controller.LoginController`
- 登录页面元素：`usernameField`, `passwordField`, `errorLabel`
- 事件：
  - 登录：`handleLogin()`
  - 前往注册：`showRegister()`
- 注册页面元素：`regUsernameField`, `regPasswordField`, `regEmailField`, `verificationCodeField`, `registerErrorLabel`
- 事件：
  - 注册：`handleRegister()`
  - 返回登录：`showLogin()`

## OrderManagement.fxml
- 控制器：`controller.OrderManagementController`
- 主要组件：
  - 搜索框：`searchField`
  - 表格：`orderTableView`，列包括选择、订单编号、日期、运输状态、支付状态、运费、发件人、收件人
  - 表尾按钮：`支付`（handlePayment）、`修改订单`（handleModifyOrder）、`删除订单`（handleDeleteOrder）
- 行为：
  - 双击表格行打开详情（`showOrderDetail`）
  - 顶部复选框全选/全不选

## NewOrder.fxml
- 控制器：`controller.NewOrderController`
- 分区：
  - 航班信息（航班号、日期、起飞/降落机场、最大载重）
  - 客户信息（客户类型、ID、姓名、电话、地址）
  - 货物信息（表格 + 添加/修改/删除按钮）
  - 发件人与收件人信息
  - 订单操作（支付方式、下单日期、创建/完成修改按钮）
- 关键事件：
  - `handleAddCargo()`/`handleEditCargo()`/`handleDeleteCargo()`
  - `handleCreateOrder()`
- 功能：
  - `loadOrderFromFile(String path)` 将订单文件载入编辑
  - 货物体积自动绑定，运费由其他模块计算

## OrderDetail.fxml
- 控制器：`controller.OrderDetailController`
- 作用：以只读方式展示完整订单详情，包括运费、运输状态与货物明细
- 接口：`setOrder(Order)`

## AddCargoDialog.fxml
- 控制器：`controller.AddCargoDialogController`
- 作用：添加或编辑单件货物
- 关键方法：
  - `processResult()`：从输入生成 `Cargo`
  - `setCargo(Cargo)` / `updateCargo(Cargo)`：编辑模式支持

## PaymentDialog.fxml
- 控制器：`controller.PaymentDialogController`
- 作用：显示订单支付信息与二维码
- 关键方法：`setOrder(OrderSummary order, String paymentMethod)`
- 资源：二维码图片来自 `resources/qrcodes/` 目录