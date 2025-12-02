package com.java.entity;

import lombok.Data;

@Data
public class BillViewDTO {
    private int orderId;
    private String customerId;
    private String fullName;
    private String phone;
    private String address;
    private String product_list;
    private String status;
    private String orderDate;
    private double total_price;

    @Override
    public String toString() {
        return "BillViewDTO{" +
                "orderId=" + orderId +
                ", customerId='" + customerId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", product_list='" + product_list + '\'' +
                ", status='" + status + '\'' +
                ", orderDate='" + orderDate + '\'' +
                ", totalPrice=" + total_price +
                '}';
    }
}

