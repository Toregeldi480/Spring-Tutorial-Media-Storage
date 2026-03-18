package com.media_storage.file_service.service;

import com.media_storage.file_service.exception.EmptyFileException;
import com.media_storage.file_service.exception.SecurityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {
  @Value("${spring.application.root-location}")
  private String rootLocation;

  private final UserServiceClient userServiceClient;

  public FileService(UserServiceClient userServiceClient) {
      this.userServiceClient = userServiceClient;
  }

  public ResponseEntity<String[]> all(String username) throws IOException {
    File userDir = this.getUserPath(username).toFile();
    String[] fileNames = userDir.list();
    return ResponseEntity.ok().body(fileNames);
  }

  public Resource serve(Path filePath) throws IOException {
    if (!filePath.toFile().exists()) {
      throw new FileNotFoundException(String.format("File %s Not Found", filePath.getFileName()));
    }
    return new FileSystemResource(filePath.toFile());
  }

  public Resource serveRangeFile(Path filePath, long start, long end) throws IOException {
    InputStream inputStream = Files.newInputStream(filePath);
    inputStream.skip(start);
    long contentLength = end - start + 1;
    return new InputStreamResource(new LimitedInputStream(inputStream, contentLength));
  }

  public void upload(String username, MultipartFile file) throws IOException {
    if (file.isEmpty()) {
      throw new EmptyFileException("Cannot Upload Empty File");
    }

    Path filePath = this.getSecureFilePath(username, file.getOriginalFilename());

    if (!filePath.getParent().equals(this.getUserPath(username))) {
      throw new SecurityViolationException("Cannot Store File Outside User Directory");
    }
    if (filePath.toFile().exists()) {
      throw new FileAlreadyExistsException(String.format("File With Name '%s' Already Exists", filePath.getFileName()));
    }

    userServiceClient.changeFileCount(username, 1);
    InputStream inputStream = file.getInputStream();
    Files.copy(inputStream, filePath);
  }

  public void delete(String username, String fileName) throws IOException {
    FileInfo fileInfo = this.getFileInfo(username, fileName);

    if (!fileInfo.getPath().getParent().equals(this.getUserPath(username))) {
      throw new SecurityViolationException("Cannot Delete Outside User Directory");
    }

    if (!Files.exists(fileInfo.getPath())) {
      throw new FileNotFoundException(String.format("File %s Not Found", fileName));
    }

    userServiceClient.changeFileCount(username, -1);
    Files.delete(fileInfo.getPath());
  }

  public FileInfo getFileInfo(String username, String fileName) throws IOException {
    Path filePath = this.getSecureFilePath(username, fileName);
    File file = filePath.toFile();

    return new FileInfo(filePath, file.length(), Files.probeContentType(filePath));
  }

  private Path getSecureFilePath(String username, String fileName) throws IOException {
    String cleanFileName = new File(fileName).getName();
    Path userPath = this.getUserPath(username);
    Path filePath = userPath.resolve(cleanFileName).normalize();

    if (!filePath.startsWith(userPath)) {
      throw new SecurityViolationException("Path Traversal Attempt");
    }

    return filePath;
  }

  private Path getUserPath(String username) throws IOException {
    Path path = Paths.get(this.rootLocation, username).normalize();
    if (!Files.exists(path)) {
      Files.createDirectory(path);
    }

    return path;
  }

  public static class FileInfo {
    private Path path;
    private long size;
    private String contentType;

    public FileInfo(Path path, long size, String contentType) {
      this.path = path;
      this.size = size;
      this.contentType = contentType;
    }

    public Path getPath() {
      return this.path;
    }
    public long getSize() {
      return this.size;
    }
    public String getContentType() {
      return this.contentType;
    }
  }

  private static class LimitedInputStream extends InputStream {
    private final InputStream inputStream;
    private long remaining;

    LimitedInputStream(InputStream inputStream, long limit) {
      this.inputStream = inputStream;
      this.remaining = limit;
    }

    @Override
    public int read() throws IOException {
      if (remaining <= 0) return -1;
      int result = inputStream.read();
      if (result != -1) remaining--;
      return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (remaining <= 0) return -1;
      int bytesToRead = (int) Math.min(len, remaining);
      int bytesRead = inputStream.read(b, off, bytesToRead);
      if (bytesRead > 0) remaining -= bytesRead;
      return bytesRead;
    }

    @Override
    public void close() throws IOException {
      inputStream.close();
    }
  }
}
