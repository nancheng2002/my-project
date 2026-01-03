package com.zhaox.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhaox.backend.common.Result;
import com.zhaox.backend.entity.Game;
import com.zhaox.backend.entity.GameCategory;
import com.zhaox.backend.mapper.GameCategoryMapper;
import com.zhaox.backend.mapper.GameMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final GameMapper gameMapper;
    private final GameCategoryMapper categoryMapper;

    public HomeController(GameMapper gameMapper, GameCategoryMapper categoryMapper) {
        this.gameMapper = gameMapper;
        this.categoryMapper = categoryMapper;
    }

    // 首页分类（只返回启用的）
    @GetMapping("/categories")
    public Result<List<GameCategory>> categories() {
        LambdaQueryWrapper<GameCategory> qw = new LambdaQueryWrapper<>();
        qw.eq(GameCategory::getStatus, 1)
                .orderByAsc(GameCategory::getSort)
                .orderByAsc(GameCategory::getId);
        return Result.ok(categoryMapper.selectList(qw));
    }

    // 首页推荐：只取上架(status=1) + 按更新时间倒序 + 取前 6 条
    @GetMapping("/recommend")
    public Result<List<Game>> recommend() {
        LambdaQueryWrapper<Game> qw = new LambdaQueryWrapper<>();
        qw.eq(Game::getStatus, 1)
                .orderByDesc(Game::getUpdatedAt)
                .last("LIMIT 6");
        return Result.ok(gameMapper.selectList(qw));
    }

    // 首页详情：只允许看上架的（你也可以去掉 status 判断）
    @GetMapping("/games/{id}")
    public Result<Game> gameDetail(@PathVariable Long id) {
        Game game = gameMapper.selectById(id);
        if (game == null) return Result.fail("游戏不存在");
        if (game.getStatus() != null && game.getStatus() == 0) {
            return Result.fail("游戏已下架");
        }
        return Result.ok(game);
    }
}
