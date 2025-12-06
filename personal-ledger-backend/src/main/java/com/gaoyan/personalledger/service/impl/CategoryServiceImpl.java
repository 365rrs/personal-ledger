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
        List<Category> allCategories = categoryMapper.selectList(new QueryWrapper<Category>()
                .orderByAsc("type", "sort_order"));
        return buildTree(allCategories);
    }
    
    private List<Category> buildTree(List<Category> categories) {
        List<Category> tree = new java.util.ArrayList<>();
        java.util.Map<Long, Category> map = new java.util.HashMap<>();
        
        for (Category category : categories) {
            map.put(category.getId(), category);
        }
        
        for (Category category : categories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                tree.add(category);
            } else {
                Category parent = map.get(category.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new java.util.ArrayList<>());
                    }
                    parent.getChildren().add(category);
                }
            }
        }
        
        return tree;
    }
    
    @Override
    public List<Category> getCategoriesByType(String type) {
        List<Category> allCategories = categoryMapper.selectList(new QueryWrapper<Category>()
                .eq("type", type)
                .eq("enabled", true)
                .orderByAsc("sort_order"));
        return buildTree(allCategories);
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
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setLevel(1);
        } else {
            category.setLevel(2);
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
