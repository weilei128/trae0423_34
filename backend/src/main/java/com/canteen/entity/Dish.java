package com.canteen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {
    private Long id;
    private String name;
    private BigDecimal price;
    private String image;
    private String description;
    private Long categoryId;
    private Integer stock;
    private Integer maxStock;
    private Integer status;
    private Integer isToday;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
