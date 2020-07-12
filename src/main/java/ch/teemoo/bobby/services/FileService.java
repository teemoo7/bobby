package ch.teemoo.bobby.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.moves.Move;

public class FileService {
    public void writeGameToFileBasicNotation(Game game, File file) throws IOException {
        Files.write(Paths.get(file.toURI()),
            game.getHistory().stream().map(Move::getBasicNotation).collect(Collectors.toList()));
    }

    public List<String> readFile(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    public List<String> readFileFromResourceFolder(String folder, String filename) throws IOException {
        try {
            InputStream inputStream = Optional
                .ofNullable(Thread.currentThread().getContextClassLoader().getResourceAsStream(folder + "/" + filename))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
            try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return buffer.lines().collect(Collectors.toList());
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unable to read file " + filename + " in resource folder " + folder, e);
        }
    }
}
