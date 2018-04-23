import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Erola on 2018/4/23.
 */
public class Server {

    public  static  void main(){
        int port=9988;
        ExecutorService executor=null;
        ServerSocket serverSocket=null;

        try{
            executor=Executors.newFixedThreadPool(100);
            serverSocket=new ServerSocket(port);
            while (true){
                Socket socket=serverSocket.accept();
                executor.execute(new ServerHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(serverSocket!=null){
                try {
                    serverSocket.close();
                    serverSocket=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
