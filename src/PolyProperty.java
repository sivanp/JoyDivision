import java.awt.Polygon;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ij.gui.PolygonRoi;


public class PolyProperty implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Map<Integer, Double> propId2Value;
	protected PolygonRoi roi;
	

	public PolyProperty(Polygon p, int type) {
		roi=new PolygonRoi(p, type);		
		propId2Value=new HashMap<Integer,Double>();
	}
	
	public PolyProperty(PolygonRoi roi){
		this(roi.getPolygon(), roi.getType());		
	}
	
	public PolygonRoi getRoi() {
		return roi;
	}

	public void setRoi(PolygonRoi roi) {
		this.roi = roi;
	}
	
	public Double setProperty(int propId, Double value){
		return propId2Value.put(propId, value);
	}
	
	public Double getPropertyValue(int propId){
		return propId2Value.get(propId);
	}
	
	public String toString(){
		String res="";
		res+=roi.toString();
		Iterator<Integer> iter=propId2Value.keySet().iterator();
		while(iter.hasNext()){
			int curId=iter.next();
			res+=" prop"+curId+" :"+propId2Value.get(curId);
		}				
		return res;
	}

}
