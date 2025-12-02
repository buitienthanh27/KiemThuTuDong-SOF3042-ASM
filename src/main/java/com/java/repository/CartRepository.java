package com.java.repository;

import com.java.entity.Cart;
import com.java.entity.CartProductViewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    @Query("SELECT c FROM Cart c WHERE c.customerId = :customerId")
    List<Cart> getCartsByCustomerId(@Param("customerId") String customerId);

    @Modifying
    @Transactional
    @Query("UPDATE Cart c SET c.quantity = :#{#cartProductViewDTO.quantity}, c.price = :#{#cartProductViewDTO.price} WHERE c.customerId = :#{#cartProductViewDTO.customerId} AND c.productId = :#{#cartProductViewDTO.productId}")
    void updateCart(@Param("cartProductViewDTO") CartProductViewDTO cartProductViewDTO);

    @Transactional
    void deleteByCustomerIdAndProductId(String customerId, Integer productId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM carts WHERE customerId = :customerId", nativeQuery = true)
    void emptyCart(@Param("customerId") String customerId);

    @Procedure(procedureName = "UpdateOrInsertIntoCart")
    void updateOrInsertIntoCart(String customer, int product);
}
