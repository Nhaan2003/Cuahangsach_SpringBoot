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
import com.example.bookstore.dto.InventoryDTO;
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

    @Autowired
    private ProductFacade productFacade;    /**
     * Process checkout request - creates order, order details, and payment
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResponseDTO processCheckout(CheckoutRequestDTO checkoutRequest) {
        // 1. Validate user exists
        User user = userService.getUserById(checkoutRequest.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("Người dùng không tồn tại");
        }

        // 2. Validate items and stock availability
        List<CheckoutItemDTO> items = checkoutRequest.getItems();
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (CheckoutItemDTO item : items) {
            Book book = bookService.getBookById(item.getBookId());
            if (book == null) {
                throw new IllegalArgumentException("Sách với ID " + item.getBookId() + " không tồn tại");
            }            // Check stock availability
            System.out.println("🔍 Validating stock for book: " + book.getTitle() + 
                             ", Available: " + book.getStockQuantity() + 
                             ", Requested: " + item.getQuantity());
            
            if (book.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Sách '" + book.getTitle() + "' không đủ số lượng trong kho. Còn lại: "
                                + book.getStockQuantity());
            }

            // Verify price
            if (item.getPrice().compareTo(book.getPrice()) != 0) {
                throw new IllegalArgumentException(
                        "Giá sách '" + book.getTitle() + "' đã thay đổi. Vui lòng cập nhật giỏ hàng");
            }

            calculatedTotal = calculatedTotal.add(item.getSubtotal());
        }

        // 3. Verify total amount
        if (calculatedTotal.compareTo(checkoutRequest.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("Tổng tiền không chính xác");
        }        // 4. Create order
        Order order = createOrder(user, checkoutRequest);

        // 5. Create order details (NO STOCK UPDATE - only when delivered)
        List<OrderDetail> orderDetails = createOrderDetailsWithoutStockUpdate(order, items);

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
    }

    /**
     * Create order record
     */
    private Order createOrder(User user, CheckoutRequestDTO checkoutRequest) {
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(checkoutRequest.getTotalAmount());
        order.setShippingAddress(checkoutRequest.getShippingAddress());
        order.setStatus("Đang xử lý");
        order.setOrderDate(LocalDateTime.now());

        return orderService.save(order);
    }    /**
     * Create order details WITHOUT updating stock (for new orders)
     */
    private List<OrderDetail> createOrderDetailsWithoutStockUpdate(Order order, List<CheckoutItemDTO> items) {
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

            System.out.println("📦 Order detail created for book: " + book.getTitle() + 
                             ", Quantity: " + item.getQuantity() + 
                             ", Status: Đang xử lý (NO stock update)");
        }

        return orderDetails;
    }

    /**
     * Create order details WITH stock update and inventory transactions (when order is delivered)
     */
    private List<OrderDetail> createOrderDetailsWithStockUpdate(Order order, List<CheckoutItemDTO> items) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        List<InventoryDTO> inventoryDTOs = new ArrayList<>();

        for (CheckoutItemDTO item : items) {
            Book book = bookService.getBookById(item.getBookId());

            // Create order detail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setBook(book);
            orderDetail.setQuantity(item.getQuantity());
            orderDetail.setPriceAtOrder(item.getPrice());            OrderDetail savedOrderDetail = orderDetailService.save(orderDetail);
            orderDetails.add(savedOrderDetail);

            // DON'T update stock here - let InventoryService handle it to avoid double deduction
            // The stock will be updated when creating inventory transactions
            
            System.out.println("📦 Order detail created for book: " + book.getTitle() + 
                             ", Quantity: " + item.getQuantity() + 
                             ", Current Stock: " + book.getStockQuantity());

            // Create inventory transaction DTO for "Xuất" (export)
            InventoryDTO inventoryDTO = new InventoryDTO(
                    item.getBookId(),
                    "Xuất", // Transaction type - export
                    item.getQuantity(),
                    item.getPrice(),
                    order.getUser().getUserId());            inventoryDTOs.add(inventoryDTO);
            
            System.out.println("📦 Created inventory DTO for book: " + book.getTitle() + 
                             ", Transaction Quantity: " + item.getQuantity() + 
                             ", Transaction Type: Xuất");
        }        // Create inventory transactions
        if (!inventoryDTOs.isEmpty()) {
            System.out.println("📋 Creating " + inventoryDTOs.size() + " inventory transactions...");
            productFacade.createInventoryTransactions(inventoryDTOs);
            System.out.println("✅ Inventory transactions created successfully");
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
    }    /**
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
    
    /**
     * Cancel order by customer (only if status is "Đang xử lý")
     */
    @Transactional
    public OrderDTO cancelOrder(Integer orderId, Integer userId) {
        // Get order with details
        Order order = orderService.getOrderById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại với ID: " + orderId));
        
        // Validate ownership
        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn hàng này");
        }
        
        // Only allow cancellation if order is still processing
        if (!"Đang xử lý".equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể hủy đơn hàng đang xử lý. Trạng thái hiện tại: " + order.getStatus());
        }
        
        // Update order status to cancelled
        Order cancelledOrder = orderService.updateOrderStatus(orderId, "Đã hủy");
        
        System.out.println("❌ Customer cancelled order - ID: " + orderId + ", User: " + userId);
        
        return new OrderDTO(cancelledOrder);
    }
}
