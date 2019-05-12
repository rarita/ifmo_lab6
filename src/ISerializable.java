import java.util.List;

public interface ISerializable<V> {
    static <V> V fromJson(String jsonObject) { return null; }
    String toJson();

    static <V> V fromCSV(List<String> csvContents) { return null; }
    String toCSV();
}
