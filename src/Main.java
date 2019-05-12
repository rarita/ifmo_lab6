
import java.sql.SQLOutput;
import java.util.Scanner;

public class Main {

    public static void main(String[] arguments) {
        // Создаем экземпляр коллекции
        final HumanCollection collection = new HumanCollection("test.csv");
        // Задаем действия при выходе (сохранение данных в файл)
        Runtime.getRuntime().addShutdownHook(new Thread(collection::saveToFile));
        // Интерактивное меню программы
        System.out.println("\nКлючи сравниваются как строки\nЗначения сравниваются по возрасту");
        System.out.println("Введите команду:");
        final Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String command = sc.nextLine();
            String[] args = command.split("\\s");
            switch (args[0]) {
                case "info":
                    collection.showInfo();
                    break;
                case "show":
                    collection.showContents();
                    break;
                case "save":
                    collection.saveToFile();
                    break;
                case "remove_greater_key":
                    if (args.length != 2) {
                        System.out.println("Неверное число аргументов");
                        break;
                    }
                    collection.removeGreaterKeys(args[1]);
                    break;
                case "remove_lower":
                    if (args.length != 2) {
                        System.out.println("Неверное число аргументов");
                        break;
                    }
                    collection.removeLower(args[1]);
                    break;
                case "insert":
                    if (args.length != 3) {
                        System.out.println("Неверное число аргументов");
                        break;
                    }
                    collection.insert(args[1], args[2]);
                    break;
                case "remove":
                    if (args.length != 2) {
                        System.out.println("Неверное число аргументов");
                        break;
                    }
                    collection.remove(args[1]);
                    break;
                case "quit":
                case "exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Неизвестная команда. Проверьте синтакисис!");
            }
        }
    }
}