package dev.amrw.clovelang.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import dev.amrw.clovelang.interpreter.Interpreter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag("integration-test")
@DisplayName("Snapshot tests")
@Execution(ExecutionMode.CONCURRENT)
class SnapshotTest {

  //  private static PrintStream stdout;
//  private PrintStream stdout;
  private static final ThreadLocal<PrintStream> stdout = new ThreadLocal<>();

//  @BeforeAll
//  static void beforeAll() {
//    stdout = System.out;
//  }
//
//  @AfterAll
//  static void afterAll() {
//    // Restore standard output stream
//    System.setOut(stdout);
//  }

//  @BeforeEach
//  void beforeEach() {
//    stdout = System.out;
//  }
//
//  @AfterEach
//  void afterEach() {
//    // Restore standard output stream
//    System.setOut(stdout);
//  }

  @BeforeEach
  void beforeEach() {
    if (stdout.get() != null) {
      System.out.println("stdout ThreadLocal is not empty");
    }
    stdout.set(System.out);
  }

  @AfterEach
  void afterEach() {
    // Restore standard output stream
    System.setOut(stdout.get());
    stdout.remove();
  }

  @DisplayName("Fast tests")
  @ParameterizedTest(name = "[{index}] filename: {0}")
  @CsvSource({
      "test01.clove",
      "test02.clove",
      "test03.clove",
      "test04.clove",
      "test05.clove",
      "test06.clove",
      "test07.clove",
      "test08.clove",
      "test09.clove",
      "test10.clove"
  })
  void fastTests(final String filename) throws IOException {
    snapshotTest("integration/" + filename);
  }

  @Nested
  @Timeout(60)
  @Tag("integration-test")
  @DisplayName("Slow tests")
  class SlowTests {

    @Test
    @DisplayName("Fibonacci")
    void fibonacci() throws IOException {
      snapshotTest("integration/fibonacci.clove");
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
//      System.setOut(stdout);
      System.setOut(stdout.get());
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
