import ij.plugin.filter.PlugInFilter;
import java.awt.Color;
import java.util.*;
import java.io.*;
import java.lang.Float;
import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.process.*;
import ij.plugin.filter.ParticleAnalyzer;
import ij.measure.*;
 

/**
	Uses ImageJ's particle analyzer to track the movement of
	multiple objects through a stack.
	Based on the Object Tracker plugin filter by Wayne Rasband

	Based on Multitracker, but should be quite a bit more intelligent
	Nico Stuurman, Vale Lab, UCSF/HHMI, May,June 2003
	
	Modified by David Eccles (gringer) <bioinformatics@gringer.org>
	David Eccles, MPI Münster, September 2011
	 - new result save format, 7-column: track,frame,x,velX,y,velY,flags
	 - separate summary table: track,length,distance,nFrames,mean/median velocity
	 - can take previous velocity into account when discovering new positions
	 - particle processing changed to per frame, rather than per track
	 - collision is only flagged when a track cannot be assigned uniquely
*/
public class MTrack3_ implements PlugInFilter, Measurements  {

	ImagePlus	imp;
	int		nParticles;
	float[][]	ssx;
	float[][]	ssy;
	String directory,filename;

	static int	minSize = 1;
	static int	maxSize = 999999;
	static int 	minTrackLength = 2;
	static boolean 	bSaveResultsFile = false;
	static boolean 	bShowLabels = false;
	static boolean 	bShowPositions = false;
	static boolean 	bShowPaths = false;
	static boolean 	bShowPathLengths = false;
	static boolean  bUseVelocity = true;
   static float   	maxVelocity = 10;
	static int 	maxColumns=75;
   static boolean skipDialogue = false;
   private static String resultsFileName = null;

//JoyDivision 
   Cells cells ;
   
   public class Particle {
		float	x;
		float	y;
		float   velX = 0.0f;
		float   velY = 0.0f;
		int	z;
		int	trackNr = 0;
		int displayTrackNr = 0;
		boolean inTrack=false;
		boolean flag=false;

		public void copy(Particle source) {
			this.x=source.x;
			this.y=source.y;
			this.velX = source.velX;
			this.velY = source.velY;
			this.z=source.z;
			this.inTrack=source.inTrack;
			this.flag=source.flag;
		}

		public float distance (Particle p, boolean useVelocity) {
			if(useVelocity){
				return (float) Math.sqrt(sqr(this.x - (p.x + p.velX)) + 
						sqr(this.y - (p.y + p.velY)));
			} else {
				return (float) Math.sqrt(sqr(this.x - p.x) + 
						sqr(this.y - p.y));
			}
		}
	}

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		if (IJ.versionLessThan("1.17y"))
			return DONE;
		else
			return DOES_8G+NO_CHANGES;
	}

   public static void setProperty (String arg1, String arg2) {
      if (arg1.equals("minSize"))
         minSize = Integer.parseInt(arg2);
      else if (arg1.equals("maxSize"))
         maxSize = Integer.parseInt(arg2);
      else if (arg1.equals("minTrackLength"))
         minTrackLength = Integer.parseInt(arg2);
      else if (arg1.equals("maxVelocity"))
         maxVelocity = Float.valueOf(arg2).floatValue();
      else if (arg1.equals("resultsFileName"))
          resultsFileName = arg2;
      else if (arg1.equals("saveResultsFile"))
         bSaveResultsFile = Boolean.valueOf(arg2);
      else if (arg1.equals("showPathLengths"))
         bShowPathLengths = Boolean.valueOf(arg2);
      else if (arg1.equals("showLabels"))
         bShowLabels = Boolean.valueOf(arg2);
      else if (arg1.equals("showPositions"))
         bShowPositions = Boolean.valueOf(arg2);
      else if (arg1.equals("showPaths"))
         bShowPaths = Boolean.valueOf(arg2);
      else if (arg1.equals("useVelocity"))
          bUseVelocity = Boolean.valueOf(arg2);
      else if (arg1.equals("skipDialogue"))
         skipDialogue = Boolean.valueOf(arg2);
   }

	public void run(ImageProcessor ip) {
      if (!skipDialogue) {
         GenericDialog gd = new GenericDialog("Object Tracker");
         gd.addNumericField("Minimum Object Size (pixels): ", minSize, 0);
         gd.addNumericField("Maximum Object Size (pixels): ", maxSize, 0);
         gd.addNumericField("Maximum_ Velocity:", maxVelocity, 0);
         gd.addNumericField("Minimum_ track length (frames)", minTrackLength, 0);
         gd.addCheckbox("Save Results File", bSaveResultsFile);
         gd.addCheckbox("Display Path Lengths", bShowPathLengths);
         gd.addCheckbox("Show Labels", bShowLabels);
         gd.addCheckbox("Show Positions", bShowPositions);
         gd.addCheckbox("Show Paths", bShowPaths);
         gd.addCheckbox("Predict Using Velocity", bUseVelocity);
         gd.showDialog();
         if (gd.wasCanceled())
            return;
         minSize = (int)gd.getNextNumber();
         maxSize = (int)gd.getNextNumber();
         maxVelocity = (float)gd.getNextNumber();
         minTrackLength = (int)gd.getNextNumber();
         bSaveResultsFile = gd.getNextBoolean();
         bShowPathLengths = gd.getNextBoolean();
         bShowLabels = gd.getNextBoolean();
         bShowPositions = gd.getNextBoolean();
         bShowPaths = gd.getNextBoolean();
         bUseVelocity = gd.getNextBoolean();
         if (bShowPositions)
            bShowLabels =true;
      }
      if (bSaveResultsFile) {
    	  if(resultsFileName == null){
    		  SaveDialog sd=new  SaveDialog("Save Track Results","particlePositions",".csv");
    		  directory=sd.getDirectory();
    		  filename=sd.getFileName();
    		  resultsFileName = directory+filename;
    	  } else {
    		  directory = new File(resultsFileName).getParent();
    		  if(directory == null){
    			  directory = "";
    		  }
    		  filename = new File(resultsFileName).getName();
    	  }
      }
		track(imp, minSize, maxSize, maxVelocity, directory, filename);
	}
	

	public Vector<Vector<Particle>>  track(ImagePlus imp, int minSize, int maxSize, float maxVelocity, String directory, String filename) {
		int nFrames = imp.getStackSize();
		if (nFrames<2) {
			IJ.showMessage("MTrack3", "Stack required");
			return null;
		}

		ImageStack stack = imp.getStack();
		int options = 0; // set all PA options false
		int measurements = CENTROID;

		// Initialize results table
		ResultsTable rt = new ResultsTable();
		rt.reset();

		// create storage for particle positions
		Vector<Vector<Particle>> theParticles = new Vector<Vector<Particle>>();
		int trackCount=0;

		IJ.showStatus("Recording particle positions and assembling tracks");
		// record particle positions for each frame in an ArrayList
		for (int iFrame = 0; iFrame < nFrames; iFrame++) {
			IJ.showProgress((double)iFrame / nFrames);
			Vector<Particle> currentFrameParticles = new Vector<Particle>();
			rt.reset();
			ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize);
			if(!pa.analyze(imp, stack.getProcessor(iFrame+1))){
				// the particle analyzer should produce a dialog on error, so don't make another one
				return null;
			}
			float[] sxRes = rt.getColumn(ResultsTable.X_CENTROID);				
			float[] syRes = rt.getColumn(ResultsTable.Y_CENTROID);
			if (sxRes==null)
				continue;
			HashSet<Particle> assignedParticles = new HashSet<Particle>();
			for (int iPart=0; iPart<sxRes.length; iPart++) {
				Particle aParticle = new Particle();
				aParticle.x=sxRes[iPart];
				aParticle.y=syRes[iPart];
				aParticle.z=iFrame+1;
				if(iFrame == 0){
					// initialise particle
					aParticle.trackNr = ++trackCount;
				} else {
					// look for closest particle from a previous frame
					Particle closestParticle = theParticles.get(iFrame-1).firstElement();
					for(Particle oldParticle : theParticles.get(iFrame-1)){
						if(aParticle.distance(oldParticle, bUseVelocity) < 
								aParticle.distance(closestParticle, bUseVelocity)){
							closestParticle = oldParticle;
						}
					}
					if(aParticle.distance(closestParticle, false) <= maxVelocity){
						// found a good fit
						int closestTrack = closestParticle.trackNr;
						if(assignedParticles.contains(closestParticle)){
							// the particle from the previous frame has already been 
							// assigned to a track in this frame, so report collision
							closestParticle.flag = true;
							// a new track is created after the collision
							aParticle.trackNr = ++trackCount;
						} else {
							aParticle.trackNr = closestTrack;
							aParticle.velX = aParticle.x - closestParticle.x;
							aParticle.velY = aParticle.y - closestParticle.y;
							assignedParticles.add(closestParticle);
						}
					} else {
						// no matching particle, so create a new one
						aParticle.trackNr = ++trackCount;
					}
				}
				currentFrameParticles.add(aParticle);
			}
			theParticles.add(currentFrameParticles);
		}
		IJ.showProgress(1.0);

		boolean writefile=false;
		if (filename != null) {
			File outputfile=new File (directory,filename);
			if (!outputfile.canWrite()) {
				try {
					outputfile.createNewFile();
				}
				catch (IOException e) {
					IJ.showMessage ("Error", "Could not create "+directory+filename);
				}
			}
			if (outputfile.canWrite())
				writefile=true;
			else
				IJ.showMessage ("Error", "Could not write to " + directory + filename);
		}

		IJ.showStatus("Creating particle tracks");
		Vector<Vector<Particle>> theTracks = new Vector<Vector<Particle>>(trackCount);
		for(int i = 0; i < trackCount; i++){
			theTracks.add(new Vector<Particle>());
		}
		int framesProcessed = 0;
		for(Vector<Particle> currentFrameParticles : theParticles){
			IJ.showProgress((framesProcessed++) / (double)nFrames);
			for(Particle aParticle : currentFrameParticles){
				int trackNumber = aParticle.trackNr;
				theTracks.get(trackNumber-1).add(aParticle);
			}
		}
		IJ.showProgress(1.0);
		
		Vector<Vector<Particle>> newTracks = new Vector<Vector<Particle>>();
		IJ.showStatus("Removing short tracks");
		int tracksProcessed = 0;
		for(Vector<Particle> trackParticles : theTracks){
			IJ.showProgress((tracksProcessed++) / (double)trackCount);
			if(trackParticles.size() >= minTrackLength){
				newTracks.add(trackParticles);
			}
		}
		IJ.showProgress(1.0);
		theTracks = newTracks;
		trackCount = theTracks.size();
	
		IJ.showStatus("Assigning track numbers and creating results");
		ResultsTable positionTable = new ResultsTable();
		// set up and display the table with particle positions
		if(theParticles.size() > 0)
		{
			for(int i = 0; i < theTracks.size(); i++)
			{
				Vector<Particle> trackParticles = theTracks.get(i);
				if(trackParticles.size() >= minTrackLength)
				{
					for(Particle aParticle : trackParticles)
					{	
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
		}
		if(writefile){
			try {
				positionTable.saveAs(directory + filename);
			} catch (IOException e) {
				IJ.showMessage ("Error", "Could not save to "+directory+filename);
				positionTable.show("Particle positions");
			}
		} else {
			positionTable.show("Particle positions");
		}
	
		
		// Now do the fancy stuff when requested:

		// makes a new stack with objects labeled with track nr
		// optionally also displays centroid position
		if (bShowLabels) {
			IJ.showStatus("Writing labels");
			Overlay imageOverlay = new Overlay();
			String strPart;
			ImageStack newstack = imp.createEmptyStack();
			int xHeight=newstack.getHeight();
			int yWidth=newstack.getWidth();
			for (int i=0; i<=(nFrames-1); i++) {
				int iFrame=i+1;
				ImageProcessor ip = stack.getProcessor(iFrame);
				newstack.addSlice(stack.getSliceLabel(iFrame),ip.crop());
				ImageProcessor nip = newstack.getProcessor(iFrame);
				nip.setColor(Color.black);
				for (Particle aParticle : theParticles.get(i)){
					// only show particles if they have a display number
					if (aParticle.displayTrackNr != 0) {
						strPart=""+aParticle.displayTrackNr;
						if(aParticle.flag){
							strPart += "*";
						}
						if (bShowPositions) {
							strPart+="="+(int)aParticle.x+","+(int)aParticle.y;
						}
						// we could do someboundary testing here to place the labels better when we are close to the edge
						imageOverlay.add(new Roi((int)aParticle.x, (int)aParticle.y, imp));
						nip.moveTo(doOffset((int)aParticle.x,xHeight,5),
								doOffset((int)aParticle.y,yWidth,5) );
						//nip.moveTo(doOffset((int)aParticle.x,xHeight,5),doOffset((int)aParticle.y,yWidth,5) );
						nip.drawString(strPart);
					}
				}
				IJ.showProgress((double)iFrame/nFrames);
			}
			ImagePlus nimp = new ImagePlus(imp.getTitle() + " labels",newstack);
			nimp.show();
			imp.show();
			nimp.updateAndDraw();
		}

		ResultsTable summaryTable = new ResultsTable();
		// total length traversed
		if (bShowPathLengths) {
			IJ.showStatus("Calculating path lengths");
			double[] lengths = new double[trackCount];
			double[] distances = new double[trackCount];
			double[] medianVelocities = new double[trackCount];
			int[] frames = new int[trackCount];
			Vector<Vector<Double>> trackVelocities = new Vector<Vector<Double>>();
			int displayTrackNr=0;
			for (Vector<Particle> bTrack : theTracks) {
				trackVelocities.add(displayTrackNr++, new Vector<Double>());
				ListIterator<Particle> jT=bTrack.listIterator();
				Particle oldParticle=jT.next();
				Particle firstParticle=new Particle();
				firstParticle.copy(oldParticle);
				frames[displayTrackNr-1]=bTrack.size();
				for (;jT.hasNext();) {
					Particle newParticle=jT.next();
					double velocity = Math.sqrt(sqr(oldParticle.x-newParticle.x)+sqr(oldParticle.y-newParticle.y));
					lengths[displayTrackNr-1]+=velocity;
					trackVelocities.get(displayTrackNr-1).add(velocity);
					oldParticle=newParticle;
				}
				distances[displayTrackNr-1]=Math.sqrt(sqr(oldParticle.x-firstParticle.x)+sqr(oldParticle.y-firstParticle.y));
				Collections.sort(trackVelocities.get(displayTrackNr-1));
				int tvSize = trackVelocities.get(displayTrackNr-1).size();
				if(tvSize % 2 == 0){						
					medianVelocities[displayTrackNr-1] = (trackVelocities.get(displayTrackNr-1)
							.get(tvSize / 2 -1) + trackVelocities.get(displayTrackNr-1)
							.get(tvSize / 2))/2;
				} else {
					medianVelocities[displayTrackNr-1] = trackVelocities.get(displayTrackNr-1)
					.get((tvSize-1) / 2);
				}
			}
			
			for (int i=0; i<displayTrackNr; i++) {
				summaryTable.incrementCounter();
				summaryTable.addValue("Track", i+1);
				summaryTable.addValue("Length", lengths[i]);
				summaryTable.addValue("Distance travelled", distances[i]);
				summaryTable.addValue("Number of Frames", frames[i]);
				summaryTable.addValue("Mean Velocity", lengths[i] / frames[i]);
				summaryTable.addValue("Median Velocity", medianVelocities[i]);
			}
			if(writefile){
				try {
					summaryTable.saveAs(directory + "summary_" + filename);
				} catch (IOException e) {
					IJ.showMessage ("Error", "Could not save to "+directory+filename);
					summaryTable.show("Track summaries");
				}
			} else {
				summaryTable.show("Track summaries");
			}
		}

		// 'map' of tracks
		if (bShowPaths) {
			IJ.showStatus("Creating paths");
			if (imp.getCalibration().scaled()) {
				IJ.showMessage("MTrack3", "Cannot display paths if image is spatially calibrated");
				return null;
			}
			ImageProcessor ip = new ByteProcessor(imp.getWidth(), imp.getHeight());
			ip.setColor(Color.white);
			ip.fill();
			trackCount=0;
			int color;
			for (Vector<Particle> bTrack : theTracks) {
				trackCount++;
				ListIterator<Particle> jT=bTrack.listIterator();
				Particle oldParticle=jT.next();
				for (;jT.hasNext();) {
					Particle newParticle=jT.next();
					color =Math.min(trackCount+1,254);
					ip.setValue(color);
					ip.moveTo((int)oldParticle.x, (int)oldParticle.y);
					ip.lineTo((int)newParticle.x, (int)newParticle.y);
					oldParticle=newParticle;
				}
			}
			new ImagePlus("Paths", ip).show();
		}
		IJ.showStatus("Done!");
		
		return theTracks;
	}

	// Utility functions
	double sqr(double n) {return n*n;}
	
	int doOffset (int center, int maxSize, int displacement) {
		if ((center - displacement) < 2*displacement) {
			return (center + 4*displacement);
		}
		else {
			return (center - displacement);
		}
	}

 	double s2d(String s) {
		Double d;
		try {d = new Double(s);}
		catch (NumberFormatException e) {d = null;}
		if (d!=null)
			return(d.doubleValue());
		else
			return(0.0);
	}

}


