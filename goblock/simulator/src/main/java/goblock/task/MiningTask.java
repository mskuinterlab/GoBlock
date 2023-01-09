package goblock.task;

import static goblock.simulator.Timer.getCurrentTime;

import java.math.BigInteger;
import goblock.block.ProofOfWorkBlock;
import goblock.node.Node;

public class MiningTask extends AbstractMintingTask {

  private final BigInteger difficulty;

  public MiningTask(Node minter, long interval, BigInteger difficulty) {
    super(minter, interval);
    this.difficulty = difficulty;
  }

  @Override
  public void run() {
    ProofOfWorkBlock createdBlock = new ProofOfWorkBlock(
        (ProofOfWorkBlock) this.getParent(), this.getMinter(), getCurrentTime(),
        this.difficulty);
    this.getMinter().receiveBlock(createdBlock, null, this.getMinter());
  }
}