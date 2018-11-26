/*
 * Copyright (c) 2018, vindell (https://github.com/vindell).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.shiro.spring.boot.jwt.token;

import java.security.Key;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.biz.utils.StringUtils;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.spring.boot.jwt.JwtPayload;
import org.apache.shiro.spring.boot.jwt.exception.ExpiredJwtException;
import org.apache.shiro.spring.boot.jwt.exception.IncorrectJwtException;
import org.apache.shiro.spring.boot.jwt.exception.InvalidJwtToken;
import org.apache.shiro.spring.boot.jwt.exception.NotObtainedJwtException;
import org.apache.shiro.spring.boot.jwt.time.JwtTimeProvider;
import org.apache.shiro.spring.boot.utils.JJwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodec;
import io.jsonwebtoken.CompressionCodecResolver;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolver;

/**
 * <b> JSON Web Token (JWT) with signature  </b>
 * https://github.com/jwtk/jjwt
 */
public class SignedWithSecretResolverJWTRepository implements JwtKeyResolverRepository<Key> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private long allowedClockSkewSeconds = -1;
	private CompressionCodec compressWith = CompressionCodecs.DEFLATE;
	private final SigningKeyResolver signingKeyResolver;
    private CompressionCodecResolver compressionCodecResolver;
    private JwtTimeProvider timeProvider = JwtTimeProvider.DEFAULT_TIME_PROVIDER;
    
    public SignedWithSecretResolverJWTRepository(SigningKeyResolver signingKeyResolver) {
    	this.signingKeyResolver = signingKeyResolver;
    }
    
	/**
	 * Issue JSON Web Token (JWT)
	 * @author ：<a href="https://github.com/vindell">vindell</a>
	 * @param secretKey		: Signing key
	 * @param keyId			: Key Id
	 * @param jwtId			: Jwt Id
	 * @param subject		: Jwt Subject
	 * @param issuer 		: Jwt Issuer
	 * @param audience 		: Jwt Audience
	 * @param roles			: The Roles
	 * @param permissions	: The Perms
	 * @param algorithm		: Supported algorithms：
	 *  <p> HS256: HMAC using SHA-256 </p>
	 *  <p> HS384: HMAC using SHA-384 </p>
     *  <p> HS512: HMAC using SHA-512 </p>
     *  <p> ES256: ECDSA using P-256 and SHA-256 </p>
     *  <p> ES384: ECDSA using P-384 and SHA-384 </p>
     *  <p> ES512: ECDSA using P-521 and SHA-512 </p>
     *  <p> RS256: RSASSA-PKCS-v1_5 using SHA-256 </p>
     *  <p> RS384: RSASSA-PKCS-v1_5 using SHA-384 </p>
     *  <p> RS512: RSASSA-PKCS-v1_5 using SHA-512 </p>
     *  <p> PS256: RSASSA-PSS using SHA-256 and MGF1 with SHA-256 </p>
     *  <p> PS384: RSASSA-PSS using SHA-384 and MGF1 with SHA-384 </p>
     *  <p> PS512: RSASSA-PSS using SHA-512 and MGF1 with SHA-512 </p>
     * @param period 		: Jwt Expiration Cycle
	 * @return JSON Web Token (JWT)
	 * @throws AuthenticationException When Authentication Exception
	 */
	@Override
	public String issueJwt(Key secretKey, String keyId, String jwtId, String subject, String issuer, String audience,
			String roles, String permissions, String algorithm, long period)  throws AuthenticationException {
		Map<String, Object> claims = Maps.newHashMap();
		claims.put("roles", roles);
		claims.put("perms", permissions);
		
		return this.issueJwt(secretKey, keyId, jwtId, subject, issuer, audience, claims, algorithm, period);
	}
	
	/**
	 * Issue JSON Web Token (JWT)
	 * @author ：<a href="https://github.com/vindell">vindell</a>
	 * @param secretKey		: Signing key
	 * @param keyId			: Key Id
	 * @param jwtId			: Jwt Id
	 * @param subject		: Jwt Subject
	 * @param issuer 		: Jwt Issuer
	 * @param audience 		: Jwt Audience
	 * @param claims		: Jwt Claims
	 * @param algorithm		: Supported algorithms：
	 *  <p> HS256: HMAC using SHA-256 </p>
	 *  <p> HS384: HMAC using SHA-384 </p>
     *  <p> HS512: HMAC using SHA-512 </p>
     *  <p> ES256: ECDSA using P-256 and SHA-256 </p>
     *  <p> ES384: ECDSA using P-384 and SHA-384 </p>
     *  <p> ES512: ECDSA using P-521 and SHA-512 </p>
     *  <p> RS256: RSASSA-PKCS-v1_5 using SHA-256 </p>
     *  <p> RS384: RSASSA-PKCS-v1_5 using SHA-384 </p>
     *  <p> RS512: RSASSA-PKCS-v1_5 using SHA-512 </p>
     *  <p> PS256: RSASSA-PSS using SHA-256 and MGF1 with SHA-256 </p>
     *  <p> PS384: RSASSA-PSS using SHA-384 and MGF1 with SHA-384 </p>
     *  <p> PS512: RSASSA-PSS using SHA-512 and MGF1 with SHA-512 </p>
     * @param period 		: Jwt Expiration Cycle
	 * @return JSON Web Token (JWT)
	 * @throws AuthenticationException When Authentication Exception
	 */
	@Override
	public String issueJwt(Key secretKey, String keyId, String jwtId, String subject, String issuer, String audience,
			Map<String, Object> claims,	String algorithm, long period) throws AuthenticationException {
		
		JwtBuilder builder = JJwtUtils
				.jwtBuilder(jwtId, subject, issuer, audience, claims, period)
				// 指定KeyID以便进行验证时，动态获取该ID对应的Key
				.setHeaderParam(JwsHeader.KEY_ID, StringUtils.hasText(keyId) ? keyId : Base64.encodeToString(secretKey.getEncoded()))
				// 压缩类型
				.compressWith(getCompressWith())
				// 设置算法（必须）
				.signWith(SignatureAlgorithm.forName(algorithm), secretKey);

		// 签发时间
		long currentTimeMillis = this.getTimeProvider().now();
		Date now = new Date(currentTimeMillis);
		builder.setIssuedAt(now);
		// 有效期起始时间
		builder.setNotBefore(now);
		// Token过期时间
		if (period >= 0) {
			// 有效时间
			Date expiration = new Date(currentTimeMillis + period);
			builder.setExpiration(expiration);
		}
				
		return builder.compact();
	}
	
	/**
	 * Verify the validity of JWT
	 * @author 				: <a href="https://github.com/vindell">vindell</a>
	 * @param token  		: JSON Web Token (JWT)
	 * @param checkExpiry 	: If Check validity.
	 * @return If Validity
	 * @throws AuthenticationException When Authentication Exception
	 */
	@Override
	public boolean verify(String token, boolean checkExpiry) throws AuthenticationException {
			
		try {
			
			// Retrieve / verify the JWT claims according to the app requirements
			JwtParser jwtParser = Jwts.parser();
			// 设置允许的时间误差
			if(getAllowedClockSkewSeconds() > 0) {
				jwtParser.setAllowedClockSkewSeconds(getAllowedClockSkewSeconds());	
			}
			// 设置压缩方式解析器
			if(null != getCompressionCodecResolver() ) {
				jwtParser.setCompressionCodecResolver(getCompressionCodecResolver());
			}

			// 解密JWT，如果无效则会抛出异常
			Jws<Claims> jws = jwtParser.setSigningKeyResolver(signingKeyResolver).parseClaimsJws(token);
			// 解密成功且不需要进行过期检查则返回true
			if(!checkExpiry) {
				return true;
			}
			
			Claims claims = jws.getBody();

			Date issuedAt = claims.getIssuedAt();
			Date notBefore = claims.getNotBefore();
			Date expiration = claims.getExpiration();
			long currentTimeMillis = this.getTimeProvider().now();

			if (logger.isDebugEnabled()) {
				logger.debug("JWT IssuedAt:" + issuedAt);
				logger.debug("JWT NotBefore:" + notBefore);
				logger.debug("JWT Expiration:" + expiration);
				logger.debug("JWT Now:" + new Date(currentTimeMillis));
			}
			
			if(notBefore != null && currentTimeMillis <= notBefore.getTime()) {
				throw new NotObtainedJwtException(String.format("JWT was not obtained before this timestamp : [%s].", notBefore));
			}
			if(expiration != null && expiration.getTime() < currentTimeMillis) {
				throw new ExpiredJwtException("Expired JWT value. ");
			}
			return true;
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			throw new ExpiredJwtException(e);
		} catch (InvalidClaimException e) {
			throw new InvalidJwtToken(e);
		} catch (JwtException e) {
			throw new IncorrectJwtException(e);
		} catch (IllegalArgumentException e) {
			throw new IncorrectJwtException(e);
		}
		
	}

	/**
	 * Parser JSON Web Token (JWT)
	 * @author 		：<a href="https://github.com/vindell">vindell</a>
	 * @param token  		: JSON Web Token (JWT)
	 * @param checkExpiry 	: If Check validity.
	 * @return JwtPlayload {@link JwtPayload}
	 * @throws AuthenticationException When Authentication Exception
	 */
	@Override
	public JwtPayload getPlayload(String token, boolean checkExpiry)  throws AuthenticationException {
		try {
			
			// Retrieve JWT claims
			JwtParser jwtParser = Jwts.parser();
			// 设置允许的时间误差
			if(getAllowedClockSkewSeconds() > 0) {
				jwtParser.setAllowedClockSkewSeconds(getAllowedClockSkewSeconds());	
			}
			// 设置压缩方式解析器
			if(null != getCompressionCodecResolver() ) {
				jwtParser.setCompressionCodecResolver(getCompressionCodecResolver());
			}
			
			Jws<Claims> jws = jwtParser.setSigningKeyResolver(signingKeyResolver).parseClaimsJws(token);
			
			return JJwtUtils.payload(jws.getBody());
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			throw new ExpiredJwtException(e);
		} catch (InvalidClaimException e) {
			throw new InvalidJwtToken(e);
		} catch (JwtException e) {
			throw new IncorrectJwtException(e);
		} catch (IllegalArgumentException e) {
			throw new IncorrectJwtException(e);
		} catch (ParseException e) {
			throw new IncorrectJwtException(e);
		}
	}

	public long getAllowedClockSkewSeconds() {
		return allowedClockSkewSeconds;
	}

	public void setAllowedClockSkewSeconds(long allowedClockSkewSeconds) {
		this.allowedClockSkewSeconds = allowedClockSkewSeconds;
	}

	public CompressionCodec getCompressWith() {
		return compressWith;
	}

	public void setCompressWith(CompressionCodec compressWith) {
		this.compressWith = compressWith;
	}
	
	public CompressionCodecResolver getCompressionCodecResolver() {
		return compressionCodecResolver;
	}

	public void setCompressionCodecResolver(CompressionCodecResolver compressionCodecResolver) {
		this.compressionCodecResolver = compressionCodecResolver;
	}

	public JwtTimeProvider getTimeProvider() {
		return timeProvider;
	}

	public void setTimeProvider(JwtTimeProvider timeProvider) {
		this.timeProvider = timeProvider;
	}
	
	public SigningKeyResolver getSigningKeyResolver() {
		return signingKeyResolver;
	}

}
