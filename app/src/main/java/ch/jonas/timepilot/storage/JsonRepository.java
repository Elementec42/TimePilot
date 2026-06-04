package ch.jonas.timepilot.storage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class JsonRepository<T> {
    private final Path filePath;
    private final Gson gson;
    private final Class<T> dataType;

    public JsonRepository(Path filePath, Gson gson, Class<T> dataType) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
        this.dataType = Objects.requireNonNull(dataType, "dataType must not be null");
    }

    public Optional<T> load() {
        if (Files.notExists(filePath)) {
            return Optional.empty();
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(gson.fromJson(reader, dataType));
        } catch (IOException | JsonParseException exception) {
            throw new StorageException("Could not load JSON data from " + filePath, exception);
        }
    }

    public void save(T data) {
        Objects.requireNonNull(data, "data must not be null");

        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }
        } catch (IOException exception) {
            throw new StorageException("Could not save JSON data to " + filePath, exception);
        }
    }
}
