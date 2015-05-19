package se.caplogic.mappedbus.integrity;

import java.io.File;
import java.util.Arrays;

import se.caplogic.mappedbus.MappedBusReader;
import se.caplogic.mappedbus.MappedBusWriter;

public class IntegrityTester {
	
	public static final String FILE_NAME = "/tmp/integrity-test";
	
	public static final long FILE_SIZE = 4000000L;
	
	public static final int NUM_WRITERS = 10;
	
	public static final int RECORD_LENGTH = 10;
	
	public static final int NUM_RECORDS = 10000;
	
	public static void main(String[] args) throws Exception {
		IntegrityTester tester = new IntegrityTester();
		tester.run();
	}
	
	public void run() throws Exception {
		new File(FILE_NAME).delete();

		Writer[] writers = new Writer[NUM_WRITERS];
		for(int i = 0; i < writers.length; i++) {
			writers[i] = new Writer(i);
		}
		for(int i = 0; i < writers.length; i++) {
			writers[i].start();
		}
		for(int i = 0; i < writers.length; i++) {
			writers[i].join();
		}
		
		MappedBusReader reader = new MappedBusReader(FILE_NAME, FILE_SIZE, RECORD_LENGTH);
		byte[] data = new byte[RECORD_LENGTH];
		while(reader.hasNext()) {
			if(!reader.next()) {
				continue; // the record was abandoned, skip it
			}
			reader.readBuffer(data, 0);
			for(int i=0; i < data.length; i++) {
				System.out.println("Read: " + Arrays.toString(data));
				if(data[0] != data[i]) {
					System.out.println("---");
					System.out.println("TEST FAILED!");
					return;
				}
			}
		}
		System.out.println("---");
		System.out.println("Test passed.");
	}

}

class Writer extends Thread {
	
	private final int id;
	
	public Writer(int id) {
		this.id = id;
	}
	
	public void run() {
		try {
			MappedBusWriter writer = new MappedBusWriter(IntegrityTester.FILE_NAME, IntegrityTester.FILE_SIZE, IntegrityTester.RECORD_LENGTH, true);

			byte[] data = new byte[IntegrityTester.RECORD_LENGTH];
			Arrays.fill(data, (byte)id);
			
			for(int i=0; i < IntegrityTester.NUM_RECORDS; i++) {
				writer.write(data, 0, data.length);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}