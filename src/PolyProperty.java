import java.awt.Polygon;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import ij.gui.PolygonRoi;
import ij.gui.Roi;


public class PolyProperty implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Map<Integer, Double> propId2Value;
	protected Polygon roi;
	

	public PolyProperty(Polygon p) {
		roi=new Polygon(p.xpoints, p.ypoints,p.npoints);		
		propId2Value=new HashMap<Integer,Double>();
	}
	
	public PolyProperty(Roi roi){
		this(roi.getPolygon());		
	}
	
	public PolygonRoi getRoi() {
		return new PolygonRoi(roi, Roi.FREEROI);
	}

	public void setRoi(Roi roi) {
		this.roi = roi.getPolygon();
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
		String res="";
		int[] xpixels=roi.xpoints;
		int[] ypixels=roi.ypoints;
		for (int i=0; i<xpixels.length;i++){
			res+=xpixels[i]+","+ypixels[i]+";";
		}		
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
