import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class PropertiesCells extends Cells {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Map<Integer, String> propId2NameMapping;
	 public PropertiesCells(){
		 super();
		 propId2NameMapping=new HashMap<Integer, String>();
	 }
	 
	 /**
	  * This constructor excepts a Cells instance for which the lastId, cellParents and cellLocations
	  * will be the same as the new PropertiesCells instance 
	  * @param cells
	  */
	 public PropertiesCells(Cells cells){
//		 super(cells); //need to do deep copying so that the cells will hold a reference to the new proerties cells
//		 this.cp=cells.cp; 
//		 this.cl=cells.cl;
//		 this.lastId=cells.lastId;
//		 propId2NameMapping=new HashMap<Integer, String>();
		 super();
		 Set<Integer> oldIds=cells.keySet();		 
		 Map<Integer, Integer> old2NewIdsMap=new HashMap<Integer,Integer>();
		 if(oldIds!=null){
			 Iterator<Integer> iter=oldIds.iterator();
			 while(iter.hasNext()){
				 int cellId=iter.next();
				 Cell curCell=cells.get(cellId);
				 Cell newCell=this.addNewCell();
				 old2NewIdsMap.put(curCell.getId(), newCell.getId());
				 Set<Integer> frames=curCell.getFrames();
				 Iterator<Integer> fiter=frames.iterator();
				 while(fiter.hasNext()){
					 int frame=fiter.next();
					 PolyProperty p=curCell.getLocationInFrame(frame);
					 newCell.addLocation(frame, p);
				 }
			 }
			 //got all cells and locations updated. now update mother-daughter mapping
			 iter=oldIds.iterator();
			 while(iter.hasNext()){
				 int cellId=iter.next();
				 Cell curCell=cells.get(cellId);
				 int newCellId=old2NewIdsMap.get(cellId);
				 Cell newCell=this.getCell(newCellId);
				 Set<Cell> daughts=curCell.getDaughters();
				 if(daughts==null){
					 continue;
				 }
				 Iterator<Cell> citer=daughts.iterator();
				 while(citer.hasNext()){
					 Cell daught=citer.next();	
					 int newDaughtId=old2NewIdsMap.get(daught.getId());
					 Cell newDaught=this.get(newDaughtId);
					 newCell.addDaughter(newDaught);
				 }
			 }
		 }
		 propId2NameMapping=new HashMap<Integer, String>();
		 
	 }
	 
	 public String addProperty(int id, String name){
		 return propId2NameMapping.put(id,name);
	 }
	 
	 public String addProperty(String name){
		 //find maximal property and add one larger
		 int maxFluoId=0;
		 if(propId2NameMapping.isEmpty()){
			 maxFluoId=1;
		 }
		 else{
			 Iterator<Integer> fiter=propId2NameMapping.keySet().iterator();
			 while(fiter.hasNext()){
				 Integer curId=fiter.next();
				 if(maxFluoId<curId){
					 maxFluoId=curId;
				 }
			 }
		 }
		 maxFluoId++;
		 return addProperty(maxFluoId,name);
	 }
	 
	 public String getPropertyName(int id){
		 return propId2NameMapping.get(id);
	 }
	 
	 public Set<Integer> getPropertyIdSet(){
		 return propId2NameMapping.keySet();
	 }
	 
	 public Collection<String> getPropertyNames(){
		 return propId2NameMapping.values();
	 }

	 /**
	  * 
	  * @param propname
	  * @return the property id, -1 if no such property exist
	  */
	public int getPropertyId(String propname) {
		Set<Integer> ids=getPropertyIdSet();
		Iterator<Integer> iter=ids.iterator();
		while(iter.hasNext()){
			int curId=iter.next();
			if(propname.equals(propId2NameMapping.get(curId))){
				return curId;
			}
		}
		return -1;
	}
}
