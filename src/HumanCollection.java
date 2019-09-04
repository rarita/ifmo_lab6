import com.google.gson.*;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Потокобезопасная коллекция типа Human
 * Методы, изменяющие коллекцию, помечены как synchronized
 */
public class HumanCollection extends ConcurrentHashMap<String, Human> {

    private final Date initDate;
    private final String fileName;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(APlace.class, new PlaceDeserializer())
            .create();
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Конструктор класса, принимающий имя файла, с которым будет производится работа
     * Также регистрирует время, когда был создан экземпляр класса
     * @param fileName путь к целевому файлу
     */
    public HumanCollection(String fileName) {
        initDate = new Date();
        this.fileName = fileName;
        System.out.println(loadFromFile().replace("\"", ""));
    }

    /**
     * Получить информацию о текущем состоянии коллекции
     * @return строка, описывающая состояние коллекции
     */
    String showInfo() {
        return gson.toJson("Тип коллекции: " + getClass() + "\n" +
                "Была создана: " + dateFormat.format(initDate) + "\n" +
                "Текущее количество элементов: " + this.size());
    }

    /**
     * Процедура удаления из коллекции всех элементов, с ключами больше, чем заданный
     * Ключи сравниваются как строки
     * @param key контрольный ключ
     * @return
     */
    synchronized String removeGreaterKeys(String key) {
        this.keySet()
                            .stream()
                            .filter(human -> key.compareTo(human) < 0)
                            .forEach(this::remove);

        return "\"Элементы с ключом, большим чем " + key + " удалены\"";
    }

    /**
     * Процедура удаления из коллекции всех элементов со значениями меньше, чем заданный
     * Элементы сравниваются по возрасту
     * @param jsonElement контрольный элемент в формате JSON
     * @return Результат удаления элементов
     */
    synchronized String removeLower(String jsonElement) {
        try {
            Human elem = Human.fromJson(jsonElement);
            this.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().compareTo(elem) < 0)
                    .map(Entry::getKey)
                    .forEach(this::remove);
            return "\"Элементы, меньшие чем" + elem.toString() + " успешно удалены\"";
        }
        catch (JsonSyntaxException e) {
            return ("\"Ошибка обработки JSON строки: " + e.getMessage() + "\"");
        }
    }

    /**
     * Процедура возвращает данные, которые содержит коллекция на данный момент
     * @return Сериализованная строка, отражающая данные коллекции
     */
    String getContents() {

        var filteredContents = this.entrySet()
                                    .stream()
                                    .sorted(Comparator.comparingInt(stringHumanEntry -> stringHumanEntry.getValue().age))
                                    .map(stringHumanEntry -> {
                                        var obj = new JsonObject();
                                        var key = stringHumanEntry.getKey();
                                        obj.add(key, gson.toJsonTree(this.get(key)));
                                        return obj;
                                    })
                                    //.map(stringHumanEntry -> new SimpleEntry(stringHumanEntry.getKey(), gson.toJson(this.get(stringHumanEntry.getKey()))))
                                    .collect(Collectors.toList());

        return gson.toJson(filteredContents, filteredContents.getClass());
    }

    /**
     * Процедура выводит текущие элементы коллекции на экран
     */
    void showContents() {
        for (Map.Entry<String, Human> entry : this.entrySet())
            System.out.println(entry.getKey() + " : " + entry.getValue().toString());
    }

    /**
     * Процедура добавления нового элемента в коллекцию
     * @param key ключ добавляемого элемента
     * @param jsonElement добавляемый элемент, заданный в формате JSON
     * @return Результат добавления элемента в коллекцию
     */
    synchronized String insert(String key, String jsonElement) {
        try {
            boolean result = put(key, Human.fromJson(jsonElement)) != null;
            return ("\"Элемент с ключом " + key + (result ? " пере" : " ") + "записан в коллекцию\"");
        }
        catch (JsonSyntaxException e) {
            return ("\"Ошибка обработки JSON строки: " + e.getMessage() + "\"");
        }
    }

    /**
     * Загружает содержимое файла как массив строк
     * @param fileName Путь к целевому файлу
     * @return Содержимое файла в виде массива строк
     * @throws IOException если файла нет/нет прав на чтение
     */
    static String[] loadFileLines(String fileName) throws IOException {
        InputStream inputStream = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(inputStream);
        StringBuilder sb = new StringBuilder();

        int t;
        while ((t = isr.read()) != -1)
            sb.append((char) t);

        isr.close();
        inputStream.close();

        return sb.toString().split("\r?\n");
}

    /**
     * Процедура загрузки данных в коллекцию из файла, указанного при создании класса
     * @see #HumanCollection(String)
     * @return Результат операции
     */
    String loadFromFile() {
        try {
            String[] lines = loadFileLines(fileName);
            loadCSVLines(lines);
        }
        catch (IOException | IllegalArgumentException e) {
            return "\"Во время чтения произошла ошибка: " + e.getMessage() + "\"";
        }

        return "\"Данные из файла " + fileName + " успешно загружены!\"";
    }

    /**
     * Заполняет коллекцию записями из массива CSV строк
     * @param lines Входной массив строк
     * @return Результат операции
     */
    String loadFromLines(String[] lines) {

        try { loadCSVLines(lines); }
        catch (IllegalArgumentException e) {
            return "\"Во время чтения произошла ошибка: " + e.getMessage() + "\"";
        }

        return "\"Данные из строк успешно загружены!\"";
    }

    /**
     * Процедура десериализации и записи массива CSV - строк в текущее хранилище коллекции
     * @param lines Строки, которые должны быть записаны
     * @throws IllegalArgumentException при неверном формате входного CSV
     */
    void loadCSVLines(String[] lines) throws IllegalArgumentException {
        for (String line : lines) {
            String[] fields = line.split(",");
            String key = fields[0];
            Human value = Human.fromCSV(Arrays.copyOfRange(fields, 1, fields.length));
            this.put(key, value);
        }
    }

    /**
     * Процедура сохранения данных коллекции в файл, заданный при создании класса
     * @see #HumanCollection(String)
     * @return Возвращает сообщение о результате записи значений в файл
     */
    String saveToFile() {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            for (Map.Entry<String, Human> entry : this.entrySet()) {
                String data = entry.getKey() + ',' + entry.getValue().toCSV();
                bos.write(data.getBytes());
                bos.write("\r\n".getBytes());
            }

            bos.close();
            fos.close();
            return "\"Данные успешно сохранены в файл " + fileName + "\"";
        }
        catch (IOException e) {
            return "\"Ошибка сохранения данных в файл " + fileName + "\"";
        }
    }

    /**
     * Процедура удаления заданного ключом элемента из списка
     * @param key ключ элемента, подлежащего к удалению
     * @return Сообщение о результате выполнения метода
     */
    synchronized String remove(String key){
        boolean result = super.remove(key) != null;
        return (result ? "\"Элемент с ключем " + key + " был успешно удален\"" : "Элемент не был обнаружен\"");
    }
}
