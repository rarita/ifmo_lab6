class Kids extends Human{

    private AThing inHands;
    Kids(String n, APlace p) {
        super(n, p);
    }

    public void Speak(){
        System.out.println(this.getName() + " Speaks with Bublik");
    }

    public void Put(AThing c){
        inHands = c;
        System.out.println(this.getName() + " Put te car under the " +inHands.getType());
    }

    public void Release(){
        System.out.println(this.getName() + " Release the rope");
    }

    public void Build(){
        System.out.println(this.getName() + " build the tends in the street ");
    }

    public void taketoh(APlace h, AThing c ){
        place = h;
        inHands = c;
        System.out.println(this.getName() + " brings " + inHands.getType() + " in to the " + place.getPlace());
    }
}