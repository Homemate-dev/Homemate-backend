package com.zerobase.homemate.recommend.service;




import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.repository.ChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final ChoreRepository choreRepository;
    private static final int DEFAULT_PAGE_SIZE = 4;

    public List<ChoreResponse> getChoresByCategory(Long categoryId) {
        return choreRepository.findByCategoryChores_Category_Id(
                        categoryId,
                        PageRequest.of(0, DEFAULT_PAGE_SIZE)
                )
                .stream()
                .map(ChoreResponse::fromEntity)
                .toList();
    }

}
