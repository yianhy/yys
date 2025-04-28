/**
 * 非全集赏抽卡期望及概率分布计算
 * 模拟抽卡系统，计算抽到新式神的期望抽数及总结概率分布
 */
public class PCRExpectationCalculator {
    // 常量定义
    private static final double SSR_PROBABILITY = 0.0125; // SSR/SP概率：1.25%
    private static final double INITIAL_NEW_SHIKIGAMI_PROBABILITY = 0.04; // 初始新式神概率：4%
    private static final int PITY_THRESHOLD = 60; // 保底阈值：60次
    private static final int NEW_SHIKIGAMI_PITY_THRESHOLD = 800; // 新式神保底阈值：800次
    private static final int SIMULATION_TIMES = 1000000; // 模拟次数
    
    // 新式神概率提升阈值和对应概率
    private static final int[] PROBABILITY_THRESHOLDS = {0, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720, 780};
    private static final double[] NEW_SHIKIGAMI_PROBABILITIES = {0.04, 0.07, 0.11, 0.15, 0.20, 0.25, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90, 1.00}; // 对应概率值
    
    // 模拟结果数组，存储每次模拟抽到新式神所需的抽卡次数
    private static int[] results;

    /**
     * 模拟单次抽卡过程
     * @param currentDraws 当前已抽次数（用于判断保底）
     * @return 是否抽到SSR或SP
     */
    private static boolean drawSSR(int currentDraws) {
        // 如果达到保底次数，必定抽到SSR/SP
        if (currentDraws >= PITY_THRESHOLD) {
            return true;
        }
        
        // 否则按概率抽取
        return Math.random() < SSR_PROBABILITY;
    }
    
    /**
     * 抽中ssr或sp后, 根据总抽卡次数获取当前的新式神概率
     * @param totalDraws 总抽卡次数
     * @return 当前的新式神概率
     */
    private static double getNewShikigamiProbability(int totalDraws) {
        // 根据总抽卡次数确定当前概率
        for (int i = PROBABILITY_THRESHOLDS.length - 1; i >= 0; i--) {
            if (totalDraws > PROBABILITY_THRESHOLDS[i]) {
                return NEW_SHIKIGAMI_PROBABILITIES[i];
            }
        }
        // 默认返回初始概率
        return INITIAL_NEW_SHIKIGAMI_PROBABILITY;
    }
    
    /**
     * 判断抽到的SSR/SP是否为新式神
     * @param totalDraws 总抽卡次数
     * @return 是否为新式神
     */
    private static boolean isNewShikigami(int totalDraws) {
        double probability = getNewShikigamiProbability(totalDraws);
        return Math.random() < probability;
    }
    
    /**
     * 模拟抽卡直到获得新式神，返回抽卡次数
     * @return 抽到新式神所需的抽卡次数
     */
    private static int simulateUntilNewShikigami() {
        int draws = 0; // 抽卡次数
        int drawsSinceLastSSR = 0; // 自上次SSR以来的抽卡次数（用于保底计算）
        
        while (true) {
            draws++;
            drawsSinceLastSSR++;

            //连续799抽都没有抽到新式神，第800抽必定为新式神
            if (draws >= NEW_SHIKIGAMI_PITY_THRESHOLD) {
                return draws;
            }
            
            // 判断是否抽到SSR/SP
            if (drawSSR(drawsSinceLastSSR)) {
                // 抽中ssr或sp, 重置保底计数器
                drawsSinceLastSSR = 0;
                
                // 判断是否为新式神，是的话结束并返回总抽卡次数, 否的话继续抽卡
                if (isNewShikigami(draws)) {
                    return draws;
                }
            }
        }
    }
    
    /**
     * 通过蒙特卡洛模拟计算期望抽数并收集模拟结果
     * @return 期望抽数
     */
    private static double calculateExpectedDrawsAndCollectResults() {
        long totalDraws = 0;
        results = new int[SIMULATION_TIMES];
        
        for (int i = 0; i < SIMULATION_TIMES; i++) {
            results[i] = simulateUntilNewShikigami();
            totalDraws += results[i];
        }
        
        return (double) totalDraws / SIMULATION_TIMES;
    }
    
    /**
     * 计算指定百分比概率能在多少次内抽出新式神
     * @param results 模拟结果数组
     * @param percentile 百分比值（0.0-1.0之间）
     * @return 指定百分比概率抽出所需的抽卡次数
     */
    private static int calculatePercentileDraws(int[] results, double percentile) {
        // 对结果进行排序（注意：这会修改原数组）
        java.util.Arrays.sort(results);
        
        // 计算指定百分比分位数的索引
        int index = (int)(SIMULATION_TIMES * percentile);
        
        // 返回指定百分比分位数的值
        return results[index];
    }
    
    /**
     * 计算每10抽区间的概率分布
     * @param results 模拟结果数组
     * @param maxDraws 最大抽数范围
     * @return 每10抽区间的概率分布数组
     */
    private static double[] calculateProbabilityDistribution(int[] results, int maxDraws) {
        // 计算需要多少个区间（每10抽一个区间）
        int intervals = maxDraws / 10;
        double[] distribution = new double[intervals];
        
        // 统计每个区间的抽数次数
        for (int result : results) {
            if (result <= maxDraws) {
                int interval = (result - 1) / 10; // 0-9抽归为第0区间，10-19抽归为第1区间，以此类推
                distribution[interval]++;
            }
        }
        
        // 转换为概率（累积概率）
        double cumulativeProbability = 0;
        for (int i = 0; i < intervals; i++) {
            distribution[i] = distribution[i] / SIMULATION_TIMES;
            cumulativeProbability += distribution[i];
            distribution[i] = cumulativeProbability;
        }
        
        return distribution;
    }
    

    

    
    public static void main(String[] args) {
        System.out.println("抽卡期望计算程序启动...");
        System.out.println("参数设置：");
        System.out.println("- SSR/SP概率: " + (SSR_PROBABILITY * 100) + "%");
        System.out.println("- 初始新式神概率: " + (INITIAL_NEW_SHIKIGAMI_PROBABILITY * 100) + "%");
        System.out.println("- 新式神概率提升机制: ");
        for (int i = 0; i < PROBABILITY_THRESHOLDS.length; i++) {
            System.out.println("  - " + PROBABILITY_THRESHOLDS[i] + "抽后: " + (NEW_SHIKIGAMI_PROBABILITIES[i] * 100) + "%");
        }
        System.out.println("- 保底阈值: " + PITY_THRESHOLD + "次");
        System.out.println("- 模拟次数: " + SIMULATION_TIMES);
        System.out.println();
        
        System.out.println("开始模拟...");
        long startTime = System.currentTimeMillis();
        
        // 通过模拟计算期望抽数并收集结果
        double expectedDraws = calculateExpectedDrawsAndCollectResults();
        
        // 计算每10抽区间的概率分布
        double[] probabilityDistribution = calculateProbabilityDistribution(results, NEW_SHIKIGAMI_PITY_THRESHOLD);
        
        // 计算50%、75%、90%、99%概率抽出所需的抽卡次数
        int percentile50 = calculatePercentileDraws(results, 0.5);
        int percentile75 = calculatePercentileDraws(results, 0.75);
        int percentile90 = calculatePercentileDraws(results, 0.9);
        int percentile99 = calculatePercentileDraws(results, 0.99);
        

        
        long endTime = System.currentTimeMillis();
        System.out.println("计算完成，耗时: " + (endTime - startTime) / 1000.0 + "秒");
        System.out.println();
        
        System.out.println("结果：");
        System.out.println("- 模拟计算的期望抽数: " + String.format("%.2f", expectedDraws) + "次");

        System.out.println("- 概率分位数：");
        System.out.println("  - 50%概率抽出所需抽数: " + percentile50 + "次");
        System.out.println("  - 75%概率抽出所需抽数: " + percentile75 + "次");
        System.out.println("  - 90%概率抽出所需抽数: " + percentile90 + "次");
        System.out.println("  - 99%概率抽出所需抽数: " + percentile99 + "次");
        System.out.println("- 每10抽区间的累积概率分布：");
        for (int i = 0; i < probabilityDistribution.length; i++) {
            int drawRange = (i + 1) * 10;
            System.out.println("  - " + drawRange + "抽内: " + String.format("%.2f%%", probabilityDistribution[i] * 100));
        }
    }
}
