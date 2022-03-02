package AgentsAbstract;
import java.util.Collections;
/**
 * 
 * @author Omer Perry
 *
 */
public class NodeId implements Comparable<NodeId> {

    ///// ******* Variables ******* ////

	public enum NodeType {Variable, Function}; 
	private NodeType type;
	private int id1;
	private int id2;
	private boolean isPlusOne;
	//-----------------------------------------------------------------------------------------------------------//

	//OmerP - Constructor for Variable Node.
	public NodeId(int id1, boolean isPlusOne) {
		this.isPlusOne = isPlusOne;

		if (isPlusOne) {
			this.id1 = id1+1; //shouldnt be plus one?  
		}else {
		this.id1 = id1;
		}
		this.id2 = 0;
		setType(NodeType.Variable);
		
	}
	
	//OmerP - Constructor for Function Node.
	public NodeId(int id1, int id2) {
		this.isPlusOne = true;
		this.id1 = id1+1; 
		this.id2 = id2+1;
		setType(NodeType.Function);

	}
	
    ///// ******* Compare and Equals ******* ////
	

	
	@Override
	public int compareTo(NodeId toCheck) {
		// TODO Auto-generated method stub
		
		if (toCheck.id1 - this.id1 == 0 ) {
			return toCheck.id2- this.id2;
		}
		return toCheck.id1 - this.id1;
		
		/*
		if(this.id1 < toCheck.id1) { //Variable node this is in before variable node toCheck
			
			return 1; 
			
		}
		
		if(this.id1 > toCheck.id1) { //Variable node this is in after variable node toCheck

			return -1; 
			
		}
		
		else { //For the case that are both variable nodes have the same id1, than its a function node comparison. 
			
			 if(this.id2 < toCheck.id2) { //Function node this is before function node toCheck. 
				 
				 return 1; 
				 
			 }
			 
			 if(this.id2 > toCheck.id2) { //Function node this is after function node toCheck.
				 
				 return -1;
				 
			 }
			 
			 else {
				 
				 return 0; //The same function node. 
				 
			 }
			
		}
		*/
			
	}
		
    ///// ******* Getters ******* ////

	public int getId1() {
		
		return id1; 
		
	}
	
	public int getId2() {
		
		return id2; 
		
	}
	
    ///// ******* Setter ******* ////

	public void setId1(int id1) {
		
		this.id1 = id1;
		
	}
	
	public void setId2(int id2) {
		
		this.id2 = id2;
		
	}
	
	public void setType(NodeType type) {
		
		this.type = type;
		
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof NodeId) {
			NodeId other  = (NodeId)obj;
			if (other.getId1() == this.id1 && other.getId2() == this.id2) {
				return true;
			}
			else {
				return false;
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
	     int hash = 17; 
	     hash = 31 * hash + this.id1;
	     hash = 31 * hash + this.id2;
	     return hash;
	 }
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "A_"+this.id1;
	}

	public boolean isPlusOne() {
		// TODO Auto-generated method stub
		return this.isPlusOne;
	}
	
	
	
	
}
