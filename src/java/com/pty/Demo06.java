package src.java.com.pty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

import static src.java.com.pty.Demo05.server;

/**
 * @author pety
 * @date 2022/07/06 20:08
 **/
public class Demo06 {
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

        //创建客户端通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 2022));

        //设置为非阻塞模式，默认是阻塞模式
        socketChannel.configureBlocking(false);

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        Scanner sc = new Scanner(System.in);

        while(sc.hasNext()){
            String str = sc.next();
            byteBuffer.put(str.getBytes());
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            byteBuffer.clear();
        }

        byteBuffer.clear();
        socketChannel.close();
    }

    /**
     * 服务器端
     */
    public static void server() throws IOException{
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //设置非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //绑定
        serverSocketChannel.bind(new InetSocketAddress(2022));

        //获得选择器
        Selector selector = Selector.open();

        //将通道注册到选择器，并设定为接收操作
        //第一个参数是绑定的选择器，第二个是监听事件类型：read、accept、write、connect
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while(selector.select()>0){
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                //如果处于accept状态，就将通道注册到选择器并设置为读操作
                if(key.isAcceptable()){
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ);
                //如果处于读操作，就开始读取数据。
                }else if(key.isReadable()){
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(10);
                    while(socketChannel.read(byteBuffer)!=-1){
                        int len = byteBuffer.limit();
                        byteBuffer.flip();
                        System.out.println(new String(byteBuffer.array(),0,len));
                        byteBuffer.clear();
                    }
                    socketChannel.close();
                }
                iterator.remove();
            }
        }
        serverSocketChannel.close();
    }
}
