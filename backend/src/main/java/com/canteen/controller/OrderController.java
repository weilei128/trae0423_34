package com.canteen.controller;

import com.canteen.common.Result;
import com.canteen.entity.OrderItem;
import com.canteen.entity.Orders;
import com.canteen.service.OrderService;
import com.canteen.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> params) {
        try {
            if (token == null) {
                return Result.error(401, "未登录");
            }
            
            Long userId = JwtUtil.getUserId(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error(401, "token无效");
            }
            
            LocalDate orderDate = LocalDate.parse((String) params.get("orderDate"));
            String remark = (String) params.get("remark");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dishList = (List<Map<String, Object>>) params.get("dishes");
            
            if (dishList == null || dishList.isEmpty()) {
                return Result.error("请选择菜品");
            }
            
            Map<String, Object> result = orderService.createOrder(userId, orderDate, remark, dishList);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result<List<Orders>> getMyOrders(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null) {
                return Result.error(401, "未登录");
            }
            
            Long userId = JwtUtil.getUserId(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error(401, "token无效");
            }
            
            List<Orders> orders = orderService.getMyOrders(userId);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{orderId}")
    public Result<Map<String, Object>> getOrderDetail(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long orderId) {
        try {
            if (token == null) {
                return Result.error(401, "未登录");
            }
            
            Long userId = JwtUtil.getUserId(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error(401, "token无效");
            }
            
            Orders order = orderService.getOrderDetail(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (!order.getUserId().equals(userId) && !"admin".equals(JwtUtil.getRole(token.replace("Bearer ", "")))) {
                return Result.error("无权查看此订单");
            }
            
            List<OrderItem> items = orderService.getOrderItems(orderId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("order", order);
            result.put("items", items);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel/{orderId}")
    public Result<String> cancelOrder(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long orderId) {
        try {
            if (token == null) {
                return Result.error(401, "未登录");
            }
            
            Long userId = JwtUtil.getUserId(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error(401, "token无效");
            }
            
            orderService.cancelOrder(orderId, userId);
            return Result.success("退餐成功，款项已退回余额");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public Result<Orders> verifyOrder(@RequestBody Map<String, String> params) {
        try {
            String verifyCode = params.get("verifyCode");
            if (verifyCode == null || verifyCode.isEmpty()) {
                return Result.error("请输入取餐码");
            }
            
            Orders order = orderService.verifyOrder(verifyCode);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
