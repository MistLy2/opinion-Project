package com.example.utils;

import com.example.config.BaseContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import java.util.Date;

import static io.jsonwebtoken.SignatureAlgorithm.HS512;

public class JwtUtils {

    public static final long EXPIRE = 1000 * 60 * 60 * 24; //token过期时间
    public static final String APP_SECRET = "hwfw02irj1jfkqjgkjaiqfq"; //数字签名的私钥

    public static String getJwtToken(Long uid, String account) {
        //生成JWT Token令牌

        /**jwt由以下三部分所组成 （请求头部信息+有效载荷部分+哈希签名） */
        String JwtToken = Jwts.builder()
                //请求头部信息
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")

                //token的过期时间
                .setSubject("my-user")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))

                //有效载荷部分，里面包含的是用户的数据
                //设置token主体部分 ，存储用户信息
                .claim("uid", uid)
                .claim("account", account)

                //哈希签名，防伪标签部分
                .signWith(HS512, APP_SECRET)
                .compact();

        return JwtToken;
    }

    public static boolean checkToken(String token){
        if(token == null){
            return false;
        }
        Long uid = null;
        try {
            Jws<Claims> claimsJws =
                    Jwts.parser()
                            .setSigningKey(APP_SECRET)
                            .parseClaimsJws(token);
            Claims body = claimsJws.getBody();
             uid= (Long)body.get("uid");


        }catch (Exception e){
            //如果解析异常，则说明身份验证不成功
            return false;
        }
        if(uid == BaseContext.getId()){
            //说明用户正确，鉴权成功
            return true;
        }else{
            //说明用户不正确
            return false;
        }
    }
}
