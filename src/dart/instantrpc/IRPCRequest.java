package dart.instantrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class IRPCRequest {

	static final String REQUEST_EVENT_UPDATE = "__IRPC_EVT__" ;
	static final String REQUEST_EVENT_SYNCH_ID = "__IRPC_EVT_ID__" ;
	
	String providerPath ;
	String methodName ;
	
	List<String> positionalParams ;
	Map<String,String> namedParams ;
	
	int lastEventTableId ;
	
	boolean eventUpdateRequest ;
	int eventUpdateLastID ;
	
	
	public IRPCRequest(String path, Map<String,String> query) {
		
		String[] pathSplit = path.split("\\/") ;
		
		String methodName = pathSplit[ pathSplit.length-1 ] ;
		
		this.providerPath = path.substring(0 , path.length() - (methodName.length() + 1) ) ;
		
	    if ( methodName == REQUEST_EVENT_UPDATE ) {
	    	int id = query.containsKey("id") ? Integer.parseInt( query.get("id") ) : 0 ;
	    	
	    	this.eventUpdateRequest = true ;
	    	this.eventUpdateLastID = id ;
	    }
	    else {
	    	
	    	this.methodName = methodName ;
	    	
	    	String lastEventTableIdStr = query.get(REQUEST_EVENT_SYNCH_ID) ;
	        
	        this.lastEventTableId = lastEventTableIdStr != null ? Integer.parseInt(lastEventTableIdStr) : 0 ;
	        
	        List<String> positionalParams = new ArrayList<>() ;
	        
	        int lastAddedIdx = -1 ;
	        for (int i = 0 ; i <= 20 ; i++) {
	          String val = query.get(""+i) ;
	          
	          if (val != null) {
	            for (int j = lastAddedIdx+1 ; j < i ; j++) {
	              positionalParams.add(null) ;  
	            }
	            
	            positionalParams.add(val) ;
	            
	            lastAddedIdx = i ;
	          }
	        }
	        
	        Map<String,String> namedParams = new HashMap<String, String>() ;
	        
	        for (Entry<String, String> entry : query.entrySet()) {
	        	String k = entry.getKey() ;
				if ( !k.isEmpty() ) {
					char c = k.charAt(0) ;
					
					if ( !(c >= 48 && c <= 57 && k.matches("^\\d+$") ) ) {
						namedParams.put(k, entry.getValue()) ;
					}
				}
			}
	        
	        
	        this.positionalParams = positionalParams ;
	        this.namedParams = namedParams ;
	    }
	    
	}

}
