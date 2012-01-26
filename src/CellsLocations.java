
import java.awt.Polygon;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import java.util.HashSet;

public class CellsLocations implements Serializable {
	
	private static final long serialVersionUID = 3641410585003171843L;
	SortedMap<Integer,Set<Cell>> byFrame;
	Map<Cell, SortedMap<Integer,PolyProperty>> byCell;
	
	public CellsLocations(){
		byCell= new HashMap<Cell, SortedMap<Integer,PolyProperty>>();
		byFrame=new TreeMap<Integer,Set<Cell>>();
	}
	
	public void clear() {
		byCell.clear();
		byFrame.clear();
		
	}

	public Map<Integer,PolyProperty> addLocationToCell(Cell cell, Integer frame, PolyProperty roi){
		Polygon poly=roi.getRoi().getPolygon();		
		PolyProperty proi=new PolyProperty(poly);
		proi.propId2Value=roi.propId2Value;
		Set<Cell> cells=byFrame.get(frame);
		if(cells ==null){
			cells=new HashSet<Cell>();
		}
		cells.add(cell);
		byFrame.put(frame, cells);
		
		SortedMap<Integer, PolyProperty> locs=byCell.get(cell);
		if(locs==null){
			locs=new TreeMap<Integer,PolyProperty>();
		}
		locs.put(frame, proi);
		return this.put(cell, locs);
	}
	
	public Map<Integer,PolyProperty> addLocationToCell(Cell cell, Integer frame, Roi roi){
		Polygon poly=roi.getPolygon();
		PolyProperty proi=new PolyProperty(poly);		
		return this.addLocationToCell(cell, frame, proi);
		
	}
	
	
	public Map<Integer, PolyProperty> put(Cell cell, SortedMap<Integer, PolyProperty> value) 
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
	
	
	public Map<Integer, PolyProperty> removeCell(Cell cell) {
		Map<Integer,PolyProperty> locs=byCell.get(cell);
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
	
	public Map<Cell,PolyProperty> getCellsByFrame(Integer frame){
		Map<Cell, PolyProperty> locs=new HashMap<Cell,PolyProperty>();
		Set<Cell> cells=byFrame.get(frame);
		if(cells==null){
			return null;
		}
		Iterator<Cell> citer = cells.iterator();
		while(citer.hasNext()){
			Cell c=citer.next();
			PolyProperty roi=byCell.get(c).get(frame);
			locs.put(c,roi); 
		}
		return locs;
	}
	
	/**
	 * 
	 * @param cell
	 * @param frame
	 * @return PolygonRoi associated with the given cell in the given frame
	 */
	public PolyProperty getCellLocationInFrame(Cell cell, int frame){
		Map<Cell, PolyProperty> cellsInFrame=getCellsByFrame(frame);
		if(cellsInFrame==null){
			return null;
		}
		return cellsInFrame.get(cell);
	}
	public Set<Integer> getFrames(Cell cell) {
		SortedMap<Integer,PolyProperty> locs=byCell.get(cell);
		if(locs==null){
			return null;
		}
		return locs.keySet();
		
	}

	/**
	 * Remove the cell location from this structure
	 * @param cell
	 * @param frame
	 */
	public boolean removeCellLocation(Cell cell, int frame) {
		Map<Integer,PolyProperty> cellsLocs=this.byCell.get(cell);
		if(cellsLocs==null){
			return false;
		}
		cellsLocs.remove(frame);		
		Set<Cell> frameCells=byFrame.get(frame);
		if(frameCells==null){
			return false;
		}
		return frameCells.remove(cell);
		
	}

	/**
	 * Swaps the Roi set of cell1 with that of cell2 from frame 1 forward
	 * @param cell1
	 * @param cell2
	 * @param frame
	 */
	public void swapCellLocations(Cell cell1, Cell cell2, Integer frame) {
		Map<Integer, PolyProperty> locs1=byCell.get(cell1);
		Map<Integer,PolyProperty> locs2=byCell.get(cell2);
		Map<Integer, PolyProperty> newLocs1=new HashMap<Integer,PolyProperty>();
		Map<Integer, PolyProperty> newLocs2=new HashMap<Integer,PolyProperty>();
		
		Set<Integer> frames1=locs1.keySet();
		Set<Integer> frames2=locs2.keySet();
		Set<Integer> frames=new HashSet<Integer>();	
	
		frames.addAll(frames1);
		frames.addAll(frames2);
		
		Iterator<Integer> iter=frames.iterator();		
		while(iter.hasNext()){
			int curFrame=iter.next();
			if(curFrame<frame){
				if(locs1.get(curFrame)!=null){
					newLocs1.put(curFrame, locs1.get(curFrame));
				}
				if(locs2.get(curFrame)!=null){
					newLocs2.put(curFrame, locs2.get(curFrame));
				}
			}
			else{
				if(locs2.get(curFrame)!=null){
					newLocs1.put(curFrame, locs2.get(curFrame));
				}
				if(locs1.get(curFrame)!=null){
					newLocs2.put(curFrame, locs1.get(curFrame));
				}
			}		
		}
		//now we have newLocs have the updated locs.
		this.removeCell(cell1);
		this.removeCell(cell2);
		frames1=newLocs1.keySet();
		iter=frames1.iterator();		
		while(iter.hasNext()){
			Integer curFrame=iter.next();
			this.addLocationToCell(cell1, curFrame, newLocs1.get(curFrame));
		}
		
		frames2=newLocs2.keySet();
		iter=frames2.iterator();		
		while(iter.hasNext()){
			Integer curFrame=iter.next();
			this.addLocationToCell(cell2, curFrame, newLocs2.get(curFrame));
		}
		
		
		
	}
	
}
