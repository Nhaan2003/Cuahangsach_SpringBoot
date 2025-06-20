package com.example.bookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.bookstore.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // Find all orders for a specific user
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserId(@Param("userId") Integer userId);
    
    // Find orders by status
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByStatus(@Param("status") String status);
    
    // Find orders by user ID and status
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId AND o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") String status);
    
    // Count orders by user
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.userId = :userId")
    Long countByUserId(@Param("userId") Integer userId);
      // Count orders by status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") String status);
    
    // Admin order management queries
    
    // Get all orders with details for admin
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.book " +
           "ORDER BY o.orderDate DESC")
    List<Order> findAllWithDetails();
    
    // Get order by ID with full details
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.book " +
           "WHERE o.orderId = :orderId")
    Order findByIdWithDetails(@Param("orderId") Integer orderId);
    
    // Get orders by status with details
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.book " +
           "WHERE o.status = :status " +
           "ORDER BY o.orderDate DESC")
    List<Order> findByStatusWithDetails(@Param("status") String status);
    
    // Get orders within date range
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.book " +
           "WHERE DATE(o.orderDate) BETWEEN :fromDate AND :toDate " +
           "ORDER BY o.orderDate DESC")
    List<Order> findByDateRange(@Param("fromDate") String fromDate, @Param("toDate") String toDate);
    
    // Get recent orders for admin dashboard
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.book " +
           "ORDER BY o.orderDate DESC")
    List<Order> findRecentOrdersWithDetails();
}
