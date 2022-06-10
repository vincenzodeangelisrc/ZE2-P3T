

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import util.ClusteredTuple;
import util.ContactClusters;
import util.ContactTuple;
import util.Key;
import util.Tuple;

/**
 * Servlet implementation class InfectionReporting
 */
@WebServlet("/InfectionReporting")
public class InfectionReporting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static long tsDuration=10;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InfectionReporting() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Gson gson = new Gson();
		String json=request.getParameter("listOfTuples");
		Type collectionType = new TypeToken<List<Tuple>>(){}.getType();
        List<Tuple> tuples = gson.fromJson(json, collectionType);
        HashSet<ContactTuple> contactSet= new HashSet<ContactTuple>();
        for(Tuple t: tuples) {
        	long timeslot=t.getTimestamp()/tsDuration;
        	Key k = new Key(timeslot,t.getDigest());
        	HashSet<ClusteredTuple> set=ContactClusters.contactCluster.get(k);
    		for(ClusteredTuple ct: set) {
    			if(!ct.getEphemeralId().equals(t.getEphemeralId())) {
    				contactSet.add(new ContactTuple(ct.getEphemeralId(),ct.getDigest(),t.getPho(), t.getTheta(),ct.getTimeslot()));
    			}
    		}
        	
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
