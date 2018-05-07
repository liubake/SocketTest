import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * Created by Erola on 2018/5/7.
 */
public class NioServerHandler implements Runnable {

    /**
     *
     */
    private SelectionKey selectionKey;

    /**
     *
     * @param selectionKey
     */
    public NioServerHandler(SelectionKey selectionKey){
        this.selectionKey=selectionKey;
    }

    @Override
    public void run() {
        try {
            Convert.nioReceiveProcess(selectionKey, (String message)->{
                System.out.println(String.format("%s", message));
                /*String content = ""
                        +"我是这个世界的一个和平主义者，我首先收到信息是你们文明的幸运，警告你们：不要回答！不要回答！！不要回答！！！\n"
                        +"你们的方向上有千万颗恒星，只要不回答，这个世界就无法定位发射源。\n"
                        +"如果回答，发射源将被定位，你们的行星系将遭到入侵，你们的世界将被占领！不要回答！不要回答！！不要回答！！！\n";*/
                Convert.nioSendProcess(selectionKey, message);
            });
        } catch (Exception e) {
            Convert.nioReceiveMap.remove(selectionKey.channel());
            try {
                selectionKey.channel().close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            selectionKey.cancel();
            e.printStackTrace();
        }
    }
}