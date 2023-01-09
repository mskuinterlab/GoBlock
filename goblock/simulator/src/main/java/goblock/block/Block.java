package goblock.block;

import java.util.ArrayList;
import java.util.HashMap;

import goblock.node.Node;

public class Block {

  private final int height;

  private final Block parent;

  private final Node minter;

  private final long time;

  private final int id;

  private static int latestId = 0;

  private HashMap<Integer, ArrayList<Integer>> route;

  public Block(Block parent, Node minter, long time) {
    this.height = parent == null ? 0 : parent.getHeight() + 1;
    this.parent = parent;
    this.minter = minter;
    this.time = time;
    this.id = latestId;
    latestId++;
    this.route = new HashMap<Integer, ArrayList<Integer>>();
  }

  public int getHeight() {
    return this.height;
  }

  public HashMap<Integer, ArrayList<Integer>> getRoute() {
    return this.route;
  }

  public Block getParent() {
    return this.parent;
  }

  public Node getMinter() {
    return this.minter;
  }

  public long getTime() {
    return this.time;
  }

  public int getId() {
    return this.id;
  }

  public static Block genesisBlock(Node minter) {
    return new Block(null, minter, 0);
  }

  public Block getBlockWithHeight(int height) {
    if (this.height == height) {
      return this;
    } else {
      return this.parent.getBlockWithHeight(height);
    }
  }

  public boolean isOnSameChainAs(Block block) {
    if (block == null) {
      return false;
    } else if (this.height <= block.height) {
      return this.equals(block.getBlockWithHeight(this.height));
    } else {
      return this.getBlockWithHeight(block.height).equals(block);
    }
  }
}