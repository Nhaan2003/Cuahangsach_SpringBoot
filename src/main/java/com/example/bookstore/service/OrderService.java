package com.example.bookstore.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.model.Order;
import com.example.bookstore.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get order by ID
    public Optional<Order> getOrderById(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    // Get orders by user ID
    public List<Order> getOrdersByUserId(Integer userId) {
        return orderRepository.findByUserId(userId);
    }

    // Get orders by status
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    // Get orders by user ID and status
    public List<Order> getOrdersByUserIdAndStatus(Integer userId, String status) {
        return orderRepository.findByUserIdAndStatus(userId, status);
    }

    // Count orders by user
    public Long countOrdersByUserId(Integer userId) {
        return orderRepository.countByUserId(userId);
    }

    // Count orders by status
    public Long countOrdersByStatus(String status) {
        return orderRepository.countByStatus(status);
    }    // Save order (for direct entity saving)
    @Transactional
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    // Create order - placeholder for future implementation
    @Transactional
    public Order createOrder(OrderDTO orderDTO) {
        // TODO: Implement order creation logic
        throw new UnsupportedOperationException("Order creation not implemented yet");
    }

    // Update order - placeholder for future implementation
    @Transactional
    public Order updateOrder(Integer orderId, OrderDTO orderDTO) {
        // TODO: Implement order update logic
        throw new UnsupportedOperationException("Order update not implemented yet");
    }

    // Delete order - placeholder for future implementation
    @Transactional
    public void deleteOrder(Integer orderId) {
        // TODO: Implement order deletion logic
        throw new UnsupportedOperationException("Order deletion not implemented yet");
    }    public Long getTotalOrderCount() {
        return orderRepository.count();
    }
    
    // Admin order management methods
    
    /**
     * Get all orders with details for admin management
     */
    public List<Order> getAllOrdersWithDetails() {
        return orderRepository.findAllWithDetails();
    }
    
    /**
     * Get order by ID with full details (order details, user info, etc.)
     */
    public Order getOrderWithDetailsById(Integer orderId) {
        return orderRepository.findByIdWithDetails(orderId);
    }
    
    /**
     * Update order status - for admin approval/management
     */
    @Transactional
    public Order updateOrderStatus(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại với ID: " + orderId));
        
        // Validate status
        if (!isValidOrderStatus(newStatus)) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: " + newStatus);
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        System.out.println("📋 Order status updated - ID: " + orderId + 
                         ", From: " + oldStatus + " → To: " + newStatus);
        
        return updatedOrder;
    }
    
    /**
     * Get orders by status with details for admin dashboard
     */
    public List<Order> getOrdersByStatusWithDetails(String status) {
        return orderRepository.findByStatusWithDetails(status);
    }
    
    /**
     * Get orders within date range for admin reporting
     */
    public List<Order> getOrdersByDateRange(String fromDate, String toDate) {
        return orderRepository.findByDateRange(fromDate, toDate);
    }
      /**
     * Get order statistics for admin dashboard
     */
    public OrderStatistics getOrderStatistics() {
        Long totalOrders = orderRepository.count();
        Long pendingOrders = orderRepository.countByStatus("Đang xử lý");
        Long deliveredOrders = orderRepository.countByStatus("Đã giao");
        Long cancelledOrders = orderRepository.countByStatus("Đã hủy");
        
        return new OrderStatistics(totalOrders, pendingOrders, deliveredOrders, cancelledOrders);
    }
      /**
     * Validate order status values
     */
    private boolean isValidOrderStatus(String status) {
        return status.equals("Đang xử lý") || 
               status.equals("Đã giao") || 
               status.equals("Đã hủy");
    }    /**
     * Inner class for order statistics
     */
    public static class OrderStatistics {
        private final Long totalOrders;
        private final Long pendingOrders;
        private final Long deliveredOrders;
        private final Long cancelledOrders;
        
        public OrderStatistics(Long totalOrders, Long pendingOrders, Long deliveredOrders, Long cancelledOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.deliveredOrders = deliveredOrders;
            this.cancelledOrders = cancelledOrders;
        }
        
        // Getters
        public Long getTotalOrders() { return totalOrders; }
        public Long getPendingOrders() { return pendingOrders; }
        public Long getDeliveredOrders() { return deliveredOrders; }
        public Long getCancelledOrders() { return cancelledOrders; }
        
        // For backward compatibility
        public Long getCompletedOrders() { return deliveredOrders; }
    }
}
