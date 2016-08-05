/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.server;

/**
 *
 * @author sergiorebelo
 */
public class Schedule {
    protected String  start, end;
    protected int day;
    
    public Schedule (int day, String start, String end) {
        this.day=day;
        this.start=start;
        this.end=end;
    }
}
