/**
 * 抽卡期望计算程序（诞生之遇）
 * 模拟抽卡系统，计算抽到新式神的期望抽数
 * 抽卡规则：SSR/SP概率：1.25%，概率为初始概率20%，60抽30%，120抽40%，180抽50%，第200抽必定抽中
 */
public class NewExpectationCalculator {
    // 常量定义
    private static final double SSR_PROBABILITY = 0.0125; // SSR/SP概率：1.25%
    private static final double INITIAL_NEW_SHIKIGAMI_PROBABILITY = 0.20; // 初始新式神概率：20%
    private static final int HARD_PITY_THRESHOLD = 200; // 硬保底阈值：200次
    private static final int SIMULATION_TIMES = 1000000; // 模拟次数
    
    // 新式神概率提升阈值和对应概率
    private static final int[] PROBABILITY_THRESHOLDS = {0, 60, 120, 180};
    private static final double[] NEW_SHIKIGAMI_PROBABILITIES = {0.20, 0.30, 0.40, 0.50}; // 对应概率值

    /**
     * 模拟单次抽卡过程
     * @param currentDraws 当前已抽次数（用于判断保底）
     * @return 是否抽到SSR/SP
     */
    private static boolean drawSSR(int currentDraws) {
        // 没有60抽保底，只按概率抽取
        return Math.random() < SSR_PROBABILITY;
    }
    
    /**
     * 根据总抽卡次数获取当前的新式神概率
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
     * 判断抽到的SSR是否为新式神
     * @param totalDraws 总抽卡次数
     * @return 是否为新式神
     */
    private static boolean isNewShikigami(int totalDraws) {
        // 如果是第200抽，必定是新式神
        if (totalDraws == HARD_PITY_THRESHOLD) {
            return true;
        }
        
        double probability = getNewShikigamiProbability(totalDraws);
        return Math.random() < probability;
    }
    
    /**
     * 模拟抽卡直到获得新式神，返回抽卡次数
     * @return 抽到新式神所需的抽卡次数
     */
    private static int simulateUntilNewShikigami() {
        int draws = 0; // 抽卡次数
        
        while (true) {
            draws++;
            
            // 如果达到硬保底次数，必定抽中新式神
            if (draws == HARD_PITY_THRESHOLD) {
                return draws;
            }
            
            // 判断是否抽到SSR
            if (drawSSR(draws)) {
                // 判断是否为新式神，传入总抽卡次数
                if (isNewShikigami(draws)) {
                    return draws;
                }
            }
        }
    }
    
    /**
     * 通过蒙特卡洛模拟计算期望抽数
     * @return 期望抽数
     */
    private static double calculateExpectedDraws() {
        long totalDraws = 0;
        
        for (int i = 0; i < SIMULATION_TIMES; i++) {
            totalDraws += simulateUntilNewShikigami();
        }
        
        return (double) totalDraws / SIMULATION_TIMES;
    }
    
    /**
     * 计算指定百分比概率能在多少次内抽出新式神
     * @param percentile 百分比值（0.0-1.0之间）
     * @return 指定百分比概率抽出所需的抽卡次数
     */
    private static int calculatePercentileDraws(double percentile) {
        // 收集所有模拟结果
        int[] results = new int[SIMULATION_TIMES];
        
        for (int i = 0; i < SIMULATION_TIMES; i++) {
            results[i] = simulateUntilNewShikigami();
        }
        
        // 对结果进行排序
        java.util.Arrays.sort(results);
        
        // 计算指定百分比分位数的索引
        int index = (int)(SIMULATION_TIMES * percentile);
        
        // 返回指定百分比分位数的值
        return results[index];
    }
    
    /**
     * 计算每10抽区间的概率分布
     * @param maxDraws 最大抽数范围
     * @return 每10抽区间的概率分布数组
     */
    private static double[] calculateProbabilityDistribution(int maxDraws) {
        // 计算需要多少个区间（每10抽一个区间）
        int intervals = maxDraws / 10;
        double[] distribution = new double[intervals];
        
        // 收集所有模拟结果
        int[] results = new int[SIMULATION_TIMES];
        
        for (int i = 0; i < SIMULATION_TIMES; i++) {
            results[i] = simulateUntilNewShikigami();
        }
        
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
        System.out.println("新版抽卡期望计算程序启动...");
        System.out.println("参数设置：");
        System.out.println("- SSR概率: " + (SSR_PROBABILITY * 100) + "%");
        System.out.println("- 初始新式神概率: " + (INITIAL_NEW_SHIKIGAMI_PROBABILITY * 100) + "%");
        System.out.println("- 新式神概率提升机制: ");
        for (int i = 0; i < PROBABILITY_THRESHOLDS.length; i++) {
            System.out.println("  - " + PROBABILITY_THRESHOLDS[i] + "抽后: " + (NEW_SHIKIGAMI_PROBABILITIES[i] * 100) + "%");
        }
        System.out.println("- 硬保底阈值: " + HARD_PITY_THRESHOLD + "次");
        System.out.println("- 模拟次数: " + SIMULATION_TIMES);
        System.out.println();
        
        System.out.println("开始计算...");
        long startTime = System.currentTimeMillis();
        
        // 通过模拟计算期望抽数
        double expectedDraws = calculateExpectedDraws();
        
        // 计算每10抽区间的概率分布
        int maxDraws = 210; // 由于有200抽硬保底，最大值设为210
        double[] probabilityDistribution = calculateProbabilityDistribution(maxDraws);
        
        long endTime = System.currentTimeMillis();
        System.out.println("计算完成，耗时: " + (endTime - startTime) / 1000.0 + "秒");
        System.out.println();
        
        System.out.println("结果：");
        System.out.println("- 模拟计算的期望抽数: " + String.format("%.2f", expectedDraws) + "次");
        System.out.println("- 每10抽区间的累积概率分布：");
        for (int i = 0; i < probabilityDistribution.length; i++) {
            int drawRange = (i + 1) * 10;
            System.out.println("  - " + drawRange + "抽内: " + String.format("%.2f%%", probabilityDistribution[i] * 100));
        }
    }
}
