package com.java.service.impl;

import com.java.entity.Product;
import com.java.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryImpl {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ProductRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Product addOrUpdateQuantityProduct(Product product) {
        String sql = "CALL AddOrUpdateProduct(:description, :discount, :enteredDate, :image, :name, :price, :quantity, :categoryId, :supplierId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("description", product.getDescription())
                .addValue("discount", product.getDiscount())
                .addValue("enteredDate", product.getEnteredDate())
                .addValue("image", product.getImage())
                .addValue("name", product.getName())
                .addValue("price", product.getPrice())
                .addValue("quantity", product.getQuantity())
                .addValue("categoryId", product.getCategory().getCategoryId())
                .addValue("supplierId", product.getSupplier().getId());

        namedParameterJdbcTemplate.update(sql, params);

        return product;
    }

    // Triển khai các phương thức khác trong ProductRepository (nếu cần)
}
