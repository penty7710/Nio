package src.java.com.pty;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * nio 服务器 可写事件
 * @author : pety
 * @date : 2022/7/9 0:49
 */
public class Demo08 {
    public static void main(String[] args) {
        try(ServerSocketChannel server = ServerSocketChannel.open()){
            server.bind(new InetSocketAddress(8888));
            server.configureBlocking(false);

            Selector selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);

            while(true){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()){
                    SelectionKey key = iterator.next();

                    iterator.next();
                    if(key.isAcceptable()){
                        SocketChannel socket = server.accept();
                        socket.configureBlocking(false);
                        SelectionKey sckey = socket.register(selector, SelectionKey.OP_READ, null);

                        //构造写入的数据
                        StringBuilder stringBuilder = new StringBuilder();
                        for(int i=0;i<5000000;i++){
                            stringBuilder.append("a");
                        }
                        ByteBuffer buffer = Charset.defaultCharset().encode(stringBuilder.toString());

                        //写入数据
                        int write = socket.write(buffer);
                        System.out.println(write);

                        //如果buffer没有发送完
                        if(buffer.hasRemaining()){
                            //关注可写事件，sckey.interestOps()得到已经关注的事件
                            sckey.interestOps(sckey.interestOps()+SelectionKey.OP_WRITE);
                            //设置附件
                            sckey.attach(buffer);
                        }
                    }else if(key.isWritable()){
                        SocketChannel socket = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int write = socket.write(buffer);
                        System.out.println(write);

                        //如果buffer已经没有数据，进行清理操作
                        if(!buffer.hasRemaining()){
                            //清除buffer
                            key.attach(null);
                            //清除可写事件
                            key.interestOps(key.interestOps()-SelectionKey.OP_WRITE);
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
