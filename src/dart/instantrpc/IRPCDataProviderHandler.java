package dart.instantrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

final public class IRPCDataProviderHandler {

	final private IRPCProcessor processor ;
	final private Object dataProvider ;
	final private Class<?> dataProviderClass ;
	
	public IRPCDataProviderHandler(IRPCProcessor processor, Object dataProvider) {
		this.processor = processor;
		this.dataProvider = dataProvider;
		this.dataProviderClass = dataProvider.getClass() ;
	}
	
	public IRPCProcessor getProcessor() {
		return processor;
	}
	
	private HashMap<String, Method> methods = new HashMap<String, Method>() ;
	
	private Method getMethod(String methodName) {
		
		synchronized (methods) {
			Method method = methods.get(methodName) ;
			if (method != null) return method ;

			Method[] classMethods = dataProviderClass.getMethods() ;
			
			for (Method m : classMethods) {
				if ( m.getName().equals(methodName) ) {
					method = m ;
					break ;
				}
			}
			
			if (method == null) return null ;
			
			method.setAccessible(true);
			
			methods.put(methodName, method) ;
			
			return method ;
		}
			
	}
	
	@SuppressWarnings("unchecked")
	static private Object toType(String val , Class<?> type) {
		
		if ( type == String.class ) {
			return val ;
		}
		else if ( type == int.class || type == Integer.class ) {
			return val == null ? ( type.isPrimitive() ? (int)0 : null ) : Integer.parseInt(val) ;
		}
		else if ( type == boolean.class || type == Boolean.class  ) {
			return val == null ? ( type.isPrimitive() ? false : null ) : Boolean.parseBoolean(val) ;
		}
		else if ( type == long.class || type == Long.class  ) {
			return val == null ? ( type.isPrimitive() ? 0L : null ) : Long.parseLong(val) ;
		}
		else if ( type == double.class || type == Double.class  ) {
			return val == null ? ( type.isPrimitive() ? 0D : null ) : Double.parseDouble(val) ;
		}
		else if ( type == float.class || type == Float.class  ) {
			return val == null ? ( type.isPrimitive() ? 0F: null ) : Float.parseFloat(val) ;
		}
		else if ( type == short.class || type == Short.class  ) {
			return val == null ? ( type.isPrimitive() ? (short)0 : null ) : Short.parseShort(val) ;
		}
		else if (val == null) {
			return null ;
		}
		else if ( List.class.isAssignableFrom(type) ) {
			JSONArray jsonArray = new JSONArray(val) ;
			
			int sz = jsonArray.length() ;
			List<Object> list ;
			
			if ( ArrayList.class.isAssignableFrom(type) || List.class == type ) {
				list = new ArrayList<Object>(sz) ;
			}
			else {
				try {
					list = (List<Object>) type.newInstance() ;
				}
				catch (Exception e) {
					e.printStackTrace(); 
					list = new ArrayList<Object>(sz) ;
				}
			}
			
			for (int i = 0; i < sz; i++) {
				Object obj = jsonArray.get(i) ;
				list.add(obj) ;
			}
			
			return list ;
		}
		else if ( Map.class.isAssignableFrom(type) ) {
			JSONObject jsonObject = new JSONObject(val) ;
			
			Map<String, Object> map = new HashMap<String, Object>() ;
			
			if ( HashMap.class.isAssignableFrom(type) || Map.class == type ) {
				map = new HashMap<String, Object>() ;
			}
			else {
				try {
					map = (Map<String, Object>) type.newInstance() ;
				}
				catch (Exception e) {
					e.printStackTrace();
					map = new HashMap<>() ;
				}
			}
			
			for (String k : jsonObject.keySet()) {
				Object v = jsonObject.get(k) ;
				map.put(k, v) ;
			}
			
			return map ;
		}
		
		return null ;
	}
	
	protected void process( IRPCRequest request , IRPCResponse response ) {
		
		Method method = getMethod( request.methodName ) ;
		
		if (method == null) {
			response.ok = false ;
			response.pathNotFound = true ;
			return ;
		}
		
		Class<?>[] parameterTypes = method.getParameterTypes() ;
		
		Object[] args = new Object[ parameterTypes.length ] ;
		
		List<String> positionalParams = request.positionalParams;
		
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> classType = parameterTypes[i];
			
			if ( positionalParams.size() <= i ) {
				args[i] = toType(null, classType);
			}
			else {
				String paramVal = positionalParams.get(i) ;
				Object type = toType(paramVal, classType) ;
				args[i] = type ;
			}
		}
		
		
		
		try {
			IRPCResponse.setLocalResponse(response);
			
			method.invoke(dataProvider, args) ;
			response.ok = true ;
		}
		catch (InvocationTargetException e) {
			response.ok = false ;
			Throwable cause = e.getCause() ;
			
			if (cause != null && (cause instanceof Exception)) {
				response.error = (Exception) cause ;
				cause.printStackTrace();
			}
			else {
				response.error = e ;
				e.printStackTrace();
			}
		}
		catch (IllegalAccessException | IllegalArgumentException e) {
			response.ok = false ;
			response.error = e ;
			e.printStackTrace();
		}
		finally {
			IRPCResponse.removeLocalResponse(response);
		}
		
	}
	
}
