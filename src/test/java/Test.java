import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Date  2020/12/27 18:15
 *
 * @author msm
 */
public class Test {
  InputStream in;

  public static void main(String[] args) throws IOException {
    Test test = new Test();
    Path path = Paths.get("File", "3.txt");
    test.in = new FileInputStream(path.toFile());
    //test.test1();
    //test.test2();
    //test.test3();
    //test.test4();
    //test.test5();
    test.test6();
  }


  public void test1() throws IOException {
    long start = System.currentTimeMillis();
    Path path = Paths.get("File", UUID.randomUUID().toString() + ".txt");
    FileOutputStream out = new FileOutputStream(path.toFile());
    //BufferedInputStream in = new FileInputStream(srcFile);
    // BufferedOutputStream out = new FileOutputStream(destFile);
    int length = 0;// 每次读取到的字节数组的长度
    //用来存储每次读取到的字节数组,设置的读取大小跟耗时有关
    byte[] bytes = new byte[1024 * 1000];

    while ((length = in.read(bytes)) > 0) {
      out.write(bytes, 0, length);
    }
    out.close();
    System.out.println("测试1：" + ((System.currentTimeMillis() - start) / 1000.0) + "秒");
    Files.deleteIfExists(path);
  }

  public void test2() throws IOException {
    long start = System.currentTimeMillis();
    Path path = Paths.get("File", UUID.randomUUID().toString() + ".txt");
    Files.copy(in, path);
    System.out.println("测试2：" + ((System.currentTimeMillis() - start) / 1000.0) + "秒");
    Files.deleteIfExists(path);
  }

  public void test3() throws IOException {
    long start = System.currentTimeMillis();
    Path path = Paths.get("File", UUID.randomUUID().toString() + ".txt");
    BufferedReader bur = new BufferedReader(new InputStreamReader(System.in));
    BufferedWriter buw = new BufferedWriter(new FileWriter(path.toFile()));
    int num = 0;
    char[] ch = new char[1024 * 10];
    while ((num = bur.read(ch, 0, ch.length)) != -1)//num为读取的字符数，如果已到达流末尾，则返回 -1
    {
      buw.write(new String(ch), 0, num);
      buw.flush();
    }
    long end = System.currentTimeMillis();
    System.out.println("time_04: " + (end - start));
    bur.close();
    buw.close();
    Files.deleteIfExists(path);
  }

  public void test5() throws IOException {
    /*
     * 测试结果与MappedByteBuffer size有关
     */
    Path file = Paths.get("File", UUID.randomUUID().toString() + ".txt");
    RandomAccessFile rafi = new RandomAccessFile(Paths.get("File", "3.txt").toFile(), "r");
    RandomAccessFile rafo = new RandomAccessFile(file.toFile(), "rw");
    FileChannel fci = rafi.getChannel();
    FileChannel fco = rafo.getChannel();
    long size = fci.size();
    //FileChannel提供了map方法来把文件影射为内存映像文件（即虚拟内存）；
    //可以把文件的从position开始的size大小的区域映射为内存映像文件，mode指出了 可访问该内存映像文件的方式：
    MappedByteBuffer mbbi = fci.map(FileChannel.MapMode.READ_ONLY, 0, size);
    MappedByteBuffer mbbo = fco.map(FileChannel.MapMode.READ_WRITE, 0, size);
    long start = System.currentTimeMillis();
    //或者
    mbbo.put(mbbi.get(new byte[mbbi.limit()]));
    fci.close();
    fco.close();
    rafi.close();
    rafo.close();
    System.out.println("test5 Spend: " + (double) (System.currentTimeMillis() - start) + "ms");
    Files.deleteIfExists(file);
  }

  public void test6() throws IOException {
    Path file = Paths.get("File", UUID.randomUUID().toString() + ".txt");
    RandomAccessFile rafi = new RandomAccessFile(Paths.get("File", "1.txt").toFile(), "r");
    RandomAccessFile rafo = new RandomAccessFile(file.toFile(), "rw");
    FileChannel fci = rafi.getChannel();
    FileChannel fco = rafo.getChannel();
    long start = System.currentTimeMillis();
    fci.transferTo(0, fci.size(), fco);
    fci.close();
    fco.close();
    rafi.close();
    rafo.close();
    System.out.println("测试5: " + (double) (System.currentTimeMillis() - start) + "ms");
    Files.deleteIfExists(file);
  }

}
