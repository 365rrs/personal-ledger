package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gaoyan.personalledger.entity.Category;
import com.gaoyan.personalledger.mapper.CategoryMapper;
import com.gaoyan.personalledger.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 分类服务实现
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(new QueryWrapper<Category>()
                .orderByAsc("type", "sort_order"));
    }
    
    @Override
    public List<Category> getCategoriesByType(String type) {
        return categoryMapper.selectList(new QueryWrapper<Category>()
                .eq("type", type)
                .eq("enabled", true)
                .orderByAsc("sort_order"));
    }
    
    @Override
    public Category addCategory(Category category) {
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        if (category.getEnabled() == null) {
            category.setEnabled(true);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
        return category;
    }
    
    @Override
    public Category updateCategory(Category category) {
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
        return category;
    }
    
    @Override
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }
    

}
