/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author smallgod
 */
class Counter {

    private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();

    //increment the count for the user
    public void increment(String user) {
        
        while (true) {
            AtomicInteger current = counts.get(user);
            if (current == null) {
                //new user, initialize the count
                counts.putIfAbsent(user, new AtomicInteger());
                continue;
            }

            int value = current.incrementAndGet();
            if (value > 0) {
                //we have incremented the counter
                break;
            } else {
                //someone is flushing this key, remove it
                //so we can increment on our next iteration
                counts.replace(user, current, new AtomicInteger());
            }

        }
    }
}
