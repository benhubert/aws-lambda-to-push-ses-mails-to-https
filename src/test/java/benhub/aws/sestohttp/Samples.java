package benhub.aws.sestohttp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Samples {

    public static String open(String name) throws RuntimeException {
        try {
            return new String(Files.readAllBytes(Path.of("src/test/json/" + name + ".json")), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
