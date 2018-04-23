import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Erola on 2018/4/25.
 */
public class ServerHandler implements Runnable {

    private Socket socket;

    public  ServerHandler(Socket socket){
        this.socket=socket;
    }

    @Override
    public void run() {
        PrintWriter out=null;
        BufferedReader in=null;
        try{
            String content=null;
            out=new PrintWriter(socket.getOutputStream(), true);
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true){
                content=in.readLine();



            }



        }catch (Exception e){

        }finally {
            if(in!=null){
                try {
                    in.close();
                    in=null;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
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
