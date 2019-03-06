/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
*/

package com.vvsip.ansip;

import android.os.Binder;

public class VvsipServiceBinder extends Binder {

	private IVvsipService service = null; 
	  
    public VvsipServiceBinder(IVvsipService service) { 
        super(); 
        this.service = service; 
    } 
 
    public IVvsipService getService(){ 
        return service; 
    } 
}
