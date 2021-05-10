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
        try {
            output = new ObjectOutputStream(request.getOutputStream());
            input = new ObjectInputStream(request.getInputStream());
            switch((String)input.readObject()) { //clients send a request type
                //LOGIN
                case "LOGIN":
                    login(db_connect,input,output);
                    break;
                //SIGNUP FOR CUSTOMERS AND CREATORS
                case "SIGNUP_USER":
                    signup(db_connect,input,output);
                    break;
                case "LOAD_MAIN_SCREEN":
                    //TODO request most recent listings
                    break;
                case "SEARCH":
                    //TODO request search results
                    break;
                case "REQUEST_PROFILE":
                    //TODO request user and creator profile info
                    break;
                case "CREATE_LISTING":
                    //TODO add listing to database
                    break;
                case "VIEW_LISTING":
                    //TODO request listing info
                    break;
                case "UPDATE_LISTING":
                    //TODO update listing info in database
                    break;
                case "CREATE_BID":
                    //TODO add offer to database
                    break;
                case "VIEW_OFFERS":
                    //TODO request list of offers for a listing
                    break;
                case "DECLINE_OFFER":
                    //TODO remove offer from database
                    break;
                case "CREATE_EVALUATION":
                    //TODO add evaluation to database
                    break;
                case "CREATE_REPORT":
                    //TODO add report to database
                    break;
                case "VIEW_REWARDS":
                    //TODO request list of creator's rewards
                    break;
                case "ADD_REWARD":
                    //TODO add reward for creator in database
                    break;
            }
            //after serving request
            db_connect.close();
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process request");
            e.printStackTrace();
            return;
        }
    }

    /** LOGIN
     * Check credentials, first if the username exists, then if the password is correct */
    public void login(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new LOGIN request");
        try {
            String username = (String) input.readObject();
            String password = (String) input.readObject();
            System.out.println("Username= " + username + ",password = " + password);
            //check if credentials are correct
            String query = "SELECT * FROM UserInfo WHERE username= \'" + username + "\';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
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
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process login request");
            e.printStackTrace();
        }
    }//login

    /** SIGNUP
     * Basic signup first, same for all users. Check if username and email already exist and register user
     * Then additional signup for creators only, with mandatory phone number and extra info */
    public void signup(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new SIGNUP_USER request");
        try {
            //get all basic signup information for all users, also if the user is a creator
            String username = (String)input.readObject();
            String password = (String)input.readObject();
            String fullname = (String)input.readObject();
            String email = (String)input.readObject();
            String phone = (String)input.readObject(); //send "NULL" if none given
            String desc = (String)input.readObject(); //send "NULL" if none given
            String photo = (String)input.readObject(); //send "NULL" if none given
            boolean is_creator = (boolean)input.readObject();
            System.out.println("Username= "+username+",password = "+password+
                    ",FullName= "+fullname+",email= "+email+",phone= "+phone+",description= "+desc);

            //check if username already exists
            String query = "SELECT * FROM UserInfo WHERE username= \'"+username+"\';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            if(res.next()) {
                System.out.println("Username already exists!");
                output.writeObject("USER ALREADY EXISTS");
                output.flush();
            }else {
                //TODO: check if email already exists
                query = "SELECT * FROM UserInfo WHERE email= \'"+email+"\';";
                stm = db_connect.createStatement();
                res = stm.executeQuery(query);
                if(res.next()) {
                    System.out.println("Email already exists!");
                    output.writeObject("EMAIL ALREADY EXISTS");
                    output.flush();
                }else {
                    System.out.println("Username and email don't exist. Registering user!");
                    //insert new tuple to db
                    query = "INSERT INTO UserInfo (username,password,fullname,email,phoneNumber,description,photo) "+
                            "VALUES(\'"+username+"\',\'"+password+"\',\'"+fullname+"\',\'"+email+"\'," +
                            (phone.equals("NULL")?phone : "\'"+phone+"\'")+","+
                            (desc.equals("NULL")?desc : "\'"+desc+"\'")+","+
                            (photo.equals("NULL")?photo : "\'"+photo+"\'")+");"; //insert nulls to table only if user sent "NULL"
                    stm.executeUpdate(query);
                    output.writeObject("BASIC REGISTER SUCCESSFUL");
                    output.flush();

                    //if is creator
                    //creators phone is never null, also some additional info (the android clients perform validation checking)
                    if(is_creator) {
                        System.out.println("User is a creator. Adding more info");
                        int bit = (Integer)input.readObject();
                        String expertise = (String)input.readObject();
                        query = "INSERT INTO Creator (username,isFreelancer,phoneNumber,hasExpertise) "+
                                "VALUES(\'"+username+"\',"+bit+",\'"+phone+"\',\'"+expertise+"\');";
                        stm.executeUpdate(query);
                        output.writeObject("CREATOR REGISTER SUCCESSFUL");
                        output.flush();
                    }
                }
            }
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process login request");
            e.printStackTrace();
        }
    }//signup

    public static void main(String[] args) {
        new Server("192.168.2.2",6500,100);
    }
}