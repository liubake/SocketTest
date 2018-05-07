import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
     *
     */
    public static Map<Channel, PackageReceiveTempData> nioReceiveMap = new ConcurrentHashMap<Channel, PackageReceiveTempData>();

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
     * BIO发送数据处理
     *
     * @param outputStream
     * @param content
     * @throws IOException
     */
    public static void bioSendProcess(OutputStream outputStream, String content) throws IOException {
        if (outputStream == null)
            throw new IllegalArgumentException("无效的输出流");
        else {
            byte[] totalPackage = getTotalPackage(content);
            outputStream.write(totalPackage);
            outputStream.flush();
        }
    }

    /**
     * BIO接收数据处理
     *
     * @param inputStream
     * @param handler
     * @throws IOException
     */
    public static void bioReceiveProcess(InputStream inputStream, IMessageHandler handler) throws IOException, InterruptedException {
        if (inputStream == null)
            throw new IllegalArgumentException("无效的输入流");
        else {
            PackageReceiveTempData tempData=new PackageReceiveTempData();
            while (true) {
                if (tempData.headLength < Convert.HeaderLength) {
                    if(tempData.headLength==0){
                        tempData.headBytes = new byte[Convert.HeaderLength];
                    }
                    byte[] remainHeader = new byte[Convert.HeaderLength - tempData.headLength];
                    int readLength = inputStream.read(remainHeader);
                    if (readLength > 0) {
                        System.arraycopy(remainHeader, 0, tempData.headBytes, tempData.headLength, readLength);
                        tempData.headLength += readLength;
                    }
                    if (tempData.headLength < Convert.HeaderLength) {
                        continue;
                    }else{
                        tempData.totalBodyLength = Convert.getBodyLength(tempData.headBytes);
                    }
                }
                if (tempData.bodyLength < tempData.totalBodyLength) {
                    if(tempData.bodyLength==0){
                        tempData.bodyBytes = new byte[tempData.totalBodyLength];
                    }
                    byte[] remainBody = new byte[tempData.totalBodyLength - tempData.bodyLength];
                    int readLength = inputStream.read(remainBody);
                    if (readLength > 0) {
                        System.arraycopy(remainBody, 0, tempData.bodyBytes, tempData.bodyLength, readLength);
                        tempData.bodyLength += readLength;
                    }
                    if (tempData.bodyLength < tempData.totalBodyLength) {
                        continue;
                    }
                }
                if (handler != null) {
                    handler.prcessMessage(getContent(tempData.bodyBytes));
                }
                tempData.reset();
            }
        }
    }

    /**
     * NIO发送数据处理
     *
     * @param selectionKey
     * @param content
     * @throws IOException
     */
    public static void nioSendProcess(SelectionKey selectionKey, String content) throws IOException {
        if (selectionKey == null)
            throw new IllegalArgumentException("无效的SelectionKey");
        else {
            byte[] totalPackage = getTotalPackage(content);
            ByteBuffer sendBuffer = ByteBuffer.allocate(totalPackage.length);
            sendBuffer.clear();
            sendBuffer.put(totalPackage);
            sendBuffer.flip();
            while(sendBuffer.hasRemaining()) {
                ((SocketChannel)selectionKey.channel()).write(sendBuffer);
            }
        }
    }

    /**
     * NIO接收数据处理
     *
     * @param selectionKey
     * @param handler
     * @throws IOException
     * @throws InterruptedException
     */
    public static void nioReceiveProcess(SelectionKey selectionKey, IMessageHandler handler) throws IOException, InterruptedException {
        if (selectionKey == null)
            throw new IllegalArgumentException("无效的SelectionKey");
        else{
            ByteBuffer receiveBuffer = ByteBuffer.allocate(128);
            int readLength = ((SocketChannel)selectionKey.channel()).read(receiveBuffer);
            PackageReceiveTempData tempData=nioReceiveMap.getOrDefault(selectionKey.channel(), new PackageReceiveTempData());
            while (receiveBuffer.hasRemaining() || readLength>0) {
                receiveBuffer.flip();
                if (tempData.headLength < Convert.HeaderLength) {
                    if (tempData.headLength == 0) {
                        tempData.headBytes = new byte[Convert.HeaderLength];
                    }
                    int bufferRemainLength = receiveBuffer.remaining();
                    int headerRemainLength = Convert.HeaderLength-tempData.headLength;
                    if(bufferRemainLength >= headerRemainLength){
                        receiveBuffer.get(tempData.headBytes, tempData.headLength, headerRemainLength);
                        tempData.headLength=Convert.HeaderLength;
                    }else{
                        receiveBuffer.get(tempData.headBytes, tempData.headLength, bufferRemainLength);
                        tempData.headLength+=bufferRemainLength;
                    }
                    if (tempData.headLength < Convert.HeaderLength) {
                        receiveBuffer.clear();
                        readLength = ((SocketChannel)selectionKey.channel()).read(receiveBuffer);
                        continue;
                    } else {
                        tempData.totalBodyLength = Convert.getBodyLength(tempData.headBytes);
                    }
                }
                if (tempData.bodyLength < tempData.totalBodyLength) {
                    if (tempData.bodyLength == 0) {
                        tempData.bodyBytes = new byte[tempData.totalBodyLength];
                    }
                    int bufferRemainLength = receiveBuffer.remaining();
                    int bodyRemainLength = tempData.totalBodyLength-tempData.bodyLength;
                    if(bufferRemainLength >= bodyRemainLength){
                        receiveBuffer.get(tempData.bodyBytes, tempData.bodyLength, bodyRemainLength);
                        tempData.bodyLength=tempData.totalBodyLength;
                    }else{
                        receiveBuffer.get(tempData.bodyBytes, tempData.bodyLength, bufferRemainLength);
                        tempData.bodyLength+=bufferRemainLength;
                    }
                    if (tempData.bodyLength < tempData.totalBodyLength) {
                        receiveBuffer.clear();
                        readLength = ((SocketChannel)selectionKey.channel()).read(receiveBuffer);
                        continue;
                    }else{
                        if (handler != null) {
                            handler.prcessMessage(getContent(tempData.bodyBytes));
                        }
                        tempData.reset();
                    }
                }
            }
            if(readLength==0){
                nioReceiveMap.put(selectionKey.channel(), tempData);
            }else{
                nioReceiveMap.remove(selectionKey.channel());
                try {
                    selectionKey.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                selectionKey.cancel();
            }
        }
    }

    /**
     * 存储包数据模型
     */
    private static class PackageReceiveTempData{
        public int headLength = 0;
        public int bodyLength = 0;
        public int totalBodyLength = 0;
        public byte[] headBytes = null;
        public byte[] bodyBytes = null;

        /**
         * 重置数据
         */
        public void reset(){
            this.headLength=0;
            this.bodyLength=0;
            this.totalBodyLength=0;
            this.headBytes=null;
            this.bodyBytes=null;
        }
    }
}