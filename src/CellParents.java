
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class CellParents extends HashMap<Cell, HashSet<Cell>>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//private Map<Integer, HashSet<Integer>> byParent;
	private Map<Cell, HashSet<Cell>> byChild;
	
	public CellParents()
	{
//		byParent = new HashMap<Integer, HashSet<Integer>> ();
		byChild = new HashMap<Cell, HashSet<Cell>> ();
	}

	public void clear() 
	{
		super.clear();
		byChild.clear();
		
	}

	
	@Override
	public boolean containsValue(Object value) 
	{		
		return byChild.containsKey(value);
	}
	
		
	public Set<Cell> childKeySet() 
	{
		return byChild.keySet();
	}
	

	
	/**
	 * 
	 * @param mom
	 * @param daughter
	 * @return HashSet of all the children associated with the given key (parent id)
	 */
	public HashSet<Cell> put(Cell mom, Cell daughter) 
	{
		HashSet<Cell> parents=byChild.get(daughter);
		if(parents ==null){
			parents=new HashSet<Cell>();
		}
		parents.add(mom);
		byChild.put(daughter, parents);
		HashSet<Cell> childs=this.get(mom);
		if(childs ==null){
			childs=new HashSet<Cell>();
		}
		childs.add(daughter);
		this.put(mom,childs);
		return childs;
		
		
	}
	
	@Override
	public HashSet<Cell> put(Cell key, HashSet<Cell> value) 
	{
		Iterator<Cell> viter=value.iterator();
		while(viter.hasNext()){
			Cell child=viter.next();
			HashSet<Cell> parents=byChild.get(child);
			if(parents ==null){
				parents=new HashSet<Cell>();
			}
			parents.add(key);
			byChild.put(child, parents);
		}
		
		
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends Cell, ? extends HashSet<Cell>> m) 
	{
		Set<? extends Cell> parents=m.keySet();
		Iterator<? extends Cell> piter=parents.iterator();
		while(piter.hasNext())
		{
			Cell p=piter.next();
			HashSet<Cell> childs = this.get(p);
			this.put(p, childs);
		}
		
	}

	@Override
	public HashSet<Cell> remove(Object key) {
		//remove this parent key from all cells
		HashSet<Cell> value=this.get(key);
		Iterator<Cell> viter=value.iterator();
		while(viter.hasNext()){
			Cell child=viter.next();
			HashSet<Cell> parents=byChild.get(child);
			parents.remove(key);
		}
		return super.remove(key);
	}

//	public HashSet<Cell> removeMother(Cell mother) {
//		return remove(mother);
//	}
//	
//	public HashSet<Cell> removeDaughter(Cell daughter) {
//		//remove this parent key from all cells
//		HashSet<Cell> value=byChild.get(daughter);
//		Iterator<Cell> viter=value.iterator();
//		while(viter.hasNext()){
//			Cell parent=viter.next();
//			HashSet<Cell> childs=this.get(parent);
//			childs.remove(daughter);
//		}
//		return byChild.remove(daughter);
//	}
//	
	public Set<Cell> getByChild(Cell child){
		return byChild.get(child);
	}
	
	public Set<Cell> getByParent(Cell parent){
		return this.get(parent);
	}
	
	public void removeCell(Cell cell)
	{
		this.remove(cell);
		byChild.remove(cell);
	}
	
	public boolean removeMomDaughter(Cell mom, Cell daughter){
		boolean res=false;
		HashSet<Cell> moms=byChild.get(daughter);
		if(moms!=null){
			moms.remove(mom);
			byChild.put(daughter, moms);
		}		
		HashSet<Cell> kids=this.get(mom);
		if(kids!=null){
			res=kids.remove(daughter);
			this.put(mom, kids);
		}			
		return res;
	}
	
}
