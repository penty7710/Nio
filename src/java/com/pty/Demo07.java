package src.java.com.pty;

import com.sun.org.apache.bcel.internal.generic.Select;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * nio 非阻塞 服务器读数据
 * @author : pety
 * @date : 2022/7/8 21:54
 */
public class Demo07 {
    public static void main(String[] args) {
        //获得服务器通道
        try(ServerSocketChannel server = ServerSocketChannel.open()){
            //绑定端口
            server.bind(new InetSocketAddress(8888));
            //设置为非阻塞模式
            server.configureBlocking(false);
            //创建选择器
            Selector selector = Selector.open();
            //将通道注册到选择器，并设置感兴趣的事件
            server.register(selector, SelectionKey.OP_ACCEPT);

            while(true){
                //若没有事件发生，线程会阻塞；当事件发生后，线程运行，防止cpu空转
                //返回发生的事件的个数
                int ready = selector.select();
                System.out.println("selector ready counts :"+ready);

                //获取发生的所有事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                //使用迭代器是为了在运行过程中移出元素
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                //遍历事件
                while(iterator.hasNext()){
                    //拿到事件
                    SelectionKey key = iterator.next();
                    //判断事件的类型
                    if(key.isAcceptable()){
                        //得到监听事件的通道
                        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                        System.out.println("before accepting...");
                        //获取连接
                        SocketChannel socketChannel = channel.accept();
                        System.out.println("after accepting...");
                        //将通道设置为非阻塞模式，并注册到selectionkey中
                        socketChannel.configureBlocking(false);
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        //设置附件，相当于将一个buffer和channel绑定在一起
                        socketChannel.register(selector,SelectionKey.OP_READ,buffer);
                        //移除当前selectionKey
                        iterator.remove();
                    }else if(key.isReadable()){
                        SocketChannel channel = (SocketChannel) key.channel();
                        System.out.println("before reading...");
                        //通过key得到附件:attachment()方法
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = channel.read(buffer);
                        if(read == -1){
                            //取消事件
                            key.cancel();
                            channel.close();
                        }else{
                            split(buffer);
                            // 如果缓冲区太小，就进行扩容
                            if(buffer.position() == buffer.limit()){
                                //创建一个新的buffer，容量是原来的两倍
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                //切换到读模式
                                buffer.flip();
                                // 将旧buffer中的内容放入新的buffer中
                                newBuffer.put(buffer);
                                // 将新buffer放到key中作为附件
                                key.attach(newBuffer);
                            }
                        }
                        System.out.println("after reading...");
                        // 处理完毕后移除
                        iterator.remove();
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void split(ByteBuffer buffer) {
        buffer.flip();
        for(int i = 0; i < buffer.limit(); i++) {
            // 遍历寻找分隔符
            // get(i)不会移动position
            if (buffer.get(i) == '\n') {
                // 缓冲区长度
                int length = i+1-buffer.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                // 将前面的内容写入target缓冲区
                for(int j = 0; j < length; j++) {
                    // 将buffer中的数据写入target中
                    target.put(buffer.get());
                }
                // 打印结果
                System.out.println(StandardCharsets.UTF_8.decode(target));
            }
        }
        // 切换为写模式，但是缓冲区可能未读完，这里需要使用compact
        buffer.compact();
    }
}
