package com.craftbid.craftbid;

import java.net.*;
import java.io.*;
import java.sql.*;

public class Server {

    String ip; int port,concurrent_requests;
    ServerSocket requests;


    public Server(String ip,int port,int concurrent_requests) {
        System.out.println("Starting server in ip "+ip+" and port "+port);
        this.ip = ip;
        this.port = port;
        this.concurrent_requests = concurrent_requests;
        connect();
    }   

    public void connect() {
        try{
            requests = new ServerSocket(this.port,concurrent_requests);
            Socket request;
            while(true) {
                request= requests.accept();
                //new thread per request
                new Thread(new Runnable(){
                    Socket request;

                    public Runnable init(Socket request) {
                        this.request = request;
                        return this;
                    }

                    public void run() {
                        serve_request(request);
                    }
                }.init(request)).start();
            }
        } catch(IOException e) {
            System.err.println("Application server failed");
            e.printStackTrace();
        }
    }

    public void serve_request(Socket request) {
        ObjectInputStream input; 
        ObjectOutputStream output;
        try {
            output = new ObjectOutputStream(request.getOutputStream());
            input = new ObjectInputStream(request.getInputStream());
            switch((String)input.readObject()) { //clients send a request type
                case "REQUEST TYPE 1":
                    //execute code for this request type (read database info, write to database tables, process data)
                    break;
                case "REQUEST TYPE 2":
                    //execute code for this request type (read database info, write to database tables, process data)
                    break;
            }
        }catch(IOException | ClassNotFoundException e) {
            System.err.println("Unable to process request");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //new Server("192.168.2.2",6500,100);

        //load jdbc driver
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //test connection to database
            Connection db_connect = null;
            String db_url = "jdbc:sqlserver://"+Constants.HOST+":"+Constants.PORT+";databaseName="+Constants.DATABASE;
            db_connect = DriverManager.getConnection(db_url,Constants.USER,Constants.PASSWORD);

            String query = "SELECT * FROM Creator;";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
        }catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}