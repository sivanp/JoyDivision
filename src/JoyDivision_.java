import ij.IJ;
import ij.ImagePlus;

import javax.swing.JFrame;
//import ij.plugin.frame.RoiManager;
import java.awt.event.*;

import ij.gui.*;
import ij.io.OpenDialog;
import ij.io.SaveDialog;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ij.plugin.filter.*;
import ij.*;
import ij.process.ImageProcessor;

import javax.swing.*;

public class JoyDivision_  extends MouseAdapter implements PlugInFilter,ActionListener, ItemListener, ImageListener, KeyListener  {
	ImagePlus imp;	
	ImagePlus origImp;
	SortedMap<Integer, Integer> stack2frameMap;
	ImagePlus newImp;
	int curSlice;
	int curX=-1;
	int curY=-1;
	Cell curCell;
	Set<Cell> monitorSet;
	Cells cellsStruct;
	

	//GUI Stuff
	JPanel panel;
	JFrame frame;
	JButton butGetCell;
	JButton butCellDivision;
	JButton butAddSis;
	JButton butDeleteCell;
	JButton butAddMom;
	JButton butRemoveMom;
	JToggleButton butContMode;
	JButton butCellStart;
	JButton butCellEnd;
	JButton butAddLoc;
	JTextField textCellId;
	JTextField txtCellName;
	JTextField textMomId;
	JTextField txtMomName;
	JMenuBar menuBar;
	JMenu menu;
	JMenuItem menuItemSaveStruct;
	JMenuItem menuItemLoadStruct;
	

	boolean mouseListening=false;

	public void run(ImageProcessor ip) {

		createGui();


				Cell cell1=cellsStruct.addNewCell();		
				try {			
//					Roi roi1=new Roi(100,100,20,20);
//					IJ.showMessage("image of ROI is:"+roi1.getImage());
//					roi1=imp.getRoi();
//					IJ.showMessage("image of ROI is:"+roi1.getImage());
//					Polygon poly=roi1.getPolygon();
//					roi1=new PolygonRoi(poly,Roi.FREELINE);
//					IJ.showMessage("image of ROI is:"+roi1.getImage());
//					roi1.setImage(null);
//					cell1.addLocation(1003, roi1);
		//			cell1.addLocation(1002, roi1);
		//			cell1.addLocation(1001, roi1);
		//			Cell cell2=cellsStruct.addNewCell();
		//			cell2.addMother(cell1);
		//			Cell cell3=cellsStruct.addNewCell();
		//			cell2.addDaughter(cell3);
		//			cell1.addMother(cell3);
		//			cell3.addDaughter(cell1);			
		//			IJ.showMessage("before dissociating cell2 and cell1: "+cellsStruct.toString());
		//			cellsStruct.dissociateMotherDaughter(cell2,cell1);
		//			IJ.showMessage("after dissociating cell2 and cell1: "+cellsStruct.toString());			
		//			cellsStruct.remove(cell2);
		//			IJ.showMessage("after deleting 2: "+cellsStruct.toString());
		//			cell3.addMother(cell2);
		//			cell3.addMother(cell1);
		//			cellsStruct.dissociateMotherDaughter(cell3, cell1);
		//			IJ.showMessage("after dissociating cell3 to cell1 wrong: "+cellsStruct.toString());
		//			cellsStruct.dissociateMotherDaughter(cell1, cell3);
		//			IJ.showMessage("after dissociating cell3 to cell1 right: "+cellsStruct.toString());
		//			cell1.addDaughter(cell3);
		//			IJ.showMessage("after adding cell3 to cell1 right: "+cellsStruct.toString());
		//			cellsStruct.remove(cell1);
		//			IJ.showMessage("after removing cell1: "+cellsStruct.toString());
		//			cellsStruct.remove(cell3);
		//			IJ.showMessage("after removing cell3: "+cellsStruct.toString());
		//
		//
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
//				IJ.showMessage("before save: "+cellsStruct.toString());
//				String filename="C:/Users/sivan-nqb/Desktop/cellStruct";
//				saveCellsStruct(filename);
//				Cells cellsStructLoaded = loadCellsStruct(filename);
//				IJ.showMessage("after save: "+cellsStructLoaded.toString());
		//
		//
		//		
		//		IJ.showMessage(stack2frameMap.toString());
		//		


	}

	/**
	 * 
	 */
	private void createGui() {
		IJ.run("Misc...", "divide=Infinity require run");
		frame = new JFrame("testing!");
		panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();	
		panel.setBackground(SystemColor.control);
		c.fill = GridBagConstraints.HORIZONTAL;

		menuBar = new JMenuBar();
		menu = new JMenu("File");		
		menuBar.add(menu);
		
		menuItemSaveStruct = new JMenuItem("save cell structure");
		menuItemSaveStruct.addActionListener(this);
		menu.add(menuItemSaveStruct);
		
		menuItemLoadStruct=new JMenuItem("Load cell structure");
		menuItemLoadStruct.addActionListener(this);
		menu.add(menuItemLoadStruct);		
		
		menuBar.add(menu);
		//menuBar.s
		
		
		c.weightx=0.5;
		c.gridx=0;
		c.gridy=0;
		panel.add(new JLabel("cell id:"),c);

		textCellId=new JTextField("");
		textCellId.addActionListener(this);
		c.weightx=0.5;
		c.gridx=1;
		c.gridy=0;		
		panel.add(textCellId,c);

		c.weightx=0.5;
		c.gridx=2;
		c.gridy=0;
		panel.add(new JLabel("cell name:"),c);

		txtCellName=new JTextField("");		
		c.weightx=0.5;
		c.gridx=3;
		c.gridy=0;
		panel.add(txtCellName,c);

		c.weightx=0.5;
		c.gridx=0;
		c.gridy=1;
		panel.add(new JLabel("mom id:"),c);

		textMomId=new JTextField("");	
		c.weightx=0.5;
		c.gridx=1;
		c.gridy=1;		
		panel.add(textMomId,c);

		c.weightx=0.5;
		c.gridx=2;
		c.gridy=1; 
		panel.add(new JLabel("mom name:"),c);

		txtMomName=new JTextField("");		
		c.weightx=0.5;
		c.gridx=3;
		c.gridy=1;
		panel.add(txtMomName,c);


		butGetCell = new JButton("Get Cell");
		butGetCell.addActionListener(this);
		c.weightx=0.5;
		c.gridx=0;
		c.gridy=2;
		panel.add(butGetCell,c);

		butCellDivision = new JButton("Cell Division");
		butCellDivision.addActionListener(this);
		c.weightx=0.5;
		c.gridx=1;
		c.gridy=2;
		panel.add(butCellDivision,c);

		butAddSis = new JButton("Add Sister");
		butAddSis.addActionListener(this);
		c.weightx=0.5;
		c.gridx=2;
		c.gridy=2;
		panel.add(butAddSis,c);

		butDeleteCell = new JButton("Delete Cell");
		butDeleteCell.addActionListener(this);
		c.weightx=0.5;
		c.gridx=3;
		c.gridy=2;
		panel.add(butDeleteCell,c);

		butAddMom = new JButton("Add Mother");
		butAddMom.addActionListener(this);
		c.weightx=0.5;
		c.gridx=0;
		c.gridy=3;
		panel.add(butAddMom,c);
		
		butRemoveMom = new JButton("Remove Mother");
		butRemoveMom.addActionListener(this);
		c.weightx=0.5;
		c.gridx=0;
		c.gridy=4;
		panel.add(butRemoveMom,c);

		butContMode = new JToggleButton("Cont Mode");
		butContMode.addActionListener(this);
//		butContMode.doClick();
		c.weightx=0.5;
		c.gridx=3;
		c.gridy=3;
		panel.add(butContMode,c);

		butCellStart = new JButton("Go to Cell Start");
		butCellStart.addActionListener(this);
		c.weightx=0.5;
		c.gridx=0;
		c.gridy=5;
		panel.add(butCellStart,c);

		butCellEnd = new JButton("Go to Cell End");
		butCellEnd.addActionListener(this);
		c.weightx=0.5;
		c.gridx=1;
		c.gridy=5;
		panel.add(butCellEnd,c);

		butAddLoc = new JButton("Add roi to cell");		
		butAddLoc.addActionListener(this);
		c.weightx=0.5;
		c.gridx=0;
		c.gridy=6;
		panel.add(butAddLoc,c);



		
		
		imp.getCanvas().addMouseListener(this);
		imp.getCanvas().addKeyListener(this) ;
		ImagePlus.addImageListener(this);

		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(panel,BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	public int setup(String arg, ImagePlus imp) {		
		this.imp = imp;
		this.origImp=imp.duplicate();		
		stack2frameMap=new TreeMap<Integer, Integer>();
		cellsStruct=new Cells();
		this.generateStack2FrameMapping();
		curSlice=1;
		curCell=null;
		monitorSet=new HashSet<Cell>();
		
		return DOES_ALL;
	}

	public void mouseReleased(MouseEvent m) {
		if(mouseListening){
			ImageWindow win = imp.getWindow();
			ImageCanvas canvas=win.getCanvas();
			int x=m.getX();
			int y=m.getY();
			curX=canvas.offScreenX(x);
			curY=canvas.offScreenY(y);
			updateCellInCoordiantes(curX,curY);
			mouseListening=false;
		}
	}

	/**
	 * Look for a cell containing coordinates x,y in the current frame. 
	 * If finds updates curCell and the Gui -> note if they are more then one returns the first found.
	 * @param x
	 * @param y
	 */
	void updateCellInCoordiantes(int x, int y){
		Set<Cell> containedCells=cellsStruct.getCellsContaining(getCurFrame(), x, y);
		if(!containedCells.isEmpty()){
			Cell c=containedCells.iterator().next();
			this.updateCurCell(c);
		}		
		else{
			this.updateCurCell(null);
		}
	}

	void setCellsGui(){
		if(curCell==null){
			this.textCellId.setText("");
			this.textMomId.setText("");
			this.txtCellName.setText("");
			this.txtMomName.setText("");
		}
		else{		
			this.textCellId.setText(String.valueOf(curCell.getId()));
			this.txtCellName.setText(curCell.getName());
			Set<Cell> moms=curCell.getMothers();
			if(moms==null || moms.isEmpty()){
				this.textMomId.setText("");
				this.txtMomName.setText("");
			}
			else{
				Cell momCell=moms.iterator().next();
				this.textMomId.setText(String.valueOf(momCell.getId()));
				this.txtMomName.setText(momCell.getName());
			}	

		}
	}
	/**
	 * 
	 * @return the current frame from the current slice name (in the label)
	 */
	public int getCurFrame(){
		ImageStack stack=imp.getStack();
		String label=stack.getSliceLabel(imp.getCurrentSlice());
		PathTokens pt=new PathTokens(label);
		return pt.getFrame();
	}

	/**
	 * 
	 * @return the current site from the current slice name (in the label)
	 */
	public int getCurSite(){
		ImageStack stack=imp.getStack();
		String label=stack.getSliceLabel(imp.getCurrentSlice());
		PathTokens pt=new PathTokens(label);
		return pt.getSite();
	}

	public void actionPerformed(ActionEvent e){

		if (e.getSource() == butGetCell) {
			mouseListening=true;

		}
		if(e.getSource() ==butAddLoc){
			Roi roi =imp.getRoi();
			this.addRoiToCell(roi);
			this.drawFrame();
		}
		else if(e.getSource()==textCellId){
			if(textCellId.getText().equals("")){
				this.updateCurCell(null);
			}
			else{
				int id = Integer.parseInt(textCellId.getText());
				Cell cell=cellsStruct.getCell(id);
				this.updateCurCell(cell);				
			}				
		}
		
		else if(e.getSource()==butCellDivision){
			Roi roi = imp.getRoi();
			if(roi==null || curCell==null){
				IJ.showMessage("can't divide: either no current cell or no roi");
				return;
			}				
			Cell mom=curCell;
			Cell newCell=cellsStruct.addNewCell();
			newCell.addMother(mom);
			this.updateCurCell(newCell);			
			this.addRoiToCell(imp.getRoi());			
		}
		
		else if(e.getSource()==butCellStart){
			getToCellStart();
		}
		
		else if(e.getSource()==butCellEnd){
			getToCellEnd();
		}

		else if(e.getSource()==butAddMom){
			if(curCell==null){
				IJ.showMessage("cannot add mother: no current cell");
				return;
			}
			GenericDialog gd=new GenericDialog("Add mother cell with id:");
			gd.addNumericField("what is mother id?", -1, 4);
			gd.showDialog();
			int momid=(int)gd.getNextNumber();		
			Cell mom=cellsStruct.getCell(momid);
			if(mom==null){
				IJ.showMessage("cannot add mother: no cell exist with the given id");
				return;
			}
			curCell.addMother(mom);
			this.setCellsGui();			
		}
		else if(e.getSource()==butRemoveMom){
			if(curCell==null){
				IJ.showMessage("cannot add mother: no current cell");
				return;
			}
			GenericDialog gd=new GenericDialog("Remove mother cell with id:");
			gd.addNumericField("what is mother id?", -1, 4);
			gd.showDialog();
			int momid=(int)gd.getNextNumber();		
			Cell mom=cellsStruct.getCell(momid);
			if(mom==null){
				IJ.showMessage("cannot remove mother: no cell exist with the given id");
				return;
			}
			curCell.removeMother(mom);
			this.setCellsGui();			
		}
		
		else if(e.getSource()==butDeleteCell){
			if(curCell==null){
				IJ.showMessage("cannot remove: no current cell");
				return;
			}
			cellsStruct.remove(curCell);
			this.updateCurCell(null);
			drawFrame();					
		}
		else if(e.getSource()==butAddSis){
			IJ.showMessage("Not set yet go to mother....");
		}
		
		else if(e.getSource()==butContMode){
			if(butContMode.isSelected()){
				Thread queryThread = new Thread() {
				      public void run() {
				    	  wandTrack();
				      }
				    };
				    queryThread.start();
				    drawFrame();
			}
			
		}
		
		else if(e.getSource()==menuItemSaveStruct){
			this.saveCellStructure();	
			 
		}
		else if(e.getSource()==menuItemLoadStruct){
			cellsStruct=this.loadCellsStruct();
			drawFrame();
		}
	}
	public void itemStateChanged(ItemEvent e){
		IJ.showMessage("itemPerformed", "at: "+e);	
	}


	/**
	 * This generates the mapping between the stack numbers and specific frames according to the pathTokenizer
	 */
	protected void generateStack2FrameMapping(){
		ImageStack stack=imp.getStack();
		//TODO deal with when the stack is not virtual
		for(int i=1; i<=stack.getSize();i++){
			String label =stack.getSliceLabel(i);
			PathTokens pt= new PathTokens(label);
			int frame=pt.getFrame();
			stack2frameMap.put(new Integer(i),new Integer(frame));
		}
	}

	public void drawFrame(){
		//TODO deal with when the stack is virtual
		int stackFrame=imp.getCurrentSlice();	
		Overlay ov = new Overlay();

		//look for all Rois for this frame
		Integer frame = stack2frameMap.get(stackFrame);
		Set<Cell> frameCells = cellsStruct.getCellsInFrame(frame);
		if(frameCells!=null){
			Iterator<Cell> iter=frameCells.iterator();
			while(iter.hasNext()){
				Cell curCell=iter.next();			
				Roi mroi=(Roi) curCell.getLocationInFrame(frame).clone();
				mroi.setStrokeColor(Color.GREEN);
				ov.add(mroi);
			}
		}
		imp.setOverlay(ov);
		imp.updateAndDraw();
	}


	/**
	 * Saves the cells in the cellsStruct 
	 * @param filename the path of the file save the cellsStruct in
	 */
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
	
	public void saveCellStructure(){
		 SaveDialog sd = new SaveDialog("Save cell structure", "cell Struct", "");
		 String name = sd.getFileName();
        if (name==null){
            return;  
        }
        String directory = sd.getDirectory();         
        String path = directory+name;
        this.saveCellsStruct(path);
	}

	
	public Cells loadCellsStruct(){
		OpenDialog od = new OpenDialog("Choose a cells structure file (note this will erase current structure)", null);  
        String dir = od.getDirectory();  
        if (null == dir){ 
        	return null; // dialog was canceled  
        }
        dir = dir.replace('\\', '/'); // Windows safe  
        if (!dir.endsWith("/")){
        	dir += "/";  
        }
        String path=dir + od.getFileName();
        return loadCellsStruct(path);
	}
	
	/**
	 * Load and return the SiteNqbCellMap in the given path into the cellsStruct object 
	 * @param filename the path of the file from which the cellsStruct is written to
	 * @return the SiteNqbCellMap saved in the file with the given filename
	 */
	protected Cells loadCellsStruct(String filename){
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			Cells cells=(Cells)in.readObject();
			in.close();
			return cells;
		}
		catch(IOException ex)
		{
			IJ.showMessage("IOException","io: "+ex);
			ex.printStackTrace();
			return null;
		}
		catch(ClassNotFoundException ex)    			
		{
			IJ.showMessage("Exception","classNotFound");
			ex.printStackTrace();
			return null;
		}
	}

	
	private boolean getToCellStart(){
		if(curCell==null){
			return false;
		}
		Set<Integer> cellFrames =curCell.getFrames();
		Iterator<Integer> fiter=cellFrames.iterator();
		int minFrame=Integer.MAX_VALUE;
		while(fiter.hasNext()){
			int f=fiter.next();
			if(f<minFrame){
				minFrame=f;
			}
		}
		int slice=getSliceOfFrame(minFrame);
		if(slice==-1){
			IJ.showMessage("Problem getting start frame for this cell");
			return false;
		}
		imp.setSlice(slice);
		return true;
	}
	
	private boolean getToCellEnd(){
		if(curCell==null){
			return false;
		}
		Set<Integer> cellFrames =curCell.getFrames();
		Iterator<Integer> fiter=cellFrames.iterator();
		int maxFrame=Integer.MIN_VALUE;
		while(fiter.hasNext()){
			int f=fiter.next();
			if(f>maxFrame){
				maxFrame=f;
			}
		}
		int slice=getSliceOfFrame(maxFrame);
		
		if(slice==-1){
			IJ.showMessage("Problem getting start frame for this cell");
			return false;
		}
		imp.setSlice(slice);
		return true;
	}
	/**
	 * Add the given ROI to the current cell in the current frame- according to the UI
	 * @param roi
	 */
	public void addRoiToCell(Roi roi){
		if(roi==null){
			IJ.showMessage("no roi is found");
			return;
		}
		ImageStack stack=imp.getStack();
		int slice=imp.getCurrentSlice();
		String filename=stack.getSliceLabel(slice);
		PathTokens pt=new PathTokens(filename);
		int frame=pt.frame;
		if(curCell==null){
			GenericDialog gd = new GenericDialog("YesNoCancel ");
			gd.addMessage("There is no selected cell add a new cell with this Roi?");
			gd.enableYesNoCancel();
			gd.showDialog();
			if (gd.wasCanceled())
				return;
			else if (!gd.wasOKed())
				return;
			Cell newCell= cellsStruct.addNewCell();	
			this.updateCurCell(newCell);
		}
		//To do check distance from previous and possible current
		curCell.addLocation(frame, roi);

	}

	
	@Override
	public void imageOpened(ImagePlus imp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void imageClosed(ImagePlus imp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void imageUpdated(ImagePlus imp) {
		if(curSlice!=imp.getSlice()){
			curSlice=imp.getSlice();
			drawFrame();
		}

	}
	private void updateCurCell(Cell newCell){
		curCell=newCell;
		setCellsGui();
	}

	@Override
	public void keyPressed(KeyEvent key) {
	}
	private int getSliceOfFrame(int frame){
		Iterator<Integer> slicesIter=this.stack2frameMap.keySet().iterator();		
		boolean found=false;
		while(slicesIter.hasNext() && !found){
			int slice=slicesIter.next();
			if(stack2frameMap.get(slice).intValue()==frame){
				return slice;				
			}
		}
		return -1;
	}



	@Override
	public void keyReleased(KeyEvent key) {


	}

	@Override
	public void keyTyped(KeyEvent key) {
		//n means add current selection as roi for the current cell and go to the next slice 
		if(key.getKeyChar()=='n'){
			this.addRoiToCell(imp.getRoi());
			boolean suc=nextSlice();
			if(!suc){
				drawFrame();
			}
		}
		//a means add current selection as roi for the current cell and go to the next slice 
		else if(key.getKeyChar()=='a'){
			this.addRoiToCell(imp.getRoi());
			drawFrame();
		}		
	}

	void wandTrack(){
		//TODO - how to make it on a monitor list
		Roi roi= imp.getRoi();
		if(roi==null){
			return;
		}
		this.addRoiToCell(roi);
		if(curCell==null){
			IJ.showMessage("cannot track: no current cell");
			return;
		}
		//TODO - check about the offScreen if needed here...
		int x =(int) roi.getBounds().getCenterX();
		int y =(int) roi.getBounds().getCenterY();
		//TODO need to calculate nearest mask.... if want to mark it
		//gotNext=true;
		boolean gotNext=nextSlice();	
		while(butContMode.isSelected()){
					
			if(!gotNext){	
				 drawFrame();
				butContMode.setSelected(false);
				return;
			}
			double tolerance=0;
			int dots=IJ.doWand(x, y, tolerance,  "8-connected" ); //looks like this returns the number of points
//			IJ.showMessage("tracking wand dots: "+dots);
			if(dots==0){
				butContMode.setSelected(false);
				return;
			}
			roi= imp.getRoi();
			this.addRoiToCell(roi);
			x =(int) roi.getBounds().getCenterX();
			y =(int) roi.getBounds().getCenterY();
			//  
			//do what you want to do before sleeping
			  try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//sleep for 1000 ms
			 
			gotNext=nextSlice();
			
		}
		
	}

//	/**
//	 * 
//	 * @return Roi of the current cell in the previous slice, null if can't find this.
//	 */
//
//	Roi getRoiOfPreviuosSlice(){
//		if(curCell!=null){
//			int curSlice=imp.getCurrentSlice();
//			int prevFrame=this.stack2frameMap.get(curSlice-1);			
//			Roi roi=curCell.getLocationInFrame(prevFrame);
//			return roi;
//		}
//		return null;
//	}

	
	
	
private boolean nextSlice(){
	boolean res=false;
	int curSlice=imp.getCurrentSlice();
	if(curSlice<imp.getImageStackSize()){
		imp.setSlice(++curSlice);
		res=true;
	}
	return res;
}

}
//public void drawFrame(){
//		//TODO deal with when the stack is not virtual
//		int stackFrame=imp.getCurrentSlice();		
//		ImageStack imstack=imp.getStack();
//		ImageProcessor ip2 = imstack.getProcessor(stackFrame);
//		PathTokens pt=new PathTokens(imstack.getSliceLabel(stackFrame));
//		ip2.setColor(Color.GREEN);
//		//look for all Rois for this frame
//		Integer frame = stack2frameMap.get(stackFrame);
//		List<NQBCell> frameCells= cellsStruct.getCellsInFrame(pt.site, frame);
//		ListIterator<NQBCell> iter=frameCells.listIterator();
//		while(iter.hasNext()){
//			NQBCell curCell=iter.next();
//			Roi mroi=(Roi) curCell.getLocationOfFrame(frame).clone();
//			ip2.setRoi(mroi);
//			ip2.draw(mroi);
//		}
//		imp.updateAndDraw();
//		byte[] pix = (byte[])ip2.getPixels();
//		IJ.showMessage(" pix 200 200 :"+pix[400]);
//		int width = ip2.getWidth();  
//	    int height = ip2.getHeight();  
//	    byte[] new_pixels = new byte[width * height];  
//	    for (int y=0; y<height; y++) {  
//	        for (int x=0; x<width; x++) {  
//	            // Editing pixel at x,y position  
//	            new_pixels[y * width + x] =127;  
//	        }  
//	    }  
//	    // update ImageProcessor to new array  
//	    ip2.setPixels(new_pixels);  
//		IJ.showMessage(" pix 200 200 :"+new_pixels[400]);
//		imp.updateAndDraw(); imp.show();
//		ImageProcessor ip3=imp.getProcessor();
//		IJ.showMessage(" ip3 pix 200 200 :"+ip3.getPixel(200,200));	

//	}


/**
 * Adds the given roi to the cell with the given cell id in the GUI
 * @param frame 
 * @param id the id of the cell to be added
 * @param roi the roi
//	 */
//	public void addRoiToCell(int id, int frame, Roi roi){
//		
//		NQBCell curCell=cellsStruct.addNQBCellInFrame(id, frame, roi);
//		if(curCell==null){
//			IJ.showMessage("addRoiToCell","failed to add roi to cell: "+id);
//		}		
//	}
