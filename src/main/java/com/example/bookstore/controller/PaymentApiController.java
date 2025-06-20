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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.CheckoutRequestDTO;
import com.example.bookstore.dto.CheckoutResponseDTO;
import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.dto.PaymentDTO;
import com.example.bookstore.dto.UserDTO;
import com.example.bookstore.facade.AuthFacade;
import com.example.bookstore.facade.PaymentFacade;

import jakarta.validation.Valid;

/**
 * REST API Controller for payment and checkout operations
 * Handles all payment-related endpoints following the project's architecture
 */
@RestController
@RequestMapping("/api/user/payment")
@Validated
public class PaymentApiController {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private AuthFacade authFacade;    /**
     * POST /api/user/payment/checkout - Process checkout request
     * Creates order, order details, and payment records
     */
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> processCheckout(@Valid @RequestBody CheckoutRequestDTO checkoutRequest) {
        try {
            CheckoutResponseDTO result = paymentFacade.processCheckout(checkoutRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result.getMessage());
            
            Map<String, Object> data = new HashMap<>();
            data.put("order", result.getOrder());
            data.put("payment", result.getPayment());
            response.put("data", data);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi hệ thống khi xử lý đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/user/payment/orders/{orderId} - Get order details by ID
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Integer orderId) {
        try {
            OrderDTO order = paymentFacade.getOrderById(orderId);
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
     * GET /api/user/payment/orders/{orderId}/payment - Get payment details for an order
     */
    @GetMapping("/orders/{orderId}/payment")
    public ResponseEntity<Map<String, Object>> getPaymentByOrderId(@PathVariable Integer orderId) {
        try {
            PaymentDTO payment = paymentFacade.getPaymentByOrderId(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", payment);
            response.put("message", "Lấy thông tin thanh toán thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin thanh toán: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/user/payment/orders/user/{userId} - Get all orders for a user
     */
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrders(@PathVariable Integer userId) {
        try {
            List<OrderDTO> orders = paymentFacade.getUserOrders(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("message", "Lấy danh sách đơn hàng thành công");
            response.put("totalOrders", orders.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/user/payment/orders - Get all orders for the current authenticated user
     */
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getCurrentUserOrders(@RequestHeader("Authorization") String token) {
        try {
            // Get current user from token
            UserDTO currentUser = authFacade.getCurrentUser(token.replace("Bearer ", ""));
            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Token không hợp lệ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<OrderDTO> orders = paymentFacade.getUserOrders(currentUser.getUserId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("message", "Lấy danh sách đơn hàng thành công");
            response.put("totalOrders", orders.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }    /**
     * POST /api/user/payment/orders/{orderId}/status - Update order status (for admin use)
     */
    @PostMapping("/orders/{orderId}/status")
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

            // Validate status values
            if (!isValidOrderStatus(newStatus)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trạng thái đơn hàng không hợp lệ: " + newStatus);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            OrderDTO updatedOrder = paymentFacade.updateOrderStatus(orderId, newStatus);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedOrder);
            response.put("message", "Cập nhật trạng thái đơn hàng thành công");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * POST /api/user/payment/orders/{orderId}/cancel - Cancel order by customer
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Integer orderId,
            @RequestHeader("Authorization") String token) {
        try {
            // Get current user from token
            UserDTO currentUser = authFacade.getCurrentUser(token.replace("Bearer ", ""));
            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Token không hợp lệ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            OrderDTO cancelledOrder = paymentFacade.cancelOrder(orderId, currentUser.getUserId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cancelledOrder);
            response.put("message", "Đã hủy đơn hàng thành công");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi hủy đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }    /**
     * Validate order status values
     */
    private boolean isValidOrderStatus(String status) {
        return status.equals("Đang xử lý") || 
               status.equals("Đã giao") || 
               status.equals("Đã hủy");
    }
}
