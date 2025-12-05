package com.java.repository;

import com.java.entity.RevenueViewDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RevenueViewRepository {
    private final JdbcTemplate jdbcTemplate;

    public RevenueViewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RevenueViewDTO getRevenueViewFromDB() {
        String sql = "SELECT total_revenue FROM revenue_view LIMIT 1";
        try {
            Double totalRevenue = jdbcTemplate.queryForObject(sql, Double.class);
            RevenueViewDTO revenueDTO = new RevenueViewDTO();
            revenueDTO.setTotal_revenue(totalRevenue);
            return revenueDTO;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
