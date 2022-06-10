

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet implementation class BlindSignature
 */
@WebServlet("/BlindSignature")
public class BlindSignature extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public BlindSignature() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String message_string = request.getParameter("message");
		System.out.println(message_string);
		BigInteger message = new BigInteger(message_string);
		BigInteger signedmessage = HealthFacility.calculateSignatureOfMessage(message); 
		
		Response res = new Response(signedmessage);
			
		
		Gson gson = new Gson();
		String res_json = gson.toJson(res);
		System.out.println(res_json);

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		out.print(res_json);
		out.flush();   
		
	}

}
