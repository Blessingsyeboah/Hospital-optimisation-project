package com.hospitaloptimizer.tests;

import com.hospitaloptimizer.shared.util.SimpleTest;

/**
 * Aggregate test runner: exercises every pod's unit test suite in one run.
 * This is the top-level "tests" folder referenced in the repo README --
 * each pod ALSO has its own src/test tree it can run independently
 * (see each pod's build.sh / test.sh), but this class is the one command
 * an examiner runs to see the whole team's evidence at once.
 *
 * Exits with status 1 if any test failed, so it can be wired into CI.
 */
public class RunAllTests {

    public static void main(String[] args) throws Exception {
        System.out.println("=====================================================");
        System.out.println(" Hospital Optimizer -- full test suite (all 5 pods)");
        System.out.println("=====================================================");

        // Pod 2 -- Lists & Search/Sort
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod2.CustomArrayListTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod2.CustomLinkedListTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod2.SearchAlgorithmsTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod2.SortAlgorithmsTest"));

        // Pod 3 -- Queues, Priority & Smart Assignment
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod3.LinearStructuresTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod3.CustomPriorityQueueTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod3.SchedulingEngineTest"));

        // Pod 4 -- Trees, Fast Lookup & Planning Ahead
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod4.CustomBSTTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod4.RedBlackTreeTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod4.BTreeTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod4.DynamicProgrammingTest"));

        // Pod 5 -- Hashing, Grouping & Maps
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod5.CustomHashMapTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod5.DisjointSetTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod5.GraphAlgorithmsTest"));

        // Pod 1 -- Data, Database & Delivery
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod1.DataLoaderTest"));
        SimpleTest.run(Class.forName("com.hospitaloptimizer.pod1.DispatchDeliveryServiceTest"));

        System.out.println();
        SimpleTest.printSummary();

        if (!SimpleTest.allPassed()) {
            System.exit(1);
        }
    }
}
