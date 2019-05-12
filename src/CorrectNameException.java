public class CorrectNameException extends Exception {
    private String exc;
    CorrectNameException(String s){
        super(s);
        exc = s;
    }

    public String getExc() {
        return exc;
    }
}