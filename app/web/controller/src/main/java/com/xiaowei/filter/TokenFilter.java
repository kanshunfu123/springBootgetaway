package com.xiaowei.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xiaowei.entity.RedisUser;
import com.xiaowei.exception.RedisKeyEnum;
import com.xiaowei.res.JwtResponse;
import com.xiaowei.utils.DateUtil;
import com.xiaowei.utils.JwtTokenUtil;
import com.xiaowei.utils.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by MOMO on 2018/12/28.
 * 2.全局过滤器（GlobalFilter）
 * JTW 校验
 */
@Component
@Slf4j
public class TokenFilter implements GlobalFilter, Ordered {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private RedisUtil redisUtil;
    // JWT 失效时间小于60分钟，更新JWT
    private final static Long EXPIREDJWT = 3600L;
    //redis 30 分钟会话失效时间
    private final static Long EXPIREDREDIS = 1800L;
    @Value("${xiaowei.ignores}")
    private String ignores;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uuid = exchange.getRequest().getHeaders().getFirst(RedisKeyEnum.REDIS_KEY_HEADER_INFO.getKey());
        String authToken = "";
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        try {
            URI uri = serverHttpRequest.getURI();
            String path = uri.getPath();
            String[] str = ignores.split(",");
            for (String s : str) {
                if (path.contains(s)){
                    return chain.filter(exchange);
                }
            }
            if (StringUtils.isBlank(uuid)) {
                return JwtResponse.jwtResponse(exchange, HttpStatus.UNAUTHORIZED.value(), "token出错");
            } else {

                //用户 Token
                Object sessionJwt = redisUtil.get(RedisKeyEnum.REDIS_KEY_USER_INFO.getKey() + uuid);
                if (sessionJwt == null) {
                    return JwtResponse.jwtResponse(exchange, HttpStatus.UNAUTHORIZED.value(), "会话已失效，请重新登录");
                }
                authToken = String.valueOf(sessionJwt);
                //解析token
                String userInfo = jwtTokenUtil.getUsernameFromToken(authToken);
                RedisUser redisUser = JSON.parseObject(userInfo, new TypeReference<RedisUser>() {
                });
                //第三方
                //jwt 失效时间
                Date getExpirationDateFromToken = jwtTokenUtil.getExpirationDateFromToken(String.valueOf(sessionJwt));
                long remainingMinutes = DateUtil.getMinuteDifference(getExpirationDateFromToken, DateUtil.getCurrentTime());
                //刷新JTW
                if (remainingMinutes <= EXPIREDJWT) {
                    // randomKey和token已经生成完毕
                    final String randomKey = jwtTokenUtil.getRandomKey();
                    final String newToken = jwtTokenUtil.generateToken(userInfo, randomKey);
                    redisUtil.set(RedisKeyEnum.REDIS_KEY_USER_INFO.getKey() + redisUser.getRedisUserKey(), newToken, RedisKeyEnum.REDIS_KEY_USER_INFO.getExpireTime());
                    authToken = newToken;
                }
                //刷新Redis-token时间
                Object o = redisUtil.getExpire(RedisKeyEnum.REDIS_KEY_USER_INFO.getKey() + redisUser.getRedisUserKey());
                if ((Long) o <= EXPIREDREDIS) {
                    //刷新redis时间
                    redisUtil.expire(RedisKeyEnum.REDIS_KEY_USER_INFO.getKey() + redisUser.getRedisUserKey(), RedisKeyEnum.REDIS_KEY_USER_INFO.getExpireTime());
                }
                //刷新Redis-userId时间
                Object userId = redisUtil.getExpire(RedisKeyEnum.REDIS_KEY_USER_ID.getKey() + redisUser.getId());
                if ((Long) userId <= EXPIREDREDIS) {
                    //刷新redis时间
                    redisUtil.expire(RedisKeyEnum.REDIS_KEY_USER_ID.getKey() + redisUser.getId(), RedisKeyEnum.REDIS_KEY_USER_ID.getExpireTime());
                }
                ServerHttpRequest request = exchange
                        .getRequest().mutate()
                        // 统一头部，用于防止直接调用服务
                        .header(RedisKeyEnum.REDIS_KEY_USER_HEADER_CODE.getKey(), authToken)
                        .header("exit", uuid)
                        .build();
                exchange.getResponse().getHeaders().add("testresponse", "testresponse");
                //将新的request 变成 change对象
                ServerWebExchange serverWebExchange = exchange.mutate().request(request).build();
                return chain.filter(serverWebExchange);
            }
        } catch (MalformedJwtException e) {
            log.error("JWT格式错误异常:{}======>>>:{}====={}", uuid, e.getMessage(), e);
            return JwtResponse.jwtResponse(exchange, HttpStatus.UNAUTHORIZED.value(), "JWT格式错误");
        } catch (SignatureException e) {
            log.error("JWT签名错误异常:{}======>>>:{}", uuid, e.getMessage(), e);
            return JwtResponse.jwtResponse(exchange, HttpStatus.UNAUTHORIZED.value(), "JWT签名错误");
        } catch (ExpiredJwtException e) {
            log.error("JWT过期异常:{}======>>>:{}", uuid, e.getMessage(), e);
            return JwtResponse.jwtResponse(exchange, HttpStatus.UNAUTHORIZED.value(), "会话已失效，请重新登录");
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT异常:{}======>>>:{}", uuid, e.getMessage(), e);
            return JwtResponse.jwtResponse(exchange, HttpStatus.UNAUTHORIZED.value(), "JWT格式不正确");
        } catch (Exception e) {
            log.error("TokenFilter，不支持的异常:{}======>>>:{}", uuid, e.getMessage(), e);
            return JwtResponse.jwtResponse(exchange, HttpStatus.UNAUTHORIZED.value(), "TokenFilter：token 解析异常:");
        }
    }

    @Override
    public int getOrder() {
        return -999999999;
    }

    private String urlEncode(Object o) {
        try {
            return URLEncoder.encode(JSONObject.toJSONString(o), "UTF-8");
        } catch (Exception e) {
            log.error("urlEncode:{}", e.getMessage());
        }
        return "";
    }

}