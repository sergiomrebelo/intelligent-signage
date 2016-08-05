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
import java.applet.Applet;
import java.util.*;
import processing.core.*;
import processing.opengl.*;
import static processing.core.PConstants.P2D;

import de.bezier.data.sql.*;

public class ServerSide {

    /**
     * @param args the command line arguments
     */
    public static String user ="", pass="",database="";
    public static MySQL mysql;
    
    public static void main(String[] args) {
        Sketch.main ("org.me.server.Sketch");
        
    }
    
}
