package goblock.node;

import static goblock.settings.SimulationConfiguration.BLOCK_SIZE;
import static goblock.simulator.Main.NUM_OF_NODES;
import static goblock.simulator.Main.OUT_JSON_FILE;
import static goblock.simulator.Main.matrix;
import static goblock.simulator.Main.NEIGH_SEL;
import static goblock.simulator.Network.getBandwidth;
import static goblock.simulator.Simulator.arriveBlock;
import static goblock.simulator.Timer.getCurrentTime;
import static goblock.simulator.Timer.putTask;
import static goblock.simulator.Timer.removeTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import goblock.block.Block;
import goblock.node.consensus.AbstractConsensusAlgo;
import goblock.node.routing.AbstractRoutingTable;
import goblock.task.AbstractMessageTask;
import goblock.task.AbstractMintingTask;
import goblock.task.BlockMessageTask;
import goblock.task.InvMessageTask;
import goblock.task.RecMessageTask;

public class Node {

  private final int nodeID;

  private int region_id;

  private String ip;

  private Double latitude;

  private Double longitude;

  private String location;

  private final long downloadSpeed;

  private final long uploadSpeed;

  private final int miningPower;

  private AbstractRoutingTable routingTable;

  private AbstractConsensusAlgo consensusAlgo;

  private Block block;

  private final Set<Block> orphans = new HashSet<>();

  private AbstractMintingTask mintingTask = null;

  private boolean sendingBlock = false;

  private final ArrayList<AbstractMessageTask> messageQue = new ArrayList<>();

  private final Set<Block> downloadingBlocks = new HashSet<>();

  private final long processingTime = 2;

  public Node(
      int nodeID, int numConnection, String ip, int region_id, Double latitude, Double longitude,
      String location, int miningPower, long downloadSpeed, long uploadSpeed) {
    this.nodeID = nodeID;
    this.region_id = region_id;
    this.ip = ip;
    this.latitude = latitude;
    this.longitude = longitude;
    this.location = location;
    this.miningPower = miningPower;
    this.downloadSpeed = downloadSpeed;
    this.uploadSpeed = uploadSpeed;

    try {
      this.routingTable = (AbstractRoutingTable) Class.forName("goblock.node.routing.BitcoinCoreTable").getConstructor(
          Node.class).newInstance(this);
      this.consensusAlgo = (AbstractConsensusAlgo) Class.forName("goblock.node.consensus.ProofOfWork").getConstructor(
          Node.class).newInstance(this);
      this.setNumConnection(numConnection);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int getNodeID() {
    return this.nodeID;
  }

  public int getRegionID() {
    return this.region_id;
  }

  public void setRegionID(int region_id) {
    this.region_id = region_id;
  }

  public String getIP() {
    return this.ip;
  }

  public Double getLatitude() {
    return this.latitude;
  }

  public Double getLongitude() {
    return this.longitude;
  }

  public String getLocation() {
    return this.location;
  }

  public long getMiningPower() {
    return this.miningPower;
  }

  public AbstractConsensusAlgo getConsensusAlgo() {
    return this.consensusAlgo;
  }

  public AbstractRoutingTable getRoutingTable() {
    return this.routingTable;
  }

  public Block getBlock() {
    return this.block;
  }

  public Set<Block> getOrphans() {
    return this.orphans;
  }

  public int getNumConnection() {
    return this.routingTable.getNumConnection();
  }

  public void setNumConnection(int numConnection) {
    this.routingTable.setNumConnection(numConnection);
  }

  public ArrayList<Node> getNeighbors() {
    return this.routingTable.getNeighbors();
  }

  public long getUploadSpeed() {
    return this.uploadSpeed;
  }

  public long getDownloadSpeed() {
    return this.downloadSpeed;
  }

  public boolean addNeighbor(Node node) {
    return this.routingTable.addNeighbor(node);
  }

  public boolean removeNeighbor(Node node) {
    return this.routingTable.removeNeighbor(node);
  }

  public void joinNetwork(int CON_ALG) {
    this.routingTable.initTable(CON_ALG);
  }

  public void joinNetworkBCBSN(int CON_ALG, ArrayList<Node> nodelist) {
    this.routingTable.initTableBCBSN(CON_ALG, nodelist);
  }

  public void genesisBlock() {
    Block genesis = this.consensusAlgo.genesisBlock();
    this.receiveBlock(genesis, null, this);
  }

  public void addToChain(Block newBlock) {
    if (this.mintingTask != null) {
      removeTask(this.mintingTask);
      this.mintingTask = null;
    }
    this.block = newBlock;
    printAddBlock(newBlock);
    arriveBlock(newBlock, this, NUM_OF_NODES);
  }

  private void printAddBlock(Block newBlock) {
    OUT_JSON_FILE.print("{");
    OUT_JSON_FILE.print("\"kind\":\"add-block\",");
    OUT_JSON_FILE.print("\"content\":{");
    OUT_JSON_FILE.print("\"timestamp\":" + getCurrentTime() + ",");
    OUT_JSON_FILE.print("\"node-id\":" + this.getNodeID() + ",");
    OUT_JSON_FILE.print("\"block-id\":" + newBlock.getId() + ",");
    OUT_JSON_FILE.print("\"network-route\":" + newBlock.getRoute().get(this.getNodeID()));
    OUT_JSON_FILE.print("}");
    OUT_JSON_FILE.print("},");
    OUT_JSON_FILE.flush();
  }

  public void addOrphans(Block orphanBlock, Block validBlock) {
    if (orphanBlock != validBlock) {
      this.orphans.add(orphanBlock);
      this.orphans.remove(validBlock);
      if (validBlock == null || orphanBlock.getHeight() > validBlock.getHeight()) {
        this.addOrphans(orphanBlock.getParent(), validBlock);
      } else if (orphanBlock.getHeight() == validBlock.getHeight()) {
        this.addOrphans(orphanBlock.getParent(), validBlock.getParent());
      } else {
        this.addOrphans(orphanBlock, validBlock.getParent());
      }
    }
  }

  public void minting() {
    AbstractMintingTask task = this.consensusAlgo.minting();
    this.mintingTask = task;
    if (task != null) {
      putTask(task);
    }
  }

  public void sendInv(Block block) {
    for (Node to : this.routingTable.getOutbound()) {
      AbstractMessageTask task = new InvMessageTask(this, to, block);
      putTask(task);
    }
  }

  public void receiveBlock(Block block, Node from, Node to) {

    if (this.consensusAlgo.isReceivedBlockValid(block, this.block)) {
      if (this.block != null && !this.block.isOnSameChainAs(block)) {
        this.addOrphans(this.block, block);
      }

      ArrayList<Integer> nodes = new ArrayList<>();

      if (from != null && !block.getRoute().get(from.nodeID).isEmpty()) {
        for (int j = 0; j < block.getRoute().get(from.nodeID).size(); j++) {
          nodes.add(block.getRoute().get(from.nodeID).get(j));
        }
        block.getRoute().put(to.nodeID, nodes);
        if (!block.getRoute().get(to.nodeID).contains(from.nodeID)) {
          block.getRoute().get(to.nodeID).add(from.nodeID);
        }
      } else {
        nodes.add(to.nodeID);
        block.getRoute().put(to.nodeID, nodes);
      }

      this.addToChain(block);

      this.minting();

      this.sendInv(block);

    } else if (!this.orphans.contains(block) && !block.isOnSameChainAs(this.block)) {
      this.addOrphans(block, this.block);
      arriveBlock(block, this, NUM_OF_NODES);
    }
  }

  public void receiveMessage(AbstractMessageTask message) {
    Node from = message.getFrom();
    Node to = message.getTo();

    if (message instanceof InvMessageTask) {
      Block block = ((InvMessageTask) message).getBlock();
      long time = getCurrentTime() - block.getTime();
      if (from != null && (NEIGH_SEL.equals("E") || NEIGH_SEL.equals("e"))) {
        if (from.nodeID != to.nodeID) {
          if (matrix[from.nodeID][to.nodeID] != 0) {
            matrix[from.nodeID][to.nodeID] = (long) ((1 - 0.3) * matrix[from.nodeID][to.nodeID] + 0.3 * (time));
          } else {
            matrix[from.nodeID][to.nodeID] = time;
          }
        }
      }
      if (!this.orphans.contains(block) && !this.downloadingBlocks.contains(block)) {
        if (this.consensusAlgo.isReceivedBlockValid(block, this.block)) {
          AbstractMessageTask task = new RecMessageTask(this, from, block);
          putTask(task);
          downloadingBlocks.add(block);
        } else if (!block.isOnSameChainAs(this.block)) {
          AbstractMessageTask task = new RecMessageTask(this, from, block);
          putTask(task);
          downloadingBlocks.add(block);
        }
      }
    }

    if (message instanceof RecMessageTask) {
      this.messageQue.add((RecMessageTask) message);
      if (!sendingBlock) {
        this.sendNextBlockMessage();
      }
    }

    if (message instanceof BlockMessageTask) {
      Block block = ((BlockMessageTask) message).getBlock();
      downloadingBlocks.remove(block);
      this.receiveBlock(block, from, this);
    }
  }

  public void sendNextBlockMessage() {
    if (this.messageQue.size() > 0) {
      Node to = this.messageQue.get(0).getFrom();
      long bandwidth = getBandwidth(this, to);

      AbstractMessageTask messageTask;

      if (this.messageQue.get(0) instanceof RecMessageTask) {
        Block block = ((RecMessageTask) this.messageQue.get(0)).getBlock();
        long delay = BLOCK_SIZE * 8 / (bandwidth / 1000) + processingTime;
        messageTask = new BlockMessageTask(this, to, block, delay);
      } else {
        throw new UnsupportedOperationException();
      }

      sendingBlock = true;
      this.messageQue.remove(0);
      putTask(messageTask);
    } else {
      sendingBlock = false;
    }
  }
}