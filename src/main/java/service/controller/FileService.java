package service.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import service.bean.FileBean;

import javax.websocket.server.PathParam;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件服务
 * Date  2020/12/25 14:50
 *
 * @author msm
 */
@Controller
public class FileService {
  private static final Logger L = LogManager.getLogger("文件服务");
  public static Path source;

  public static File multipartFileToFile(MultipartFile file) throws Exception {
    InputStream ins = file.getInputStream();
    File toFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
    inputStreamToFile(ins, toFile);
    ins.close();
    return toFile;
  }

  @RequestMapping("mkdir")
  @ResponseBody
  public String mkdir(@RequestParam("mkdir") String mkdir) {
    L.info("创建文件夹:{}", mkdir);
    try {

    } catch (Exception e) {
      L.catching(e);
      return "上传失败";
    }
    return "上传成功";
  }

  @RequestMapping(value = "list", method = RequestMethod.GET)
  @ResponseBody
  public List<FileBean> fileList(@PathParam("url") String url) {
    try {
      return Files.list(Paths.get(source.toString(), url)).map(o -> {
        FileBean fileBean = new FileBean();
        try {
          File file = o.toFile();
          String name = file.getName();
          long[] sizeArray = {0};
          countFileSize(sizeArray, o);
          FileTime modifiedTime = Files.getLastModifiedTime(o);
          LocalDateTime localDateTime = LocalDateTime.ofInstant(
              Instant.ofEpochMilli(modifiedTime.toMillis()),
              ZoneOffset.of("+8")
          );
          String fileType;
          if (Files.isDirectory(o)) {
            fileType = "文件夹";
          } else {
            int index = name.lastIndexOf(".");
            if (index != -1) {
              fileType = name.substring(index + 1).toUpperCase() + "文件";
            } else {
              fileType = "未知文件";
            }
          }
          fileBean.setName(name);
          fileBean.setSize(sizeArray[0]);
          fileBean.setId(name);
          fileBean.setType(fileType);
          fileBean.setTime(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (IOException e) {
          e.printStackTrace();
        }
        return fileBean;
      }).collect(Collectors.toList());
    } catch (IOException e) {
      L.catching(e);
    }
    return null;
  }

  @RequestMapping(value = "delete", method = RequestMethod.GET)
  @ResponseBody
  public String delete(@PathParam("id") String id) {
    L.info("删除文件:{}", id);
    try {
      deleteFile(Paths.get(source.toString(), id));
    } catch (IOException e) {
      return e.getMessage();
    }
    return "删除成功";
  }

  /**
   * 删除文件和文件价如果有下级文件 一起删除
   *
   * @param path 文件路径
   */
  private void deleteFile(Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.list(path).forEach(o -> {
        try {
          deleteFile(o);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
    Files.deleteIfExists(path);
  }

  private void countFileSize(long[] initSize, Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.newDirectoryStream(path).forEach(o -> {
        try {
          countFileSize(initSize, o);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    } else {
      initSize[0] += Files.size(path);
    }
  }

  public static void inputStreamToFile(InputStream ins, File file) {
    try {
      OutputStream os = new FileOutputStream(file);
      int bytesRead = 0;
      byte[] buffer = new byte[8192];
      while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
        os.write(buffer, 0, bytesRead);
      }
      os.close();
      ins.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @RequestMapping(value = "upload", method = RequestMethod.POST)
  @ResponseBody
  public String upload(FileBean fileBean) {
    L.info("上传文件:{}", fileBean.getName());
    try {
      Path file = Paths.get(source.toAbsolutePath().toString(), fileBean.getId());
      Files.copy(fileBean.getFile().getInputStream(), file);
    } catch (Exception e) {
      L.catching(e);
      return "上传失败";
    }
    return "上传成功";
  }
}
