class Adults extends Human{

    private AThing inHands;
    Adults(String n, APlace p, int a) {
        super(n, p, a);
    }

    void Drink(AThing c){
        inHands = c;
        System.out.println(this.getName()+" Drink " +inHands.getType());
    }

    void Smoke(AThing i){
        inHands = i;
        System.out.println(this.getName()+" Smoke his " +inHands.getType());
    }
}