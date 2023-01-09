package goblock.block;

import static goblock.simulator.Simulator.getSimulatedNodes;
import static goblock.simulator.Simulator.getTargetInterval;

import java.math.BigInteger;
import goblock.node.Node;

public class ProofOfWorkBlock extends Block {
  private final BigInteger difficulty;
  private final BigInteger totalDifficulty;
  private final BigInteger nextDifficulty;
  private static BigInteger genesisNextDifficulty;

  public ProofOfWorkBlock(ProofOfWorkBlock parent, Node minter, long time, BigInteger difficulty) {
    super(parent, minter, time);
    this.difficulty = difficulty;

    if (parent == null) {
      this.totalDifficulty = BigInteger.ZERO.add(difficulty);
      this.nextDifficulty = ProofOfWorkBlock.genesisNextDifficulty;
    } else {
      this.totalDifficulty = parent.getTotalDifficulty().add(difficulty);
      this.nextDifficulty = parent.getNextDifficulty();
    }

  }

  public BigInteger getDifficulty() {
    return this.difficulty;
  }

  public BigInteger getTotalDifficulty() {
    return this.totalDifficulty;
  }

  public BigInteger getNextDifficulty() {
    return this.nextDifficulty;
  }

  public static ProofOfWorkBlock genesisBlock(Node minter) {
    long totalMiningPower = 0;
    for (Node node : getSimulatedNodes()) {
      totalMiningPower += node.getMiningPower();
    }
    genesisNextDifficulty = BigInteger.valueOf(totalMiningPower * getTargetInterval());
    return new ProofOfWorkBlock(null, minter, 0, BigInteger.ZERO);
  }
}