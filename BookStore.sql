-- Tạo cơ sở dữ liệu
CREATE DATABASE bookstore;

-- Kết nối đến cơ sở dữ liệu
\c bookstore

-- Tạo bảng Categories
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL
);

-- Tạo bảng Books
CREATE TABLE books (
    book_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    publisher VARCHAR(255),
    publication_year SMALLINT,
    description TEXT, -- Thêm cột description
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL,
    CONSTRAINT chk_books_price CHECK (price >= 0),
    CONSTRAINT chk_books_stock CHECK (stock_quantity >= 0)
);

-- Tạo bảng Categories_Details (bảng trung gian cho mối quan hệ nhiều-đối-nhiều giữa Books và Categories)
CREATE TABLE categories_details (
    book_id INT,
    category_id INT,
    PRIMARY KEY (book_id, category_id),
    CONSTRAINT fk_categories_details_book FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE,
    CONSTRAINT fk_categories_details_category FOREIGN KEY (category_id) REFERENCES categories (category_id) ON DELETE CASCADE
);

-- Tạo bảng Users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(5) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100), 
    phone VARCHAR(20),
    address TEXT,
    status VARCHAR(10) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_role CHECK (role IN ('KH', 'Nvien', 'Qly')),
    CONSTRAINT chk_users_status CHECK (status IN ('Active', 'Lock'))
);

-- Tạo bảng Book_Images
CREATE TABLE book_images (
    image_id SERIAL PRIMARY KEY,
    book_id INT,
    image_url VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_images_book FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE
);

-- Tạo bảng Inventory_Transactions
CREATE TABLE inventory_transactions (
    transaction_id SERIAL PRIMARY KEY,
    book_id INT,
    transaction_type VARCHAR(10),
    quantity INT NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    price_at_transaction DECIMAL(10, 2),
    user_id INT,
    CONSTRAINT fk_inventory_transactions_book FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_transactions_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL,
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('Nhập', 'Xuất')),
    CONSTRAINT chk_quantity CHECK (quantity > 0)
);

-- Tạo bảng Orders
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Đang xử lý',
    shipping_address TEXT NOT NULL, -- Thêm cột shipping_address
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL,
    CONSTRAINT chk_orders_status CHECK (status IN ('Đang xử lý', 'Đã giao', 'Đã hủy')),
    CONSTRAINT chk_orders_total CHECK (total_amount >= 0)
);

-- Tạo bảng Order_Details
CREATE TABLE order_details (
    order_detail_id SERIAL PRIMARY KEY,
    order_id INT,
    book_id INT,
    quantity INT NOT NULL,
    price_at_order DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_details_order FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_details_book FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE,
    CONSTRAINT chk_order_details_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_details_price CHECK (price_at_order >= 0)
);

-- Tạo bảng Payments
CREATE TABLE payments (
    payment_id SERIAL PRIMARY KEY,
    order_id INT,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
    CONSTRAINT chk_payments_amount CHECK (amount >= 0)
);

-- Tạo bảng Shopping_Cart
CREATE TABLE shopping_cart (
    user_id INT,
    book_id INT,
    PRIMARY KEY (user_id, book_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);


-- 1. Thêm dữ liệu mẫu cho bảng Categories
INSERT INTO categories (category_name) VALUES
    ('Khoa học'),
    ('Công nghệ'),
    ('Văn học'),
    ('Lịch sử'),
    ('Kinh tế');

-- 2. Thêm dữ liệu mẫu cho bảng Books
INSERT INTO books (title, author, publisher, publication_year, description, price, stock_quantity) VALUES
    ('Sapiens: Lược sử loài người', 'Yuval Noah Harari', 'NXB Trẻ', 2017, 'Cuốn sách khám phá lịch sử nhân loại từ thời kỳ đồ đá đến hiện đại.', 29.99, 0),
    ('Clean Code', 'Robert C. Martin', 'NXB Công nghệ', 2008, 'Hướng dẫn viết mã nguồn sạch và dễ bảo trì.', 35.50, 0),
    ('Nhà giả kim', 'Paulo Coelho', 'NXB Văn học', 1988, 'Câu chuyện về hành trình theo đuổi giấc mơ.', 19.99, 0),
    ('Lược sử thời gian', 'Stephen Hawking', 'NXB Khoa học', 1988, 'Khám phá vũ trụ qua góc nhìn vật lý lý thuyết.', 25.00, 0),
    ('Tư duy nhanh và chậm', 'Daniel Kahneman', 'NXB Kinh tế', 2011, 'Phân tích cách con người đưa ra quyết định.', 27.50, 0);

-- 3. Thêm dữ liệu mẫu cho bảng Categories_Details
INSERT INTO categories_details (book_id, category_id) VALUES
    (1, 1), -- Sapiens: Khoa học
    (1, 4), -- Sapiens: Lịch sử
    (2, 2), -- Clean Code: Công nghệ
    (3, 3), -- Nhà giả kim: Văn học
    (4, 1), -- Lược sử thời gian: Khoa học
    (4, 2), -- Lược sử thời gian: Công nghệ
    (5, 5); -- Tư duy nhanh và chậm: Kinh tế

-- 4. Thêm dữ liệu mẫu cho bảng Users
INSERT INTO users (username, password, role, email, full_name, phone, address, status) VALUES
    ('admin', '$2a$10$3zHzV2vXg9k6l3z5q3z5q.3zHzV2vXg9k6l3z5q3z5q3zHzV2vXg9k', 'Qly', 'admin@bookstore.com', 'Nguyễn Văn Quản Lý', '0901234567', '123 Hà Nội', 'Active'),
    ('khachhang1', '$2a$10$3zHzV2vXg9k6l3z5q3z5q.3zHzV2vXg9k6l3z5q3z5q3zHzV2vXg9k', 'KH', 'khachhang1@gmail.com', 'Trần Thị Khách', '0912345678', '456 TP.HCM', 'Active'),
    ('nhanvien1', '$2a$10$3zHzV2vXg9k6l3z5q3z5q.3zHzV2vXg9k6l3z5q3z5q3zHzV2vXg9k', 'Nvien', 'nhanvien1@bookstore.com', 'Lê Văn Nhân Viên', '0923456789', '789 Đà Nẵng', 'Active');

-- 5. Thêm dữ liệu mẫu cho bảng Inventory_Transactions (giao dịch nhập kho bởi user Qly)
INSERT INTO inventory_transactions (book_id, transaction_type, quantity, transaction_date, price_at_transaction, user_id) VALUES
    (1, 'Nhập', 50, '2025-05-24 14:00:00', 29.99, 1), -- Nhập 50 cuốn Sapiens
    (2, 'Nhập', 30, '2025-05-24 14:05:00', 35.50, 1), -- Nhập 30 cuốn Clean Code
    (3, 'Nhập', 40, '2025-05-24 14:10:00', 19.99, 1), -- Nhập 40 cuốn Nhà giả kim
    (4, 'Nhập', 25, '2025-05-24 14:15:00', 25.00, 1), -- Nhập 25 cuốn Lược sử thời gian
    (5, 'Nhập', 35, '2025-05-24 14:20:00', 27.50, 1); -- Nhập 35 cuốn Tư duy nhanh và chậm

-- Cập nhật stock_quantity trong bảng Books dựa trên các giao dịch nhập
UPDATE books SET stock_quantity = 50 WHERE book_id = 1;
UPDATE books SET stock_quantity = 30 WHERE book_id = 2;
UPDATE books SET stock_quantity = 40 WHERE book_id = 3;
UPDATE books SET stock_quantity = 25 WHERE book_id = 4;
UPDATE books SET stock_quantity = 35 WHERE book_id = 5;