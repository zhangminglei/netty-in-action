package non.blocking.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SelectorServer {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8081));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        while (true) {
            // 需要不断地去调用 select() 方法获取最新的准备好的通道
            int readyChannels = selector.select();
            if (readyChannels == 0) {
                continue;
            }

            System.out.println("Ready channel : " + readyChannels);

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                System.out.println();
                System.out.println("while loop start ... ");
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    System.out.println("Key is acceptable.");
                    // 有已经接受的新的到服务端的连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    System.out.println("Key is readable.");
                    // 有数据可读
                    // 上面一个 if 分支中注册了监听 OP_READ 事件的 SocketChannel
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int num = socketChannel.read(readBuffer);
                    System.out.println("Num = " + num);
                    if (num > 0) {
                        // 处理进来的数据...
                        System.out.println("收到数据：" + new String(readBuffer.array()).trim());
                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                    } else if (num == -1) {
                        // -1 代表连接已经关闭
                        socketChannel.close();
                        System.out.println("Close invoked.");
                    }
                } else if (key.isWritable()) {
                    System.out.println("Key is writable.");
                    // 通道可写
                    // 给用户返回数据的通道可以进行写操作了
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.wrap("返回给客户端的数据...".getBytes());
                    socketChannel.write(buffer);

                    // 重新注册这个通道，监听 OP_READ 事件，客户端还可以继续发送内容过来
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }
        }
    }
}
