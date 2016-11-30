/*******************************************************************************
 * Copyright (c) 2016 eBay Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.ebayopensource.scc;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import org.codehaus.plexus.util.ReflectionUtils;
import org.littleshoot.proxy.MitmManager;
import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.BouncyCastleSslEngineSource;
import org.littleshoot.proxy.mitm.FakeCertificateException;
import org.littleshoot.proxy.mitm.RootCertificateException;
import org.littleshoot.proxy.mitm.SubjectAlternativeNameHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.security.ssl.SSLContextImpl;

public class ProxyServerMitmManager implements MitmManager {

	 private static final Logger LOGGER = LoggerFactory
	            .getLogger(ProxyServerMitmManager.class);
	
	private BouncyCastleSslEngineSource sslEngineSource;

	public ProxyServerMitmManager() throws RootCertificateException {
		this(new Authority(), true, true);
	}

	public ProxyServerMitmManager(Authority authority, boolean trustAllServers,
			boolean sendCerts) throws RootCertificateException {
		try {
			sslEngineSource = new BouncyCastleSslEngineSource(authority,
					trustAllServers, sendCerts);
			SSLContext sslContext = (SSLContext) ReflectionUtils
					.getValueIncludingSuperclasses("sslContext",
							sslEngineSource);
			SSLContextImpl sslContextImpl = (SSLContextImpl) ReflectionUtils
					.getValueIncludingSuperclasses("contextSpi", sslContext);
			ReflectionUtils.setVariableValueInObject(sslContextImpl,
					"trustManager", new InsecureX509ExtendedTrustManager());
		} catch (final Exception e) {
			throw new RootCertificateException(
					"Errors during assembling root CA.", e);
		}
	}

	public SSLEngine serverSslEngine(String peerHost, int peerPort) {
		return sslEngineSource.newSslEngine(peerHost, peerPort);
	}

    public SSLEngine serverSslEngine() {
        return sslEngineSource.newSslEngine();
    }
    
    public SSLEngine clientSslEngineFor(HttpRequest httpRequest, SSLSession serverSslSession) {
        try {
            X509Certificate upstreamCert = getCertificateFromSession(serverSslSession);
            String commonName = getCommonName(upstreamCert);

            SubjectAlternativeNameHolder san = new SubjectAlternativeNameHolder();

            san.addAll(upstreamCert.getSubjectAlternativeNames());

            LOGGER.debug("Subject Alternative Names: {}", san);
            return sslEngineSource.createCertForHost(commonName, san);

        } catch (Exception e) {
            throw new FakeCertificateException(
                    "Creation dynamic certificate failed", e);
        }
    }

    private X509Certificate getCertificateFromSession(SSLSession sslSession)
            throws SSLPeerUnverifiedException {
        Certificate[] peerCerts = sslSession.getPeerCertificates();
        Certificate peerCert = peerCerts[0];
        if (peerCert instanceof java.security.cert.X509Certificate) {
            return (java.security.cert.X509Certificate) peerCert;
        }
        throw new IllegalStateException(
                "Required java.security.cert.X509Certificate, found: "
                        + peerCert);
    }

    private String getCommonName(X509Certificate c) {
        LOGGER.debug("Subject DN principal name: {}", c.getSubjectDN().getName());
        for (String each : c.getSubjectDN().getName().split(",\\s*")) {
            if (each.startsWith("CN=")) {
                String result = each.substring(3);
                LOGGER.debug("Common Name: {}", result);
                return result;
            }
        }
        throw new IllegalStateException("Missed CN in Subject DN: "
                + c.getSubjectDN());
    }

	public static class InsecureX509ExtendedTrustManager extends
			X509ExtendedTrustManager {

		private X509TrustManager m_trustManager;

		public InsecureX509ExtendedTrustManager() {
			m_trustManager = (X509TrustManager) InsecureTrustManagerFactory.INSTANCE
					.getTrustManagers()[0];
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			m_trustManager.checkClientTrusted(chain, authType);
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			m_trustManager.checkServerTrusted(chain, authType);
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return m_trustManager.getAcceptedIssuers();
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain,
				String authType, Socket socket) throws CertificateException {
			m_trustManager.checkClientTrusted(chain, authType);
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain,
				String authType, Socket socket) throws CertificateException {
			m_trustManager.checkServerTrusted(chain, authType);
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain,
				String authType, SSLEngine engine) throws CertificateException {
			m_trustManager.checkClientTrusted(chain, authType);
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain,
				String authType, SSLEngine engine) throws CertificateException {
			m_trustManager.checkServerTrusted(chain, authType);
		}

	}

}
