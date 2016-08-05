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
public class Teacher extends Info{
    protected ArrayList <Schedule> officeHours;
    
    public Teacher (int id, String name, ArrayList <Schedule> officeHours) {
        super (id, name);
        this.officeHours = officeHours;
        
    }
    
    public Teacher (int id, String name) {
        super (id, name);
    }
    
    public void setOfficeHours (ArrayList <Schedule> offH) {
        this.officeHours=offH;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
