package com.canteen.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            createTables();
            insertInitialData();
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
        }
    }

    private void createTables() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS category (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL,
                    description VARCHAR(200),
                    sort INT DEFAULT 0,
                    status TINYINT DEFAULT 1,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        } catch (Exception e) {
            System.err.println("创建category表失败: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS dish (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    image VARCHAR(500),
                    description VARCHAR(500),
                    category_id BIGINT NOT NULL,
                    stock INT DEFAULT 50,
                    max_stock INT DEFAULT 50,
                    status TINYINT DEFAULT 1,
                    is_today TINYINT DEFAULT 1,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        } catch (Exception e) {
            System.err.println("创建dish表失败: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(100) NOT NULL,
                    name VARCHAR(50) NOT NULL,
                    student_no VARCHAR(50),
                    phone VARCHAR(20),
                    avatar VARCHAR(500),
                    balance DECIMAL(10,2) DEFAULT 0.00,
                    role VARCHAR(20) DEFAULT 'student',
                    status TINYINT DEFAULT 1,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        } catch (Exception e) {
            System.err.println("创建user表失败: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS recharge_record (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    amount DECIMAL(10,2) NOT NULL,
                    payment_method VARCHAR(20) DEFAULT 'balance',
                    status TINYINT DEFAULT 1,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        } catch (Exception e) {
            System.err.println("创建recharge_record表失败: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    order_no VARCHAR(32) NOT NULL UNIQUE,
                    user_id BIGINT NOT NULL,
                    total_amount DECIMAL(10,2) NOT NULL,
                    status VARCHAR(20) DEFAULT 'pending',
                    order_date DATE NOT NULL,
                    remark VARCHAR(200),
                    verify_code VARCHAR(20),
                    verify_time DATETIME,
                    cancel_time DATETIME,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        } catch (Exception e) {
            System.err.println("创建orders表失败: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS order_item (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    order_id BIGINT NOT NULL,
                    dish_id BIGINT NOT NULL,
                    dish_name VARCHAR(100) NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    quantity INT NOT NULL,
                    subtotal DECIMAL(10,2) NOT NULL,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        } catch (Exception e) {
            System.err.println("创建order_item表失败: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS system_config (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    config_key VARCHAR(50) NOT NULL UNIQUE,
                    config_value VARCHAR(500),
                    description VARCHAR(200),
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        } catch (Exception e) {
            System.err.println("创建system_config表失败: " + e.getMessage());
        }
    }

    private void insertInitialData() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_config", Integer.class);
            if (count == null || count == 0) {
                jdbcTemplate.update("INSERT INTO system_config (config_key, config_value, description) VALUES (?, ?, ?)",
                        "refund_deadline", "20:00", "退餐截止时间（当天）");
                jdbcTemplate.update("INSERT INTO system_config (config_key, config_value, description) VALUES (?, ?, ?)",
                        "next_day_deadline", "22:00", "次日订餐截止时间");
            }
        } catch (Exception e) {
            System.err.println("插入system_config失败: " + e.getMessage());
        }

        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM category", Integer.class);
            if (count == null || count == 0) {
                jdbcTemplate.update("INSERT INTO category (name, description, sort) VALUES (?, ?, ?)",
                        "一楼中餐", "一楼中餐厅", 1);
                jdbcTemplate.update("INSERT INTO category (name, description, sort) VALUES (?, ?, ?)",
                        "一楼面食", "一楼面食窗口", 2);
                jdbcTemplate.update("INSERT INTO category (name, description, sort) VALUES (?, ?, ?)",
                        "二楼快餐", "二楼快餐窗口", 3);
                jdbcTemplate.update("INSERT INTO category (name, description, sort) VALUES (?, ?, ?)",
                        "二楼饮品", "二楼饮品窗口", 4);
            }
        } catch (Exception e) {
            System.err.println("插入category失败: " + e.getMessage());
        }

        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dish", Integer.class);
            if (count == null || count == 0) {
                String image1 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=delicious%20braised%20pork%20rice%20Chinese%20food&image_size=square";
                String image2 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=kung%20pao%20chicken%20Chinese%20food&image_size=square";
                String image3 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fish%20flavored%20shredded%20pork%20Chinese%20food&image_size=square";
                String image4 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lanzhou%20beef%20noodles%20Chinese%20food&image_size=square";
                String image5 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=dao%20sliced%20noodles%20Chinese%20food&image_size=square";
                String image6 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fried%20chicken%20set%20meal%20fast%20food&image_size=square";
                String image7 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hamburger%20set%20meal%20fast%20food&image_size=square";
                String image8 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bubble%20tea%20pearl%20milk%20tea%20drink&image_size=square";
                String image9 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lemon%20water%20fresh%20drink&image_size=square";

                jdbcTemplate.update("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "红烧肉套餐", 15.00, image1, "精选五花肉配米饭蔬菜", 1L, 50, 50, 1, 1);
                jdbcTemplate.update("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "宫保鸡丁", 12.00, image2, "经典川菜，麻辣鲜香", 1L, 50, 50, 1, 1);
                jdbcTemplate.update("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "鱼香肉丝", 10.00, image3, "酸甜可口", 1L, 50, 50, 1, 1);
                jdbcTemplate.update("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "兰州拉面", 12.00, image4, "正宗兰州牛肉面", 2L, 50, 50, 1, 1);
                jdbcTemplate.update("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "刀削面", 10.00, image5, "山西刀削面", 2L, 50, 50, 1, 1);
                jdbcTemplate.update("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "炸鸡套餐", 18.00, image6, "香脆炸鸡配薯条", 3L, 50, 50, 1, 1);
                jdbcTemplate.update("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "汉堡套餐", 16.00, image7, "美味汉堡配可乐", 3L, 50, 50, 1, 1);
                jdbcTemplate.update("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "珍珠奶茶", 8.00, image8, "香浓珍珠奶茶", 4L, 50, 50, 1, 1);
                jdbcTemplate.update("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "柠檬水", 5.00, image9, "新鲜柠檬水", 4L, 50, 50, 1, 1);
            }
        } catch (Exception e) {
            System.err.println("插入dish失败: " + e.getMessage());
        }

        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Integer.class);
            if (count == null || count == 0) {
                String md5Pwd = "e10adc3949ba59abbe56e057f20f883e";
                jdbcTemplate.update("INSERT INTO user (username, password, name, student_no, phone, balance, role) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        "admin", md5Pwd, "管理员", "000000", "13800000000", 100.00, "admin");
                jdbcTemplate.update("INSERT INTO user (username, password, name, student_no, phone, balance, role) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        "student1", md5Pwd, "张三", "2024001", "13800000001", 200.00, "student");
                jdbcTemplate.update("INSERT INTO user (username, password, name, student_no, phone, balance, role) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        "teacher1", md5Pwd, "李老师", "T001", "13800000002", 500.00, "teacher");
            }
        } catch (Exception e) {
            System.err.println("插入user失败: " + e.getMessage());
        }
    }
}
