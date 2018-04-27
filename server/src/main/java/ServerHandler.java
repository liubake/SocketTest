import java.io.*;
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
        InputStream in=null;

        byte[] headBytes = new byte[0];
        byte[] bodyBytes = new byte[0];
        try{
            out=new PrintWriter(socket.getOutputStream(), true);
            in = socket.getInputStream();
            while (true){
                if(headBytes.length < Convert.HeaderLength){
                    int remainLength = Convert.HeaderLength-headBytes.length;
                    byte[] remainHeader = new byte[remainLength];
                    int readLength = in.read(remainHeader);
                    if(readLength>0) {
                        System.arraycopy(remainHeader, 0, headBytes, headBytes.length, readLength);
                    }
                    if(readLength < remainLength){
                        continue;
                    }
                }
                //int bodyLength = Convert.getBodyLength()





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
