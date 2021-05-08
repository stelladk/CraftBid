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
        //load jdbc driver and connect to the database
        Connection db_connect = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String db_url = "jdbc:sqlserver://"+Constants.HOST+":"+Constants.PORT+";databaseName="+Constants.DATABASE;
            db_connect = DriverManager.getConnection(db_url,Constants.USER,Constants.PASSWORD);
        }catch (ClassNotFoundException | SQLException e) {
            System.err.println("Unable to connect to database!");
            return;
        }

        //serve the request
        ObjectInputStream input; 
        ObjectOutputStream output;
        String query,username,password,fullname,email,phone,desc;
        boolean is_creator;
        Statement stm;
        ResultSet res;
        try {
            output = new ObjectOutputStream(request.getOutputStream());
            input = new ObjectInputStream(request.getInputStream());
            switch((String)input.readObject()) { //clients send a request type
                //LOGIN
                case "LOGIN":
                    System.out.println("Received a new LOGIN request");
                    username = (String) input.readObject();
                    password = (String) input.readObject();
                    System.out.println("Username= " + username + ",password = " + password);
                    //check if credentials are correct
                    query = "SELECT * FROM UserInfo WHERE username= \'" + username + "\';";
                    stm = db_connect.createStatement();
                    res = stm.executeQuery(query);
                    if (res.next()) {
                        System.out.println("Username is correct.");
                        String pass = res.getString("password");
                        if (pass.equals(password)) {
                            System.out.println("Password correct! Login Successful");
                            output.writeObject("LOGIN SUCCESSFUL");
                            output.flush();
                        } else {
                            System.out.println("Password incorrect!");
                            output.writeObject("WRONG PASSWORD");
                            output.flush();
                        }
                    } else {
                        System.out.println("Username doesn't exist");
                        output.writeObject("WRONG USERNAME");
                        output.flush();
                    }
                    break;

                //SIGNUP FOR CUSTOMERS AND CREATORS
                case "SIGNUP_USER":
                    System.out.println("Received a new SIGNUP_USER request");
                    username = (String)input.readObject();
                    password = (String)input.readObject();
                    fullname = (String)input.readObject();
                    email = (String)input.readObject();
                    phone = (String)input.readObject(); //send "NULL" if none given
                    desc = (String)input.readObject(); //send "NULL" if none given
                    is_creator = (boolean)input.readObject();
                    System.out.println("Username= "+username+",password = "+password+
                                        ",FullName= "+fullname+",email= "+email+",phone= "+phone+",description= "+desc);
                    //check if username already exists
                    query = "SELECT * FROM UserInfo WHERE username= \'"+username+"\';";
                    stm = db_connect.createStatement();
                    res = stm.executeQuery(query);
                    if(res.next()) {
                        System.out.println("Username already exists!");
                        output.writeObject("USER ALREADY EXISTS");
                        output.flush();
                    }else {
                        System.out.println("Username doesn't exist. Registering user!");
                        //insert new tuple to db
                        query = "INSERT INTO UserInfo (username,password,fullname,email,phoneNumber,description,photo) "+
                                "VALUES(\'"+username+"\',\'"+password+"\',\'"+fullname+"\',\'"+email+"\' " +
                                ",NULL,NULL,NULL);"; //TODO: insert nulls to table only if user sent "NULL"

                        //if is creator
                        if(is_creator) {
                            System.out.println("User is a creator. Adding more info");
                            //TODO: signup creator
                        }
                        stm.executeUpdate(query);
                        output.writeObject("REGISTER SUCCESSFUL");
                        output.flush();
                    }
                    break;
                case "REQUEST TYPE 2":
                    //execute code for this request type (read database info, write to database tables, process data)
                    break;
            }
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process request");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server("192.168.2.2",6500,100);
    }
}