package server;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

/** 
 * @author Graham Stubbs (wgs11@georgetown.edu)
 * @author Cooper Logerfo (cml264@georgetown.edu)
 */
public class GameQueue<T> {
	
	private Stack<T> s1;
	private Stack<T> s2;
	
	public GameQueue() {
		s1 = new Stack<T>();
		s2 = new Stack<T>();
	}

	public T dequeue() {
		if (s2.empty()) {
			int size = s1.size();
			for (int i=0; i<size; i++) {
				T t = s1.pop();
				s2.push(t);
			}
			
			if (s2.empty()) {
				throw new EmptyStackException();
			}
			else
				return s2.pop();
		}
		else
			return s2.pop();
	}
	
	public ArrayList<T> dequeuePair() {
		if (!isPairAvailable())
			return null;
		
		T first = this.dequeue();
		T second = this.dequeue();
		
		ArrayList<T> pair = new ArrayList<T>(2);
		pair.add(first);
		pair.add(second);
		
		return pair;
	}
	
	public void enqueue(T newItem) {
		s1.push(newItem);
	}
	
	public boolean isPairAvailable() {
		T first = this.peek();
		if (first == null) 
			return false;
		else {
			s2.pop();
			T second = this.peek();
			s2.push(first);
			
			if (second == null)
				return false;
			else 
				return true;
		}
	}

	public T peek() {
		T output;
		try {
			output = this.dequeue();
		} catch (EmptyStackException e) {
			return null;
		}
		
		s2.push(output);
		
		return output;
	}
	
	public void putBack(T t) {
		s2.push(t);
	}
}
