package goblock.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkConfiguration {
    public static final List<String> REGION_LIST = new ArrayList<>(
            Arrays.asList("America", "Europe", "Asia", "Australia", "Africa",
                    "Atlantic", "Pacific"));

    public static final long[][] LATENCY = {
            { 102, 146, 291, 260, 285, 200, 313 },
            { 146, 39, 178, 283, 206, 63, 320 },
            { 291, 178, 191, 252, 307, 211, 329 },
            { 260, 283, 252, 45, 472, 323, 68 },
            { 285, 206, 307, 472, 125, 175, 453 },
            { 200, 63, 211, 323, 175, 132, 300 },
            { 313, 320, 329, 68, 453, 300, 259 }
    };

    public static final long[] DOWNLOAD_BANDWIDTH = {
            91717209, 83048043, 82312500, 52188000,
            20602500, 63973333, 174985000
    };

    public static final long[] UPLOAD_BANDWIDTH = {
            28977209, 48203260, 58036944, 18060000,
            15537500, 57480000, 57875000
    };

    public static final double[] REGION_DISTRIBUTION = {
            0.3269, 0.5469, 0.0973,
            0.0185, 0.0050, 0.0020, 0.0034
    };
}