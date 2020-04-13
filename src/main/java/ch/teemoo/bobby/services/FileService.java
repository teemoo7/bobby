package ch.teemoo.bobby.services;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Move;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileService {
    public void writeGameToFileBasicNotation(Game game, File file) throws IOException {
        Files.write(Paths.get(file.toURI()),
            game.getHistory().stream().map(Move::getBasicNotation).collect(Collectors.toList()));
    }

    public List<String> readFile(File file) throws IOException {
        return Files.readAllLines(Paths.get(file.toURI()));
    }
}
