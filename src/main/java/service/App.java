package service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
      Path newFile = Files.createFile(
          Paths.get(path.toAbsolutePath().toString(), file.getOriginalFilename())
      );
      file.transferTo(newFile);
    } catch (IOException e) {
      L.catching(e);
    }
    return "上传成功";
  }
}
