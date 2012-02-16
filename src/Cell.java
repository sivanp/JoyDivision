import ij.IJ;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

import java.awt.Polygon;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;


public class Cell implements Serializable, Comparable<Cell> 
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

	
	
	
	
	
	public void addMother(Cell mother){
		mother.addDaughter(this);
	}

	public void addDaughter(Cell daughter){
		if(!parentCells.contains(daughter) || !parentCells.contains(this)){
			IJ.showStatus("cannot add: one of the cells does not exist in structure ");
			return;
		}
		if(this.equals(daughter)){
			IJ.showStatus("daughter"+daughter.getId()+" is the same as mother- cannot add");
			return;
		}
		//no circles are allowed- if mother and daughter share a common ancestor,
		//than daughter distance from this ancestor should be larger than mother distance.
		//This distance also can be equal to allow fusion of sisters....
		Set<Cell> commonAnces=this.getCommonAncestors(daughter);
		for(Cell ances:commonAnces){
			int momDist=this.getDist(ances);
			int daughtDist = daughter.getDist(ances);
			if(momDist>=daughtDist){
				IJ.showStatus("cannot add daughter: "+daughter.getId()+" to mother "+this.getId()+": will cause circles");
				return;
			}
		}	
		//also check if mother appears in frames > first frame of daughter 
		int lastMomFrame=this.getFrames().last();
		SortedSet<Integer> dframes=daughter.getFrames();
		if(dframes!=null){ //not a new daughter
			int firstDaughtFrame=dframes.first();
			if(lastMomFrame>=firstDaughtFrame){
				IJ.showStatus("cannot add daughter: "+daughter.getId()+" to mother "+this.getId()+": mother appears later than daughter begins");
				return;
			}
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


	@Override
	public int compareTo(Cell otherCell) {
		// TODO Auto-generated method stub
		Integer thisId=new Integer(this.id);
		Integer otherId=new Integer(otherCell.getId());
		return thisId.compareTo(otherId);
	}
	
	
	/////////////////////// Part of lineages messing around////////////////////
	
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
	
	
	
	/*
	 * Returns the Set of Cells which are the ancestors (cells without mohter) of this cell. If this cell has no mother returns itself.
	 */
	public Set<Cell> getAncestors(){
		Set<Cell> ances=new HashSet<Cell>();
		Stack<Cell> linStack= new Stack<Cell>();
		linStack.add(this);
		while(!linStack.isEmpty()){
			Cell curCell=linStack.pop();
			Set<Cell> moms=curCell.getMothers();
			if(moms==null){
				ances.add(curCell);				
			}
			else{
				linStack.addAll(moms);
			}			
		}
		return ances;
	}
	/*
	 * Returns the Set of Cells which are part of this cell lineage. 
	 */
	public Set<Cell> getEntireLineage(){
		Set<Cell> lin= new HashSet<Cell>();
		//go to the ancestors (those with no mother) and return all their descendants
		Set<Cell>ances = getAncestors();
		//now we have all the ancestor without mothers
		for(Cell c: ances){
			lin.add(c);
			lin.addAll(getAllDescendants());
		}		
		return lin;
	}
	
	/**
	 * @return the number symbolizing the distance of this cell in the lineage created by ances, 
	 * Meaning - the length of the shortest path from ances to this cell.  
	 * Returns -1 if this cell is not a descendant of ances;
	 * Returns 0 if this cell is the ances.
	 * 
	 */
	public int getDist(Cell ances){
		int dist=0;
		Set<Cell> distCells= new HashSet<Cell>();
		distCells.add(ances);	
		while(!distCells.isEmpty()){
			if(distCells.contains(this)){
				return dist;
			}
			Set<Cell> nextCells=new HashSet<Cell>();
			for(Cell c: distCells){
				Set<Cell >daughts = c.getDaughters();
				if(daughts!=null){
					nextCells.addAll(daughts);
				}				
			}
			distCells=nextCells;
			dist++;			
		}
		//no more descendants
		return -1;
	}
	
	public Set<Cell> getCommonAncestors(Cell other){		
		Set<Cell> ances=this.getAncestors();
		Set<Cell> otherAnces=other.getAncestors();
		ances.retainAll(otherAnces);		
		return ances;		
	}
	
	/**
	 * 
	 * @return a set of the sisters of this cell, null if no sisters exist.
	 */
	public Set<Cell> getSisters(){
		Set<Cell> sisters=new HashSet<Cell>();
		Set<Cell> moms=this.getMothers();
		if(moms==null){
			return null;
		}
		for(Cell mom: moms){
			Set<Cell> daughts=mom.getDaughters();
			for(Cell d: daughts){
				if(d.getId()!=this.getId()){
					sisters.add(d);
				}
			}
		}
		if(sisters.isEmpty()){
			return null;
		}
		return sisters;
			
	}

}
