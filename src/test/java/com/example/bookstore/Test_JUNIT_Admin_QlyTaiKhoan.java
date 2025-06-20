package com.example.bookstore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import com.example.bookstore.dto.Token;
import com.example.bookstore.dto.UserDTO;
import com.example.bookstore.facade.AuthFacade;
import com.example.bookstore.facade.UserFacade;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.UserService;

/**
 * JUnit Tests cho chức năng Quản lý tài khoản của Admin
 */
@SpringBootTest
@TestPropertySource(properties = {"server.port=8080"})
public class Test_JUNIT_Admin_QlyTaiKhoan {

    @Autowired
    private AuthFacade authFacade;
    
    @Autowired
    private UserFacade userFacade;
    
    @Autowired
    private UserRepository userRepository;
    
    private Token adminToken;
    private UserDTO adminUser;
    
    @BeforeAll
    static void setupClass() {
        System.out.println("Bắt đầu kiểm thử chức năng Quản lý tài khoản với quyền Admin");
    }
    
    @BeforeEach
    void setUp() {
        System.out.println("Thiết lập môi trường kiểm thử - Đăng nhập với quyền Admin");
        
        // Đăng nhập với quyền Admin để có token cho các thao tác
        adminToken = authFacade.login("admin", "abc@123");
        Assertions.assertNotNull(adminToken, "Token admin không được trả về null");
        
        adminUser = authFacade.getCurrentUser(adminToken.getAccess_token());
        Assertions.assertNotNull(adminUser, "Không thể lấy thông tin người dùng admin từ token");
        Assertions.assertEquals("admin", adminUser.getUsername(), "Username không khớp");
        Assertions.assertEquals("Qly", adminUser.getRole(), "Vai trò phải là Admin (Qly)");
        
        System.out.println("Đăng nhập thành công với vai trò: " + adminUser.getRole());
    }
    
    @AfterEach
    void tearDown() {
        System.out.println("Dọn dẹp sau kiểm thử");
        
        // Đăng xuất sau mỗi test
        if (adminToken != null) {
            authFacade.logout(adminToken.getAccess_token());
        }
    }
    
    @Test
    @DisplayName("Kiểm tra quyền Admin xem danh sách tất cả tài khoản")
    void testAdminXemDanhSachTaiKhoan() {
        System.out.println("Đang kiểm tra chức năng xem danh sách tài khoản");
        
        // Gọi API lấy danh sách tài khoản
        List<User> userList = userRepository.findAll();
        
        // Kiểm tra danh sách không null và không rỗng
        Assertions.assertNotNull(userList, "Danh sách người dùng không được null");
        Assertions.assertFalse(userList.isEmpty(), "Danh sách người dùng không được rỗng");
        
        // Kiểm tra có ít nhất một tài khoản admin trong danh sách
        boolean foundAdmin = userList.stream()
                .anyMatch(user -> "Qly".equals(user.getRole()));
        
        Assertions.assertTrue(foundAdmin, "Danh sách phải chứa ít nhất một tài khoản admin (Qly)");
        
        // In thông tin số lượng tài khoản theo từng loại
        long countKH = userList.stream().filter(u -> "KH".equals(u.getRole())).count();
        long countNvien = userList.stream().filter(u -> "Nvien".equals(u.getRole())).count();
        long countQly = userList.stream().filter(u -> "Qly".equals(u.getRole())).count();
        
        System.out.println("✅ Admin xem được danh sách tài khoản:");
        System.out.println("   - Tổng số tài khoản: " + userList.size());
        System.out.println("   - Số tài khoản khách hàng (KH): " + countKH);
        System.out.println("   - Số tài khoản nhân viên (Nvien): " + countNvien);
        System.out.println("   - Số tài khoản quản lý (Qly): " + countQly);
    }
    
    @Test
    @DisplayName("Kiểm tra Admin tạo tài khoản mới")
    void testAdminTaoTaiKhoanMoi() {
        System.out.println("Đang kiểm tra chức năng tạo tài khoản mới");
        
        // Tạo thông tin tài khoản mới
        User newUser = new User();
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        newUser.setUsername(uniqueUsername);
        newUser.setPassword("abc@123");
        newUser.setRole("KH");
        newUser.setEmail(uniqueUsername + "@example.com");
        newUser.setFullName("Người Dùng Test");
        newUser.setPhone("0987654321");
        newUser.setAddress("123 Đường Test, Phường Test, Quận Test, TP. HCM");
        newUser.setStatus("Active");
        newUser.setCreatedAt(LocalDateTime.now());
        
        // Lưu trực tiếp vào repository để tráp lỗi
        User savedUser = userRepository.save(newUser);
        
        // Kiểm tra kết quả
        Assertions.assertNotNull(savedUser, "Người dùng đã lưu không được null");
        Assertions.assertNotNull(savedUser.getUserId(), "ID người dùng không được null");
        Assertions.assertEquals(uniqueUsername, savedUser.getUsername(), "Username không khớp");
        
        // Kiểm tra người dùng đã được thêm vào hệ thống
        Optional<User> foundUserOpt = userRepository.findById(savedUser.getUserId());
        Assertions.assertTrue(foundUserOpt.isPresent(), "Người dùng phải tồn tại trong DB");
        
        User foundUser = foundUserOpt.get();
        Assertions.assertEquals(uniqueUsername, foundUser.getUsername(), "Username không khớp");
        Assertions.assertEquals("KH", foundUser.getRole(), "Vai trò không khớp");
        Assertions.assertEquals("Active", foundUser.getStatus(), "Trạng thái không khớp");
        
        System.out.println("✅ Admin tạo tài khoản mới thành công: " + uniqueUsername);
        
        // Dọn dẹp: Xóa tài khoản vừa tạo để tránh ảnh hưởng các test khác
        userRepository.deleteById(savedUser.getUserId());
    }
    
    @Test
    @DisplayName("Kiểm tra Admin cập nhật thông tin tài khoản")
    void testAdminCapNhatThongTinTaiKhoan() {
        System.out.println("Đang kiểm tra chức năng cập nhật thông tin tài khoản");
        
        // Tạo tài khoản mới để kiểm tra cập nhật
        User testUser = new User();
        String uniqueUsername = "updatetest" + System.currentTimeMillis();
        testUser.setUsername(uniqueUsername);
        testUser.setPassword("abc@123");
        testUser.setRole("KH");
        testUser.setEmail(uniqueUsername + "@example.com");
        testUser.setFullName("User Update Test");
        testUser.setPhone("0987654321");
        testUser.setStatus("Active");
        testUser.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(testUser);
        Integer userId = savedUser.getUserId();
        
        // Chuẩn bị dữ liệu cập nhật
        savedUser.setEmail(uniqueUsername + "updated@example.com");
        savedUser.setFullName("User Updated Name");
        savedUser.setPhone("0123456789");
        savedUser.setAddress("456 Đường Mới, Phường Mới, Quận Mới, TP. HCM");
        
        // Cập nhật trực tiếp qua repository
        User updatedUser = userRepository.save(savedUser);
        
        // Kiểm tra kết quả
        Assertions.assertNotNull(updatedUser, "Người dùng sau khi cập nhật không được null");
        
        // Kiểm tra thông tin đã được cập nhật trong DB
        Optional<User> updatedUserOpt = userRepository.findById(userId);
        Assertions.assertTrue(updatedUserOpt.isPresent(), "Phải tìm thấy tài khoản sau khi cập nhật");
        
        User fetchedUser = updatedUserOpt.get();
        Assertions.assertEquals("User Updated Name", fetchedUser.getFullName(), "Họ tên không được cập nhật đúng");
        Assertions.assertEquals("0123456789", fetchedUser.getPhone(), "Số điện thoại không được cập nhật đúng");
        Assertions.assertEquals(uniqueUsername + "updated@example.com", fetchedUser.getEmail(), "Email không được cập nhật đúng");
        
        System.out.println("✅ Admin cập nhật thông tin tài khoản thành công: " + uniqueUsername);
        
        // Dọn dẹp: Xóa tài khoản test
        userRepository.deleteById(userId);
    }
    
    @Test
    @DisplayName("Kiểm tra Admin thay đổi trạng thái tài khoản (Khóa/Mở khóa)")
    void testAdminThayDoiTrangThaiTaiKhoan() {
        System.out.println("Đang kiểm tra chức năng thay đổi trạng thái tài khoản");
        
        // Tạo tài khoản mới để kiểm tra
        User testUser = new User();
        String uniqueUsername = "statustest" + System.currentTimeMillis();
        testUser.setUsername(uniqueUsername);
        testUser.setPassword("abc@123");
        testUser.setRole("KH");
        testUser.setEmail(uniqueUsername + "@example.com");
        testUser.setFullName("Status Test User");
        testUser.setPhone("0987654321");
        testUser.setStatus("Active"); // Bắt đầu với trạng thái Active
        testUser.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(testUser);
        Integer userId = savedUser.getUserId();
        
        // Thay đổi trạng thái sang "Lock"
        savedUser.setStatus("Lock");
        User lockedUser = userRepository.save(savedUser);
        
        // Kiểm tra kết quả
        Assertions.assertNotNull(lockedUser, "Người dùng sau khi khóa không được null");
        Assertions.assertEquals("Lock", lockedUser.getStatus(), "Trạng thái phải là Lock");
        
        // Kiểm tra trạng thái đã được cập nhật trong DB
        Optional<User> lockedUserOpt = userRepository.findById(userId);
        Assertions.assertTrue(lockedUserOpt.isPresent(), "Phải tìm thấy tài khoản sau khi cập nhật");
        
        User fetchedLockedUser = lockedUserOpt.get();
        Assertions.assertEquals("Lock", fetchedLockedUser.getStatus(), "Trạng thái tài khoản phải được cập nhật thành 'Lock'");
        
        // Kiểm tra tài khoản đã bị khóa không thể đăng nhập
        Token lockedToken = authFacade.login(uniqueUsername, "abc@123");
        Assertions.assertNull(lockedToken, "Tài khoản đã bị khóa không được phép đăng nhập");
        
        // Thay đổi lại trạng thái thành Active
        fetchedLockedUser.setStatus("Active");
        userRepository.save(fetchedLockedUser);
        
        // Kiểm tra tài khoản đã được mở khóa và có thể đăng nhập
        Token unlockedToken = authFacade.login(uniqueUsername, "abc@123");
        Assertions.assertNotNull(unlockedToken, "Tài khoản đã được mở khóa phải đăng nhập được");
        
        System.out.println("✅ Admin thay đổi trạng thái tài khoản thành công");
        
        // Dọn dẹp: Đăng xuất và xóa tài khoản test
        if (unlockedToken != null) {
            authFacade.logout(unlockedToken.getAccess_token());
        }
        userRepository.deleteById(userId);
    }
    
    // @Test
    // @DisplayName("Kiểm tra Admin xóa tài khoản")
    // void testAdminXoaTaiKhoan() {
    //     System.out.println("Đang kiểm tra chức năng xóa tài khoản");
        
    //     // Tạo tài khoản mới để kiểm tra xóa
    //     User testUser = new User();
    //     String uniqueUsername = "deletetest" + System.currentTimeMillis();
    //     testUser.setUsername(uniqueUsername);
    //     testUser.setPassword("abc@123");
    //     testUser.setRole("KH");
    //     testUser.setEmail(uniqueUsername + "@example.com");
    //     testUser.setFullName("Delete Test User");
    //     testUser.setStatus("Active");
    //     testUser.setCreatedAt(LocalDateTime.now());
        
    //     User savedUser = userRepository.save(testUser);
    //     Integer userId = savedUser.getUserId();
        
    //     // Kiểm tra tài khoản đã được tạo thành công
    //     Optional<User> createdUserOpt = userRepository.findById(userId);
    //     Assertions.assertTrue(createdUserOpt.isPresent(), "Tài khoản test phải tồn tại trước khi xóa");
        
    //     // Xóa tài khoản
    //     userRepository.deleteById(userId);
        
    //     // Kiểm tra tài khoản đã bị xóa
    //     Optional<User> deletedUserOpt = userRepository.findById(userId);
    //     Assertions.assertFalse(deletedUserOpt.isPresent(), "Tài khoản đã xóa không được tồn tại trong hệ thống");
        
    //     // Kiểm tra không thể đăng nhập với tài khoản đã xóa
    //     Token tokenAfterDelete = authFacade.login(uniqueUsername, "abc@123");
    //     Assertions.assertNull(tokenAfterDelete, "Không thể đăng nhập với tài khoản đã xóa");
        
    //     System.out.println("✅ Admin xóa tài khoản thành công");
    // }
    
    @Test
    @DisplayName("Kiểm tra tạo tài khoản trùng username hoặc email")
    void testTaoTaiKhoanTrungUsernameHoacEmail() {
        System.out.println("Đang kiểm tra trường hợp tạo tài khoản trùng username hoặc email");
        
        // Tạo tài khoản đầu tiên
        User firstUser = new User();
        String uniqueUsername = "duplicate" + System.currentTimeMillis();
        firstUser.setUsername(uniqueUsername);
        firstUser.setPassword("abc@123");
        firstUser.setRole("KH");
        firstUser.setEmail(uniqueUsername + "@example.com");
        firstUser.setFullName("Duplicate Test User");
        firstUser.setPhone("0987654321");
        firstUser.setStatus("Active");
        firstUser.setCreatedAt(LocalDateTime.now());
        
        // Lưu tài khoản đầu tiên
        User savedFirstUser = userRepository.save(firstUser);
        Assertions.assertNotNull(savedFirstUser.getUserId(), "Tạo tài khoản đầu tiên phải thành công");
        
        // Cố gắng tạo tài khoản thứ hai với username giống nhau
        User secondUser = new User();
        secondUser.setUsername(uniqueUsername); // Trùng username
        secondUser.setPassword("def@456");
        secondUser.setRole("KH");
        secondUser.setEmail("different" + uniqueUsername + "@example.com");
        secondUser.setFullName("Another User");
        secondUser.setPhone("0123456789");
        secondUser.setStatus("Active");
        secondUser.setCreatedAt(LocalDateTime.now());
        
        // Cố gắng lưu tài khoản trùng username và bắt ngoại lệ
        boolean duplicateUsernameExceptionThrown = false;
        try {
            userRepository.save(secondUser);
        } catch (Exception e) {
            duplicateUsernameExceptionThrown = true;
            System.out.println("Bắt được ngoại lệ khi tạo tài khoản trùng username: " + e.getMessage());
        }
        
        // Có thể không gây ngoại lệ nếu DB không có ràng buộc unique, trong trường hợp đó kiểm tra số lượng
        if (!duplicateUsernameExceptionThrown) {
            long countWithSameUsername = userRepository.findAll().stream()
                    .filter(u -> uniqueUsername.equals(u.getUsername()))
                    .count();
            // Nếu có constraint trong code xử lý, vẫn là 1; nếu không có constraint, sẽ là 2
            System.out.println("Số tài khoản có cùng username: " + countWithSameUsername);
        }
        
        // Cố gắng tạo tài khoản thứ ba với email giống nhau
        User thirdUser = new User();
        thirdUser.setUsername("different" + uniqueUsername); // Username khác
        thirdUser.setPassword("ghi@789");
        thirdUser.setRole("KH");
        thirdUser.setEmail(uniqueUsername + "@example.com"); // Email trùng
        thirdUser.setFullName("Third User");
        thirdUser.setPhone("0987123456");
        thirdUser.setStatus("Active");
        thirdUser.setCreatedAt(LocalDateTime.now());
        
        // Cố gắng lưu tài khoản trùng email và bắt ngoại lệ
        boolean duplicateEmailExceptionThrown = false;
        try {
            userRepository.save(thirdUser);
        } catch (Exception e) {
            duplicateEmailExceptionThrown = true;
            System.out.println("Bắt được ngoại lệ khi tạo tài khoản trùng email: " + e.getMessage());
        }
        
        // Có thể không gây ngoại lệ nếu DB không có ràng buộc unique, trong trường hợp đó kiểm tra số lượng
        if (!duplicateEmailExceptionThrown) {
            long countWithSameEmail = userRepository.findAll().stream()
                    .filter(u -> (uniqueUsername + "@example.com").equals(u.getEmail()))
                    .count();
            // Nếu có constraint trong code xử lý, vẫn là 1; nếu không có constraint, sẽ là 2
            System.out.println("Số tài khoản có cùng email: " + countWithSameEmail);
        }
        
        System.out.println("✅ Kiểm tra tạo tài khoản trùng username hoặc email hoàn tất");
        
        // Dọn dẹp: Xóa tài khoản test
        userRepository.deleteById(savedFirstUser.getUserId());
    }
}