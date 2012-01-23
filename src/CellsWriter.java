import ij.IJ;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.beans.*;

public class CellsWriter {
	
	public static boolean writeStructure(String filename,Cells cells){
		boolean suc=false;
//		writeXML(cells, filename);
		String  toWrite=getSturcutreProperties(cells);	
		FileOutputStream fos = null;
		PrintStream out=null;

		try{
			fos = new FileOutputStream(filename);			
			out = new PrintStream(fos);
			out.print(toWrite);
			out.close();			
		}
		catch(IOException ex)
		{
			IJ.showMessage("Export cellsStruct failed ","io: "+ex);
			ex.printStackTrace();
		}
		return suc;
	}
		
	
	public static String getSturcutreProperties(Cells cells){		
		String res="---cell ids---\n";			     	
		//output all the cell by ids.
		Set<Integer> cellIds=cells.keySet();
		Iterator<Integer> iter=cellIds.iterator();
		while(iter.hasNext()){
			Integer cellId=iter.next();
			Cell curCell=cells.get(cellId);
			res+=("cell id = " +cellId.toString()+" ; "+curCell.getName()+"\n");		
		}
		res+="---\n---cell poly---";	
		//output all cell's locations by frame
		Collection<Cell> allCells=cells.values();
		Iterator<Cell> citer= allCells.iterator();
		while(citer.hasNext()){
			Cell curCell=citer.next();
			res+="\nCell id="+curCell.getId()+"\n";
			Set<Integer> frames=curCell.getFrames();	
			frames = new TreeSet<Integer>(frames);
			iter=frames.iterator();
			while(iter.hasNext()){
				Integer curFrame=iter.next();
				PolyProperty curPoly=curCell.getLocationInFrame(curFrame);
				res+=curFrame+"="+curPoly.roiToWriter()+"\n";
			}
		}
		res+="\n---properties---\n";
		// now we output the properties of each cell
		if(cells instanceof PropertiesCells){
			Set<Integer> propIds=((PropertiesCells)cells).getPropertyIdSet();
			Iterator<Integer> piter= propIds.iterator();
			while(piter.hasNext()){
				int propId=piter.next();
				res+="propName="+ ((PropertiesCells)cells).getPropertyName(propId);
				citer= allCells.iterator();
				while(citer.hasNext()){
					Cell curCell=citer.next();
					res+="\nCell id="+curCell.getId()+"\n";
					Set<Integer> frames=curCell.getFrames();
					frames = new TreeSet<Integer>(frames);
					iter=frames.iterator();
					while(iter.hasNext()){
						Integer curFrame=iter.next();
						PolyProperty curPoly=curCell.getLocationInFrame(curFrame);
						Double propVal=curPoly.getPropertyValue(propId);
						if(propVal!=null){
							res+=curFrame+"="+propVal+";";
						}
					}
				}
				res+="\n---\n";
			}			
		}
		//now we output the mother-daughter relations
		res+="---mother daughter---\n";
		citer= allCells.iterator();
		while(citer.hasNext()){
			Cell curCell=citer.next();
			Set<Cell> moms=curCell.getMothers();
			if(moms!=null){
				Iterator<Cell> miter=moms.iterator();
				while(miter.hasNext()){
					Cell mom=miter.next();
					res+=mom.getId()+","+curCell.getId()+"\n";
				}
			}
		}
		return res;
	}
	
    public static void serializeObjectToXML(String xmlFileLocation,Object objectToSerialize) throws Exception 
    {
        FileOutputStream os = new FileOutputStream(xmlFileLocation);
        XMLEncoder encoder = new XMLEncoder(os);
        encoder.writeObject(objectToSerialize);
        encoder.close();
    }



public static void writeXML(Cells cells, String filename)
{
	try {
		Set<Cell> cs=cells.getCellsInFrame(1001);
		serializeObjectToXML(filename,cs.iterator().next());
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

}
