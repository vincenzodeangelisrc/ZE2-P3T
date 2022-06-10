package com.unirc.tesi.marcoventura.contacttracing.token;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenHelper {

    private static int random_lenght = 128;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String generateRandom(){

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[random_lenght];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String generateMacAddressRandom(){

        final char[] characters = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        SecureRandom random = new SecureRandom();

        StringBuilder res = new StringBuilder();

        for (int i = 0; i < 8; i++)
            res.append(characters[random.nextInt(characters.length)]);

        return res.toString();

    }

}
