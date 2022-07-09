package src.java.com.pty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * nio selector 多线程优化
 * boss线程用来处理accpet事件
 * woker线程用来处理read事件
 * boss线程将客户端连接轮询发给woker线程
 * @author : pety
 * @date : 2022/7/10 1:31
 */
public class Demo10 {
    public static void main(String[] args) {
        try(ServerSocketChannel server = ServerSocketChannel.open()){
            //设置当前线程为boss线程
            Thread.currentThread().setName("boss");
            server.bind(new InetSocketAddress(8888));
            //负责轮询accept事件的selector
            Selector boss = Selector.open();
            server.configureBlocking(false);
            server.register(boss, SelectionKey.OP_ACCEPT);

            //创建固定数量的worker
            Worker [] workers = new Worker[4];

            //用于负载均衡的原子整数
            AtomicInteger robin = new AtomicInteger(0);

            for(int i=0;i<workers.length;i++){
                workers[i] = new Worker("work"+i);
            }

            while(true){
                boss.select();
                Set<SelectionKey> selectionKeys = boss.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    // BossSelector负责Accept事件
                    if(key.isAcceptable()){
                        //建立连接
                        SocketChannel socket = server.accept();
                        System.out.println("connected...");
                        socket.configureBlocking(false);

                        System.out.println("before read...");
                        // socket注册到Worker的Selector中
                        // 负载均衡，轮询分配Worker
                        workers[robin.getAndIncrement()% workers.length].register(socket);
                        System.out.println("after read...");
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static class Worker implements Runnable{
        private String name;
        private Thread thread;
        private Selector selector;

        private volatile boolean started = false;

        //同步队列，用于boss线程和woker线程之间的通信
        private ConcurrentLinkedDeque<Runnable> queue;


        public Worker(String name){
            this.name = name;
        }

        public void register(SocketChannel socket) throws IOException {
            //初始化，开启一个新的线程，一个woker只会执行一次
            if(!started){
                thread = new Thread(this,name);
                selector = Selector.open();
                queue = new ConcurrentLinkedDeque<>();
                thread.start();;
                started = true;
            }

            // 向同步队列中添加SocketChannel的注册事件
            // 在Worker线程中执行注册事件
            queue.add(()->{
                try {
                    socket.register(selector,SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            // 唤醒被阻塞的Selector
            // select类似LockSupport中的park，wakeup的原理类似LockSupport中的unpark
            selector.wakeup();
        }

        @Override
        public void run() {
            while(true){
                try {
                    selector.select();
                    Runnable task = queue.poll();
                    if(task!= null){
                        //执行channel的注册操作
                        //调用run方法，没有开启新的线程
                        task.run();
                    }
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    //遍历事件
                    while(iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if(key.isReadable()){
                            // 简化处理，省略细节
                            SocketChannel socket = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            socket.read(buffer);
                            buffer.flip();
                            //打印
                            System.out.println(Charset.defaultCharset().decode(buffer));
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
