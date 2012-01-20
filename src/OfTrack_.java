import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class OfTrack_ extends  MTrack3_ 
{
	
	Cells cellsStruct;
	
	@Override
	public int setup(String arg, ImagePlus imp) 
	{
		return super.setup(arg,imp);	
	}
	
	@Override
	public void run(ImageProcessor ip) 
	{
		Vector<Vector<Particle>> theTracks= track(imp, 50, 200,(float) 10.0,null ,null) ;
		
		cellsStruct = new Cells();
		
		
		
		
		ResultsTable positionTable = new ResultsTable();
		
		
			for(int i = 0; i < theTracks.size(); i++)
			{
				
				Vector<Particle> trackParticles = theTracks.get(i);
				if(trackParticles.size() >= minTrackLength)
				{
					Cell childCell = cellsStruct.addNewCell();
					for(Particle aParticle : trackParticles)
					{
						int slice= aParticle.z; 
						PathTokens pt=new PathTokens(imp.getStack(),slice);
						imp.setSlice(slice);
						IJ.doWand((int)aParticle.x, (int)aParticle.y);
						Roi roi = imp.getRoi();
						childCell.addLocation(pt.getFrame(), roi);
						
						positionTable.incrementCounter();
						aParticle.displayTrackNr = i+1;
						positionTable.addValue("Track", aParticle.displayTrackNr);
						positionTable.addValue("Frame", aParticle.z);
						positionTable.addValue("X", aParticle.x);
						positionTable.addValue("velX", aParticle.velX);
						positionTable.addValue("Y", aParticle.y);
						positionTable.addValue("velY", aParticle.velY);
						positionTable.addValue("Flags", (aParticle.flag?1:0));
					}
				}
			}
			positionTable.show("Particle positions");
			
			saveCellsStruct("C:\\Users\\owner\\Desktop\\IJ\\ghghj");
			
	}
	
	
	protected void saveCellsStruct(String filename){		
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try{
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(cellsStruct);
			out.close();
		}
		catch(IOException ex)
		{
			IJ.showMessage("Save cellsStruct failed ","io: "+ex);
			ex.printStackTrace();
		}

	}
	

}
