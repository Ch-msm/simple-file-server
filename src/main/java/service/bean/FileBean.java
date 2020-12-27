package service.bean;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件bean
 * Date  2020/12/26 22:15
 *
 * @author msm
 */
public class FileBean implements java.io.Serializable {
  /**
   * 文件id
   */
  private String id;
  /**
   * 父级id
   */
  private String parentId;
  /**
   * 文件名字
   */
  private String name;
  /**
   * 文件大小
   */
  private Long size;
  /**
   * 文件当前大小
   */
  private Long currentSize;
  /**
   * 文件类 [文件夹|文件]
   */
  private String type;
  /**
   * 文件后缀
   */
  private String suffix;
  /**
   * 上传时间
   */
  private String time;
  /**
   * 文件
   */
  private MultipartFile file;

  public FileBean() {

  }

  public FileBean(String id, String parentId, String name, Long size, Long currentSize, String type, String suffix, String time) {
    this.id = id;
    this.parentId = parentId;
    this.name = name;
    this.size = size;
    this.currentSize = currentSize;
    this.type = type;
    this.suffix = suffix;
    this.time = time;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public Long getCurrentSize() {
    return currentSize;
  }

  public void setCurrentSize(Long currentSize) {
    this.currentSize = currentSize;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }
}
