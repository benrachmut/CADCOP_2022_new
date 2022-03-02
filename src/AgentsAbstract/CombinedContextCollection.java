package AgentsAbstract;

import java.util.HashSet;
import java.util.Set;

public class CombinedContextCollection {

	private Set<Integer>ids;
	private Set<Context>contexts;
	public CombinedContextCollection(Set<Integer> ids) {
		super();
		this.ids = ids;
		this.contexts = new HashSet<Context>();
	}
	public void restart() {
		this.contexts = new HashSet<Context>();
		
	}
	public boolean isIdInCCC(int creator) {
		
		return ids.contains(creator);
	}
	public void addContext(Context combined) {
		this.contexts.add(combined);
	}
	public  Set<Context> getContexts() {
		// TODO Auto-generated method stub
		return contexts;
	}
	public Set<Integer> getIds() {
		return this.ids;
	}
	public boolean onlyIdOfIntput(int iCreatedWith) {
		if (this.ids.contains(iCreatedWith) && this.ids.size() ==1) {
			return true;
		}
		return false;
	}
}
