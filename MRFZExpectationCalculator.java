import java.util.stream.IntStream;

public class MRFZExpectationCalculator {

    /**
     * 在限定池出限定的抽卡期望及概率分布计算
     * 模拟抽卡系统，计算抽到新式神的期望抽数及总结概率分布
     */

    // 常量定义
    private static final double PROBABILITY = 0.02; // 六星初始概率：2%
    private static final int PITY_THRESHOLD = 300; // 井：300次
    private static final int SIMULATION_TIMES = 10000000; // 模拟次数
    //新限定干员在限定池里的概率为35%
    private static final double NEW_Operators_PROBABILITIES = 0.35;


    // 模拟结果数组，存储每次模拟抽到新式神所需的抽卡次数
    private static int[] results;

    /**
     * 模拟单次抽卡过程
     * @param Draws 距离上次出六星干员的次数（用于判断保底）
     * @return 是否抽到六星干员
     */
    private static boolean drawOperators(int Draws) {
        // 否则按概率抽取
        return Math.random() < getOperatorsProbability(Draws);
    }

    /**
     * 根据抽卡次数获取当前的六星干员概率
     * @param Draws 抽卡次数
     * @return 当前的六星干员概率
     */
    private static double getOperatorsProbability(int Draws) {
        // 根据总抽卡次数确定当前概率
        if(Draws<50){
            return PROBABILITY;
        }else return PROBABILITY+((Draws-50)*2)*0.01;
    }

    /**
     * 模拟抽卡直到获得新式神，返回抽卡次数
     * @return 抽到新式神所需的抽卡次数
     */
    private static int simulateUntilNewOperators() {
        int draws = 0; // 抽卡次数
        int drawsSinceLastOperators = 0; // 自上次六星干员以来的抽卡次数（用于保底计算）

        while (true) {
            draws++;
            drawsSinceLastOperators++;

            // 连续299抽都没有抽到限定干员，第300抽井
            if (draws >= PITY_THRESHOLD) {
                return draws;
            }

            // 判断是否抽到六星干员
            if (drawOperators(drawsSinceLastOperators)) {
                // 重置保底计数器
                drawsSinceLastOperators = 0;

                // 判断是否为限定干员，是的话结束并返回总抽卡次数, 否的话继续抽卡
                if (Math.random() <= NEW_Operators_PROBABILITIES) {
                    return draws;
                }
            }
        }
    }

    /**
     * 通过蒙特卡洛模拟计算期望抽数
     * @return 期望抽数
     */
    private static double calculateExpectedDrawsAndCollectResults() {
        long totalDraws = 0L;
        results = new int[SIMULATION_TIMES];

        for (int i = 0; i < SIMULATION_TIMES; i++) {
            results[i] = simulateUntilNewOperators();
            totalDraws += results[i];
        }

        return (double) totalDraws / SIMULATION_TIMES;
    }

    /**
     * 计算指定百分比概率能在多少次内抽出限定干员
     * @param percentile 百分比值（0.0-1.0之间）
     * @return 指定百分比概率抽出所需的抽卡次数
     */
    private static int calculatePercentileDraws(double percentile) {
        // 对结果进行排序（注意：这会修改原数组）
        java.util.Arrays.sort(results);

        // 计算指定百分比分位数的索引
        int index = (int)(SIMULATION_TIMES * percentile);

        // 返回指定百分比分位数的值
        return results[index];
    }

    /**
     * 计算每10抽区间的概率分布
     * @return 每10抽区间的概率分布数组
     */
    private static double[] calculateProbabilityDistribution() {
        // 计算需要多少个区间（每10抽一个区间）
        int intervals = PITY_THRESHOLD / 10;
        double[] distribution = new double[intervals];

        // 统计每个区间的抽数次数
        // 0-9抽归为第0区间，10-19抽归为第1区间，以此类推
        for (int result : results) {
            int interval = (result - 1) / 10;
            distribution[interval]++;
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
        System.out.println("井的抽数为: " + PITY_THRESHOLD);
        System.out.println("新限定干员占六星概率为: 35%");
        System.out.println("- 模拟次数: " + SIMULATION_TIMES);
        System.out.println();

        System.out.println("开始计算...");
        long startTime = System.currentTimeMillis();

        // 通过模拟计算期望抽数
        double expectedDraws = calculateExpectedDrawsAndCollectResults();

        // 计算每10抽区间的概率分布
        double[] probabilityDistribution = calculateProbabilityDistribution();

        // 计算50%、75%、90%、99%概率抽出所需的抽卡次数
        int percentile50 = calculatePercentileDraws(0.5);
        int percentile75 = calculatePercentileDraws(0.75);
        int percentile90 = calculatePercentileDraws(0.9);
        int percentile99 = calculatePercentileDraws(0.99);

        //计算保底的概率
        double probability = (double) IntStream.range(1, results.length + 1)
                .filter(i -> results[i-1] == PITY_THRESHOLD)
                .findFirst()
                .orElse(-1) /SIMULATION_TIMES;

        long endTime = System.currentTimeMillis();
        System.out.println("计算完成，耗时: " + (endTime - startTime) / 1000.0 + "秒");
        System.out.println();

        System.out.println("结果：");
        System.out.println("- 模拟计算的期望抽数: " + String.format("%.2f", expectedDraws) + "次");
        System.out.println("- 保底概率: " + String.format("%.2f%%", probability * 100));
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