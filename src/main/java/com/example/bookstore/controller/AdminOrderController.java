package com.example.bookstore.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.facade.OrderManagementFacade;
import com.example.bookstore.service.OrderService.OrderStatistics;

/**
 * REST API Controller for admin order management
 * Handles order approval, status updates, and reporting for administrators
 */
@RestController
@RequestMapping("/api/admin/orders")
@Validated
public class AdminOrderController {

    @Autowired
    private OrderManagementFacade orderManagementFacade;

    /**
     * GET /api/admin/orders - Get all orders for admin management
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        try {
            List<OrderDTO> orders = orderManagementFacade.getAllOrdersForAdmin();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("totalOrders", orders.size());
            response.put("message", "Lấy danh sách đơn hàng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/admin/orders/{orderId} - Get order details by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Integer orderId) {
        try {
            OrderDTO order = orderManagementFacade.getOrderByIdForAdmin(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", order);
            response.put("message", "Lấy thông tin đơn hàng thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * PUT /api/admin/orders/{orderId}/status - Update order status
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestBody Map<String, String> statusRequest) {
        try {
            String newStatus = statusRequest.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trạng thái đơn hàng không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            OrderDTO updatedOrder = orderManagementFacade.updateOrderStatus(orderId, newStatus);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedOrder);
            response.put("message", "Cập nhật trạng thái đơn hàng thành công");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/admin/orders/status/{status} - Get orders by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(@PathVariable String status) {
        try {
            List<OrderDTO> orders = orderManagementFacade.getOrdersByStatus(status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("totalOrders", orders.size());
            response.put("status", status);
            response.put("message", "Lấy danh sách đơn hàng theo trạng thái thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/admin/orders/pending - Get pending orders
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingOrders() {
        try {
            List<OrderDTO> orders = orderManagementFacade.getPendingOrders();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("totalOrders", orders.size());
            response.put("message", "Lấy danh sách đơn hàng chờ duyệt thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách đơn hàng chờ duyệt: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/admin/orders/statistics - Get order statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        try {
            OrderStatistics stats = orderManagementFacade.getOrderStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "totalOrders", stats.getTotalOrders(),
                "pendingOrders", stats.getPendingOrders(),
                "completedOrders", stats.getCompletedOrders(),
                "cancelledOrders", stats.getCancelledOrders()
            ));
            response.put("message", "Lấy thống kê đơn hàng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thống kê đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/admin/orders/date-range - Get orders by date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getOrdersByDateRange(
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        try {
            List<OrderDTO> orders = orderManagementFacade.getOrdersByDateRange(fromDate, toDate);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("totalOrders", orders.size());
            response.put("fromDate", fromDate);
            response.put("toDate", toDate);
            response.put("message", "Lấy danh sách đơn hàng theo khoảng thời gian thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * PUT /api/admin/orders/bulk-status - Bulk update order status
     */
    @PutMapping("/bulk-status")
    public ResponseEntity<Map<String, Object>> bulkUpdateOrderStatus(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> orderIds = (List<Integer>) request.get("orderIds");
            String newStatus = (String) request.get("status");

            if (orderIds == null || orderIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Danh sách đơn hàng không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (newStatus == null || newStatus.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trạng thái đơn hàng không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            List<OrderDTO> updatedOrders = orderManagementFacade.bulkUpdateOrderStatus(orderIds, newStatus);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedOrders);
            response.put("updatedCount", updatedOrders.size());
            response.put("message", "Cập nhật trạng thái đơn hàng hàng loạt thành công");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
