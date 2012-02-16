import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;


public class PaprikaTrack{
	static int	minSize = 30;
	static int	maxSize = 999999;
	static int	minCirc = 0;
	static int	maxCirc = 1;
	double minDivRatio=0.65;  
	double maxMDDist=50;
	double maxDist=50;
	double maxOverlap=10;
	double minFrames=5;
	ImagePlus imp;
	PropertiesCells cells;
	Set<Cell> workingSet;
	int options = ParticleAnalyzer.SHOW_RESULTS;//0; // set all PA options false
	 
	int measurements = Measurements.CENTROID | Measurements.AREA;
	int propId;
	
	public PaprikaTrack(ImagePlus imp, PropertiesCells cells){
		this.imp = imp;	
		this.workingSet= new HashSet<Cell>();		
		propId=cells.getPropertyId("Area");
		if(propId==-1){
			IJ.showMessage("no area property in the cells structure- aborting");
			return;
		}
		this.cells=cells;
	}	
	
	public Cells track(Set<Cell> workingSet){
		int slice=imp.getSlice();
		PathTokens pt=new PathTokens(imp.getStack(), slice);
		int frame=pt.getFrame();
		if(workingSet.isEmpty()){			
			//add all cells in the current frame to the workingSet
			workingSet=cells.getCellsInFrame(frame);
			if(workingSet==null ||workingSet.isEmpty()){
				return cells;
			}
		}
		this.workingSet.addAll(workingSet);		
		 IJ.setAutoThreshold(imp, "Default");	
		 //if working set cells have daughters remove them- erase them.
		 SortedSet<Cell> workingCells=new TreeSet<Cell>();
		 SortedSet<Cell> solvedCells=new TreeSet<Cell>();
			//add to the sortedCells only cells that are in the workingSet
		 initializeWorkingAndSolvedSet(workingCells, solvedCells, frame);
		 //get all descendants of solved set- these are cells that should not be tempered
		 Set<Cell> solvedDescendants= new HashSet<Cell>();
		 for(Cell solvedCell: solvedCells){
			 Set<Cell> descs=solvedCell.getAllDescendants();
			 if(descs!=null){
				 solvedDescendants.addAll(descs);
			 }
		 }
		 //go over all the descendants of a all the workingCells add remove them from their mother unless their mother is in the solvedDesendant
		 Stack<Cell> orphans2be = new Stack<Cell>();
		 orphans2be.addAll(workingCells);
		 Set<Cell> toRemove= new HashSet<Cell>();
		 while(!orphans2be.isEmpty()){
			 Cell cell=orphans2be.pop();
			 if(!solvedDescendants.contains(cell)){ //this cell should add its daughters to the set and then remove them from itself
				 if(cell.getDaughters()!=null){
					 Set<Cell> daughts= new HashSet<Cell>(cell.getDaughters());
					 orphans2be.addAll(daughts);			 
					 for(Cell d: daughts){
						 cell.removeDaughter(d);
						 if(!solvedDescendants.contains(cell)){						
							 toRemove.add(d);
						 }
					 }
				 }
			 }
		 }
		 for(Cell c: toRemove){
			 cells.remove(c);
		 }
		 
		 
		 
		 
		while(imp.getStack().getSize()>slice){			
			associateRois(slice++);
		}
		SisterFusionSmoother smoother = new SisterFusionSmoother(cells);
		cells=smoother.smooth();
		return cells;
	}
	
	
	
	private void initializeWorkingAndSolvedSet(Set<Cell> workingCells, Set<Cell> solvedCells,int frame){		
		Set<Cell> frameCells=cells.getCellsInFrame(frame);
		if(frameCells==null ||frameCells.isEmpty()){
			return;
		}
		//add to the sortedCells only cells that are in the workingSet
		for(Cell curCell: frameCells){
			if(workingSet.contains(curCell)){
				workingCells.add(curCell);
			}
			else{
				solvedCells.add(curCell);
			}
		}
	}
	
	/*
	 * This method associate the cells in the first frame to rois in the following one in the imp.
	 */
	private void associateRois(int slice){		
		PathTokens pt=new PathTokens(imp.getStack(), slice);
		int frame=pt.getFrame();
		SortedSet<Cell> sortedCells=new TreeSet<Cell>();
		SortedSet<Cell> solvedCells=new TreeSet<Cell>();
		//add to the sortedCells only cells that are in the workingSet
		initializeWorkingAndSolvedSet(sortedCells, solvedCells, frame);
		if(sortedCells.isEmpty()){
			return;
		}		
		
		ArrayList<PolyProperty> polys= new ArrayList<PolyProperty>();
		int[] orderedCells=new int[sortedCells.size()];
		int i=0;
		for(Cell curCell : sortedCells){
			orderedCells[i++]=curCell.getId();			
			polys.add(curCell.getLocationInFrame(frame));
		}		
		ArrayList<PolyProperty> polysTargets=getPolys(slice+1);
		pt=new PathTokens(imp.getStack(), slice+1);		
		int secFrame=pt.getFrame();
		polysTargets=getAllowedPolys(polysTargets, solvedCells, secFrame);
		double[][] costMatrix= getCostMatrix(polys,polysTargets);
		IJ.showStatus("matching slice "+slice);
		int[][] mapping=HungarianAlgorithm.hgAlgorithm(costMatrix, "min");		
		
		//now add the polyProps 
		for (i=0; i<mapping.length; i++){
			int cellInd=mapping[i][0];
			int targetPolyInd=mapping[i][1];
			int curId=orderedCells[cellInd];
			PolyProperty p=polys.get(cellInd);
			PolyProperty t=polysTargets.get(targetPolyInd);
			if(p.getDist(t)<maxDist){
				cells.getCell(curId).addLocation(secFrame, polysTargets.get(targetPolyInd));
			}
		}
		//need to check unmatched cells for divisions
		//Are any cells are matched to a significantly smaller cell? 
		for (i=0; i<mapping.length; i++){
			int cellInd=mapping[i][0];
			int targetPolyInd=mapping[i][1];
			PolyProperty p=polys.get(cellInd);
			double parea =p.getPropertyValue(propId);
			double px=p.getRoi().getBounds().getCenterX();
			double py=p.getRoi().getBounds().getCenterY();
			//System.out.println("polysTargets size: "+polysTargets.size()+" index: "+targetPolyInd);
			PolyProperty t=polysTargets.get(targetPolyInd);
			
			double tarea =t.getPropertyValue(propId);
			double tx=t.getRoi().getBounds().getCenterX();
			double ty=t.getRoi().getBounds().getCenterY();
			if(tarea/parea<minDivRatio){
				//This cell might be dividing- try to match this to another cell
				ArrayList<PolyProperty> newPolysTargets=new ArrayList<PolyProperty>(polysTargets);
				newPolysTargets.remove(targetPolyInd);
				
				double[][] newCostMatrix= getCostMatrix(polys,newPolysTargets);
				int[][] newMapping=HungarianAlgorithm.hgAlgorithm(newCostMatrix, "min");
				//now look if a new match is found that is close and small enough. 
				//If found another match look if it coincide with a different cell matched in mapping
				int newTargetPolyInd=newMapping[cellInd][1];
				PolyProperty nt=newPolysTargets.get(newTargetPolyInd);
				double ntarea =nt.getPropertyValue(propId);
				double ntx=nt.getRoi().getBounds().getCenterX();
				double nty=nt.getRoi().getBounds().getCenterY();
				if(ntarea/parea>=minDivRatio){
					continue; //this is not small enough we'll continue
				}
				if(t.getDist(nt)>maxMDDist){
					continue; //this is not close enough we'll continue
				}
				//look if the target already exists in mapping
				int match=-1;
				int newTargetPolyIndInOld = newTargetPolyInd;
				if(newTargetPolyInd>=targetPolyInd){
					newTargetPolyIndInOld++;//this is since the new cost matrix indexes are based on the shorter newPolysTargets in which the indexes of the rois following the target are jumped forward				
				}
				for (int j=0; j<mapping.length; j++){					
					if(mapping[j][1]==newTargetPolyIndInOld){
						match=j;
						break;
					}
				}
				if(match==-1){
				//create new daughters for the cells matching p in this frame
					int curId=orderedCells[cellInd];	
					Cell curCell=cells.getCell(curId);
					cells.removeCellLocation(curCell, secFrame);
					Cell daught1= cells.addNewCell();
					Cell daught2= cells.addNewCell();
					daught1.addLocation(secFrame, t);
					daught2.addLocation(secFrame, nt);
					curCell.addDaughter(daught1);
					curCell.addDaughter(daught2);
					//remove the current cell from the working set and add its daughters
					workingSet.remove(curCell);
					workingSet.add(daught1);
					workingSet.add(daught2);
				}
			}
		}
	}
	
	
	//returns an ArrayList containing all the PolyProperties in targetPolys which are not overlaping any cell in solvedCell in the given frame 
	private ArrayList<PolyProperty> getAllowedPolys(ArrayList<PolyProperty> targetPolys, Set<Cell> solvedCells, int frame){
		ArrayList<PolyProperty> allowedPolys= new ArrayList<PolyProperty>();
		allowedPolys.addAll(targetPolys);
		for(Cell curCell: solvedCells){
			ArrayList<PolyProperty> intersecting = new ArrayList<PolyProperty>();
			PolyProperty curPoly=curCell.getLocationInFrame(frame);
			if(curPoly==null){
				continue;
			}
			for(PolyProperty p: allowedPolys){				
				double overlap= Cells.overlap(p.getRoi(), curPoly.getRoi());
				if(overlap>maxOverlap){
					intersecting.add(p);
				}
			}
			//remove all the intersecting 
			for(PolyProperty p: intersecting){
				allowedPolys.remove(p);
			}
		}
		return allowedPolys;
	}
	
	
	
	private double[][] getCostMatrix(ArrayList<PolyProperty> polys1,ArrayList<PolyProperty> polys2 ){
		double[][] costMat=new double[polys1.size()][polys2.size()];
		for(int i=0;i<polys1.size();i++){
			for(int j=0; j<polys2.size();j++){
				costMat[i][j]=getScore(polys1.get(i),polys2.get(j));
			}			
		}
		return costMat;
	}
	
	private ArrayList<PolyProperty> getPolys(int slice){
		imp.setSlice(slice);
		ArrayList<PolyProperty> polys= new ArrayList<PolyProperty>();
		ResultsTable rt = new ResultsTable();
		rt.reset();
		ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize, minCirc, maxCirc);
		if(!pa.analyze(imp, imp.getStack().getProcessor(slice))){
			// the particle analyzer should produce a dialog on error, so don't make another one
			return null;
		}		
		float[] sxRes = rt.getColumn(ResultsTable.X_CENTROID);				
		float[] syRes = rt.getColumn(ResultsTable.Y_CENTROID);
		float[] saRes= rt.getColumn(ResultsTable.AREA);
		if (sxRes==null){
			return null;
		}
		//createPolys
		for(int i=0;i<sxRes.length;i++){
			IJ.doWand((int)sxRes[i], (int)syRes[i],0,"8-connected");
			Roi roi = imp.getRoi();
			if(roi==null){ //need to move x y of wand till find something new
				for(int step=1;step<maxDist; step++){					
					IJ.doWand((int)sxRes[i]+step, (int)syRes[i],0,"8-connected");
					roi = imp.getRoi();
					if(roi!=null){
						break;
					}
					IJ.doWand((int)sxRes[i], (int)syRes[i]+step,0,"8-connected");
					roi = imp.getRoi();
					if(roi!=null){
						break;
					}
					IJ.doWand((int)sxRes[i]+step, (int)syRes[i]+step,0,"8-connected");
					roi = imp.getRoi();
					if(roi!=null){
						break;
					}
					IJ.doWand((int)sxRes[i]-step, (int)syRes[i],0,"8-connected");
					roi = imp.getRoi();
					if(roi!=null){
						break;
					}
					IJ.doWand((int)sxRes[i], (int)syRes[i]-step,0,"8-connected");
					roi = imp.getRoi();
					if(roi!=null){
						break;
					}
					IJ.doWand((int)sxRes[i]-step, (int)syRes[i]-step,0,"8-connected");
					roi = imp.getRoi();
					if(roi!=null){
						break;
					}
				}
				if(roi==null){
					continue; //we have one less poly
				}
			}
			PolyProperty p=new PolyProperty(roi);
			p.setProperty(propId,new Double(saRes[i]));
			polys.add(p);
			roi=null;
			imp.killRoi();
		}
		return polys;
	}
	
	
	private double getScore(PolyProperty p1, PolyProperty p2){
		double dist=p1.getDist(p2);
		double cost=dist;
		return cost;
	}
}
