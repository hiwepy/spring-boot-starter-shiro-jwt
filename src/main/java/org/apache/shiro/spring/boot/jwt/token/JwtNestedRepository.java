package org.apache.shiro.spring.boot.jwt.token;

import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.spring.boot.jwt.JwtPayload;

public interface JwtNestedRepository<S, E> {

	public abstract String issueJwt(S signingKey, E encryptKey, String jwtId, String subject, String issuer,
			String roles, String permissions, String algorithm, long period) throws AuthenticationException;
	
	public abstract String issueJwt(S signingKey, E encryptKey, String jwtId, String subject, String issuer,
			Map<String, Object> claims, String algorithm, long period) throws AuthenticationException;

	public abstract boolean verify(S signingKey, E encryptKey, String token, boolean checkExpiry)
			throws AuthenticationException;

	public abstract JwtPayload getPlayload(S signingKey, E encryptKey, String token, boolean checkExpiry)
			throws AuthenticationException;
}
