-- 建立資料庫
drop database IF EXISTS Farmily;
CREATE DATABASE IF NOT EXISTS Farmily CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Farmily;

-- ==========================================
-- 前置/獨立設定表 (無 FK 相依)
-- ==========================================

-- 8. 地址參照
CREATE TABLE CITY_DISTRICT (
                               district_id  INT         PRIMARY KEY AUTO_INCREMENT,
                               city_name    VARCHAR(50) NOT NULL,
                               city_eng     VARCHAR(50) NOT NULL,
                               dist_name    VARCHAR(50) NOT NULL,
                               dist_eng     VARCHAR(50) NOT NULL,
                               zipcode      SMALLINT    NOT NULL,
                               note         VARCHAR(50)
);
-- 參數 (另一個檔案)
-- INSERT INTO CITY_DISTRICT (city_name, city_eng, dist_name, dist_eng, zipcode, note)
-- VALUES ('臺北市', 'Taipei City', '中正區', 'Zhongzheng Dist.', 100, NULL),
-- 		('臺北市', 'Taipei City', '大同區', 'Datong Dist.', 103, NULL),
-- 		('臺北市', 'Taipei City', '中山區', 'Zhongshan Dist.', 104, NULL),
-- 		('基隆市', 'Keelung City', '仁愛區', 'Renai Dist.', 200, NULL),
-- 		('新北市', 'New Taipei City', '板橋區', 'Banqiao Dist.', 220, NULL);

-- 1. 會員系統 - 消費級距
CREATE TABLE SPENDING_TIER (
                               tier_id     INT         PRIMARY KEY,
                               tier_name   VARCHAR(20),
                               min_amount  INT         NOT NULL,
                               max_amount  INT,
                               description VARCHAR(100)
);
-- 參數
INSERT INTO SPENDING_TIER (tier_id, tier_name, min_amount, max_amount, description)
VALUES
    (1, '一般會員', 0,    0,    '尚無消費紀錄'),
    (2, '銅級會員', 1,    1000, '每月消費 $1 – $1,000'),
    (3, '銀級會員', 1001, 3000, '每月消費 $1,001 – $3,000'),
    (4, '金級會員', 3001, NULL, '每月消費 $3,001 以上');

-- 2. 管理員 - 功能權限
CREATE TABLE ADMIN_ROLE (
                            permission_id   INT         AUTO_INCREMENT PRIMARY KEY,
                            permission_name VARCHAR(50),
                            permission_code VARCHAR(30) NOT NULL UNIQUE,
                            description     VARCHAR(100)
);
-- 參數 (種子資料)
INSERT INTO ADMIN_ROLE (permission_name, permission_code, description)
VALUES
    ('最新消息管理',       'NEWS',        '刊登/修改/刪除、即時資訊更新'),
    ('專欄部落格內容管理', 'BLOG',        '檢舉處理、刪除違規文章'),
    ('會員管理',           'MEMBER',      '消費者會員資料管理、權限控管'),
    ('小農管理',           'FARMER',      '小農帳號審核、資料管理、權限控管'),
    ('管理員管理',         'ADMIN',       '管理員帳號新增/修改/刪除、權限控管'),
    ('商城管理',           'SHOP',        '管理優惠券、資料統計、爭議處理'),
    ('團購管理',           'GROUP_BUY',   '爭議處理'),
    ('體驗活動管理',       'EVENT',       '活動審核、瀏覽、搜尋、狀態管理'),
    ('市場數據管理',       'MARKET_DATA', '數據更新'),
    ('網頁流量分析',       'ANALYTICS',   '用戶增加趨勢、訂單數量統計');

-- 2. 管理員 - 管理員帳號
CREATE TABLE ADMIN (
                       admin_id       INT          AUTO_INCREMENT PRIMARY KEY,
                       admin_email    VARCHAR(100) NOT NULL UNIQUE,
                       admin_password VARCHAR(255),
                       admin_name     VARCHAR(50),
                       admin_status   ENUM('ACTIVE', 'SUSPENDED', 'DELETED'),
                       created_at     DATETIME     NOT NULL,
                       updated_at     DATETIME
);
-- 參數
INSERT INTO ADMIN (admin_email, admin_password, admin_name, admin_status, created_at, updated_at) VALUES
                                                                                                      ('admin01@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '王管理', 'ACTIVE',    '2024-01-01 09:00:00', '2024-06-15 10:30:00'),
                                                                                                      ('admin02@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '林管理', 'ACTIVE',    '2024-01-02 09:00:00', '2024-07-20 14:00:00'),
                                                                                                      ('admin03@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '陳管理', 'ACTIVE',    '2024-02-10 09:00:00', NULL),
                                                                                                      ('admin04@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '張管理', 'SUSPENDED', '2024-03-05 09:00:00', '2024-09-01 08:00:00'),
                                                                                                      ('admin05@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '黃管理', 'DELETED',   '2024-04-20 09:00:00', '2025-01-10 17:00:00');

-- 3-1. 農場商品 - 商品大類表
CREATE TABLE MAINCATEGORY (
    product_main_cat_id INT AUTO_INCREMENT PRIMARY KEY,
    product_main_cat_name VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO MAINCATEGORY (product_main_cat_id, product_main_cat_name) VALUES
(1, '水果');


-- 3-6. 農場商品 - 優惠卷
CREATE TABLE COUPON (
    coupon_id VARCHAR(50) PRIMARY KEY,
    coupon_info VARCHAR(255) NOT NULL,
    issue_start_date DATETIME,
    issue_end_date DATETIME,
    amount INT,
    min_spending INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO COUPON (coupon_id, coupon_info, issue_start_date, issue_end_date, amount, min_spending) VALUES
('WELCOME100', '新會員首購滿500折100元', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 500);


-- 7. 通知 - 通知類型定義表
CREATE TABLE NOTIFICATION_TYPE (
    type_code VARCHAR(40) PRIMARY KEY,
    template_zh VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO NOTIFICATION_TYPE (type_code, template_zh) VALUES
('payment_held', '付款成功，款項暫時凍結，待確認收貨後撥款給小農。');


-- 6. 專欄部落格 - 部落格類別
CREATE TABLE BLOG_TYPE (
    blog_type_id INT AUTO_INCREMENT PRIMARY KEY,
    blog_type_name VARCHAR(15),
    blog_type_img LONGBLOB,
    blog_type_text VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_TYPE (blog_type_id, blog_type_name, blog_type_img, blog_type_text) VALUES
(1, '產地日記', NULL, '分享農友日常、作物成長與產地故事'),
(2, '蔬果知識分享', NULL, '蔬果挑選、保存、營養小百科'),
(3, '農作體驗回顧', NULL, '參加體驗活動或產地參訪的心得'),
(4, '食譜分享', NULL, '在家就能做的料理');


-- ==========================================
-- 一、會員與小農表 (依賴地址)
-- ==========================================

-- 1. 會員系統 - 一般會員
CREATE TABLE USER (
                      user_id          INT          PRIMARY KEY AUTO_INCREMENT,
                      email            VARCHAR(255)  UNIQUE,
                      district_id      INT,
                      user_address     VARCHAR(100),
                      email_verified   BOOLEAN,
                      password         VARCHAR(255),
                      user_created_at  DATETIME,
                      user_name        VARCHAR(100),
                      user_nickname    VARCHAR(100),
                      birthday         DATE,
                      user_phone_num   VARCHAR(50),
                      user_status      ENUM('ACTIVE', 'WARNED', 'SUSPENDED', 'DELETED') DEFAULT 'ACTIVE',
                      monthly_spending INT          NOT NULL DEFAULT 0,
                      auth_provider    ENUM('LOCAL', 'GOOGLE') NOT NULL DEFAULT 'LOCAL',
                      provider_id      VARCHAR(255),
                      FOREIGN KEY (district_id) REFERENCES CITY_DISTRICT(district_id)
);
-- 參數
INSERT INTO USER
(email, district_id, user_address, email_verified, password, user_created_at,
 user_name, user_nickname, birthday, user_phone_num, user_status,
 monthly_spending, auth_provider, provider_id)
VALUES
    ('a0928826097',  2, '桃園區提八路1號',      TRUE, '$2a$12$jUA/0ty7AeeXWaKjysIHheumlQfBRupsGrmcrFYRzzzii759aGsze', '2026-07-03 18:50:11', 'chris',     '06chris', '1997-03-16', '0928826097', 'ACTIVE',  8000,  'LOCAL',  NULL);
-- #     ('user2@example.com',  2, '桃園區中正路50號',          TRUE, '$2b$12$Husx6NWgvI7KFkVszwGPD.p2FiEv1hdScVX.NDZKu9WyUS2l1/oXG', '2024-02-03 14:05:47', '林淑惠',     '惠惠', '1988-07-22', '0922333444', 'WARNED',  2150, 'LOCAL',  NULL),
-- #     ('userg3@gmail.com',   3, '信義區松高路11號',          TRUE, '$2b$12$9v39B9kiotoUje71Cn2luO5cNyU0BixHrOTkJxl8j03pzHKns1XCq', '2024-06-01 13:22:09', 'Alex Tsai', '小蔡', '1993-04-08', '0977888999', 'ACTIVE',  4200, 'LOCAL',  NULL),
-- #     ('userg4@gmail.com',   4, '臺中市西屯區臺灣大道300號',  TRUE, NULL, '2024-06-14 10:11:44', 'Jessica Wu', 'Jess', '1996-12-14', '0911222333', 'ACTIVE',  760,  'GOOGLE', '113985620174639205841'),
-- #     ('userg5@gmail.com',   5, '高雄市鳳山區青年路200號',    TRUE, NULL, '2024-07-08 16:38:27', 'Kevin Lin',  NULL,   '1990-08-19', '0922888777', 'ACTIVE',  0,    'GOOGLE', '102758493016283749561');



-- 2. 管理員 - 小農申請案件 (FARMER 依賴此表的 review_id)
CREATE TABLE FARMER_REVIEW (
                               review_id             INT           AUTO_INCREMENT PRIMARY KEY,
                               farmer_id             INT           NULL,
                               admin_id              INT,
                               review_round          INT           NOT NULL DEFAULT 1,
                               review_status         ENUM('PENDING', 'REVIEWING', 'APPROVED', 'REJECTED') NOT NULL,  -- 管理員視角
                               submitted_at          DATETIME      NOT NULL,
                               reviewed_at           DATETIME,
                               reject_reason         VARCHAR(255),
                               notes                 TEXT,
                               cert_file_land        LONGBLOB,
                               cert_file_product     LONGBLOB,
                               cert_file_identity    LONGBLOB,
    -- 重審暫存欄位：小農申請變更的內容，審核通過後才寫回 FARMER --
                               submitted_farm_name    VARCHAR(50)   NULL,
                               submitted_farm_address VARCHAR(100)  NULL,
                               submitted_district_id  INT           NULL,
                               submitted_loc_lat      DECIMAL(10,8) NULL,
                               submitted_loc_long     DECIMAL(11,8) NULL,
                               FOREIGN KEY (farmer_id)            REFERENCES FARMER(farmer_id),
                               FOREIGN KEY (admin_id)             REFERENCES ADMIN(admin_id),
                               CONSTRAINT fk_review_submitted_district
                                   FOREIGN KEY (submitted_district_id) REFERENCES CITY_DISTRICT(district_id)
);
-- 參數
-- 第 2、3 筆同屬 farmer_id = 2，示範「一個 farmer 多筆審核」
-- proposed_* 僅在「申請變更農場資料」時填值；首次申請留 NULL
INSERT INTO FARMER_REVIEW
(farmer_id, admin_id, review_round, review_status, submitted_at, reviewed_at, reject_reason, notes,
 cert_file_land, cert_file_product, cert_file_identity,
 submitted_farm_name, submitted_farm_address, submitted_district_id, submitted_loc_lat, submitted_loc_long)
VALUES
    (1, 1, 1, 'APPROVED',  '2024-02-01 10:00:00', '2024-02-03 14:00:00', NULL,             '資料齊全，審核通過',
     NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL),
    (2, 2, 1, 'REJECTED',  '2024-03-10 09:00:00', '2024-03-12 11:00:00', '土地文件不清晰', '請重新上傳土地證明',
     NULL, NULL, NULL,
     '綠野有機農場', '大同街250號', 2, 25.06400000, 121.51300000),
    (2, 1, 2, 'APPROVED',  '2024-03-20 10:00:00', '2024-03-22 15:00:00', NULL,             '補件後審核通過，已更新農場資料',
     NULL, NULL, NULL,
     '綠野有機農場', '大同街250號', 2, 25.06400000, 121.51300000),
    (3, 3, 1, 'REVIEWING', '2024-05-15 08:00:00', NULL,                  NULL,             '農場遷址審核中',
     NULL, NULL, NULL,
     '山間農場',     '中山路888號', 4, 25.13000000, 121.74000000),
    (5, 3, 1, 'PENDING',   '2024-06-01 13:00:00', NULL,                  NULL,             NULL,
     NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL);




-- 1. 會員系統 - 小農會員
CREATE TABLE FARMER (
                        farmer_id         INT           AUTO_INCREMENT PRIMARY KEY,
                        email             VARCHAR(255)  UNIQUE,
                        email_verified    BOOLEAN,
                        district_id       INT,
                        uploaded_at       DATETIME,
                        password          VARCHAR(255),
                        farm_address      VARCHAR(100),
                        farm_name         VARCHAR(50),
                        loc_lat           DECIMAL(10,8),
                        loc_long          DECIMAL(11,8),
                        farm_desc         TEXT,
                        farmer_created_at DATETIME,
                        farmer_phone_num  VARCHAR(15),
                        farmer_status     ENUM('PENDING', 'ACTIVE', 'SUSPENDED') DEFAULT 'PENDING',
                        FOREIGN KEY (district_id) REFERENCES CITY_DISTRICT(district_id)
);
-- 參數
-- 密碼: farmer
INSERT INTO FARMER (email, email_verified, district_id, uploaded_at, password, farm_address, farm_name, loc_lat, loc_long, farm_desc, farmer_created_at, farmer_phone_num, farmer_status)
VALUES
    ('farmer01@gmail.com', TRUE, 1, '2024-02-01 10:00:00', '$2a$12$lVBrPeTNgXO3YecDS6eh2O0.yFKD8on95ekcUanvypsNDVGpZvj/y', '中正路500號', '陽光農場', 25.04776000, 121.53185000, '專注有機蔬菜栽培，堅持無農藥',   '2024-02-03 14:00:00', '0911111111', 'ACTIVE'),
    ('farmer02@gmail.com', TRUE, 2, '2024-03-10 09:00:00', '$2a$12$lVBrPeTNgXO3YecDS6eh2O0.yFKD8on95ekcUanvypsNDVGpZvj/y', '大同街200號', '綠野農場', 25.06321000, 121.51234000, '自然農法種植，提供當季新鮮蔬果', '2024-03-22 15:00:00', '0922222222', 'ACTIVE'),
    ('farmer03@gmail.com', TRUE, 3, '2024-05-15 08:00:00', '$2a$12$lVBrPeTNgXO3YecDS6eh2O0.yFKD8on95ekcUanvypsNDVGpZvj/y', '中山路333號', '山間農場', 24.98765000, 121.54321000, '山區有機農場，專售高山茶與蔬菜', '2024-05-15 08:00:00', '0933333333', 'PENDING'),
    ('farmer04@gmail.com', TRUE, 4, '2024-03-10 09:00:00', '$2a$12$lVBrPeTNgXO3YecDS6eh2O0.yFKD8on95ekcUanvypsNDVGpZvj/y', '仁愛路88號',  '海風農場', 25.12345000, 121.73456000, '靠海農場，專售海鹽與特色農產品', '2024-03-10 09:00:00', '0944444444', 'SUSPENDED'),
    ('farmer05@gmail.com', TRUE, 5, '2024-06-01 13:00:00', '$2a$12$lVBrPeTNgXO3YecDS6eh2O0.yFKD8on95ekcUanvypsNDVGpZvj/y', '板橋路77號',  '稻香農場', 24.87654000, 121.45678000, '傳統水稻種植，提供在地新鮮稻米', '2024-06-01 13:00:00', '0955555555', 'PENDING');


-- ==========================================
-- 二、依賴 User, Farmer, Admin 的進階表
-- ==========================================

-- 2. 管理員 - 管理功能對照
CREATE TABLE ADMIN_PERMISSION_ROLE (
                                       admin_id      INT,
                                       permission_id INT,
                                       PRIMARY KEY (admin_id, permission_id),
                                       FOREIGN KEY (admin_id)      REFERENCES ADMIN(admin_id),
                                       FOREIGN KEY (permission_id) REFERENCES ADMIN_ROLE(permission_id)
);
-- 參數
INSERT INTO ADMIN_PERMISSION_ROLE (admin_id, permission_id)
VALUES
-- admin1（王管理）：全權限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
-- admin2（林管理）：NEWS, BLOG, MEMBER —— 故意不給 FARMER，當「對比帳號」
(2, 1), (2, 2), (2, 3),
-- admin3（陳管理）：MEMBER, FARMER, SHOP, GROUP_BUY
(3, 3), (3, 4), (3, 6), (3, 7),
-- admin4（張管理，已停權）：EVENT, MARKET_DATA
(4, 8), (4, 9),
-- admin5（黃管理，已刪除）：ANALYTICS
(5, 10);


-- 2. 管理員 - 最新消息
CREATE TABLE NEWS (
    news_id      INT          AUTO_INCREMENT PRIMARY KEY,
    admin_id     INT          NOT NULL,
    title        VARCHAR(50),
    content      VARCHAR(500),
    cover_image  LONGBLOB,
    publish_time DATETIME,
    news_status  ENUM('VISIBLE', 'HIDDEN', 'DRAFT'),
    created_at   DATETIME,
    FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
);
-- 參數
INSERT INTO NEWS (admin_id, title, content, cover_image, publish_time, news_status, created_at)
VALUES
(1, '平台正式上線公告',     '你儂我農平台正式上線，歡迎小農與消費者加入我們的大家庭！',                       NULL, '2024-01-15 09:00:00', 'VISIBLE', '2024-01-14 18:00:00'),
(2, '春季蔬果產季開始',     '春季蔬果陸續上架，歡迎選購當季新鮮農產品。',                                   NULL, '2024-03-01 09:00:00', 'VISIBLE', '2024-02-28 17:00:00'),
(1, '系統維護通知',         '平台將於 2024/04/10 凌晨 2:00 – 4:00 進行系統維護，期間暫停服務。',           NULL, '2024-04-08 12:00:00', 'VISIBLE', '2024-04-08 10:00:00'),
(3, '小農申請流程更新說明', '即日起小農申請流程新增第二階段審核，請備妥相關證明文件再行送件。',               NULL, '2024-05-20 10:00:00', 'HIDDEN',  '2024-05-19 16:00:00'),
(2, '端午節限定農產品上架', '多款端午節限定農產品即將上架，敬請期待！',                                       NULL, '2024-06-05 09:00:00', 'VISIBLE', '2024-06-04 15:00:00');

-- 7. 通知 - 通知
CREATE TABLE NOTIFICATION (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    farmer_id INT,
    created_at DATETIME,
    content VARCHAR(500),
    status ENUM('UNREAD', 'READ'),

    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO NOTIFICATION (notification_id, user_id, farmer_id, created_at, content, status) VALUES
(1, 1, 1, '2026-02-15 09:00:00', '付款成功，款項暫時凍結，待確認收貨後撥款給小農。', 'UNREAD');


-- 3-7. 農場商品 - 優惠卷明細
CREATE TABLE COUPON_DETAILS (
    user_id INT,
    coupon_id VARCHAR(50),
    status ENUM('UNUSED', 'USED', 'EXPIRED'),

    PRIMARY KEY (user_id, coupon_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (coupon_id) REFERENCES COUPON(coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO COUPON_DETAILS (user_id, coupon_id, status) VALUES
(1, 'WELCOME100', 'UNUSED');


-- 3-2. 農場商品 - 商品子類表
CREATE TABLE SUBCATEGORY (
    sub_cat_class_id INT AUTO_INCREMENT PRIMARY KEY,
    product_main_cat_id INT NOT NULL,
    sub_cat_class_name VARCHAR(50) NOT NULL,
    FOREIGN KEY (product_main_cat_id) REFERENCES MAINCATEGORY(product_main_cat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO SUBCATEGORY (sub_cat_class_id, product_main_cat_id, sub_cat_class_name) VALUES
(1, 1, '香蕉');


-- 3-3. 農場商品 - 商城農場產品
CREATE TABLE PRODUCT_DETAIL (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    sub_cat_class_id INT NOT NULL,
    farmer_id INT NOT NULL,
    retail_price INT NOT NULL,
    group_price INT,
    unit_pricing_measure VARCHAR(20) NOT NULL,
    product_image LONGBLOB,
    is_group_buy BOOLEAN,
    description VARCHAR(500),
    status ENUM('ACTIVE', 'INACTIVE'),
    product_name varchar(20),
    FOREIGN KEY (sub_cat_class_id) REFERENCES SUBCATEGORY(sub_cat_class_id),
    FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO PRODUCT_DETAIL (product_id, sub_cat_class_id, farmer_id, retail_price, group_price, unit_pricing_measure, product_image, is_group_buy, description, status,product_name) VALUES
(1, 1, 1, 120, 100, '包/300g', NULL, 1, '無毒香蕉', 'ACTIVE','屏東霸王農場香蕉'),
(2, 1, 2, 180, 150, '盒/600g', NULL, 1, '無毒香蕉。', 'ACTIVE','桃園香蕉'),
(3, 1, 3, 250, 220, '箱/3kg', NULL, 1, '屏東產地直送香蕉，香氣濃郁。', 'ACTIVE','屏東香蕉'),
(4, 1, 4, 160, 140, '包/500g', NULL, 1, '花蓮有機香蕉，鬆甜綿密，適合蒸烤。', 'ACTIVE','花蓮香蕉'),
(5, 1, 5, 199, 169, '箱/6入', NULL, 1, '城市小田香蕉箱。', 'ACTIVE','哈哈農場香蕉水果箱');


-- 3-4. 農場商品 - 產品圖片
CREATE TABLE PRODUCT_IMAGE (
    product_image_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    product_image LONGBLOB,
    FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO PRODUCT_IMAGE (product_image_id, product_id, product_image) VALUES
(1, 1, NULL);


-- 3-5. 農場商品 - 一般會員商品收藏列表
CREATE TABLE GENERAL_MEMBER_PRODUCT_WISHLIST (
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (product_id, user_id),
    FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO GENERAL_MEMBER_PRODUCT_WISHLIST (product_id, user_id) VALUES
(1, 1);


-- 3-8. 農場商品 - 產品購物車
CREATE TABLE PRODUCT_SHOPPING_CART (
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (product_id, user_id),
    FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO PRODUCT_SHOPPING_CART (product_id, user_id, quantity) VALUES
(1, 1, 2);


-- 3-9. 農場商品 - 訂單 (將 ORDER 改為 ORDERS 避開保留字)
CREATE TABLE ORDERS (
    order_id INT PRIMARY KEY,
    user_id INT NOT NULL,
    farmer_id INT NOT NULL,
    coupon_id VARCHAR(50),
    order_date DATETIME NOT NULL,
    shipping_address VARCHAR(100) NOT NULL,
    payment_id INT NOT NULL,
    paid_status VARCHAR(20),
    paid_datetime DATETIME,
    refunded_at DATETIME,
    total_amount INT NOT NULL,
    discount_amount INT NOT NULL,
    final_payment INT NOT NULL,
    shipping_date DATETIME,
    shipping_status VARCHAR(20),
    receipt_datetime DATETIME,
    receipt_status ENUM('RECEIVED', 'NOT_RECEIVED'),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id),
    FOREIGN KEY (coupon_id) REFERENCES COUPON(coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO ORDERS (order_id, user_id, farmer_id, coupon_id, order_date, shipping_address, payment_id, paid_status, paid_datetime, refunded_at, total_amount, discount_amount, final_payment, shipping_date, shipping_status, receipt_datetime, receipt_status) VALUES
(1, 1, 1, 'WELCOME100', '2026-03-01 14:00:00', '臺北市中正區重慶南路一段122號', 8881, 'PAID', '2026-03-01 14:05:00', NULL, 240, 100, 140, '2026-03-02 10:00:00', 'SHIPPED', '2026-03-03 15:30:00', 'RECEIVED');


-- 3-10. 農場商品 - 訂單明細
CREATE TABLE ORDER_ITEM (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    order_id INT NOT NULL,
    quantity INT NOT NULL,
    price INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id),
    FOREIGN KEY (order_id) REFERENCES ORDERS(order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO ORDER_ITEM (order_item_id, product_id, order_id, quantity, price) VALUES
(1, 1, 1, 2, 120);


-- ==========================================
-- 三、團購模組 (依賴 Product, User)
-- ==========================================

-- 4. 團購 - 團購活動表
CREATE TABLE GROUP_BUY (
    group_buy_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    host_user_id INT,
    target_amount INT NOT NULL,
    group_price INT,
    open_datetime DATETIME,
    ddl_datetime DATETIME,
    status ENUM('open','success','failed','cancelled','pending') NOT NULL DEFAULT 'pending',
    created_at DATETIME,
    request_status ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending',
    request_datetime DATETIME,
    reply_datetime DATETIME,
    reject_reason VARCHAR(255),
    pickup_address VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id),
    FOREIGN KEY (host_user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO GROUP_BUY (group_buy_id,product_id,host_user_id,target_amount,group_price,open_datetime,ddl_datetime,`status`,created_at,request_status,request_datetime,reply_datetime,reject_reason,pickup_address
) VALUES
(1, 3, 3, 3000, 220, '2026-03-05 00:00:00', '2026-03-12 23:59:59', 'open', '2026-03-04 18:00:00', 'approved', '2026-03-04 10:00:00', '2026-03-04 11:00:00', NULL,'桃園市中壢區復興路46號9樓'),
(2, 4, 5, 5000, 180, NULL, '2026-03-20 23:59:59', 'pending', '2026-03-10 09:00:00', 'pending', '2026-03-10 09:00:00', NULL, NULL,'桃園市蘆竹區南崁路一段10號'),
(3, 5, 2, 2000, 350, NULL, '2026-03-18 23:59:59', 'cancelled', '2026-03-09 14:30:00', 'rejected', '2026-03-09 14:30:00', '2026-03-09 16:00:00', '商品庫存不足，無法開團','台北市中山區中正路66號');


-- 4. 團購 - 團購參與記錄表
CREATE TABLE GB_PARTICIPATION (
    participation_id INT AUTO_INCREMENT PRIMARY KEY,
    group_buy_id INT,
    user_id INT,
    is_host TINYINT,
    buy_qty INT,
    join_datetime DATETIME,
    join_status ENUM('active','cancelled') DEFAULT 'active',
    paid_amount INT,
    paid_datetime DATETIME,
    FOREIGN KEY (group_buy_id) REFERENCES GROUP_BUY(group_buy_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO GB_PARTICIPATION (participation_id, group_buy_id, user_id, is_host, buy_qty, join_datetime, join_status, paid_amount, paid_datetime) VALUES
(1, 1, 3, 1, 5, '2026-03-05 08:30:00', 'active', 1100, '2026-03-05 08:35:00'),
(2, 1, 1, 0, 6, '2026-03-06 10:20:00', 'active', 1320, '2026-03-06 10:25:00'),
(3, 1, 2, 0, 4, '2026-03-07 15:10:00', 'active', 880, '2026-03-07 15:15:00');


-- 4. 團購 - 團購訂單
CREATE TABLE GB_ORDER (
    order_id INT PRIMARY KEY,
    group_buy_id INT,
    total_quantity INT,
    group_price INT,
    total_amount INT,
    shipped_status ENUM('PENDING','SHIPPED','DELIVERED','CANCELED') DEFAULT 'PENDING',
    shipped_at DATETIME,
    tracking_num VARCHAR(50),
    created_at DATETIME,
    received_at DATETIME,
    order_status ENUM('PENDING', 'COMPLETED', 'CANCELED'),
    paid_status ENUM('UNPAID', 'PAID', 'REFUNDED'),
    completed_at DATETIME,
    FOREIGN KEY (group_buy_id) REFERENCES GROUP_BUY(group_buy_id)


) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO GB_ORDER (order_id, group_buy_id, total_quantity, group_price, total_amount, shipped_status, shipped_at, tracking_num, created_at, received_at, order_status, paid_status, completed_at) VALUES
(90001, 1, 15, 220, 3300, 'PENDING', NULL, NULL, '2026-03-12 23:59:59', NULL, 'PENDING', 'PAID', NULL);


-- 4. 團購 - 團購收藏表
CREATE TABLE GB_WISHLIST (
    user_id INT,
    group_buy_id INT,
    saved_datetime DATETIME,
    PRIMARY KEY (user_id, group_buy_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (group_buy_id) REFERENCES GROUP_BUY(group_buy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO GB_WISHLIST (
    user_id,
    group_buy_id,
    saved_datetime
) VALUES
(1, 1, '2026-03-06 12:00:00'),
(2, 1, '2026-03-06 13:30:00'),
(5, 1, '2026-03-07 09:15:00');


-- ==========================================
-- 四、體驗活動模組 (依賴 Farmer, User, Admin)
-- ==========================================

-- 5. 體驗活動 - 體驗活動
CREATE TABLE FARM_TRIP (
    farm_trip_id INT AUTO_INCREMENT PRIMARY KEY,
    farmer_id INT NOT NULL,
    farm_trip_type ENUM('FARM_EXPERIENCE','FIELD_VISIT'),
    farm_trip_title VARCHAR(30),
    farm_trip_pic LONGBLOB,
    farm_trip_intro VARCHAR(500),
    location VARCHAR(100),
    refer_price INT,
    status ENUM('PENDING','REJECTED','ACTIVE','CLOSED'),
    comment_numbers INT,
    star_numbers INT,
    FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP (farm_trip_id, farmer_id, farm_trip_type, farm_trip_title, farm_trip_pic, farm_trip_intro, location, refer_price, status, comment_numbers, star_numbers) VALUES
(5001, 1, 'FARM_EXPERIENCE','有機蔬菜採收體驗',NULL,'走進陽光農場認識有機蔬菜，親手完成採收與清洗。','臺北市中正區中正路500號',500, 'ACTIVE', 1, 5),

(5002, 2, 'FIELD_VISIT','自然農法農場導覽',NULL,'由農友介紹自然農法、土壤照護及友善環境的栽培方式。','臺北市大同區大同街200號',300, 'ACTIVE', 1, 5),

(5003, 2, 'FARM_EXPERIENCE','當季蔬果採收體驗',NULL,'親手採收當季蔬果，學習辨認成熟度及正確保存方法。','臺北市大同區大同街200號', 450, 'ACTIVE', 1, 5),

(5004, 3, 'FARM_EXPERIENCE','高山茶採摘製茶體驗', NULL, '體驗茶葉採摘、揉捻與烘焙，認識茶葉從產地到茶杯的過程。','臺北市中山區中山路333號', 800, 'ACTIVE', 1, 4),

(5005, 5, 'FARM_EXPERIENCE','稻田插秧與米食體驗',NULL,'走入稻田體驗插秧，並認識稻米生長及傳統米食製作。','新北市板橋區板橋路77號',600, 'ACTIVE', 1, 4);



-- 5. 體驗活動 - 體驗活動場次
CREATE TABLE FARM_TRIP_SESSION (
    farm_session_id INT AUTO_INCREMENT PRIMARY KEY,
    farm_trip_id INT NOT NULL,
    farm_trip_start DATETIME,
    farm_trip_end DATETIME,
    trip_book_start DATETIME,
    trip_book_end DATETIME,
    attendance INT,
    session_status ENUM('ACTIVE','CANCELLED','COMPLETED'),
    FOREIGN KEY (farm_trip_id) REFERENCES FARM_TRIP(farm_trip_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP_SESSION (farm_session_id, farm_trip_id, farm_trip_start, farm_trip_end, trip_book_start, trip_book_end, attendance, session_status) VALUES
(6001, 5001,
 '2026-04-10 09:00:00', '2026-04-10 15:00:00','2026-03-10 00:00:00', '2026-04-08 23:59:59',30, 'COMPLETED'),

(6002, 5002, '2026-04-17 09:00:00', '2026-04-17 12:00:00', '2026-03-15 00:00:00', '2026-04-15 23:59:59', 25, 'COMPLETED'),

(6003, 5003, '2026-04-24 08:30:00', '2026-04-24 14:00:00', '2026-03-20 00:00:00', '2026-04-22 23:59:59', 20, 'COMPLETED'),

(6004, 5004, '2026-05-08 08:00:00', '2026-05-08 16:00:00', '2026-04-01 00:00:00', '2026-05-06 23:59:59', 15, 'COMPLETED'),

(6005, 5005, '2026-05-15 09:00:00', '2026-05-15 16:00:00', '2026-04-05 00:00:00', '2026-05-13 23:59:59', 20, 'COMPLETED');


-- 5. 體驗活動 - 體驗活動審核
CREATE TABLE FARM_TRIP_AUDITS (
    farm_trip_audits_id INT AUTO_INCREMENT PRIMARY KEY,
    farm_trip_id INT NOT NULL,
    admin_id INT NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED'),
    reason VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (farm_trip_id) REFERENCES FARM_TRIP(farm_trip_id),
    FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP_AUDITS (farm_trip_audits_id, farm_trip_id, admin_id, status, reason, created_at, updated_at) VALUES
(7001, 5001, 1, 'APPROVED', '活動流程完整，場地資訊清楚。', '2026-03-05 14:00:00', '2026-03-05 14:00:00'),

(7002, 5002, 2, 'APPROVED', '導覽內容及安全規劃符合要求。', '2026-03-10 10:00:00', '2026-03-10 11:00:00'),

(7003, 5003, 1, 'APPROVED','採收流程與參加規範說明完整。','2026-03-15 09:00:00', '2026-03-15 10:30:00'),

(7004, 5004, 3, 'APPROVED','製茶體驗設備及安全措施符合規定。','2026-03-25 13:00:00', '2026-03-25 15:00:00'),

(7005, 5005, 2, 'APPROVED','插秧與米食活動流程規劃完善。', '2026-03-30 10:00:00', '2026-03-30 12:00:00');


-- 5. 體驗活動 - 體驗活動預約訂單
CREATE TABLE FARM_TRIP_ORDER (
    farm_trip_order_id INT AUTO_INCREMENT PRIMARY KEY,
    farm_session_id INT NOT NULL,
    user_id INT NOT NULL,
    farm_trip_order_booking_no VARCHAR(30),
    num_people INT NOT NULL,
    status ENUM('CONFIRMED','CANCELLED','COMPLETED'),
    booked_at DATETIME NOT NULL,
    cancelled_at DATETIME,
    completed_at DATETIME,
    user_name VARCHAR(30) NOT NULL,
    user_phone_num VARCHAR(15) NOT NULL,
    note VARCHAR(100),
    FOREIGN KEY (farm_session_id) REFERENCES FARM_TRIP_SESSION(farm_session_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP_ORDER (farm_trip_order_id, farm_session_id, user_id, farm_trip_order_booking_no, num_people, status, booked_at, cancelled_at, completed_at, user_name, user_phone_num, note) VALUES
(8001, 6001, 5, 'TRIP-20260410-001',2, 'COMPLETED','2026-03-15 10:00:00', NULL, '2026-04-10 15:10:00','黃雅婷', '0956789012','第一次參加農村體驗，希望有導覽說明。'),

(8002, 6002, 1, 'TRIP-20260417-001', 2, 'COMPLETED', '2026-03-20 11:00:00', NULL, '2026-04-17 12:10:00', '陳小美', '0912345678', '希望了解自然農法與土壤照護。'),

(8003, 6003, 2, 'TRIP-20260424-001', 3, 'COMPLETED', '2026-03-25 14:30:00', NULL, '2026-04-24 14:10:00', '林大明', '0923456789', '三人同行，想體驗當季蔬果採收。'),

(8004, 6004, 3, 'TRIP-20260508-001', 1, 'COMPLETED', '2026-04-10 09:20:00', NULL, '2026-05-08 16:10:00', '王小華', '0934567890', '對茶葉製作過程很有興趣。'),

(8005, 6005, 5, 'TRIP-20260515-001', 2, 'COMPLETED', '2026-04-15 16:00:00', NULL, '2026-05-15 16:10:00', '黃雅婷', '0956789012', '希望體驗插秧與傳統米食製作。');
-- 5. 體驗活動 - 體驗活動評論
CREATE TABLE FARM_TRIP_COMMENT (
    farm_trip_comment_id INT AUTO_INCREMENT PRIMARY KEY,
    farm_trip_id INT NOT NULL,
    user_id INT NOT NULL,
    star INT,
    content VARCHAR(255),
    created_at DATETIME,
    FOREIGN KEY (farm_trip_id) REFERENCES FARM_TRIP(farm_trip_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP_COMMENT (farm_trip_comment_id, farm_trip_id, user_id, star, content, created_at) VALUES
(1, 5001, 5, 5, '有機蔬菜採收體驗很有趣，農友解說得非常仔細。', '2026-04-11 10:00:00'),

(2, 5002, 1, 4, '導覽內容很豐富，讓我更了解自然農法。', '2026-04-18 11:00:00'),

(3, 5003, 2, 5, '親手採收蔬果非常有成就感，適合親子參加。', '2026-04-25 09:30:00'),

(4, 5004, 3, 5, '從採茶到製茶都能親自參與，是很難得的體驗。', '2026-05-09 10:00:00'),

(5, 5005, 5, 4, '插秧很有趣，米食製作活動也很好吃。', '2026-05-16 11:00:00');


-- ==========================================
-- 五、專欄部落格模組 (依賴 User, Farmer, Product, Admin, Blog_Type)
-- ==========================================

-- 6. 專欄部落格 - 部落格文章
CREATE TABLE BLOG (
    blog_id INT AUTO_INCREMENT PRIMARY KEY,
    blog_title VARCHAR(30) NOT NULL,
    user_id INT,
    farmer_id INT,
    blog_type_id INT,
    product_id INT NOT NULL,
    blog_content TEXT NOT NULL,
    blog_img LONGBLOB,
    blog_like_count INT NOT NULL,
    blog_time DATETIME NOT NULL,
    blog_status ENUM('VISIBLE','HIDDEN'),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id),
    FOREIGN KEY (blog_type_id) REFERENCES BLOG_TYPE(blog_type_id),
    FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG (blog_id, blog_title, user_id, farmer_id, blog_type_id, product_id, blog_content, blog_img, blog_like_count, blog_time, blog_status) VALUES
-- 一根香蕉
(9001, '一根香蕉的產地旅程',NULL, 1, 1, 1,'從清晨採收到冷鏈配送，每一根香蕉都承載著小農的用心。', NULL, 48, '2026-02-20 14:00:00', 'VISIBLE'),
-- 產地日記
(9002, '有機農場的一天',NULL, 1, 1, 1,'農友從整地、灌溉到採收，每個步驟都堅持友善土地與自然栽培。', NULL, 32, '2026-02-21 09:30:00', 'VISIBLE'),

-- 蔬果知識分享
(9003, '當季蔬果怎麼挑', NULL, 2, 2, 2,'挑選當季蔬果時，可以觀察外觀、香氣與觸感，也能向農友了解採收日期。', NULL, 65, '2026-02-22 11:00:00', 'VISIBLE'),

-- 農作體驗回顧，由一般會員發表
(9004, '山間農場參訪記', 1, NULL, 3, 3,'第一次走進山間農場，親手體驗採收，也更了解農作物從產地到餐桌的過程。', NULL, 41, '2026-02-23 15:20:00', 'VISIBLE'),

-- 農作體驗回顧，由一般會員發表
(9005, '海風農場體驗日', 2, NULL, 3, 4, '迎著海風參觀農場，除了認識不同的栽培方式，也體會到農友工作的辛苦。', NULL, 27, '2026-02-24 10:10:00', 'VISIBLE'),

-- 食譜分享
(9006, '香蕉燕麥鬆餅', 5, NULL, 4, 5, '將熟香蕉壓成泥，加入雞蛋與燕麥拌勻，再用平底鍋煎成香甜鬆餅。', NULL, 53, '2026-02-25 13:40:00', 'VISIBLE');

-- 6. 專欄部落格 - 按讚檢查
CREATE TABLE BLOG_LIKE (
    blog_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (blog_id, user_id),

    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO BLOG_LIKE (blog_id, user_id, created_at) value
(9001, 1 ,'2026-06-04 14:00:00') ,
(9002, 2, '2026-02-21 12:00:00'),
(9003, 3, '2026-02-22 14:00:00'),
(9004, 4, '2026-02-23 17:00:00'),
(9006, 1, '2026-02-25 16:00:00');

-- 6. 專欄部落格 - 部落格評論
CREATE TABLE BLOG_COMMENT (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    blog_id INT NOT NULL,
    user_id INT NOT NULL,
    comment_time DATETIME,
    comment_post VARCHAR(200) NOT NULL,
    comment_like INT NOT NULL,
    comment_status ENUM('VISIBLE','HIDDEN'),
    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_COMMENT (comment_id, blog_id, user_id, comment_time, comment_post, comment_like, comment_status) VALUES
(10001, 9001, 1, '2026-02-21 09:00:00', '看完更想支持在地小農，內容很有溫度。', 5, 'VISIBLE'),

(10002, 9002, 2, '2026-02-21 13:00:00', '原來有機農場每天需要做這麼多工作！', 3, 'VISIBLE'),

(10003, 9003, 3, '2026-02-22 15:30:00','這些挑選蔬果的方法很實用。', 4, 'VISIBLE'),

(10004, 9004, 4, '2026-02-23 18:00:00', '看完也想親自參加農場體驗。', 2, 'VISIBLE'),

(10005, 9005, 5, '2026-02-24 14:00:00','農場環境看起來很舒服。', 1, 'VISIBLE'),

(10006, 9006, 1, '2026-02-25 18:30:00', '做法簡單，下次想在家試試看。', 5, 'VISIBLE');


-- 6. 專欄部落格 - 部落格照片
CREATE TABLE BLOG_PHOTO (
    blog_photo_id INT AUTO_INCREMENT PRIMARY KEY,
    blog_id INT NOT NULL,
    blog_photo LONGBLOB,
    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_PHOTO (blog_photo_id, blog_id, blog_photo) VALUES
(1, 9001, NULL),
(2, 9002, NULL),
(3, 9003, NULL),
(4, 9004, NULL),
(5, 9005, NULL),
(6, 9006, NULL);


-- 6. 專欄部落格 - 部落格檢舉
CREATE TABLE BLOG_REPORT (
    blog_report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    blog_id INT NOT NULL,
    admin_id INT,
    report_time DATETIME,
    report_reason VARCHAR(100),
    report_status ENUM('PENDING', 'APPROVED_VISIBLE', 'REJECTED_HIDDEN'),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id),
    FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_REPORT (blog_report_id, user_id, blog_id, admin_id, report_time, report_reason, report_status) VALUES
(12001,1 ,9001, NULL, '2026-02-22 18:00:00', '內容疑似與商品資訊不符，請平台確認。', 'PENDING');


-- 6. 專欄部落格 - 部落格評論檢舉
CREATE TABLE BLOG_COMMENT_REPORT (
    report_comment_id INT AUTO_INCREMENT PRIMARY KEY,
    comment_id INT NOT NULL,
    blog_id INT NOT NULL,
    user_id INT NOT NULL,
    admin_id INT,
    report_time DATETIME,
    report_reason VARCHAR(100),
    report_status ENUM('PENDING', 'APPROVED_VISIBLE', 'REJECTED_HIDDEN'),
    FOREIGN KEY (comment_id) REFERENCES BLOG_COMMENT(comment_id),
    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_COMMENT_REPORT (report_comment_id,comment_id, blog_id, user_id, admin_id, report_time, report_reason, report_status) VALUES
(13001, 10001, 9001, 3, NULL, '2026-02-22 19:00:00', '留言內容可能涉及不當評論。', 'PENDING');