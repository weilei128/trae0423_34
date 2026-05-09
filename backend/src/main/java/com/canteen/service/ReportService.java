package com.canteen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.canteen.entity.OrderItem;
import com.canteen.entity.Orders;
import com.canteen.entity.RechargeRecord;
import com.canteen.mapper.OrderItemMapper;
import com.canteen.mapper.OrdersMapper;
import com.canteen.mapper.RechargeRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private OrdersMapper ordersMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    public Map<String, Object> getDailyReport(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, start)
                .le(Orders::getCreateTime, end));
        
        List<OrderItem> allItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int pendingCount = 0;
        int finishedCount = 0;
        int refundedCount = 0;
        
        for (Orders order : orders) {
            totalAmount = totalAmount.add(order.getTotalAmount());
            if ("pending".equals(order.getStatus())) {
                pendingCount++;
            } else if ("finished".equals(order.getStatus())) {
                finishedCount++;
            } else if ("refunded".equals(order.getStatus())) {
                refundedCount++;
            }
            
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, order.getId()));
            allItems.addAll(items);
        }
        
        Map<String, Integer> dishCountMap = new HashMap<>();
        for (OrderItem item : allItems) {
            String dishName = item.getDishName();
            dishCountMap.merge(dishName, item.getQuantity(), Integer::sum);
        }
        
        List<Map<String, Object>> topDishes = new ArrayList<>();
        dishCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .forEach(entry -> {
                    Map<String, Object> dishData = new HashMap<>();
                    dishData.put("dishName", entry.getKey());
                    dishData.put("count", entry.getValue());
                    topDishes.add(dishData);
                });
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("totalOrders", orders.size());
        result.put("pendingCount", pendingCount);
        result.put("finishedCount", finishedCount);
        result.put("refundedCount", refundedCount);
        result.put("totalAmount", totalAmount);
        result.put("topDishes", topDishes);
        
        return result;
    }

    public Map<String, Object> getRechargeReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        List<RechargeRecord> records = rechargeRecordMapper.selectList(new LambdaQueryWrapper<RechargeRecord>()
                .ge(RechargeRecord::getCreateTime, start)
                .le(RechargeRecord::getCreateTime, end)
                .eq(RechargeRecord::getStatus, 1));
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (RechargeRecord record : records) {
            totalAmount = totalAmount.add(record.getAmount());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalCount", records.size());
        result.put("totalAmount", totalAmount);
        
        return result;
    }
}
