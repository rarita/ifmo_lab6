public class Human implements IHuman, IStandardFunc {
    String Name;
    APlace place;
    Human(String n, APlace p) {
        Name = n;
        place = p;
    }

    @Override
    public int hashCode() {
        return super.hashCode()+Name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean comp = obj.hashCode()== this.hashCode() ? true : false;
        return comp;
    }

    @Override
    public String toString() {
        try {
            if (place==null) throw new ExistException();
        } catch (ExistException e) {
            e.printStackTrace();
            place = new Place("unknown");
        } finally {
            return Name + " stays in that place " + place.getPlace();
        }
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
