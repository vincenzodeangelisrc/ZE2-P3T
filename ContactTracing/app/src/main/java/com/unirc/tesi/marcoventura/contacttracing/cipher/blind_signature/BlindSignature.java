package com.unirc.tesi.marcoventura.contacttracing.cipher.blind_signature;


import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.unirc.tesi.marcoventura.contacttracing.database.SQLiteHelper;
import com.unirc.tesi.marcoventura.contacttracing.token.SendSignedToken;
import com.unirc.tesi.marcoventura.contacttracing.token.Token;
import com.unirc.tesi.marcoventura.contacttracing.util.JSONHelper;

import java.math.BigInteger;
import java.util.ArrayList;


public class BlindSignature {


	static BigInteger message; //first message User sends to HF, message = H(message) * r^e mod N

	static BigInteger signedmessage;// HF's message to User, signed_message = message^d mod N

	public static String signature;

	@RequiresApi(api = Build.VERSION_CODES.O)
	public static boolean executeBlindSignature(final Context context){

		final SQLiteHelper database = new SQLiteHelper(context);

		try {

			message = User.calculateMessage(context); //call User's function calculateMu with HF Public key as input in order to calculate message, and store it in mu variable

			final RequestQueue queue = Volley.newRequestQueue(context);
			String url = "http://192.168.1.200:8080/HealtFacility/BlindSignature?message=" + message;

			final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {

					ResServlet res = (ResServlet) JSONHelper.getObjectFromJson(response, ResServlet.class);
					signedmessage = res.getSignature(); // call HF's function calculateMuPrime with message produced earlier by User as input, to calculate  signed_message and store it to signed_message variable

					/* ######## Verify Signature ######## */
					signature = User.signatureCalculation(signedmessage, context); // call User's function signatureCalculation with signed_message as input and calculate the signature, then store it in sig variable

					if (User.verify(signature, context)) {      //User is checking if the signature he got from HF is valid, that can be easily computed because (m^d)^e modN = m

						Log.d("Signature Status...", "Success!");

						ArrayList<Token> list = database.readAllToken();
						SendSignedToken data = new SendSignedToken(signature, list);
						String json = JSONHelper.getJsonFromObject(data);
						Log.d("Send data to server... ", json);

					}else{
						Log.d("Signature Status", "FAILED");
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Log.d("ERROR", String.valueOf(error));
				}
			});
			queue.add(stringRequest);


		} catch (Exception e) {

			Log.e("BlindSignature", String.valueOf(e));
		}
		return false;
	}
}
