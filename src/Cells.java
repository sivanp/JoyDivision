import ij.gui.Roi;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class Cells extends Hashtable<Integer, Cell> implements Serializable 
{
	private static final long serialVersionUID = 1L;

	protected int lastId;

	protected CellParents cp; 
	protected CellsLocations cl;


	public Cells(int lastId) 
	{
		super();
		this.lastId = lastId;
		cp=new CellParents();
		cl=new CellsLocations();
	}
	public Cells() 
	{
		this(0);
	}
	
	public Cells(Cells cells){
		super(cells);
		this.lastId = cells.lastId;
		cp=cells.cp;
		cl=cells.cl;
	}

	public CellParents getCp() {
		return cp;
	}
	public CellsLocations getCl() {
		return cl;
	}

	@Override
	public synchronized Cell put(Integer arg0, Cell arg1) {
		// TODO What to do?
		return super.put(arg0, arg1);
	}


	@Override
	public synchronized Cell remove(Object cell) {
		Cell c=(Cell)cell;
		cp.removeCell(c);
		cl.removeCell(c);
		return super.remove(c.getId());
	}

	/**
	 * Add a new cell to this structure, with the highest id
	 * @return The new Cell instance
	 */
	public Cell addNewCell()
	{
		Cell c = new Cell(++lastId,this);
		this.put(c.getId(),c);		
		return c;
	}
	
	public Set<Cell> getCellsInFrame(int frame){
		Map<Cell,PolyProperty> cells=cl.getCellsByFrame(frame);
		if(cells==null){
			return null;
		}
		return cells.keySet();
	}
	
	/**
	 * 
	 * @param frame
	 * @param x
	 * @param y
	 * @return A set of all the cells in the given frame that contain the pixel (x,y) 
	 */
	public Set<Cell> getCellsContaining(int frame, int x, int y){
		Set<Cell> resCells= new HashSet<Cell>();
		Map<Cell, PolyProperty> cellLocs=cl.getCellsByFrame(frame);
		Iterator<Cell> citer=cellLocs.keySet().iterator();
		while(citer.hasNext()){
			Cell c=citer.next();
			Roi roi=cellLocs.get(c).getRoi();
			if(roi.contains(x, y)){
				resCells.add(c);
			}
		}
		return resCells;
	}
	
	public Cell getCell(int id) {		
		return this.get(id);
	}

	
	public String toString(){
		String res="";
		Collection<Cell> cells=this.values();
		Iterator<Cell> citer=cells.iterator();
		while(citer.hasNext()){
			Cell c=citer.next();
			res+=c.toString()+"\n--------------\n";
		}		
		return res;
	}
	public void dissociateMotherDaughter(Cell mom, Cell daughter) {
		cp.removeMomDaughter(mom, daughter);		
	}
	
	/**
	 * Removes the location of the given cell from the given frame
	 * @param cell
	 * @param frame
	 */
	public boolean removeCellLocation(Cell cell, int frame) {
		boolean res=cl.removeCellLocation(cell, frame);
		Set<Integer> cellFrames=cl.getFrames(cell);
		//if all such instances are remove remove the cell from the structure
		if(cellFrames==null || cellFrames.isEmpty()){
			this.remove(cell);
		}
		return res;
	}
	public void swapCellLocations(Cell cell1, Cell cell2, Integer frame) {
		cl.swapCellLocations(cell1, cell2,frame);
		//check if cells have been removed from all their locations
		Set<Integer> frames=cl.getFrames(cell1);
		boolean cellDeleted=false;
		if(frames==null){
			cellDeleted=true;
			swapDaughters(cell2, cell1, 1);
			moveMothers(cell1,cell2);
			super.remove(cell1.getId());
			
		}
		frames=cl.getFrames(cell2);
		if(frames==null){
			if(!cellDeleted){
				swapDaughters(cell1, cell2, 1);
			}

			cellDeleted=true;

			moveMothers(cell2,cell1);
			super.remove(cell2.getId());
		}
		if(!cellDeleted){
			swapDaughters(cell1,cell2,0);
		}
		//now check if need to update daughters and mothers of these cells
		
	}
	
	//move mothers of deletedCell to swapedCell.
	private void moveMothers(Cell deletedcell, Cell swapedCell){
		Set<Cell> moms= cp.getByChild(deletedcell);
		if(moms==null){
			return;
		}
		Iterator<Cell> iter=moms.iterator();
		while(iter.hasNext()){
			Cell mom=iter.next();
			swapedCell.addMother(mom);			
		}
		cp.removeCell(deletedcell);		
	}
	
	//swapping daughters.  if type==0 swap, if type ==1 move cell2 daughters to cell1.
	private void swapDaughters(Cell cell1, Cell cell2, int type){
		Set<Cell> daughts1= cp.getByParent(cell1);
		Set<Cell> daughts2= cp.getByParent(cell2);
		Set<Cell> newDaughts1 = new HashSet<Cell>();
		Set<Cell> newDaughts2 = new HashSet<Cell>();
		if(daughts1!=null && type==0){ //copy daughts1 to newDaughts2 and remove all
			Iterator<Cell> iter1=daughts1.iterator();
			while(iter1.hasNext()){
				Cell cell=iter1.next();
				newDaughts2.add(cell);
			}
			iter1=newDaughts2.iterator();
			while(iter1.hasNext()){
				Cell cell=iter1.next();
				cp.removeMomDaughter(cell1, cell);
			}
		}
		if(daughts2!=null){
			Iterator<Cell> iter2=daughts2.iterator();
			while(iter2.hasNext()){
				Cell cell=iter2.next();
				newDaughts1.add(cell);
			}
			iter2=newDaughts1.iterator();
			while(iter2.hasNext()){
				Cell cell=iter2.next();
				cp.removeMomDaughter(cell2,cell);
			}		
		}
		Iterator<Cell> iter1= newDaughts1.iterator();
		while(iter1.hasNext()){
			Cell cell=iter1.next();
			cell1.addDaughter(cell);
		}
		Iterator<Cell> iter2= newDaughts2.iterator();
		while(iter2.hasNext()){
			Cell cell=iter2.next();
			cell2.addDaughter(cell);
		}
		
	}
	
	
	/**
	 * Returns the first cell found that has an roi in the given frame which overlap the given roi, 
	 * such that the overlapping area is at least the ovelapingRatio of the given roi. Null if no such cell is found. 
	 * @param roi
	 * @param frame
	 * @param overlapingRatio
	 * @return
	 */
	public Cell overlapingLocation(Roi roi, int frame, double overlapingRatio){		
		Set<Cell> cellsInFrame=this.getCellsInFrame(frame);
		if(cellsInFrame==null){
			return null;
		}
		Rectangle rect=roi.getBounds();
		double roiArea=rect.getWidth()*rect.getHeight();
		boolean overlaping=false;
		
		for(Cell c: cellsInFrame){
			Roi croi=c.getLocationInFrame(frame).getRoi();			
			double overlap=overlap(roi,croi);
			if(overlap/roiArea>overlapingRatio){
				return c;				
			}			
		}
		return null;
	}
	
	private double overlap(Roi roi1, Roi roi2){
		 double res=0;
		 Rectangle r1=roi1.getBounds();
		 Rectangle r2=roi2.getBounds();
		 Rectangle2D intersect=r1.createIntersection(r2);
		 if(intersect.getWidth()>0&& intersect.getHeight()>0){
			 res=intersect.getWidth()*intersect.getHeight();
		 }
		 return res;
	 }
		
	
	



}

