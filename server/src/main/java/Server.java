import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Erola on 2018/4/23.
 */
public class Server {

    public  static  void main(String[] args){
        ExecutorService executor=null;
        ServerSocket serverSocket=null;

        try{
            executor=Executors.newFixedThreadPool(100);
            serverSocket=new ServerSocket(Convert.Port);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
