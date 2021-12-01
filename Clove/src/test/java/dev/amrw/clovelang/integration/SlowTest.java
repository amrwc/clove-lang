package dev.amrw.clovelang.integration;

import dev.amrw.clovelang.extension.TimingExtension;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

@Timeout(60)
@ExtendWith(TimingExtension.class)
class SlowTest implements SnapshotTest {

  @Test
  @DisplayName("Slow tests > Fibonacci")
  void fibonacci() throws IOException {
    snapshotTest("integration/slow/fibonacci.clove");
  }

  @Test
  @DisplayName("Slow tests > Long loop")
  void longLoop() throws IOException {
    snapshotTest("integration/slow/long-loop.clove");
  }
}
