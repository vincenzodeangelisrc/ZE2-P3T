

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;

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
	private static long tsDuration=10;
       
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
		
		long currentTimestamp = Instant.now().getEpochSecond();
		
		if(currentTimestamp>Rectimestamp+10) {
			return;
		}
		long timeslot=Rectimestamp/tsDuration;
		ClusteredTuple ct= new ClusteredTuple(eph,digest,timeslot);
		Key k= new Key(timeslot,digest);
		HashSet<ClusteredTuple> set=ContactClusters.contactCluster.get(k);
		if(set==null) {
			set=new HashSet<ClusteredTuple>();
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

}
