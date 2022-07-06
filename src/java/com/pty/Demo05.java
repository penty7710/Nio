package src.java.com.pty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 阻塞网络通信演示
 * @author pety
 * @date 2022/07/06 19:44
 **/
public class Demo05 {
    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> {
            try {
                server();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                client();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread1.start();;
        thread2.start();
    }

    /**
     * 客户端
     */
    public static void client() throws IOException {
        //创建客户端通道，InetSocketAddress第一个参数是主机，第二个是端口号，注意不是字符串
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 2022));

        //读取信息
        FileChannel filechannel = FileChannel.open(Paths.get("D:\\Nio\\img.png"), StandardOpenOption.READ);

        //创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //写入数据：将channel中的数据写入到buffer中
        while(filechannel.read(byteBuffer)!=-1){
            byteBuffer.flip();
            //将buffer中的数据通过socketChannel写出
            socketChannel.write(byteBuffer);
            byteBuffer.clear();
        }

        //关闭
        filechannel.close();
        socketChannel.close();
    }

    /**
     * 服务器
     */
    public static  void server() throws IOException {

        //创建服务端通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();


        FileChannel fileChannel = FileChannel.open(Paths.get("D:\\Nio\\4.png"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        //绑定监听的端口号
        serverSocketChannel.bind(new InetSocketAddress(2022));

        //获取客户端的通道，会等待客户端的连接
        SocketChannel socketChannel = serverSocketChannel.accept();

        //创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while(socketChannel.read(byteBuffer)!=-1){
            byteBuffer.flip();
            fileChannel.write(byteBuffer);
            byteBuffer.clear();
        }

        //关闭
        socketChannel.close();
        fileChannel.close();
        serverSocketChannel.close();
    }
}
