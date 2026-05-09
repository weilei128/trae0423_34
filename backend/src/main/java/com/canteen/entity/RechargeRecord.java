package com.canteen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recharge_record")
public class RechargeRecord {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private Integer status;
    private LocalDateTime createTime;
}
