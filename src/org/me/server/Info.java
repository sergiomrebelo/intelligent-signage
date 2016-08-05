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
public class Info {
    protected int id;
    protected String name;
    protected String longname;
    
    public Info (int id, String name, String longname) {
        this.id = id;
        this.name = name;
        this.longname=longname;
    }
    
    public Info (int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Info (int id) {
        this.id=id;
    }
    
    public Info () {
        
    }
}
