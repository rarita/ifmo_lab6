public class ExistException extends NullPointerException {
    private String trouble  = " not initialized";
    ExistException(){
        trouble = "The entity is not initialized.";
    }

    ExistException(String s){
        super(s);
        trouble = s + trouble;
    }

    ExistException(String s, String ss){
        super(s);
        trouble = s + ss;
    }


    public String getExc() {
        return trouble;
    }
}