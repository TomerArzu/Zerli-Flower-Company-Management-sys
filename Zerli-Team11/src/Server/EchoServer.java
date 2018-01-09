/************************************************************************** 
 * Copyright (�) Zerli System 2017-2018 - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Edo Notes <Edoono24@gmail.com>
 * 			  Tomer Arzuan <Tomerarzu@gmail.com>
 * 			  Matan Sabag <19matan@gmail.com>
 * 			  Ido Kalir <idotehila@gmail.com>
 * 			  Elinor Faddoul<elinor.faddoul@gmail.com
 **************************************************************************/
package Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeMap;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.io.*;
import Entities.*;
import common.*;
import ocsf.server.AbstractServer;
//import ocsf.server.*;
import ocsf.server.ConnectionToClient;

public class EchoServer extends AbstractServer {
	/*
	 * Attributes Area
	 * =============================================================================
	 * ==
	 */
	/**
	 * The default port and host to listen on.
	 */
	final public static int DEFAULT_PORT = 5555;
	final public static String HOST = "localhost";

	/*
	 * Methods Area
	 * =============================================================================
	 * ==
	 */

	public EchoServer(int port) {
		super(port);
	}

	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try {
			Msg msgRecived = (Msg) msg;
			Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/zerli", "root", "root");
			System.out.println("SQL connection succeed 222");
			if ((msgRecived.getClassType()).equalsIgnoreCase("User")) {
				userHandeler(msgRecived, "user", client, conn);
			}
			else if ((msgRecived.getClassType()).equalsIgnoreCase("survey")) {
				SurveyHandeler(msgRecived, "survey", client, conn);
			}
				else if ((msgRecived.getClassType()).equalsIgnoreCase("report")) {
					get_order_report(msgRecived, conn, client);
				}
		} catch (SQLException ex) {/* handle any errors */
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public static void userHandeler(Object msg, String tableName, ConnectionToClient client, Connection con) {
		String queryToDo = ((Msg) msg).getQueryQuestion();
		Msg requestMsg = (Msg) msg;
		if (requestMsg.getqueryToDo().compareTo("checkUserExistence") == 0)  // If we want to check if user is exist
			searchUserInDB(msg, tableName, client, con);  // e.g to logIn
		else if(requestMsg.getqueryToDo().compareTo("update user")==0)
			updateUserDetails(msg,tableName,client,con);
			else if(requestMsg.getqueryToDo().compareTo("AddNewUserToDB")==0) 
					AddNewUser(msg,tableName,client,con);				

	}// userHandler
	
	/*
	 * 
	 * =============================================================================
	 * ==
	 */
	
	public static void SurveyHandeler(Object msg, String tableName, ConnectionToClient client, Connection con) {
		String queryToDo = ((Msg) msg).getQueryQuestion();
		Msg requestMsg = (Msg) msg;
		if (requestMsg.getqueryToDo().compareTo("SendSurveyToDB") == 0) // If we want to check if user is exist																	// e.g to logIn
			InsertSurveyToDB(msg, tableName, client, con);

	}// SurveyHandler
	
	

	private static void updateUserDetails(Object msg, String tableName, ConnectionToClient client, Connection con) {
		User userToUpdate = (User) (((Msg) msg).getSentObj());
		Msg message=(Msg)msg;
		try {
			PreparedStatement stmt=con.prepareStatement(message.getQueryQuestion()+" zerli."+tableName+" Set "+message.getColumnToUpdate()+"= ? WHERE UserName='"+userToUpdate.getUserName()+"'");
			stmt.setString(1, message.getValueToUpdate());
			stmt.executeUpdate();
			con.close();
		} catch (SQLException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void searchUserInDB(Object msg, String tableName, ConnectionToClient client, Connection con) {
		User toSearch = (User) (((Msg) msg).getSentObj());
		User tmpUsr = new User();
		try {
			Statement stmt = con.createStatement();
			// Case 1: Username and Password Correct

			ResultSet rs = stmt.executeQuery(((Msg) msg).getQueryQuestion() + " FROM " + tableName + " WHERE UserName='"
					+ toSearch.getUserName() + "' AND Password='" + toSearch.getPassword() + "';");
			if (rs.next()) {

				tmpUsr.setUserName(rs.getString(1));// Set user name for returned object
				tmpUsr.setPassword(rs.getString(2));// Set Password for returned object
				tmpUsr.setID(Integer.parseInt(rs.getString(3)));// Set ID for returned object
				tmpUsr.setFirstName(rs.getString(4));// Set FirstName for returned object
				tmpUsr.setLastName(rs.getString(5));// Set tLastName for returned object
				tmpUsr.setConnectionStatus(rs.getString(6));// Set ConnectionStatus for returned object
				tmpUsr.setUserType(rs.getString(7));// Set UserType for returned object
				tmpUsr.setPhone(rs.getString(8));// Set Phone for returned object
				tmpUsr.setGender(rs.getString(9));// Set Gender for returned object
				tmpUsr.setEmail(rs.getString(10));// Set Email for returned object
			}
			// Case 2 :UserName Correct But Password Wrong
			else if (rs.next() == false) {
				rs = stmt.executeQuery(((Msg) msg).getQueryQuestion() + " FROM " + tableName + " WHERE UserName='"
						+ toSearch.getUserName()+"';");
				if (rs.next()) {
					tmpUsr.setUserName(rs.getString(1));// Set user name for returned object
					tmpUsr.setPassword(rs.getString(2));// Set Password for returned object
					tmpUsr.setID(Integer.parseInt(rs.getString(3)));// Set ID for returned object
					tmpUsr.setFirstName(rs.getString(4));// Set FirstName for returned object
					tmpUsr.setLastName(rs.getString(5));// Set tLastName for returned object
					tmpUsr.setConnectionStatus(rs.getString(6));// Set ConnectionStatus for returned object
					tmpUsr.setUserType(rs.getString(7));// Set UserType for returned object
					tmpUsr.setPhone(rs.getString(8));// Set Phone for returned object
					tmpUsr.setGender(rs.getString(9));// Set Gender for returned object
					tmpUsr.setEmail(rs.getString(10));// Set Email for returned object
				}
				
			}
			rs.close();

			con.close();
			System.out.println(tmpUsr);// works

			((Msg) msg).setReturnObj(tmpUsr);

			try {
				client.sendToClient(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}
	
	/*
	 * Methods that insert a new survey to DB
	 * =============================================================================
	 * ==
	 */
	
	public static void InsertSurveyToDB(Object msg, String tableName, ConnectionToClient client, Connection con) {
		Survey surveyDB = (Survey) (((Msg) msg).getSentObj());
		Msg message=(Msg)msg;
		try {

			//ResultSet rs =stmt.executeQuery("SELECT MAX(SurveyNum) FROM "+ tableName); ���� ������..
			
			PreparedStatement stmt=con.prepareStatement(message.getQueryQuestion()+" " + tableName+" ("+"SurveyNum ,question1 ,question2 ,question3 , question4 ,question5 ,question6 ,answer1 ,answer2 ,answer3 ,answer4 ,answer5 ,answer6 )"
			+ "\nVALUES "+"('" + Survey.NumSurvey +"','" +
			surveyDB.getQuestion1() + "','" +  surveyDB.getQuestion2() + "','" + surveyDB.getQuestion3()  + "','" + surveyDB.getQuestion4() + "','" +
			surveyDB.getQuestion5() + "','" + surveyDB.getQuestion6() + "','" +surveyDB.getAnswer1() + "','" +surveyDB.getAnswer2() + "','" +
			surveyDB.getAnswer3()  + "','" +surveyDB.getAnswer4() + "','" +surveyDB.getAnswer5() + "','" +surveyDB.getAnswer6()+"');");
			
			stmt.executeUpdate();

			Survey.NumSurvey++;
			con.close();
			try {
				client.sendToClient(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}
	
	/*
	 * Method that adding new user to DB
	 * =============================================================================
	 * ==
	 */
	
	public static void AddNewUser(Object msg, String tableName, ConnectionToClient client, Connection con) {
		User NewUserToAdd = (User) (((Msg) msg).getSentObj());
		Msg message=(Msg)msg;
		try {
							
			PreparedStatement stmt=con.prepareStatement(message.getQueryQuestion()+" " + tableName  + " ("+ "UserName ,Password ,ID ,FirstName ,LastName ,ConnectionStatus ,Premission ,Phone ,Gender ,Email)"
					+"\nVALUES "+ "('"+NewUserToAdd.getUserName()+ "','" + NewUserToAdd.getPassword() + "','"  +NewUserToAdd.getID() + "','" + NewUserToAdd.getFirstName() +  "','" +NewUserToAdd.getLastName() 
					+  "','" + NewUserToAdd.getConnectionStatus() +  "','"+ NewUserToAdd.getUserType() +  "','"+NewUserToAdd.getPhone() +  "','" +NewUserToAdd.getGender() +  "','" 
					+ NewUserToAdd.getEmail()+ "');");
			stmt.executeUpdate();
			con.close();
			try {
				client.sendToClient(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());
	}

	public void get_order_report(Msg msg, Connection con, ConnectionToClient client) {
		System.out.println("great" + msg.getQueryQuestion());
		TreeMap<String, String> directory = new TreeMap<String, String>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(msg.getQueryQuestion());

			while (rs.next()) {

				System.out.println(rs.getString(1) + rs.getString(2));
				directory.put(rs.getString(1), rs.getString(2));
			}

			rs.close();
			System.out.println(directory);
			try {
				client.sendToClient(directory);
			} catch (IOException e) {
				e.printStackTrace();
			}
			con.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	// SELECT orders.type,count(*) as count FROM zerli.orders WHERE date BETWEEN
	// '2011-10-01' AND '2011-12-31' and orders.shop = 'Ako' group by orders.type ;

}
