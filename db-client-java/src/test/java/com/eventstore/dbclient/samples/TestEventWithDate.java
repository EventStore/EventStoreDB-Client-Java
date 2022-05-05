package com.eventstore.dbclient.samples;

import java.time.Instant;
import java.util.Objects;

public class TestEventWithDate {
  private Instant instant;

  public TestEventWithDate() {
  }

  public TestEventWithDate(Instant instant) {
    this.instant = instant;
  }

  public Instant getInstant() {
    return instant;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TestEventWithDate testEventWithDate = (TestEventWithDate) o;
    return instant.equals(testEventWithDate.instant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(instant);
  }
}
