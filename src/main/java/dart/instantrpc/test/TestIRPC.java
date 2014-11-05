package dart.instantrpc.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dart.instantrpc.IRPCEventTable;
import dart.instantrpc.IRPCProcessor;
import dart.instantrpc.IRPCResponse;

public class TestIRPC {

	
	static class Foo {
		
		public String test1(int a, String b, List<String> l, Map<String,Object> m) {
			IRPCResponse response = IRPCResponse.get() ;
			
			IRPCEventTable.get().addEvent("test", "a","1"  , "b","2");
			
			System.out.println("test>>> "+ a +" > "+ b +" > "+ l +" > "+ m +" >>> "+ response);
			
			response.print("MOHHHH>> "+ a +" > "+ b +" > "+ l +" > "+ m +" >>> "+ response);
			
			if (l != null) { 
				for (Object e : l) {
					System.out.println("> "+ e +" > "+ e.getClass() );
				}
			}
			
			System.out.println("-----------------");
			
			if (m != null) {
				for (Entry<String, Object> entry : m.entrySet()) {
					System.out.println("> "+ entry +" > "+ entry.getKey().getClass() +" > "+ entry.getValue().getClass());
				}
			}
			
			return "test1:ret#"+ System.currentTimeMillis() ;
		}
		
	}
	
	public static void main(String[] args) {
		
		IRPCProcessor irpcProcessor = new IRPCProcessor() ;
		
		Foo foo = new Foo() ;
		
		irpcProcessor.registerDataProvider("/foo", foo);
		
		IRPCResponse response = irpcProcessor.processRequest("/foo/test1?0=123&1=abcdefg&2=[1,2,'aa bb']&3={'a':1 , 'b':2}", null);
		
		System.out.println( response );
		System.out.println( response.getOutput() );
		System.out.println( response.buildIRPCResponse() );
		
		
	}
	
}
