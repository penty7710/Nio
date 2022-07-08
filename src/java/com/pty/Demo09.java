package src.java.com.pty;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * nio 客户端 可写事件
 * @author : pety
 * @date : 2022/7/9 1:10
 */
public class Demo09 {
    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open()){
            //设置连接的主机和端口号
            socketChannel.connect(new InetSocketAddress("127.0.0.1",8888));

            //接收数据
            int count =0;
            while(true){
                ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                count+= socketChannel.read(buffer);
                System.out.println(count);
                buffer.clear();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
