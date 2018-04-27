import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Created by Erola on 2018/4/24.
 */
public class Convert {




    public static void receiveProcess(Socket socket, IMessageHandler handler){

        if(socket == null)
            throw new IllegalArgumentException("无效的 socket");
        else{
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
                    int bodyLength = Convert.getBodyLength(headBytes);





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












    /**
     * 包头长度
     */
    public final static int HeaderLength = 2;

    /**
     * 包体长度
     */
    public final static int MaxBodyLength = 65535;

    /**
     * 字符编码格式
     */
    public final static String CharsetName="UTF-8";

    /**
     * 将长度转换成 2 byte 的数组（uint）
     * @param bodyLength
     * @return
     */
    public static byte[] getHeadPackage(int bodyLength){
        if(bodyLength<0 || bodyLength>MaxBodyLength)
            throw new IllegalArgumentException("无效的长度");
        else{
            byte[] ret = new byte[HeaderLength];
            ret[0] = (byte)((bodyLength >>> 8) & 0xff);
            ret[1] = (byte)((bodyLength >>> 0)& 0xff );
            return ret;
        }
    }

    /**
     * 将 2 byte 的数组转换成长度（uint）
     * @param headPackage
     * @return
     */
    public static int getBodyLength(byte[] headPackage){
        if(headPackage==null || headPackage.length!=2)
            throw new IllegalArgumentException("无效的包头");
        else{
            int a = (headPackage[0] & 0xff) << 8;
            int b = headPackage[1] & 0xff;
            return a | b;
        }
    }


    /**
     *
     * @param content
     * @return
     */
    public static byte[] getBodyFormContent(String content){
        if(content==null || content.isEmpty()){
            return null;
        }
        else{
            return content.getBytes(Charset.forName(CharsetName));
        }
    }

    /**
     *
     * @param content
     * @return
     */
    public static byte[] getPackageBodyFromContent(byte[] content){
        if(content==null || content.length==0){
            return null;
        }else {
            byte[] headerBytes = getHeadPackage(content.length);
            byte[] packageBytes = new byte[headerBytes.length + content.length];
            System.arraycopy(headerBytes, 0, packageBytes, 0, headerBytes.length);
            System.arraycopy(content, 0, packageBytes, headerBytes.length, content.length);
            return packageBytes;
        }

    }










}
