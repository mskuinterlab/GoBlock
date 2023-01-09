package goblock.simulator;

import static goblock.simulator.Timer.getCurrentTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import goblock.block.Block;
import goblock.node.Node;

public class Simulator {

  private static final ArrayList<Node> simulatedNodes = new ArrayList<>();

  private static long targetInterval;

  public static ArrayList<Node> getSimulatedNodes() {
    return simulatedNodes;
  }

  public static long getTargetInterval() {
    return targetInterval;
  }

  public static void setTargetInterval(long interval) {
    targetInterval = interval;
  }

  public static void addNode(Node node) {
    simulatedNodes.add(node);
  }

  public static void removeNode(Node node) {
    simulatedNodes.remove(node);
  }

  public static void addNodeWithConnection(Node node, int CON_ALG) {
    node.joinNetwork(CON_ALG);
    addNode(node);
    for (Node existingNode : simulatedNodes) {
      existingNode.addNeighbor(node);
    }
  }

  private static final ArrayList<Block> observedBlocks = new ArrayList<>();

  private static final ArrayList<LinkedHashMap<Integer, Long>> observedPropagations = new ArrayList<>();

  public static void arriveBlock(Block block, Node node, int numNodes) {

    if (observedBlocks.contains(block)) {
      LinkedHashMap<Integer, Long> propagation = observedPropagations.get(
          observedBlocks.indexOf(block));

      propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
    } else {
      if (observedBlocks.size() > 10) {
        printPropagation(observedBlocks.get(0), observedPropagations.get(0), numNodes);
        observedBlocks.remove(0);
        observedPropagations.remove(0);
      }
      LinkedHashMap<Integer, Long> propagation = new LinkedHashMap<>();
      propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
      observedBlocks.add(block);
      observedPropagations.add(propagation);
    }
  }

  public static void printPropagation(Block block, LinkedHashMap<Integer, Long> propagation, int numNodes) {
    System.out.println(block + ":" + block.getHeight());
    System.out.println("Network routes for every node:");
    for (int i = 0; i < numNodes + 1; i++) {
      if (block.getRoute().get(i) != null) {
        System.out.println("Network route for node ID " + i + ":");
        for (int j = 0; j < block.getRoute().get(i).size(); j++) {
          System.out.print(block.getRoute().get(i).get(j));
          if (j + 1 < block.getRoute().get(i).size()) {
            System.out.print(", ");
          }
        }
        System.out.println("");
        System.out.println("");
      }
    }

    System.out.println("");

    for (Map.Entry<Integer, Long> timeEntry : propagation.entrySet()) {
      System.out.println(timeEntry.getKey() + "," + timeEntry.getValue());
    }
    System.out.println();
  }

  public static void printAllPropagation(int numNodes) {
    for (int i = 0; i < observedBlocks.size(); i++) {
      printPropagation(observedBlocks.get(i), observedPropagations.get(i), numNodes);
    }
  }
}