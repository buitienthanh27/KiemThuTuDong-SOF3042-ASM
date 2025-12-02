package com.java.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProductViewDTO {
    private int cartId;
    private String customerId;
    private int productId;
    private int quantity;
    private String name;
    private double price;
    private double totalPrice;
    private String image;
    private double discount;

    @Override
    public String toString() {
        return "CartProductViewDTO{" +
                "cartId=" + cartId +
                ", customerId='" + customerId + '\'' +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", totalPrice=" + totalPrice +
                ", image='" + image + '\'' +
                ", discount=" + discount +
                '}';
    }
}
