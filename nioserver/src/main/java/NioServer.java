import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Erola on 2018/4/23.
 */
public class NioServer {

    public  static  void main(String[] args){
        ExecutorService executor=null;
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;

        try {
            executor= Executors.newFixedThreadPool(100);
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(Convert.Port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true){
                if (selector.select() > 0) {
                    Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                    Iterator<SelectionKey> selectionKeyIterator = selectionKeySet.iterator();
                    while (selectionKeyIterator.hasNext()) {
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        selectionKeyIterator.remove();
                        if (selectionKey.isAcceptable()) {
                            SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                        }
                        if (selectionKey.isReadable()) {
                            //executor.execute(new NioServerHandler(selectionKey));
                            final Executor finalExecutor=executor;
                            try {
                                Convert.nioReceiveProcess(selectionKey, (String message)->{
                                    System.out.println(String.format("%s", message));
                                    /*String content = ""
                                            +"我是这个世界的一个和平主义者，我首先收到信息是你们文明的幸运，警告你们：不要回答！不要回答！！不要回答！！！\n"
                                            +"你们的方向上有千万颗恒星，只要不回答，这个世界就无法定位发射源。\n"
                                            +"如果回答，发射源将被定位，你们的行星系将遭到入侵，你们的世界将被占领！不要回答！不要回答！！不要回答！！！\n";*/
                                    finalExecutor.execute(()->{
                                        try {
                                            Convert.nioSendProcess(selectionKey, message);
                                        } catch (IOException e) {
                                            try {
                                                selectionKey.channel().close();
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }
                                            e.printStackTrace();
                                        }
                                    });
                                });
                            } catch (IOException e) {
                                try {
                                    selectionKey.channel().close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        /*if (selectionKey.isWritable()) {

                        }*/
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(serverSocketChannel!=null) {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(selector!=null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}