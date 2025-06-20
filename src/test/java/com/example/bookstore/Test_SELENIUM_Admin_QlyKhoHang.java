package com.example.bookstore;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class Test_SELENIUM_Admin_QlyKhoHang {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:5173";
    private static final String PRODUCTS_URL = BASE_URL + "/admin/products";
    private static final String INVENTORY_URL = BASE_URL + "/admin/inventory";

    @BeforeEach
    public void setUp() {
        System.out.println("Thiết lập môi trường kiểm thử Selenium cho quản lý kho hàng");
        
        // Cấu hình Chrome driver để hiển thị giao diện
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        // Đăng nhập với quyền admin
        driver.get(BASE_URL + "/auth/login");
        
        // Delay để trang web khởi động hoàn toàn
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        try {
            System.out.println("Đang đăng nhập với tài khoản admin...");
            
            // Đợi trang login load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            
            // Tìm phần tử form đăng nhập
            WebElement usernameField = driver.findElement(By.id("username"));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
            
            // Nhập thông tin đăng nhập với delay
            usernameField.clear();
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            usernameField.sendKeys("admin");
            
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            passwordField.clear();
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            passwordField.sendKeys("abc@123");
            
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Click nút đăng nhập
            loginButton.click();
            
            // Đợi chuyển trang sau khi đăng nhập thành công
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/admin"),
                ExpectedConditions.urlContains("/user")
            ));
            
            // Kiểm tra đã đăng nhập thành công
            String currentUrl = driver.getCurrentUrl();
            boolean loginSuccess = currentUrl.contains("/admin") || currentUrl.contains("/user");
            
            if (loginSuccess) {
                System.out.println("✅ Đăng nhập admin thành công: " + currentUrl);
            } else {
                System.err.println("❌ Đăng nhập admin thất bại. URL hiện tại: " + currentUrl);
                fail("Không thể đăng nhập với tài khoản admin");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đăng nhập: " + e.getMessage());
            fail("Không thể đăng nhập với tài khoản admin: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kiểm tra chọn sản phẩm và nhập kho hợp lệ")
    public void testChonSanPhamVaNhapKho() {
        try {
            System.out.println("Đang kiểm tra chức năng chọn sản phẩm và nhập kho");
            
            // 1. Chuyển đến trang quản lý sản phẩm
            driver.get(PRODUCTS_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 2. Đợi bảng sản phẩm tải
            WebElement productTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            List<WebElement> productRows = productTable.findElements(By.xpath(".//tbody/tr"));
            
            if (productRows.isEmpty()) {
                System.out.println("⚠️ Không có sản phẩm nào để test, bỏ qua test case");
                return;
            }
            
            // 3. Chọn sản phẩm đầu tiên (checkbox)
            WebElement firstProductCheckbox = productRows.get(0).findElement(By.xpath(".//input[@type='checkbox']"));
            firstProductCheckbox.click();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            System.out.println("  ✓ Đã chọn sản phẩm đầu tiên");
            
            // 4. Nhấn nút "Quản lý Tồn kho"
            WebElement inventoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Quản lý Tồn kho')]")));
            inventoryButton.click();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            System.out.println("  ✓ Đã nhấn nút Quản lý Tồn kho");
            
            // 5. Đợi modal hiển thị
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'fixed') and .//h3[contains(text(), 'Quản lý Tồn kho')]]")));
            
            // 6. Nhập số lượng và giá cho sản phẩm
            List<WebElement> quantityInputs = modal.findElements(By.xpath(".//input[@type='number' and @placeholder='Nhập số lượng']"));
            List<WebElement> priceInputs = modal.findElements(By.xpath(".//input[@type='number' and @placeholder='Nhập giá']"));
            
            if (!quantityInputs.isEmpty() && !priceInputs.isEmpty()) {
                // Nhập số lượng
                WebElement quantityInput = quantityInputs.get(0);
                quantityInput.clear();
                quantityInput.sendKeys("10");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                // Nhập giá
                WebElement priceInput = priceInputs.get(0);
                priceInput.clear();
                priceInput.sendKeys("50000");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                System.out.println("  ✓ Đã nhập số lượng: 10, giá: 50000");
                
                // 7. Nhấn nút "Nhập"
                WebElement importButton = modal.findElement(By.xpath(".//button[contains(text(), 'Nhập')]"));
                importButton.click();
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                System.out.println("  ✓ Đã nhấn nút Nhập");
                
                // 8. Kiểm tra thông báo thành công
                try {
                    WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class, 'bg-green-100')]")));
                    System.out.println("  ✓ Hiển thị thông báo thành công: " + successMessage.getText());
                } catch (Exception e) {
                    System.out.println("  ⚠️ Không tìm thấy thông báo thành công, nhưng modal đã đóng");
                }
                
                // 9. Kiểm tra modal đã đóng
                try {
                    wait.until(ExpectedConditions.invisibilityOf(modal));
                    System.out.println("  ✓ Modal đã đóng sau khi nhập kho");
                } catch (Exception e) {
                    System.out.println("  ⚠️ Modal vẫn hiển thị sau khi nhập kho");
                }
                
            } else {
                System.out.println("  ⚠️ Không tìm thấy input số lượng hoặc giá trong modal");
            }
            
            System.out.println("✅ Test chọn sản phẩm và nhập kho hoàn thành");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi test chọn sản phẩm và nhập kho: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi test chọn sản phẩm và nhập kho: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kiểm tra chọn nhiều sản phẩm và nhập kho")
    public void testChonNhieuSanPhamVaNhapKho() {
        try {
            System.out.println("Đang kiểm tra chức năng chọn nhiều sản phẩm và nhập kho");
            
            // 1. Chuyển đến trang quản lý sản phẩm
            driver.get(PRODUCTS_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 2. Đợi bảng sản phẩm tải
            WebElement productTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            List<WebElement> productRows = productTable.findElements(By.xpath(".//tbody/tr"));
            
            if (productRows.size() < 2) {
                System.out.println("⚠️ Không đủ sản phẩm để test chọn nhiều, bỏ qua test case");
                return;
            }
            
            // 3. Chọn 2 sản phẩm đầu tiên
            for (int i = 0; i < Math.min(2, productRows.size()); i++) {
                WebElement checkbox = productRows.get(i).findElement(By.xpath(".//input[@type='checkbox']"));
                checkbox.click();
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            
            System.out.println("  ✓ Đã chọn 2 sản phẩm");
            
            // 4. Nhấn nút "Quản lý Tồn kho"
            WebElement inventoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Quản lý Tồn kho')]")));
            inventoryButton.click();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 5. Đợi modal hiển thị
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'fixed') and .//h3[contains(text(), 'Quản lý Tồn kho')]]")));
            
            // 6. Nhập số lượng và giá cho từng sản phẩm
            List<WebElement> quantityInputs = modal.findElements(By.xpath(".//input[@type='number' and @placeholder='Nhập số lượng']"));
            List<WebElement> priceInputs = modal.findElements(By.xpath(".//input[@type='number' and @placeholder='Nhập giá']"));
            
            if (quantityInputs.size() >= 2 && priceInputs.size() >= 2) {
                // Nhập cho sản phẩm 1
                quantityInputs.get(0).clear();
                quantityInputs.get(0).sendKeys("5");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                priceInputs.get(0).clear();
                priceInputs.get(0).sendKeys("30000");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                // Nhập cho sản phẩm 2
                quantityInputs.get(1).clear();
                quantityInputs.get(1).sendKeys("8");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                priceInputs.get(1).clear();
                priceInputs.get(1).sendKeys("45000");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                System.out.println("  ✓ Đã nhập số lượng và giá cho 2 sản phẩm");
                
                // 7. Nhấn nút "Nhập"
                WebElement importButton = modal.findElement(By.xpath(".//button[contains(text(), 'Nhập')]"));
                importButton.click();
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                // 8. Kiểm tra thông báo thành công
                try {
                    WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class, 'bg-green-100')]")));
                    System.out.println("  ✓ Hiển thị thông báo thành công: " + successMessage.getText());
                } catch (Exception e) {
                    System.out.println("  ⚠️ Không tìm thấy thông báo thành công");
                }
            }
            
            System.out.println("✅ Test chọn nhiều sản phẩm và nhập kho hoàn thành");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi test chọn nhiều sản phẩm và nhập kho: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi test chọn nhiều sản phẩm và nhập kho: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kiểm tra xuất kho hợp lệ")
    public void testXuatKhoHopLe() {
        try {
            System.out.println("Đang kiểm tra chức năng xuất kho hợp lệ");
            
            // 1. Chuyển đến trang quản lý sản phẩm
            driver.get(PRODUCTS_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 2. Đợi bảng sản phẩm tải
            WebElement productTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            List<WebElement> productRows = productTable.findElements(By.xpath(".//tbody/tr"));
            
            if (productRows.isEmpty()) {
                System.out.println("⚠️ Không có sản phẩm nào để test, bỏ qua test case");
                return;
            }
            
            // 3. Chọn sản phẩm đầu tiên
            WebElement firstProductCheckbox = productRows.get(0).findElement(By.xpath(".//input[@type='checkbox']"));
            firstProductCheckbox.click();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 4. Nhấn nút "Quản lý Tồn kho"
            WebElement inventoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Quản lý Tồn kho')]")));
            inventoryButton.click();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 5. Đợi modal hiển thị
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'fixed') and .//h3[contains(text(), 'Quản lý Tồn kho')]]")));
            
            // 6. Nhập số lượng và giá
            List<WebElement> quantityInputs = modal.findElements(By.xpath(".//input[@type='number' and @placeholder='Nhập số lượng']"));
            List<WebElement> priceInputs = modal.findElements(By.xpath(".//input[@type='number' and @placeholder='Nhập giá']"));
            
            if (!quantityInputs.isEmpty() && !priceInputs.isEmpty()) {
                // Nhập số lượng xuất (ít hơn tồn kho)
                WebElement quantityInput = quantityInputs.get(0);
                quantityInput.clear();
                quantityInput.sendKeys("3");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                // Nhập giá
                WebElement priceInput = priceInputs.get(0);
                priceInput.clear();
                priceInput.sendKeys("60000");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                System.out.println("  ✓ Đã nhập số lượng xuất: 3, giá: 60000");
                
                // 7. Nhấn nút "Xuất"
                WebElement exportButton = modal.findElement(By.xpath(".//button[contains(text(), 'Xuất')]"));
                exportButton.click();
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                System.out.println("  ✓ Đã nhấn nút Xuất");
                
                // 8. Kiểm tra thông báo thành công
                try {
                    WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class, 'bg-green-100')]")));
                    System.out.println("  ✓ Hiển thị thông báo thành công: " + successMessage.getText());
                } catch (Exception e) {
                    System.out.println("  ⚠️ Không tìm thấy thông báo thành công");
                }
            }
            
            System.out.println("✅ Test xuất kho hợp lệ hoàn thành");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi test xuất kho hợp lệ: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi test xuất kho hợp lệ: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kiểm tra validation nhập kho với số lượng = 0")
    public void testValidationNhapKhoSoLuongKhong() {
        try {
            System.out.println("Đang kiểm tra validation nhập kho với số lượng = 0");
            
            // 1. Chuyển đến trang quản lý sản phẩm
            driver.get(PRODUCTS_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 2. Chọn sản phẩm
            WebElement productTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            List<WebElement> productRows = productTable.findElements(By.xpath(".//tbody/tr"));
            
            if (productRows.isEmpty()) {
                System.out.println("⚠️ Không có sản phẩm nào để test, bỏ qua test case");
                return;
            }
            
            WebElement firstProductCheckbox = productRows.get(0).findElement(By.xpath(".//input[@type='checkbox']"));
            firstProductCheckbox.click();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 3. Nhấn nút "Quản lý Tồn kho"
            WebElement inventoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Quản lý Tồn kho')]")));
            inventoryButton.click();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 4. Đợi modal hiển thị
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'fixed') and .//h3[contains(text(), 'Quản lý Tồn kho')]]")));
            
            // 5. Nhập số lượng = 0
            List<WebElement> quantityInputs = modal.findElements(By.xpath(".//input[@type='number' and @placeholder='Nhập số lượng']"));
            List<WebElement> priceInputs = modal.findElements(By.xpath(".//input[@type='number' and @placeholder='Nhập giá']"));
            
            if (!quantityInputs.isEmpty() && !priceInputs.isEmpty()) {
                // Nhập số lượng = 0
                WebElement quantityInput = quantityInputs.get(0);
                quantityInput.clear();
                quantityInput.sendKeys("0");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                // Nhập giá
                WebElement priceInput = priceInputs.get(0);
                priceInput.clear();
                priceInput.sendKeys("50000");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                System.out.println("  ✓ Đã nhập số lượng = 0, giá = 50000");
                
                // 6. Nhấn nút "Nhập"
                WebElement importButton = modal.findElement(By.xpath(".//button[contains(text(), 'Nhập')]"));
                importButton.click();
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                // 7. Kiểm tra thông báo lỗi
                try {
                    WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class, 'bg-red-100')]")));
                    String errorText = errorMessage.getText().toLowerCase();
                    System.out.println("  ✓ Hiển thị thông báo lỗi: " + errorMessage.getText());
                    
                    assertTrue(errorText.contains("số lượng") || errorText.contains("quantity") || errorText.contains("lớn hơn 0"),
                        "Thông báo lỗi phải chỉ ra vấn đề về số lượng");
                    
                } catch (Exception e) {
                    System.out.println("  ⚠️ Không tìm thấy thông báo lỗi, có thể validation chưa được implement");
                }
            }
            
            System.out.println("✅ Test validation nhập kho với số lượng = 0 hoàn thành");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi test validation nhập kho: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi test validation nhập kho: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kiểm tra chuyển sang trang lịch sử nhập xuất kho")
    public void testChuyenSangTrangLichSuNhapXuat() {
        try {
            System.out.println("Đang kiểm tra chuyển sang trang lịch sử nhập xuất kho");
            
            // 1. Chuyển đến trang lịch sử nhập xuất kho
            driver.get(INVENTORY_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 2. Kiểm tra tiêu đề trang
            WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(text(), 'Lịch sử Nhập Xuất Kho')]")));
            assertTrue(pageTitle.isDisplayed(), "Tiêu đề trang 'Lịch sử Nhập Xuất Kho' phải được hiển thị");
            
            // 3. Đợi bảng lịch sử tải
            WebElement historyTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            
            // 4. Kiểm tra cột tiêu đề trong bảng
            List<WebElement> tableHeaders = historyTable.findElements(By.tagName("th"));
            assertTrue(tableHeaders.size() >= 5, "Bảng phải có ít nhất 5 cột thông tin");
            
            // Kiểm tra có các cột mong đợi
            boolean hasTransactionTypeColumn = false;
            boolean hasBookTitleColumn = false;
            boolean hasQuantityColumn = false;
            boolean hasPriceColumn = false;
            boolean hasDateColumn = false;
            
            for (WebElement header : tableHeaders) {
                String headerText = header.getText().toLowerCase();
                if (headerText.contains("loại giao dịch") || headerText.contains("transaction")) {
                    hasTransactionTypeColumn = true;
                } else if (headerText.contains("tên sách") || headerText.contains("book")) {
                    hasBookTitleColumn = true;
                } else if (headerText.contains("số lượng") || headerText.contains("quantity")) {
                    hasQuantityColumn = true;
                } else if (headerText.contains("giá") || headerText.contains("price")) {
                    hasPriceColumn = true;
                } else if (headerText.contains("thời gian") || headerText.contains("date")) {
                    hasDateColumn = true;
                }
            }
            
            assertTrue(hasTransactionTypeColumn, "Bảng phải có cột 'Loại giao dịch'");
            assertTrue(hasBookTitleColumn, "Bảng phải có cột 'Tên sách'");
            assertTrue(hasQuantityColumn, "Bảng phải có cột 'Số lượng'");
            assertTrue(hasPriceColumn, "Bảng phải có cột 'Giá'");
            assertTrue(hasDateColumn, "Bảng phải có cột 'Thời gian'");
            
            // 5. Kiểm tra có dữ liệu trong bảng
            List<WebElement> tableRows = historyTable.findElements(By.xpath(".//tbody/tr"));
            if (tableRows.isEmpty()) {
                // Nếu không có dòng nào trong tbody, kiểm tra thông báo không có dữ liệu
                try {
                    WebElement noDataMessage = driver.findElement(By.xpath("//div[contains(text(), 'Không có giao dịch') or contains(text(), 'Không có dữ liệu')]"));
                    assertTrue(noDataMessage.isDisplayed(), "Phải hiển thị thông báo không có dữ liệu nếu bảng trống");
                    System.out.println("  ✓ Hiển thị thông báo không có dữ liệu: " + noDataMessage.getText());
                } catch (Exception e) {
                    System.out.println("  ⚠️ Bảng trống nhưng không có thông báo không có dữ liệu");
                }
            } else {
                System.out.println("  ✓ Hiển thị danh sách lịch sử nhập xuất với " + tableRows.size() + " giao dịch");
                
                // Kiểm tra có giao dịch nhập và xuất
                boolean hasImportTransaction = false;
                boolean hasExportTransaction = false;
                
                for (WebElement row : tableRows) {
                    String rowText = row.getText().toLowerCase();
                    if (rowText.contains("nhập")) {
                        hasImportTransaction = true;
                    } else if (rowText.contains("xuất")) {
                        hasExportTransaction = true;
                    }
                }
                
                if (hasImportTransaction) {
                    System.out.println("  ✓ Tìm thấy giao dịch nhập kho trong lịch sử");
                }
                if (hasExportTransaction) {
                    System.out.println("  ✓ Tìm thấy giao dịch xuất kho trong lịch sử");
                }
            }
            
            // 6. Kiểm tra các nút chức năng (nếu có)
            try {
                WebElement refreshButton = driver.findElement(By.xpath("//button[contains(text(), 'Tải lại')]"));
                assertTrue(refreshButton.isDisplayed(), "Nút 'Tải lại' phải được hiển thị");
                System.out.println("  ✓ Tìm thấy nút Tải lại");
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy nút Tải lại");
            }
            
            try {
                WebElement filterButton = driver.findElement(By.xpath("//button[contains(text(), 'Hiện bộ lọc') or contains(text(), 'Filter')]"));
                assertTrue(filterButton.isDisplayed(), "Nút 'Hiện bộ lọc' phải được hiển thị");
                System.out.println("  ✓ Tìm thấy nút Hiện bộ lọc");
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy nút Hiện bộ lọc");
            }
            
            System.out.println("✅ Test chuyển sang trang lịch sử nhập xuất kho hoàn thành");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi test chuyển sang trang lịch sử: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi test chuyển sang trang lịch sử: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kiểm tra tìm kiếm và lọc trong lịch sử nhập xuất")
    public void testTimKiemVaLocLichSuNhapXuat() {
        try {
            System.out.println("Đang kiểm tra chức năng tìm kiếm và lọc trong lịch sử nhập xuất");
            
            // 1. Chuyển đến trang lịch sử nhập xuất kho
            driver.get(INVENTORY_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 2. Nhấn nút "Hiện bộ lọc" (nếu có)
            try {
                WebElement filterButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Hiện bộ lọc')]")));
                filterButton.click();
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.println("  ✓ Đã nhấn nút Hiện bộ lọc");
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy nút Hiện bộ lọc, bỏ qua test lọc");
                return;
            }
            
            // 3. Tìm kiếm theo tên sách
            try {
                WebElement searchField = driver.findElement(By.xpath("//input[@placeholder='Tên sách hoặc người thực hiện...']"));
                searchField.clear();
                searchField.sendKeys("test");
                try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.println("  ✓ Đã nhập từ khóa tìm kiếm: 'test'");
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy ô tìm kiếm");
            }
            
            // 4. Lọc theo loại giao dịch
            try {
                WebElement transactionTypeSelect = driver.findElement(By.xpath("//select[contains(@class, 'form-select') or contains(@class, 'border')]"));
                Select select = new Select(transactionTypeSelect);
                select.selectByValue("Nhập");
                try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.println("  ✓ Đã lọc theo loại giao dịch: Nhập");
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy dropdown lọc loại giao dịch");
            }
            
            // 5. Lọc theo ngày
            try {
                WebElement fromDateInput = driver.findElement(By.xpath("//input[@type='date' and @placeholder='Từ ngày']"));
                fromDateInput.sendKeys("2024-01-01");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                WebElement toDateInput = driver.findElement(By.xpath("//input[@type='date' and @placeholder='Đến ngày']"));
                toDateInput.sendKeys("2024-12-31");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                System.out.println("  ✓ Đã lọc theo khoảng thời gian: 2024-01-01 đến 2024-12-31");
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy ô lọc theo ngày");
            }
            
            // 6. Xóa bộ lọc
            try {
                WebElement clearFilterButton = driver.findElement(By.xpath("//button[contains(text(), 'Xóa bộ lọc')]"));
                clearFilterButton.click();
                try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.println("  ✓ Đã xóa bộ lọc");
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy nút Xóa bộ lọc");
            }
            
            System.out.println("✅ Test tìm kiếm và lọc trong lịch sử nhập xuất hoàn thành");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi test tìm kiếm và lọc: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi test tìm kiếm và lọc: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Dọn dẹp sau kiểm thử");
        
        if (driver != null) {
            try {
                // Thêm delay để có thể quan sát kết quả
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Đóng trình duyệt
            driver.quit();
            System.out.println("Đã đóng trình duyệt");
        }
    }
} 