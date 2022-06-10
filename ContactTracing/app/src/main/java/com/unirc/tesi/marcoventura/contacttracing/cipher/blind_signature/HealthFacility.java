package com.unirc.tesi.marcoventura.contacttracing.cipher.blind_signature;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * The class Health Facility represents Health Facility who can create an RSA keypair and can issue digital signatures
 */
public class HealthFacility {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static PublicKey getPublic(Context context)
            throws Exception {

        AssetManager manager = context.getAssets();

        String filename = "public_key.der";
        InputStream inputStream = manager.open(filename);

        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        byte keyBytes[] = output.toByteArray();


        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);

    }

}


