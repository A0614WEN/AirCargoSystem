package model;

public class Agent {
    private RateCalculator rateCalculator;
    private double totalRate;

    public Agent(String cargoType) {
        if (cargoType.equals("Normal")) {
            rateCalculator = new NormalRateCalculator();
        }
        else if (cargoType.equals("Expedite")) {
            rateCalculator = new ExpediteRateCalculator();
        }
        else if (cargoType.equals("Dangerous")) {
            rateCalculator = new DangerousRateCalculator();
        }
    }

    public Agent(RateCalculator rateCalculator) {
        this.rateCalculator = rateCalculator;
    }

    public double calculateTotalWeight(Order order) {
        double totalWeight = 0.0;
        for (Cargo cargo : order.getCargoItems()) {
            totalWeight += cargo.getChargeableWeight();
        }
        return totalWeight;
    }

    public double calculateTotalFreight(Order order) {
        double totalFreight = 0.0;
        for (Cargo cargo : order.getCargoItems()) {
            double weight = cargo.getChargeableWeight();
            double rate = rateCalculator.calculateRate(weight);
            totalFreight += weight * rate * order.getCustomer().getDiscount();
        }
        return totalFreight;
    }

    public void Print(Customer customer, Flight flight, Order order, Sender sender, Recipient recipient, Payment paymentMethod) {
        double totalWeight = this.calculateTotalWeight(order);

        // 检查航班载重量
        if (totalWeight > flight.getMaxWeight()) {
            System.out.println("The flight with flight number:" + flight.getFlightNumber() +
                    " has exceeded its load capacity and cannot carry the order.");
            return;
        }

        // 生成并输出报告
        System.out.println("客户：" + customer.getName() + "(" + customer.getPhoneNumber() + ")订单信息如下：");
        System.out.println("-----------------------------------------");
        System.out.println("航班号：" + flight.getFlightNumber());
        System.out.println("订单号：" + order.getOrderNumber());
        System.out.println("订单日期：" + order.getOrderDate());
        System.out.println("发件人姓名：" + sender.getName());
        System.out.println("发件人电话：" + sender.getPhoneNumber());
        System.out.println("发件人地址：" + sender.getAddress().getName());
        System.out.println("收件人姓名：" + recipient.getName());
        System.out.println("收件人电话：" + recipient.getPhoneNumber());
        System.out.println("收件人地址：" + recipient.getAddress().getName());
        System.out.println("订单总重量(kg)：" + String.format("%.1f", totalWeight));
        System.out.println(this.getPayment(paymentMethod) + "金额：" + String.format("%.1f", this.calculateTotalFreight(order)));

        // 输出货物明细
        System.out.println("\n货物明细如下：");
        System.out.println("-----------------------------------------");
        System.out.println("明细编号\t货物名称\t计费重量\t计费费率\t应交运费");
        int index = 1;
        for (Cargo cargo : order.getCargoItems()) {
            double chargeableWeight = cargo.getChargeableWeight();
            double rate = this.rateCalculator.calculateRate(chargeableWeight);
            double freight = chargeableWeight * rate;

            System.out.println(index + "\t" + cargo.getName() + "\t" +
                    String.format("%.1f", chargeableWeight) + "\t" + String.format("%.1f", rate) +
                    "\t" + String.format("%.1f", freight));
            index++;
        }
    }

    public String getPayment(Payment payment) {
        if (payment instanceof WeChatPayment) {
            return "微信支付";
        }
        else if (payment instanceof AlipayPayment) {
            return "支付宝支付";
        }
        else if (payment instanceof CashPayment) {
            return "现金支付";
        }
        return null;
    }
} 