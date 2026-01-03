package com.zhaox.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class FileController {

    @Value("${app.upload-dir}")
    private String uploadDir;

    // 测试用：确认控制器是否生效
    @GetMapping("/file-test")
    public String fileTest() {
        return "ok";
    }

    // 访问：http://localhost:8080/api/file/xxx.png
    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws Exception {
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path file = base.resolve(filename).normalize();

        if (!file.startsWith(base)) return ResponseEntity.badRequest().build();
        if (!Files.exists(file)) return ResponseEntity.notFound().build();

        Resource resource = new UrlResource(file.toUri());
        String contentType = Files.probeContentType(file);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
    @GetMapping("/file-exists/{filename:.+}")
    public String fileExists(@PathVariable String filename) {
        try {
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path file = base.resolve(filename).normalize();
            return "base=" + base + "\nfile=" + file + "\nexists=" + Files.exists(file);
        } catch (Exception e) {
            return "error=" + e.getMessage();
        }
    }

    @GetMapping("/files")
    public String listFiles() throws Exception {
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(base)) return "dir not exists: " + base;

        StringBuilder sb = new StringBuilder();
        sb.append("dir=").append(base).append("\n");

        try (var stream = Files.list(base)) {
            stream.forEach(p -> sb.append(p.getFileName()).append("\n"));
        }
        return sb.toString();
    }


}
