package academy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApplicationArgsSanitizerTest {

    @Test
    @DisplayName("Sanitizer removes leading descriptive arguments before options")
    void removesLeadingNoise() {
        String[] sanitized = Application.sanitizeArgs(new String[] {
            "tive",
            "input file does not exist",
            "2",
            "-p",
            "/tmp/data/input/file.txt",
            "-f",
            "json",
            "-o",
            "/tmp/data/output/result.json"
        });

        assertThat(sanitized)
                .containsExactly("-p", "/tmp/data/input/file.txt", "-f", "json", "-o", "/tmp/data/output/result.json");
    }

    @Test
    @DisplayName("Sanitizer preserves already valid argument list")
    void keepsValidArgs() {
        String[] args = {"-p", "/tmp/data/input/file.txt", "-f", "json", "-o", "/tmp/data/output/result.json"};

        assertThat(Application.sanitizeArgs(args)).containsExactly(args);
    }

    @Test
    @DisplayName("Sanitizer trims whitespace and keeps argument values")
    void trimsWhitespace() {
        String[] sanitized = Application.sanitizeArgs(new String[] {
            "  noise  ",
            "  ",
            "-p",
            " /tmp/data/input/file.txt ",
            "-f",
            " json ",
            "-o",
            " /tmp/data/output/result.json "
        });

        assertThat(sanitized)
                .containsExactly("-p", "/tmp/data/input/file.txt", "-f", "json", "-o", "/tmp/data/output/result.json");
    }
}
