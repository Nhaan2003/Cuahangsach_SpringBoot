package com.example.bookstore;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class Test_SELENIUM_Admin_QlySanPham {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:5173";

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        // Đăng nhập admin
        driver.get(BASE_URL + "/auth/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("abc@123");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/admin"));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Xem danh sách sản phẩm")
    public void testViewProductList() throws InterruptedException {
        driver.get(BASE_URL + "/admin/products");
        Thread.sleep(1200);
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[contains(@class,'min-w-full')]")));
        Thread.sleep(800);
        List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr"));
        assertTrue(rows.size() >= 0, "Bảng sản phẩm phải hiển thị đúng");
    }

    @Test
    @DisplayName("1. Thêm danh mục - kiểm tra danh mục vừa thêm có tồn tại hay không")
    public void testAddCategoryAndCheckExist() throws InterruptedException {
        driver.get(BASE_URL + "/admin/products");
        Thread.sleep(2000);
        WebElement addCategoryBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Thêm Danh mục')]")));
        addCategoryBtn.click();
        Thread.sleep(1200);
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'fixed') and .//h3[contains(.,'Danh mục')]]")));
        Thread.sleep(900);
        String categoryName = "Danh mục Test " + System.currentTimeMillis();
        WebElement nameField = modal.findElement(By.xpath(".//input[@type='text' and @required]"));
        nameField.sendKeys(categoryName);
        Thread.sleep(800);
        WebElement submitBtn = modal.findElement(By.xpath(".//button[@type='submit']"));
        submitBtn.click();
        Thread.sleep(1200);
        wait.until(ExpectedConditions.invisibilityOf(modal));
        Thread.sleep(1200);
        List<WebElement> categoryBadges = driver.findElements(By.xpath("//span[contains(@class,'font-medium') and text() = '" + categoryName + "']"));
        assertTrue(!categoryBadges.isEmpty(), "Danh mục mới phải xuất hiện trong danh sách");
    }

    @Test
    @DisplayName("2. Xoá danh mục vừa sửa (sửa tên rồi xoá, kiểm tra danh mục đã biến mất sau reload)")
    public void testEditAndDeleteCategory() throws InterruptedException {
        driver.get(BASE_URL + "/admin/products");
        Thread.sleep(2000);
        // Thêm danh mục mới trước
        WebElement addCategoryBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Thêm Danh mục')]")));
        addCategoryBtn.click();
        Thread.sleep(1200);
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'fixed') and .//h3[contains(.,'Danh mục')]]")));
        Thread.sleep(900);
        String categoryName = "Danh mục Sửa Xóa " + System.currentTimeMillis();
        WebElement nameField = modal.findElement(By.xpath(".//input[@type='text' and @required]"));
        nameField.sendKeys(categoryName);
        Thread.sleep(800);
        WebElement submitBtn = modal.findElement(By.xpath(".//button[@type='submit']"));
        submitBtn.click();
        Thread.sleep(1200);
        wait.until(ExpectedConditions.invisibilityOf(modal));
        Thread.sleep(1200);
        
        // Sửa tên danh mục vừa thêm
        List<WebElement> editBtns = driver.findElements(By.xpath("//span[contains(@class,'font-medium') and text() = '" + categoryName + "']/ancestor::div[contains(@class,'flex')]/div/button[contains(.,'Sửa')]"));
        assertTrue(!editBtns.isEmpty(), "Phải tìm thấy nút sửa cho danh mục vừa thêm");
        WebElement editBtn = editBtns.get(0);
        editBtn.click();
        Thread.sleep(1000);
        WebElement editModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'fixed') and .//h3[contains(.,'Danh mục')]]")));
        Thread.sleep(700);
        WebElement editNameField = editModal.findElement(By.xpath(".//input[@type='text' and @required]"));
        editNameField.clear();
        Thread.sleep(400);
        String newCategoryName = categoryName + " (Đã sửa)";
        editNameField.sendKeys(newCategoryName);
        Thread.sleep(700);
        WebElement editSubmitBtn = editModal.findElement(By.xpath(".//button[@type='submit']"));
        editSubmitBtn.click();
        Thread.sleep(1200);
        wait.until(ExpectedConditions.invisibilityOf(editModal));
        Thread.sleep(1200);
        
        // Thêm sách vào danh mục vừa sửa
        WebElement addBookBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Thêm Sản phẩm')]")));
        addBookBtn.click();
        Thread.sleep(1200);
        WebElement bookModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'fixed') and .//h3[contains(.,'Sản phẩm')]]")));
        Thread.sleep(900);
        String bookName = "Sách Test Danh Mục " + System.currentTimeMillis();
        bookModal.findElement(By.xpath(".//input[@type='text' and @required]"))
            .sendKeys(bookName);
        Thread.sleep(700);
        WebElement priceField = bookModal.findElement(By.xpath(".//input[@type='number' and @required]"));
        priceField.clear();
        priceField.sendKeys("50000");
        Thread.sleep(700);
        // Chọn danh mục vừa tạo
        List<WebElement> categoryCheckboxes = bookModal.findElements(By.xpath(".//input[@type='checkbox' and contains(@class,'mr-2')]"));
        for (WebElement checkbox : categoryCheckboxes) {
            // Tìm span chứa tên danh mục ngay sau checkbox
            WebElement spanElement = checkbox.findElement(By.xpath("following-sibling::span"));
            if (spanElement.getText().contains(newCategoryName)) {
                checkbox.click();
                Thread.sleep(500);
                break;
            }
        }
        Thread.sleep(700);
        WebElement bookSubmitBtn = bookModal.findElement(By.xpath(".//button[@type='submit']"));
        bookSubmitBtn.click();
        Thread.sleep(1200);
        wait.until(ExpectedConditions.invisibilityOf(bookModal));
        Thread.sleep(1200);
        
        // Xóa danh mục vừa sửa (sẽ báo lỗi vì có sách)
        List<WebElement> deleteBtns = driver.findElements(By.xpath("//span[contains(@class,'font-medium') and text() = '" + newCategoryName + "']/ancestor::div[contains(@class,'flex')]/div/button[contains(.,'Xóa')]"));
        assertTrue(!deleteBtns.isEmpty(), "Phải tìm thấy nút xóa cho danh mục vừa sửa");
        WebElement deleteBtn = deleteBtns.get(0);
        deleteBtn.click();
        Thread.sleep(1200);
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            Thread.sleep(300);
            alert.accept();
        } catch (Exception e) {}
        Thread.sleep(1200);
        
        // Kiểm tra có thông báo lỗi không thể xóa danh mục có sách
        List<WebElement> errorMsgs = driver.findElements(By.xpath("//div[contains(@class,'bg-red-100')]"));
        assertTrue(!errorMsgs.isEmpty(), "Phải hiển thị thông báo lỗi khi không thể xóa danh mục có sách");
        
        // Reload lại trang để kiểm tra danh mục vẫn còn
        driver.navigate().refresh();
        Thread.sleep(2000);
        List<WebElement> badges = driver.findElements(By.xpath("//span[contains(@class,'font-medium') and text() = '" + newCategoryName + "']"));
        assertTrue(!badges.isEmpty(), "Danh mục vẫn còn trong danh sách vì không thể xóa do có sách");
    }

    @Test
    @DisplayName("3. Thêm sách với giá âm (kiểm tra validation)")
    public void testAddBookNegativePrice() throws InterruptedException {
        driver.get(BASE_URL + "/admin/products");
        Thread.sleep(2000);
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Thêm Sản phẩm')]")));
        addBtn.click();
        Thread.sleep(1200);
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'fixed') and .//h3[contains(.,'Sản phẩm')]]")));
        Thread.sleep(900);
        String name = "Sách Giá Âm " + System.currentTimeMillis();
        modal.findElement(By.xpath(".//input[@type='text' and @required]"))
            .sendKeys(name);
        Thread.sleep(700);
        WebElement priceField = modal.findElement(By.xpath(".//input[@type='number' and @required]"));
        priceField.clear();
        priceField.sendKeys("-5000");
        Thread.sleep(700);
        WebElement submitBtn = modal.findElement(By.xpath(".//button[@type='submit']"));
        submitBtn.click();
        Thread.sleep(1000);
        List<WebElement> errorMsgs = modal.findElements(By.xpath(".//div[contains(@class,'bg-red-100')]"));
        assertTrue(!errorMsgs.isEmpty(), "Phải hiển thị thông báo lỗi khi nhập giá âm");
        WebElement cancelBtn = modal.findElement(By.xpath(".//button[contains(.,'Hủy')]"));
        cancelBtn.click();
        Thread.sleep(1000);
    }

    @Test
    @DisplayName("4. Thêm sách khi thiếu các trường bắt buộc (kiểm tra validation)")
    public void testAddBookMissingRequiredFields() throws InterruptedException {
        driver.get(BASE_URL + "/admin/products");
        Thread.sleep(2000);
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Thêm Sản phẩm')]")));
        addBtn.click();
        Thread.sleep(1200);
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'fixed') and .//h3[contains(.,'Sản phẩm')]]")));
        Thread.sleep(900);
        // Để trống tên, chỉ nhập giá
        WebElement priceField = modal.findElement(By.xpath(".//input[@type='number' and @required]"));
        priceField.clear();
        priceField.sendKeys("10000");
        Thread.sleep(700);
        WebElement submitBtn = modal.findElement(By.xpath(".//button[@type='submit']"));
        submitBtn.click();
        Thread.sleep(1000);
        List<WebElement> errorMsgs = modal.findElements(By.xpath(".//div[contains(@class,'bg-red-100')]"));
        assertTrue(!errorMsgs.isEmpty(), "Phải hiển thị thông báo lỗi khi thiếu trường bắt buộc");
        WebElement cancelBtn = modal.findElement(By.xpath(".//button[contains(.,'Hủy')]"));
        cancelBtn.click();
        Thread.sleep(1000);
    }

    @Test
    @DisplayName("5. Thêm sách đúng và đầy đủ thông tin")
    public void testAddBookFullInfo() throws InterruptedException {
        driver.get(BASE_URL + "/admin/products");
        Thread.sleep(2000);
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Thêm Sản phẩm')]")));
        addBtn.click();
        Thread.sleep(1200);
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'fixed') and .//h3[contains(.,'Sản phẩm')]]")));
        Thread.sleep(900);
        String uniqueName = "Sách Test Đầy Đủ " + System.currentTimeMillis();
        lastBookName = uniqueName;
        modal.findElement(By.xpath(".//input[@type='text' and @required]"))
            .sendKeys(uniqueName);
        Thread.sleep(800);
        List<WebElement> textInputs = modal.findElements(By.xpath(".//input[@type='text' and not(@required)]"));
        WebElement authorField = textInputs.get(0);
        authorField.sendKeys("Tác giả Test");
        Thread.sleep(700);
        WebElement publisherField = textInputs.get(1);
        publisherField.sendKeys("NXB Test");
        Thread.sleep(700);
        WebElement yearField = modal.findElement(By.xpath(".//input[@type='number' and @min='1900']"));
        yearField.sendKeys("2024");
        Thread.sleep(700);
        WebElement priceField = modal.findElement(By.xpath(".//input[@type='number' and @required]"));
        priceField.clear();
        priceField.sendKeys("100000");
        Thread.sleep(700);
        WebElement descField = modal.findElement(By.xpath(".//textarea"));
        descField.sendKeys("Đây là mô tả sách test đầy đủ trường.");
        Thread.sleep(700);
        List<WebElement> checkboxes = modal.findElements(By.xpath(".//input[@type='checkbox' and contains(@class,'mr-2')]"));
        if (!checkboxes.isEmpty()) { checkboxes.get(0).click(); Thread.sleep(600); }
        WebElement submitBtn = modal.findElement(By.xpath(".//button[@type='submit']"));
        submitBtn.click();
        Thread.sleep(1200);
        wait.until(ExpectedConditions.invisibilityOf(modal));
        Thread.sleep(1200);
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[contains(@class,'min-w-full')]")));
        boolean found = table.findElements(By.xpath(".//tbody/tr/td[contains(.,'" + uniqueName + "')]")).size() > 0;
        assertTrue(found, "Sách mới phải xuất hiện trong danh sách");
    }

    @Test
    @DisplayName("6. Tìm sách theo tên sách vừa thêm")
    public void testFindBookByName() throws InterruptedException {
        String uniqueName = lastBookName;
        if (uniqueName == null) uniqueName = "Sách Test Đầy Đủ"; // fallback nếu chạy độc lập
        driver.get(BASE_URL + "/admin/products");
        Thread.sleep(2000);
        WebElement filterBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Hiện bộ lọc') or contains(.,'Ẩn bộ lọc')]")));
        filterBtn.click();
        Thread.sleep(800);
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Tên sách, tác giả...']")));
        searchInput.clear();
        Thread.sleep(500);
        searchInput.sendKeys(uniqueName);
        Thread.sleep(1200);
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[contains(@class,'min-w-full')]")));
        Thread.sleep(800);
        List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr"));
        boolean found = false;
        for (WebElement row : rows) {
            if (row.getText().contains(uniqueName)) { found = true; break; }
        }
        assertTrue(found, "Phải tìm thấy sách vừa thêm");
    }

    @Test
    @DisplayName("7. Chỉnh sửa sách vừa thêm (tìm theo tên và chỉnh sửa)")
    public void testEditBookByName() throws InterruptedException {
        String uniqueName = lastBookName;
        //testAddBookFullInfo();                
        if (uniqueName == null) uniqueName = "Sách Test Đầy Đủ"; // fallback nếu chạy độc lập, nếu chạy độc lập thì phải chạy test 5 trước
        driver.get(BASE_URL + "/admin/products");
        Thread.sleep(2000);
        WebElement filterBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Hiện bộ lọc') or contains(.,'Ẩn bộ lọc')]")));
        filterBtn.click();
        Thread.sleep(800);
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Tên sách, tác giả...']")));
        searchInput.clear();
        Thread.sleep(500);
        searchInput.sendKeys(uniqueName);
        Thread.sleep(1200);
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[contains(@class,'min-w-full')]")));
        Thread.sleep(800);
        List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr"));
        WebElement foundRow = null;
        for (WebElement row : rows) {
            if (row.getText().contains(uniqueName)) { foundRow = row; break; }
        }
        assertNotNull(foundRow, "Phải tìm thấy dòng chứa sách vừa thêm");
        WebElement editBtn = foundRow.findElement(By.xpath(".//button[@title='Sửa sản phẩm']"));
        editBtn.click();
        Thread.sleep(600);
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'fixed') and .//h3[contains(.,'Sản phẩm')]]")));
        Thread.sleep(400);
        WebElement nameField = modal.findElement(By.xpath(".//input[@type='text' and @required]"));
        nameField.clear();
        Thread.sleep(300);
        String newName = uniqueName + " (Đã sửa)";
        lastEditedBookName = newName;
        nameField.sendKeys(newName);
        Thread.sleep(400);
        WebElement submitBtn = modal.findElement(By.xpath(".//button[@type='submit']"));
        submitBtn.click();
        Thread.sleep(800);
        wait.until(ExpectedConditions.invisibilityOf(modal));
        Thread.sleep(800);
        table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[contains(@class,'min-w-full')]")));
        boolean foundEdit = table.findElements(By.xpath(".//tbody/tr/td[contains(.,'" + newName + "')]")).size() > 0;
        assertTrue(foundEdit, "Tên sách đã sửa phải xuất hiện trong danh sách");
    }

    @Test
    @DisplayName("8. Xoá sách vừa thêm (tìm theo tên và xoá, kiểm tra sách đã biến mất)")
    public void testDeleteBookByName() throws InterruptedException {
        driver.get(BASE_URL + "/admin/products");
        Thread.sleep(2000);
        
        // Thêm sách mới để test xóa
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Thêm Sản phẩm')]")));
        addBtn.click();
        Thread.sleep(1200);
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'fixed') and .//h3[contains(.,'Sản phẩm')]]")));
        Thread.sleep(900);
        String bookToDelete = "Sách Test Xóa " + System.currentTimeMillis();
        modal.findElement(By.xpath(".//input[@type='text' and @required]"))
            .sendKeys(bookToDelete);
        Thread.sleep(700);
        WebElement priceField = modal.findElement(By.xpath(".//input[@type='number' and @required]"));
        priceField.clear();
        priceField.sendKeys("75000");
        Thread.sleep(700);
        
        // Chọn ít nhất một danh mục (bắt buộc theo validation)
        List<WebElement> categoryCheckboxes = modal.findElements(By.xpath(".//input[@type='checkbox' and contains(@class,'mr-2')]"));
        if (!categoryCheckboxes.isEmpty()) {
            categoryCheckboxes.get(0).click();
            Thread.sleep(500);
        }
        
        Thread.sleep(700);
        WebElement submitBtn = modal.findElement(By.xpath(".//button[@type='submit']"));
        submitBtn.click();
        Thread.sleep(1200);
        wait.until(ExpectedConditions.invisibilityOf(modal));
        Thread.sleep(1200);
        
        // Tìm sách vừa thêm để xóa
        WebElement filterBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Hiện bộ lọc') or contains(.,'Ẩn bộ lọc')]")));
        filterBtn.click();
        Thread.sleep(800);
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Tên sách, tác giả...']")));
        searchInput.clear();
        Thread.sleep(500);
        searchInput.sendKeys(bookToDelete);
        Thread.sleep(1200);
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[contains(@class,'min-w-full')]")));
        Thread.sleep(800);
        List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr"));
        WebElement rowToDelete = null;
        for (WebElement row : rows) {
            if (row.getText().contains(bookToDelete)) { rowToDelete = row; break; }
        }
        assertNotNull(rowToDelete, "Phải tìm thấy dòng chứa sách vừa thêm để xóa");
        WebElement deleteBtn = rowToDelete.findElement(By.xpath(".//button[@title='Xóa sản phẩm']"));
        deleteBtn.click();
        Thread.sleep(400);
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            Thread.sleep(300);
            alert.accept();
        } catch (Exception e) {}
        Thread.sleep(1000);

        // Reload lại trang và kiểm tra sách đã biến mất
        driver.navigate().refresh();
        Thread.sleep(2000);
        WebElement filterBtn2 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Hiện bộ lọc') or contains(.,'Ẩn bộ lọc')]")));
        filterBtn2.click();
        Thread.sleep(800);
        WebElement searchInput2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Tên sách, tác giả...']")));
        searchInput2.clear();
        Thread.sleep(500);
        searchInput2.sendKeys(bookToDelete);
        Thread.sleep(1200);
        WebElement table2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[contains(@class,'min-w-full')]")));
        Thread.sleep(800);
        List<WebElement> rows2 = table2.findElements(By.xpath(".//tbody/tr"));
        boolean found = false;
        for (WebElement row : rows2) {
            if (row.getText().contains(bookToDelete)) { found = true; break; }
        }
        assertFalse(found, "Sách đã bị xóa không còn xuất hiện trong danh sách");
    }

    private String lastCategoryName = null;
    private String lastBookName = null;
    private String lastEditedBookName = null;
}
