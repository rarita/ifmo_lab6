import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HumanCollection extends LinkedHashMap<String, Human> {

    private final Date initDate;
    private final String fileName;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Конструктор класса, принимающий имя файла, с которым будет производится работа
     * Также регистрирует время, когда был создан экземпляр класса
     * @param fileName путь к целевому файлу
     */
    public HumanCollection(String fileName) {
        initDate = new Date();
        this.fileName = fileName;
        try {
            loadFromFile();
            System.out.println("Данные из файла " + fileName + " успешно загружены!");
        }
        catch (IOException e) {
            System.out.println("Загрузка данных из файла " + fileName + " не удалась.");
        }
    }

    /**
     * Процедура отображения информации о текущем состоянии коллекции
     */
    void showInfo() {
        System.out.println("Тип коллекции: " + getClass());
        System.out.println("Была создана: " + dateFormat.format(initDate));
        System.out.println("Текущее количество элементов: " + this.size());
    }

    /**
     * Процедура удаления из коллекции всех элементов, с ключами меньше, чем заданный
     * Ключи сравниваются как строки
     * @param key контрольный ключ
     */
    void removeGreaterKeys(String key) {
        List<Map.Entry<String, Human>> list = new ArrayList(entrySet());
        list.sort(Map.Entry.comparingByKey());
        // Двигаемся по списку с конца в начало
        for (int i = list.size() - 1; i >= 0; i-- ){
            if (list.get(i).getKey().compareTo(key) <= 0)
                break;
            remove(list.get(i).getKey());
        }
    }

    /**
     * Процедура удаления из коллекции всех элементов со значениями меньше, чем заданный
     * Элементы сравниваются по возрасту
     * @param jsonElement контрольный элемент в формате JSON
     */
    void removeLower(String jsonElement) {
        Human elem = Human.fromJson(jsonElement);
        List<Map.Entry<String, Human>> list = new ArrayList(entrySet());
        list.sort(Map.Entry.comparingByValue());
        for (Map.Entry<String, Human> item : list) {
            if (item.getValue().compareTo(elem) >= 0)
                break;
            remove(item.getKey());
        }
    }

    /**
     * Процедура отображения на экран элементов, находящихся в коллекции.
     */
    void showContents() {
        for (Map.Entry<String, Human> entry : this.entrySet())
            System.out.println(entry.getKey() + " : " + entry.getValue().toString());
    }

    /**
     * Процедура добавления нового элемента в коллекцию
     * @param key ключ добавляемого элемента
     * @param jsonElement добавляемый элемент, заданный в формате JSON
     */
    void insert(String key, String jsonElement) {
        boolean result = put(key, Human.fromJson(jsonElement)) != null;
        System.out.println("Элемент с ключом " + key + (result ? " пере": "") + "записан в коллекцию");
    }

    /**
     * Процедура загрузки данных в коллекцию из файла, указанного при создании класса
     * @see #HumanCollection(String)
     */
    void loadFromFile() throws IOException {
        InputStream inputStream = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(inputStream);
        StringBuilder sb = new StringBuilder();
        int t;
        while((t=isr.read())!= -1)
            sb.append((char)t);
        String[] lines = sb.toString().split("\r?\n");
        for (String line : lines) {
            String[] fields = line.split(",");
            String key = fields[0];
            Human value = Human.fromCSV(Arrays.copyOfRange(fields, 1, fields.length));
            this.put(key, value);
        }
        isr.close();
        inputStream.close();
    }

    /**
     * Процедура сохранения данных коллекции в файл, заданный при создании класса
     * @see #HumanCollection(String)
     */
    void saveToFile() {
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
            System.out.println("Данные успешно сохранены в файл " + fileName);
        }
        catch (IOException e) {
            System.out.println("Ошибка сохранения данных в файл " + fileName);
        }
    }

    /**
     * Процедура удаления заданного ключом элемента из списка
     * @param key ключ элемента, подлежащего к удалению
     */
    void remove(String key){
        boolean result = super.remove(key) != null;
        System.out.println(result ? "Элемент с ключем " + key + " был успешно удален" : "Элемент не был обнаружен");
    }
}
