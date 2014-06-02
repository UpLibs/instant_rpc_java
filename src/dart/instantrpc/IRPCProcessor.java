package dart.instantrpc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

final public class IRPCProcessor {

	final private HashMap<String, Object> dataProviders = new HashMap<String, Object>() ;
	
	public void clearRegisteredDataProviders() {
		synchronized (dataProviders) {
			dataProviders.clear(); 
		}
	}
	
	public void registerDataProvider(String path, Object provider) {
		synchronized (dataProviders) {
			dataProviders.put(path, provider) ;
		}
	}
	
	public Object unregisterDataProvider(String path) {
		synchronized (dataProviders) {
			return dataProviders.remove(path) ;
		}
	}
	
	public IRPCResponse processRequest(String fullPath, IRPCSession session) {
		
		String[] parts = fullPath.split("\\?",2) ;
		
		Map<String,String> queryParameters = null ;
		
		if (parts.length > 1 && parts[1].length() > 0) {
			try {
				queryParameters = new HashMap<String, String>() ;
				
				String[] pairs = parts[1].split("&") ;
				
				for (String pair : pairs) {
					String[] keyVal = pair.split("=",2);
					
					String key = URLDecoder.decode(keyVal[0], "UTF-8") ;
					String val = keyVal.length > 1 ? URLDecoder.decode(keyVal[1], "UTF-8") : null ;
					
					queryParameters.put(key, val) ;
				}
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	
		}
		
		return processRequest(parts[0] , queryParameters, session);
		
	}
	
	private WeakHashMap<Object, IRPCDataProviderHandler> providerHandlers = new WeakHashMap<Object, IRPCDataProviderHandler>() ;
	
	private IRPCDataProviderHandler getDataProviderHandler(String providerPath) {
	
		synchronized (dataProviders) {
			Object provider = dataProviders.get(providerPath) ;
			
			if (provider == null) return null ;
			
			IRPCDataProviderHandler providerHandler = providerHandlers.get(provider) ;
			if (providerHandler != null) return providerHandler ;
			
			providerHandler = new IRPCDataProviderHandler(this, provider) ;
			
			providerHandlers.put(provider, providerHandler) ;
			
			return providerHandler ;
		}
		
	}
	
	public IRPCResponse processRequest(String path, Map<String,String> queryParameters, IRPCSession session) {
		if (session == null) {
			session = new IRPCSessionWrapper("0", new HashMap<String,Object>()) ;
		}
		
		IRPCEventTable eventTable = getEventTable(session) ;
		
		IRPCResponse response = new IRPCResponse(eventTable) ;
		
		IRPCRequest request = new IRPCRequest(path, queryParameters) ;
		
		if ( request.eventUpdateRequest ) {
			processEventTableUpdate(response, session, request.eventUpdateLastID);
			
			return response ;
		}
		else {
			
			IRPCDataProviderHandler dataProviderHandler = getDataProviderHandler( request.providerPath ) ;
			
			if (dataProviderHandler == null) {
				response.pathNotFound = true ;
				return response ;
			}
			
			dataProviderHandler.process(request, response, session);
			
			return response ;
		}
		
	}

	static final String SESSION_IRPC_EVENT_TABLE = "__IRPC_EVENT_TABLE__";
	  
	private IRPCEventTable getEventTable( IRPCSession session ) {
	    IRPCEventTable eventTable = (IRPCEventTable) session.get(SESSION_IRPC_EVENT_TABLE) ;
	    
	    if (eventTable == null) {
	      eventTable = new IRPCEventTable() ;
	      session.put(SESSION_IRPC_EVENT_TABLE , eventTable) ;
	    }
	    
	    return eventTable ;
	  }
	  
	
	private void processEventTableUpdate( IRPCResponse response , IRPCSession session , int lastEventId ) {
	    IRPCEventTable eventTable = getEventTable(session) ;
	    
	    eventTable.consumeUntilEventID(lastEventId) ;
	    
	    response.fullIRPCResponse = eventTable.toString() ;
	}

	
}
