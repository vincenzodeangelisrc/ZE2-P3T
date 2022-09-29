

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.ClusteredTuple;
import util.ContactClusters;
import util.Key;

/**
 * Servlet implementation class RestrictedTuplesReport
 */
@WebServlet("/RestrictedTuplesReport")
public class RestrictedTuplesReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static long tsDuration=300;

	private static int currentCluster=0;


	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RestrictedTuplesReport() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


		String eph=request.getParameter("ephemeralID");
		String digest=request.getParameter("digest");
		long Rectimestamp=Long.parseLong(request.getParameter("timestamp"));

		if(eph.equals("count")) { 
			long in=System.nanoTime();

			
			ConcurrentHashMap<Key, Set<ClusteredTuple>> contactCluster=null;
			if(currentCluster==0) {
				contactCluster=ContactClusters.contactCluster0;}
			if(currentCluster==1) {
				contactCluster=ContactClusters.contactCluster1;}
			if(currentCluster==2) {
				contactCluster=ContactClusters.contactCluster2;}	
			currentCluster=(currentCluster+1)%3;
			try {
				insertDB(contactCluster);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long fin=System.nanoTime();
			System.out.println("Processing time Server (ms):"+ (fin-in)/1000000);
			return;}



		long timeslot=Rectimestamp/tsDuration;
		ClusteredTuple ct= new ClusteredTuple(eph,digest,timeslot);
		Key k= new Key(timeslot,digest);

		Set<ClusteredTuple> set=null;
		if(currentCluster==0) {
			set=ContactClusters.contactCluster0.get(k);}
		if(currentCluster==1) {
			set=ContactClusters.contactCluster1.get(k);}
		if(currentCluster==2) {
			set=ContactClusters.contactCluster2.get(k);}

		if(set==null) {


			set=ConcurrentHashMap.newKeySet();
			set=new HashSet<ClusteredTuple>();
			if(currentCluster==0) {
				ContactClusters.contactCluster0.put(k, set);}
			if(currentCluster==1) {
				ContactClusters.contactCluster1.put(k, set);}
			if(currentCluster==2) {
				ContactClusters.contactCluster2.put(k, set);}		
			set.add(ct);

		}
		else {			
			set.add(ct);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	private static void insertDB(ConcurrentHashMap<Key, Set<ClusteredTuple>> contactCluster) throws SQLException {
		final int batchSize = 10000; //Batch size is important.
		DriverManager.registerDriver(new com.mysql.jdbc.Driver ());
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb","root","root");
		PreparedStatement ps = null;
		try {
			String sql = "INSERT INTO CONTACT_TUPLES (DIGEST, TIMESLOT, EPHEMERAL) VALUES (?,?,?)";
			ps = conn.prepareStatement(sql);

			int insertCount=0;
			for (Set<ClusteredTuple> set : contactCluster.values()) {
				if(set.size()>1) {
					for(ClusteredTuple t: set) {
						ps.setString(1, t.getDigest());
						ps.setLong(2, t.getTimeslot());
						ps.setString(3, t.getEphemeralId());
						ps.addBatch();
						if (++insertCount % batchSize == 0) {
							ps.executeBatch();
						}
					}
				}
			}
			ps.executeBatch();

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		finally {
			try {
				contactCluster.clear();
				ps.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	} 
}
