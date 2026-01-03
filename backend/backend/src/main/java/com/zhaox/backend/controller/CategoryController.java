package com.zhaox.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhaox.backend.common.Result;
import com.zhaox.backend.entity.GameCategory;
import com.zhaox.backend.mapper.GameCategoryMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.util.List;

@RestController
@RequestMapping(value = "/api/categories", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {

    private final GameCategoryMapper gameCategoryMapper;

    public CategoryController(GameCategoryMapper gameCategoryMapper) {
        this.gameCategoryMapper = gameCategoryMapper;
    }

    // 查询全部（按 sort 升序）
    @GetMapping
    public Result<List<GameCategory>> list(){
        List<GameCategory> list = gameCategoryMapper.selectList(
                new LambdaQueryWrapper<GameCategory>().orderByAsc(GameCategory::getSort)
        );
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result<GameCategory> detail(@PathVariable Long id) {
        GameCategory category = gameCategoryMapper.selectById(id);
        if (category == null) {
            return Result.fail("分类不存在");
        }
        return Result.ok(category);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody GameCategory category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return Result.fail("name 不能为空");
        }
        category.setId(id);
        int rows = gameCategoryMapper.updateById(category);
        if (rows == 0) {
            return Result.fail("分类不存在或更新失败");
        }
        return Result.ok(null);
    }



    // 新增分类
    @PostMapping
    public Result<Long> add(@RequestBody GameCategory category) {
        // 最小校验：name 不能为空
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return Result.fail("name 不能为空");
        }
        gameCategoryMapper.insert(category);
        return Result.ok(category.getId());
    }

    // 删除分类
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        gameCategoryMapper.deleteById(id);
        return Result.ok(null);
    }
}
