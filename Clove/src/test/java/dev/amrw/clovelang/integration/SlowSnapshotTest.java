package dev.amrw.clovelang.integration;

import dev.amrw.clovelang.extension.TimingExtension;
import dev.amrw.clovelang.namegenerator.SlowTestDisplayNameGenerator;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * These Clove programs weren't slow in the past. At some point of the development, they've become
 * slow. I think it was around the time when type casting shenanigans have been introduced. Revert
 * the design decision regarding type casting, and keep Java's defaults. These tests should speed
 * up over time, and be moved to the {@link FastSnapshotTest} class.
 * <p>
 * E.g.: when doing operations between two <code>floats</code>, don't force the result to also be
 * <code>float</code>, use <code>double</code>.
 */
@Timeout(60)
@ExtendWith(TimingExtension.class)
@DisplayNameGeneration(SlowTestDisplayNameGenerator.class)
class SlowSnapshotTest implements SnapshotTest {

  @DisplayName("Slow tests")
  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource({
      "fibonacci.clove",
      "long-loop.clove",
  })
  void slowTests(final String filePath) throws IOException {
    snapshotTest("integration/slow/" + filePath);
  }
}
