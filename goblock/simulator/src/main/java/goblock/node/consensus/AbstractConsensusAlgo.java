package goblock.node.consensus;

import goblock.block.Block;
import goblock.node.Node;
import goblock.task.AbstractMintingTask;

public abstract class AbstractConsensusAlgo {
  private final Node selfNode;

  public AbstractConsensusAlgo(Node selfNode) {
    this.selfNode = selfNode;
  }

  public Node getSelfNode() {
    return this.selfNode;
  }

  public abstract AbstractMintingTask minting();

  public abstract boolean isReceivedBlockValid(Block receivedBlock, Block currentBlock);

  public abstract Block genesisBlock();
}