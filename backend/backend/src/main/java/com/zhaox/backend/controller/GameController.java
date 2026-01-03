package com.zhaox.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhaox.backend.common.Result;
import com.zhaox.backend.dto.GameVO;
import com.zhaox.backend.entity.Game;
import com.zhaox.backend.entity.GameCategory;
import com.zhaox.backend.mapper.GameCategoryMapper;
import com.zhaox.backend.mapper.GameMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameMapper gameMapper;
    private final GameCategoryMapper gameCategoryMapper;

    public GameController(GameMapper gameMapper, GameCategoryMapper gameCategoryMapper) {
        this.gameMapper = gameMapper;
        this.gameCategoryMapper = gameCategoryMapper;
    }

    // 分页列表 + 关键词搜索 + 分类筛选 + 状态筛选 + 平台筛选
    // /api/games?page=1&size=10&keyword=xxx&categoryId=1&status=1&platform=PC
    @GetMapping
    public Result<Page<GameVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String platform
    ) {
        LambdaQueryWrapper<Game> qw = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like(Game::getTitle, keyword.trim());
        }
        if (categoryId != null) {
            qw.eq(Game::getCategoryId, categoryId);
        }
        if (status != null) {
            qw.eq(Game::getStatus, status);
        }
        if (platform != null && !platform.trim().isEmpty()) {
            qw.eq(Game::getPlatform, platform.trim());
        }

        qw.orderByDesc(Game::getUpdatedAt);

        Page<Game> p = gameMapper.selectPage(new Page<>(page, size), qw);

        // 分类 Map(id -> name)
        List<GameCategory> categories = gameCategoryMapper.selectList(null);
        Map<Long, String> cateMap = new HashMap<>();
        for (GameCategory c : categories) {
            cateMap.put(c.getId(), c.getName());
        }

        // 转 VO
        Page<GameVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        List<GameVO> voRecords = new ArrayList<>();
        for (Game g : p.getRecords()) {
            GameVO vo = new GameVO();
            vo.setId(g.getId());
            vo.setTitle(g.getTitle());
            vo.setCategoryId(g.getCategoryId());
            vo.setCategoryName(cateMap.get(g.getCategoryId()));
            vo.setCoverUrl(g.getCoverUrl());
            vo.setIntro(g.getIntro());
            vo.setContent(g.getContent());
            vo.setPlatform(g.getPlatform());
            vo.setStatus(g.getStatus());
            vo.setCreatedAt(g.getCreatedAt());
            vo.setUpdatedAt(g.getUpdatedAt());
            voRecords.add(vo);
        }
        voPage.setRecords(voRecords);

        return Result.ok(voPage);
    }

    // 详情
    @GetMapping("/{id}")
    public Result<Game> detail(@PathVariable Long id) {
        Game game = gameMapper.selectById(id);
        if (game == null) return Result.fail("游戏不存在");
        return Result.ok(game);
    }

    // 新增
    @PostMapping
    public Result<Long> add(@RequestBody Game game) {
        if (game.getTitle() == null || game.getTitle().trim().isEmpty()) {
            return Result.fail("title 不能为空");
        }
        if (game.getCategoryId() == null) {
            return Result.fail("categoryId 不能为空");
        }
        gameMapper.insert(game);
        return Result.ok(game.getId());
    }

    // 修改（全量更新）
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Game game) {
        if (game.getTitle() == null || game.getTitle().trim().isEmpty()) {
            return Result.fail("title 不能为空");
        }
        if (game.getCategoryId() == null) {
            return Result.fail("categoryId 不能为空");
        }
        game.setId(id);
        int rows = gameMapper.updateById(game);
        if (rows == 0) return Result.fail("游戏不存在或更新失败");
        return Result.ok(null);
    }

    // 上架/下架（只改 status）
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Game db = gameMapper.selectById(id);
        if (db == null) return Result.fail("游戏不存在");
        db.setStatus(status);
        gameMapper.updateById(db);
        return Result.ok(null);
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        gameMapper.deleteById(id);
        return Result.ok(null);
    }
}
