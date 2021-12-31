package dev.ricecx.examplegson;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CheckpointFile implements JsonDeserializer<List<CheckpointFile.CheckpointData>>, JsonSerializer<List<CheckpointFile.CheckpointData>> {

    private final Type checkpointType = new TypeToken<List<CheckpointData>>() {}.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(checkpointType, this).create();
    private final List<CheckpointData> cachedCheckpoints;

    private final File jsonFile;


    public CheckpointFile(File file) {
        this.jsonFile = file;

        if(!jsonFile.exists()) {
            try {
                if(!jsonFile.createNewFile()) throw new IOException("Could not create new file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cachedCheckpoints = readCheckpointData();
    }

    public void removeCheckpoint(Location location) {
        for(CheckpointData checkpoint : cachedCheckpoints) {
            if(checkpoint.isEqual(location)) {
                cachedCheckpoints.remove(checkpoint);
                return;
            }
        }
    }

    public void removeCheckpoint(CheckpointData checkpoint) {
        cachedCheckpoints.remove(checkpoint);
    }

    public void reloadCheckpoints() {
        cachedCheckpoints.clear();
        cachedCheckpoints.addAll(readCheckpointData());
    }

    /**
     * Adds checkpoint to file
     * @param checkpoint checkpoint to add
     */
    public void addCheckpoint(CheckpointData checkpoint) {
        List<CheckpointData> checkpoints = getCachedCheckpoints();
        checkpoints.add(checkpoint);
    }

    /**
     * Saves checkpoints to file
     */
    public void saveCheckpoints() {
        if(!jsonFile.exists()) return;

        try(FileWriter writer = new FileWriter(jsonFile)) {
            gson.toJson(cachedCheckpoints, checkpointType, writer);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private List<CheckpointData> readCheckpointData() {
        List<CheckpointData> checkpointData = new ArrayList<>();

        try(Reader reader = Files.newBufferedReader(jsonFile.toPath())) {

            checkpointData = gson.fromJson(reader, checkpointType);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(checkpointData == null) {
            Bukkit.getLogger().warning("Checkpoints file has nothing in it. Please add some checkpoints.");
            checkpointData = new ArrayList<>();
        }

        return checkpointData;
    }


    public List<CheckpointData> getCachedCheckpoints() {
        return cachedCheckpoints;
    }

    @Override
    public List<CheckpointData> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final List<CheckpointData> data = new ArrayList<>();

        final JsonObject parent = json.getAsJsonObject();

        for (JsonElement checkpoint : parent.getAsJsonArray("checkpoints")) {
            data.add(CheckpointData.fromJSON(checkpoint.getAsJsonObject()));
        }

        return data;
    }

    @Override
    public JsonElement serialize(List<CheckpointData> src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject parent = new JsonObject();
        final JsonArray checkpoints = new JsonArray();

        for (CheckpointData checkpointData : src) {
            checkpoints.add(checkpointData.toJSON());
        }

        parent.add("checkpointsData", checkpoints);

        return parent;
    }

    /**
     * This record represents a checkpoint that can be serialized and deserialized
     */
    protected static record CheckpointData(String worldName, double x, double y, double z) {

        /**
         * Compares a Bukkit {@link Location} to this checkpoint
         * @param location The Bukkit {@link Location} to compare
         * @return true if the Bukkit {@link Location} is equal to this checkpoint
         */
        public boolean isEqual(Location location) {
            Preconditions.checkNotNull(location.getWorld(), "Location must have a world that is not null.");
            return location.getWorld().getName().equals(worldName) && location.getX() == x && location.getY() == y && location.getZ() == z;
        }

        /**
         * Converts a Bukkit {@link Location} to a {@link CheckpointData}
         * @param location The Bukkit {@link Location} to convert
         * @return A {@link CheckpointData} that represents the Bukkit {@link Location}
         */
        public static CheckpointData fromBukkit(Location location) {
            Preconditions.checkNotNull(location.getWorld(), "Location must have a world that is not null.");
            return new CheckpointData(
                    location.getWorld().getName(),
                    location.getX(),
                    location.getY(),
                    location.getZ());
        }


        /**
         * Converts a {@link JsonObject} to a {@link CheckpointData}
         * @param obj The {@link JsonObject} to convert
         * @return A {@link CheckpointData} that represents the {@link JsonObject}
         */
        public static CheckpointData fromJSON(JsonObject obj) {
            return new CheckpointData(
                    obj.get("worldName").getAsString(),
                    obj.get("x").getAsDouble(),
                    obj.get("y").getAsDouble(),
                    obj.get("z").getAsDouble());
        }

        /**
         * Converts a {@link CheckpointData} to a {@link JsonObject}
         * @return A {@link JsonObject} that represents the {@link CheckpointData}
         */
        public JsonObject toJSON() {
            final JsonObject obj = new JsonObject();
            obj.addProperty("worldName", worldName);
            obj.addProperty("x", x);
            obj.addProperty("y", y);
            obj.addProperty("z", z);
            return obj;
        }

        @Override
        public String toString() {
            return "CheckpointData{" +
                    "worldName='" + worldName + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }
}

