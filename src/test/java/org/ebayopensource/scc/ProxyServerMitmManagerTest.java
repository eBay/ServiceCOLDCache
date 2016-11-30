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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.Socket;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;

import org.ebayopensource.scc.ProxyServerMitmManager.InsecureX509ExtendedTrustManager;
import org.junit.Test;
import org.littleshoot.proxy.mitm.RootCertificateException;

public class ProxyServerMitmManagerTest {

	@Test
	public void test() throws RootCertificateException {
		ProxyServerMitmManager manager = new ProxyServerMitmManager();

		assertNotNull(manager.serverSslEngine("www.ebay.com", 443));
	}

	@Test
	public void testInsecureX509ExtendedTrustManager()
			throws CertificateException {
		InsecureX509ExtendedTrustManager manager = new InsecureX509ExtendedTrustManager();
		X509Certificate[] certs = new X509Certificate[1];
		certs[0] = mock(X509Certificate.class);
		when(certs[0].getSubjectDN()).thenReturn(mock(Principal.class));
		manager.checkClientTrusted(certs, "");
		manager.checkClientTrusted(certs, "", new Socket());
		manager.checkClientTrusted(certs, "", mock(SSLEngine.class));

		manager.checkServerTrusted(certs, "");
		manager.checkServerTrusted(certs, "", new Socket());
		manager.checkServerTrusted(certs, "", mock(SSLEngine.class));

		assertNotNull(manager.getAcceptedIssuers());
	}
}
