package com.example.bookstore;

import com.example.bookstore.dto.CategoryDTO;
import com.example.bookstore.dto.ProductDTO;
import com.example.bookstore.facade.ProductFacade;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderDetail;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.repository.OrderDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class Test_JUNIT_Admin_QlySanPham {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    private ProductDTO testProductDTO;
    private Category testCategory;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Tạo danh mục test
        testCategory = new Category();
        testCategory.setCategoryName("Test Category " + System.currentTimeMillis());
        testCategory = categoryRepository.save(testCategory);

        // Tạo DTO sản phẩm test
        testProductDTO = new ProductDTO();
        testProductDTO.setTitle("Test Book " + System.currentTimeMillis());
        testProductDTO.setAuthor("Test Author");
        testProductDTO.setPublisher("Test Publisher");
        testProductDTO.setPublicationYear((short) 2023);
        testProductDTO.setDescription("Test Description");
        testProductDTO.setPrice(new BigDecimal("150000"));
        testProductDTO.setStockQuantity(10);
        testProductDTO.setCategoryIds(Collections.singletonList(testCategory.getCategoryId()));
    }

    @Test
    @DisplayName("Thêm sản phẩm mới thành công")
    void testCreateProduct_Success() {
        // When - thực hiện tạo sản phẩm mới
        ProductDTO createdProduct = productFacade.createProduct(testProductDTO);

        // Then - kiểm tra kết quả
        assertNotNull(createdProduct);
        assertNotNull(createdProduct.getBookId());
        assertEquals(testProductDTO.getTitle(), createdProduct.getTitle());
        assertEquals(testProductDTO.getAuthor(), createdProduct.getAuthor());
        assertEquals(testProductDTO.getPrice(), createdProduct.getPrice());
        assertEquals(testProductDTO.getStockQuantity(), createdProduct.getStockQuantity());
        
        // Kiểm tra danh mục đã được gán đúng
        assertTrue(createdProduct.getCategories().stream()
            .anyMatch(cat -> cat.getCategoryId().equals(testCategory.getCategoryId())));
    }

    @Test
    @DisplayName("Cập nhật thông tin sản phẩm thành công")
    void testUpdateProduct_Success() {
        // Given - tạo sản phẩm mới trước
        ProductDTO createdProduct = productFacade.createProduct(testProductDTO);
        Integer bookId = createdProduct.getBookId();

        // When - cập nhật thông tin sản phẩm
        String updatedTitle = "Updated Title " + System.currentTimeMillis();
        BigDecimal updatedPrice = new BigDecimal("200000");
        
        createdProduct.setTitle(updatedTitle);
        createdProduct.setPrice(updatedPrice);
        
        ProductDTO updatedProduct = productFacade.updateProduct(bookId, createdProduct);

        // Then - kiểm tra kết quả
        assertNotNull(updatedProduct);
        assertEquals(bookId, updatedProduct.getBookId());
        assertEquals(updatedTitle, updatedProduct.getTitle());
        assertEquals(updatedPrice, updatedProduct.getPrice());
    }

    @Test
    @DisplayName("Lấy thông tin sản phẩm theo ID thành công")
    void testGetProductById_Success() {
        // Given - tạo sản phẩm mới trước
        ProductDTO createdProduct = productFacade.createProduct(testProductDTO);
        Integer bookId = createdProduct.getBookId();

        // When - lấy thông tin sản phẩm
        ProductDTO retrievedProduct = productFacade.getProductByIdDTO(bookId);

        // Then - kiểm tra kết quả
        assertNotNull(retrievedProduct);
        assertEquals(bookId, retrievedProduct.getBookId());
        assertEquals(testProductDTO.getTitle(), retrievedProduct.getTitle());
        assertEquals(testProductDTO.getAuthor(), retrievedProduct.getAuthor());
    }

    @Test
    @DisplayName("Lấy thông tin sản phẩm không tồn tại sẽ ném ra ngoại lệ")
    void testGetProductById_NotFound() {
        // Given - ID không tồn tại
        Integer nonExistentId = 9999;

        // When & Then - kiểm tra ngoại lệ
        assertThrows(IllegalArgumentException.class, () -> {
            productFacade.getProductByIdDTO(nonExistentId);
        });
    }

    @Test
    @DisplayName("Lấy danh sách tất cả sản phẩm thành công")
    void testGetAllProducts_Success() {
        // Given - tạo một vài sản phẩm mới
        productFacade.createProduct(testProductDTO);
        
        // When - lấy danh sách sản phẩm
        List<ProductDTO> products = productFacade.getAllProductsDTO();

        // Then - kiểm tra kết quả
        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    @DisplayName("Xóa sản phẩm thành công")
    void testDeleteProduct_Success() {
        // Given - tạo sản phẩm mới trước
        ProductDTO createdProduct = productFacade.createProduct(testProductDTO);
        Integer bookId = createdProduct.getBookId();

        // When - xóa sản phẩm
        productFacade.deleteProduct(bookId);

        // Then - kiểm tra sản phẩm đã bị xóa
        Optional<Book> deletedBook = bookRepository.findById(bookId);
        assertFalse(deletedBook.isPresent());
    }

    // @Test
    // @DisplayName("Xóa sản phẩm đã có đơn hàng sẽ thất bại")
    // void testDeleteProduct_WithOrders_Fails() {
    //     // Given - tạo sản phẩm mới
    //     ProductDTO createdProduct = productFacade.createProduct(testProductDTO);
    //     Integer bookId = createdProduct.getBookId();
        
    //     // Tạo một đơn hàng liên quan đến sản phẩm này
    //     Book book = bookRepository.findById(bookId).orElseThrow();
    //     OrderDetail orderDetail = new OrderDetail();
    //     orderDetail.setBook(book);
    //     orderDetail.setQuantity(1);
    //     orderDetail.setPriceAtOrder(book.getPrice());
        
    //     // Lưu orderDetail trực tiếp hoặc thông qua order
    //     Order order = new Order();
    //     // Thiết lập các trường cần thiết cho order
    //     orderDetail.setOrder(order);
    //     orderDetailRepository.save(orderDetail);

    //     // When & Then - xóa sản phẩm sẽ ném ra ngoại lệ
    //     assertThrows(DataIntegrityViolationException.class, () -> {
    //         productFacade.deleteProduct(bookId);
    //     });
    // }

    @Test
    @DisplayName("Thêm danh mục mới thành công")
    void testCreateCategory_Success() {
        // Given
        String categoryName = "New Test Category " + System.currentTimeMillis();

        // When
        CategoryDTO createdCategory = productFacade.createCategory(categoryName);

        // Then
        assertNotNull(createdCategory);
        assertNotNull(createdCategory.getCategoryId());
        assertEquals(categoryName, createdCategory.getCategoryName());
    }

    @Test
    @DisplayName("Cập nhật tên danh mục thành công")
    void testUpdateCategory_Success() {
        // Given - tạo danh mục mới
        String originalName = "Original Category " + System.currentTimeMillis();
        CategoryDTO createdCategory = productFacade.createCategory(originalName);
        
        // When - cập nhật tên danh mục
        String updatedName = "Updated Category " + System.currentTimeMillis();
        CategoryDTO updatedCategory = productFacade.updateCategory(
            createdCategory.getCategoryId(), updatedName);

        // Then
        assertNotNull(updatedCategory);
        assertEquals(createdCategory.getCategoryId(), updatedCategory.getCategoryId());
        assertEquals(updatedName, updatedCategory.getCategoryName());
    }

    @Test
    @DisplayName("Xóa danh mục thành công")
    void testDeleteCategory_Success() {
        // Given - tạo danh mục mới
        String categoryName = "Category To Delete " + System.currentTimeMillis();
        CategoryDTO createdCategory = productFacade.createCategory(categoryName);
        Integer categoryId = createdCategory.getCategoryId();
        
        // When - xóa danh mục
        productFacade.deleteCategory(categoryId);
        
        // Then - kiểm tra danh mục đã bị xóa
        Optional<Category> deletedCategory = categoryRepository.findById(categoryId);
        assertFalse(deletedCategory.isPresent());
    }

    @Test
    @DisplayName("Xóa danh mục không tồn tại sẽ ném ra ngoại lệ")
    void testDeleteCategory_NotFound() {
        // Given - ID không tồn tại
        Integer nonExistentId = 9999;
        
        // When & Then - kiểm tra ngoại lệ
        assertThrows(IllegalArgumentException.class, () -> {
            productFacade.deleteCategory(nonExistentId);
        });
    }
}