package com.canteen.controller;

import com.canteen.common.Result;
import com.canteen.service.ReportService;
import com.canteen.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/daily")
    public Result<Map<String, Object>> getDailyReport(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String date) {
        try {
            if (token == null) {
                return Result.error(401, "未登录");
            }
            
            String role = JwtUtil.getRole(token.replace("Bearer ", ""));
            if (!"admin".equals(role)) {
                return Result.error(403, "无权限访问");
            }
            
            LocalDate reportDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            Map<String, Object> report = reportService.getDailyReport(reportDate);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/recharge")
    public Result<Map<String, Object>> getRechargeReport(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            if (token == null) {
                return Result.error(401, "未登录");
            }
            
            String role = JwtUtil.getRole(token.replace("Bearer ", ""));
            if (!"admin".equals(role)) {
                return Result.error(403, "无权限访问");
            }
            
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            
            Map<String, Object> report = reportService.getRechargeReport(start, end);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
