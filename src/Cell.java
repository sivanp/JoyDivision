import ij.IJ;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

import java.awt.Polygon;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;


public class Cell implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Cells parentCells;
	protected int id;


	Cell(int id,Cells parentCells)
	{
		this.parentCells = parentCells;
		this.id = id;
	}


	public int getId() 
	{
		return id;
	}

	@Override
	public boolean equals(Object o) 
	{
		if (!( o instanceof Cell))
		{
			return false;
		}
		Cell other = (Cell)o;
		return this.id == other.getId();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return  new Integer(id).hashCode();
	}

	@Override
	public String toString() 
	{
		String res="Cell id:"  + new Integer(id).toString();
		Set<Integer> frames=getFrames();
		if(frames!=null){
			Iterator<Integer> iter = frames.iterator();
			while(iter.hasNext()){
				Integer frame=iter.next();
				PolyProperty roi=this.getLocationInFrame(frame);
				res+=" "+frame+": "+roi.toString()+";";
			}		
		}
		Set<Cell> daughters=this.getDaughters();
		if(daughters==null || daughters.isEmpty()){
			return res;
		}
		res+="\nDaughters:";
		Iterator<Cell> diter=daughters.iterator();
		while(diter.hasNext()){
			Cell daughter=diter.next();
			res+=" "+daughter.getId();
		}
		return res;
	}

	public SortedSet<Integer> getFrames()
	{
		return parentCells.getCl().getFrames(this);
	}


	
	/**
	 * 
	 * @return the Set of daughters. If no daughters of this cells exists returns null
	 */
	public Set<Cell> getDaughters()
	{
		Set<Cell> daughters=parentCells.getCp().getByParent(this);
		if(daughters==null ||daughters.isEmpty()){
			return null;
		}
		return daughters;
	}

	/**
	 * 
	 * @return the Set of mothers. If no mothers of this cells exists returns null
	 */
	public Set<Cell> getMothers()
	{	
		Set<Cell> moms=parentCells.getCp().getByChild(this);
		if(moms==null || moms.isEmpty()){
			return null;
		}
		
		return moms; 
	}

	public void addMother(Cell mother)
	{
		if(!parentCells.contains(mother) || !parentCells.contains(this)){
			IJ.showMessage("cannot add: this or mother does not exist in structure ");
			return;
		}
			
		if(this.equals(mother)){
			IJ.showMessage("cannot add: mother is the same as this cell- cannot add");
			return;
		}
		//no circles are allowed- mother cannot be one of this cell descendants
		Set<Cell> desc=getAllDescendants();
		if(desc.contains(mother)){
			IJ.showMessage("Cannot add mother: mother is a descendant of this cell");
			return;
		}
		parentCells.getCp().put(mother,this);
	}
	
	public Set<Cell> getAllDescendants(){
		Set<Cell> desc=new HashSet<Cell>();
		Set<Cell> daughts=this.getDaughters();
		if(daughts==null || daughts.isEmpty()){
			return desc;
		}		
		Iterator<Cell> diter=daughts.iterator();
		while(diter.hasNext()){
			Cell d=diter.next();
			Set<Cell> gds= d.getAllDescendants(desc);
			desc.addAll(gds);	
			desc.add(d);
		}
		return desc;
	}
	
	private Set<Cell> getAllDescendants(Set<Cell> desc){
		Set<Cell> daughts=this.getDaughters();
		if(daughts==null || daughts.isEmpty()){
			return new HashSet<Cell>();
		}		
		Iterator<Cell> diter=daughts.iterator();
		while(diter.hasNext()){
			Cell d=diter.next();
			Set<Cell> gds= d.getAllDescendants(desc);
			desc.addAll(gds);
			desc.add(d);
		}
		return desc;
	}
	

	public void addDaughter(Cell daughter)
	{
		if(!parentCells.contains(daughter) || !parentCells.contains(this)){
			IJ.showMessage("cannot add: this or daughter does not exist in structure ");
			return;
		}
		if(this.equals(daughter)){
			IJ.showMessage("daughter is the same as this cell- cannot add");
		}
		//no circles are allowed- daughter cannot have this as one of its descendants
		Set<Cell> desc=daughter.getAllDescendants();
		if(desc.contains(this)){
			IJ.showMessage("Cannot add daughter: daughter is an ancestor of this cell");
			return;
		}
		parentCells.getCp().put(this,daughter);
	}

	public void removeMother(Cell mother)
	{
		parentCells.getCp().removeMomDaughter(mother, this);
	}

	public void removeDaughter(Cell daughter)
	{
		parentCells.getCp().removeMomDaughter(this, daughter);
	}

	public void addLocation(int frame , Roi roi) 
	{
		//converting the ROI to polygon ROI that will not hold imagePlus that screws up serialization...
		Polygon poly=roi.getPolygon();
		roi=new PolygonRoi(poly,Roi.FREEROI);
		
		//TODO should we conduct checks such that a mother cell cannot continue in frames following daughters birth?
		parentCells.getCl().addLocationToCell(this, frame, roi);
	}
	
	public void addLocation(int frame , PolyProperty proi) 
	{
		parentCells.getCl().addLocationToCell(this, frame, proi);
	}

	public PolyProperty getLocationInFrame(int frame){
		
		return parentCells.getCl().getCellLocationInFrame(this, frame);
	}

	/**
	 * Dynamically goes over the parentsCells structure to issue a name for this cell 
	 * @return a String representing the name of this cell
	 */
	public String getName(){
		Set<Cell> moms=this.getMothers();
		if(moms==null || moms.isEmpty()){
			return String.valueOf(id);
		}
		else{			
			Cell mom=moms.iterator().next();			
			//numbering siblings and giving my number in the sorted list 
			int rank=1;
			Set<Cell> siblings=mom.getDaughters();
			Iterator<Cell> siter=siblings.iterator();
			while(siter.hasNext()){
				Cell sis=siter.next();
				if(sis.getId()<id){
					rank++;
				}
			}
			String name=getName(mom,"")+"_"+rank;
			return name;
		}

	}

	private String getName(Cell mom,String name){
		Set<Cell> moms=mom.getMothers();
		if(moms==null || moms.isEmpty()){
			return String.valueOf(mom.getId());
		}
		else{
			Cell gmom=moms.iterator().next();
			name=getName(gmom,name);

			int rank=1;
			Set<Cell> siblings=gmom.getDaughters();
			Iterator<Cell> siter=siblings.iterator();
			while(siter.hasNext()){ 
				Cell sis=siter.next();
				if(sis.getId()<mom.getId()){
					rank++;
				}
			}
			return name+"_"+rank;			
		}
	}


	
	 boolean deleteLocation(int frame) {
		 boolean res=false;
		res=parentCells.removeCellLocation(this, frame);
		return res;
	}

}
