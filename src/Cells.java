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

