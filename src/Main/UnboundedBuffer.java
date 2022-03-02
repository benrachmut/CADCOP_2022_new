package Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class UnboundedBuffer<T>{

	private List<List<T>> buffer;
	public static int bufferId;
	private int id;
	public UnboundedBuffer() {
		buffer = new ArrayList<List<T>>();
		bufferId=+1;
		this.id = bufferId;
	}
	@Override
	public String toString() {
		return "buffer_"+this.id;
	}

	public synchronized void insert(List<T> item) {
		buffer.add(item);
		this.notifyAll();
	}

	public synchronized List<T> extract() {
		while (buffer.isEmpty() ) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		List<T> ans = new ArrayList<T>();
		
		for (List<T> l1 : buffer) {
			if (l1 == null) {
				return null;
			}
			for (T t : l1) {
				ans.add(t);
			}
		}
	
		
		buffer.clear();
		return ans;
	}
	public synchronized void removeAllMsgs() {
		buffer.clear();
		
	}
	public boolean isEmpty() {
		return this.buffer.isEmpty();
	}
}



