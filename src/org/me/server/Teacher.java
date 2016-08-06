/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.server;

import java.util.*;
import org.joda.time.LocalTime;
import static processing.core.PApplet.*;

/**
 *
 * @author sergiorebelo
 * 
 * Object Teacher 
 * 
 */

public class Teacher extends Info{
    protected ArrayList <Schedule> officeHours;
    private Boolean isPresent = false;
            
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
    
    /*
    getAvailability : Boolean
    return true is teacher are present in office and false if not
    */
    public boolean getAvailability() {
        return isPresent;
    }
    
    /*
    isDisponible : void
    the teacher is present in your office
    */
    public void isDisponible () {
        this.isPresent = true;
    } 
    
    /*
    isntDisponible : void
    the teacher isn't present in your office
    */
    public void isntDisponible () {
        this.isPresent = false;
        println("não estou disponivel");
    }
    
    /*
    inOffice (Teacher t): return boolean
    —
    see if the teacher <t> is in your office hours
    returns true if it is and false in other times
    */
    public Boolean inOffice () {
        Boolean b=false;
        Calendar c;
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);  
        for (Schedule soh : this.officeHours) {
           if (dayOfWeek == soh.day) {
               if (timeComparation(soh.start, soh.end)) {
                    b=true;
               }
           } 
        }
        return b;
    }
    
    /*
    timeComparation (String <start>, String <end>): return boolean
    —
    compares the current time with a time interval [start,end]
    the input parameter (Strings) is formated as (HH:MM:SS), like a time SQL object
    return true is <actualTime> are in interval and false on the contrary
    */
    public boolean timeComparation(String start, String end) {
        LocalTime init, limit,now;
        String actualTime = hour()+":"+minute()+":"+second(); 
        init = new LocalTime(start);
        limit = new LocalTime (end);
        now = new LocalTime(actualTime);
        return (now.isAfter(init) && now.isBefore(limit));
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
