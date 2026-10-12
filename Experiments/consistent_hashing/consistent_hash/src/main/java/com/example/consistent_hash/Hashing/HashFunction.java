package com.example.consistent_hash.Hashing;

import java.math.BigInteger;
import java.security.MessageDigest;

public class HashFunction {

    public static long generateHash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return Math.abs(new BigInteger(1, md.digest(key.getBytes())).longValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
