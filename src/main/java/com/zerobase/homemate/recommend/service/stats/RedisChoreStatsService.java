package com.zerobase.homemate.recommend.service.stats;

import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Space;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisChoreStatsService {

    private final String CATEGORY_KEY_PREFIX = "chore:count:category:";
    private final String SPACE_KEY_PREFIX = "chore:count:space:";
    private final RedisTemplate<String, String> redisTemplate;

    public void increment(Category category, Space space){
        if (category != null) {
            redisTemplate.opsForValue().increment(CATEGORY_KEY_PREFIX + category.name());
        }
        if (space != null) {
            redisTemplate.opsForValue().increment(SPACE_KEY_PREFIX + space.name());
        }
    }

    public Map<String, Long> getCategoryStats(){
        Map<String, Long> categoryResult = new HashMap<>();

        for(Category category: Category.values()){
            String key = CATEGORY_KEY_PREFIX + category.name();
            String val = redisTemplate.opsForValue().get(key);

            categoryResult.put(category.name(), val == null ? 0 : Long.parseLong(val));
        }
        return categoryResult;
    }

    public Map<String, Long> getSpaceStats(){
        Map<String, Long> spaceResult = new HashMap<>();
        for(Space space: Space.values()){
            String key = SPACE_KEY_PREFIX + space.name();
            String val = redisTemplate.opsForValue().get(key);

            spaceResult.put(space.name(), val == null ? 0 : Long.parseLong(val));
        }

        return spaceResult;
    }

    public void resetAllCounts(){
        for(Category category: Category.values()){
            redisTemplate.opsForValue().set(CATEGORY_KEY_PREFIX + category.name(), "0");
        }
        for(Space space: Space.values()){
            redisTemplate.opsForValue().set(SPACE_KEY_PREFIX + space.name(), "0");
        }
    }
}
