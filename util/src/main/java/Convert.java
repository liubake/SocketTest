import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by Erola on 2018/4/24.
 */
public class Convert {

    /**
     * 端口号
     */
    public final static int Port = 9988;

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
    public final static String CharsetName = "UTF-8";

    /**
     * 将长度转换成 2 byte 的数组（uint）
     *
     * @param bodyLength
     * @return
     */
    public static byte[] getHeadPackage(int bodyLength) {
        if (bodyLength < 0 || bodyLength > MaxBodyLength)
            throw new IllegalArgumentException("无效的长度");
        else {
            byte[] ret = new byte[HeaderLength];
            ret[0] = (byte) ((bodyLength >>> 8) & 0xff);
            ret[1] = (byte) ((bodyLength >>> 0) & 0xff);
            return ret;
        }
    }

    /**
     * 将 2 byte 的数组转换成长度（uint）
     *
     * @param headPackage
     * @return
     */
    public static int getBodyLength(byte[] headPackage) {
        if (headPackage == null || headPackage.length != 2)
            throw new IllegalArgumentException("无效的包头");
        else {
            int a = (headPackage[0] & 0xff) << 8;
            int b = headPackage[1] & 0xff;
            return a | b;
        }
    }

    /**
     * 将 byte 数组转换为字符串
     *
     * @param bodyPackage
     * @return
     */
    public static String getContent(byte[] bodyPackage) {
        if (bodyPackage == null || bodyPackage.length == 0) {
            return "";
        } else {
            return new String(bodyPackage, Charset.forName(CharsetName));
        }
    }

    /**
     * 发送数据处理
     *
     * @param output
     * @param content
     * @throws IOException
     */
    public static void sendProcess(OutputStream output, String content) throws IOException {
        if (output == null)
            throw new IllegalArgumentException("无效的输出流");
        else {
            byte[] totalPackage = getTotalPackage(content);
            output.write(totalPackage);
            output.flush();
        }
    }

    /**
     * 根据内容，返回整个包
     *
     * @param content
     * @return
     */
    public static byte[] getTotalPackage(String content) {
        int bodyLength = 0;
        byte[] headBytes = null;
        byte[] bodyBytes = null;
        if (content != null && !content.isEmpty()) {
            bodyBytes = content.getBytes(Charset.forName(CharsetName));
            bodyLength = bodyBytes.length;
        }
        headBytes = getHeadPackage(bodyLength);
        byte[] packageBytes = new byte[headBytes.length + bodyLength];
        System.arraycopy(headBytes, 0, packageBytes, 0, headBytes.length);
        if (bodyLength > 0) {
            System.arraycopy(bodyBytes, 0, packageBytes, headBytes.length, bodyBytes.length);
        }
        return packageBytes;
    }

    /**
     * 接收数据处理
     *
     * @param input
     * @param handler
     * @throws IOException
     */
    public static void receiveProcess(InputStream input, IMessageHandler handler) throws IOException, InterruptedException {
        if (input == null)
            throw new IllegalArgumentException("无效的输入流");
        else {
            int headLength = 0;
            int bodyLength = 0;
            byte[] headBytes = null;
            byte[] bodyBytes = null;
            while (true) {
                headBytes = new byte[Convert.HeaderLength];
                if (headLength < Convert.HeaderLength) {
                    byte[] remainHeader = new byte[Convert.HeaderLength - headLength];
                    int readLength = input.read(remainHeader);
                    if (readLength > 0) {
                        System.arraycopy(remainHeader, 0, headBytes, headLength, readLength);
                        headLength += readLength;
                    }
                    if (headLength < Convert.HeaderLength) {
                        continue;
                    }
                }
                int totalBodyLength = Convert.getBodyLength(headBytes);
                bodyBytes = new byte[totalBodyLength];
                if (bodyLength < totalBodyLength) {
                    byte[] remainBody = new byte[totalBodyLength - bodyLength];
                    int readLength = input.read(remainBody);
                    if (readLength > 0) {
                        System.arraycopy(remainBody, 0, bodyBytes, bodyLength, readLength);
                        bodyLength += readLength;
                    }
                    if (bodyLength < totalBodyLength) {
                        continue;
                    }
                }
                if (handler != null) {
                    handler.prcessMessage(getContent(bodyBytes));
                }
                headLength = 0;
                bodyLength = 0;
            }
        }
    }
}