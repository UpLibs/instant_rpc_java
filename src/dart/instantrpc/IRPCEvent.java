package dart.instantrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

final public class IRPCEvent {
	private int id;
	private String type;
	private Map<String, String> parameters;

	public IRPCEvent(String str) {
		int splitIdx1 = str.indexOf(";");
		int splitIdx2 = str.indexOf("\n");

		this.id = Integer.parseInt(str.substring(0, splitIdx1));
		this.type = str.substring(splitIdx1 + 1, splitIdx2);

		String paramsStr = str.substring(splitIdx2 + 1);

		String[] lines = paramsStr.split("\n");

		Map<String, String> params = new HashMap<>();

		for (String l : lines) {
			int idx = l.indexOf("=");

			if (idx >= 0) {
				String k = l.substring(0, idx);
				String v = l.substring(idx + 1);
				params.put(k, v);
			}
		}

		this.parameters = params;
	}

	public IRPCEvent(String type, Map<String, String> parameters) {
		this.id = 0;
		this.type = type ;
		this.parameters = parameters ;

		this.parameters = parameters != null ? parameters
				: new HashMap<String, String>();

		check();
	}

	private void check() {
		if (this.type == null || this.type.contains("\n") || this.type.contains(IRPCEventTable.MARK_EVENT_DELIMITER))
			throw new IllegalArgumentException("Invalid type: " + type);

		for (Entry<String, String> entry : this.parameters.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();

			if (k.contains("\n") || k.contains("=")
					|| k.contains(IRPCEventTable.MARK_EVENT_DELIMITER))
				throw new IllegalArgumentException("Invalid key: "+ k);
			if (v.contains("\n")
					|| k.contains(IRPCEventTable.MARK_EVENT_DELIMITER))
				throw new IllegalArgumentException("Invalid value: "+ v);
		}
		;

	}

	public int getID() {
		return id;
	}
	
	protected void setID(int id) {
		this.id = id ;
	}

	public String getType() {
		return type;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		String str = id + ";" + type + "\n";

		for (Entry<String, String> entry : this.parameters.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();

			str += k + "=" + v + "\n";
		}
		;

		return str;
	}

}
