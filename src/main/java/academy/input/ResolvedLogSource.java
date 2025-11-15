package academy.input;

import java.io.BufferedReader;
import java.io.IOException;

public record ResolvedLogSource(String displayName, SourceOpener opener) {

    @FunctionalInterface
    public interface SourceOpener {
        BufferedReader open() throws IOException;
    }
}
