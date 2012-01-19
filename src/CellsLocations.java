
import java.awt.Polygon;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import java.util.HashSet;

public class CellsLocations implements Serializable {
	
	private static final long serialVersionUID = 3641410585003171843L;
	Map<Integer,Set<Cell>> byFrame;
	Map<Cell, Map<Integer,PolygonRoi>> byCell;
	
	public CellsLocations(){
		byCell= new HashMap<Cell, Map<Integer,PolygonRoi>>();
		byFrame=new HashMap<Integer,Set<Cell>>();
	}
	
	public void clear() {
		byCell.clear();
		byFrame.clear();
		
	}

	public Map<Integer,PolygonRoi> addLocationToCell(Cell cell, Integer frame, Roi roi){
		Polygon poly=roi.getPolygon();
		PolygonRoi proi=new PolygonRoi(poly,Roi.FREEROI);
		Set<Cell> cells=byFrame.get(frame);
		if(cells ==null){
			cells=new HashSet<Cell>();
		}
		cells.add(cell);
		byFrame.put(frame, cells);
		
		Map<Integer, PolygonRoi> locs=byCell.get(cell);
		if(locs==null){
			locs=new HashMap<Integer,PolygonRoi>();
		}
		locs.put(frame, proi);
		return this.put(cell, locs);
		
	}
	
	
	public Map<Integer, PolygonRoi> put(Cell cell, Map<Integer, PolygonRoi> value) 
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
	
	
	public Map<Integer, PolygonRoi> removeCell(Cell cell) {
		Map<Integer,PolygonRoi> locs=byCell.get(cell);
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
	
	public Map<Cell,PolygonRoi> getCellsByFrame(Integer frame){
		Map<Cell, PolygonRoi> locs=new HashMap<Cell,PolygonRoi>();
		Set<Cell> cells=byFrame.get(frame);
		if(cells==null){
			return null;
		}
		Iterator<Cell> citer = cells.iterator();
		while(citer.hasNext()){
			Cell c=citer.next();
			PolygonRoi roi=byCell.get(c).get(frame);
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
	public PolygonRoi getCellLocationInFrame(Cell cell, int frame){
		Map<Cell, PolygonRoi> cellsInFrame=getCellsByFrame(frame);
		return cellsInFrame.get(cell);
	}
	public Set<Integer> getFrames(Cell cell) {
		Map<Integer,PolygonRoi> locs=byCell.get(cell);
		if(locs==null){
			return null;
		}
		return locs.keySet();
		
	}
	
}
