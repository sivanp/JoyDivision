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
	
	
	
	public String roiToWriter(){
		String res="[";
		int[] xpixels=roi.getPolygon().xpoints;
		int[] ypixels=roi.getPolygon().ypoints;
		for (int i=0; i<xpixels.length;i++){
			res+=xpixels[i]+","+ypixels[i]+";";
		}
		res+="]";
		return res;
	}
	
	public String propertyToWriter(int propId){
		
		Double val=propId2Value.get(propId);
		if(val==null){
			return null;
		}		
		return propId+"="+val.toString();
	}

}
