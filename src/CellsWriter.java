import ij.IJ;
import ij.gui.Roi;
import ij.measure.ResultsTable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.beans.*;


public class CellsWriter {
	
	public static void writeStructure(String filename,Cells cells){	
		FileOutputStream fos = null;
		PrintStream out=null;
		try{
		fos = new FileOutputStream(filename);			
		out = new PrintStream(fos);
		out.println("---cell ids---");
//			String res="---cell ids---\n";
			//output all the cell by ids.
			Set<Integer> cellIds=cells.keySet();
			Iterator<Integer> iter=cellIds.iterator();
			while(iter.hasNext()){
				Integer cellId=iter.next();
				Cell curCell=cells.get(cellId);
				out.println("cell id = " +cellId.toString()+" ; "+curCell.getName()+" "+curCell.isDies()+" "+curCell.getRemark());		
			}
			
			out.println("\n---cell poly---");	
			//output all cell's locations by frame
			Collection<Cell> allCells=cells.values();
			Iterator<Cell> citer= allCells.iterator();
			while(citer.hasNext()){
				Cell curCell=citer.next();
				System.out.println("Cell id="+curCell.getId());
				out.println("Cell id="+curCell.getId());
				Set<Integer> frames=curCell.getFrames();	
				if(frames!=null){
					frames = new TreeSet<Integer>(frames);
					iter=frames.iterator();
					while(iter.hasNext()){
						Integer curFrame=iter.next();
						PolyProperty curPoly=curCell.getLocationInFrame(curFrame);
						out.println(curFrame+"="+curPoly.roiToWriter());
					}
				}
				out.println(); //discriminating between cells
			}
			out.println("---properties---");
			// now we output the properties of each cell
			if(cells instanceof PropertiesCells){
				Set<Integer> propIds=((PropertiesCells)cells).getPropertyIdSet();
				Iterator<Integer> piter= propIds.iterator();
				while(piter.hasNext()){
					int propId=piter.next();
					out.println("---propName="+ ((PropertiesCells)cells).getPropertyName(propId)+"---");
					citer= allCells.iterator();
					while(citer.hasNext()){
						Cell curCell=citer.next();
						System.out.println("Cell id="+curCell.getId());
						out.println("Cell id="+curCell.getId());
						Set<Integer> frames=curCell.getFrames();
						if(frames!=null){
							frames = new TreeSet<Integer>(frames);
							iter=frames.iterator();
							while(iter.hasNext()){
								Integer curFrame=iter.next();
								PolyProperty curPoly=curCell.getLocationInFrame(curFrame);
								Double propVal=curPoly.getPropertyValue(propId);
								if(propVal!=null){
									out.println(curFrame+"="+propVal+";");
								}
							}
						}
						out.println(); //discriminating between cells
					}
					out.println("---");
				}			
			}
			//now we output the mother-daughter relations
			out.println("---mother daughter---");
			System.out.println("writing mother daughters");
			citer= allCells.iterator();
			while(citer.hasNext()){
				Cell curCell=citer.next();
				Set<Cell> moms=curCell.getMothers();
				if(moms!=null){
					Iterator<Cell> miter=moms.iterator();
					while(miter.hasNext()){
						Cell mom=miter.next();
						out.println(mom.getId()+","+curCell.getId());
					}
				}
			}
			out.close();
		}
		catch(IOException ex)
		{
			out.close();
			IJ.showMessage("Export cellsStruct failed ","io: "+ex);
			ex.printStackTrace();
		}
	}
	
//	public static void writeStructureToResultsTable(Cells cells){
//		ResultsTable positionTable = new ResultsTable();
//		Set<Integer> cellIds=cells.keySet();
//		Iterator<Integer> iter=cellIds.iterator();		
//		while(iter.hasNext()){
//			Integer cellId=iter.next();
//			Cell curCell=cells.get(cellId);			
//			Set<Integer> frames=curCell.getFrames();
//			if(frames!=null){
//				frames = new TreeSet<Integer>(frames);
//				iter=frames.iterator();
//				while(iter.hasNext()){
//					positionTable.incrementCounter();
//					Integer curFrame=iter.next();
//					positionTable.addValue("id", cellId);
//					positionTable.addValue("frame", curFrame);
//					PolyProperty curPoly=curCell.getLocationInFrame(curFrame);
//					Roi roi=curPoly.getRoi();
//					positionTable.addValue("Center X",roi.getBounds().getCenterX());
//					positionTable.addValue("Center Y",roi.getBounds().getCenterY());
//					if(cells instanceof PropertiesCells){
//						Set<Integer> propIds=((PropertiesCells)cells).getPropertyIdSet();
//						Iterator<Integer> piter= propIds.iterator();
//						while(piter.hasNext()){
//							int propId=piter.next();
//							String propName=((PropertiesCells)cells).getPropertyName(propId);							
//							Double propVal=curPoly.getPropertyValue(propId);
//							if(propVal!=null){
//								positionTable.addValue(propName, propVal);
//							}
//						}
//						
//						
//				}
//			}
//		}
//	}
//}


	
//    public static void serializeObjectToXML(String xmlFileLocation,Object objectToSerialize) throws Exception 
//    {
//        FileOutputStream os = new FileOutputStream(xmlFileLocation);
//        XMLEncoder encoder = new XMLEncoder(os);
//        encoder.writeObject(objectToSerialize);
//        encoder.close();
//    }
//
//
//
//public static void writeXML(Cells cells, String filename)
//{
//	try {
//		Set<Cell> cs=cells.getCellsInFrame(1001);
//		serializeObjectToXML(filename,cs.iterator().next());
//	} catch (Exception e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//}

}
