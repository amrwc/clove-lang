package dev.amrw.clovelang.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import dev.amrw.clovelang.extension.TimingExtension;
import dev.amrw.clovelang.interpreter.Interpreter;
import dev.amrw.clovelang.tag.IntegrationTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@IntegrationTest
@DisplayName("Snapshot tests")
class SnapshotTest {

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

  @DisplayName("Fast tests")
  @ParameterizedTest(name = "[{index}] path: {0}")
  @CsvSource({
      "functions/function-definition.clove",
      "functions/nested-function-definition.clove",
      "for-loop-classic.clove",
      "for-loop-expression.clove",
      "for-loop-classic2.clove",
      "for-loop-nested.clove",
      "if-else-statement.clove",
      "block-statement.clove",
      "unary-expressions.clove",
  })
  void fastTests(final String filename) throws IOException {
    snapshotTest("integration/" + filename);
  }

  @Nested
  @Timeout(60)
  @IntegrationTest
  @DisplayName("Slow tests")
  @ExtendWith(TimingExtension.class)
  class SlowTests {

    @Test
    @DisplayName("Fibonacci")
    void fibonacci() throws IOException {
      snapshotTest("integration/slow/fibonacci.clove");
    }

    @Test
    @DisplayName("Long loop")
    void longLoop() throws IOException {
      snapshotTest("integration/slow/long-loop.clove");
    }
  }

  private void snapshotTest(final String resourcePath) throws IOException {
    final var result = executeClove(resourcePath);
    final var expectedOutput = getResourceAsString(resourcePath + ".snapshot");
    assertThat(result).isEqualTo(expectedOutput);
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

  private static InputStream gerResourceAsStream(final String path) throws IOException {
    return Resources.getResource(path).openStream();
  }

  private static String getResourceAsString(final String path) throws IOException {
    return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
  }
}
