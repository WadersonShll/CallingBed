/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
*/

package com.vvsip.ansip;

/*
 * 帐号类，功能：存储帐号信息
 */
public class VvsipAccount {

	private String domain;
	private String userid;
	private String passwd;
	private String identity;
	private String outboundProxy;
	private String transport;
	private int registerInterval;
	private boolean echoCancellation;
	
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @param userid the userid to set
	 */
	public void setUserid(String userid) {
		this.userid = userid;
	}
	/**
	 * @return the userid
	 */
	public String getUserid() {
		return userid;
	}
	/**
	 * @param passwd the passwd to set
	 */
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	/**
	 * @return the passwd
	 */
	public String getPasswd() {
		return passwd;
	}
	/**
	 * @param identity the identity to set
	 */
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	/**
	 * @return the identity
	 */
	public String getIdentity() {
		return identity;
	}
	/**
	 * @param identity the identity to set
	 */
	public void setOutboundProxy(String outboundProxy) {
		this.outboundProxy = outboundProxy;
	}
	/**
	 * @return the identity
	 */
	public String getOutboundProxy() {
		return outboundProxy;
	}
	/**
	 * @param transport the transport to set
	 */
	public void setTransport(String transport) {
		this.transport = transport;
	}
	/**
	 * @return the transport
	 */
	public String getTransport() {
		return transport;
	}
	
	public boolean isDefined() {

		if (domain!=null && domain.length()==0)
			domain = null;
		if (userid!=null && userid.length()==0)
			userid = null;
		if (passwd!=null && passwd.length()==0)
			passwd = null;
		if (identity!=null && identity.length()==0)
			identity = null;
		if (outboundProxy!=null && outboundProxy.length()==0)
			outboundProxy = null;
		if (transport!=null && transport.length()==0)
			transport = null;
		
		
		if (transport==null)
			transport = "udp";
		else if (transport.compareToIgnoreCase("udp")!=0
				&&transport.compareToIgnoreCase("tcp")!=0
				&&transport.compareToIgnoreCase("tls")!=0)
			transport = "udp";
		
		if (registerInterval<=0)
			registerInterval=600; //default!
		
		if (registerInterval<100)
			registerInterval=100;
		
		
		if (domain==null || domain.length()==0)
			return false;
		if (userid==null || userid.length()==0)
			return false;
		if (identity==null || identity.length()==0)
		{
			identity = "<sip:"+userid+"@"+domain+">";
		}
		
		return true;
	}
	
	/**
	 * @param registerInterval the registerInterval to set
	 */
	public void setRegisterInterval(int registerInterval) {
		this.registerInterval = registerInterval;
	}
	
	/**
	 * @return the registerInterval
	 */
	public int getRegisterInterval() {
		return registerInterval;
	}
	/**
	 * @param echoCancellation the echoCancellation to set
	 */
	public void setEchoCancellation(boolean echoCancellation) {
		this.echoCancellation = echoCancellation;
	}
	/**
	 * @return the echoCancellation
	 */
	public boolean isEchoCancellation() {
		return echoCancellation;
	}
}
