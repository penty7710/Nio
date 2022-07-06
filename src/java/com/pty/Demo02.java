package src.java.com.pty;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * open + 直接缓冲区
 * @author pety
 * @date 2022/07/06 17:43
 **/
public class Demo02 {
    public static void main(String[] args) throws IOException {
        //通过open()获取通道，第一个参数是文件的路径，第二个是打开方式
        FileChannel inchannel = FileChannel.open(Paths.get("D:\\Nio\\img.png"), StandardOpenOption.READ);

        // outChannel需要为 READ WRITE CREATE模式
        // READ WRITE是因为后面获取直接缓冲区时模式为READ_WRITE模式
        // CREATE是因为要创建新的文件
        FileChannel outchannel = FileChannel.open(Paths.get("D:\\Nio\\2.png"), StandardOpenOption.READ,
                                                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        //获得直接缓冲区
        MappedByteBuffer inMapBuf = inchannel.map(FileChannel.MapMode.READ_ONLY, 0, inchannel.size());
        MappedByteBuffer outMapBuf = outchannel.map(FileChannel.MapMode.READ_WRITE, 0, inchannel.size());

        //字节数组
        byte[]bytes = new byte[inMapBuf.limit()];

        // 因为是直接缓冲区，可以直接将数据放入到内存映射文件，无需通过通道传输
        inMapBuf.get(bytes);
        outMapBuf.put(bytes);
    }
}
