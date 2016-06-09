/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author smallgod
 */
public class MemoryUsageIndicator {

    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public static void getMemoryUsed(String[] args) {
        // I assume you will know how to create a object Person yourself...
//        List<Person> list = new ArrayList<Person>();
//        for (int i = 0; i <= 100000; i++) {
//            list.add(new Person("Jim", "Knopf"));
//        }
        
        
        
        
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: " + memory);
        System.out.println("Used memory is megabytes: "
                + bytesToMegabytes(memory));
    }

    public static void in(String args[]) {
        int n = Integer.parseInt(args[0]);
        int k = 100000;

        ArrayList<String> list = new ArrayList<String>(n);
        char[] array = new char[3];

        for (int i = 0; i < n; i++) {

            array[0] = 'a';
            array[1] = 'b';
            array[2] = 'c';
            list.add(new String(array));

            if (i % k == 0) {
                long m1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                m1 /= (1024 * 1024);
                System.out.println("memory used  when " + i + " key inserted =" + m1 + " MBytes.");
            }
        }

    }

    public static void getTimeUsed(String[] args) {

        long startTime = System.currentTimeMillis();

        long total = 0;
        for (int i = 0; i < 10000000; i++) {
            total += i;
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime);
    }
}
