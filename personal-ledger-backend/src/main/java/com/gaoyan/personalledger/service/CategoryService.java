package com.gaoyan.personalledger.service;

import com.gaoyan.personalledger.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {
    
    /**
     * 获取所有分类
     */
    List<Category> getAllCategories();
    
    /**
     * 根据类型获取分类
     */
    List<Category> getCategoriesByType(String type);
    
    /**
     * 添加分类
     */
    Category addCategory(Category category);
    
    /**
     * 更新分类
     */
    Category updateCategory(Category category);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
    

}
