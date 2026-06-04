package ch.jonas.timepilot.storage;

import java.lang.reflect.Type;
import java.time.LocalTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    @Override
    public JsonElement serialize(LocalTime source, Type typeOfSource, JsonSerializationContext context) {
        return new JsonPrimitive(source.toString());
    }

    @Override
    public LocalTime deserialize(JsonElement json, Type typeOfTarget, JsonDeserializationContext context)
            throws JsonParseException {
        return LocalTime.parse(json.getAsString());
    }
}
