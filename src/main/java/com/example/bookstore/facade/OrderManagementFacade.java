package com.example.bookstore.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.InventoryDTO;
import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.dto.OrderDetailDTO;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderDetail;
import com.example.bookstore.service.OrderDetailService;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.service.OrderService.OrderStatistics;

/**
 * Facade for admin order management operations
 * Handles business logic for order approval, status updates, and reporting
 */
@Component
public class OrderManagementFacade {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * Get all orders with full details for admin dashboard
     */
    public List<OrderDTO> getAllOrdersForAdmin() {
        List<Order> orders = orderService.getAllOrdersWithDetails();
        return orders.stream()
                .map(this::mapToOrderDTOWithDetails)
                .collect(Collectors.toList());
    }

    /**
     * Get order by ID with full details for admin view
     */
    public OrderDTO getOrderByIdForAdmin(Integer orderId) {
        Order order = orderService.getOrderWithDetailsById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không tồn tại với ID: " + orderId);
        }
        return mapToOrderDTOWithDetails(order);
    }

    /**
     * Update order status - admin approval workflow
     */
    @Transactional
    public OrderDTO updateOrderStatus(Integer orderId, String newStatus) {
        // Validate status transition
        validateStatusTransition(orderId, newStatus);
        
        Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        
        // Additional business logic based on status
        handleStatusChange(updatedOrder, newStatus);
        
        return mapToOrderDTOWithDetails(updatedOrder);
    }

    /**
     * Get orders by status for admin filtering
     */
    public List<OrderDTO> getOrdersByStatus(String status) {
        List<Order> orders = orderService.getOrdersByStatusWithDetails(status);
        return orders.stream()
                .map(this::mapToOrderDTOWithDetails)
                .collect(Collectors.toList());
    }

    /**
     * Get orders within date range for admin reporting
     */
    public List<OrderDTO> getOrdersByDateRange(String fromDate, String toDate) {
        List<Order> orders = orderService.getOrdersByDateRange(fromDate, toDate);
        return orders.stream()
                .map(this::mapToOrderDTOWithDetails)
                .collect(Collectors.toList());
    }

    /**
     * Get order statistics for admin dashboard
     */
    public OrderStatistics getOrderStatistics() {
        return orderService.getOrderStatistics();
    }

    /**
     * Get pending orders that need admin attention
     */
    public List<OrderDTO> getPendingOrders() {
        return getOrdersByStatus("Đang xử lý");
    }

    /**
     * Bulk update order status (for multiple orders)
     */
    @Transactional
    public List<OrderDTO> bulkUpdateOrderStatus(List<Integer> orderIds, String newStatus) {
        return orderIds.stream()
                .map(orderId -> updateOrderStatus(orderId, newStatus))
                .collect(Collectors.toList());
    }

    /**
     * Validate status transition rules
     */
    private void validateStatusTransition(Integer orderId, String newStatus) {
        Order currentOrder = orderService.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        
        String currentStatus = currentOrder.getStatus();
        
        // Define allowed transitions
        if ("Đã giao".equals(currentStatus) && !"Đã giao".equals(newStatus)) {
            throw new IllegalArgumentException("Không thể thay đổi trạng thái của đơn hàng đã giao");
        }
        
        if ("Đã hủy".equals(currentStatus) && !"Đã hủy".equals(newStatus)) {
            throw new IllegalArgumentException("Không thể thay đổi trạng thái của đơn hàng đã hủy");
        }
        
        System.out.println("✅ Status transition validated: " + currentStatus + " → " + newStatus);
    }    /**
     * Handle additional business logic when status changes
     */
    private void handleStatusChange(Order order, String newStatus) {
        switch (newStatus) {
            case "Đã giao":
                System.out.println("✅ Order delivered - ID: " + order.getOrderId());
                // Create inventory transactions when order is delivered
                createInventoryTransactionsForDeliveredOrder(order);
                break;
            case "Đã hủy":
                System.out.println("❌ Order cancelled - ID: " + order.getOrderId());
                // Additional logic: Send cancellation email, etc.
                break;
            default:
                System.out.println("📝 Order status updated - ID: " + order.getOrderId() + ", Status: " + newStatus);
        }
    }

    /**
     * Create inventory transactions when order is delivered
     */
    private void createInventoryTransactionsForDeliveredOrder(Order order) {
        try {
            // Get order details with book information
            Order orderWithDetails = orderService.getOrderWithDetailsById(order.getOrderId());
            if (orderWithDetails == null || orderWithDetails.getOrderDetails() == null) {
                throw new IllegalArgumentException("Không thể lấy chi tiết đơn hàng");
            }

            List<InventoryDTO> inventoryDTOs = new ArrayList<>();
            
            for (OrderDetail orderDetail : orderWithDetails.getOrderDetails()) {
                // Create inventory transaction DTO for "Xuất" (export)
                InventoryDTO inventoryDTO = new InventoryDTO(
                        orderDetail.getBook().getBookId(),
                        "Xuất", // Transaction type - export when delivered
                        orderDetail.getQuantity(),
                        orderDetail.getPriceAtOrder(),
                        order.getUser().getUserId()
                );
                inventoryDTOs.add(inventoryDTO);
                
                System.out.println("📦 Creating inventory transaction for delivery - Book: " + 
                                 orderDetail.getBook().getTitle() + 
                                 ", Quantity: " + orderDetail.getQuantity());
            }

            // Create inventory transactions (this will also update stock)
            if (!inventoryDTOs.isEmpty()) {
                productFacade.createInventoryTransactions(inventoryDTOs);
                System.out.println("✅ Inventory transactions created for delivered order: " + order.getOrderId());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to create inventory transactions for order " + order.getOrderId() + ": " + e.getMessage());
            throw new RuntimeException("Lỗi khi tạo giao dịch kho: " + e.getMessage(), e);
        }
    }

    /**
     * Map Order entity to OrderDTO with full details
     */
    private OrderDTO mapToOrderDTOWithDetails(Order order) {
        OrderDTO orderDTO = new OrderDTO(order);
        
        // Add order details if available
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            List<OrderDetailDTO> orderDetailDTOs = order.getOrderDetails().stream()
                    .map(OrderDetailDTO::new)
                    .collect(Collectors.toList());
            orderDTO.setOrderDetails(orderDetailDTOs);
        }
        
        return orderDTO;
    }
}
