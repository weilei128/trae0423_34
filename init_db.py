import mysql.connector
from mysql.connector import Error
from datetime import datetime

def create_connection():
    try:
        connection = mysql.connector.connect(
            host='49.235.161.106',
            port=10011,
            user='wl',
            password='Wl1733811',
            database='db4'
        )
        if connection.is_connected():
            print("成功连接到数据库")
            return connection
    except Error as e:
        print(f"连接失败: {e}")
        return None

def execute_sql(connection, sql):
    try:
        cursor = connection.cursor()
        cursor.execute(sql)
        connection.commit()
        print(f"SQL执行成功")
    except Error as e:
        print(f"SQL执行失败(已忽略): {e}")
    finally:
        if 'cursor' in locals():
            cursor.close()

def init_database():
    connection = create_connection()
    if not connection:
        return

    create_category_table = """
    CREATE TABLE IF NOT EXISTS category (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(50) NOT NULL,
        description VARCHAR(200),
        sort INT DEFAULT 0,
        status TINYINT DEFAULT 1,
        create_time TIMESTAMP,
        update_time TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """
    execute_sql(connection, create_category_table)

    create_dish_table = """
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
        create_time TIMESTAMP,
        update_time TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """
    execute_sql(connection, create_dish_table)

    create_user_table = """
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
        create_time TIMESTAMP,
        update_time TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """
    execute_sql(connection, create_user_table)

    create_recharge_table = """
    CREATE TABLE IF NOT EXISTS recharge_record (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL,
        amount DECIMAL(10,2) NOT NULL,
        payment_method VARCHAR(20) DEFAULT 'balance',
        status TINYINT DEFAULT 1,
        create_time TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """
    execute_sql(connection, create_recharge_table)

    create_orders_table = """
    CREATE TABLE IF NOT EXISTS orders (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_no VARCHAR(32) NOT NULL UNIQUE,
        user_id BIGINT NOT NULL,
        total_amount DECIMAL(10,2) NOT NULL,
        status VARCHAR(20) DEFAULT 'pending',
        order_date DATE NOT NULL,
        remark VARCHAR(200),
        verify_code VARCHAR(20),
        verify_time TIMESTAMP,
        cancel_time TIMESTAMP,
        create_time TIMESTAMP,
        update_time TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """
    execute_sql(connection, create_orders_table)

    create_order_item_table = """
    CREATE TABLE IF NOT EXISTS order_item (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_id BIGINT NOT NULL,
        dish_id BIGINT NOT NULL,
        dish_name VARCHAR(100) NOT NULL,
        price DECIMAL(10,2) NOT NULL,
        quantity INT NOT NULL,
        subtotal DECIMAL(10,2) NOT NULL,
        create_time TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """
    execute_sql(connection, create_order_item_table)

    create_config_table = """
    CREATE TABLE IF NOT EXISTS system_config (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        config_key VARCHAR(50) NOT NULL UNIQUE,
        config_value VARCHAR(500),
        description VARCHAR(200),
        update_time TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """
    execute_sql(connection, create_config_table)

    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    cursor = connection.cursor()
    
    cursor.execute("SELECT COUNT(*) FROM category")
    count = cursor.fetchone()[0]
    if count == 0:
        cursor.execute("INSERT INTO category (name, description, sort, create_time, update_time) VALUES (%s, %s, %s, %s, %s)", 
                      ("一楼中餐", "一楼中餐厅", 1, now, now))
        cursor.execute("INSERT INTO category (name, description, sort, create_time, update_time) VALUES (%s, %s, %s, %s, %s)", 
                      ("一楼面食", "一楼面食窗口", 2, now, now))
        cursor.execute("INSERT INTO category (name, description, sort, create_time, update_time) VALUES (%s, %s, %s, %s, %s)", 
                      ("二楼快餐", "二楼快餐窗口", 3, now, now))
        cursor.execute("INSERT INTO category (name, description, sort, create_time, update_time) VALUES (%s, %s, %s, %s, %s)", 
                      ("二楼饮品", "二楼饮品窗口", 4, now, now))
        connection.commit()
        print("分类数据插入成功")
    cursor.close()

    cursor = connection.cursor()
    cursor.execute("SELECT COUNT(*) FROM dish")
    count = cursor.fetchone()[0]
    if count == 0:
        image1 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=delicious%20braised%20pork%20rice%20Chinese%20food&image_size=square"
        image2 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=kung%20pao%20chicken%20Chinese%20food&image_size=square"
        image3 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fish%20flavored%20shredded%20pork%20Chinese%20food&image_size=square"
        image4 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lanzhou%20beef%20noodles%20Chinese%20food&image_size=square"
        image5 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=dao%20sliced%20noodles%20Chinese%20food&image_size=square"
        image6 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fried%20chicken%20set%20meal%20fast%20food&image_size=square"
        image7 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hamburger%20set%20meal%20fast%20food&image_size=square"
        image8 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bubble%20tea%20pearl%20milk%20tea%20drink&image_size=square"
        image9 = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lemon%20water%20fresh%20drink&image_size=square"
        
        cursor.execute("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("红烧肉套餐", 15.00, image1, "精选五花肉配米饭蔬菜", 1, 50, 50, 1, 1, now, now))
        cursor.execute("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("宫保鸡丁", 12.00, image2, "经典川菜，麻辣鲜香", 1, 50, 50, 1, 1, now, now))
        cursor.execute("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("鱼香肉丝", 10.00, image3, "酸甜可口", 1, 50, 50, 1, 1, now, now))
        cursor.execute("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("兰州拉面", 12.00, image4, "正宗兰州牛肉面", 2, 50, 50, 1, 1, now, now))
        cursor.execute("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("刀削面", 10.00, image5, "山西刀削面", 2, 50, 50, 1, 1, now, now))
        cursor.execute("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("炸鸡套餐", 18.00, image6, "香脆炸鸡配薯条", 3, 50, 50, 1, 1, now, now))
        cursor.execute("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("汉堡套餐", 16.00, image7, "美味汉堡配可乐", 3, 50, 50, 1, 1, now, now))
        cursor.execute("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("珍珠奶茶", 8.00, image8, "香浓珍珠奶茶", 4, 50, 50, 1, 1, now, now))
        cursor.execute("INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("柠檬水", 5.00, image9, "新鲜柠檬水", 4, 50, 50, 1, 1, now, now))
        connection.commit()
        print("菜品数据插入成功")
    cursor.close()

    cursor = connection.cursor()
    cursor.execute("SELECT COUNT(*) FROM user")
    count = cursor.fetchone()[0]
    if count == 0:
        md5_pwd = "e10adc3949ba59abbe56e057f20f883e"
        cursor.execute("INSERT INTO user (username, password, name, student_no, phone, balance, role, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("admin", md5_pwd, "管理员", "000000", "13800000000", 100.00, "admin", now, now))
        cursor.execute("INSERT INTO user (username, password, name, student_no, phone, balance, role, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("student1", md5_pwd, "张三", "2024001", "13800000001", 200.00, "student", now, now))
        cursor.execute("INSERT INTO user (username, password, name, student_no, phone, balance, role, create_time, update_time) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)",
                      ("teacher1", md5_pwd, "李老师", "T001", "13800000002", 500.00, "teacher", now, now))
        connection.commit()
        print("用户数据插入成功")
    cursor.close()

    cursor = connection.cursor()
    cursor.execute("SELECT COUNT(*) FROM system_config")
    count = cursor.fetchone()[0]
    if count == 0:
        cursor.execute("INSERT INTO system_config (config_key, config_value, description, update_time) VALUES (%s, %s, %s, %s)",
                      ("refund_deadline", "20:00", "退餐截止时间", now))
        cursor.execute("INSERT INTO system_config (config_key, config_value, description, update_time) VALUES (%s, %s, %s, %s)",
                      ("next_day_deadline", "22:00", "次日订餐截止时间", now))
        connection.commit()
        print("系统配置数据插入成功")
    cursor.close()

    connection.close()
    print("数据库初始化完成")

if __name__ == "__main__":
    init_database()
