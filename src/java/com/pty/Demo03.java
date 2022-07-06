package src.java.com.pty;


import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 通道间直接传输
 * @author pety
 * @date 2022/07/06 18:00
 **/
public class Demo03 {
     public static void main(String[] args) throws IOException {
          FileChannel inchannel = FileChannel.open(Paths.get("D:\\Nio\\img.png"), StandardOpenOption.READ);
          FileChannel outchannel = FileChannel.open(Paths.get("D:\\Nio\\3.png"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

          //通道间直接传输,position表示传输的起始位置
          inchannel.transferTo(0,inchannel.size(),outchannel);

          //对应的还有transferFrom,
          //outchannel.transferFrom(inchannel,0,inchannel.size());

          /**
           *  通道内直接传输效率最高，open+直接缓存次之，最差的是getchannel+非直接缓存
           */

     }
}
