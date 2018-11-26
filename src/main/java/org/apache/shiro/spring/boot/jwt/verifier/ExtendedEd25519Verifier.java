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
package org.apache.shiro.spring.boot.jwt.verifier;

import java.util.Date;
import java.util.Set;

import org.apache.shiro.spring.boot.jwt.exception.ExpiredJwtException;
import org.apache.shiro.spring.boot.jwt.exception.NotObtainedJwtException;
import org.apache.shiro.spring.boot.jwt.time.JwtTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * TODO
 * @author ： <a href="https://github.com/vindell">vindell</a>
 */
public class ExtendedEd25519Verifier extends Ed25519Verifier {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private final JWTClaimsSet claimsSet;
	private final JwtTimeProvider timeProvider;
	
	public ExtendedEd25519Verifier(OctetKeyPair publicKey, JWTClaimsSet claimsSet, JwtTimeProvider timeProvider) throws JOSEException {
		super(publicKey);
		this.claimsSet = claimsSet;
		this.timeProvider = timeProvider;
	}

	public ExtendedEd25519Verifier(OctetKeyPair publicKey, Set<String> defCritHeaders, JWTClaimsSet claimsSet, JwtTimeProvider timeProvider)
			throws JOSEException {
		super(publicKey, defCritHeaders);
		this.claimsSet = claimsSet;
		this.timeProvider = timeProvider;
	}
	
	@Override
	public boolean verify(final JWSHeader header, final byte[] signingInput, final Base64URL signature)
			throws JOSEException {
		boolean value = super.verify(header, signingInput, signature);

		if (value) {

			Date issuedAt = claimsSet.getIssueTime();
			Date notBefore = claimsSet.getNotBeforeTime();
			Date expiration = claimsSet.getExpirationTime();
			long currentTimeMillis = timeProvider.now();

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
		}

		return value;
	}

}
