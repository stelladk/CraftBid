package com.craftbid.craftbid;

import com.amazonaws.services.s3.AmazonS3;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;

//TODO add replies to all requests where the client sends a message last (server must be the last to message)
//TODO check for quotes in numeric values in queries

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
                case "CHANGE_PROFILE_IMAGE":
                    change_profile_picture(input,output);
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
                    create_evaluation(db_connect,input,output);
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
                    request_locations(db_connect,input,output);
                    break;
                case "REQUEST_CATEGORIES":
                    request_categories(db_connect,input,output);
                    break;
                case "REQUEST_EXPERTISES":
                    request_expertises(db_connect,input,output);
                    break;
                case "CREATE_PURCHASE":
                    create_purchase(db_connect,input,output);
                    break;
                case "SEND_NOTIFICATION":
                    send_notification(db_connect,input,output);
                    break;
                case "REQUEST_NOTIFICATIONS":
                    request_notifications(db_connect,input,output);
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
            String query = "SELECT * FROM UserInfo WHERE username= '" + username + "';";
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
                    query = "SELECT * FROM Creator WHERE username= '" + username + "';";
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
            String query = "SELECT * FROM UserInfo WHERE username= '"+username+"';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            if(res.next()) {
                System.out.println("Username already exists!");
                output.writeObject("USER ALREADY EXISTS");
                output.flush();
            }else {
                //check if email already exists
                query = "SELECT * FROM UserInfo WHERE email= '"+email+"';";
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
                    query = "INSERT INTO UserInfo (username,password,fullname,email,phoneNumber,description) "+
                            "VALUES('"+username+"','"+password+"','"+fullname+"','"+email+"'," +
                            (phone.equals("NULL")?phone : "'"+phone+"'")+","+
                            (desc.equals("NULL")?desc : "'"+desc+"'")+");"; //insert nulls to table only if user sent "NULL"
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
                                "VALUES('"+username+"',"+bit+",'"+phone+"','"+expertise+"');";
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
            while(res.next()) { //TODO get more (maybe add index of last loaded)
                //create a list of listing thumbnails
                int id = res.getInt("id");
                String name =  res.getString("name");
                String desc = res.getString("description");
                String category = res.getString("category");
                float min_price = res.getFloat("min_price");
                //get the listing thumbnail from the bucket and add it to thumbnail object as byte array
                byte[] thumbnail = null;
                String filepath = res.getString("published_by")+"/"+name+"/thumbnail.jpeg";
                AmazonS3 connect = S3Bucket.connectToBucket(); //connect to bucket
                S3Bucket.getFromFolder(filepath,connect,"temp/"+name+"thumbnail.jpeg");
                //read file from temp folder and convert to byte array
                File f2 = new File("temp/"+name+"thumbnail.jpeg");
                FileInputStream in = new FileInputStream(f2);
                thumbnail = new byte[(int) f2.length()];
                int error = in.read(thumbnail);
                listing_thumbnails.add(new Thumbnail(id,name,desc,category,min_price,thumbnail));
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
            String query = "SELECT * FROM Listing WHERE name LIKE '%"+search_text+"%' OR published_by LIKE '%"+search_text+"%' OR category LIKE '%"+search_text+"%';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            ArrayList<Thumbnail> listing_thumbnails =new ArrayList<Thumbnail>();
            while(res.next()) { //TODO get more (maybe add index of last loaded)
                //create a list of listing thumbnails
                int id = res.getInt("id");
                String name =  res.getString("name");
                String desc = res.getString("description");
                String category = res.getString("category");
                float min_price = res.getFloat("min_price");
                //get the listing thumbnail from the bucket and add it to thumbnail object as byte array
                byte[] thumbnail = null;
                String filepath = res.getString("published_by")+"/"+name+"/thumbnail.jpeg";
                AmazonS3 connect = S3Bucket.connectToBucket(); //connect to bucket
                S3Bucket.getFromFolder(filepath,connect,"temp/"+name+"thumbnail.jpeg");
                //read file from temp folder and convert to byte array
                File f2 = new File("temp/"+name+"thumbnail.jpeg");
                FileInputStream in = new FileInputStream(f2);
                thumbnail = new byte[(int) f2.length()];
                int error = in.read(thumbnail);
                listing_thumbnails.add(new Thumbnail(id,name,desc,category,min_price,thumbnail));
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
            String query = "SELECT * FROM UserInfo WHERE username= '" + username + "';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            if (res.next()) {
                System.out.println("Got all information for this user.");
                ArrayList<String> info = new ArrayList<String>();
                info.add(res.getString("fullname"));
                info.add(res.getString("email"));
                info.add(res.getString("phoneNumber"));
                info.add(res.getString("description"));
                output.writeObject(info); //send basic info
                output.flush();
                //get the profile pic of user from the bucket and send it to client as byte array
                byte[] pfp = null;
                String filepath = username+"/"+username+"_pfp";
                AmazonS3 connect = S3Bucket.connectToBucket(); //connect to bucket
                S3Bucket.getFromFolder(filepath,connect,"temp/"+username+".jpeg");
                //read file from temp folder and convert to byte array
                File f2 = new File("temp/"+username+".jpeg");
                FileInputStream in = new FileInputStream(f2);
                pfp = new byte[(int) f2.length()];
                int error = in.read(pfp);
                output.writeObject(pfp);
                output.flush();
            }

            //FOR CUSTOMERS: list of all evaluations they've posted
            if(!is_creator) {
                query = "SELECT * FROM Evaluation WHERE submitted_by= '" + username + "' ORDER BY date;";
                stm = db_connect.createStatement();
                res = stm.executeQuery(query);
                ArrayList<Evaluation> evaluations =new ArrayList<Evaluation>();
                //create a list of Evaluations
                while(res.next()) {
                    int id = res.getInt("id");
                    String refers_to = res.getString("refers_to");
                    int rating = res.getInt("rating");
                    String date = res.getString("date");
                    String comment = res.getString("comment");
                    //for customers, the image of the evaluation is their own image
                    evaluations.add(new Evaluation(id,username,refers_to,rating,date,comment));
                }
                output.writeObject(evaluations);
                output.flush();
            }

            //FOR CREATORS: list of all listings they've posted, list of all evaluations
            else {
                //evaluations
                query = "SELECT * FROM Evaluation WHERE refers_to= '" + username + "' ORDER BY date;";
                stm = db_connect.createStatement();
                res = stm.executeQuery(query);
                ArrayList<Evaluation> evaluations =new ArrayList<Evaluation>();
                //create a list of Evaluations
                while(res.next()) {
                    int id = res.getInt("id");
                    String submitted_by = res.getString("submitted_by");
                    int rating = res.getInt("rating");
                    String date = res.getString("date");
                    String comment = res.getString("comment");

                    //get customer's profile picture from the bucket
                    byte[] pfp = null;
                    String filepath = res.getString("submitted_by")+"/"+res.getString("submitted_by")+"_pfp";
                    AmazonS3 connect = S3Bucket.connectToBucket(); //connect to bucket
                    S3Bucket.getFromFolder(filepath,connect,"temp/"+res.getString("submitted_by")+".jpeg");
                    //read file from temp folder and convert to byte array
                    File f2 = new File("temp/"+res.getString("submitted_by")+".jpeg");
                    FileInputStream in = new FileInputStream(f2);
                    pfp = new byte[(int) f2.length()];
                    int error = in.read(pfp);

                    //create new evaluation object
                    Evaluation temp = new Evaluation(id,submitted_by,username,rating,date,comment);
                    temp.setThumbnail(pfp);
                    evaluations.add(temp);
                }
                output.writeObject(evaluations);
                output.flush();

                //listings
                query = "SELECT * FROM Listing WHERE published_by= '" + username + "' ORDER BY date_published ;";
                stm = db_connect.createStatement();
                res = stm.executeQuery(query);
                ArrayList<Thumbnail> thumbnails =new ArrayList<Thumbnail>();
                //create a list of Listing Thumbnails
                while(res.next()) {
                    int id = res.getInt("id");
                    String name =  res.getString("name");
                    String desc = res.getString("description");
                    String category = res.getString("category");
                    float min_price = res.getFloat("min_price");
                    //get the listing thumbnail from the bucket and add it to thumbnail object as byte array
                    byte[] thumbnail = null;
                    String filepath = username+"/"+name+"/thumbnail.jpeg";
                    AmazonS3 connect = S3Bucket.connectToBucket(); //connect to bucket
                    S3Bucket.getFromFolder(filepath,connect,"temp/"+name+"thumbnail.jpeg");
                    //read file from temp folder and convert to byte array
                    File f2 = new File("temp/"+name+"thumbnail.jpeg");
                    FileInputStream in = new FileInputStream(f2);
                    thumbnail = new byte[(int) f2.length()];
                    int error = in.read(thumbnail);
                    thumbnails.add(new Thumbnail(id,name,desc,category,min_price,thumbnail));
                }
                output.writeObject(thumbnails);
                output.flush();
            }
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process request profile request");
            e.printStackTrace();
        }
    }//request_profile


    /** UPDATE PROFILE
     * Modify a UserInfo or Creator's field in the database */
    public void update_profile(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new UPDATE_PROFILE request");
        try {
            String username = (String)input.readObject(); //the username
            String field = (String)input.readObject(); //the column to be changed
            String new_val = (String)input.readObject(); //the new value
            String query = null;
            Statement stm;
            ResultSet res;
            boolean mail_ok = true;
            if(field.equals("isFreelancer")) { //sent value is either 0 or 1
                query = "UPDATE Creator SET isFreelancer= "+new_val+"WHERE username= '"+username+"';";
            }else if(field.equals("fullname") || field.equals("email") || field.equals("phoneNumber") || field.equals("description")){
                if(field.equals("email")) {
                    //check if new email already exists
                    query = "SELECT * FROM UserInfo WHERE email= '"+new_val+"';";
                    stm = db_connect.createStatement();
                    res = stm.executeQuery(query);
                    if(res.next()) {
                        System.out.println("Email already exists!");
                        mail_ok=false;
                    }
                }
                query = "UPDATE UserInfo SET "+field+" = '"+new_val+"' WHERE username= '"+username+"';";
            }else {
                System.out.println("Field "+field+" cannot be edited in a user's info");
            }
            if(mail_ok) {
                //update the value in the database
                stm = db_connect.createStatement();
                stm.executeUpdate(query);
            }
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process update profile request");
            e.printStackTrace();
        }
    }//update profile


    /** CHANGE PROFILE IMAGE
     * Change the profile picture of a user in the bucket */
    public void change_profile_picture(ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new CHANGE_PROFILE_PICTURE request");
        try {
            String username = (String)input.readObject(); //the username
            byte[] new_img = (byte[])input.readObject(); // the new image
            //TODO replace image in bucket with new image
        }catch(IOException | ClassNotFoundException e) {
            System.err.println("Unable to process change profile picture request");
            e.printStackTrace();
        }
    }//change profile picture


    /** CREATE LISTING
     * Creator publishes a new listing for a handmade product */
    public void create_listing(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new CREATE_LISTING request");
        try {
            Listing listing = (Listing)input.readObject(); //android client sends a Listing object with all the info for this Listing
            //add the listing to the database
            String query = "INSERT INTO Listing(name,description,category,min_price,reward_points,quantity,is_located,published_by,date_published,delivery,total_photos) "+
                    "VALUES('"+listing.getName()+"','"+listing.getDescription()+"','"
                    +listing.getCategory()+"',"+listing.getMin_price()+","+listing.getReward_points()+","
                    +listing.getQuantity()+",'"+listing.getLocation()+"','"+listing.getPublished_by()+"','"
                    +listing.getDatePublished()+"','"+listing.getDelivery()+"',"+listing.getTotal_photos()+");";
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
            //add thumbnail to bucket (receive as byte array from client) !!!! photo is never null in a listing
            byte[] thumbnail = (byte[])input.readObject();
            if(thumbnail!= null) {
                String bucket_path = listing.getPublished_by()+"/"+listing.getName()+"/thumbnail.jpeg";
                //put thumbnail to the bucket as "username/listing_name/thumbnail.jpeg"
                File f2 = new File("temp/"+listing.getName()+"thumbnail.jpeg"); //write to temp file
                FileOutputStream out = new FileOutputStream(f2);
                out.write(thumbnail);
                out.close();
                AmazonS3 connect = S3Bucket.connectToBucket(); //connect to bucket and write file inside
                S3Bucket.addToFolder(bucket_path,f2,connect);
                System.out.println("Added image to bucket!");
            }
            for(int i = 0; i < listing.getTotal_photos(); i++) {
                //TODO get the rest of the photos and add them to bucket
            }
            output.writeObject("LISTING CREATION SUCCESSFUL");
            output.flush();
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
            String query = "SELECT * FROM Listing WHERE id=" + id + ";";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            if (res.next()) {
                System.out.println("Got all information for this listing.");
                Listing listing = new Listing(id,res.getString("name"),res.getString("description"), res.getString("category"),
                                              res.getString("published_by"),res.getString("is_located"),
                                              res.getInt("reward_points"),res.getInt("quantity"),res.getFloat("min_price"),
                                              res.getDate("date_published").toString(),res.getString("delivery"),res.getInt("total_photos"));
                output.writeObject(listing); //send basic info
                output.flush();

                ArrayList<byte[]> photos = new ArrayList<byte[]>();
                for(int i = 0; i < listing.getTotal_photos(); i++) {
                    //TODO get all the photos from bucket and send them to client as byte arrays
                }
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
            String query = "INSERT INTO Offer(price,submitted_by,submitted_for) "+
                    "VALUES("+offer.getPrice()+",'"+ offer.getSubmitted_by()+"',"+offer.getSubmitted_for()+");";
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


    /** CREATE EVALUATION
     * Customer submits a new evaluation for a creator */
    public void create_evaluation(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new CREATE_EVALUATION request");
        try {
            Evaluation evaluation = (Evaluation)input.readObject(); //android client sends an Evaluation object with all the info for this Evaluation
            //add the evaluation to the database
            String query = "INSERT INTO Evaluation(submitted_by,refers_to,rating,date,comment) "+
                    "VALUES('"+evaluation.getSubmitted_by()+"','"+evaluation.getRefers_to()+"',"
                    +evaluation.getRating()+",'"+evaluation.getDate()+"','"+evaluation.getComment()+"');";
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process create evaluation request");
            e.printStackTrace();
        }
    }//create evaluation


    /** CREATE REPORT
     * Customer submits a new report for a creator */
    public void create_report(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new CREATE_REPORT request");
        try {
            Report report = (Report)input.readObject(); //android client sends a Report object with all the info for this Report
            //add the report to the database
            String query = "INSERT INTO Report(submitted_by,refers_to,reason,date,description) "+
                    "VALUES('"+report.getSubmitted_by()+"','"+report.getRefers_to()+"','"
                    +report.getReason()+"','"+report.getDate()+"','"+report.getDescription()+"');";
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
            String query = "SELECT * FROM UserInfo WHERE username= '" + username + "';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            ArrayList<Reward> rewards =new ArrayList<Reward>();
            while(res.next()) {
                int price_in_points = res.getInt("price_in_points");
                int id = res.getInt("id");
                String name = res.getString("name");
                //TODO get the reward thumbnail from bucket
                byte[] thumbnail = null;
                rewards.add(new Reward(id,price_in_points,name,username,thumbnail));
            }
            output.writeObject(rewards);
            output.flush();

            //if the user is not the creator, get number of points for this creator
            if(!is_creator) {
                //get the username of the customer
                String customer = (String)input.readObject();
                query = "SELECT * FROM RewardPoint WHERE creator= '" + username + "' AND client = '" + customer + "' ;";
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
            String query = "INSERT INTO Reward (name,price_in_points,offered_by) "+
                    "VALUES('"+reward.getName()+"',"+reward.getPrice()+",'"+reward.getOffered_by()+"');";
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
            //TODO get the reward thumbnail as byte array and add to bucket
        }catch(IOException | ClassNotFoundException| SQLException e) {
            System.err.println("Unable to process add reward request");
            e.printStackTrace();
        }
    }//add reward


    /** REQUEST LOCATIONS
     * Return a list of all locations */
    public void request_locations(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new REQUEST_LOCATIONS request");
        try {
            //get a list of all locations
            String query = "SELECT * FROM Location;";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            ArrayList<String> locations =new ArrayList<String>();
            while(res.next()) {
                //create a list of locations
                String name = res.getString("name");
                locations.add(name);
            }
            output.writeObject(locations); //send locations
            output.flush();
        }catch(IOException | SQLException e) {
            System.err.println("Unable to process request locations request");
            e.printStackTrace();
        }
    }//request locations


    /** REQUEST CATEGORIES
     * Return a list of all PRODUCT categories*/
    public void request_categories(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new REQUEST_CATEGORIES request");
        try {
            //get a list of all categories
            String query = "SELECT * FROM Category;";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            ArrayList<String> categories =new ArrayList<String>();
            while(res.next()) {
                //create a list of categories
                String name = res.getString("name");
                categories.add(name);
            }
            output.writeObject(categories); //send categories
            output.flush();
        }catch(IOException | SQLException e) {
            System.err.println("Unable to process request categories request");
            e.printStackTrace();
        }
    }//request categories


    /** REQUEST EXPERTISES
     * Return a list of all CREATOR categories */
    public void request_expertises(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new REQUEST_EXPERTISES request");
        try {
            //get a list of all expertises
            String query = "SELECT * FROM Expertise;";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            ArrayList<String> expertises =new ArrayList<String>();
            while(res.next()) {
                //create a list of expertises
                String name = res.getString("name");
                expertises.add(name);
            }
            output.writeObject(expertises); //send expertises
            output.flush();
        }catch(IOException | SQLException e) {
            System.err.println("Unable to process request expertises request");
            e.printStackTrace();
        }
    }//request expertises


    /** CREATE PURCHASE
     * Add a new purchase to database */
    public void create_purchase(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new CREATE_PURCHASE request");
        try {
            Purchase purchase = (Purchase)input.readObject(); //android client sends a Purchase object with all the info for this Purchase
            //add the reward to the database
            String query = "INSERT INTO Purchase (done_by,done_on,date) "+
                    "VALUES('"+purchase.getDone_by()+"',"+purchase.getDone_on()+",'"+purchase.getDate()+"');";
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process create purchase request");
            e.printStackTrace();
        }
    }//create purchase


    /** SEND NOTIFICATION
     * send a new notification to customer when creator acceptS an offer (BY STORING IT TO DB)
     * also delete all other offers */
    public void send_notification(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new SEND_NOTIFICATION request");
        try {
            Notification notification = (Notification)input.readObject(); //android client sends a Notification object with all the info for this Notification
            //add the reward to the database
            String query = "INSERT INTO Notification (listing_id,belongs_to,price) "+
                    "VALUES("+notification.getListing_id()+",'"+notification.getBelongs_to()+"',"+notification.getPrice()+");";
            Statement stm = db_connect.createStatement();
            stm.executeUpdate(query);
            //TODO delete all other offers for this listing id
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process send notification request");
            e.printStackTrace();
        }
    }//send notification


    /** REQUEST NOTIFICATIONS
     * Return a list of all notifications given a username */
    public void request_notifications(Connection db_connect, ObjectInputStream input, ObjectOutputStream output) {
        System.out.println("Received a new REQUEST_NOTIFICATIONS request");
        try {
            String username = (String)input.readObject();
            //get a list of all expertises
            String query = "SELECT * FROM Notification WHERE belongs_to='"+username+"';";
            Statement stm = db_connect.createStatement();
            ResultSet res = stm.executeQuery(query);
            ArrayList<Notification> notifications =new ArrayList<Notification>();
            while(res.next()) {
                //create a list of notifications
                int listing_id = res.getInt("listing_id");
                float price = res.getFloat("price");
                notifications.add(new Notification(listing_id,username,price));
            }
            output.writeObject(notifications); //send notifications
            output.flush();
        }catch(IOException | ClassNotFoundException | SQLException e) {
            System.err.println("Unable to process request notifications request");
            e.printStackTrace();
        }
    }//request notifications


    public static void main(String[] args) {
        new Server("192.168.2.2",6500,100);
    }
}