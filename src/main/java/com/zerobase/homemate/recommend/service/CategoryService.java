package com.zerobase.homemate.recommend.service;



import com.zerobase.homemate.entity.Category;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.repository.CategoryRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ChoreRepository choreRepository;
    private static final int DEFAULT_PAGE_SIZE = 5;

    @Transactional
    public Category createCategory(String name) {
        Category category = Category.builder()
                .nameKo(name)
                .build();

        return categoryRepository.save(category);
    }

    public Category getCategory(Long id) {
        return  categoryRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Category not found: " + id)
        );
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category updateCategory(Long id, String newName) {
        Category category = getCategory(id);
        category.updateName(newName); // 아, 여기는 Entity에다가 updateName 메소드를 집어넣을 것이라고 얘기하는 거구나.
        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    
}
