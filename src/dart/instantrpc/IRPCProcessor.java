package dart.instantrpc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

final public class IRPCProcessor {

	final private HashMap<String, Object> dataProviders = new HashMap<>() ;
	
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
	
	public IRPCResponse processRequest(String fullPath, Map<String,Object> sessionMap, String sessionId) {
		
		String[] parts = fullPath.split("\\?",2) ;
		
		Map<String,String> queryParameters = null ;
		
		if (parts.length > 1 && parts[1].length() > 0) {
			try {
				queryParameters = new HashMap<String, String>() ;
				
				String[] pairs = parts[1].split("&") ;
				
				for (String pair : pairs) {
					String[] keyVal = pair.split("=",2);
					
					String key = URLDecoder.decode(keyVal[0], "UTF-8") ;
					String val = URLDecoder.decode(keyVal[1], "UTF-8") ;
					
					queryParameters.put(key, val) ;
				}
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	
		}
		
		return processRequest(parts[0] , queryParameters, sessionMap, sessionId);
		
	}
	
	private WeakHashMap<Object, IRPCDataProviderHandler> providerHandlers = new WeakHashMap<>() ;
	
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
	
	public IRPCResponse processRequest(String path, Map<String,String> queryParameters, Map<String,Object> sessionMap, String sessionId) {
		
		IRPCResponse response = new IRPCResponse() ;
		
		IRPCRequest request = new IRPCRequest(path, queryParameters) ;
		
		if ( request.eventUpdateRequest ) {
			
			return response ;
		}
		else {
			
			IRPCDataProviderHandler dataProviderHandler = getDataProviderHandler( request.providerPath ) ;
			
			if (dataProviderHandler == null) {
				response.pathNotFound = true ;
				return response ;
			}
			
			IRPCSessionWrapper session = sessionMap != null ? new IRPCSessionWrapper(sessionId, sessionMap) : new IRPCSessionWrapper("0", new HashMap<String, Object>()) ;
			
			dataProviderHandler.process(request , response, session);
			
			return response ;
		}
		
	}
	
}
