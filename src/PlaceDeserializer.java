import com.google.gson.*;

import java.lang.reflect.Type;

public class PlaceDeserializer implements JsonDeserializer<APlace> {

    @Override
    public APlace deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return jsonDeserializationContext.deserialize(jsonElement, Place.class);
    }
}
