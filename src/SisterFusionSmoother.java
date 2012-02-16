import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class SisterFusionSmoother {
	PropertiesCells cells;
	int areaPropId;
	int maxFusionFrames=3;
	double minAreaIncrease=1.4;
	public SisterFusionSmoother(PropertiesCells cells){
		this.cells=cells;
		areaPropId=cells.getPropertyId("Area");
		if(areaPropId==-1){
			throw new IllegalArgumentException("no area property in the given PropertiesCells instance");
		}		
		
	}
	
	public void setMaxFusionSlices(int maxFrames){
		maxFusionFrames=maxFrames;
	}
	public void setAreaIncrease(int areaIncrease){
		this.minAreaIncrease=areaIncrease;
	}
	
	public PropertiesCells smooth(){
		//go over all cells to find cells which have a sister that end 
		//when their size increase by areIncrease and they divide at most after maxFusionFrames
		Stack<Cell> cellsStack= new Stack<Cell>();
		cellsStack.addAll(cells.values());
		while(!cellsStack.isEmpty()){
			Cell cell=cellsStack.pop();
			Set<Cell> daughts= cell.getDaughters();
			if(daughts==null || daughts.size()!=2){
				continue;
			}
			Iterator<Cell> diter=daughts.iterator();
			Cell daught1=diter.next();
			Cell daught2=diter.next();
			int d1firstFrame=daught1.getFrames().first();
			int d2firstFrame=daught2.getFrames().first();
			if(d1firstFrame!=d2firstFrame){
				continue; //don't know what to do here
			}
			
			Set<Cell> sisters=cell.getSisters();
			if(sisters==null || sisters.size()>1){
				continue;
			}
			Cell sis=sisters.iterator().next();
			int sisLastFrame=sis.getFrames().last();
			int cellLastFrame=cell.getFrames().last();
			if(sisLastFrame>=cellLastFrame || (cellLastFrame-sisLastFrame)>maxFusionFrames){
				continue;
			}
			if(sis.getDaughters()!=null){
				continue;
			}
			PolyProperty cellBeforeSisLoss=cell.getLocationInFrame(sisLastFrame);
			double cellAreaBeforeSisLoss= cellBeforeSisLoss.getPropertyValue(areaPropId);
			int nextFrame=cell.getFrames().tailSet(sisLastFrame+1).first();
			PolyProperty cellAfterSisLoss=cell.getLocationInFrame(nextFrame);
			double cellAreaAfterSisLoss= cellAfterSisLoss.getPropertyValue(areaPropId);
			if(cellAreaAfterSisLoss/cellAreaBeforeSisLoss<minAreaIncrease){
				continue;
			}
			//if we've reached here the cell is treated as if it went fusion
			//find the daughter closer to the lost sister
			PolyProperty psis=sis.getLocationInFrame(sisLastFrame);
			PolyProperty pd1=daught1.getLocationInFrame(d1firstFrame);
			PolyProperty pd2=daught2.getLocationInFrame(d2firstFrame);
			double dist1=psis.getDist(pd1);
			double dist2=psis.getDist(pd2);
			if(dist1<dist2){						
				cells.swapCellLocations(sis, daught1, d1firstFrame);
				cells.swapCellLocations(cell, daught2, d2firstFrame);						
				
			}
			else{
				cells.swapCellLocations(sis, daught1, d1firstFrame);
				cells.swapCellLocations(cell, daught2, d2firstFrame);
			}
			cellsStack.removeElement(daught1);
			cellsStack.removeElement(daught2);
			cellsStack.push(sis);
			cellsStack.push(cell);
		}
		
		return cells;
	}
	
	
	
	
}
