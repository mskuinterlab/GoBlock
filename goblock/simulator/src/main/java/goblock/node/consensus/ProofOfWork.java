package goblock.node.consensus;

import static goblock.simulator.Main.random;

import java.math.BigInteger;
import goblock.block.Block;
import goblock.block.ProofOfWorkBlock;
import goblock.node.Node;
import goblock.task.MiningTask;

public class ProofOfWork extends AbstractConsensusAlgo {

  public ProofOfWork(Node selfNode) {
    super(selfNode);
  }

  @Override
  public MiningTask minting() {
    Node selfNode = this.getSelfNode();
    ProofOfWorkBlock parent = (ProofOfWorkBlock) selfNode.getBlock();
    BigInteger difficulty = parent.getNextDifficulty();
    double p = 1.0 / difficulty.doubleValue();
    double u = random.nextDouble();
    return p <= Math.pow(2, -53) ? null
        : new MiningTask(selfNode, (long) (Math.log(u) / Math.log(
            1.0 - p) / selfNode.getMiningPower()), difficulty);
  }

  @Override
  public boolean isReceivedBlockValid(Block receivedBlock, Block currentBlock) {
    if (!(receivedBlock instanceof ProofOfWorkBlock)) {
      return false;
    }
    ProofOfWorkBlock recPoWBlock = (ProofOfWorkBlock) receivedBlock;
    ProofOfWorkBlock currPoWBlock = (ProofOfWorkBlock) currentBlock;
    int receivedBlockHeight = receivedBlock.getHeight();
    ProofOfWorkBlock receivedBlockParent = receivedBlockHeight == 0 ? null
        : (ProofOfWorkBlock) receivedBlock.getBlockWithHeight(receivedBlockHeight - 1);

    return (receivedBlockHeight == 0 ||
        recPoWBlock.getDifficulty().compareTo(receivedBlockParent.getNextDifficulty()) >= 0)
        && (currentBlock == null ||
            recPoWBlock.getTotalDifficulty().compareTo(currPoWBlock.getTotalDifficulty()) > 0);
  }

  @Override
  public ProofOfWorkBlock genesisBlock() {
    return ProofOfWorkBlock.genesisBlock(this.getSelfNode());
  }
}