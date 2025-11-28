package com.gaoyan.personalledger.controller;

import com.gaoyan.personalledger.entity.Category;
import com.gaoyan.personalledger.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    public List<Category> getAllCategories() {
        log.info("获取所有分类");
        return categoryService.getAllCategories();
    }
    
    /**
     * 根据类型获取分类
     */
    @GetMapping("/list/{type}")
    public List<Category> getCategoriesByType(@PathVariable String type) {
        log.info("获取分类，类型: {}", type);
        return categoryService.getCategoriesByType(type);
    }
    
    /**
     * 添加分类
     */
    @PostMapping("/add")
    public Category addCategory(@RequestBody Category category) {
        log.info("添加分类: {}", category.getName());
        return categoryService.addCategory(category);
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/update")
    public Category updateCategory(@RequestBody Category category) {
        log.info("更新分类: {}", category.getId());
        return categoryService.updateCategory(category);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    public void deleteCategory(@PathVariable Long id) {
        log.info("删除分类: {}", id);
        categoryService.deleteCategory(id);
    }
    

}
