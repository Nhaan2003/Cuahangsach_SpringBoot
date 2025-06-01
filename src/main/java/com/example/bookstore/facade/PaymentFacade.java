package com.example.bookstore.facade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.CheckoutItemDTO;
import com.example.bookstore.dto.CheckoutRequestDTO;
import com.example.bookstore.dto.CheckoutResponseDTO;
import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.dto.OrderDetailDTO;
import com.example.bookstore.dto.PaymentDTO;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderDetail;
import com.example.bookstore.model.Payment;
import com.example.bookstore.model.User;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.OrderDetailService;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.service.PaymentService;
import com.example.bookstore.service.ShoppingCartService;
import com.example.bookstore.service.UserService;

@Component
public class PaymentFacade {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderDetailService orderDetailService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private ShoppingCartService shoppingCartService;
    
    /**
     * Process checkout request - creates order, order details, and payment
     */
    @Transactional
    public CheckoutResponseDTO processCheckout(CheckoutRequestDTO checkoutRequest) {
        try {
            // 1. Validate user exists
            User user = userService.getUserById(checkoutRequest.getUserId());
            if (user == null) {
                return new CheckoutResponseDTO(false, "Người dùng không tồn tại");
            }
            
            // 2. Validate items and stock availability
            List<CheckoutItemDTO> items = checkoutRequest.getItems();
            BigDecimal calculatedTotal = BigDecimal.ZERO;
            
            for (CheckoutItemDTO item : items) {
                Book book = bookService.getBookById(item.getBookId());
                if (book == null) {
                    return new CheckoutResponseDTO(false, "Sách với ID " + item.getBookId() + " không tồn tại");
                }
                
                // Check stock availability
                if (book.getStockQuantity() < item.getQuantity()) {
                    return new CheckoutResponseDTO(false, 
                        "Sách '" + book.getTitle() + "' không đủ số lượng trong kho. Còn lại: " + book.getStockQuantity());
                }
                
                // Verify price
                if (item.getPrice().compareTo(book.getPrice()) != 0) {
                    return new CheckoutResponseDTO(false, 
                        "Giá sách '" + book.getTitle() + "' đã thay đổi. Vui lòng cập nhật giỏ hàng");
                }
                
                calculatedTotal = calculatedTotal.add(item.getSubtotal());
            }
            
            // 3. Verify total amount
            if (calculatedTotal.compareTo(checkoutRequest.getTotalAmount()) != 0) {
                return new CheckoutResponseDTO(false, "Tổng tiền không chính xác");
            }
            
            // 4. Create order
            Order order = createOrder(user, checkoutRequest);
            
            // 5. Create order details and update stock
            List<OrderDetail> orderDetails = createOrderDetails(order, items);
            
            // 6. Create payment record
            Payment payment = createPayment(order, checkoutRequest.getPaymentMethod());
            
            // 7. Clear shopping cart for user
            shoppingCartService.clearCart(checkoutRequest.getUserId());
            
            // 8. Prepare response
            OrderDTO orderDTO = new OrderDTO(order);
            orderDTO.setOrderDetails(orderDetails.stream()
                .map(OrderDetailDTO::new)
                .toList());
            
            PaymentDTO paymentDTO = new PaymentDTO(payment);
            
            return new CheckoutResponseDTO(true, "Đặt hàng thành công", orderDTO, paymentDTO);
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý đơn hàng: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create order record
     */
    private Order createOrder(User user, CheckoutRequestDTO checkoutRequest) {        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(checkoutRequest.getTotalAmount());
        order.setShippingAddress(checkoutRequest.getShippingAddress());
        order.setStatus("Đang xử lý");
        order.setOrderDate(LocalDateTime.now());
        
        return orderService.save(order);
    }
    
    /**
     * Create order details and update book stock
     */
    private List<OrderDetail> createOrderDetails(Order order, List<CheckoutItemDTO> items) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        
        for (CheckoutItemDTO item : items) {
            Book book = bookService.getBookById(item.getBookId());
            
            // Create order detail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setBook(book);
            orderDetail.setQuantity(item.getQuantity());
            orderDetail.setPriceAtOrder(item.getPrice());
            
            OrderDetail savedOrderDetail = orderDetailService.save(orderDetail);
            orderDetails.add(savedOrderDetail);
            
            // Update book stock
            book.setStockQuantity(book.getStockQuantity() - item.getQuantity());
            bookService.updateBookStock(book.getBookId(), book.getStockQuantity());
        }
        
        return orderDetails;
    }
    
    /**
     * Create payment record
     */
    private Payment createPayment(Order order, String paymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        
        return paymentService.save(payment);
    }
    
    /**
     * Get order by ID with details
     */
    public OrderDTO getOrderById(Integer orderId) {
        Order order = orderService.getOrderById(orderId).orElse(null);
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không tồn tại");
        }
        
        return new OrderDTO(order);
    }
    
    /**
     * Get payment by order ID
     */
    public PaymentDTO getPaymentByOrderId(Integer orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null) {
            throw new IllegalArgumentException("Thông tin thanh toán không tồn tại");
        }
        
        return new PaymentDTO(payment);
    }
    
    /**
     * Get all orders for a user
     */
    public List<OrderDTO> getUserOrders(Integer userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return orders.stream()
            .map(OrderDTO::new)
            .toList();
    }
    
    /**
     * Update order status
     */
    @Transactional
    public OrderDTO updateOrderStatus(Integer orderId, String status) {
        Order order = orderService.getOrderById(orderId).orElse(null);
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không tồn tại");
        }
        
        order.setStatus(status);
        Order updatedOrder = orderService.save(order);
        
        return new OrderDTO(updatedOrder);
    }
}
