
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class CellParents implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//private Map<Integer, HashSet<Integer>> byParent;
	private Map<Cell, HashSet<Cell>> byChild;
	private Map<Cell, HashSet<Cell>> byParent;
	public CellParents()
	{
		byParent = new HashMap<Cell, HashSet<Cell>> ();
		byChild = new HashMap<Cell, HashSet<Cell>> ();
	}

	public void clear() 
	{
		byParent.clear();
		byChild.clear();
		
	}

	
	
	public boolean containsParent(Cell cell) 
	{	
		return byParent.containsKey(cell);
		
	}
	
	public boolean containsChild(Cell cell) 
	{		
		return byChild.containsKey(cell);
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
		HashSet<Cell> childs=byParent.get(mom);
		if(childs ==null){
			childs=new HashSet<Cell>();
		}
		childs.add(daughter);
		byParent.put(mom,childs);
		return childs;		
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
		return byParent.get(parent);
	}
	
	public void removeCell(Cell cell)
	{
		//go over all children and erase this cell from their byChild mapping
		Set<Cell> daughters=byParent.get(cell);
		if(daughters!=null){
			Iterator<Cell> iter=daughters.iterator();
			while(iter.hasNext()){
				Cell daughter = iter.next();
				byChild.get(daughter).remove(cell);
			}
		}
		//go over all parents and erase this cell from their mothers mapping
		Set<Cell> moms=byChild.get(cell);
		if(moms!=null){
			Iterator<Cell> iter2=moms.iterator();
			while(iter2.hasNext()){
				Cell mom = iter2.next();
				byParent.get(mom).remove(cell);
			}
		}		
		byParent.remove(cell);		
		byChild.remove(cell);
	}
	
	public boolean removeMomDaughter(Cell mom, Cell daughter){
		boolean res=false;
		HashSet<Cell> moms=byChild.get(daughter);
		if(moms!=null){
			moms.remove(mom);
			byChild.put(daughter, moms);
		}		
		HashSet<Cell> kids=byParent.get(mom);
		if(kids!=null){
			res=kids.remove(daughter);
			byParent.put(mom, kids);
		}			
		return res;
	}
	
}
