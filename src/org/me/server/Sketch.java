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

import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import processing.core.*;
import processing.serial.*;





public class Sketch extends PApplet {
    static final String USER ="root";
    static final  String PASS="root";
    static final  String HOST="localhost";
    static final Integer PORT =8889;
    static final String DB_NAME= "darq_db";
    static final String DB_URL="jdbc:mysql://"+HOST+":"+PORT+"/"+DB_NAME;
    public Connection conn = null;
    public Statement stmt = null;
    public String actualTime = null; 
    //port= 8889
    //Serial conection
    public Serial myPort;
    public String val="";
    final int SERIAL_PORT= 9600;
    public int institution=1; //1 Darq — 2: CA
    public int officeNumber=2361;
    
    //
    public ArrayList<Service> services;
    public Office office;
    public ArrayList<Boolean> onOfficeArray = new ArrayList<Boolean> ();
    
    
    @Override
    public void setup() {
        frameRate(1);
        //Init Serial Comunication
        printArray(Serial.list());
        String portName =Serial.list()[1];
        myPort = new Serial (this,portName, SERIAL_PORT);
        
        /*server-side*/
        //database connection
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("connecting to the database");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("creating a statement..");
            stmt = conn.createStatement();
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } catch (ClassNotFoundException e) { 
           // System.out.println("2");
           e.printStackTrace();
        }
    }
    
    @Override
    public void draw() {
        background (40);
        //Date actualTime= getCurrentTime(stmt); 
        actualTime = hour()+":"+minute()+":"+second();
        
        office = getOffice();   
                    
    }
    
    
    public void serialEvent(Serial myPort) {
        try {
            val = myPort.readStringUntil('\n');
            if (val != null) {
                if (val.equals("TOF")) { //gabinetes
                    office = getOffice();   
                    onOfficeArray.clear();
                    for (Teacher t :office.teachers){
                       onOfficeArray.add(inOffice (t)); 
                    }
                
                } 
                //if(value.indexOf("t:") >= 0) 
                else if (val.indexOf("SER") >=0) {
                   services=getCurrentServices (stmt); 
                   if (services.size() > 0 || services!= null){
                       for (Service s : services){
                           myPort.write("ON"+s.id+"");
                           //myPort.write("ON"+13+"");
                          //myPort.write("ON"+12+"");
                          
                       }
                   }
                   else if (services.size() <0)  myPort.write("SER:0");
                   else if (services!= null)  myPort.write("SER:!"); //aconteceu um erro — voltar a tentar
                } else if (val.equals ("CROOM")) { //classroom

                } else if (val.indexOf("START") >=0) { //start conexion
                    if (val.indexOf("INST") >=0) {
                       // println(val);
                        String[] info = val.split(" ");
                        String inst[] = info[1].split(":"); 
                        institution = Integer.parseInt(inst[1]);
                    } else if (val.indexOf("OFF") >=0){
                        String[] info = val.split(" ");
                        String off[] = info[1].split(":");
                        officeNumber = Integer.parseInt(off[1]);
                                
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        
    }

    
    
    
    
    public void settings() {
        //fullScreen();
        size (500,500);
    }
    
    public Date getCurrentTime (Statement stmt) {
        String sql="SELECT NOW()";
        Date now = new Date();
        String result = new String();
        
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                result = rs.getString("NOW()");  
            }
            
            String explode[]=result.split(" ");
            if (explode.length==2) {
                 String date[] = explode[0].split("-");
                 Integer year = Integer.parseInt(date[0])-1900, month=Integer.parseInt(date[1])-1, day=Integer.parseInt(date[2]);
                 String time[] = explode[1].split(":");
                 Integer hrs= Integer.parseInt(time[0]), min=Integer.parseInt(time[1]); 
                 Integer sec = Math.round(Float.parseFloat(time[2]));
                 now = new Date (year, month, day, hrs,min, sec);
            } else {
                now = null;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Sketch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return now;
    }
    
    public ArrayList<Service> getCurrentServices (Statement stmt) {
        String sql ="SELECT * FROM services where ((Opening_hours <= '"+actualTime+"' && Close_hours >= '"+actualTime+"'&& !(Lunch_start_time <='"+actualTime+"' && '"+actualTime+"'<= Lunch_ending_time)) OR Operating_days = 1) && Institution="+institution;
        //&& Institution="+institution
        //String result = new String();
       ArrayList<Service> services = new ArrayList<Service>();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                int id = rs.getInt("inst_id");
                String name = rs.getString("Short_name");
                int inst = rs.getInt("Institution");
                int timetable = rs.getInt("Operating_days"); //type of schedule
                services.add(new Service (id, name,timetable, inst));   
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Sketch.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return services;
        
    }
    
    
    public Office getOffice () {
        String sql= "SELECT * FROM Teachers WHERE Office="+officeNumber;
        ArrayList<Teacher> teacher = new ArrayList<Teacher>();
        Office of = new Office();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                teacher.add( new Teacher (id, name));
            }
            for (Teacher t : teacher) {
                //SELECT * FROM office_times WHERE teacher_id=8
                sql = "SELECT * FROM office_times WHERE teacher_id="+t.id;
                ArrayList<Schedule> schedules = new ArrayList<Schedule>();
                try {
                  rs = stmt.executeQuery(sql);
                  while(rs.next()){
                      //Schedule (String day, String start, String end)
                     int day = rs.getInt("day");
                     String start = rs.getString("start_time");
                     String end = rs.getString("end_time");
                     schedules.add(new Schedule(day, start, end));
                  }
                  t.setOfficeHours(schedules);
                } catch (SQLException ex) {
                    Logger.getLogger(Sketch.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            of = new Office (officeNumber, teacher);
        } catch (SQLException ex) {
            Logger.getLogger(Sketch.class.getName()).log(Level.SEVERE, null, ex);
        }
            
                
        return of;
    }
    
public Boolean inOffice (Teacher t) {
    //ver se esta entre as horas de gabinete
    Boolean b=false;
    Calendar c;
    
     
    int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);   
    for (Schedule soh : t.officeHours) {
       if (dayOfWeek == soh.day) {
           String [] start = soh.start.split(":");
           String [] end = soh.end.split(":");
           if (hour()>=Integer.parseInt(start[0]) && hour()<=Integer.parseInt(end[0])) {
              if (minute()>=Integer.parseInt(start[1]) && minute()<=Integer.parseInt(end[1])){
                b=true;
              }
           }
       } 
       
    }
    return b;
}
    
}


