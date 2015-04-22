package com.dci.intellij.dbn.connection.ssh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshTunnelConnection {
    static int lport;
    static String rhost;
    static int rport;
    public static void go(){
        String user = "ripon";
        String password = "wasim";
        String host = "myhost.ripon.wasim";
        int port=22;
        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            lport = 4321;
            rhost = "localhost";
            rport = 3306;
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("Establishing Connection...");
            session.connect();
            int assinged_port=session.setPortForwardingL(lport, rhost, rport);
            System.out.println("localhost:"+assinged_port+" -> "+rhost+":"+rport);
        }
        catch(Exception e){System.err.print(e);}
    }
    public static void main(String[] args) {
        try{
            go();
        } catch(Exception ex){
            ex.printStackTrace();
        }
        System.out.println("An example for updating a Row from Mysql Database!");
        Connection con = null;
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://" + rhost +":" + lport + "/";
        String db = "testDB";
        String dbUser = "wasim";
        String dbPasswd = "riponalwasim123";
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUser, dbPasswd);
            try{
                Statement st = con.createStatement();
                String sql = "UPDATE MyTableName " +
                        "SET email = 'ripon.wasim@smile.com' WHERE email='peace@happy.com'";

                int update = st.executeUpdate(sql);
                if(update >= 1){
                    System.out.println("Row is updated.");
                }
                else{
                    System.out.println("Row is not updated.");
                }
            }
            catch (SQLException s){
                System.out.println("SQL statement is not executed!");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
