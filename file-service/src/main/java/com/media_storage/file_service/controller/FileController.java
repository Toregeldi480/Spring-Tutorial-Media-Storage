package com.media_storage.file_service.controller;

import com.media_storage.file_service.component.RangeParser;
import com.media_storage.file_service.service.FileService;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/file")
@RestController
public class FileController {
    private final FileService fileService;
    private final RangeParser rangeParser;

    public FileController(FileService fileService, RangeParser rangeParser) {
        this.fileService = fileService;
        this.rangeParser = rangeParser;
    }

    @GetMapping("/all")
    public ResponseEntity<String[]> all(@RequestHeader(value = "X-Username") String username) throws IOException {
        return fileService.all(username);
    }

    @GetMapping("/get")
    public ResponseEntity<Resource> serve(@RequestHeader(value = "X-Username") String username, @RequestHeader(value = "Range", required = false) String rangeHeader, @RequestParam(value = "filename") String fileName) throws IOException {
        FileService.FileInfo fileInfo = fileService.getFileInfo(username, fileName);

        if (rangeHeader != null) {
            RangeParser.Range range = rangeParser.parse(rangeHeader, fileInfo.getSize());
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, fileInfo.getContentType())
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(range.getContentLength()))
                    .header(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d-%d", range.getStart(), range.getEnd(), fileInfo.getSize()))
                    .body(fileService.serveRangeFile(fileInfo.getPath(), range.getStart(), range.getEnd()));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, fileInfo.getContentType())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileInfo.getSize()))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(fileService.serve(fileInfo.getPath()));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestHeader(value = "X-Username") String username, @RequestPart MultipartFile file) throws IOException {
        fileService.upload(username, file);
        return ResponseEntity.ok().body(String.format("File %s Uploaded", file.getOriginalFilename()));
    }

    @PostMapping("/delete")
    public ResponseEntity<String> delete(@RequestHeader(value = "X-Username") String username, @RequestParam(value = "filename") String fileName) throws IOException {
        fileService.delete(username, fileName);
        return ResponseEntity.ok().body(String.format("File %s Deleted", fileName));
    }
}
