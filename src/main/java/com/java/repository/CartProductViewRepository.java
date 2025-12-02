package com.java.repository;

import com.java.entity.CartProductViewDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
@Repository
public class CartProductViewRepository {
    private final JdbcTemplate jdbcTemplate;

    public CartProductViewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CartProductViewDTO> getCartProductView() {
        String sql = "SELECT * FROM cart_product_view";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CartProductViewDTO cartProductView = new CartProductViewDTO();
            cartProductView.setCartId(rs.getInt("cartId"));
            cartProductView.setCustomerId(rs.getString("customerId"));
            cartProductView.setProductId(rs.getInt("productId"));
            cartProductView.setQuantity(rs.getInt("quantity"));
            cartProductView.setName(rs.getString("name"));
            cartProductView.setPrice(rs.getDouble("price"));
            cartProductView.setTotalPrice(rs.getDouble("totalPrice"));
            cartProductView.setImage(rs.getString("image"));
            cartProductView.setDiscount(rs.getInt("discount"));
            return cartProductView;
        });
    }

    public Collection<CartProductViewDTO> getCartProductViewByCustomerId(String customerId) {
        String sql = "SELECT * FROM cart_product_view WHERE customerId = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CartProductViewDTO cartProductView = new CartProductViewDTO();
            cartProductView.setCartId(rs.getInt("cartId"));
            cartProductView.setCustomerId(rs.getString("customerId"));
            cartProductView.setProductId(rs.getInt("productId"));
            cartProductView.setQuantity(rs.getInt("quantity"));
            cartProductView.setName(rs.getString("name"));
            cartProductView.setPrice(rs.getDouble("price"));
            cartProductView.setTotalPrice(rs.getDouble("totalPrice"));
            cartProductView.setImage(rs.getString("image"));
            cartProductView.setDiscount(rs.getInt("discount"));
            return cartProductView;
        }, customerId);
    }
}
