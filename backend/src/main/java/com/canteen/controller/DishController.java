package com.canteen.controller;

import com.canteen.common.Result;
import com.canteen.entity.Category;
import com.canteen.entity.Dish;
import com.canteen.service.CategoryService;
import com.canteen.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/today")
    public Result<List<Dish>> getTodayDishes() {
        try {
            List<Dish> dishes = dishService.getTodayDishes();
            return Result.success(dishes);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<Dish>> getDishesByCategory(@PathVariable Long categoryId) {
        try {
            List<Dish> dishes = dishService.getDishesByCategory(categoryId);
            return Result.success(dishes);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/menu")
    public Result<Map<String, Object>> getMenuWithCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            Map<String, Object> result = new HashMap<>();
            result.put("categories", categories);
            
            for (Category category : categories) {
                List<Dish> dishes = dishService.getDishesByCategory(category.getId());
                result.put("category_" + category.getId(), dishes);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Dish> getDishDetail(@PathVariable Long id) {
        try {
            Dish dish = dishService.getById(id);
            if (dish == null) {
                return Result.error("菜品不存在");
            }
            return Result.success(dish);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
