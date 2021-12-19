package dev.amrw.clovelang.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import dev.amrw.clovelang.interpreter.Interpreter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Snapshot tests")
public interface SnapshotTest {

  default void snapshotTest(final String resourcePath) throws IOException {
    final var result = executeClove(resourcePath);
    final var expectedOutput = getResourceAsString(resourcePath + ".snapshot");
    assertThat(result).isEqualTo(expectedOutput);
  }

  private String executeClove(final String filePath) throws IOException {
    final var stdin = System.in;
    final var stdout = System.out;
    try (
        final var fileStream = gerResourceAsStream(filePath);
        final var outputStream = new ByteArrayOutputStream();
        final var printStream = new PrintStream(outputStream);
    ) {
      System.setIn(fileStream);
      System.setOut(printStream);
      Interpreter.main(new String[]{});
      System.setIn(stdin);
      System.setOut(stdout);
      return outputStream.toString();
    }
  }

  private static InputStream gerResourceAsStream(final String path) throws IOException {
    return Resources.getResource(path).openStream();
  }

  private static String getResourceAsString(final String path) throws IOException {
    return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
  }
}
