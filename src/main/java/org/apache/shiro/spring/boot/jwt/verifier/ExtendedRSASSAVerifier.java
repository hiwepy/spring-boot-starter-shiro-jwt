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

import java.security.interfaces.RSAPublicKey;
import java.util.Set;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * TODO
 * @author ： <a href="https://github.com/vindell">vindell</a>
 */
public class ExtendedRSASSAVerifier extends RSASSAVerifier {
	
	private final JWTClaimsSet claimsSet;

	public ExtendedRSASSAVerifier(RSAKey rsaJWK, JWTClaimsSet claimsSet) throws JOSEException {
		super(rsaJWK);
		this.claimsSet = claimsSet;
	}

	public ExtendedRSASSAVerifier(RSAPublicKey publicKey, JWTClaimsSet claimsSet) {
		super(publicKey);
		this.claimsSet = claimsSet;
	}

	public ExtendedRSASSAVerifier(RSAPublicKey publicKey, Set<String> defCritHeaders, JWTClaimsSet claimsSet) {
		super(publicKey, defCritHeaders);
		this.claimsSet = claimsSet;
	}

	@Override
	public boolean verify(final JWSHeader header, final byte[] signingInput, final Base64URL signature)
			throws JOSEException {
		boolean value = super.verify(header, signingInput, signature);
		long time = System.currentTimeMillis();

		return value && claimsSet.getNotBeforeTime().getTime() <= time
				&& time < claimsSet.getExpirationTime().getTime();
	}

}
