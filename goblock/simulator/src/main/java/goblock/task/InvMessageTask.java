package goblock.task;

import goblock.block.Block;
import goblock.node.Node;

public class InvMessageTask extends AbstractMessageTask {

  private final Block block;

  public InvMessageTask(Node from, Node to, Block block) {
    super(from, to);
    this.block = block;
  }

  public Block getBlock() {
    return this.block;
  }
}