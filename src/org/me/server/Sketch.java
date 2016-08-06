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
import java.time.LocalTime.*;
import org.joda.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import processing.core.*;
import processing.serial.*;





public class Sketch extends PApplet {
    //DATABASE
    //consts to connect database
    static final String USER ="root";
    static final  String PASS="root";
    static final  String HOST="localhost";
    static final Integer PORT =8889;
    static final String DB_NAME= "darq_db";
    static final String DB_URL="jdbc:mysql://"+HOST+":"+PORT+"/"+DB_NAME;
    //variable
    public Connection conn = null;
    public Statement stmt = null;
    
    //SERIAL COMUNICATION
    public Serial myPort;
    public String val="";
    final int SERIAL_PORT= 9600;
    
    public String actualTime = null; 
    public int institution=1; //1 Darq — 2: CA
    public int officeNumber=1;
    
    public ArrayList<Service> services;
    public Office office;
    public ArrayList<Boolean> onOfficeArray = new ArrayList<> ();
    
    public int interfaceMode = 0;
    //public ArrayList<Office> officesInterface;
    
    //Graphic variables
    public long margin = 0;
    public PFont font, secundaryFont;

    
    @Override
    public void setup() {
        //printArray(Serial.list());
        //Serial Connection with Hardware
        String portName =Serial.list()[1];
        myPort = new Serial (this,portName, SERIAL_PORT);
        databaseConnection();
        
        
        //Interface inits
        smooth();
        font = createFont("KarbonSlabStencil-Regular", 120);
        secundaryFont = createFont ("OfficeCodePro-Medium",16);
        //String[] fontList = PFont.list();
        //printArray(fontList);
    }
    
    @Override
    public void draw() {
        margin = Math.round(width*0.007);
        background (40);
        fill(245);
        if(institution==1) {
            rect (margin, margin, width-margin*2, height-margin*2);
            fill(0);
        }
        //Date actualTime= getCurrentTime(stmt); 
        actualTime = hour()+":"+minute()+":"+second(); 
        textFont(secundaryFont);
        textAlign(LEFT);
        String s = "";
        if (interfaceMode == 1) s = "Office "+officeNumber;
        else if (interfaceMode == 0) s="services";
        else if (interfaceMode == 3) s="loading";
        int textMargin = width/12;
        text(s,textMargin,Math.round(margin*6));
        s=actualTime;
        text(s,textMargin*3,Math.round(margin*6));
        textFont (font);
        s= "";

        switch (interfaceMode){
            case 0:
                break;
            case 1:
                if (office !=null){
                    float y = Math.round(margin*20);
                    for (Teacher t :office.teachers ){
                        s = t.name;
                        boolean inOffice =  t.inOffice();
                        if (t.getAvailability()) inOffice=true;
                        if (inOffice) s+=" · ";
                        text(s, textMargin, y);
                        y +=Math.round(margin*14);
                    }
                    
                }
            break;
        }
    }
    
    /*
    serialEvente (Serial <myPort>) : void
    —
    Serial Connection with Hardware by the Serial port <myPort> 
    */
    public void serialEvent(Serial myPort) {
        try {
            val = myPort.readStringUntil('\n');
            if (val != null) {
                //Hardwares needs Offices Times
                if (val.contains("TOF")) { //TOF: Teachers Offices
                    //office = getOffice(); //get the Office information
                    if(office == null) office = getOffice(officeNumber);
                    onOfficeArray.clear();
                    int i =0;
                    boolean reset=true;
                    for (Teacher t :office.teachers) {
                       boolean inOffice =  t.inOffice();
                       if (t.getAvailability()) 
                           inOffice=true; 
                       onOfficeArray.add(inOffice);
                       if (inOffice) {
                           reset = false;
                           myPort.write(""+i); 
                       } 
                       i++;
                    }
                    
                    if (reset){
                        myPort.write("RESET");
                    }
                } 
                //Hardwares needs Services information
                else if (val.contains("SER")) {
                   services=getCurrentServices (stmt); 
                   if (services.size() > 0 || services!= null){
                       services.stream().forEach((s) -> {
                           myPort.write("ON"+s.id+"");
                       });
                   }
                   else if (services.size() <0)  myPort.write("SER:0"); // no services disponibles
                  else if (services!= null)  myPort.write("SER:!"); //a error happened, try again
                //Hardware Starts Conexion
                } else if (val.contains("START")) { 
                    //Service plantaform
                    if (val.contains("INST")) {
                        interfaceMode = 2;
                        String[] info = val.split(" ");
                        String inst[] = info[1].split(":"); 
                        institution = Integer.parseInt(inst[1]);
                    } else if (val.contains("OFF")){ //offices plantaform
                        interfaceMode = 1;
                        String[] info = val.split(" ");
                        String off[] = info[1].split(":");
                        officeNumber = Integer.parseInt(off[1]);
                        office = getOffice(officeNumber);
                        myPort.write("NPROF:"+office.teachers.size()); //send number of teacher using the office
                    }
                } 
                //The teacher are/aren't avaiable in office schedule
                //only in office cases
                else if (val.contains("NOT:")) { 
                     int t = Character.getNumericValue(val.charAt(4));
                     if (!office.teachers.get(t).getAvailability())
                        office.teachers.get(t).isDisponible();
                     else 
                        office.teachers.get(t).isntDisponible();
                }
            }
        } catch(Exception e){
            //e.getCause();
        }
        
    }

    /* 
    Settings: void 
    —
    Define the seetings of PApplet
    */
    @Override
    public void settings() {
        fullScreen();
        //size (1000,700);
    }
    
    /*
    Hardware—Database comunication functions
    */
    
    /*DESCRITION*/
    public ArrayList<Service> getCurrentServices (Statement stmt) {
        String sql ="SELECT * FROM services where ((Opening_hours <= '"+actualTime+"' && Close_hours >= '"+actualTime+"'&& !(Lunch_start_time <='"+actualTime+"' && '"+actualTime+"'<= Lunch_ending_time)) OR Operating_days = 1) && Institution="+institution;
        ArrayList<Service> servicesLocal;
        servicesLocal = new ArrayList<>();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                int id = rs.getInt("inst_id");
                String name = rs.getString("Short_name");
                int inst = rs.getInt("Institution");
                int timetable = rs.getInt("Operating_days"); //type of schedule
                servicesLocal.add(new Service (id, name,timetable, inst));   
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Sketch.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return servicesLocal;
        
    }
    
    
    /*
    getOffice (int officeNumber) : Office
    —
    return the information about the office with the office number
    */
    public Office getOffice (int officeNumber) {
        String sql= "SELECT * FROM Teachers WHERE Office="+officeNumber;
        ArrayList<Teacher> teacher = new ArrayList<>();
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
                ArrayList<Schedule> schedules = new ArrayList<>();
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
    
    /*
    globalFunctions
    */
    
    /*
    databaseConnection
    —
    Connect with Database
    */
    public void databaseConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            println("connecting to the database");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            println("creating a statement..");
            stmt = conn.createStatement();
            println ("Connection Established with"+DB_URL);
        } catch (SQLException ex) {
            println("SQLException: " + ex.getMessage());
            println("SQLState: " + ex.getSQLState());
            println("VendorError: " + ex.getErrorCode());
            exit();
        } catch (ClassNotFoundException e) { 
           e.getCause();
           exit();
        }
    }

    /*
    get CurrentTime (Statement stmt): return Date [NOT USED]
    —
    Return the currentTime of the database through a select request
    implemented only if the times GMT of software are differntes, 
    or if the query is performed by hardware
    */
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
    
    /*
    getTeachersInOffice : ArrayList <Office> [only for interface] [NOT USED]
    —
    Return all the teachers in office time
    */
    public ArrayList <Office> getTeachersInOffice () {
        ArrayList <Office> out = new ArrayList <>();
        final int offices [] = {1,216,223,242,243,260,1260,2361,2371};
        for (int n : offices) {
            out.add(getOffice (n));
        }
        return out;
    }
   
}



