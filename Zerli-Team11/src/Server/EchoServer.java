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
import java.util.Vector;

import com.mysql.jdbc.util.ServerController;
import com.sun.javafx.collections.MappingChange.Map;

import java.sql.Connection;
import java.sql.DriverManager;
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
			if ((msgRecived.getClassType()).equalsIgnoreCase("report")) {
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
		if (requestMsg.getQueryNeedTo().compareTo("checkUserExistence")==0) //If we want to check if user is exist e.g to logIn
			searchUserInDB(msg, tableName, client, con);


	}// userHandler

	public static void searchUserInDB(Object msg, String tableName, ConnectionToClient client, Connection con) {
		User toSearch = (User) (((Msg) msg).getSentObj());
		User tmpUsr = new User();
		try {
			Statement stmt = con.createStatement();
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
	
	
	public void get_order_report(Msg msg, Connection con, ConnectionToClient client)
	{   
		System.out.println("great" + msg.getQueryQuestion());
		TreeMap<String, String> directory = new TreeMap<String, String>();
		try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(msg.getQueryQuestion()); 

				while (rs.next()) {
					
					System.out.println(rs.getString(1)+rs.getString(2));
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
		}
		 catch (SQLException e) {
		System.out.println(e.getMessage());
		}
	}
	
	
	
	//SELECT orders.type,count(*) as count FROM zerli.orders WHERE date BETWEEN '2011-10-01' AND '2011-12-31'  and orders.shop = 'Ako' group by orders.type ;
	

}
