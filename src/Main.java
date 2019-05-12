public class Main {

    public static void main(String[] args) {

        System.out.println();

        Place house = new Place("House");
        Place street = new Place("Street");
        Place tend = new Place("Tend");

        Thing apples = new Thing("apples");
        Thing pears = new Thing("Pears");
        Thing plums = new Thing("Plums");
        Thing water = new Thing("Sparkling water with syrup");
        Thing cookies = new Thing("cakes, all kinds of biscuits, pretzels and sweets");
        Thing rope = new Thing("Rope");
        Thing car = new Thing("Car");

        Kids Vitnik = new Kids("Vitnik",street);
        Kids Bublik = new Kids ("Bublik",street);
        Kids Childrens = new Kids ("Childrens",street);

        Vitnik.Speak();
        Bublik.Put(apples);
        Childrens.Release();
        apples.Fall();
        rope.Untied();
        car.taketo(house);

        System.out.println("Mechanization greatly facilitated the work");

        apples.drives(house);
        pears.drives(house);
        plums.drivep(house);
        Childrens.Build();
        Childrens.taketoh(tend, water);
        Childrens.taketoh(tend, cookies);
    }
}