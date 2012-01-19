import ij.gui.PolygonRoi;
import ij.gui.Roi;

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

	private int lastId;

	private CellParents cp; 
	private CellsLocations cl;


	public Cells(int lastId) 
	{
		super();
		this.lastId = lastId;
		cp=new CellParents();
		cl=new CellsLocations();
	}
	public Cells() 
	{
		super();
		lastId = 0;
		cp=new CellParents();
		cl=new CellsLocations();
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
		Map<Cell,PolygonRoi> cells=cl.getCellsByFrame(frame);
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
		Map<Cell, PolygonRoi> cellLocs=cl.getCellsByFrame(frame);
		Iterator<Cell> citer=cellLocs.keySet().iterator();
		while(citer.hasNext()){
			Cell c=citer.next();
			Roi roi=cellLocs.get(c);
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



}

