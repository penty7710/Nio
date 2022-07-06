package src.java.com.pty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 分散读取和聚集写入
 * @author pety
 * @date 2022/07/06 19:19
 **/
public class Demo04 {
    public static void main(String[] args) throws IOException {
        FileInputStream is = new FileInputStream("D:\\Nio\\hello.txt");
        FileOutputStream os = new FileOutputStream("D:\\Nio\\1.txt");

        //获取channel
        FileChannel inchannel = is.getChannel();
        FileChannel outchannel = os.getChannel();

        //创建多个缓冲区，并放入到缓冲区数组中
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(50);
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(1024);
        ByteBuffer [] byteBuffers = new ByteBuffer[]{byteBuffer1,byteBuffer2};

        //分散读取：是指从Channel 中读取的数据“分散”到多个Buffer 中
        inchannel.read(byteBuffers);
        byteBuffer1.flip();
        byteBuffer2.flip();

        //聚集写入：将多个Buffer 中的数据“聚集”到Channel
        outchannel.write(byteBuffers);
    }
}
