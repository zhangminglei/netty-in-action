package blocking.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 192.168.1.109:3777
 收到请求：1
 收到请求：1
 收到请求：1
 收到请求：2
 收到请求：2
 收到请求：2
 收到请求：3
 收到请求：3
 收到请求：3
 收到请求：7
 收到请求：8
 收到请求：9
 */
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        while (true) {
            System.out.println("while loop begin......");
            SocketChannel socketChannel = serverSocketChannel.accept(); // blocking here.
            if (socketChannel.isConnected()) {
                System.out.println(socketChannel.getRemoteAddress());
                SocketHandler handler = new SocketHandler(socketChannel);
                System.out.println("This connected thread hashcode : " + handler.hashCode());
                Thread thread = new Thread(handler);
                thread.start();
                System.out.println("This thread state is : " + thread.getState());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println();
            }
        }
    }
}

class SocketHandler implements Runnable {

    private SocketChannel socketChannel;

    public SocketHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            // 将请求数据读入 Buffer 中
            int num;
            while ((num = socketChannel.read(buffer)) > 0) {
                // 读取 Buffer 内容之前先 flip 一下
                buffer.flip();

                // 提取 Buffer 中的数据
                byte[] bytes = new byte[num];
                buffer.get(bytes);

                String re = new String(bytes, "UTF-8");
                System.out.println("Clients request received：" + re);

                // 回应客户端
                ByteBuffer writeBuffer = ByteBuffer.wrap(("I am a server and I have received your data : " + re).getBytes());
                socketChannel.write(writeBuffer);

                buffer.flip();
            }
        } catch (IOException e) {
            try {
                socketChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}