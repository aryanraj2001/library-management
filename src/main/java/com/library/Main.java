package com.library;

import com.library.command.CommandProcessor;
import com.library.service.LibraryService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java -jar library.jar <commands-file>");
            System.exit(1);
        }

        Path inputFile = Path.of(args[0]);
        if (!Files.exists(inputFile)) {
            System.err.println("File not found: " + inputFile);
            System.exit(1);
        }

        LibraryService service = new LibraryService();
        CommandProcessor processor = new CommandProcessor(service);

        List<String> lines = Files.readAllLines(inputFile);
        for (String line : lines) {
            String result = processor.process(line);
            if (result != null) {
                System.out.println(result);
            }
        }
    }
}
