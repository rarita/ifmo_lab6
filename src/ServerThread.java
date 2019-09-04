import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ServerThread implements Runnable {

    private static final int BUFFER_DEFAULT_SIZE = 1024;
    private static final JsonParser parser = new JsonParser();

    private final SocketChannel socketChannel;
    private final String clientHost;
    private final HumanCollection collection;

    public ServerThread(HumanCollection collection, SocketChannel socketChannel) {

        this.socketChannel = socketChannel;
        this.collection = collection;

        String mClientHost;
        try { mClientHost = socketChannel.getRemoteAddress().toString(); }
        catch (IOException e) { mClientHost = "Неизвестен"; }
        this.clientHost = mClientHost;

    }

    @Override
    public void run() {
        try {
            System.out.println(clientHost + " подключился...");
            writeResponse(setJsonObjectType(getIntro()));

            // Пока пользователь не отправит команду завершения или не отключится от сессии
            // продолжаем обрабатывать поступающие запросы
            do { }
            while (socketChannel.isConnected() && handleCommand());
        }
        catch (IOException e) {
            System.out.println("Ошибка передачи данных между клиентом и сервером. Текст ошибки: " + e.getMessage());
        }
        System.out.println("Клиент " + clientHost + " отключен. Завершаю поток...");
    }

    /**
     * Считывает строку запроса, поступающую с клиента. Строка, должна кончаться символом завершения запроса
     * @see TransmissionParams
     * @return Запрос клиента в формате JSON
     * @throws IOException
     */
    private String readQuery() throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_DEFAULT_SIZE);
        buffer.clear();

        StringBuilder sb = new StringBuilder();
        while (socketChannel.isConnected() && socketChannel.read(buffer) > 0) {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.flip());

            while (charBuffer.hasRemaining()) {
                char c = charBuffer.get();

                if (c == TransmissionParams.TERMINATING_CHAR)
                    return sb.toString();

                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Отвечает на запрос клиента сформированной строкой
     * Добавляет в конец строки символ, обозначающий конец передачи сообщения
     * @see TransmissionParams
     * @param response Строка, которую необходимо отправить текущему клиенту
     * @throws IOException
     */
    private void writeResponse(String response) throws IOException {
        CharBuffer charBuffer = CharBuffer.wrap(response + TransmissionParams.TERMINATING_CHAR);
        ByteBuffer encodedBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        socketChannel.write(encodedBuffer);
    }

    /**
     * Возвращает сообщение, которое сервер посылает сразу после установки соединения с клиентом
     * @return Приветственное сообщение сервера
     */
    private String getIntro() {

        return "\"Принято соединение от "+ clientHost +
                "\nКлиент находится в потоке " + Thread.currentThread().toString() +
                "\n" + "*".repeat(18) +
                "\nКлючи сравниваются как строки\nЗначения (Human) сравниваются по возрасту\nЭлементы JSON записываются БЕЗ пробелов\n" +
                "Введите команду:\n\"";
    }

    /**
     * Стандартная реализация
     * @param json JSON-строка самого объекта
     * @return Декорированный типом JSON-объект
     */
    private String setJsonObjectType(String json) {
        return setJsonObjectType("text", json);
    }

    /**
     * Декорирует существующий JSON параметром type, позволяющим клиенту легко понять, что за тип нужно использовать для расшифровки
     * @param type Строка типа данного объекта
     * @param json JSON-строка самого объекта
     * @return Декорированный типом JSON-объект
     */
    private String setJsonObjectType(String type, String json) {
        return "{\"type\":\"" + type + "\",\"object\":" + json + "}";
    }

    /**
     * Преобразует JsonElement, являющийся массивом, в массив String
     * @param json JsonElement (Array)
     * @return Массив строк, представляющий входной параметр
     */
    private String[] jsonToStringArray(JsonElement json) {
        if (json == null || !json.isJsonArray())
            return null;

        return StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .toArray(String[]::new);
    }

    /**
     * Получает команду, переданную клиентом и выполняет ее
     * @return true, если ожидаются новые команды от клиента; false, если поступил запрос о завершении сеанса
     */
    private boolean handleCommand() throws IOException {
        // Если соединение закрылось, завершаем обработку этого потока
        if (!socketChannel.isConnected())
            return false;

        // Считываем и расшифровываем команду
        // Команда должна быть упакована клиентом в JSON, содержащий cmd и data[]
        String query = readQuery();
        JsonElement jsonCommand = parser.parse(query);

        String action = jsonCommand.getAsJsonObject()
                                    .get("cmd")
                                    .getAsString();

        JsonElement jsonArgs = null;
        if (jsonCommand.getAsJsonObject().has("data")) {
            jsonArgs = jsonCommand.getAsJsonObject()
                                    .get("data")
                                    .getAsJsonArray();
        }
        String[] args = jsonToStringArray(jsonArgs);

        // Отвечаем на команду
        String response;
        switch (action) {
            case "info":
                response = collection.showInfo();
                writeResponse(setJsonObjectType(response));
                break;
            case "load":
                response = collection.loadFromFile();
                writeResponse(setJsonObjectType(response));
            case "import":
                response = collection.loadFromLines(args);
                writeResponse(setJsonObjectType(response));
                break;
            case "show":
                response = collection.getContents();
                writeResponse(setJsonObjectType("map", response));
                break;
            case "save":
                response = collection.saveToFile();
                writeResponse(setJsonObjectType(response));
                break;
            case "remove_greater_key":
                if (args == null || args.length != 1) {
                    writeResponse(setJsonObjectType("\"Неверное число аргументов\""));
                    break;
                }
                writeResponse(setJsonObjectType(collection.removeGreaterKeys(args[0])));
                break;
            case "remove_lower":
                if (args == null || args.length != 1) {
                    writeResponse(setJsonObjectType("\"Неверное число аргументов\""));
                    break;
                }
                writeResponse(setJsonObjectType(collection.removeLower(args[0])));
                break;
            case "insert":
                if (args == null || args.length != 2) {
                    writeResponse(setJsonObjectType("\"Неверное число аргументов\""));
                    break;
                }
                writeResponse(setJsonObjectType(collection.insert(args[0], args[1])));
                break;
            case "remove":
                if (args == null || args.length != 1) {
                    writeResponse(setJsonObjectType("\"Неверное число аргументов\""));
                    break;
                }
                writeResponse(setJsonObjectType(collection.remove(args[0])));
                break;
            case "quit":
            case "exit":
                return false;
            case "nop":
                writeResponse(setJsonObjectType("\"Введите команду:\""));
                break;
            default:
                writeResponse(setJsonObjectType("\"Неизвестная команда. Проверьте синтакисис!\""));
        }
        return true;
    }

}
