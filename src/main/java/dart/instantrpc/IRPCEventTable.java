package dart.instantrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IRPCEventTable {

	static public IRPCEventTable get() {
		IRPCResponse response = IRPCResponse.get() ;
		return response != null ? response.getEventTable() : null;
	}

	// //////////////////////////////////////////////////////////////

	static final protected String MARK_EVENT_DELIMITER = "!_EVT_!";

	private List<IRPCEvent> events = new ArrayList<IRPCEvent>();

	private int lastConsumedEvent = 0;

	public int getLastConsumedEvent() {
		return lastConsumedEvent;
	}

	private int lastCreatedEvent = 0;
	private int maxKnownEventId = 0;

	public int getMaxKnownEventId() {
		return maxKnownEventId;
	}

	public void addEvent(String type) {
		addEvent(type, (Map<String,String>)null);
	}
	
	public void addEvent(String type, String... parametersPairs) {
		HashMap<String, String> map = new HashMap<String,String>() ;
		
		for (int i = 0; i < parametersPairs.length; i+=2) {
			String k = parametersPairs[i];
			String v = parametersPairs[i+1];
			
			map.put(k, v) ;
		}
		
		addEvent(type, map);
	}

	public void addEvent(String type, Map<String, String> params) {
		add(new IRPCEvent(type, params));
	}

	public void add(IRPCEvent event) {
		event.setID( ++lastCreatedEvent ) ;

		events.add(event);

		updateMaxKnownEventId(event.getID());
	}

	private void updateMaxKnownEventId(int id) {
		if (maxKnownEventId < id)
			maxKnownEventId = id;
	}

	public int getTotalEventsToConsume() {
		return events.size();
	}

	private List<IRPCEvent> consumedEvents = new ArrayList<IRPCEvent>();
	private int consumedEventsDeleteCount = 0;

	public int getTotalConsumedEvents() {
		return consumedEvents.size() + consumedEventsDeleteCount;
	}

	public int getTotalConsumedEventsDeleted() {
		return consumedEventsDeleteCount;
	}

	public int deleteConsumedEvents() {
		return deleteConsumedEvents(1000);
	}

	public int deleteConsumedEvents(int maxEventsInList) {
		if (maxEventsInList < 0)
			maxEventsInList = 0;

		int del = 0;

		while (consumedEvents.size() > maxEventsInList) {
			if (deleteConsumedEvent()) {
				del++;
			} else {
				break;
			}
		}

		return del;
	}

	public boolean deleteConsumedEvent() {
		if (consumedEvents.isEmpty())
			return false;

		consumedEvents.remove(0);
		consumedEventsDeleteCount++;

		return true;
	}

	protected int consumeUntilEventID(int eventID) {
		int del = 0;

		while (!events.isEmpty()) {
			IRPCEvent event = events.get(0);

			if (event.getID() <= eventID) {
				events.remove(0);
				del++;
			} else {
				break;
			}
		}

		return del;
	}

	public boolean hasEventToConsume() {
		return !events.isEmpty();
	}

	public IRPCEvent consumeEvent() {
		if (events.isEmpty())
			return null;

		IRPCEvent event = events.remove(0);
		consumedEvents.add(event);

		if (lastConsumedEvent + 1 != event.getID())
			throw new IllegalStateException("Out of sync consumed events!");
		lastConsumedEvent = event.getID();

		return event;
	}

	protected void update(IRPCEventTable other) {
		if (this == other)
			return;

		this.lastConsumedEvent = other.lastConsumedEvent;
		this.lastCreatedEvent = other.lastCreatedEvent;
		this.consumedEventsDeleteCount = other.consumedEventsDeleteCount;

		this.events = other.events;
		this.consumedEvents = other.consumedEvents;

		if (!events.isEmpty())
			updateMaxKnownEventId(events.get(events.size() - 1).getID());
		if (!consumedEvents.isEmpty())
			updateMaxKnownEventId(consumedEvents.get(events.size() - 1)
					.getID());

		System.out.println("UPDATE EVT TBL<<<\n${ this.toString() }\n>>>");
	}

	protected void merge(IRPCEventTable other) {
		if (this == other)
			return;

		int lastId = !this.events.isEmpty() ? this.events.get(
				events.size() - 1).getID() : this.lastConsumedEvent;

		for (IRPCEvent e : other.events) {

			if (lastId + 1 == e.getID()) {
				this.events.add(e);
				lastId = e.getID();

				updateMaxKnownEventId(lastId);
			} else if (e.getID() <= lastId) {
				continue;
			} else {
				throw new IllegalStateException("Out of sync event table!");
			}

		}

	}

	public IRPCEventTable() {
	}

	public IRPCEventTable(String str) {

		int splitIdx1 = str.indexOf("\n");

		String head = str.substring(0, splitIdx1);

		String[] headValues = head.split(";");

		this.lastConsumedEvent = Integer.parseInt(headValues[0]);
		this.lastCreatedEvent = Integer.parseInt(headValues[1]);
		int eventsSz = Integer.parseInt(headValues[2]);

		String strEvents = str.substring(splitIdx1 + 1);

		String[] events = strEvents.split(MARK_EVENT_DELIMITER);

		for (int i = 0; i < eventsSz; i++) {
			this.events.add(new IRPCEvent(events[i]));
		}

		for (int i = eventsSz; i < events.length; i++) {
			String s = events[i];
			if (s.isEmpty())
				continue;
			this.consumedEvents.add(new IRPCEvent(s));
		}

	}

	static private String join(List<?> l, String delimiter) {
		StringBuilder str = new StringBuilder();

		int i = 0;
		for (Object s : l) {
			i++;

			if (i > 1)
				str.append(delimiter);

			str.append(s.toString());
		}

		return str.toString();
	}

	@Override
	public String toString() {
		String str = lastConsumedEvent +";"+ lastCreatedEvent +";"+ events.size() +"\n";

		str += join(events, MARK_EVENT_DELIMITER);
		str += MARK_EVENT_DELIMITER;
		str += join(consumedEvents, MARK_EVENT_DELIMITER);

		return str;
	}

}
