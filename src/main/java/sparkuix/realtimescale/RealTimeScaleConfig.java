package sparkuix.realtimescale;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 该类用于读写 RealTimeScale.toml 文件。
 * 文件内保存了默认缩放数值（固定参数）以及若干自定义参数标题对应的缩放值。
 * 当写入时，如果标题已存在则更新数值，如果不存在则新增记录。
 * 读取时若找不到任何对应标题，则返回默认缩放数值。
 *
 * Toml 简化约定示例：
 * RealTimeScale.toml 文件示例：
 *
 * [RealTimeScale]
 * "DefaultScale" = 1.00
 * "TitleA" = 2.50
 * "TitleB" = 0.75
 *
 * 写入和读取方法均使用上述约定格式，以“"标题" = 值”的形式存储在 [RealTimeScale] 节点下。
 */
public class RealTimeScaleConfig {

    // Forge 环境下默认的配置目标文件路径
    private static final Path CON_PATH = FMLPaths.CONFIGDIR.get();

    // 文件名
    private static final String FILE_NAME = "RealTimeScale.toml";

    // Toml 中用于区分的节名（节点），这里统一使用 [RealTimeScale]
    private static final String SECTION_HEADER = "[RealTimeScale]";

    // Toml 中默认缩放参数键
    private static final String DEFAULT_SCALE_KEY = "\"DefaultScale\"";

    // 保留两位小数的格式
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    /**
     * 读取 Toml 文件中的默认缩放数值。
     * 如果文件不存在，或没有对应记录，返回一个默认值（可自行指定）。
     */
    public double readDoubleDefaultScale() {
        Path filePath = CON_PATH.resolve(FILE_NAME);
        // 如果文件不存在，按照需求可直接返回一个默认数值，示例中设定为 4.00
        if (!Files.exists(filePath)) {
            return 4.00;
        }

        boolean inSection = false;
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 检测到 [RealTimeScale] 时，进入对应节
                if (line.equalsIgnoreCase(SECTION_HEADER)) {
                    inSection = true;
                    continue;
                }
                // 超出此 section 时，退出
                if (inSection && line.startsWith("[") && line.endsWith("]")) {
                    break;
                }
                // 在节内查找 "\"DefaultScale\" = xxxx" 格式
                if (inSection && line.startsWith(DEFAULT_SCALE_KEY + " =")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        try {
                            return Double.parseDouble(parts[1].trim());
                        } catch (NumberFormatException e) {
                            // 解析失败则返回默认值
                            return 4.00;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 若未找到，返回一个默认数值（此处为 1.00）
        return 4.00;
    }

    /**
     * 写入（或更新）默认缩放数值。成功返回 true，失败返回 false。
     */
    public boolean writeDoubleDefaultScale(double newVal) {
        // 读取现有文件所有条目，然后更新默认缩放参数后再写出
        Map<String, Double> dataMap = readAllScaleValues();
        // 更新默认缩放数值
        dataMap.put(DEFAULT_SCALE_KEY, newVal);
        // 将更新后的数据重新写回文件
        return writeAllScaleValues(dataMap);
    }

    /**
     * 读取自定义标题对应的缩放数值。
     * 如果没有找到对应标题，则返回默认缩放数值。
     */
    public double readCustomScale(String title) {
        // 先获取默认缩放值
        double defaultScale = readDoubleDefaultScale();

        // 读取所有记录
        Map<String, Double> dataMap = readAllScaleValues();
        // 如果标题存在，则返回值，否则返回默认缩放
        String quotedTitle = "\"" + title + "\"";
        return dataMap.getOrDefault(quotedTitle, defaultScale);
    }

    /**
     * 写入或更新自定义标题对应的缩放数值。成功返回 true，失败返回 false。
     * @param title   标题
     * @param newVal  新的缩放值（保留两位小数）
     */
    public boolean writeCustomScale(String title, double newVal) {
        // 读取所有条目
        Map<String, Double> dataMap = readAllScaleValues();

        // 写入或更新自定义标题
        String quotedTitle = "\"" + title + "\"";
        dataMap.put(quotedTitle, newVal);

        // 写入文件
        return writeAllScaleValues(dataMap);
    }

    /**
     * 读取文件内所有缩放值，包括 DefaultScale 和自定义标题。
     * @return Map<String, Double> 其中 key 为标题或 DefaultScale，value 为其对应的双精度数值
     */
    private Map<String, Double> readAllScaleValues() {
        Map<String, Double> dataMap = new LinkedHashMap<>();
        Path filePath = CON_PATH.resolve(FILE_NAME);

        // 如果文件不存在，则在 map 中仅保留默认缩放值
        if (!Files.exists(filePath)) {
            dataMap.put(DEFAULT_SCALE_KEY, 1.00);
            return dataMap;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            boolean inSection = false;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 找到 [RealTimeScale] 节，进入该节后开始处理数据
                if (line.equalsIgnoreCase(SECTION_HEADER)) {
                    inSection = true;
                    continue;
                }
                // 如果再次遇到新的 [xxx]，说明已离开 [RealTimeScale] 节
                if (inSection && line.startsWith("[") && line.endsWith("]")) {
                    break;
                }
                // 处理节内的 "\"Key\" = Value" 行
                if (inSection && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim().replaceAll("\"", "");
                        String valStr = parts[1].trim();
                        try {
                            double val = Double.parseDouble(valStr);
                            dataMap.put("\"" + key + "\"", val);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 如果 map 中没有默认缩放值，手动添加一个
        dataMap.putIfAbsent(DEFAULT_SCALE_KEY, 1.00);

        return dataMap;
    }

    /**
     * 将所有缩放值写回 Toml 文件。
     * @param dataMap 所有键值对，包括 DEFAULT_SCALE_KEY 和其他标题
     * @return 是否写入成功（true/false）
     */
    private boolean writeAllScaleValues(Map<String, Double> dataMap) {
        Path filePath = CON_PATH.resolve(FILE_NAME);

        // 这里直接覆盖写回
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            // 写入节头
            writer.write(SECTION_HEADER);
            writer.newLine();
            writer.newLine();

            // 依次写入 key = value
            // 在 Toml 中，数值部分尽量保留两位小数
            for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
                String key = entry.getKey();
                double value = entry.getValue();
                String formattedValue = DECIMAL_FORMAT.format(value);
                writer.write(String.format("%s = %s", key, formattedValue));
                writer.newLine();
            }
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}