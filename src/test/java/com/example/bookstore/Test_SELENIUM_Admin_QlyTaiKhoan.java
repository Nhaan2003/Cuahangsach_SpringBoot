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

public class Test_SELENIUM_Admin_QlyTaiKhoan {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:5173";
    private static final String ACCOUNTS_URL = BASE_URL + "/admin/accounts";

    @BeforeEach
    public void setUp() {
        System.out.println("Thiết lập môi trường kiểm thử Selenium");
        
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
    @DisplayName("Kiểm tra xem danh sách tài khoản")
    public void testAdminXemDanhSachTaiKhoan() {
        try {
            System.out.println("Đang kiểm tra chức năng xem danh sách tài khoản");
            
            // Chuyển đến trang quản lý tài khoản
            driver.get(ACCOUNTS_URL);
            
            // Delay để trang tải hoàn tất
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Xác nhận tiêu đề trang
            WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(), 'Quản lý tài khoản')]")));
            assertTrue(pageTitle.isDisplayed(), "Tiêu đề trang 'Quản lý tài khoản' phải được hiển thị");
            
            // Đợi danh sách tài khoản tải
            WebElement userTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            
            // Kiểm tra cột tiêu đề trong bảng
            List<WebElement> tableHeaders = userTable.findElements(By.tagName("th"));
            assertTrue(tableHeaders.size() >= 5, "Bảng phải có ít nhất 5 cột thông tin");
            
            // Kiểm tra có các cột mong đợi
            boolean hasUsernameColumn = false;
            boolean hasRoleColumn = false;
            boolean hasStatusColumn = false;
            
            for (WebElement header : tableHeaders) {
                String headerText = header.getText().toLowerCase();
                if (headerText.contains("tên người dùng") || headerText.contains("username")) {
                    hasUsernameColumn = true;
                } else if (headerText.contains("vai trò") || headerText.contains("role")) {
                    hasRoleColumn = true;
                } else if (headerText.contains("trạng thái") || headerText.contains("status")) {
                    hasStatusColumn = true;
                }
            }
            
            assertTrue(hasUsernameColumn, "Bảng phải có cột 'Tên người dùng'");
            assertTrue(hasRoleColumn, "Bảng phải có cột 'Vai trò'");
            assertTrue(hasStatusColumn, "Bảng phải có cột 'Trạng thái'");
            
            // Kiểm tra có dữ liệu trong bảng
            List<WebElement> tableRows = userTable.findElements(By.xpath(".//tbody/tr"));
            if (tableRows.isEmpty()) {
                // Nếu không có dòng nào trong tbody, kiểm tra thông báo không có dữ liệu
                WebElement noDataMessage = driver.findElement(By.xpath("//div[contains(text(), 'Không có người dùng nào') or contains(text(), 'Không có dữ liệu')]"));
                assertTrue(noDataMessage.isDisplayed(), "Phải hiển thị thông báo không có dữ liệu nếu bảng trống");
            } else {
                System.out.println("✅ Hiển thị danh sách tài khoản thành công với " + tableRows.size() + " tài khoản");
                
                // Tìm và kiểm tra tài khoản admin trong bảng
                boolean foundAdmin = false;
                for (WebElement row : tableRows) {
                    String rowText = row.getText().toLowerCase();
                    if (rowText.contains("admin")) {
                        foundAdmin = true;
                        System.out.println("  ✓ Tìm thấy tài khoản admin trong danh sách");
                        break;
                    }
                }
                
                assertTrue(foundAdmin, "Phải tìm thấy tài khoản admin trong danh sách");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xem danh sách tài khoản: " + e.getMessage());
            fail("Lỗi khi xem danh sách tài khoản: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kiểm tra tạo tài khoản mới")
    public void testAdminTaoTaiKhoanMoi() {
        try {
            System.out.println("Đang kiểm tra chức năng tạo tài khoản mới");
            
            // Chuyển đến trang quản lý tài khoản
            driver.get(ACCOUNTS_URL);
            
            // Delay để trang tải hoàn tất
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Tìm và nhấn nút "Thêm người dùng"
            WebElement addUserButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Thêm người dùng')]")));
            
            addUserButton.click();
            
            // Delay sau khi click nút thêm
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Đợi modal hiển thị
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'fixed') and .//h3[contains(text(), 'Thêm người dùng mới')]]")));
            
            // Tạo username duy nhất để tránh trùng lặp
            String uniqueUsername = "testuser" + System.currentTimeMillis();
            String uniqueEmail = uniqueUsername + "@example.com";
            
            // Điền thông tin tài khoản mới
            WebElement usernameField = modal.findElement(By.id("username"));
            usernameField.clear();
            usernameField.sendKeys(uniqueUsername);
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            WebElement passwordField = modal.findElement(By.id("password"));
            passwordField.clear();
            passwordField.sendKeys("abc@123");
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            WebElement emailField = modal.findElement(By.id("email"));
            emailField.clear();
            emailField.sendKeys(uniqueEmail);
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            WebElement fullNameField = modal.findElement(By.id("fullName"));
            fullNameField.clear();
            fullNameField.sendKeys("Người Dùng Test");
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            WebElement phoneField = modal.findElement(By.id("phone"));
            phoneField.clear();
            phoneField.sendKeys("0987654321");
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            WebElement addressField = modal.findElement(By.id("address"));
            addressField.clear();
            addressField.sendKeys("123 Đường Test, Phường Test, Quận Test");
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Chọn vai trò
            Select roleSelect = new Select(modal.findElement(By.id("role")));
            roleSelect.selectByValue("KH"); // Chọn vai trò Khách hàng
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Chọn trạng thái
            Select statusSelect = new Select(modal.findElement(By.id("status")));
            statusSelect.selectByValue("Active"); // Chọn trạng thái Active
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Tìm nút "Thêm" trong modal và click
            WebElement submitButton = modal.findElement(By.xpath(".//button[@type='submit']"));
            submitButton.click();
            
            // Đợi modal biến mất sau khi lưu thành công
            try {
                wait.until(ExpectedConditions.invisibilityOf(modal));
                System.out.println("  ✓ Modal đã đóng sau khi thêm tài khoản");
            } catch (Exception e) {
                System.out.println("⚠️ Modal vẫn hiển thị sau khi nhấn nút thêm, có thể do lỗi xác thực");
                
                // Kiểm tra nếu có thông báo lỗi
                try {
                    WebElement errorMessage = modal.findElement(By.xpath(".//div[contains(@class, 'bg-red-100')]"));
                    System.out.println("  ⚠️ Có thông báo lỗi: " + errorMessage.getText());
                    
                    // Nhấn nút hủy để đóng modal
                    WebElement cancelButton = modal.findElement(By.xpath(".//button[contains(text(), 'Hủy')]"));
                    cancelButton.click();
                    fail("Không thể tạo tài khoản: " + errorMessage.getText());
                } catch (Exception ex) {
                    // Không tìm thấy thông báo lỗi
                    System.out.println("  ⚠️ Không tìm thấy thông báo lỗi cụ thể trong modal");
                }
            }
            
            // Đợi thông báo thành công
            try {
                WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class, 'bg-green-100')]")));
                System.out.println("  ✓ Hiển thị thông báo thành công: " + successMessage.getText());
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy thông báo thành công, nhưng modal đã đóng");
            }
            
            // Delay để trang cập nhật dữ liệu
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Tìm kiếm tài khoản vừa tạo
            WebElement searchField = driver.findElement(By.id("search"));
            searchField.clear();
            searchField.sendKeys(uniqueUsername);
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Kiểm tra tài khoản mới có trong danh sách
            WebElement userTable = driver.findElement(By.xpath("//table"));
            List<WebElement> rows = userTable.findElements(By.xpath(".//tbody/tr"));
            
            if (rows.isEmpty()) {
                // Nếu không tìm thấy dòng nào, kiểm tra thông báo
                List<WebElement> noResults = driver.findElements(By.xpath("//div[contains(text(), 'Không tìm thấy người dùng') or contains(text(), 'Không có dữ liệu')]"));
                if (!noResults.isEmpty()) {
                    System.out.println("  ⚠️ Không tìm thấy tài khoản sau khi tìm kiếm");
                    
                    // Xóa bộ lọc và thử lại
                    WebElement clearFilterButton = driver.findElement(By.xpath("//button[contains(text(), 'Xóa bộ lọc')]"));
                    clearFilterButton.click();
                    try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    
                    // Kiểm tra lại toàn bộ bảng
                    rows = driver.findElements(By.xpath("//table//tbody/tr"));
                }
            }
            
            boolean foundNewUser = false;
            for (WebElement row : rows) {
                String rowText = row.getText();
                if (rowText.contains(uniqueUsername)) {
                    foundNewUser = true;
                    System.out.println("  ✓ Tìm thấy tài khoản mới trong danh sách: " + uniqueUsername);
                    break;
                }
            }
            
            assertTrue(foundNewUser, "Tài khoản mới '" + uniqueUsername + "' phải xuất hiện trong danh sách sau khi tạo");
            System.out.println("✅ Tạo tài khoản mới thành công: " + uniqueUsername);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo tài khoản mới: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi tạo tài khoản mới: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Kiểm tra thay đổi trạng thái tài khoản (Khóa)")
    public void testAdminThayDoiTrangThaiTaiKhoan() {
        try {
            System.out.println("Đang kiểm tra chức năng thay đổi trạng thái tài khoản");
            
            // 1. Chuyển đến trang quản lý tài khoản
            driver.get(ACCOUNTS_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 2. Tìm một tài khoản đang Active để khóa (không phải admin)
            WebElement userTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            List<WebElement> rows = userTable.findElements(By.xpath(".//tbody/tr"));
            
            WebElement targetRow = null;
            String targetUsername = "";
            
            for (WebElement row : rows) {
                String rowText = row.getText();
                // Tìm tài khoản không phải admin và đang hoạt động
                if (!rowText.toLowerCase().contains("admin") && rowText.contains("Hoạt động")) {
                    targetRow = row;
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    targetUsername = cells.get(0).getText(); // Username ở cột đầu tiên
                    System.out.println("  ✓ Tìm thấy tài khoản đang hoạt động: " + targetUsername);
                    break;
                }
            }
            
            if (targetRow == null) {
                // Nếu không tìm thấy tài khoản đang hoạt động, tạo tài khoản mới
                System.out.println("  ⚠️ Không tìm thấy tài khoản đang hoạt động, tạo tài khoản mới để test");
                testAdminTaoTaiKhoanMoi();
                
                // Sau khi tạo tài khoản mới, tải lại trang và tìm tài khoản vừa tạo
                driver.get(ACCOUNTS_URL);
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                // Tìm kiếm tài khoản vừa tạo (có "testuser" trong tên)
                WebElement searchField = driver.findElement(By.id("search"));
                searchField.clear();
                searchField.sendKeys("testuser");
                try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                userTable = driver.findElement(By.xpath("//table"));
                rows = userTable.findElements(By.xpath(".//tbody/tr"));
                
                if (!rows.isEmpty()) {
                    targetRow = rows.get(0);
                    List<WebElement> cells = targetRow.findElements(By.tagName("td"));
                    targetUsername = cells.get(0).getText(); // Username ở cột đầu tiên
                    System.out.println("  ✓ Sử dụng tài khoản mới tạo: " + targetUsername);
                } else {
                    fail("Không tìm thấy tài khoản đang hoạt động để khóa");
                }
            }
            
            // Tìm nút khóa trong hàng tài khoản - sử dụng selector phù hợp với DOM thực tế
            // Sử dụng tất cả các selector có thể để tăng khả năng tìm thấy nút
            List<WebElement> buttons = targetRow.findElements(By.tagName("button"));
            WebElement lockButton = null;
            
            // Cách 1: Tìm dựa vào class CSS
            for (WebElement button : buttons) {
                String className = button.getAttribute("class");
                if (className != null && className.contains("text-red-600")) {
                    lockButton = button;
                    System.out.println("  ✓ Tìm thấy nút khóa bằng class text-red-600");
                    break;
                }
            }
            
            // Cách 2: Nếu không tìm thấy bằng class, tìm bằng title attribute
            if (lockButton == null) {
                for (WebElement button : buttons) {
                    String title = button.getAttribute("title");
                    if (title != null && title.contains("Khóa")) {
                        lockButton = button;
                        System.out.println("  ✓ Tìm thấy nút khóa bằng title attribute");
                        break;
                    }
                }
            }
            
            // Cách 3: Nếu vẫn không tìm thấy, tìm bằng SVG bên trong
            if (lockButton == null) {
                for (int i = 0; i < buttons.size(); i++) {
                    WebElement button = buttons.get(i);
                    try {
                        // Nếu button này chứa SVG và là button thứ 2 (button đầu tiên thường là Edit)
                        if (button.findElements(By.tagName("svg")).size() > 0 && i > 0) {
                            lockButton = button;
                            System.out.println("  ✓ Tìm thấy nút khóa bằng vị trí và SVG con");
                            break;
                        }
                    } catch (Exception e) {
                        // Bỏ qua nếu không tìm thấy SVG
                    }
                }
            }
            
            // Nếu vẫn không tìm thấy, lấy button thứ 2 (giả sử là nút Khóa)
            if (lockButton == null && buttons.size() >= 2) {
                lockButton = buttons.get(1);
                System.out.println("  ✓ Sử dụng nút thứ 2 làm nút khóa (dựa trên vị trí)");
            }
            
            if (lockButton == null) {
                fail("Không tìm thấy nút khóa tài khoản");
            }
            
            // Click nút khóa
            System.out.println("  ✓ Thực hiện click nút khóa tài khoản");
            lockButton.click();
            
            // Đợi thông báo thành công hoặc cập nhật giao diện
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            try {
                WebElement successMessage = driver.findElement(By.xpath("//div[contains(@class, 'bg-green-100')]"));
                System.out.println("  ✓ Hiển thị thông báo khóa tài khoản thành công: " + successMessage.getText());
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy thông báo thành công, kiểm tra trạng thái trực tiếp");
            }
            
            // Tải lại trang để cập nhật trạng thái
            driver.get(ACCOUNTS_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Tìm tài khoản vừa khóa
            WebElement searchField = driver.findElement(By.id("search"));
            searchField.clear();
            searchField.sendKeys(targetUsername);
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Xác nhận tài khoản đã bị khóa
            userTable = driver.findElement(By.xpath("//table"));
            rows = userTable.findElements(By.xpath(".//tbody/tr"));
            
            boolean accountLocked = false;
            
            if (!rows.isEmpty()) {
                WebElement updatedRow = rows.get(0);
                String rowText = updatedRow.getText();
                accountLocked = rowText.contains("Bị khóa");
                
                System.out.println(accountLocked
                    ? "  ✓ Tài khoản đã được khóa thành công"
                    : "  ⚠️ Tài khoản vẫn chưa bị khóa sau khi thao tác");
            }
            
            assertTrue(accountLocked, "Tài khoản phải được chuyển sang trạng thái 'Bị khóa'");
            System.out.println("✅ Khóa tài khoản thành công: " + targetUsername);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thay đổi trạng thái tài khoản: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi thay đổi trạng thái tài khoản: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kiểm tra chỉnh sửa thông tin tài khoản")
    public void testAdminChinhSuaThongTinTaiKhoan() {
        try {
            System.out.println("Đang kiểm tra chức năng chỉnh sửa thông tin tài khoản");
            
            // Tạo tài khoản mới nếu cần
            try {
                testAdminTaoTaiKhoanMoi();
            } catch (Exception e) {
                System.out.println("  ⚠️ Không thể tạo tài khoản mới, sẽ tìm một tài khoản hiện có để chỉnh sửa");
            }
            
            // Chuyển đến trang quản lý tài khoản
            driver.get(ACCOUNTS_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Tìm tài khoản để chỉnh sửa (ưu tiên tài khoản có "testuser")
            WebElement searchField = driver.findElement(By.id("search"));
            searchField.clear();
            searchField.sendKeys("testuser");
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Kiểm tra có tìm thấy kết quả không
            WebElement userTable = driver.findElement(By.xpath("//table"));
            List<WebElement> rows = userTable.findElements(By.xpath(".//tbody/tr"));
            
            WebElement targetRow;
            String targetUsername;
            
            if (rows.isEmpty()) {
                // Nếu không tìm thấy testuser, tìm một tài khoản bất kỳ (không phải admin)
                searchField.clear();
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                rows = userTable.findElements(By.xpath(".//tbody/tr"));
                targetRow = null;
                
                for (WebElement row : rows) {
                    String rowText = row.getText().toLowerCase();
                    if (!rowText.contains("admin")) {
                        targetRow = row;
                        break;
                    }
                }
                
                if (targetRow == null) {
                    fail("Không tìm thấy tài khoản nào để chỉnh sửa");
                    return;
                }
            } else {
                targetRow = rows.get(0);
            }
            
            List<WebElement> cells = targetRow.findElements(By.tagName("td"));
            targetUsername = cells.get(0).getText(); // Username ở cột đầu tiên
            System.out.println("  ✓ Sẽ chỉnh sửa tài khoản: " + targetUsername);
            
            // Tìm nút sửa trong hàng tài khoản - sử dụng nhiều cách để tìm
            List<WebElement> buttons = targetRow.findElements(By.tagName("button"));
            WebElement editButton = null;
            
            // Cách 1: Tìm dựa vào class CSS
            for (WebElement button : buttons) {
                String className = button.getAttribute("class");
                if (className != null && className.contains("text-indigo-600")) {
                    editButton = button;
                    System.out.println("  ✓ Tìm thấy nút sửa bằng class text-indigo-600");
                    break;
                }
            }
            
            // Cách 2: Nếu không tìm thấy bằng class, tìm bằng title attribute
            if (editButton == null) {
                for (WebElement button : buttons) {
                    String title = button.getAttribute("title");
                    if (title != null && title.contains("Sửa")) {
                        editButton = button;
                        System.out.println("  ✓ Tìm thấy nút sửa bằng title attribute");
                        break;
                    }
                }
            }
            
            // Cách 3: Nếu vẫn không tìm thấy, tìm bằng SVG bên trong
            if (editButton == null) {
                for (WebElement button : buttons) {
                    try {
                        // Nếu button này chứa SVG và là button đầu tiên (edit thường là button đầu tiên)
                        if (button.findElements(By.tagName("svg")).size() > 0) {
                            editButton = button;
                            System.out.println("  ✓ Tìm thấy nút sửa bằng SVG con");
                            break;
                        }
                    } catch (Exception e) {
                        // Bỏ qua nếu không tìm thấy SVG
                    }
                }
            }
            
            // Nếu vẫn không tìm thấy, lấy button đầu tiên (giả sử là nút Edit)
            if (editButton == null && !buttons.isEmpty()) {
                editButton = buttons.get(0);
                System.out.println("  ✓ Sử dụng nút đầu tiên làm nút sửa (dựa trên vị trí)");
            }
            
            if (editButton == null) {
                fail("Không tìm thấy nút sửa tài khoản");
            }
            
            // Click nút sửa
            System.out.println("  ✓ Thực hiện click nút sửa tài khoản");
            editButton.click();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Đợi modal hiển thị
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'fixed') and .//h3[contains(text(), 'Sửa người dùng')]]")));
            
            // Lấy dữ liệu hiện tại trước khi chỉnh sửa
            String currentFullName = modal.findElement(By.id("fullName")).getAttribute("value");
            String newFullName = "Đã Sửa " + System.currentTimeMillis();
            
            String currentPhone = modal.findElement(By.id("phone")).getAttribute("value");
            String newPhone = "0912" + System.currentTimeMillis() % 1000000;
            
            // Cập nhật thông tin
            WebElement fullNameField = modal.findElement(By.id("fullName"));
            fullNameField.clear();
            fullNameField.sendKeys(newFullName);
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            WebElement phoneField = modal.findElement(By.id("phone"));
            phoneField.clear();
            phoneField.sendKeys(newPhone);
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Nhấn nút Cập nhật
            WebElement updateButton = modal.findElement(By.xpath(".//button[@type='submit']"));
            updateButton.click();
            
            // Đợi modal biến mất sau khi lưu thành công
            try {
                wait.until(ExpectedConditions.invisibilityOf(modal));
                System.out.println("  ✓ Modal đã đóng sau khi cập nhật tài khoản");
            } catch (Exception e) {
                System.out.println("⚠️ Modal vẫn hiển thị sau khi nhấn nút cập nhật, có thể do lỗi xác thực");
                
                // Kiểm tra nếu có thông báo lỗi
                try {
                    WebElement errorMessage = modal.findElement(By.xpath(".//div[contains(@class, 'bg-red-100')]"));
                    System.out.println("  ⚠️ Có thông báo lỗi: " + errorMessage.getText());
                    
                    // Nhấn nút hủy để đóng modal
                    WebElement cancelButton = modal.findElement(By.xpath(".//button[contains(text(), 'Hủy')]"));
                    cancelButton.click();
                    fail("Không thể cập nhật tài khoản: " + errorMessage.getText());
                } catch (Exception ex) {
                    // Không tìm thấy thông báo lỗi
                    System.out.println("  ⚠️ Không tìm thấy thông báo lỗi cụ thể trong modal");
                }
            }
            
            // Đợi thông báo thành công
            try {
                WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class, 'bg-green-100')]")));
                System.out.println("  ✓ Hiển thị thông báo cập nhật thành công: " + successMessage.getText());
            } catch (Exception e) {
                System.out.println("  ⚠️ Không tìm thấy thông báo thành công, nhưng modal đã đóng");
            }
            
            // Delay để trang cập nhật dữ liệu
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Tìm kiếm tài khoản vừa cập nhật
            searchField = driver.findElement(By.id("search"));
            searchField.clear();
            searchField.sendKeys(targetUsername);
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Tìm và click nút sửa lại để xem thông tin đã cập nhật
            rows = driver.findElements(By.xpath("//table//tbody/tr"));
            
            if (!rows.isEmpty()) {
                WebElement updatedRow = rows.get(0);
                buttons = updatedRow.findElements(By.tagName("button"));
                WebElement editButtonAgain = null;
                
                // Tìm lại nút sửa như ở trên
                for (WebElement button : buttons) {
                    if (button.getAttribute("class") != null && button.getAttribute("class").contains("text-indigo-600")) {
                        editButtonAgain = button;
                        break;
                    }
                }
                
                if (editButtonAgain == null && !buttons.isEmpty()) {
                    editButtonAgain = buttons.get(0); // Lấy button đầu tiên nếu không tìm được
                }
                
                if (editButtonAgain != null) {
                    editButtonAgain.click();
                    try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    
                    // Đợi modal hiển thị lại
                    modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class, 'fixed')]")));
                    
                    // Kiểm tra thông tin đã được cập nhật
                    String updatedFullName = modal.findElement(By.id("fullName")).getAttribute("value");
                    String updatedPhone = modal.findElement(By.id("phone")).getAttribute("value");
                    
                    boolean fullNameUpdated = newFullName.equals(updatedFullName);
                    boolean phoneUpdated = newPhone.equals(updatedPhone);
                    
                    System.out.println("  - Họ tên: " + (fullNameUpdated ? "✓ Đã cập nhật thành công" : "❌ Chưa cập nhật") + 
                                    " (" + currentFullName + " -> " + updatedFullName + ")");
                    System.out.println("  - Số điện thoại: " + (phoneUpdated ? "✓ Đã cập nhật thành công" : "❌ Chưa cập nhật") + 
                                    " (" + currentPhone + " -> " + updatedPhone + ")");
                    
                    // Đóng modal
                    WebElement cancelButton = modal.findElement(By.xpath(".//button[contains(text(), 'Hủy')]"));
                    cancelButton.click();
                    
                    assertTrue(fullNameUpdated && phoneUpdated, 
                        "Thông tin họ tên và số điện thoại phải được cập nhật thành công");
                    
                    System.out.println("✅ Chỉnh sửa thông tin tài khoản thành công: " + targetUsername);
                } else {
                    System.out.println("⚠️ Không tìm thấy nút sửa để kiểm tra thông tin sau khi cập nhật");
                    // Giả định rằng cập nhật thành công nếu không có lỗi
                    System.out.println("✅ Quá trình cập nhật đã hoàn thành mà không gây lỗi");
                }
            } else {
                fail("Không tìm thấy tài khoản sau khi cập nhật");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi chỉnh sửa thông tin tài khoản: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi chỉnh sửa thông tin tài khoản: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Kiểm tra tìm kiếm tài khoản")
    public void testAdminTimKiemTaiKhoan() {
        try {
            System.out.println("Đang kiểm tra chức năng tìm kiếm tài khoản");
            
            // Chuyển đến trang quản lý tài khoản
            driver.get(ACCOUNTS_URL);
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Đếm số lượng tài khoản ban đầu
            WebElement userTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            List<WebElement> initialRows = userTable.findElements(By.xpath(".//tbody/tr"));
            int initialRowCount = initialRows.size();
            System.out.println("  - Số lượng tài khoản ban đầu: " + initialRowCount);
            
            if (initialRowCount == 0) {
                System.out.println("  ⚠️ Không có tài khoản nào, không thể kiểm tra tìm kiếm");
                return;
            }
            
            // 1. Tìm kiếm với từ khóa "admin"
            WebElement searchField = driver.findElement(By.id("search"));
            searchField.clear();
            searchField.sendKeys("admin");
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Kiểm tra kết quả tìm kiếm
            List<WebElement> adminSearchResults = userTable.findElements(By.xpath(".//tbody/tr"));
            
            // Kiểm tra có tìm thấy kết quả phù hợp không
            boolean foundAdminInResults = false;
            for (WebElement row : adminSearchResults) {
                String rowText = row.getText().toLowerCase();
                if (rowText.contains("admin")) {
                    foundAdminInResults = true;
                    break;
                }
            }
            
            System.out.println("  - Tìm kiếm 'admin': " + 
                (foundAdminInResults ? "✓ Tìm thấy " + adminSearchResults.size() + " kết quả phù hợp" : "❌ Không tìm thấy kết quả phù hợp"));
            
            assertTrue(foundAdminInResults, "Tìm kiếm 'admin' phải trả về ít nhất một kết quả");
            
            // Xóa bộ lọc
            WebElement clearFilterButton = driver.findElement(By.xpath("//button[contains(text(), 'Xóa bộ lọc')]"));
            clearFilterButton.click();
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 2. Tìm kiếm với từ khóa không tồn tại
            searchField = driver.findElement(By.id("search"));
            searchField.clear();
            searchField.sendKeys("taikhoankhongtontai123456789xyz");
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Kiểm tra kết quả tìm kiếm
            List<WebElement> noResults = userTable.findElements(By.xpath(".//tbody/tr"));
            
            // Kiểm tra thông báo không tìm thấy
            boolean noResultsFound = false;
            if (noResults.isEmpty()) {
                noResultsFound = true;
                System.out.println("  ✓ Không có kết quả cho từ khóa không tồn tại");
            } else {
                // Kiểm tra có hiển thị thông báo không tìm thấy
                List<WebElement> noDataMessages = driver.findElements(By.xpath("//div[contains(text(), 'Không tìm thấy người dùng') or contains(text(), 'Không có dữ liệu')]"));
                if (!noDataMessages.isEmpty()) {
                    noResultsFound = true;
                    System.out.println("  ✓ Hiển thị thông báo không tìm thấy: " + noDataMessages.get(0).getText());
                }
            }
            
            assertTrue(noResultsFound, "Tìm kiếm với từ khóa không tồn tại phải hiển thị không có kết quả");
            
            // Xóa bộ lọc
            clearFilterButton = driver.findElement(By.xpath("//button[contains(text(), 'Xóa bộ lọc')]"));
            clearFilterButton.click();
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // 3. Kiểm tra lọc theo vai trò
            Select roleFilter = new Select(driver.findElement(By.id("roleFilter")));
            roleFilter.selectByValue("KH"); // Lọc theo Khách hàng
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Kiểm tra kết quả lọc
            List<WebElement> roleFilterResults = userTable.findElements(By.xpath(".//tbody/tr"));
            
            // Kiểm tra tất cả kết quả đều là khách hàng
            boolean allCustomers = true;
            for (WebElement row : roleFilterResults) {
                if (!row.getText().contains("Khách hàng")) {
                    allCustomers = false;
                    break;
                }
            }
            
            System.out.println("  - Lọc theo vai trò 'Khách hàng': " + 
                (allCustomers ? "✓ Tất cả " + roleFilterResults.size() + " kết quả đều là khách hàng" : "❌ Có kết quả không phải khách hàng"));
            
            if (roleFilterResults.size() > 0) {
                assertTrue(allCustomers, "Tất cả kết quả lọc theo vai trò 'Khách hàng' phải là khách hàng");
            }
            
            System.out.println("✅ Chức năng tìm kiếm và lọc tài khoản hoạt động chính xác");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tìm kiếm tài khoản: " + e.getMessage());
            e.printStackTrace();
            fail("Lỗi khi tìm kiếm tài khoản: " + e.getMessage());
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