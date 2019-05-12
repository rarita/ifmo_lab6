public class Thing extends AThing {
    Thing(String s) {
        super(s);
    }

    String Things;
    APlace place;

    public void Fall(){
        String Thing = Type;
        System.out.println(Thing + " Fall in to the car");
    }
    public void Untied(){
        String Thing = Type;
        System.out.println(Thing + " get untied");
    }

    public void taketo(APlace h){
        String Thing = Type;
        place = h;
        System.out.println(Thing + " take the apples to the " + place.getPlace());
    }

    public void drives(APlace h){
        String Thing = Type;
        place = h;
        System.out.println(Thing + " drive one by one to the " + place.getPlace());
    }

    public void drivep(APlace h){
        String Thing = Type;
        place = h;
        System.out.println(Thing + " drive five at once to the " + place.getPlace());
    }
}