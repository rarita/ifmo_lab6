abstract class APlace {

    private String PlaceS;
    private float latitude;
    private float longitude;

    public APlace() {

    }

    public APlace(String placeS, float latitude, float longitude) {
        PlaceS = placeS;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlace() {
        return PlaceS;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }
}