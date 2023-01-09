package goblock.task;

import static goblock.simulator.Main.OUT_JSON_FILE;
import static goblock.simulator.Network.getLatency;
import static goblock.simulator.Timer.getCurrentTime;

import goblock.block.Block;
import goblock.node.Node;

public class BlockMessageTask extends AbstractMessageTask {

  private final Block block;

  private final long interval;

  public BlockMessageTask(Node from, Node to, Block block, long delay) {
    super(from, to);
    this.block = block;
    this.interval = getLatency(this.getFrom().getRegionID(), this.getTo().getRegionID()) + delay;
  }

  @Override
  public long getInterval() {
    return this.interval;
  }

  @Override
  public void run() {

    this.getFrom().sendNextBlockMessage();

    OUT_JSON_FILE.print("{");
    OUT_JSON_FILE.print("\"kind\":\"flow-block\",");
    OUT_JSON_FILE.print("\"content\":{");
    OUT_JSON_FILE.print("\"transmission-timestamp\":" + (getCurrentTime() - this.interval) + ",");
    OUT_JSON_FILE.print("\"reception-timestamp\":" + getCurrentTime() + ",");
    OUT_JSON_FILE.print("\"begin-node-id\":" + getFrom().getNodeID() + ",");
    OUT_JSON_FILE.print("\"end-node-id\":" + getTo().getNodeID() + ",");
    OUT_JSON_FILE.print("\"block-id\":" + block.getId());
    OUT_JSON_FILE.print("}");
    OUT_JSON_FILE.print("},");
    OUT_JSON_FILE.flush();

    super.run();
  }

  public Block getBlock() {
    return this.block;
  }
}