import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Erola on 2018/4/23.
 */
public class Client {

    public  static  void main(){
        int port=9988;
        Socket socket=null;
        PrintWriter out=null;
        BufferedReader in=null;

        try{
            socket=new Socket("127.0.0.1", port);
            out=new PrintWriter(socket.getOutputStream(),true);
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));



        }catch (Exception e1){


        }finally {
            if(in!=null){
                try{
                    in.close();
                    in=null;
                }catch (IOException e1){
                    e1.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                    out.close();
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }
            if(socket!=null){
                try {
                    socket.close();
                    socket=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
