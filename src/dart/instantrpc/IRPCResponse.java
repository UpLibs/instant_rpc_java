package dart.instantrpc;

import java.lang.ref.WeakReference;


public class IRPCResponse {

	static ThreadLocal<WeakReference<IRPCResponse>> localResponse = new ThreadLocal<WeakReference<IRPCResponse>>() ;
	
	static protected void setLocalResponse(IRPCResponse response) {
		localResponse.set( new WeakReference<IRPCResponse>(response) );
	}
	
	static protected void removeLocalResponse(IRPCResponse response) {
		IRPCResponse prevResp = get() ;
		
		if (prevResp == response) {
			localResponse.set(null);
		}
	}
	
	static public IRPCResponse get() {
		WeakReference<IRPCResponse> ref = localResponse.get() ;
		return ref != null ? ref.get() : null ;
	}
	
	////////////////////////////////////////////////////////////////
	
	protected boolean ok = false ;
	protected boolean pathNotFound = false ;
	protected Exception error ;
	
	protected Object invokeReturn ;
	protected Class<?> invokeReturnType ;
	
	private IRPCEventTable eventTable ;
	
	private boolean returnRawData ;
	
	public IRPCResponse( IRPCEventTable eventTable ) {
		this.eventTable = eventTable ;
	}
	
	public void setReturnRawData(boolean returnRawData) {
		this.returnRawData = returnRawData;
	}
	
	public boolean isReturnRawData() {
		return returnRawData;
	}
	
	public IRPCEventTable getEventTable() {
		return eventTable;
	}

	public boolean isOk() {
		return ok;
	}
	
	public boolean isPathNotFound() {
		return pathNotFound;
	}
	
	public Exception getError() {
		return error;
	}
	
	private StringBuilder output = new StringBuilder() ;
	
	public String getOutput() {
		return output.toString() ;
	}
	
	////////////////////////////////////////////////////////////////////////
	
	public void print(String str) {
		output.append(str) ;
	}
	
	public void println(String str) {
		output.append(str) ;
		output.append("\n") ;
	}
	
	public void print(int n) {
		print( String.valueOf(n) );
	}
	
	public void println(int n) {
		println( String.valueOf(n) );
	}
	
	public void print(long n) {
		print( String.valueOf(n) );
	}
	
	public void println(long n) {
		println( String.valueOf(n) );
	}
	
	public void print(double n) {
		print( String.valueOf(n) );
	}
	
	public void println(double n) {
		println( String.valueOf(n) );
	}
	
	public void print(float n) {
		print( String.valueOf(n) );
	}
	
	public void println(float n) {
		println( String.valueOf(n) );
	}
	
	public void print(boolean n) {
		print( String.valueOf(n) );
	}
	
	public void println(boolean n) {
		println( String.valueOf(n) );
	}
	
	public void print(char c) {
		print( String.valueOf(c) );
	}
	
	public void println(char c) {
		println( String.valueOf(c) );
	}
	
	public void print(Object o) {
		print( String.valueOf(o) );
	}
	
	public void println(Object o) {
		println( String.valueOf(o) );
	}
	
	////////////////////////////////////////////////////////////////////////

	protected String fullIRPCResponse ;
	
	static private String DUMMY_IRPCEventTable_toString = new IRPCEventTable().toString() ;
	
	public String buildIRPCResponse() {
		if (fullIRPCResponse != null) return fullIRPCResponse ;
		
		if (returnRawData) {
			return invokeReturn.toString() ;
		}
		
		StringBuilder str = new StringBuilder() ;
	
		String eventTableStr = eventTable == null ? DUMMY_IRPCEventTable_toString : eventTable.toString() ;
		
		str.append( eventTableStr.length() +"\n"+eventTableStr+"\n") ;
		
		if (invokeReturn != null) {
			str.append( IRPCDataProviderHandler.toDartType(invokeReturnType) ) ;
			str.append("\n") ;
			
			str.append(invokeReturn) ;
		}
		
		return str.toString() ;
	}
	
	@Override
	public String toString() {
		return this.getClass().getName()+"[ok: "+ok+" ; pathNotFound: "+ pathNotFound +" ; error: "+ error +"]";
	}
	
}
