

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import util.ClusteredTuple;
import util.ContactClusters;
import util.Key;
import util.Tuple;

/**
 * Servlet implementation class InfectionReporting
 */
@WebServlet("/InfectionReporting")
public class InfectionReporting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static long tsDuration=10;
	
	 private static BloomFilter<String> filter;
    
	 
	 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InfectionReporting() {
    	 super();
    	//for test...fill bloom filter
    
       
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(request.getParameter("fill")!=null){
			 filter=BloomFilter.create(
			         Funnels.stringFunnel(
			             Charset.forName("UTF-8")),
			         800000000,0.00000001);
			for(int i=0; i<1000000;i++) {
	    		filter.put(UUID.randomUUID().toString());
	    	}
			
	    	System.out.println("Finish filling");
	    	return;
		}
		
		
		Gson gson = new Gson();
		String json=new String(request.getInputStream().readAllBytes());
		Type collectionType = new TypeToken<List<Tuple>>(){}.getType();
        List<Tuple> tuples = gson.fromJson(json, collectionType);
        try {
        	
		  	
			List<Tuple> list=query(tuples);
			

			 for(Tuple t: list) {
				filter.put(t.getEphemeralId()); 
			 }
			 
		
	        
	  
	     
	       
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	
	private static List<Tuple> query(List<Tuple> list) throws SQLException {
		
		DriverManager.registerDriver(new com.mysql.jdbc.Driver ());
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb","root","root");
		PreparedStatement ps = null;
		List<Tuple> results= new LinkedList<Tuple>();
		
		String digests="";
		for(Tuple t: list) {
			digests=digests+"'"+t.getDigest()+"'"+",";
		}
		digests=digests+"'b'";
		try {
			String sql = "SELECT * FROM CONTACT_TUPLES WHERE  DIGEST in ("+digests+")";
			
			ps = conn.prepareStatement(sql);

		  
		  	ResultSet rs=ps.executeQuery();
        	
			
			while (rs.next()) {
				
				Tuple t= new Tuple(rs.getString("Ephemeral"),"",23.2,53.2,13442);
				
				  results.add(t);
				}

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		finally {
			try {
				
				ps.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return results;
	} 
	
}
