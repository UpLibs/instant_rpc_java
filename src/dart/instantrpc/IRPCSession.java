package dart.instantrpc;

import java.lang.ref.WeakReference;
import java.util.Map;

abstract public class IRPCSession implements Map<String,Object> {

	static ThreadLocal<WeakReference<IRPCSession>> localSession = new ThreadLocal<>() ;
	
	static protected void setLocalSession(IRPCSession session) {
		localSession.set( new WeakReference<IRPCSession>(session) );
	}
	
	static protected void removeLocalSession(IRPCSession session) {
		IRPCSession prev = get() ;
		
		if (prev == session) {
			localSession.set(null);
		}
	}
	
	static public IRPCSession get() {
		WeakReference<IRPCSession> ref = localSession.get() ;
		return ref != null ? ref.get() : null ;
	}
	
	////////////////////////////////////////////////////////////////

	public IRPCSession() {
	}
	
	abstract public String getID() ;
	
	abstract public void detroy() ;
	
}

