package ch.jonas.timepilot.service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class JsonListStorage<T> {
    private final Gson gson;
    private final Class<T> elementType;

    public JsonListStorage(Class<T> elementType) {
        this.elementType = elementType;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (dateTime, type, context) -> context.serialize(dateTime.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, context) -> LocalDateTime.parse(json.getAsString()))
                .setPrettyPrinting()
                .create();
    }

    public void saveList(List<T> values, Path filePath) {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(filePath, gson.toJson(values), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save list to " + filePath, exception);
        }
    }

    public List<T> loadList(Path filePath) {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(filePath, StandardCharsets.UTF_8);
            if (json.isBlank()) {
                return new ArrayList<>();
            }

            Type listType = TypeToken.getParameterized(List.class, elementType).getType();
            List<T> values = gson.fromJson(json, listType);
            return values == null ? new ArrayList<>() : new ArrayList<>(values);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load list from " + filePath, exception);
        }
    }
}
