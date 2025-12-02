package com.java.repository;

import com.java.entity.BillViewDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BillViewRepository {

    private final JdbcTemplate jdbcTemplate;

    public BillViewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BillViewDTO> getBillView() {
        String sql = "SELECT * FROM bill_view";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BillViewDTO billView = new BillViewDTO();
            billView.setOrderId(rs.getInt("order_id"));
            billView.setCustomerId(rs.getString("customer_id"));
            billView.setFullName(rs.getString("fullname"));
            billView.setPhone(rs.getString("phone"));
            billView.setAddress(rs.getString("address"));
            billView.setProduct_list(rs.getString("product_list"));
            billView.setOrderDate(rs.getString("order_date"));
            billView.setTotal_price(rs.getDouble("total_price"));
            billView.setStatus(rs.getString("status"));
            return billView;
        });
    }

    public List<BillViewDTO> getBillViewByCustomerId(String customerId) {
        String sql = "SELECT * FROM bill_view WHERE customerId = ? ORDER BY orderId DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BillViewDTO billView = new BillViewDTO();
            billView.setOrderId(rs.getInt("orderId"));
            billView.setCustomerId(rs.getString("customerId"));
            billView.setFullName(rs.getString("fullname"));
            billView.setPhone(rs.getString("phone"));
            billView.setAddress(rs.getString("address"));
            billView.setProduct_list(rs.getString("product_list"));
            billView.setOrderDate(rs.getString("orderDate"));
            billView.setTotal_price(rs.getDouble("total_price"));
            billView.setStatus(rs.getString("status"));
            return billView;
        }, customerId);
    }
}
