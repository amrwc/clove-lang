package dev.amrw.clovelang.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import dev.amrw.clovelang.interpreter.Interpreter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration-test")
public class SnapshotTest {

  private static final Pattern FILES_PREFIX = Pattern.compile(".*/resources/test/");
  private static PrintStream stdout;

  @BeforeAll
  static void beforeAll() {
    stdout = System.out;
  }

  @AfterAll
  static void afterAll() {
    // Restore standard output stream
    System.setOut(stdout);
  }

  @Test
  @DisplayName("The outputs of the first 10 Clove programs should match their snapshots")
  void testRunsShouldMatchSnapshots_0to10() throws IOException {
    executeBatch(0, 10);
  }

  private void executeBatch(final int startIndex, final int endIndex) throws IOException {
    final var integrationDirPath = getResourcePath("integration");
    final var integrationFiles = new File(integrationDirPath).listFiles();
    final var sortedIntegrationFiles = Stream.of(integrationFiles)
        .sorted(Comparator.comparing(File::getAbsolutePath))
        .toList();

    for (int i = startIndex * 2; i < endIndex * 2; i += 2) {
      final var cloveFilePath = stripLeadingPath(sortedIntegrationFiles.get(i));
      final var result = executeClove(cloveFilePath);
      final var snapshotFilePath = stripLeadingPath(sortedIntegrationFiles.get(i + 1));
      final var expectedOutput = getResourceAsString(snapshotFilePath);
      assertThat(result).isEqualTo(expectedOutput);
    }
  }

  private String stripLeadingPath(final File file) {
    return FILES_PREFIX.matcher(file.getAbsolutePath()).replaceAll("");
  }

  private String executeClove(final String filePath) throws IOException {
    try (
        final var fileStream = gerResourceAsStream(filePath);
        final var outputStream = new ByteArrayOutputStream();
        final var printStream = new PrintStream(outputStream);
    ) {
      System.setIn(fileStream);
      System.setOut(printStream);
      Interpreter.main(new String[]{});
      System.setOut(stdout);
      return outputStream.toString();
    }
  }

  private String getResourcePath(final String path) {
    return Resources.getResource(path).getPath();
  }

  private InputStream gerResourceAsStream(String path) throws IOException {
    return Resources.getResource(path).openStream();
  }

  private static String getResourceAsString(final String path) throws IOException {
    return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
  }
}
