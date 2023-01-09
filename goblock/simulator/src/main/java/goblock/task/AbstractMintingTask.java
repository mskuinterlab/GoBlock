package goblock.task;

import goblock.block.Block;
import goblock.node.Node;

public abstract class AbstractMintingTask implements Task {

  private final Node minter;

  private final Block parent;

  private final long interval;

  public AbstractMintingTask(Node minter, long interval) {
    this.parent = minter.getBlock();
    this.minter = minter;
    this.interval = interval;
  }

  public Node getMinter() {
    return minter;
  }

  public Block getParent() {
    return parent;
  }

  @Override
  public long getInterval() {
    return this.interval;
  }
}