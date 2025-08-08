# 使用指南

本指南展示如何通过 UI 与代码使用项目的主要功能。

## 登录/注册与会话

- 首次运行时进入登录界面；若已登录则进入主界面。
- 注册：输入用户名、密码、邮箱及验证码（演示用固定 `0614`）。
- 登录成功后创建 `session.txt`，主界面全屏显示。
- 退出登录：主界面左侧“退出登录”。

## 新建订单

1. 主界面左侧点击“新建订单”。
2. 填写航班、客户、发/收件人、货物信息。
3. 点击“新建订单”保存为 `orders/order_<时间戳>.txt`。

要点：
- 货物的体积自动绑定（长×宽×高），计费重量取重量与体积重的较大值（体积重=体积/6000）。
- 支付方式可选：支付宝、微信、现金。

## 订单管理

- 在“订单管理”页查看本地 `orders/` 目录中的订单摘要。
- 搜索：按订单号、日期、发/收件人模糊过滤。
- 双击行：打开订单详情对话框。
- 批量选择：表头复选框全选/全不选。
- 修改订单：选择一条订单点击“修改订单”，将订单载入“新建订单”页进行编辑，保存后覆盖原文件。
- 删除订单：选中后点击“删除订单”。

## 订单支付

- 在“订单管理”页选择一个或多个“未支付”的订单，点击“支付”。
- 弹出支付对话框，按原订单支付方式展示信息与二维码（现金无二维码）。
- 点击“完成支付”后将把对应订单文件中的“支付状态”更新为“已支付”。

资源路径约定：
- 二维码图片文件位于 `resources/qrcodes/<支付方式>.png`（例如 `支付宝支付.png`、`微信支付.png`）。

## 代码示例

### 计算单件货物运费
```java
import model.Cargo;
import model.Agent;

Cargo cargo = new Cargo("C001", "电脑", "Normal", 1, 50, 60, 40, 10);
Agent agent = new Agent("Normal");
double freight = agent.calculateSingleCargoFreight(cargo);
```

### 读取订单摘要（与 UI 保持一致的解析逻辑）
```java
// 参考 controller.OrderManagementController#parseOrderFile 与 #loadFullOrderFromFile
```

### 自定义费率策略
```java
import model.RateCalculator;

class NightRateCalculator implements RateCalculator {
  public double calculateRate(double weight) { return 20.0; }
}
```

注入自定义策略：
```java
import model.Agent;
Agent agent = new Agent(new NightRateCalculator());
```

### 添加新支付方式的步骤
- 新建类实现 `model.Payment`，在 `processPayment` 中实现逻辑（或留空演示）。
- 在 UI 流程：
  - `NewOrder.fxml` 的支付下拉框中加入新选项
  - 支付二维码文件命名与选项一致（若需要二维码）
  - `PaymentDialogController#setOrder` 根据新选项匹配图片文件

## 常见问题

- 启动后空白或无样式：请确认 JavaFX SDK 配置与 `style.css` 路径正确。
- 无法加载二维码：检查 `resources/qrcodes` 路径与文件名是否与支付方式一致。
- 打开订单失败：确认订单文件编码为 UTF-8 且格式与示例一致（包含各分区与表头）。