package goblock.simulator;

import static goblock.settings.SimulationConfiguration.AVERAGE_MINING_POWER;
import static goblock.settings.SimulationConfiguration.INTERVAL;
import static goblock.settings.SimulationConfiguration.STDEV_OF_MINING_POWER;
import static goblock.simulator.Network.printRegion;
import static goblock.simulator.Network.getRegionDistribution;
import static goblock.settings.NetworkConfiguration.DOWNLOAD_BANDWIDTH;
import static goblock.settings.NetworkConfiguration.UPLOAD_BANDWIDTH;
import static goblock.settings.NetworkConfiguration.REGION_LIST;
import static goblock.simulator.Simulator.addNode;
import static goblock.simulator.Simulator.getSimulatedNodes;
import static goblock.simulator.Simulator.printAllPropagation;
import static goblock.simulator.Simulator.setTargetInterval;
import static goblock.simulator.Timer.getCurrentTime;
import static goblock.simulator.Timer.getTask;
import static goblock.simulator.Timer.runTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import goblock.block.Block;
import goblock.node.Node;
import goblock.task.AbstractMintingTask;
import com.google.gson.*;

public class Main {

  public static Random random = new Random(10);

  public static Integer NUM_OF_NODES;

  public static Integer NUM_OF_BLOCKS;

  public static Integer CON_ALG;

  public static Integer NODE_LIST;

  public static String NEIGH_SEL;

  public static long simulationTime = 0;

  public static long[][] matrix;

  public static PrintWriter OUT_JSON_FILE;

  public static PrintWriter STATIC_JSON_FILE;

  static {
    try {
      OUT_JSON_FILE = new PrintWriter(
          new BufferedWriter(new FileWriter(new File("simulator/src/dist/output/output.json"))));
      STATIC_JSON_FILE = new PrintWriter(
          new BufferedWriter(new FileWriter(new File("simulator/src/dist/output/static.json"))));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Scanner myObj = new Scanner(System.in);

    System.out.print("Bu simülasyonda kaç tane düğüm oluşturulacağını girin: ");
    NUM_OF_NODES = myObj.nextInt();

    System.out.print("Bu simülasyonda kaç tane blok kazılacağını girin: ");
    NUM_OF_BLOCKS = myObj.nextInt();

    System.out.print("Düğümlerin nasıl oluşturulacağını girin (1-rastgele, 2-özel, 3-bitnodes): ");
    NODE_LIST = myObj.nextInt();

    if (NODE_LIST!=1) {
    System.out.print(
        "Düğümlerin hangi eş seçim yolu/algoritması ile bağlanacağını girin (1-özel, 2-randompair, 3-nearpair, 4-clusterpair, 5-halfpair, 6-nmfpair, 7-TwoContinentBCBSN, 8-BCBSN): ");
    CON_ALG = myObj.nextInt();
    } else {
      CON_ALG = 2;
    }

    System.out.print("Düğümlerin kaç tane dışarıya giden bağlantıya sahip olabileceğini girin: ");
    Integer NUM_OF_CON = myObj.nextInt();

    System.out.print("Yakın komşu seçimi (proximity neighbor selection) algoritması aktifleştirilsin mi? (E-H): ");
    NEIGH_SEL = myObj.nextLine();

    myObj.nextLine();
    myObj.close();

    final long start = System.currentTimeMillis();
    setTargetInterval(INTERVAL);

    if (NEIGH_SEL.equals("E") || NEIGH_SEL.equals("e")) {

      matrix = new long[NUM_OF_NODES][NUM_OF_NODES];

      for (int i = 0; i < NUM_OF_NODES; i++) {
        for (int j = 0; j < NUM_OF_NODES; j++) {
          if (i == j) {
            continue;
          } else {
            matrix[i][j] = 0;
          }
        }
      }
    }

    OUT_JSON_FILE.print("[");
    OUT_JSON_FILE.flush();

    printRegion();

    constructNetworkWithAllNodes(NUM_OF_NODES, NUM_OF_CON, NODE_LIST);

    int currentBlockHeight = 1;

    while (getTask() != null) {
      if (getTask() instanceof AbstractMintingTask) {
        AbstractMintingTask task = (AbstractMintingTask) getTask();
        if (task.getParent().getHeight() == currentBlockHeight) {
          currentBlockHeight++;
        }
        if (currentBlockHeight > NUM_OF_BLOCKS) {
          break;
        }
        if (currentBlockHeight % 4 == 0 && (NEIGH_SEL.equals("E") || NEIGH_SEL.equals("e"))) {
          for (Node node : getSimulatedNodes()) {
            node.getRoutingTable().reconnectAll(matrix, NUM_OF_NODES);
          }
        }

        if (currentBlockHeight % 100 == 0 || currentBlockHeight == 2) {
          writeGraph(currentBlockHeight);
        }
      }
      runTask();
    }

    printAllPropagation(NUM_OF_NODES);

    System.out.println();

    Set<Block> blocks = new HashSet<>();

    Block block = getSimulatedNodes().get(0).getBlock();

    while (block.getParent() != null) {
      blocks.add(block);
      block = block.getParent();
    }

    ArrayList<Block> blockList = new ArrayList<>(blocks);

    blockList.sort((a, b) -> {
      int order = Long.signum(a.getTime() - b.getTime());
      if (order != 0) {
        return order;
      }
      order = System.identityHashCode(a) - System.identityHashCode(b);
      return order;
    });

    try {
      FileWriter fw = new FileWriter(new File("simulator/src/dist/output/blockList.txt"), false);
      PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

      for (Block b : blockList) {
        pw.println("OnChain : " + b.getHeight() + " : " + b);
      }
      pw.close();

    } catch (IOException ex) {
      ex.printStackTrace();
    }

    OUT_JSON_FILE.print("{");
    OUT_JSON_FILE.print("\"kind\":\"simulation-end\",");
    OUT_JSON_FILE.print("\"content\":{");
    OUT_JSON_FILE.print("\"timestamp\":" + getCurrentTime());
    OUT_JSON_FILE.print("}");
    OUT_JSON_FILE.print("}");
    OUT_JSON_FILE.print("]");
    OUT_JSON_FILE.close();

    long end = System.currentTimeMillis();
    simulationTime += end - start;
    System.out.println(simulationTime);

  }

  public static int genMiningPower() {
    double r = random.nextGaussian();

    return Math.max((int) (r * STDEV_OF_MINING_POWER + AVERAGE_MINING_POWER), 1);
  }

  public static void constructNetworkWithAllNodes(int numNodes, int numCon, int nodeList) {
    int id = 0;
    if (nodeList == 1) {
      double[] regionDistribution = getRegionDistribution();
      List<Integer> regionList = makeRandomListFollowDistribution(regionDistribution, false);

      for (id = 0; id <= numNodes - 1; id++) {

        Node node = new Node(
            id, numCon, Integer.toString(id), regionList.get(id), 0.0, 0.0, REGION_LIST.get(regionList.get(id)),
            genMiningPower(),
            DOWNLOAD_BANDWIDTH[regionList.get(id)], UPLOAD_BANDWIDTH[regionList.get(id)]);

        addNode(node);

        OUT_JSON_FILE.print("{");
        OUT_JSON_FILE.print("\"kind\":\"add-node\",");
        OUT_JSON_FILE.print("\"content\":{");
        OUT_JSON_FILE.print("\"timestamp\":0,");
        OUT_JSON_FILE.print("\"node-id\":" + id + ",");
        OUT_JSON_FILE.print("\"region-id\":" + regionList.get(id));
        OUT_JSON_FILE.print("}");
        OUT_JSON_FILE.print("},");
        OUT_JSON_FILE.flush();
      }
    } else {
      try {
        BufferedReader br = new BufferedReader(new FileReader("simulator/src/dist/input/nodes.json"));
        JsonParser parser = new JsonParser();
        JsonObject jobject = parser.parse(br).getAsJsonObject().getAsJsonObject("nodes");
        Set<Entry<String, JsonElement>> entries = jobject.entrySet();
        for (Map.Entry<String, JsonElement> entry : entries) {
          if (nodeList == 3) {
            if (!(jobject.getAsJsonArray(entry.getKey()).get(10).toString().replaceAll("\"", "").equals("null"))) {
              Integer region_id = 0;
              if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("America")) {
                region_id = 0;
              } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Europe")) {
                region_id = 1;
              } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Asia")) {
                region_id = 2;
              } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Australia")) {
                region_id = 3;
              } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Africa")) {
                region_id = 4;
              } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Atlantic")) {
                region_id = 5;
              } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Pacific")) {
                region_id = 6;
              }

              int miningPower = 0;
              if (jobject.getAsJsonArray(entry.getKey()).size() <= 13) {
                miningPower = genMiningPower();
              } else {
                miningPower = Integer
                    .parseInt(jobject.getAsJsonArray(entry.getKey()).get(13).toString().replaceAll("\"", ""));
              }

              long downloadSpeed = 0;
              if (jobject.getAsJsonArray(entry.getKey()).size() <= 13) {
                downloadSpeed = DOWNLOAD_BANDWIDTH[region_id];
              } else {
                downloadSpeed = Integer
                    .parseInt(jobject.getAsJsonArray(entry.getKey()).get(14).toString().replaceAll("\"", ""));
              }

              long uploadSpeed = 0;
              if (jobject.getAsJsonArray(entry.getKey()).size() <= 13) {
                uploadSpeed = UPLOAD_BANDWIDTH[region_id];
              } else {
                uploadSpeed = Integer
                    .parseInt(jobject.getAsJsonArray(entry.getKey()).get(15).toString().replaceAll("\"", ""));
              }

              String[] location = jobject.getAsJsonArray(entry.getKey()).get(10).toString().replaceAll("\"", "")
                  .split("/");

              Node node = new Node(id, numCon, entry.getKey(), region_id,
                  Double.parseDouble(jobject.getAsJsonArray(entry.getKey()).get(8).toString()),
                  Double.parseDouble(jobject.getAsJsonArray(entry.getKey()).get(9).toString()),
                  location[location.length - 1], miningPower, downloadSpeed, uploadSpeed);

              addNode(node);

              OUT_JSON_FILE.print("{");
              OUT_JSON_FILE.print("\"kind\":\"add-node\",");
              OUT_JSON_FILE.print("\"content\":{");
              OUT_JSON_FILE.print("\"timestamp\":0,");
              OUT_JSON_FILE.print("\"node-id\":" + id + ",");
              OUT_JSON_FILE.print("\"region-id\":" + node.getRegionID());
              OUT_JSON_FILE.print("}");
              OUT_JSON_FILE.print("},");
              OUT_JSON_FILE.flush();

              id++;

              if (id == numNodes - 1) {
                break;
              }
            }
          } else if (nodeList == 2) {
            int miningPower = 0;
            if (jobject.getAsJsonArray(entry.getKey()).get(4).equals(null)) {
              miningPower = genMiningPower();
            } else {
              miningPower = Integer
                  .parseInt(jobject.getAsJsonArray(entry.getKey()).get(4).toString());
            }

            long downloadSpeed = 0;
            if (jobject.getAsJsonArray(entry.getKey()).get(5).equals(null)) {
              downloadSpeed = DOWNLOAD_BANDWIDTH[Integer
                  .parseInt(jobject.getAsJsonArray(entry.getKey()).get(0).toString())];
            } else {
              downloadSpeed = Integer
                  .parseInt(jobject.getAsJsonArray(entry.getKey()).get(5).toString());
            }

            long uploadSpeed = 0;
            if (jobject.getAsJsonArray(entry.getKey()).get(6).equals(null)) {
              uploadSpeed = UPLOAD_BANDWIDTH[Integer
                  .parseInt(jobject.getAsJsonArray(entry.getKey()).get(0).toString())];
            } else {
              uploadSpeed = Integer
                  .parseInt(jobject.getAsJsonArray(entry.getKey()).get(6).toString());
            }

            Node node = new Node(id, numCon, entry.getKey(),
                Integer.parseInt(jobject.getAsJsonArray(entry.getKey()).get(0).toString()),
                Double.parseDouble(jobject.getAsJsonArray(entry.getKey()).get(1).toString()),
                Double.parseDouble(jobject.getAsJsonArray(entry.getKey()).get(2).toString()),
                jobject.getAsJsonArray(entry.getKey()).get(3).toString(), miningPower, downloadSpeed, uploadSpeed);
            addNode(node);

            OUT_JSON_FILE.print("{");
            OUT_JSON_FILE.print("\"kind\":\"add-node\",");
            OUT_JSON_FILE.print("\"content\":{");
            OUT_JSON_FILE.print("\"timestamp\":0,");
            OUT_JSON_FILE.print("\"node-id\":" + id + ",");
            OUT_JSON_FILE.print("\"region-id\":" + node.getRegionID());
            OUT_JSON_FILE.print("}");
            OUT_JSON_FILE.print("},");
            OUT_JSON_FILE.flush();

            id++;

            if (id == numNodes - 1) {
              break;
            }
          }
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }

    if (CON_ALG < 7 & CON_ALG > 0) {
      for (Node node : getSimulatedNodes()) {
        node.joinNetwork(CON_ALG);
      }
    } else if (CON_ALG == 7 || CON_ALG == 8) {
      getSimulatedNodes().get(0).joinNetworkBCBSN(CON_ALG, getSimulatedNodes());
    }

    getSimulatedNodes().get(0).genesisBlock();
  }

  public static ArrayList<Integer> makeRandomListFollowDistribution(double[] distribution, boolean facum) {
    ArrayList<Integer> list = new ArrayList<>();
    int index = 0;

    if (facum) {
      for (; index < distribution.length; index++) {
        while (list.size() <= NUM_OF_NODES * distribution[index]) {
          list.add(index);
        }
      }
      while (list.size() < NUM_OF_NODES) {
        list.add(index);
      }
    } else {
      double acumulative = 0.0;
      for (; index < distribution.length; index++) {
        acumulative += distribution[index];
        while (list.size() <= NUM_OF_NODES * acumulative) {
          list.add(index);
        }
      }
      while (list.size() < NUM_OF_NODES) {
        list.add(index);
      }
    }

    Collections.shuffle(list, random);
    return list;
  }

  public static void writeGraph(int blockHeight) {
    try {
      FileWriter fw = new FileWriter(
          new File("simulator/src/dist/output/graph/" + blockHeight + ".txt"), false);
      PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

      for (int index = 1; index <= getSimulatedNodes().size(); index++) {
        Node node = getSimulatedNodes().get(index - 1);
        for (int i = 0; i < node.getNeighbors().size(); i++) {
          Node neighbor = node.getNeighbors().get(i);
          pw.println(node.getNodeID() + " " + neighbor.getNodeID());
        }
      }
      pw.close();

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}