/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
*/

package com.vvsip.ansip;


/*
 * 服务监听接口
 */
public interface IVvsipServiceListener {
	public void onNewVvsipCallEvent(VvsipCall call);
	public void onRemoveVvsipCallEvent(VvsipCall call);
	public void onStatusVvsipCallEvent(VvsipCall call);
	public void onRegistrationEvent(int rid, String remote_uri, int code, String reason);
}
