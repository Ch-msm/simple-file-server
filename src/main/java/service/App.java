package service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件服务
 * Date  2020/8/12 20:34
 *
 * @author msm
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@Controller
public class App implements WebMvcConfigurer {
  private static final Logger L = LogManager.getLogger("文件服务");
  private static Path path;

  public static void main(String[] args) throws IOException {
    //创建目标文件夹
    path = Paths.get("File");
    Files.createDirectories(path);
    L.debug("目标文件夹路径：{}", path.toAbsolutePath());
    SpringApplication.run(App.class, args);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/file/**").addResourceLocations(
        path.toUri().toString());
  }

  @RequestMapping("upload")
  @ResponseBody
  public String upload(@RequestParam("file") MultipartFile file) {
    L.info("上传文件:{}", file.getOriginalFilename());
    try {
      Path path = Paths.get(App.path.toAbsolutePath().toString(), file.getOriginalFilename());
      Files.deleteIfExists(path);
      Path newFile = Files.createFile(
          Paths.get(App.path.toAbsolutePath().toString(), file.getOriginalFilename())
      );
      file.transferTo(newFile);
    } catch (IOException e) {
      L.catching(e);
      return "上传失败";
    }
    return "上传成功";
  }

  @RequestMapping(value = "list", method = RequestMethod.GET)
  @ResponseBody
  public List<List<String>> fileList() {
    try {
      return Files.list(path).map(o -> {
        List<String> arr = new ArrayList<>();
        try {
          File file = o.toFile();
          String name = file.getName();
          Long size = Files.size(o);
          FileTime modifiedTime = Files.getLastModifiedTime(o);
          LocalDateTime localDateTime = LocalDateTime.ofInstant(
              Instant.ofEpochMilli(modifiedTime.toMillis()),
              ZoneOffset.of("+8")
          );
          arr.add(name);
          arr.add(size + "");
          arr.add(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (IOException e) {
          e.printStackTrace();
        }
        return arr;
      }).collect(Collectors.toList());
    } catch (IOException e) {
      L.catching(e);
    }
    return null;
  }

}
