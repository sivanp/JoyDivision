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
		 super(cells);
		 this.cp=cells.cp; 
		 this.cl=cells.cl;
		 this.lastId=cells.lastId;
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

	public int getPropertyId(String fluoname) {
		Set<Integer> ids=getPropertyIdSet();
		Iterator<Integer> iter=ids.iterator();
		while(iter.hasNext()){
			int curId=iter.next();
			if(fluoname.equals(propId2NameMapping.get(curId))){
				return curId;
			}
		}
		return -1;
	}
}
