package com.craftbid.craftbid;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;

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
                    load_main(db_connect,input,output);
                    break;
                case "SEARCH":
                    //TODO request search results
                    break;
                case "REQUEST_PROFILE":
                    request_profile(db_connect,input,output);
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
                    view_rewards(db_connect,input,output);
                    break;
                case "ADD_REWARD":
                    add_reward(db_connect,input,output);
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
                //check if email already exists
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


    /** LOAD MAIN SCREEN
     * When a user logs into the app, a list of most recently posted listings' thumbnails appears */
    public void load_main(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new LOAD_MAIN_SCREEN request");
        try {
            //get a list of all listings, ordered by date added
            String query = "SELECT * FROM Listing ORDER BY date_published;";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            ArrayList<Thumbnail> listing_thumbnails =new ArrayList<Thumbnail>();
            while(res.next()) {
                //create a list of listing thumbnails
                int id = res.getInt("id");
                String name =  res.getString("name");
                String desc = res.getString("description");
                String category = res.getString("category");
                String thumbnail = res.getString("thumbnail");
                float min_price = res.getFloat("min_price");
                listing_thumbnails.add(new Thumbnail(id,name,desc,category,thumbnail,min_price));
            }
            output.writeObject(listing_thumbnails); //send thumbnails
            output.flush();
        }catch(IOException | SQLException e) {
            System.err.println("Unable to process login request");
            e.printStackTrace();
        }
    }//load main screen


    /** REQUEST PROFILE
     * Request the profile information of a user or creator */
    public void request_profile(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new REQUEST_PROFILE request");
        try {
            String username = (String) input.readObject();
            boolean is_creator = (boolean)input.readObject();
            //FOR ALL USERS: Full name, email,phone number, description, photo
            String query = "SELECT * FROM UserInfo WHERE username= \'" + username + "\';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            if (res.next()) {
                System.out.println("Got all information for this user.");
                ArrayList<String> info = new ArrayList<String>();
                info.add(res.getString("fullname"));
                info.add(res.getString("email"));
                info.add(res.getString("phoneNumber"));
                info.add(res.getString("description"));
                String photo = res.getString("photo");
                output.writeObject(info); //send basic info
                //TODO send photo too
                output.flush();
            }

            //FOR CUSTOMERS: list of all evaluations they've posted
            if(!is_creator) {
                query = "SELECT * FROM Evaluation WHERE submitted_by= \'" + username + "\' ORDER BY date;";
                stm = db_connect.createStatement();
                res = stm.executeQuery(query);
                ArrayList<Evaluation> evaluations =new ArrayList<Evaluation>();
                while(res.next()) {
                    //TODO create a list of Evaluations
                }
            }
            //TODO FOR CREATORS: list of all listings they've posted, list of all evaluations
            else {
                //evaluations
                query = "SELECT * FROM Evaluation WHERE refers_to= \'" + username + "\' ORDER BY date;";
                stm = db_connect.createStatement();
                res = stm.executeQuery(query);
                ArrayList<Evaluation> evaluations =new ArrayList<Evaluation>();
                while(res.next()) {
                    //TODO create a list of Evaluations
                }
                //listings
                //evaluations
                query = "SELECT * FROM Listing WHERE published_by= \'" + username + "\' ;"; //TODO orderby date
                stm = db_connect.createStatement();
                res = stm.executeQuery(query);
                ArrayList<Listing> listings =new ArrayList<Listing>();
                while(res.next()) {
                    //TODO create a list of Listings
                }
            }
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process login request");
            e.printStackTrace();
        }
    }//request_profile


    /** VIEW REWARDS
     * A list of all rewards a creator offers */
    public void view_rewards(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new VIEW_REWARDS request");
        try {
            String username = (String) input.readObject();
            boolean is_creator = (boolean)input.readObject();
            String query = "SELECT * FROM UserInfo WHERE username= \'" + username + "\';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            ArrayList<Reward> rewards =new ArrayList<Reward>();
            while(res.next()) {
                //TODO create a list of rewards
                int price_in_points = res.getInt("price_in_points");
                int id = res.getInt("id");
                String name = res.getString("name");
                String photo = res.getString("photo");
                rewards.add(new Reward(id,price_in_points,name,photo,username));
            }
            output.writeObject(rewards);
            output.flush();

            //if the user is not the creator, get number of points for this creator
            if(!is_creator) {
                //get the username of the customer
                String customer = (String)input.readObject();
                query = "SELECT * FROM RewardPoint WHERE creator= \'" + username + "\' AND client = \'" + customer + "\' ;";
                stm = db_connect.createStatement();
                res = stm.executeQuery(query);
                int points = 0;
                if(res.next()) {
                    points = res.getInt("points");
                }
                output.writeObject(points);
                output.flush();
            }
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process login request");
            e.printStackTrace();
        }
    }//view rewards


    /** ADD REWARD
     * Creator adds a new reward */
    public void add_reward(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new ADD_REWARD request");
        try {
            Reward reward = (Reward)input.readObject(); //android client sends a Reward object with all the info for this Reward
            //add the reward to the database
            String query = "INSERT INTO Reward (id,name,price_in_points,photo,offered_by) "+
                    "VALUES(\'"+reward.getId()+"\',"+reward.getName()+",\'"+reward.getPrice()+"\',\'"+reward.getPhoto()+"\',\'"+reward.getOffered_by()+"\');";
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process login request");
            e.printStackTrace();
        }
    }//add reward


    public static void main(String[] args) {
        new Server("192.168.2.2",6500,100);
    }
}