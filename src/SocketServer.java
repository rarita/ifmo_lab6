import java.io.IOException;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer implements Runnable {

    private final HumanCollection collection;
    private final ExecutorService threadPool;
    private final InetSocketAddress socketAddress;

    public SocketServer(HumanCollection collection, String host, int port, int threadNumber) {
        this.socketAddress = new InetSocketAddress(host, port);
        this.collection = collection;
        this.threadPool = Executors.newFixedThreadPool(threadNumber);
    }

    @Override
    public void run()  {
        // Открываем новый socket channel
        try (ServerSocketChannel socketChannel = ServerSocketChannel.open()) {
            socketChannel.socket().bind(socketAddress);
            System.out.println("Сервер запущен! Ожидаются входящие соединения...");
            while (true) {
                // Для каждого входящего соединения получаем сокет и передаем в отдельный поток для обработки команд
                threadPool.submit(new ServerThread(collection, socketChannel.accept()));
            }
        }
        catch (IOException e) {
            System.out.println("Не удалось открыть сокет на хосте " + socketAddress.getHostName() + "и порте " + socketAddress.getPort());
        }
    }
}
