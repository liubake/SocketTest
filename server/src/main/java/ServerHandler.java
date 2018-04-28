import java.io.*;
import java.net.Socket;

/**
 * Created by Erola on 2018/4/25.
 */
public class ServerHandler implements Runnable {

    /**
     *
     */
    private Socket socket;

    /**
     *
     * @param socket
     */
    public  ServerHandler(Socket socket){
        this.socket=socket;
    }

    /**
     *
     */
    @Override
    public void run() {
        InputStream input=null;
        OutputStream output=null;
        try{
            input = socket.getInputStream();
            output = socket.getOutputStream();
            final OutputStream finalOutput = output;
            Convert.receiveProcess(input, (String message)->{
                System.out.println(String.format("***--------------------------------------***\n%s", message));
                /**-------------下面开始回复消息------------**/
                Thread.sleep(500);
                Convert.sendProcess(finalOutput, ""
                        +"我是这个世界的一个和平主义者，我首先收到信息是你们文明的幸运，警告你们：不要回答！不要回答！！不要回答！！！\n"
                        +"你们的方向上有千万颗恒星，只要不回答，这个世界就无法定位发射源。\n"
                        +"如果回答，发射源将被定位，你们的行星系将遭到入侵，你们的世界将被占领！不要回答！不要回答！！不要回答！！！\n");
            });
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(output!=null){
                try {
                    output.close();
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }
            if(socket!=null){
                try {
                    socket.close();
                    socket=null;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}