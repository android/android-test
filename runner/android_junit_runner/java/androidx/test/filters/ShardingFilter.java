package androidx.test.filters;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * A JUnit sharding filter uses the hashcode of the test description to assign it to a shard.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public class ShardingFilter extends Filter {
  private final int numShards;
  private final int shardIndex;

  public ShardingFilter(int numShards, int shardIndex) {
    this.numShards = numShards;
    this.shardIndex = shardIndex;
  }

  @Override
  public boolean shouldRun(Description description) {
    if (description.isTest()) {
      return (Math.floorMod(description.hashCode(), numShards)) == shardIndex;
    }

    // The description is a suite, so assume that it can be run so that filtering is
    // applied to its children. If after filtering it has no children then it will be
    // automatically filtered out.
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public String describe() {
    return String.format("Shard %d of %d shards", shardIndex, numShards);
  }
}
