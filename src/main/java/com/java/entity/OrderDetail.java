package com.java.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orderDetails")
public class OrderDetail implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer orderDetailId;
	private Double price;
	private Integer quantity;
	private String status;
	@Column(name = "total_price")
	private double totalPrice;
	@ManyToOne
	@JoinColumn(name = "orderId")
	private Order order;
	@ManyToOne
	@JoinColumn(name = "productId")
	private Product product;

	@Override
	public String toString() {
		return "OrderDetail{" +
				"orderDetailId=" + orderDetailId +
				", price=" + price +
				", quantity=" + quantity +
				", status='" + status + '\'' +
				", totalPrice=" + totalPrice +
				", orderId=" + order.getOrderId() +
				", productId=" + product.getProductId() +
				'}';
	}

}
