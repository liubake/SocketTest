import java.io.IOException;

/**
 * Created by Erola on 2018/4/27.
 */
public interface IMessageHandler {

    /**
     * 处理消息，并返回
     * @param message
     * @return
     */
    void prcessMessage(String message) throws IOException, InterruptedException;
}
