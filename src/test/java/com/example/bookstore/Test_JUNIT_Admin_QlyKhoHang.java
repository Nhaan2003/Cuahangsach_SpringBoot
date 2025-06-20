package com.example.bookstore;

import com.example.bookstore.dto.InventoryDTO;
import com.example.bookstore.dto.Token;
import com.example.bookstore.dto.UserDTO;
import com.example.bookstore.facade.AuthFacade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"server.port=8080"})
public class Test_JUNIT_Admin_QlyKhoHang {

    @Autowired
    private AuthFacade authFacade;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String token;
    private Integer adminUserId;
    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/admin/inventory";
        // Đăng nhập admin lấy token và userId
        Token loginToken = authFacade.login("admin", "abc@123");
        Assertions.assertNotNull(loginToken);
        token = loginToken.getAccess_token();
        UserDTO user = authFacade.getCurrentUser(token);
        Assertions.assertNotNull(user);
        adminUserId = user.getUserId();

        // Đăng ký module cho LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Tạo giao dịch nhập kho hợp lệ")
    void testCreateImportTransaction() throws Exception {
        InventoryDTO dto = new InventoryDTO();
        dto.setBookId(1); // Đảm bảo bookId=1 tồn tại
        dto.setTransactionType("Nhập");
        dto.setQuantity(5);
        dto.setPrice(new BigDecimal("10000"));
        dto.setUserId(adminUserId);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(Collections.singletonList(dto), getAuthHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        List<InventoryDTO> result = objectMapper.readValue(response.getBody(), new TypeReference<List<InventoryDTO>>(){});
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("Nhập", result.get(0).getTransactionType());
    }

    @Test
    @DisplayName("Tạo giao dịch xuất kho vượt tồn kho - phải báo lỗi")
    void testExportOverStock() {
        InventoryDTO dto = new InventoryDTO();
        dto.setBookId(1); // Đảm bảo bookId=1 tồn tại
        dto.setTransactionType("Xuất");
        dto.setQuantity(99999); // Số lượng lớn hơn tồn kho
        dto.setPrice(new BigDecimal("10000"));
        dto.setUserId(adminUserId);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(Collections.singletonList(dto), getAuthHeaders());
        Exception ex = Assertions.assertThrows(Exception.class, () -> {
            restTemplate.postForEntity(baseUrl, request, String.class);
        });
        Assertions.assertTrue(ex.getMessage().contains("tồn kho") || ex.getMessage().contains("không đủ"));
    }

    @Test
    @DisplayName("Lấy danh sách giao dịch kho hàng")
    void testGetAllInventoryTransactions() throws Exception {
        HttpEntity<Void> request = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        List<InventoryDTO> result = objectMapper.readValue(response.getBody(), new TypeReference<List<InventoryDTO>>(){});
        Assertions.assertNotNull(result);
    }

    @Test
    @DisplayName("Chọn nhiều sản phẩm, nhập kho, kiểm tra lịch sử nhập xuất")
    void testImportAndCheckHistory() throws Exception {
        // Giả sử bookId 1 và 2 tồn tại
        int[] bookIds = {1, 2};

        // 1. Nhập kho cho nhiều sản phẩm
        InventoryDTO dto1 = new InventoryDTO();
        dto1.setBookId(bookIds[0]);
        dto1.setTransactionType("Nhập");
        dto1.setQuantity(10);
        dto1.setPrice(new BigDecimal("50000"));
        dto1.setUserId(adminUserId);

        InventoryDTO dto2 = new InventoryDTO();
        dto2.setBookId(bookIds[1]);
        dto2.setTransactionType("Nhập");
        dto2.setQuantity(5);
        dto2.setPrice(new BigDecimal("120000"));
        dto2.setUserId(adminUserId);

        List<InventoryDTO> importList = Arrays.asList(dto1, dto2);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(importList, getAuthHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        List<InventoryDTO> result = objectMapper.readValue(response.getBody(), new TypeReference<List<InventoryDTO>>() {});
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Nhập", result.get(0).getTransactionType());

        // 2. Lấy lịch sử nhập xuất kho
        HttpEntity<Void> getRequest = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<String> historyResponse = restTemplate.exchange(baseUrl, HttpMethod.GET, getRequest, String.class);
        Assertions.assertEquals(HttpStatus.OK, historyResponse.getStatusCode());

        List<InventoryDTO> history = objectMapper.readValue(historyResponse.getBody(), new TypeReference<List<InventoryDTO>>() {});
        // Kiểm tra lịch sử có chứa các giao dịch vừa nhập
        boolean found1 = history.stream().anyMatch(t -> t.getBookId().equals(bookIds[0]) && t.getTransactionType().equals("Nhập") && t.getQuantity() == 10);
        boolean found2 = history.stream().anyMatch(t -> t.getBookId().equals(bookIds[1]) && t.getTransactionType().equals("Nhập") && t.getQuantity() == 5);

        Assertions.assertTrue(found1, "Lịch sử phải có giao dịch nhập cho bookId " + bookIds[0]);
        Assertions.assertTrue(found2, "Lịch sử phải có giao dịch nhập cho bookId " + bookIds[1]);
    }

    @Test
    @DisplayName("Xuất kho hợp lệ - số lượng nhỏ hơn tồn kho")
    void testValidExport() throws Exception {
        // Giả sử bookId 1 đã có tồn kho >= 5
        int bookId = 1;

        // Xuất kho
        InventoryDTO exportDto = new InventoryDTO();
        exportDto.setBookId(bookId);
        exportDto.setTransactionType("Xuất");
        exportDto.setQuantity(3); // Xuất ít hơn tồn kho
        exportDto.setPrice(new BigDecimal("80000"));
        exportDto.setUserId(adminUserId);

        List<InventoryDTO> exportList = Arrays.asList(exportDto);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(exportList, getAuthHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        List<InventoryDTO> result = objectMapper.readValue(response.getBody(), new TypeReference<List<InventoryDTO>>() {});
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Xuất", result.get(0).getTransactionType());
        Assertions.assertEquals(3, result.get(0).getQuantity());
    }

    @Test
    @DisplayName("Xuất kho với số lượng = 0 - phải báo lỗi")
    void testExportZeroQuantity() {
        int bookId = 1;

        // Xuất kho với số lượng = 0
        InventoryDTO exportDto = new InventoryDTO();
        exportDto.setBookId(bookId);
        exportDto.setTransactionType("Xuất");
        exportDto.setQuantity(0); // Số lượng = 0
        exportDto.setPrice(new BigDecimal("80000"));
        exportDto.setUserId(adminUserId);

        List<InventoryDTO> exportList = Arrays.asList(exportDto);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(exportList, getAuthHeaders());
        
        // Phải throw exception vì số lượng = 0
        Exception ex = Assertions.assertThrows(Exception.class, () -> {
            restTemplate.postForEntity(baseUrl, request, String.class);
        });
        
        String errorMessage = ex.getMessage().toLowerCase();
        Assertions.assertTrue(
            errorMessage.contains("số lượng") || 
            errorMessage.contains("quantity") || 
            errorMessage.contains("lớn hơn 0"),
            "Thông báo lỗi phải chỉ ra vấn đề về số lượng. Actual: " + ex.getMessage()
        );
    }

    @Test
    @DisplayName("Xuất kho với giá = 0 - phải báo lỗi")
    void testExportZeroPrice() {
        int bookId = 1;

        // Xuất kho với giá = 0
        InventoryDTO exportDto = new InventoryDTO();
        exportDto.setBookId(bookId);
        exportDto.setTransactionType("Xuất");
        exportDto.setQuantity(2);
        exportDto.setPrice(BigDecimal.ZERO); // Giá = 0
        exportDto.setUserId(adminUserId);

        List<InventoryDTO> exportList = Arrays.asList(exportDto);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(exportList, getAuthHeaders());
        
        // Phải throw exception vì giá = 0
        Exception ex = Assertions.assertThrows(Exception.class, () -> {
            restTemplate.postForEntity(baseUrl, request, String.class);
        });
        
        String errorMessage = ex.getMessage().toLowerCase();
        Assertions.assertTrue(
            errorMessage.contains("giá") || 
            errorMessage.contains("price") || 
            errorMessage.contains("lớn hơn 0"),
            "Thông báo lỗi phải chỉ ra vấn đề về giá. Actual: " + ex.getMessage()
        );
    }

    @Test
    @DisplayName("Xuất kho và kiểm tra lịch sử")
    void testExportAndCheckHistory() throws Exception {
        // Giả sử bookId 1 đã có tồn kho >= 10
        int bookId = 1;

        // Xuất kho
        InventoryDTO exportDto = new InventoryDTO();
        exportDto.setBookId(bookId);
        exportDto.setTransactionType("Xuất");
        exportDto.setQuantity(5);
        exportDto.setPrice(new BigDecimal("75000"));
        exportDto.setUserId(adminUserId);

        List<InventoryDTO> exportList = Arrays.asList(exportDto);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(exportList, getAuthHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        // Lấy lịch sử nhập xuất kho
        HttpEntity<Void> getRequest = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<String> historyResponse = restTemplate.exchange(baseUrl, HttpMethod.GET, getRequest, String.class);
        Assertions.assertEquals(HttpStatus.OK, historyResponse.getStatusCode());

        List<InventoryDTO> history = objectMapper.readValue(historyResponse.getBody(), new TypeReference<List<InventoryDTO>>() {});
        
        // Kiểm tra lịch sử có chứa giao dịch xuất vừa thực hiện
        boolean foundExport = history.stream().anyMatch(t -> 
            t.getBookId().equals(bookId) && 
            t.getTransactionType().equals("Xuất") && 
            t.getQuantity() == 5
        );

        Assertions.assertTrue(foundExport, "Lịch sử phải có giao dịch xuất cho bookId " + bookId + " với số lượng 5");
    }

    @Test
    @DisplayName("Nhập kho với số lượng âm - phải báo lỗi")
    void testImportNegativeQuantity() {
        int bookId = 1;

        // Nhập kho với số lượng âm
        InventoryDTO importDto = new InventoryDTO();
        importDto.setBookId(bookId);
        importDto.setTransactionType("Nhập");
        importDto.setQuantity(-5); // Số lượng âm
        importDto.setPrice(new BigDecimal("50000"));
        importDto.setUserId(adminUserId);

        List<InventoryDTO> importList = Arrays.asList(importDto);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(importList, getAuthHeaders());
        
        // Phải throw exception vì số lượng âm
        Exception ex = Assertions.assertThrows(Exception.class, () -> {
            restTemplate.postForEntity(baseUrl, request, String.class);
        });
        
        String errorMessage = ex.getMessage().toLowerCase();
        Assertions.assertTrue(
            errorMessage.contains("số lượng") || 
            errorMessage.contains("quantity") || 
            errorMessage.contains("lớn hơn 0") ||
            errorMessage.contains("dương"),
            "Thông báo lỗi phải chỉ ra vấn đề về số lượng âm. Actual: " + ex.getMessage()
        );
    }

    @Test
    @DisplayName("Nhập kho với giá âm - phải báo lỗi")
    void testImportNegativePrice() {
        int bookId = 1;

        // Nhập kho với giá âm
        InventoryDTO importDto = new InventoryDTO();
        importDto.setBookId(bookId);
        importDto.setTransactionType("Nhập");
        importDto.setQuantity(10);
        importDto.setPrice(new BigDecimal("-50000")); // Giá âm
        importDto.setUserId(adminUserId);

        List<InventoryDTO> importList = Arrays.asList(importDto);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(importList, getAuthHeaders());
        
        // Phải throw exception vì giá âm
        Exception ex = Assertions.assertThrows(Exception.class, () -> {
            restTemplate.postForEntity(baseUrl, request, String.class);
        });
        
        String errorMessage = ex.getMessage().toLowerCase();
        Assertions.assertTrue(
            errorMessage.contains("giá") || 
            errorMessage.contains("price") || 
            errorMessage.contains("lớn hơn 0") ||
            errorMessage.contains("dương"),
            "Thông báo lỗi phải chỉ ra vấn đề về giá âm. Actual: " + ex.getMessage()
        );
    }

    @Test
    @DisplayName("Kiểm tra validation bookId không tồn tại")
    void testInvalidBookId() {
        int invalidBookId = 99999; // BookId không tồn tại

        // Nhập kho với bookId không tồn tại
        InventoryDTO importDto = new InventoryDTO();
        importDto.setBookId(invalidBookId);
        importDto.setTransactionType("Nhập");
        importDto.setQuantity(10);
        importDto.setPrice(new BigDecimal("50000"));
        importDto.setUserId(adminUserId);

        List<InventoryDTO> importList = Arrays.asList(importDto);

        HttpEntity<List<InventoryDTO>> request = new HttpEntity<>(importList, getAuthHeaders());
        
        // Phải throw exception vì bookId không tồn tại
        Exception ex = Assertions.assertThrows(Exception.class, () -> {
            restTemplate.postForEntity(baseUrl, request, String.class);
        });
        
        String errorMessage = ex.getMessage().toLowerCase();
        Assertions.assertTrue(
            errorMessage.contains("sách") || 
            errorMessage.contains("book") || 
            errorMessage.contains("không tồn tại") ||
            errorMessage.contains("not found"),
            "Thông báo lỗi phải chỉ ra vấn đề về bookId không tồn tại. Actual: " + ex.getMessage()
        );
    }
}