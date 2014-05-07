package dart.instantrpc;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class IRPCSessionWrapper extends IRPCSession {

	final private int id ;
	final private Map<String,Object> map ;
	
	public IRPCSessionWrapper(int id, Map<String,Object> map) {
		this.id = id ;
		this.map = map ;
	}
	
	@Override
	public int getID() {
		return id ;
	}

	@Override
	public void detroy() {
		throw new UnsupportedOperationException() ;
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public Object get(Object key) {
		return map.get(key);
	}

	public Object put(String key, Object value) {
		return map.put(key, value);
	}

	public Object remove(Object key) {
		return map.remove(key);
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		map.putAll(m);
	}

	public void clear() {
		map.clear();
	}

	public Set<String> keySet() {
		return map.keySet();
	}

	public Collection<Object> values() {
		return map.values();
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return map.entrySet();
	}
	
}
