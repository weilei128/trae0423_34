INSERT INTO system_config (config_key, config_value, description) 
SELECT 'refund_deadline', '20:00', '退餐截止时间（当天）' 
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'refund_deadline');

INSERT INTO system_config (config_key, config_value, description) 
SELECT 'next_day_deadline', '22:00', '次日订餐截止时间' 
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'next_day_deadline');

INSERT INTO category (name, description, sort)
SELECT '一楼中餐', '一楼中餐厅', 1 
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '一楼中餐');

INSERT INTO category (name, description, sort)
SELECT '一楼面食', '一楼面食窗口', 2 
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '一楼面食');

INSERT INTO category (name, description, sort)
SELECT '二楼快餐', '二楼快餐窗口', 3 
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '二楼快餐');

INSERT INTO category (name, description, sort)
SELECT '二楼饮品', '二楼饮品窗口', 4 
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '二楼饮品');

INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today)
SELECT '红烧肉套餐', 15.00, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=delicious%20braised%20pork%20rice%20Chinese%20food&image_size=square', '精选五花肉配米饭蔬菜', 1, 50, 50, 1, 1 
WHERE NOT EXISTS (SELECT 1 FROM dish WHERE name = '红烧肉套餐');

INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today)
SELECT '宫保鸡丁', 12.00, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=kung%20pao%20chicken%20Chinese%20food&image_size=square', '经典川菜，麻辣鲜香', 1, 50, 50, 1, 1 
WHERE NOT EXISTS (SELECT 1 FROM dish WHERE name = '宫保鸡丁');

INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today)
SELECT '鱼香肉丝', 10.00, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fish%20flavored%20shredded%20pork%20Chinese%20food&image_size=square', '酸甜可口', 1, 50, 50, 1, 1 
WHERE NOT EXISTS (SELECT 1 FROM dish WHERE name = '鱼香肉丝');

INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today)
SELECT '兰州拉面', 12.00, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lanzhou%20beef%20noodles%20Chinese%20food&image_size=square', '正宗兰州牛肉面', 2, 50, 50, 1, 1 
WHERE NOT EXISTS (SELECT 1 FROM dish WHERE name = '兰州拉面');

INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today)
SELECT '刀削面', 10.00, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=dao%20sliced%20noodles%20Chinese%20food&image_size=square', '山西刀削面', 2, 50, 50, 1, 1 
WHERE NOT EXISTS (SELECT 1 FROM dish WHERE name = '刀削面');

INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today)
SELECT '炸鸡套餐', 18.00, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fried%20chicken%20set%20meal%20fast%20food&image_size=square', '香脆炸鸡配薯条', 3, 50, 50, 1, 1 
WHERE NOT EXISTS (SELECT 1 FROM dish WHERE name = '炸鸡套餐');

INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today)
SELECT '汉堡套餐', 16.00, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hamburger%20set%20meal%20fast%20food&image_size=square', '美味汉堡配可乐', 3, 50, 50, 1, 1 
WHERE NOT EXISTS (SELECT 1 FROM dish WHERE name = '汉堡套餐');

INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today)
SELECT '珍珠奶茶', 8.00, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bubble%20tea%20pearl%20milk%20tea%20drink&image_size=square', '香浓珍珠奶茶', 4, 50, 50, 1, 1 
WHERE NOT EXISTS (SELECT 1 FROM dish WHERE name = '珍珠奶茶');

INSERT INTO dish (name, price, image, description, category_id, stock, max_stock, status, is_today)
SELECT '柠檬水', 5.00, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lemon%20water%20fresh%20drink&image_size=square', '新鲜柠檬水', 4, 50, 50, 1, 1 
WHERE NOT EXISTS (SELECT 1 FROM dish WHERE name = '柠檬水');

INSERT INTO user (username, password, name, student_no, phone, balance, role)
SELECT 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', '000000', '13800000000', 100.00, 'admin'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');

INSERT INTO user (username, password, name, student_no, phone, balance, role)
SELECT 'student1', 'e10adc3949ba59abbe56e057f20f883e', '张三', '2024001', '13800000001', 200.00, 'student'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'student1');

INSERT INTO user (username, password, name, student_no, phone, balance, role)
SELECT 'teacher1', 'e10adc3949ba59abbe56e057f20f883e', '李老师', 'T001', '13800000002', 500.00, 'teacher'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'teacher1');
