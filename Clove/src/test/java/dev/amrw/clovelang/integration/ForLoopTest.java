package dev.amrw.clovelang.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.amrw.clovelang.interpreter.Interpreter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration-test")
public class ForLoopTest {

  @Test
  @DisplayName("Should execute the for-loop Clove program")
  void shouldExecuteForLoopProgram() throws IOException {
    try (
        final var fileStream = getClass().getResource("/integration/test01.clove").openStream();
        final var outputStream = new ByteArrayOutputStream();
        final var printStream = new PrintStream(outputStream);
    ) {
      System.setIn(fileStream);
      System.setOut(printStream);
      final var snapshot = getResourceFileAsString("integration/snapshots/test01.clove.snapshot");
      final var expectedOutput = snapshot + "\n";

      Interpreter.main(new String[]{});

      assertEquals(expectedOutput, outputStream.toString());
    }
  }

  static String getResourceFileAsString(final String filePath) throws IOException {
    final var classLoader = ClassLoader.getSystemClassLoader();
    try (final var inputStream = classLoader.getResourceAsStream(filePath)) {
      if (inputStream == null) {
        return null;
      }
      try (final var inputStreamReader = new InputStreamReader(inputStream);
          final var bufferedReader = new BufferedReader(inputStreamReader)) {
        return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
      }
    }
  }
}
