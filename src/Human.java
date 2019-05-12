import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Human implements IHuman, IStandardFunc, ISerializable<Human>, Comparable<Human> {
    String Name;
    APlace place;
    int age;

    Human(String n, APlace p, int a) {
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
            if (place==null) throw new ExistException();
        } catch (ExistException e) {
            e.printStackTrace();
            place = new Place("unknown");
        } finally {
            return Name + " of age " + age + " stays in that place " + place.getPlace();
        }
    }

    private static String getJsonField(String source, String fieldName) {
        final String pattern = "(?<=\"" + fieldName + "\").*?(?=(,|}))";
        final Pattern regex = Pattern.compile(pattern);
        final Matcher matcher = regex.matcher(source);
        if (matcher.find()) {
            return matcher.group()
                    .replaceAll("(\"|:)", "")
                    .trim();
        }
        else return null;
    }

    public static Human fromJson(String jsonObject) {
        String name = getJsonField(jsonObject, "name");
        APlace place = new Place(getJsonField(jsonObject, "place"));
        Integer age = Integer.parseInt(getJsonField(jsonObject, "age"));
        return new Human(name, place, age != null ? age : 0);
    }

    public String toJson() {
        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"name\":\"");
        stringBuilder.append(this.getName() + "\",");
        stringBuilder.append("\"place\":\"");
        stringBuilder.append(this.place.getPlace() + "\",");
        stringBuilder.append("\"age\":");
        stringBuilder.append(this.age + "}");
        return stringBuilder.toString();
    }


    public static Human fromCSV(String[] csvContents) {
        if (csvContents.length != 3)
            throw new IllegalArgumentException("CSV data and object do not match");
        return new Human(csvContents[0], new Place(csvContents[1]), Integer.parseInt(csvContents[2]));
    }

    @Override
    public String toCSV() {
        StringJoiner stringJoiner = new StringJoiner(",");
        stringJoiner.add(this.getName());
        stringJoiner.add(this.place.getPlace());
        stringJoiner.add(Integer.toString(age));
        return stringJoiner.toString();
    }

    public void walk(APlace h){
        place = h;
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
