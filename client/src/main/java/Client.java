import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Erola on 2018/4/23.
 */
public class Client {

    public static void main(String[] args) {
        Socket socket = null;
        InputStream input = null;
        OutputStream output = null;

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", Convert.Port));
            input = socket.getInputStream();
            output = socket.getOutputStream();
            final InputStream finalInput = input;
            final OutputStream finalOutput = output;
            (new Thread(()->{
                try {
                    Convert.receiveProcess(finalInput, (String message)->{
                        System.out.println(String.format("***--------------------------------------***\n%s", message));
                        /**----------------响应回复---------------------**/
                        Thread.sleep(500);
                        Convert.sendProcess(finalOutput, ""
                                +"向收到该信息的世界致以美好的祝愿。\n"
                                +"通过以下信息，你们将对地球文明有一个基本的了解。\n"
                                +"人类经过漫长的劳动和创造，建立了灿烂的文明，涌现出丰富多彩的文化，并初步了解了自然界和人类社会运行发展的规律，我们珍视这一切。\n"
                                +"但我们的世界仍有很大缺陷，存在着仇恨、偏见和战争，由于生产力和生产关系的矛盾，财富的分布严重不均，相当部分的人类成员生活在贫困和苦难之中。\n"
                                +"人类社会正在努力解决自己面临的各种困难和问题，努力为地球文明创造一个美好的未来。\n"
                                +"发送该信息的国家所从事的事业就是这种努力的一部分。\n"
                                +"我们致力于建立一个理想的社会，使每个人类成员的劳动和价值都得到充分的尊重，使所有人的物质和精神需要都得到充分的满足，使地球文明成为一个更加完美的文明。\n"
                                +"我们怀着美好的愿望，期待着与宇宙中其他文明社会建立联系，期待着与你们一起，在广阔的宇宙中创造更加美好的生活。\n");
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })).start();
            Convert.sendProcess(output, "HELLO!");
            (new BufferedReader(new InputStreamReader(System.in))).readLine();
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                    output.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}