package com.canteen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.*;
import com.canteen.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class OrderService extends ServiceImpl<OrdersMapper, Orders> {

    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Long userId, LocalDate orderDate, String remark, List<Map<String, Object>> dishList) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已禁用");
        }
        
        LocalDate today = LocalDate.now();
        
        if (orderDate.isBefore(today)) {
            throw new RuntimeException("订餐日期不能早于今天");
        }
        
        if (orderDate.equals(today)) {
            throw new RuntimeException("只能提前一天订餐");
        }
        
        if (orderDate.isAfter(today.plusDays(1))) {
            throw new RuntimeException("只能预订明天的餐食");
        }
        
        String nextDayDeadline = getConfigValue("next_day_deadline", "22:00");
        LocalTime deadlineTime = LocalTime.parse(nextDayDeadline);
        if (LocalTime.now().isAfter(deadlineTime)) {
            throw new RuntimeException("已过订餐截止时间（" + nextDayDeadline + "）");
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map<String, Object> item : dishList) {
            Long dishId = Long.valueOf(item.get("dishId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            
            Dish dish = dishMapper.selectById(dishId);
            if (dish == null) {
                throw new RuntimeException("菜品不存在");
            }
            
            if (dish.getStatus() == 0) {
                throw new RuntimeException("菜品【" + dish.getName() + "】已售罄");
            }
            
            if (dish.getIsToday() == 0) {
                throw new RuntimeException("菜品【" + dish.getName() + "】不在今日菜单中");
            }
            
            if (dish.getStock() < quantity) {
                throw new RuntimeException("菜品【" + dish.getName() + "】库存不足");
            }
            
            totalAmount = totalAmount.add(dish.getPrice().multiply(new BigDecimal(quantity)));
        }
        
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("订单金额无效");
        }
        
        if (user.getBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("余额不足，请先充值");
        }
        
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");
        order.setOrderDate(orderDate);
        order.setRemark(remark);
        order.setVerifyCode(generateVerifyCode());
        order.setCreateTime(LocalDateTime.now());
        this.save(order);
        
        for (Map<String, Object> item : dishList) {
            Long dishId = Long.valueOf(item.get("dishId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            
            Dish dish = dishMapper.selectById(dishId);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setDishId(dishId);
            orderItem.setDishName(dish.getName());
            orderItem.setPrice(dish.getPrice());
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(dish.getPrice().multiply(new BigDecimal(quantity)));
            orderItem.setCreateTime(LocalDateTime.now());
            orderItemMapper.insert(orderItem);
            
            dish.setStock(dish.getStock() - quantity);
            if (dish.getStock() <= 0) {
                dish.setStatus(0);
            }
            dishMapper.updateById(dish);
        }
        
        user.setBalance(user.getBalance().subtract(totalAmount));
        userMapper.updateById(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("totalAmount", totalAmount);
        result.put("verifyCode", order.getVerifyCode());
        
        return result;
    }

    public Orders getOrderDetail(Long orderId) {
        return this.getById(orderId);
    }

    public List<Orders> getMyOrders(Long userId) {
        return this.list(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        Orders order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("当前订单状态不允许取消");
        }
        
        LocalDate today = LocalDate.now();
        if (order.getOrderDate().isBefore(today)) {
            throw new RuntimeException("订单已过期，无法取消");
        }
        
        String refundDeadline = getConfigValue("refund_deadline", "20:00");
        LocalTime deadlineTime = LocalTime.parse(refundDeadline);
        
        if (order.getOrderDate().equals(today)) {
            if (LocalTime.now().isAfter(deadlineTime)) {
                throw new RuntimeException("已过退餐截止时间（" + refundDeadline + "）");
            }
        }
        
        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        
        for (OrderItem item : orderItems) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish != null) {
                dish.setStock(dish.getStock() + item.getQuantity());
                if (dish.getStock() > 0 && dish.getStatus() == 0) {
                    dish.setStatus(1);
                }
                dishMapper.updateById(dish);
            }
        }
        
        User user = userMapper.selectById(userId);
        user.setBalance(user.getBalance().add(order.getTotalAmount()));
        userMapper.updateById(user);
        
        order.setStatus("refunded");
        order.setCancelTime(LocalDateTime.now());
        this.updateById(order);
    }

    public Orders verifyOrder(String verifyCode) {
        Orders order = this.getOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getVerifyCode, verifyCode));
        
        if (order == null) {
            throw new RuntimeException("取餐码无效");
        }
        
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许核销");
        }
        
        order.setStatus("finished");
        order.setVerifyTime(LocalDateTime.now());
        this.updateById(order);
        
        return order;
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
    }

    private String generateOrderNo() {
        return "O" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    private String generateVerifyCode() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String getConfigValue(String key, String defaultValue) {
        SystemConfig config = systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : defaultValue;
    }
}
