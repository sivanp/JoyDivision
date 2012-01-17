
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import ij.gui.Roi;
import java.util.HashSet;

public class CellsLocations  extends HashMap<Cell, Map<Integer,Roi>>
{
	
	private static final long serialVersionUID = 3641410585003171843L;
	Map<Integer,Set<Cell>> byFrame;
	
	public CellsLocations(){
		super();
		byFrame=new HashMap<Integer,Set<Cell>>();
	}
	@Override
	public void clear() {
		super.clear();
		byFrame.clear();
		
	}



	public Map<Integer,Roi> put(Cell cell, Integer frame, Roi roi){
		Set<Cell> cells=byFrame.get(frame);
		if(cells ==null){
			cells=new HashSet<Cell>();
		}
		cells.add(cell);
		byFrame.put(frame, cells);
		
		Map<Integer, Roi> locs=this.get(cell);
		if(locs==null){
			locs=new HashMap<Integer,Roi>();
		}
		locs.put(frame, roi);
		return this.put(cell, locs);
		
	}
	
	@Override
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
		
		return super.put(cell, value);
	}

	@Override
	public void putAll(Map<? extends Cell, ? extends Map<Integer, Roi>> m) {
		Set<? extends Cell> cells =m.keySet();
		Iterator<? extends Cell> citer = cells.iterator();
		while(citer.hasNext()){
			Cell cell=citer.next();
			Map<Integer, Roi> locs=this.get(cell);
			this.put(cell, locs);
		}
		
	}

	@Override
	public Map<Integer, Roi> remove(Object cell) {
		Map<Integer,Roi> locs=this.get(cell);
		Set<Integer> frames=locs.keySet();
		Iterator<Integer> fiter=frames.iterator();
		while(fiter.hasNext()){
			Integer frame=fiter.next();
			Set<Cell> cells=byFrame.get(frame);
			cells.remove(cell);
			byFrame.put(frame, cells);
		}
		return super.remove(cell);
	}
	
	public Map<Cell,Roi> getCellsByFrame(Integer frame){
		Map<Cell, Roi> locs=new HashMap<Cell,Roi>();
		Set<Cell> cells=byFrame.get(frame);
		Iterator<Cell> citer = cells.iterator();
		while(citer.hasNext()){
			Cell c=citer.next();
			Roi roi=this.get(c).get(frame);
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
		Map<Integer,Roi> locs=this.get(cell);
		if(locs==null){
			return null;
		}
		return locs.keySet();
		
	}
	
}
