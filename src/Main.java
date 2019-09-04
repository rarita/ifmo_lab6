import java.io.IOException;

public class Main {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3456;
    private static final int SERVER_THREAD_POOL_SIZE = 4;

    public static void main(String[] arguments) {

        if (arguments.length != 1) {
            System.out.println("Неверное число аргументов");
            return;
        }

        switch (arguments[0]) {
            case "client":
                runClient();
                break;
            case "server":
                runServer();
                break;
            default:
                System.out.println("Неподдерживаемый тип запуска: " + arguments[0]);
        }
    }

    private static void runClient(){

        System.out.println("Приложение запущено в режиме клиента");

        try {
            Client client = new Client(SERVER_HOST, SERVER_PORT);
            client.startSession();
        }
        catch (IOException e) {
            System.out.println("Во время установки связи с клиентом произошла ошибка: " + e.getMessage());
        }

    }

    private static void runServer() {

        System.out.println("Приложение запущено в режиме сервера");

        // Создаем экземпляр коллекции
        String fileName = System.getenv("CSVFILE");
        if (fileName == null) {
            System.out.println("Переменная среды, указывающая на файл, не найдена\nПроверьте переменные среды");
            System.exit(0);
        }

        final HumanCollection collection = new HumanCollection(fileName);
        // Задаем действия при выходе (сохранение данных в файл)
        Runtime.getRuntime().addShutdownHook(new Thread(collection::saveToFile));
        // Запускаем сторону сервера

        SocketServer server = new SocketServer(collection, SERVER_HOST, SERVER_PORT, SERVER_THREAD_POOL_SIZE);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}