package com.zhaox.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhaox.backend.common.Result;
import com.zhaox.backend.entity.Game;
import com.zhaox.backend.entity.GameCategory;
import com.zhaox.backend.mapper.GameCategoryMapper;
import com.zhaox.backend.mapper.GameMapper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final GameMapper gameMapper;
    private final GameCategoryMapper categoryMapper;

    public DashboardController(GameMapper gameMapper, GameCategoryMapper categoryMapper) {
        this.gameMapper = gameMapper;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        List<Game> games = gameMapper.selectList(null);
        List<GameCategory> categories = categoryMapper.selectList(null);

        int totalGames = games.size();
        long publishedGames = games.stream()
                .filter(g -> g.getStatus() == null || g.getStatus() == 1)
                .count();
        long offlineGames = games.stream()
                .filter(g -> g.getStatus() != null && g.getStatus() == 0)
                .count();

        Map<Long, String> categoryNames = categories.stream()
                .collect(Collectors.toMap(GameCategory::getId, GameCategory::getName, (a, b) -> a));

        Map<String, Long> categoryCount = new LinkedHashMap<>();
        for (Game game : games) {
            String label = categoryNames.getOrDefault(game.getCategoryId(), "未分类");
            categoryCount.put(label, categoryCount.getOrDefault(label, 0L) + 1);
        }

        List<Map<String, Object>> categoryBreakdown = categoryCount.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("label", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                })
                .toList();

        Map<String, Long> platformCount = new LinkedHashMap<>();
        for (Game game : games) {
            String platform = game.getPlatform() != null ? game.getPlatform() : "未知平台";
            platformCount.put(platform, platformCount.getOrDefault(platform, 0L) + 1);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> recentGames = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                        .orderByDesc(Game::getUpdatedAt)
                        .last("LIMIT 5"))
                .stream()
                .map(g -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", g.getId());
                    m.put("title", g.getTitle());
                    m.put("platform", g.getPlatform());
                    m.put("category", categoryNames.getOrDefault(g.getCategoryId(), "未分类"));
                    m.put("status", g.getStatus());
                    if (g.getUpdatedAt() != null) {
                        m.put("updatedAt", formatter.format(g.getUpdatedAt()));
                    }
                    return m;
                })
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("totalGames", totalGames);
        data.put("publishedGames", publishedGames);
        data.put("offlineGames", offlineGames);
        data.put("categories", categories.size());
        data.put("categoryBreakdown", categoryBreakdown);
        data.put("platformBreakdown", platformCount.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("label", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                })
                .toList());
        data.put("recentGames", recentGames);

        if (!CollectionUtils.isEmpty(games)) {
            data.put("featured", games.stream()
                    .sorted((a, b) -> {
                        if (a.getUpdatedAt() == null || b.getUpdatedAt() == null) return 0;
                        return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                    })
                    .limit(8)
                    .map(g -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", g.getId());
                        item.put("title", g.getTitle());
                        item.put("categoryId", g.getCategoryId());
                        item.put("coverUrl", g.getCoverUrl());
                        item.put("platform", g.getPlatform());
                        return item;
                    })
                    .toList());
        } else {
            data.put("featured", new ArrayList<>());
        }

        return Result.ok(data);
    }
}
