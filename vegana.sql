/*
 Navicat Premium Data Transfer

 Source Server         : TIenThanh
 Source Server Type    : MySQL
 Source Server Version : 100432
 Source Host           : localhost:3306
 Source Schema         : vegana_store

 Target Server Type    : MySQL
 Target Server Version : 100432
 File Encoding         : 65001

 Date: 08/12/2025 09:57:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for carts
-- ----------------------------
DROP TABLE IF EXISTS `carts`;
CREATE TABLE `carts`  (
  `cartId` int NOT NULL AUTO_INCREMENT,
  `customerId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `productId` int NULL DEFAULT NULL,
  `quantity` int NULL DEFAULT NULL,
  `price` double NULL DEFAULT NULL,
  PRIMARY KEY (`cartId`) USING BTREE,
  INDEX `customerId_idx`(`customerId` ASC) USING BTREE,
  INDEX `productId_idx`(`productId` ASC) USING BTREE,
  CONSTRAINT `customerId_fk` FOREIGN KEY (`customerId`) REFERENCES `customers` (`customerId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `productId_fk` FOREIGN KEY (`productId`) REFERENCES `products` (`productId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 181 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of carts
-- ----------------------------
INSERT INTO `carts` VALUES (122, 'khachhang00', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (152, 'newuser1763697311696', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (153, 'user1763698924964', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (154, 'user1763698929523', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (155, 'customer01', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (156, 'user1763699086568', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (157, 'user1763699090818', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (158, 'user1763699133425', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (159, 'user1763699137715', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (160, 'user1763699337875', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (161, 'user1763699341968', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (162, 'user1763699798577', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (163, 'user1763699802749', NULL, NULL, NULL);
INSERT INTO `carts` VALUES (179, 'abcd', 4, 2, 76);
INSERT INTO `carts` VALUES (180, 'abcd', 6, 1, 38);

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `categoryId` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`categoryId`) USING BTREE,
  UNIQUE INDEX `nameUnique`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (12, 'Auto Cat 1763959489661');
INSERT INTO `categories` VALUES (5, 'Candy');
INSERT INTO `categories` VALUES (13, 'Cat 1763959754248');
INSERT INTO `categories` VALUES (14, 'Cat 1764043959528');
INSERT INTO `categories` VALUES (15, 'Cat 1764044033407');
INSERT INTO `categories` VALUES (16, 'Cat 1764045350384');
INSERT INTO `categories` VALUES (17, 'Cat 1764045593680');
INSERT INTO `categories` VALUES (18, 'Cat 1764549840646');
INSERT INTO `categories` VALUES (19, 'Cat 1764550635808');
INSERT INTO `categories` VALUES (20, 'Cat 1764554951880');
INSERT INTO `categories` VALUES (21, 'Cat 1764557552166');
INSERT INTO `categories` VALUES (22, 'Cat 1764561647183');
INSERT INTO `categories` VALUES (23, 'Cat 1764653799693');
INSERT INTO `categories` VALUES (24, 'Cat 1764899131440');
INSERT INTO `categories` VALUES (2, 'Cookies');
INSERT INTO `categories` VALUES (8, 'demo cate');
INSERT INTO `categories` VALUES (4, 'Drinks');
INSERT INTO `categories` VALUES (10, 'Keo');
INSERT INTO `categories` VALUES (3, 'Milk');
INSERT INTO `categories` VALUES (1, 'Snack');
INSERT INTO `categories` VALUES (11, 'Test Category 1763959205370');

-- ----------------------------
-- Table structure for customers
-- ----------------------------
DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers`  (
  `customerId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `enabled` bit(1) NULL DEFAULT NULL,
  `fullname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `photo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `roleId` int NULL DEFAULT NULL,
  PRIMARY KEY (`customerId`) USING BTREE,
  INDEX `roleID`(`roleId` ASC) USING BTREE,
  CONSTRAINT `roleID` FOREIGN KEY (`roleId`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customers
-- ----------------------------
INSERT INTO `customers` VALUES ('abcd', 'thanhbttb01217@gmail.com', b'1', 'Tiến Thành', '$2a$10$SvjtcSLR48G22dx5N8l9y.pPZ5qPcBgkaf/L4UkyjyxfqzdGd16FK', '', 0);
INSERT INTO `customers` VALUES ('admin', 'adminvegana@gmail.com', b'1', 'Tôi là admin', '$2a$10$1iPiIh9Mw/8jFkmrTzVhs.CrY8rBMn1hWHVSw2NPn92hRTK4kYwHu', '', 1);
INSERT INTO `customers` VALUES ('customer01', 'newmail1763698944053@gmail.com', b'1', 'Duplicate Tester', '$2a$10$hZk.6eb43.ODXYaVKhSUHeSY7UaHnVqjs7VKrWRPHM8tzuWPYkncy', '', 0);
INSERT INTO `customers` VALUES ('khachhang00', 'vv@gg', b'1', 'Nguyễn Ngọc Nam', '$2a$10$f9sN7gLXdNaXYBs7nqroKeF0xf7B7DiMUBkIxJoVszQfb82nLrRWe', '', 0);
INSERT INTO `customers` VALUES ('khai00', 'khaikhai331@gmail.com', b'1', 'Phan Hoàng Khải ', '$2a$10$j.MEMT2MkdQTF8OJ59As2OTw4Nhaxg0cCBnscsxMpFwhHx1.72.6a', '', 0);
INSERT INTO `customers` VALUES ('khai11', 'khaixrkyz@gmail.com', b'1', 'phan hoang khai', '$2a$10$ITDgfCJRVqIMaXpxKL72rOZ/lLQg3.0jcMm3fypsxdIET2OKJGluK', '', 0);
INSERT INTO `customers` VALUES ('newuser1763697311696', 'customer01@gmail.com', b'1', 'Duplicate Email Tester', '$2a$10$GOTRDaWq6WWXTq0AQJeEuuyzLRDhEnrUvKPKWI8izMfiP3Tnmfz1C', '', 0);
INSERT INTO `customers` VALUES ('sonnika', 'sonnguyenhong382@gmail.com', b'1', 'nguyen hong son', '$2a$10$rFvjHR41HlhHgrk6zlrgX.x4fDJIRLi/9ufvb2AIFM4I23OqL7Xsa', '', 0);
INSERT INTO `customers` VALUES ('user1763698924964', 'test1763698924964@vegana.com', b'1', 'Test User Auto', '$2a$10$CUi2tefpt7ck3h225/d8n.rlnOD3rYue2RaAN5xZuuCPLDzVdMKDS', '', 0);
INSERT INTO `customers` VALUES ('user1763698929523', 'user1763698929523@test.com', b'1', 'Short Pass Tester', '$2a$10$ij7cy3uCF5Wlu1Entgv1zeuEJrfxGY1qgz.6eTD.RGo4LVRzpQxdW', '', 0);
INSERT INTO `customers` VALUES ('user1763699086568', 'test1763699086568@vegana.com', b'1', 'Test User Auto', '$2a$10$Oqzd36yTuWftCXJ.WIYfduig7oeLdnODNwbW4EqUWKIR40BAA2Tqy', '', 0);
INSERT INTO `customers` VALUES ('user1763699090818', 'user1763699090818@test.com', b'1', 'Short Pass Tester', '$2a$10$.dH2zy5om5l08HSbNET4.utTlV0lqM4fP.LTiLvHFYD5Nikl5wjbG', '', 0);
INSERT INTO `customers` VALUES ('user1763699133425', 'test1763699133425@vegana.com', b'1', 'Test User Auto', '$2a$10$xCS6N.U84Cwb58BNz3ZmwOqi/V.X0m5nP1Ixd39mRJO.2hhaekrZK', '', 0);
INSERT INTO `customers` VALUES ('user1763699137715', 'user1763699137715@test.com', b'1', 'Short Pass Tester', '$2a$10$rNJF9Ms/HS1DV2lJartz7uWSZOvurw1i3j8.8qkKgSs7a2oUK2XYa', '', 0);
INSERT INTO `customers` VALUES ('user1763699337875', 'test1763699337875@vegana.com', b'1', 'Test User Auto', '$2a$10$NjDa/FDhNwUv6WLkQVOSFuQeN7CVImichNksIqDqbHriOJB/O6PkW', '', 0);
INSERT INTO `customers` VALUES ('user1763699341968', 'user1763699341968@test.com', b'1', 'Short Pass Tester', '$2a$10$ElhuJOshfFX6/lOQ/YO/vecisy/6vqdZVw/WNPuhhP2gYIreV8osu', '', 0);
INSERT INTO `customers` VALUES ('user1763699798577', 'test1763699798577@vegana.com', b'1', 'Test User Auto', '$2a$10$GpuR5M31vmwJgdtmP9ij0OeChTwF30fjpkkIvrrT6CB3qRM.l0Y0.', '', 0);
INSERT INTO `customers` VALUES ('user1763699802749', 'user1763699802749@test.com', b'1', 'Short Pass Tester', '$2a$10$Yvs8TSZ9TFbbIGPhLU8iAeqGGB67qqXF5rePQLb2csAafmHo7ZeJO', '', 0);

-- ----------------------------
-- Table structure for hibernate_sequence
-- ----------------------------
DROP TABLE IF EXISTS `hibernate_sequence`;
CREATE TABLE `hibernate_sequence`  (
  `next_val` bigint NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hibernate_sequence
-- ----------------------------
INSERT INTO `hibernate_sequence` VALUES (1);

-- ----------------------------
-- Table structure for orderdetails
-- ----------------------------
DROP TABLE IF EXISTS `orderdetails`;
CREATE TABLE `orderdetails`  (
  `orderDetailId` int NOT NULL AUTO_INCREMENT,
  `price` double NULL DEFAULT NULL,
  `quantity` int NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `total_price` double NULL DEFAULT NULL,
  `orderId` int NULL DEFAULT NULL,
  `productId` int NULL DEFAULT NULL,
  PRIMARY KEY (`orderDetailId`) USING BTREE,
  INDEX `FK3ohml2o6a85wh1nn65snnaind`(`orderId` ASC) USING BTREE,
  INDEX `FK5pie1uapfd704usnm2loi3tex`(`productId` ASC) USING BTREE,
  CONSTRAINT `FK5pie1uapfd704usnm2loi3tex` FOREIGN KEY (`productId`) REFERENCES `products` (`productId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `orderDetailId` FOREIGN KEY (`orderId`) REFERENCES `orders` (`orderId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 110 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orderdetails
-- ----------------------------
INSERT INTO `orderdetails` VALUES (96, 20, 4, 'Đã Thanh Toán', 80, 49, 17);
INSERT INTO `orderdetails` VALUES (97, 190, 4, 'Đã Thanh Toán', 760, 49, 19);
INSERT INTO `orderdetails` VALUES (98, 285, 4, 'Đã Thanh Toán', 1140, 49, 20);
INSERT INTO `orderdetails` VALUES (99, 38, 3, 'Đã Thanh Toán', 114, 50, 6);
INSERT INTO `orderdetails` VALUES (100, 38, 1, 'Đã Thanh Toán', 38, 51, 6);
INSERT INTO `orderdetails` VALUES (102, 45, 7, 'Đã Thanh Toán', 315, 52, 3);
INSERT INTO `orderdetails` VALUES (103, 45, 1, 'Đang Chờ Xử Lý', 45, 53, 3);
INSERT INTO `orderdetails` VALUES (104, 45, 1, 'Đang Chờ Xử Lý', 45, 54, 3);
INSERT INTO `orderdetails` VALUES (105, 118.75, 1, 'Đang Chờ Xử Lý', 118.75, 55, 38);
INSERT INTO `orderdetails` VALUES (106, 45, 1, 'Đang Chờ Xử Lý', 45, 56, 3);
INSERT INTO `orderdetails` VALUES (107, 45, 1, 'Đang Chờ Xử Lý', 45, 57, 3);
INSERT INTO `orderdetails` VALUES (108, 45, 1, 'Đang Chờ Xử Lý', 45, 58, 3);
INSERT INTO `orderdetails` VALUES (109, 38, 1, 'Đang Chờ Xử Lý', 38, 59, 4);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `orderId` int NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `orderDate` date NULL DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `receiver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `total_price` double NULL DEFAULT NULL,
  `customerId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`orderId`) USING BTREE,
  INDEX `FK1bpj2iini89gbon333nm7tvht`(`customerId` ASC) USING BTREE,
  CONSTRAINT `customerID` FOREIGN KEY (`customerId`) REFERENCES `customers` (`customerId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (49, '01 Vo Van Ngan Street', 'giao muộn là xuống địa ngục', '2023-05-20', '0367151727', 'Khai Phan', 11486, 'khai00');
INSERT INTO `orders` VALUES (50, '01 Vo Van Ngan Street', 'giao muộn là xuống địa ngục', '2023-05-21', '0367151727', 'Khai Phan', 114, 'khai00');
INSERT INTO `orders` VALUES (51, '01 Vo Van Ngan Street', 'giao muộn là xuống địa ngục', '2023-05-22', '0367151727', 'Khai Phan', 263, 'khai00');
INSERT INTO `orders` VALUES (52, '01 Vo Van Ngan Street', 'giao muộn là xuống địa ngục', '2023-05-22', '0367151727', 'Khai Phan', 315, 'khai00');
INSERT INTO `orders` VALUES (53, 'Số 90 Cô Bắc', 'Lập trình mobile với React Native tại F8', '2025-11-21', '0793453350', 'Bùi Tiến Thành', 45, 'abcd');
INSERT INTO `orders` VALUES (54, 'Số 90 Cô Bắc', 'Lập trình mobile với Dart tại F8', '2025-11-21', '0793453350', 'Bùi Tiến Thành', 45, 'abcd');
INSERT INTO `orders` VALUES (55, 'Số 90 Cô Bắc', '123', '2025-11-24', '0793453350', 'Bùi Tiến Thành', 118.75, 'abcd');
INSERT INTO `orders` VALUES (56, '123 Testing Street', 'Giao hàng giờ hành chính', '2025-11-24', '0987654321', 'Test User Selenium', 45, 'abcd');
INSERT INTO `orders` VALUES (57, '123 Testing Street', 'Giao hàng giờ hành chính', '2025-11-24', '0987654321', 'Test User Selenium', 45, 'abcd');
INSERT INTO `orders` VALUES (58, '123 Testing Street', 'Giao hàng giờ hành chính', '2025-11-24', '0987654321', 'Test User Selenium', 45, 'abcd');
INSERT INTO `orders` VALUES (59, '123 Testing Street', 'Giao hàng giờ hành chính', '2025-12-05', '0987654321', 'Test User Selenium', 38, 'abcd');

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `productId` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `discount` double NULL DEFAULT NULL,
  `enteredDate` date NULL DEFAULT NULL,
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `price` double NULL DEFAULT NULL,
  `quantity` int NULL DEFAULT NULL,
  `categoryId` int NULL DEFAULT NULL,
  `supplierId` int NULL DEFAULT NULL,
  PRIMARY KEY (`productId`) USING BTREE,
  INDEX `FKej2ob3ifydf846t2a2tntna4e`(`categoryId` ASC) USING BTREE,
  INDEX `FKs2xbxi7wmu948op6qiho9yr8d`(`supplierId` ASC) USING BTREE,
  CONSTRAINT `FKej2ob3ifydf846t2a2tntna4e` FOREIGN KEY (`categoryId`) REFERENCES `categories` (`categoryId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FKs2xbxi7wmu948op6qiho9yr8d` FOREIGN KEY (`supplierId`) REFERENCES `suppliers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 91 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (3, 'Snack bắp vị phô mai Oishi Tom Toms (45g/gói)', 10, '2021-09-02', 'snack-oishi-toms.jpg', 'Snack Oishi Tom Toms', 50, 86, 1, 3);
INSERT INTO `products` VALUES (4, 'Snack bắp ngọt Oishi (45g/gói)', 5, '2021-09-02', 'snack-bap-ngot.jpg', 'Snack bắp ngọt Oishi', 40, 308, 1, 3);
INSERT INTO `products` VALUES (5, 'Snack nhân sô cô la Oishi Pillows (100g) ', 10, '2021-09-01', 'snack-pillows.jpg', 'Snack Oishi Pillows', 60, 60, 1, 3);
INSERT INTO `products` VALUES (6, 'Snack khoai tây vị muối Oishi Flutes (40g)', 5, '2021-09-02', 'snack-flutes.jpg', 'Snack Oishi Flutes', 40, 60, 1, 3);
INSERT INTO `products` VALUES (7, 'Snack nhân sữa dừa Oishi Pillows (100g)', 10, '2021-09-01', 'snack-nhan-pillows.jpg', 'Snack sữa dừa Oishi Pillows', 80, 94, 1, 3);
INSERT INTO `products` VALUES (8, 'Snack khoai tây poca (30) gr', 0, '2021-09-02', 'snack-poca.jpg', 'Snack khoai tây Poca', 50, 312, 1, 3);
INSERT INTO `products` VALUES (9, 'Snack cua vị sốt chua ngọt Oishi Crab Me! (45g/gói)', 10, '2021-09-02', 'snack-crabme.jpg', 'Snack Oishi Crab Me', 60, 54, 1, 3);
INSERT INTO `products` VALUES (10, 'Snack vị bò bít tết poca steack (40g)', 10, '2021-09-02', 'snack-poca-cay.jpg', 'Snack Poca Steack', 80, 75, 1, 3);
INSERT INTO `products` VALUES (11, 'Snack bim bim thái (40g)', 5, '2021-09-02', 'snack-martys.jpg', 'Snack Thái Martys', 60, 54, 1, 3);
INSERT INTO `products` VALUES (12, 'Yến mạch hạnh nhân Dan-D Pak (350g) ', 0, '2021-09-02', 'Yến-mạch-hạnh-nhân-Dan-D-Pak-350g.jpg', 'Dan-D Pak', 100, 63, 2, 4);
INSERT INTO `products` VALUES (13, 'Bánh Quy Viên Sô Cô La Misura 290g', 5, '2021-09-03', 'banh-mizura.jpg', 'Sô Cô La Misura', 110, 44, 2, 4);
INSERT INTO `products` VALUES (14, 'Bánh quy Cosy nhân mứt vị táo hộp 240g', 10, '2021-09-02', 'banh-tik.jpg', 'Cookies Tik ', 120, 24, 2, 4);
INSERT INTO `products` VALUES (15, 'Bánh gạo nướng An vị cá Nhật thượng hạng Orion gói 117.6g', 0, '2021-09-02', 'banh-orion.jpg', 'Bánh Orion', 90, 74, 2, 4);
INSERT INTO `products` VALUES (16, 'Bánh ăn sáng C’est Bon sợi thịt gà là lựa chọn hoàn hảo cho bữa ăn sáng hàng ngày của cả nhà', 5, '2021-09-02', 'banh-orion-bon.jpg', 'Bánh C’est Bon', 150, 64, 2, 4);
INSERT INTO `products` VALUES (17, 'Bánh quy mini kem socola Oreo (23g)', 0, '2021-09-03', 'banh-mini-oreo.jpg', 'Bánh quy socola Oreo', 20, 10, 2, 4);
INSERT INTO `products` VALUES (18, 'Bánh quy cacao nhân kem hạnh nhân YBC 18 cái (115.2g)', 10, '2021-09-07', 'banh-noir.jpg', 'Bánh quy cacao', 120, 44, 2, 3);
INSERT INTO `products` VALUES (19, 'Bánh quy dinh dưỡng hạt Mắc ca kết hợp Nghệ - Hộp 12 bánh 45g', 5, '2021-09-04', 'banh-mac-ca.jpg', 'Bánh quy hạt Mắc ca', 200, 10, 2, 4);
INSERT INTO `products` VALUES (20, 'Bánh quy Danisa được sản xuất từ công thức chính gốc của Đan Mạch, với nguyên liệu được lựa chọn kỹ càng, tinh túy nhất, sử dụng loại bơ thượng hạng giàu hương vị góp phần tạo nên sự khác biệt độc đáo so với các dòng bánh quy bơ khác.', 5, '2021-09-05', 'banh-danisa.jpg', 'Bánh Danisa', 300, 50, 2, 4);
INSERT INTO `products` VALUES (21, 'Kẹo dẻo Jellyc Hải Hà kotobuki 100g', 0, '2021-09-03', 'chip-chip-panda.jpg', 'Chip Chip HAIHA', 40, 100, 5, 6);
INSERT INTO `products` VALUES (22, 'Kẹo AnyTime Hàn Quốc 60 gram ( vị sữa và bạc hà) thanh mát.', 0, '2021-09-14', 'keo-anytime.jpg', 'Kẹo AnyTime', 50, 200, 5, 6);
INSERT INTO `products` VALUES (23, 'Kẹo Cao Su Doublemint Vị Bạc Hà', 10, '2021-09-01', 'keo-doublemint.jpg', 'Kẹo Doublemint', 60, 100, 5, 6);
INSERT INTO `products` VALUES (24, 'Hộp Hạt Hạnh Nhân Dinh Dưỡng Cho Mẹ REAL FOOD STORE (250g) ', 10, '2021-09-15', 'hat-hanh-nhan.jpg', 'Hạt Hạnh Nhân', 200, 200, 5, 6);
INSERT INTO `products` VALUES (25, 'Hạt hạnh nhân nguyên chất Kirkland Almonds Mỹ 1.36kg', 10, '2021-09-10', 'hat-almonds.jpg', 'Hạt hạnh nhân Kirkland', 300, 99, 5, 6);
INSERT INTO `products` VALUES (26, 'Túi Hạt Macca Dinh Dưỡng Cho Mẹ Real Food Store (500g)', 0, '2021-09-08', 'hat-nuts.jpg', 'Hạt Macca Dinh Dưỡng', 200, 100, 5, 6);
INSERT INTO `products` VALUES (27, 'Nhân Hạt Óc Chó Sunrise (120gr) Hạt Dinh Dưỡng Đã Tách Vỏ Quả Óc Chó.Nhập Khẩu Mỹ', 10, '2021-09-10', 'hat-oc-cho.jpg', 'Nhân Hạt Óc Chó Sunrise', 400, 300, 5, 6);
INSERT INTO `products` VALUES (28, 'Lốc 3 Hộp Sữa Hạt Hạnh Nhân Nguyên Chất 137 180ml', 0, '2021-09-03', 'sua-hanh-nhan.jpg', 'Sữa Hạt Hạnh Nhân ', 60, 100, 3, 2);
INSERT INTO `products` VALUES (29, 'Nước ngọt Mirinda hương cam chai 1.5 lít', 10, '2021-09-10', 'nuoc-mirinda-cam.jpg', 'Mirinda vị cam', 100, 60, 4, 5);
INSERT INTO `products` VALUES (30, 'Nước ngọt Mountain Dew 390 ml', 0, '2021-09-16', 'nuoc-mountain.jpg', 'Mountain Dew', 80, 300, 4, 5);
INSERT INTO `products` VALUES (31, 'Trà ô long TEA 350ml', 5, '2021-09-11', 'tra-o-long.jpg', 'Trà TEA+', 45, 50, 4, 5);
INSERT INTO `products` VALUES (32, 'Nước uống Isotonic vị chanh muối', 0, '2021-09-17', 'nuoc-revive.jpg', 'Nước Revive', 65, 201, 4, 5);
INSERT INTO `products` VALUES (33, 'Nước uống đóng chai Aquafina (500ml)', 0, '2021-09-09', 'nuoc-aquafina.jpg', 'Aquafina', 20, 299, 4, 5);
INSERT INTO `products` VALUES (34, 'Nước ngọt 7Up', 0, '2021-09-08', 'nuoc-7-up.jpg', '7Up', 35, 200, 4, 5);
INSERT INTO `products` VALUES (35, 'Trà Lipton ICE Tea', 20, '2021-09-06', 'lipton-tea.jpg', 'Lipton Tea', 85, 300, 4, 5);
INSERT INTO `products` VALUES (36, 'Nước giải khát Coca-Cola Plus (330ml)', 0, '2021-09-11', 'coca-cola-plus.jpg', 'Coca-Cola Plus', 100, 100, 4, 5);
INSERT INTO `products` VALUES (37, 'Nước Giải Khát Coca-Cola vị Nguyên Bản Original 320mlx6 | Nước có gas', 5, '2021-09-19', 'coca-cola-original.jpg', ' Coca-Cola vị  Original', 120, 200, 4, 5);
INSERT INTO `products` VALUES (38, 'Nước Giải Khát Coca-Cola | Nước có gas', 5, '2021-09-17', 'coca-cola.jpg', 'Coca-Cola', 125, 295, 4, 5);
INSERT INTO `products` VALUES (39, 'Sữa Dielac Grow Plus 1+ Màu Xanh Tăng Cân, 1-2 tuổi, Vinamilk', 10, '2021-09-11', 'sua-dielac-grow-plus.jpg', 'Sữa Dielac Grow Plus', 500, 300, 3, 1);
INSERT INTO `products` VALUES (40, 'SỮA BỘT GOLD YOKO 1 VINAMILK 850G DÀNH CHO BÉ TỪ 0 - 1 Tuổi | Sữa cho bé dưới 24 tháng', 10, '2021-09-09', 'sua-bot-yoko.jpg', 'SỮA BỘT GOLD YOKO', 700, 100, 3, 1);
INSERT INTO `products` VALUES (41, 'HỘP SỮA BỘT VINAMILK DIELAC ALPHA GOLD IQ 1 (400G) (CHO TRẺ TỪ 0 - 6 THÁNG TUỔI) ', 5, '2021-09-12', 'sua-alpha.jpg', 'DIELAC ALPHA GOLD', 600, 300, 3, 1);
INSERT INTO `products` VALUES (42, 'Sữa bột Vinamilk Dielac Optimum số 2 - hộp thiếc 900g (dành cho trẻ từ 6-12 tháng tuổi)', 15, '2021-09-11', 'sua-optimum.jpg', 'Dielac Optimum', 500, 100, 3, 1);
INSERT INTO `products` VALUES (43, 'Sữa dielac grow plus 1+ 900g dành cho trẻ từ 1-2 tuổi', 10, '2021-09-08', 'sua-grow-plus.jpg', 'Sữa Dielac Grow Plus-Red', 650, 300, 3, 1);
INSERT INTO `products` VALUES (44, 'Sữa tươi tiệt trùng Vinamilk 100% có đường 180ml (1 hộp)', 5, '2021-09-11', 'sua-tuoi-vinamilk.jpg', 'Sữa Tươi Vinamilk', 35, 200, 3, 1);
INSERT INTO `products` VALUES (45, 'Sữa Nestle Milo nước (Lon 240ml)', 0, '2021-09-09', 'nestle-milo.jpg', ' Sữa Nestle Milo', 25, 200, 3, 2);
INSERT INTO `products` VALUES (46, 'Sữa Lúa Mạch Nestlé MILO Lon Thùng 24 Lon x 240 ml (4x6x240ml) | Sữa Tươi', 0, '2021-09-11', 'milo-thung.jpg', 'Sữa Lúa Mạch Nestlé MILO', 300, 200, 3, 2);
INSERT INTO `products` VALUES (47, 'THÙNG SỮA ĐẬU NÀNH VINAMILK HẠT ÓC CHÓ -48 HỘP 180ML | Sữa Tươi', 0, '2021-09-11', 'sua-oc-cho.jpg', 'Sữa Hạt Óc Chó', 45, 199, 3, 1);
INSERT INTO `products` VALUES (48, 'Túi Nhân Hạt Óc Chó Dinh Dưỡng Cho Mẹ Real Food (200g) | Dinh dưỡng cho mẹ', 10, '2021-09-12', 'hat-occho.jpg', 'Túi Nhân Hạt Óc Chó', 250, 314, 1, 3);
INSERT INTO `products` VALUES (49, 'Combo 3 hộp sữa hạt dẻ 1L 137 Degrees Thái Lan', 5, '2021-09-05', 'sua-pistachio.jpg', 'Sữa Hạt Dẻ', 45, 100, 3, 2);
INSERT INTO `products` VALUES (54, 'mô tả', 5, '2021-09-09', 'den_mk052_1_0ddcbcb5ca3d4d3e8bb6ac99fcb7c23f_grande.jpg', 'test', 1000000, 20, 1, 1);
INSERT INTO `products` VALUES (59, 'Mô tả tự động từ Selenium', 10, '2025-11-24', '', 'selenium item 1763958352617', 500, 100, 5, 1);
INSERT INTO `products` VALUES (60, '1234', 12, '2001-12-12', '', 'bùi tiến thành', 12, 12, 8, 4);
INSERT INTO `products` VALUES (61, 'Mô tả tự động', 10, '2025-11-24', '02.jpg', 'test auto 1763958597923', 500, 100, 5, 1);
INSERT INTO `products` VALUES (62, 'Mô tả', 10, '2025-11-24', '02.jpg', 'test auto 1763958738729', 500, 100, 5, 1);
INSERT INTO `products` VALUES (63, 'Mô tả', 10, '2025-11-24', '02.jpg', 'test auto 1763958937572', 500, 100, 5, 1);
INSERT INTO `products` VALUES (64, 'Mô tả', 10, '2025-11-24', '02.jpg', 'test auto 1763959195131', 500, 100, 5, 1);
INSERT INTO `products` VALUES (65, 'Mô tả auto', 10, NULL, '02.jpg', 'test auto 1763959469919', 500, 100, 5, 1);
INSERT INTO `products` VALUES (66, 'Desc', 0, NULL, '02.jpg', 'autopro 1763959734824', 100, 10, 12, 1);
INSERT INTO `products` VALUES (67, 'Desc', 0, NULL, '02.jpg', 'autopro 1764043940677', 100, 10, 12, 1);
INSERT INTO `products` VALUES (68, 'Desc', 0, NULL, '02.jpg', 'autopro 1764044012192', 100, 10, 12, 1);
INSERT INTO `products` VALUES (69, 'Desc', 0, NULL, '02.jpg', 'autopro 1764045323544', 100, 10, 12, 1);
INSERT INTO `products` VALUES (71, 'Desc', 0, NULL, '02.jpg', 'autopro 1764045568105', 100, 10, 12, 1);
INSERT INTO `products` VALUES (73, '', NULL, NULL, '', '', 100, 80, 12, 1);
INSERT INTO `products` VALUES (74, 'Desc', 0, NULL, '02.jpg', 'autopro 1764549815818', 100, 10, 12, 1);
INSERT INTO `products` VALUES (76, 'Desc', 0, NULL, '02.jpg', 'autopro 1764550607278', 100, 10, 12, 1);
INSERT INTO `products` VALUES (78, 'Desc', 0, NULL, '02.jpg', 'autopro 1764554924963', 100, 10, 12, 1);
INSERT INTO `products` VALUES (80, 'Desc', 0, NULL, '02.jpg', 'autopro 1764557528099', 100, 10, 12, 1);
INSERT INTO `products` VALUES (82, 'Desc', 0, NULL, '02.jpg', 'autopro 1764561623103', 100, 10, 12, 1);
INSERT INTO `products` VALUES (84, 'Desc', 0, NULL, '02.jpg', 'autopro 1764650215041', 100, 10, 12, 1);
INSERT INTO `products` VALUES (86, 'Desc', 0, NULL, '02.jpg', 'autopro 1764652878658', 100, 10, 12, 1);
INSERT INTO `products` VALUES (87, 'Desc', 0, NULL, '02.jpg', 'autopro 1764653773210', 100, 10, 12, 1);
INSERT INTO `products` VALUES (89, 'Desc', 0, NULL, '02.jpg', 'autopro 1764899107659', 100, 10, 12, 1);

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` int NOT NULL,
  `roleName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (0, 'ROLE_CUSTOMER');
INSERT INTO `roles` VALUES (1, 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for roles_seq
-- ----------------------------
DROP TABLE IF EXISTS `roles_seq`;
CREATE TABLE `roles_seq`  (
  `next_val` bigint NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles_seq
-- ----------------------------
INSERT INTO `roles_seq` VALUES (1);

-- ----------------------------
-- Table structure for suppliers
-- ----------------------------
DROP TABLE IF EXISTS `suppliers`;
CREATE TABLE `suppliers`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `supplierUnique`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of suppliers
-- ----------------------------
INSERT INTO `suppliers` VALUES (1, 'vinamilk@gmail.com', 'Vinamilk', '0915999999');
INSERT INTO `suppliers` VALUES (2, 'nestle@gmail.com', 'Nestle', '0915999988');
INSERT INTO `suppliers` VALUES (3, 'snack@gmail.com', 'Snack', '0915999966');
INSERT INTO `suppliers` VALUES (4, 'cookies@gmail.com', 'Cookies', '0915999666');
INSERT INTO `suppliers` VALUES (5, 'pepsicola@gmail.com', 'Pepsi Cola', '0915998888');
INSERT INTO `suppliers` VALUES (6, 'bibica@gmail.com', 'Bibica', '0915998668');
INSERT INTO `suppliers` VALUES (8, 'supplier_test@gmail.com', 'Test Supplier 1763959211894', '0988777666');
INSERT INTO `suppliers` VALUES (9, 'auto@sup.com', 'Auto Sup 1763959509817', '0123456789');

-- ----------------------------
-- View structure for bill_view
-- ----------------------------
DROP VIEW IF EXISTS `bill_view`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `bill_view` AS select `o`.`orderId` AS `orderId`,`o`.`customerId` AS `customerId`,`c`.`fullname` AS `fullname`,`o`.`phone` AS `phone`,`o`.`address` AS `address`,`o`.`orderDate` AS `orderDate`,`od`.`status` AS `status`,group_concat(concat(`pr`.`name`,' (Giá: ',(`pr`.`price` - ((`pr`.`price` * `pr`.`discount`) / 100)),', Số lượng: ',`od`.`quantity`,')') separator ', ') AS `product_list`,`o`.`total_price` AS `total_price` from (((`orders` `o` join `customers` `c` on((`o`.`customerId` = `c`.`customerId`))) join `orderdetails` `od` on((`o`.`orderId` = `od`.`orderId`))) join `products` `pr` on((`od`.`productId` = `pr`.`productId`))) group by `o`.`orderId`,`o`.`customerId`,`c`.`fullname`,`o`.`phone`,`o`.`address`,`o`.`orderDate`,`od`.`status`,`o`.`total_price` ;

-- ----------------------------
-- View structure for cart_product_view
-- ----------------------------
DROP VIEW IF EXISTS `cart_product_view`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `cart_product_view` AS select `c`.`cartId` AS `cartId`,`c`.`customerId` AS `customerId`,`p`.`name` AS `name`,`p`.`image` AS `image`,`c`.`productId` AS `productId`,`c`.`quantity` AS `quantity`,`p`.`discount` AS `discount`,(`p`.`price` - ((`p`.`price` * `p`.`discount`) / 100)) AS `price`,((`p`.`price` - ((`p`.`price` * `p`.`discount`) / 100)) * `c`.`quantity`) AS `totalPrice` from (`carts` `c` join `products` `p` on((`c`.`productId` = `p`.`productId`))) ;

-- ----------------------------
-- View structure for revenue_view
-- ----------------------------
DROP VIEW IF EXISTS `revenue_view`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `revenue_view` AS select sum(`od`.`total_price`) AS `total_revenue` from (`orderdetails` `od` join `orders` `o` on((`od`.`orderId` = `o`.`orderId`))) where (`od`.`status` = 'Đã Thanh Toán') ;

-- ----------------------------
-- Procedure structure for AddOrUpdateProduct
-- ----------------------------
DROP PROCEDURE IF EXISTS `AddOrUpdateProduct`;
delimiter ;;
CREATE PROCEDURE `AddOrUpdateProduct`(IN p_description VARCHAR(255),
  IN p_discount DOUBLE,
  IN p_enteredDate DATE,
  IN p_image VARCHAR(255),
  IN p_name VARCHAR(255),
  IN p_price DOUBLE,
  IN p_quantity INT,
  IN p_categoryId INT,
  IN p_supplierId INT)
BEGIN
  DECLARE productCount INT;

  -- Loại bỏ khoảng trắng và chuyển đổi tên sản phẩm thành chữ thường
  SET p_name = LOWER(TRIM(p_name));

  -- Kiểm tra xem số lượng đầu vào là NULL hay không
  IF p_quantity IS NULL THEN
    SET p_quantity = 0;
  END IF;

  -- Kiểm tra xem sản phẩm đã tồn tại dựa trên tên (không phân biệt chữ hoa chữ thường)
  SELECT COUNT(*) INTO productCount FROM products WHERE LOWER(TRIM(name)) = p_name;

  IF productCount > 0 THEN
    -- Sản phẩm đã tồn tại, cộng dồn số lượng mới vào số lượng hiện có
    UPDATE products SET quantity = quantity + p_quantity WHERE LOWER(TRIM(name)) = p_name;
  ELSE
    -- Sản phẩm chưa tồn tại, thêm một bản ghi mới
    INSERT INTO products (description, discount, enteredDate, image, name, price, quantity, categoryId, supplierId)
    VALUES (p_description, p_discount, p_enteredDate, p_image, p_name, p_price, p_quantity, p_categoryId, p_supplierId);
  END IF;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for UpdateOrInsertIntoCart
-- ----------------------------
DROP PROCEDURE IF EXISTS `UpdateOrInsertIntoCart`;
delimiter ;;
CREATE PROCEDURE `UpdateOrInsertIntoCart`(IN p_customerid VARCHAR(255), IN p_productid INT)
BEGIN
   DECLARE existing_quantity INT;
   DECLARE existing_price DECIMAL(10, 2);
   
   SELECT quantity, price INTO existing_quantity, existing_price
   FROM carts
   WHERE customerid = p_customerid AND productid = p_productid;

   IF existing_quantity IS NOT NULL THEN
      UPDATE carts
      SET quantity = existing_quantity + 1
      WHERE customerid = p_customerid AND productid = p_productid;
   ELSE
      INSERT INTO carts(customerid, productid, quantity, price)
      SELECT p_customerid, p_productid, 1, (p.price - (p.price * p.discount / 100)) * 1 -- Tính giá trị price từ số lượng, giá và khuyến mãi sản phẩm
      FROM products p
      WHERE p.productId = p_productid;
   END IF;
   
   IF existing_quantity IS NOT NULL THEN
      UPDATE carts
      SET price = (price / existing_quantity) * (existing_quantity + 1)
      WHERE customerid = p_customerid AND productid = p_productid;
   END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table carts
-- ----------------------------
DROP TRIGGER IF EXISTS `check_product_quantity`;
delimiter ;;
CREATE TRIGGER `check_product_quantity` BEFORE UPDATE ON `carts` FOR EACH ROW BEGIN
  DECLARE product_quantity INT;

  SELECT quantity
  INTO product_quantity
  FROM products
  WHERE productId = NEW.productId;

  IF NEW.quantity > product_quantity THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Số lượng mặt hàng trong giỏ hàng không được vượt quá số lượng mặt hàng có trong bảng sản phẩm';
  END IF;

END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table carts
-- ----------------------------
DROP TRIGGER IF EXISTS `delete_cart_item`;
delimiter ;;
CREATE TRIGGER `delete_cart_item` AFTER UPDATE ON `carts` FOR EACH ROW BEGIN
  IF NEW.quantity = 0 THEN
    DELETE FROM carts WHERE cartId = NEW.cartId;
  END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table customers
-- ----------------------------
DROP TRIGGER IF EXISTS `create_cart_for_new_customer`;
delimiter ;;
CREATE TRIGGER `create_cart_for_new_customer` AFTER INSERT ON `customers` FOR EACH ROW BEGIN
    INSERT INTO carts (customerId) VALUES (NEW.customerId);
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table orderdetails
-- ----------------------------
DROP TRIGGER IF EXISTS `updateProductQuantityAfterStatusChange`;
delimiter ;;
CREATE TRIGGER `updateProductQuantityAfterStatusChange` AFTER UPDATE ON `orderdetails` FOR EACH ROW BEGIN
    IF OLD.status = 'Đang Chờ Xử Lý' AND NEW.status = 'Đã Thanh Toán' THEN
        UPDATE products
        SET quantity = quantity - (SELECT quantity FROM orderdetails WHERE orderDetailId = NEW.orderDetailId)
        WHERE productId = NEW.productId;
    END IF;
    
    IF OLD.status = 'Đã Thanh Toán' AND NEW.status = 'Đang Giao Dịch' THEN
        UPDATE products
        SET quantity = quantity + (SELECT quantity FROM orderdetails WHERE orderDetailId = NEW.orderDetailId)
        WHERE productId = NEW.productId;
    END IF;
		
		IF OLD.status = 'Đang Giao Dịch' AND NEW.status = 'Đã Thanh Toán' THEN
        UPDATE products
        SET quantity = quantity - (SELECT quantity FROM orderdetails WHERE orderDetailId = NEW.orderDetailId)
        WHERE productId = NEW.productId;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table orderdetails
-- ----------------------------
DROP TRIGGER IF EXISTS `restoreProductQuantityAfterDelete`;
delimiter ;;
CREATE TRIGGER `restoreProductQuantityAfterDelete` AFTER DELETE ON `orderdetails` FOR EACH ROW BEGIN
        UPDATE products
        SET quantity = quantity + OLD.quantity
        WHERE productId = OLD.productId;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table orders
-- ----------------------------
DROP TRIGGER IF EXISTS `orders_before_insert`;
delimiter ;;
CREATE TRIGGER `orders_before_insert` BEFORE INSERT ON `orders` FOR EACH ROW BEGIN 
  IF NEW.orderDate IS NULL THEN 
    SET NEW.orderDate = NOW(); 
  END IF; 
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
