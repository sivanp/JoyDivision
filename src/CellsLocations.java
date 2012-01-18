
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import ij.gui.Roi;
import java.util.HashSet;

public class CellsLocations implements Serializable {
	
	private static final long serialVersionUID = 3641410585003171843L;
	Map<Integer,Set<Cell>> byFrame;
	Map<Cell, Map<Integer,Roi>> byCell;
	
	public CellsLocations(){
		byCell= new HashMap<Cell, Map<Integer,Roi>>();
		byFrame=new HashMap<Integer,Set<Cell>>();
	}
	
	public void clear() {
		byCell.clear();
		byFrame.clear();
		
	}

	public Map<Integer,Roi> addLocationToCell(Cell cell, Integer frame, Roi roi){
		Set<Cell> cells=byFrame.get(frame);
		if(cells ==null){
			cells=new HashSet<Cell>();
		}
		cells.add(cell);
		byFrame.put(frame, cells);
		
		Map<Integer, Roi> locs=byCell.get(cell);
		if(locs==null){
			locs=new HashMap<Integer,Roi>();
		}
		locs.put(frame, roi);
		return this.put(cell, locs);
		
	}
	
	
	public Map<Integer, Roi> put(Cell cell, Map<Integer, Roi> value) 
	{
		Set<Integer> frames=value.keySet();
		Iterator<Integer> fiter=frames.iterator();
		while(fiter.hasNext()){
			Integer frame=fiter.next();
			Set<Cell> cells=byFrame.get(frame);
			if(cells == null){
				cells=new HashSet<Cell>();
			}
			cells.add(cell);
			byFrame.put(frame, cells);
		}
		
		return byCell.put(cell, value);
	}
	
	
	public Map<Integer, Roi> removeCell(Cell cell) {
		Map<Integer,Roi> locs=byCell.get(cell);
		if(locs==null){
			return null;
		}
		Set<Integer> frames=locs.keySet();
		Iterator<Integer> fiter=frames.iterator();
		while(fiter.hasNext()){
			Integer frame=fiter.next();
			Set<Cell> cells=byFrame.get(frame);
			cells.remove(cell);
			byFrame.put(frame, cells);
		}
		return byCell.remove(cell);
	}
	
	public Map<Cell,Roi> getCellsByFrame(Integer frame){
		Map<Cell, Roi> locs=new HashMap<Cell,Roi>();
		Set<Cell> cells=byFrame.get(frame);
		if(cells==null){
			return null;
		}
		Iterator<Cell> citer = cells.iterator();
		while(citer.hasNext()){
			Cell c=citer.next();
			Roi roi=byCell.get(c).get(frame);
			locs.put(c,roi); 
		}
		return locs;
	}
	
	/**
	 * 
	 * @param cell
	 * @param frame
	 * @return Roi associated with the given cell in the given frame
	 */
	public Roi getCellLocationInFrame(Cell cell, int frame){
		Map<Cell, Roi> cellsInFrame=getCellsByFrame(frame);
		return cellsInFrame.get(cell);
	}
	public Set<Integer> getFrames(Cell cell) {
		Map<Integer,Roi> locs=byCell.get(cell);
		if(locs==null){
			return null;
		}
		return locs.keySet();
		
	}
	
}
