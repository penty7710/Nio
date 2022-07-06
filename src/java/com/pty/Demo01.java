package src.java.com.pty;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * getchannel + 非直接缓冲区
 * @author pety
 * @date 2022/07/06 17:35
 **/
public class Demo01 {
    public static void main(String[] args) {
        //获得输入输出流
        FileInputStream is = null;
        FileOutputStream os = null;

        //获得通道
        FileChannel inchannel = null;
        FileChannel outchannel = null;

        try {
            is = new FileInputStream("D:\\Nio\\img.png");
            os = new FileOutputStream("D:\\Nio\\1.png");

            //获得通道
            inchannel = is.getChannel();
            outchannel = os.getChannel();

            //开辟缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //没有数据会返回-1
            while(inchannel.read(buffer)!=-1){
                //转为读模式
                buffer.flip();
                outchannel.write(buffer);
                //清除缓冲区数据，同时会将缓冲区切换到写模式
                buffer.clear();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
