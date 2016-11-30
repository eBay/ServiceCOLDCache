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
package org.ebayopensource.scc.track;

import java.io.Serializable;

public class Tracker implements Serializable {
	private static final long serialVersionUID = 7262830398514885269L;
	public static final String UNKNOWN = "Unknown";

	private String m_host;

	private String m_initiator;

	private String m_ip;

	private String m_os;

	private long m_startTime;
	
	private String m_user;

	public Tracker(String user, String host, String ip, String os, long startup, String initiator) {
		m_user = user;
		m_host = host;
		m_ip = ip;
		m_os = os;
		m_startTime = startup;
		m_initiator = initiator;
	}

	public String getHost() {
		return m_host;
	}

	public String getInitiator() {
		return m_initiator;
	}

	public String getIp() {
		return m_ip;
	}

	public String getOs() {
		return m_os;
	}

	public long getStartTime() {
		return m_startTime;
	}

	public String getUser() {
		return m_user;
	}

	public void setHost(String host) {
		m_host = host;
	}

	public void setInitiator(String initiator) {
		m_initiator = initiator;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public void setOs(String os) {
		m_os = os;
	}

	public void setStartTime(long startTime) {
		m_startTime = startTime;
	}

	public void setUser(String user) {
		m_user = user;
	}

}
