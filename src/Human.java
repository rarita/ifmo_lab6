import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.StringJoiner;

public class Human implements IHuman, IStandardFunc, Comparable<Human> {
    String Name;
    APlace place;
    int age;
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(APlace.class, new PlaceDeserializer()).create();

    public Human(String n, APlace p, int a) {
        this.Name = n;
        this.place = p;
        this.age = a;
    }

    @Override
    public int compareTo(Human other) {
        if (other == null)
            return 0;
        else return this.age - other.age;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Name.hashCode() + place.hashCode() + age;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode()== this.hashCode();
    }

    @Override
    public String toString() {
        try {
            if (place == null) throw new ExistException();
        } catch (ExistException e) {
            e.printStackTrace();
            place = new Place("unknown", 0F, 0F);
        } finally {
            return Name + " of age " + age + " stays in that place " + place.getPlace() +
            " at coordinates " + place.getLatitude() + ";" + place.getLongitude();
        }
    }

    public static Human fromCSV(String[] csvContents) {
        if (csvContents.length != 5)
            throw new IllegalArgumentException("Неверное число данных");
        return new Human(csvContents[0],
                        new Place(csvContents[1], Float.parseFloat(csvContents[2]), Float.parseFloat(csvContents[3])),
                        Integer.parseInt(csvContents[4]));
    }

    public String toCSV() {
        StringJoiner stringJoiner = new StringJoiner(",");
        stringJoiner.add(this.getName());
        stringJoiner.add(this.place.getPlace());
        stringJoiner.add(Float.toString(this.place.getLatitude()));
        stringJoiner.add(Float.toString(this.place.getLongitude()));
        stringJoiner.add(Integer.toString(age));
        return stringJoiner.toString();
    }

    public static Human fromJson(String jsonContents) {
        return gson.fromJson(jsonContents, Human.class);
    }

    /**
     * Получает ключ, под которым хранился объект из JSON элемента Map.Entry
     * @param jsonElement Исходный JSON - элемент
     * @return Ключ, находящийся в объекте
     */
    public static String keyFromJsonEntry(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject()
                .keySet()
                .stream()
                .findFirst()
                .get();
    }

    /**
     * Возвращает объект Human, записанный в сериализованной Map.Entry
     * @param jsonElement Исходный JSON - элемент
     * @return Экземпляр класса Human, описывающий этот элемент
     */
    public static Human fromJsonEntry(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject()
                .entrySet()
                .stream()
                .map(entry -> gson.fromJson(entry.getValue(), Human.class))
                .findFirst()
                .get();
    }

    public String toJson() {
        return gson.toJson(this, this.getClass());
    }

    public void walk(APlace h) {
        place = (APlace)h;
        System.out.println(Name + " walk " + place.getPlace());
    }

    public String getName(){
        try {
            if(haveName()) return Name;
            else {
                throw new CorrectNameException("no name");
            }
        } catch (ExistException e) {
            return e.getExc();
        } catch (CorrectNameException exc) {
            exc.printStackTrace();
            return "nameless";
        }
    }

    public boolean haveName() throws ExistException{
        if (Name==null)throw new ExistException("Name field");
        return !Name.equals("");
    }
}
