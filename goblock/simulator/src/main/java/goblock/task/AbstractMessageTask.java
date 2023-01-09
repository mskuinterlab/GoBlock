package goblock.task;

import static goblock.simulator.Network.getLatency;

import goblock.node.Node;

public abstract class AbstractMessageTask implements Task {

  private final Node from;

  private final Node to;

  public AbstractMessageTask(Node from, Node to) {
    this.from = from;
    this.to = to;
  }

  public Node getFrom() {
    return this.from;
  }

  public Node getTo() {
    return this.to;
  }

  public long getInterval() {
    long latency = getLatency(this.from.getRegionID(), this.to.getRegionID());
    return latency + 10;
  }

  public void run() {
    this.to.receiveMessage(this);
  }
}