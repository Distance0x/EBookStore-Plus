SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS user_auth;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS order_items;
SET FOREIGN_KEY_CHECKS = 1;
-- 创建book表
CREATE TABLE IF NOT EXISTS book (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(100) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  -- cover LONGTEXT,
  -- description TEXT,
  isbn VARCHAR(20),
  stock INT DEFAULT 0 COMMENT '库存量',
  publisher TEXT COMMENT '出版社',
  tags VARCHAR(500) COMMENT '标签，逗号分隔',
  deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '软删除标识，true表示已删除'
);



-- 插入图书数据
-- INSERT INTO book (title, author, price, isbn, stock, publisher, cover, description) VALUES
INSERT INTO book (title, author, price, isbn, stock, publisher, tags) VALUES
('CSAPP: 深入理解计算机系统', 'Randal E. Bryant, David R. O Hallaron', 130.0,'978-7-5455-5388-8',10000, '上海交通大学出版社', '计算机,系统,底层'),
('算法导论', 'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein', 118.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '计算机,算法,数学'),
('代码整洁之道', 'Robert C. Martin', 59.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '计算机,编程,最佳实践'),
('设计模式', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 79.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '计算机,设计模式,面向对象'),
('JavaScript高级程序设计', 'Nicholas C. Zakas, Matt Frisbie', 99.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '计算机,编程,JavaScript,前端'),
('Python编程：从入门到实践', 'Eric Matthes', 89.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '计算机,编程,Python,入门'),
('黑客与画家', 'Paul Graham', 49.0,'978-7-5455-5388-8',10000, '上海交通大学出版社', '计算机,创业,思想'),
('人月神话', 'Frederick P. Brooks Jr.', 68.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '计算机,项目管理,软件工程'),
('活着', '余华', 28.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '小说,文学,现代文学'),
('三体', '刘慈欣', 48.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '小说,科幻,硬科幻'),
('追风筝的人', '卡勒德·胡赛尼', 36.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '小说,文学,外国文学'),
('白夜行', '东野圭吾', 45.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '小说,推理,日本文学'),
('小王子', '安托万·德·圣埃克苏佩里', 22.0,'978-7-5455-5388-8',10000, '上海交通大学出版社', '小说,童话,经典'),
('围城', '钱钟书', 39.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '小说,文学,现代文学,讽刺'),
('挪威的森林', '村上春树', 38.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '小说,文学,日本文学,爱情'),
('百年孤独', '加西亚·马尔克斯', 55.0, '978-7-5455-5388-8',10000, '上海交通大学出版社', '小说,文学,魔幻现实主义');

-- 创建user表 
CREATE TABLE IF NOT EXISTS user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  phone VARCHAR(20),
  address TEXT,
  account VARCHAR(50) NOT NULL UNIQUE,
  role ENUM('user', 'admin') DEFAULT 'user',
  status ENUM('active', 'disabled') DEFAULT 'active',
  balance DECIMAL(10,2) DEFAULT 100000.00
);


-- 创建user_auth表
CREATE TABLE IF NOT EXISTS user_auth (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  FOREIGN KEY (account) REFERENCES user(account) ON DELETE CASCADE
);

-- 删除 如果用户被删除也同时在UserAuth删除
-- INSERT INTO user (name, email, phone, address, account, role) VALUES
-- ('Tom', 'tom@example.com', '13800138000', '上海市闵行区江川路街道东川路800号', 'tom', 'user'),
-- ('Admin', 'admin@example.com', '13900139000', '北京市海淀区中关村', 'admin', 'admin'),
-- ('se菜鸡', 'sjtu@sjtu.edu.cn', '13822228888', '上海市闵行区江川路街道东川路800号', 'zzh', 'user');


-- INSERT INTO user_auth (account, password) VALUES
-- ('tom', '123456'),
-- ('admin', 'admin123'),
-- ('zzh', 'zzh0312');

-- 创建购物车表
CREATE TABLE IF NOT EXISTS cart (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,              
  book_id BIGINT NOT NULL,              
  quantity INT NOT NULL DEFAULT 1,      
  added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
  FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
  UNIQUE KEY unique_user_book (user_id, book_id) -- 一个用户对同一本书只能有一条记录
);

-- 添加一些测试数据
-- INSERT INTO cart (user_id, book_id, quantity) VALUES
-- (1, 1, 2),  -- tom用户添加了2本"CSAPP: 深入理解计算机系统"
-- (1, 3, 1);  -- tom用户添加了1本"代码整洁之道"


CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  order_number VARCHAR(50) NOT NULL UNIQUE,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  shipping_address TEXT,
  contact_phone VARCHAR(20),
  payment_time TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- 创建订单项表
CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE RESTRICT
);




