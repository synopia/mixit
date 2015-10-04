package org.synopia.audio.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by synopia on 20.09.2015.
 */
public class TrackDatabase {
    private Executor executor = Executors.newFixedThreadPool(1);

    private ClipLoader clipLoader = new ClipLoader();
    private TrackAnalyzer trackAnalyzer = new TrackAnalyzer();

    private Map<String, Track> tracks = new HashMap<>();
    private Map<Track, Clip> clips = Collections.synchronizedMap(new HashMap<>());
    private final Gson gson;

    public TrackDatabase() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(new TypeToken<List<Marker>>() {
        }.getType(), (JsonSerializer<List<Marker>>) (src, typeOfSrc, context) -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            try {
                for (Marker marker : src) {
                    dos.writeFloat((float) marker.time);
                    dos.writeFloat((float) marker.salience);
                    dos.writeByte(marker.type.ordinal());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new JsonPrimitive(Base64.getMimeEncoder().encodeToString(out.toByteArray()));
        });
        gsonBuilder.registerTypeAdapter(new TypeToken<List<Marker>>() {
        }.getType(), (JsonDeserializer<List<Marker>>) (json, typeOfT, context) -> {
            byte[] data = Base64.getMimeDecoder().decode(json.getAsString());
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            List<Marker> list = new ArrayList<>();
            try {
                while (dis.available() > 0) {
                    Marker marker = new Marker();
                    marker.time = dis.readFloat();
                    marker.salience = dis.readFloat();
                    marker.type = Marker.Type.values()[dis.readByte()];
                    list.add(marker);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return list;
        });
        gson = gsonBuilder.setPrettyPrinting().create();
    }

    public List<ATrack> all() {
        return new ArrayList<>(tracks.values());
    }

    public Track get(String filename) {
        Track track = tracks.get(filename);
        if (track == null) {
            track = new Track(filename);
            clipLoader.open(track);
            tracks.put(filename, track);
        }
        return track;
    }

    public CompletableFuture<Clip> load(Track track) {
        CompletableFuture<Clip> future = new CompletableFuture<>();
        if (clips.containsKey(track)) {
            future.complete(clips.get(track));
        } else {
            executor.execute(() -> {
                System.out.println("Loading " + track.getFilename());
                Clip clip = clipLoader.load(track);
                clips.put(track, clip);
                future.complete(clip);
            });
        }
        return future;
    }

    public CompletableFuture<Track> analyze(Track track) {
        CompletableFuture<Track> future = new CompletableFuture<>();
        executor.execute(() -> {
            System.out.println("Analyzing " + track.getFilename());
            load(track).whenComplete((clip, e) -> {
                trackAnalyzer.analyze(track, clip);
                System.out.println(" " + track);
                future.complete(track);
            });
        });
        return future;
    }

    public void scan(String dir) {
        Path libraryPath = FileSystems.getDefault().getPath(dir);
        try {
            Files.walkFileTree(libraryPath, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String filename = file.toString();
                    if (filename.endsWith("mp3")) {
                        Track track = get(filename);
                        if (track.getCalculatedKey() == null) {
                            analyze(track).whenComplete((t, e) -> {
                                System.out.println(track);
                                save();
                                clips.remove(track);
                            });
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            FileWriter writer = new FileWriter("db_full.json");
            gson.toJson(tracks, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            FileReader reader = new FileReader("db_full.json");
            tracks = gson.fromJson(reader, new TypeToken<Map<String, Track>>() {
            }.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        TrackDatabase trackDatabase = new TrackDatabase();
        trackDatabase.load();
        trackDatabase.scan("e:/case/lib3");
        trackDatabase.save();
    }
}
