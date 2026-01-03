package com.zhaox.backend.controller;

import com.zhaox.backend.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api")
public class UploadController {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> upload(@RequestPart("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择文件");
        }

        String original = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(original);
        String filename = UUID.randomUUID().toString().replace("-", "") + (ext == null ? "" : "." + ext);

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());

        // 返回一个可以访问的 URL（下面第 2 步会把 /uploads 映射出来）
        return Result.ok("/api/file/" + filename);

    }
}
