package com.zerobase.homemate.recommend.service;




import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final ChoreRepository choreRepository;
    private static final int DEFAULT_PAGE_SIZE = 4;

    @Transactional(readOnly = true)
    public List<ChoreResponse> getChoresByCategory(Category category) {
            return choreRepository.findTop4ByCategory(category)
                    .stream()
                    .map(ChoreResponse::fromEntity)
                    .toList();

    }

}
