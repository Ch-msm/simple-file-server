package service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import service.controller.FileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 主服务
 * Date  2020/8/12 20:34
 *
 * @author msm
 */
@SpringBootApplication
@Controller
public class App implements WebMvcConfigurer {
  private static final Logger L = LogManager.getLogger("主服务");


  public static void main(String[] args) throws IOException {
    //创建目标文件夹
    FileService.source = Paths.get("File");
    Files.createDirectories(FileService.source);
    L.debug("目标文件夹路径：{}", FileService.source.toAbsolutePath());
    SpringApplication.run(App.class, args);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    //跨域
    registry.addMapping("/**")
        .allowedOrigins("*")
        .allowCredentials(true)
        .allowedMethods("GET", "POST", "DELETE", "PUT")
        .maxAge(3600);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/file/**").addResourceLocations(
        FileService.source.toUri().toString());
  }
}
