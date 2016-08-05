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
public class Service extends Info{
    protected int timetable;
    protected Boolean EmergencyExit = false;
    protected int institucion;
    
    public Service (int id, String name, int timetable, int institucion) {
        super (id, name);
        this.institucion=institucion;
    }
    
    @Override
    public String toString () {
        return "service | "+name+" | id:"+id;
    }
    
}
