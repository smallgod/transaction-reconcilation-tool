/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

/**
 *
 * @author smallgod
 */
public final class ReadMutexClass {

    private final String MUTEX;

    public ReadMutexClass(String mutex) {
        this.MUTEX = mutex;
    }

    public String getMUTEX() {
        return MUTEX;
    }
}
