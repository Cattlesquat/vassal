package VASSAL.counters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class GlobalCommandTargetTest {

  @Test
  public void test() {
    GlobalCommandTarget t = new GlobalCommandTarget();
    String e = t.encode();
    t.decode(e);
    assertThat(t.encode(), is(equalTo(e)));
  }

}