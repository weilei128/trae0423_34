package com.canteen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.Dish;
import com.canteen.mapper.DishMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService extends ServiceImpl<DishMapper, Dish> {

    public List<Dish> getTodayDishes() {
        return this.list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsToday, 1)
                .eq(Dish::getStatus, 1)
                .orderByAsc(Dish::getCategoryId));
    }

    public List<Dish> getDishesByCategory(Long categoryId) {
        return this.list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, 1)
                .eq(Dish::getIsToday, 1));
    }

    public void decreaseStock(Long dishId, int quantity) {
        Dish dish = this.getById(dishId);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        
        if (dish.getStock() < quantity) {
            throw new RuntimeException("菜品库存不足");
        }
        
        dish.setStock(dish.getStock() - quantity);
        
        if (dish.getStock() <= 0) {
            dish.setStatus(0);
        }
        
        this.updateById(dish);
    }

    public void increaseStock(Long dishId, int quantity) {
        Dish dish = this.getById(dishId);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        
        dish.setStock(dish.getStock() + quantity);
        
        if (dish.getStock() > 0 && dish.getStatus() == 0) {
            dish.setStatus(1);
        }
        
        this.updateById(dish);
    }
}
