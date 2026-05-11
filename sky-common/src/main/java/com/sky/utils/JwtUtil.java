package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */



    private static final String JWT_SECRET = "lxyiscoding@2263243663isthesecret";
    private static final long JWT_EXPIRATION = 7*24*60*60*1000L;


    private static SecretKey getKey(){
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }
    public static  String createJWT(Long userId){
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+JWT_EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    public static boolean parseJWT(String token){
        try{
            Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Long getUserIdFromToken(String token){
        return Long.parseLong(Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }

}
