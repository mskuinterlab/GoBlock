package goblock.simulator;

import static goblock.settings.NetworkConfiguration.LATENCY;
import static goblock.settings.NetworkConfiguration.REGION_LIST;
import static goblock.settings.NetworkConfiguration.REGION_DISTRIBUTION;
import static goblock.simulator.Main.STATIC_JSON_FILE;
import static goblock.simulator.Main.random;

import java.util.List;

import goblock.node.Node;

public class Network {

  public static final long getLatency(int from, int to) {
    long mean = LATENCY[from][to];
    double shape = 0.2 * mean;
    double scale = mean - 5;
    return Math.round(scale / Math.pow(random.nextDouble(), 1.0 / shape));
  }

  public static double[] getRegionDistribution() {
    return REGION_DISTRIBUTION;
  }

  public static final long getBandwidth(Node from, Node to) {
    return Math.min(from.getUploadSpeed(), to.getDownloadSpeed());
  }

  public static List<String> getRegionList() {
    return REGION_LIST;
  }

  public static void printRegion() {
    STATIC_JSON_FILE.print("{\"region\":[");

    int id = 0;
    for (; id < REGION_LIST.size() - 1; id++) {
      STATIC_JSON_FILE.print("{");
      STATIC_JSON_FILE.print("\"id\":" + id + ",");
      STATIC_JSON_FILE.print("\"name\":\"" + REGION_LIST.get(id) + "\"");
      STATIC_JSON_FILE.print("},");
    }

    STATIC_JSON_FILE.print("{");
    STATIC_JSON_FILE.print("\"id\":" + id + ",");
    STATIC_JSON_FILE.print("\"name\":\"" + REGION_LIST.get(id) + "\"");
    STATIC_JSON_FILE.print("}");
    STATIC_JSON_FILE.print("]}");
    STATIC_JSON_FILE.flush();
    STATIC_JSON_FILE.close();
  }
}