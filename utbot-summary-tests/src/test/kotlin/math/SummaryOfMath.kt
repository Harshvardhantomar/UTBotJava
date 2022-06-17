package math

import examples.SummaryTestCaseGeneratorTest
import guava.examples.math.Stats
import org.junit.jupiter.api.Test
import org.utbot.examples.DoNotCalculate
import org.utbot.framework.plugin.api.MockStrategyApi

class SummaryOfMath : SummaryTestCaseGeneratorTest(
    Stats::class,
) {
    @Test
    fun testOf1() {
        val summary1 = "Test calls StatsAccumulator::addAll,\n" +
                "    there it triggers recursion of addAll once, \n" +
                "Test throws NullPointerException in: acummulator.addAll(values);\n"
        val summary2 = "Test calls StatsAccumulator::addAll,\n" +
                "    there it does not iterate for(int value: values), \n" +
                "Test later calls StatsAccumulator::snapshot,\n" +
                "    there it returns from: return new Stats(count, mean, sumOfSquaresOfDeltas, min, max);\n" +
                "    \n" +
                "Test then returns from: return acummulator.snapshot();"
        val summary3 = "Test calls StatsAccumulator::addAll,\n" +
                "    there it iterates the loop for(int value: values) once. \n" +
                "Test later calls StatsAccumulator::snapshot,\n" +
                "    there it returns from: return new Stats(count, mean, sumOfSquaresOfDeltas, min, max);\n" +
                "    \n" +
                "Test then returns from: return acummulator.snapshot();"
        val summary4 = "Test calls StatsAccumulator::addAll,\n" +
                "    there it iterates the loop for(int value: values) twice. \n" +
                "Test later calls StatsAccumulator::snapshot,\n" +
                "    there it returns from: return new Stats(count, mean, sumOfSquaresOfDeltas, min, max);\n" +
                "    \n" +
                "Test later returns from: return acummulator.snapshot();\n"

        val methodName1 = "testOf2_StatsAccumulatorAddAll"
        val methodName2 = "testOf2_snapshot"
        val methodName3 = "testOf2_IterateForEachLoop"
        val methodName4 = "testOf2_IterateForEachLoop_1"

        val displayName1 = "acummulator.addAll(values) : True -> ThrowNullPointerException"
        val displayName2 = "snapshot -> return new Stats(count, mean, sumOfSquaresOfDeltas, min, max)"
        val displayName3 = "addAll -> return new Stats(count, mean, sumOfSquaresOfDeltas, min, max)"
        val displayName4 = "addAll -> return new Stats(count, mean, sumOfSquaresOfDeltas, min, max)"

        val method = Stats::of2
        val mockStrategy = MockStrategyApi.NO_MOCKS
        val coverage = DoNotCalculate

        val summaryKeys = listOf(
            summary1,
            summary2,
            summary3,
            summary4
        )

        val displayNames = listOf(
            displayName1,
            displayName2,
            displayName3,
            displayName4
        )

        val methodNames = listOf(
            methodName1,
            methodName2,
            methodName3,
            methodName4
        )

        check(method, mockStrategy, coverage, summaryKeys, methodNames, displayNames)
    }
}