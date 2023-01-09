package goblock.node.routing;

import java.util.ArrayList;
import goblock.node.Node;

public abstract class AbstractRoutingTable {
  private final Node selfNode;
  private int numConnection = 8;

  public AbstractRoutingTable(Node selfNode) {
    this.selfNode = selfNode;
  }

  protected Node getSelfNode() {
    return selfNode;
  }

  public void setNumConnection(int numConnection) {
    this.numConnection = numConnection;
  }

  public int getNumConnection() {
    return this.numConnection;
  }

  public abstract void initTable(int algo);

  public abstract void reconnectAll(long[][] matrix, int numNodes);

  public abstract ArrayList<Node> getNeighbors();

  public abstract ArrayList<Node> getInbound();

  public abstract ArrayList<Node> getOutbound();

  public abstract int calculateDistance(Node from, Node to);

  public abstract void removeInbounds(Node node);

  public abstract boolean removeOutbound(Node to);

  public abstract boolean addOutbound(Node to);

  public abstract boolean addNeighbor(Node node);

  public abstract boolean removeNeighbor(Node node);

  public boolean addInbound(Node from) {
    return false;
  }

  public boolean removeInbound(Node from) {
    return false;
  }

  public void acceptBlock() {
  }

  public void initTableBCBSN(int algo, ArrayList<Node> nodelist) {
  }
}