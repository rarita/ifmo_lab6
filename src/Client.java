import com.google.gson.*;

import java.io.*;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Client {

    private static final int BUFFER_DEFAULT_SIZE = 1024;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(APlace.class, new PlaceDeserializer())
            .create();
    private static final JsonParser parser = new JsonParser();

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    /**
     * Конструктор, устанавливающий соединение с сервером и инициализирующий потоки ввода/вывода
     * @param serverHost Хост сервера
     * @param serverPort Порт сервера
     * @throws IOException
     */
    public Client(String serverHost, int serverPort) throws IOException {

        this.socket = new Socket(serverHost, serverPort);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        System.out.println("Соединение установлено!");
    }

    /**
     * Получает данные с сервера, ожидая пригласительной строки
     * @return Ответ сервера без символа завершения сообщения
     * @throws IOException
     */
    private String getResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        CharBuffer buffer = CharBuffer.allocate(BUFFER_DEFAULT_SIZE);
        buffer.clear();

        while (reader.read(buffer) > 0) {
            buffer.flip();

            while (buffer.hasRemaining()) {
                char c = buffer.get();
                if (c == TransmissionParams.TERMINATING_CHAR)
                    return sb.toString();

                sb.append(c);
            }
        }
        throw new IOException("Сервер не отправил метку завершения ответа");
    }

    /**
     * Посылает на сервер запрос в виде строки с данными
     * @param query Запрос, который необходимо отправить
     * @throws IOException если сервер оборвал связь
     */
    private void sendQuery(String query) throws IOException {
        writer.append(query);
        writer.append(TransmissionParams.TERMINATING_CHAR);
        writer.flush();
    }

    /**
     * Переводит JSON-ответ в строку в зависимости от переданного типа JSON-объекта
     * @param response JSON-объект, который необходимо расшифровать
     * @return Строка для вывода на экран клиента
     */
    private String decryptResponse(String response) {
        try {

            JsonElement element = parser.parse(response);
            String type = element.getAsJsonObject().get("type").getAsString();
            JsonElement object = element.getAsJsonObject().get("object");

            switch (type) {
                case "text":
                    return object.getAsString();
                case "map":
                    return decryptHumanCollection(object);
                default:
                    return "Сервер ответил неподдерживаемым типом данных: " + type;
            }

        }
        catch (JsonParseException e) {
            return "Не удалось расшифровать ответ сервера: " + response + " по причине " + e.getMessage();
        }
    }

    /**
     * Возвращает строку, содержащую описание всех элементов коллекции, переданной в параметре
     * @param collection JSON-объект типа HumanCollection
     * @return Строковое предстваление объекта для вывода на экран
     */
    private String decryptHumanCollection(JsonElement collection) {
        if (!collection.isJsonArray())
            return "";

        List<String> humanList = StreamSupport.stream(collection.getAsJsonArray().spliterator(), false)
                                                .map(jsonElement -> Human.keyFromJsonEntry(jsonElement) + " : " + Human.fromJsonEntry(jsonElement).toString())
                                                .collect(Collectors.toList());

        return String.join(";\n", humanList);
    }

    /**
     * Преобразует путь к файлу, указанный пользователем в команде import, в содержимое этого файла
     * @param query Полный текст запроса, произведенного пользователем
     * @return Измененный для сервера текст запроса
     * @throws IOException при отсутствии файла/отсутствии прав на чтение
     */
    private String packImportCommand(String query) throws IOException {
        String filePath = query.split("\\s")[1];
        String[] lines = HumanCollection.loadFileLines(filePath);
        return "import " + String.join("\n", lines);
    }

    /**
     * Упаковывает введенную клиентом команду в JSON - строку
     * @param rawQuery Введенная клиентом команда
     * @return Команда, отформатированная для передачи на сервер
     */
    private String packQuery(String rawQuery) {

        if (rawQuery.isBlank())
            return "{\"cmd\":\"nop\"}";

        JsonObject result = new JsonObject();
        String[] args = rawQuery.split("\\s");

        JsonElement cmd = gson.toJsonTree(args[0]);
        result.add("cmd", cmd);

        if (args.length > 1) {
            JsonElement data = gson.toJsonTree(Arrays.copyOfRange(args, 1, args.length));
            result.add("data", data);
        }

        return gson.toJson(result);
    }

    /**
     * Начинает интерактивную сессию обмена данными с сервером
     */
    public void startSession() {
        Scanner sc = new Scanner(System.in);

        try {

            while (socket.isConnected()) {
                // Получение и расшифровка ответа
                String response = getResponse();
                System.out.println(decryptResponse(response));

                // Упаковка и отправка запроса
                System.out.print(TransmissionParams.WELCOMING_LINE);
                String query = sc.nextLine();

                if (query.startsWith("import")) {
                    try { query = packImportCommand(query); }
                    catch (IOException e) {
                        System.out.println("Не удалось прочитать файл " + query.split("\\s")[1]);
                        query = "";
                    }
                }

                sendQuery(packQuery(query));
            }

        }
        catch (IOException e) {
            System.out.println("Во время обмена данными произошла ошибка: " + e.getMessage());
        }
    }

}
