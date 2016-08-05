/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.server;

import java.util.ArrayList;

/**
 *
 * @author sergiorebelo
 */
public class Office extends Info{
    //de 1 a 14
    protected ArrayList<Teacher> teachers;
    
    public Office (int id, ArrayList<Teacher> teachers) {
        super (id); 
        this.teachers= teachers;
    } 
    
    public Office () {
        super ();
    }
    
    @Override
    public String toString() {
        return id+" "+teachers;
    }
    
 }
