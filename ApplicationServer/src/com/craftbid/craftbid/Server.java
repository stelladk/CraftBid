package com.craftbid.craftbid;

import com.amazonaws.services.s3.AmazonS3;

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
                case "LOGIN":
                    login(db_connect,input,output);
                    break;
                case "SIGNUP_USER":
                    signup(db_connect,input,output);
                    break;
                case "LOAD_MAIN_SCREEN":
                    load_main(db_connect,input,output);
                    break;
                case "SEARCH":
                    search(db_connect,input,output);
                    break;
                case "REQUEST_PROFILE":
                    request_profile(db_connect,input,output);
                    break;
                case "UPDATE_PROFILE":
                    update_profile(db_connect,input,output);
                    break;
                case "CREATE_LISTING":
                    create_listing(db_connect,input,output);
                    break;
                case "VIEW_LISTING":
                    view_listing(db_connect,input,output);
                    break;
                case "UPDATE_LISTING":
                    update_listing(db_connect,input,output);
                    break;
                case "CREATE_BID":
                    add_offer(db_connect,input,output);
                    break;
                case "VIEW_OFFERS":
                    view_offers(db_connect,input,output);
                    break;
                case "DECLINE_OFFER":
                    decline_offer(db_connect,input,output);
                    break;
                case "CREATE_EVALUATION":
                    //TODO add evaluation to database
                    break;
                case "CREATE_REPORT":
                    create_report(db_connect,input,output);
                    break;
                case "VIEW_REWARDS":
                    view_rewards(db_connect,input,output);
                    break;
                case "ADD_REWARD":
                    add_reward(db_connect,input,output);
                    break;
                case "REQUEST_LOCATIONS":
                    //TODO send a list of all locations
                    break;
                case "REQUEST_CATEGORIES":
                    //TODO send a list of all categories
                    break;
                case "CREATE_PURCHASE":
                    //TODO create a new purchase
                    break;
                case "SEND_NOTIFICATION":
                    //TODO send a new notification to customer when creator accept an offer,also delete all other offers
                    break;
                case "REQUEST_NOTIFICATIONS":
                    //TODO return a list of all notifications given a username
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
                    //getting the profile type (customer or creator)
                    String profile = "customer";
                    query = "SELECT * FROM Creator WHERE username= \'" + username + "\';";
                    stm = db_connect.createStatement();
                    res = stm.executeQuery(query);
                    if(res.next()) {
                        profile = "creator";
                    }
                    output.writeObject(profile);
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
            byte[] photo = (byte[])input.readObject(); //send empty byte array if none given
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
                    String bucket_path = "NULL";
                    //insert new tuple to db
                    query = "INSERT INTO UserInfo (username,password,fullname,email,phoneNumber,description,photo) "+
                            "VALUES(\'"+username+"\',\'"+password+"\',\'"+fullname+"\',\'"+email+"\'," +
                            (phone.equals("NULL")?phone : "\'"+phone+"\'")+","+
                            (desc.equals("NULL")?desc : "\'"+desc+"\'")+","+
                            bucket_path+");"; //insert nulls to table only if user sent "NULL"
                    stm.executeUpdate(query);
                    output.writeObject("BASIC REGISTER SUCCESSFUL");
                    output.flush();
                    //add pic to bucket
                    if(photo!=null) {
                        bucket_path = username+"/"+username+"_pfp";
                        //first put profile image to the bucket as "username/username_pfp.png"
                        File f2 = new File("temp/"+username+".jpeg"); //write to temp file
                        FileOutputStream out = new FileOutputStream(f2);
                        out.write(photo);
                        out.close();
                        AmazonS3 connect = S3Bucket.connectToBucket(); //connect to bucket and write file inside
                        S3Bucket.addToFolder(bucket_path,f2,connect);
                        System.out.println("Added image to bucket!");
                    }
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
            System.err.println("Unable to process sign up request");
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
            System.err.println("Unable to process load main screen request");
            e.printStackTrace();
        }
    }//load main screen


    /** SEARCH
     * When a user searches a word, a list of listings' thumbnails appears,
     * if the name, category, or creator username matches */
    public void search(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new SEARCH request");
        try {
            String search_text = (String)input.readObject();
            //get a list of all listings, if name, category, or creator username matches
            String query = "SELECT * FROM Listing WHERE name LIKE \'%"+search_text+"%\' OR published_by LIKE \'%"+search_text+"%\' OR category LIKE \'%"+search_text+"%\';";
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
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process search request");
            e.printStackTrace();
        }
    }//search


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
                query = "SELECT * FROM Listing WHERE published_by= \'" + username + "\' ;"; //TODO orderby date
                stm = db_connect.createStatement();
                res = stm.executeQuery(query);
                ArrayList<Listing> listings =new ArrayList<Listing>();
                while(res.next()) {
                    //TODO create a list of Listings
                }
            }
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process request profile request");
            e.printStackTrace();
        }
    }//request_profile


    /** UPDATE PROFILE
     * Modify a UserInfo's field in the database */
    public void update_profile(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new UPDATE_PROFILE request");
        try {
            String field = (String)input.readObject(); //the column to be changed
            String new_val = (String)input.readObject(); //the new value
            String query = null;
            if(field.equals("isFreelancer")) {
                //TODO change bit
            }else if(field.equals("fullname") || field.equals("email") || field.equals("phoneNumber") || field.equals("description")){
                //TODO change String
            } else {
                System.out.println("Field "+field+" cannot be edited in a user's info");
            }
            //update the value in the database
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process update profile request");
            e.printStackTrace();
        }
    }//update profile


    /** CREATE LISTING
     * Creator publishes a new listing for a handmade product */
    public void create_listing(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new CREATE_LISTING request");
        try {
            Listing listing = (Listing)input.readObject(); //android client sends a Listing object with all the info for this Listing
            //add the listing to the database
            String query = "INSERT INTO Offer(id,name,description,category,min_price,reward_points,quantity,is_located,published_by) "+
                    "VALUES(\'"+listing.getId()+"\',"+listing.getName()+",\'"+listing.getDescription()+"\',\'"
                    +listing.getCategory()+"\',\'"+listing.getMin_price()+"\',\'"+listing.getReward_points()+"\',\'"
                    +listing.getQuantity()+"\',\'"+listing.getLocation()+"\',\'"+listing.getPublished_by()+"\');";
                    //todo id is autoincrement
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
            //TODO get photos and add them to table "Photo"
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process create listing request");
            e.printStackTrace();
        }
    }//create listing


    /** VIEW LISTING
     * View all information of a listing based on the id */
    public void view_listing(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new VIEW_LISTING request");
        try {
            int id = (Integer)input.readObject();
            String query = "SELECT * FROM Listing WHERE id= \'" + id + "\';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            if (res.next()) {
                System.out.println("Got all information for this listing.");
                Listing listing = new Listing(id,res.getString("name"),res.getString("description"), res.getString("category"),
                                              res.getString("published_by"), res.getString("thumbnail"),res.getString("is_located"),
                                              res.getInt("reward_points"),res.getInt("quantity"),res.getFloat("min_price"),res.getDate("date_published"));
                output.writeObject(listing); //send basic info
                output.flush();
                //TODO get all the photos from table "Photo"
            }
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process view listing request");
            e.printStackTrace();
        }
    }//view listing


    /** UPDATE LISTING
     * Modify a listing's field in the database */
    public void update_listing(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new UPDATE_LISTING request");
        try {
            String field = (String)input.readObject(); //the column to be changed
            String new_val = (String)input.readObject(); //the new value
            String query = null;
            if(field.equals("description") || field.equals("name")) {
                //TODO change string
            }else if(field.equals("reward_points") || field.equals("quantity")){
                //TODO change int
            } else {
                System.out.println("Field "+field+" cannot be edited in a listing");
            }
            //update the value in the database
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process update listing request");
            e.printStackTrace();
        }
    }//update listing


    /** CREATE BID
     * Customer submits a new offer for a listing */
    public void add_offer(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new CREATE_BID request");
        try {
            Offer offer = (Offer)input.readObject(); //android client sends an Offer object with all the info for this Offer
            //add the offer to the database
            String query = "INSERT INTO Offer(id,price,submitted_by,submitted_for) "+
                    "VALUES(\'"+offer.getId()+"\',"+offer.getPrice()+",\'"+
                    offer.getSubmitted_by()+"\',\'"+offer.getSubmitted_for()+"\');";
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process create offer request");
            e.printStackTrace();
        }
    }//add offer


    /** VIEW OFFERS
     * Return a list of all offers for a listing */
    public void view_offers(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new VIEW_OFFERS request");
        try {
            int listing_id = (int)input.readObject();
            //get a list of all offers for given listing id
            String query = "SELECT * FROM Offer WHERE submitted_for="+listing_id+";";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            ArrayList<Offer> offers =new ArrayList<Offer>();
            while(res.next()) {
                //create a list of offers
                int id = res.getInt("id");
                float price =  res.getFloat("price");
                String submitted_by = res.getString("submitted_by");
                offers.add(new Offer(id,listing_id,submitted_by,price));
            }
            output.writeObject(offers); //send thumbnails
            output.flush();
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process view offers request");
            e.printStackTrace();
        }
    }//view offer


    /** DECLINE OFFER
     * Decline an offer by removing it from the database*/
    public void decline_offer(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new DECLINE_OFFER request");
        try {
            int id = (int)input.readObject();
            //get the offer from the database
            String query = "DELETE FROM Offer WHERE id="+id+";";
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);

        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process decline offer request");
            e.printStackTrace();
        }
    }//decline offer


    /** CREATE REPORT
     * Customer submits a new report for a creator */
    public void create_report(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new CREATE_REPORT request");
        try {
            Report report = (Report)input.readObject(); //android client sends a Report object with all the info for this Report
            //add the report to the database
            String query = "INSERT INTO Report(id,submitted_by,refers_to,reason,date,description) "+
                    "VALUES(\'"+report.getId()+"\',"+report.getSubmitted_by()+",\'"+report.getRefers_to()+"\',\'"
                    +report.getReason()+"\',\'"+report.getDate()+"\',\'"+report.getDescription()+"\');";
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process create report request");
            e.printStackTrace();
        }
    }//create report

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
            System.err.println("Unable to process view rewards request");
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
            System.err.println("Unable to process add reward request");
            e.printStackTrace();
        }
    }//add reward


    public static void main(String[] args) {
        new Server("192.168.2.2",6500,100);
    }
}