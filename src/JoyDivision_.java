import ij.IJ;
import ij.ImagePlus;

import javax.print.attribute.standard.JobName;
import javax.swing.JFrame;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

import ij.gui.*;
import ij.io.OpenDialog;
import ij.io.SaveDialog;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ij.measure.Measurements;
import ij.plugin.filter.*;
import ij.*;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import javax.swing.*;
 
public class JoyDivision_  extends MouseAdapter implements PlugInFilter,ActionListener, ItemListener, ImageListener, KeyListener  {
	ImagePlus imp;	
//	ImagePlus origImp;
	SortedMap<Integer, Integer> stack2frameMap;
	SortedMap<Integer, Integer> frame2timeMap;
	ImagePlus newImp;
	int curSlice;
	int curX=-1;
	int curY=-1;
	Cell curCell;
	Set<Cell> monitorSet;
	Cells cellsStruct;
	Font font=new Font("Book Antiqua", Font.PLAIN, 10);
	int waitmill=200;
	int allowedDist=35;
	double overlapingRatio=0.5;
	double consecAreaRatio=0.7;
	
	public enum cellFates {
		   DIE,TILLEND, OUT, FUSE, DIVABNORMALLY,SICK;
		}


	//GUI Stuff
	JPanel panel;
	JFrame frame;
	JButton butGetCell;
	JButton butCellDivision;
	JButton butAddSis;
	JButton butDeleteCell;
	JButton butDeleteCellLocations;
	JButton butSwapCells;
	JButton but;
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
	JCheckBox checkBoxCellDies;
	JCheckBox checkBoxCellOut;
	JCheckBox checkBoxCellTillEnd;
	JCheckBox checkBoxCellFuses;
	JCheckBox checkBoxCelldivAbnormally;
	JCheckBox checkBoxCellLooksSick;
	//ButtonGroup cellsChecksGroup = new ButtonGroup();
	
	JTextField txtRemark;
	
	JMenuBar menuBar;
	JMenu fileMenu;
	JMenuItem menuItemSaveStruct;
	JMenuItem menuItemLoadStruct;
	JMenuItem menuItemExportStruct;
	 	
	JMenu viewMenu;
	
	JCheckBoxMenuItem menuItemDisplayNames;
	JCheckBoxMenuItem menuItemDisplayRois;
	JMenuItem menuItemGetTimes;
	JMenuItem menuItemExtractFluo;
	JMenuItem menuItemExtractArea;
	JMenuItem menuItemRemoveProperty;
	JMenuItem menuItemDisplayProperties;	
	JMenuItem menuItemDisplayMoms;
	JMenuItem menuItemDisplayDaughters;
	JMenuItem menuItemDeleteSublineage;
	
	JMenu monitorMenu;
	JMenuItem menuItemAdd2Set;
	JMenuItem menuItemRemoveFromSet;
	JMenuItem menuItemShowSet;
	JMenuItem menuItemClearSet;
	
	JMenu propertiesMenu;
	JMenuItem menuItemChangeWait;
	JMenuItem menuItemChangeAllowedDist;	
	JMenu toolsMenu;
	JMenuItem menuItemPaprikaTrack;
	
	
	boolean mouseListening=false;

	public void run(ImageProcessor ip) {

		createGui();
	}

	/**
	 * 
	 */
	private void createGui() {
		IJ.run("Misc...", "divide=Infinity require run");
		frame = new JFrame("JoyDivision!");
		panel = new JPanel(new GridBagLayout());


		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");		
		menuBar.add(fileMenu);

		menuItemSaveStruct = new JMenuItem("save cell structure");
		menuItemSaveStruct.addActionListener(this);
		fileMenu.add(menuItemSaveStruct);

		menuItemLoadStruct=new JMenuItem("Load cell structure");
		menuItemLoadStruct.addActionListener(this);
		fileMenu.add(menuItemLoadStruct);		

		menuItemExportStruct = new JMenuItem("Export cell structure");
		menuItemExportStruct.addActionListener(this);
		fileMenu.add(menuItemExportStruct);
		
		viewMenu=new JMenu("Edit&View");
		menuBar.add(viewMenu);
		
		
		menuItemDisplayNames= new JCheckBoxMenuItem("display names");
		menuItemDisplayNames.doClick();
		menuItemDisplayNames.addActionListener(this);
		viewMenu.add(menuItemDisplayNames);
		
		menuItemDisplayRois= new JCheckBoxMenuItem("display rois");
		menuItemDisplayRois.doClick();
		menuItemDisplayRois.addActionListener(this);
		viewMenu.add(menuItemDisplayRois);	
		

		menuItemDisplayMoms=new JMenuItem("Show mothers ids");
		menuItemDisplayMoms.addActionListener(this);
		viewMenu.add(menuItemDisplayMoms);
		
		menuItemDisplayDaughters=new JMenuItem("Show daughters ids");
		menuItemDisplayDaughters.addActionListener(this);
		viewMenu.add(menuItemDisplayDaughters);		
		
		menuItemDeleteSublineage=new JMenuItem("Delete cell sublineage");
		menuItemDeleteSublineage.addActionListener(this);
		viewMenu.add(menuItemDeleteSublineage);		
				
		
		
		monitorMenu=new JMenu("Monitor Set");
		menuBar.add(monitorMenu);
		
		menuItemAdd2Set=new JMenuItem("Add to monitor set");
		menuItemAdd2Set.addActionListener(this);
		monitorMenu.add(menuItemAdd2Set);

		menuItemRemoveFromSet= new JMenuItem("Remove from monitor set");
		menuItemRemoveFromSet.addActionListener(this);
		monitorMenu.add(menuItemRemoveFromSet);
		
		menuItemShowSet = new JMenuItem("show cells ids in monitor set");
		menuItemShowSet.addActionListener(this);
		monitorMenu.add(menuItemShowSet);
		
		
		menuItemClearSet=new JMenuItem("Clear monitor set");
		menuItemClearSet.addActionListener(this);
		monitorMenu.add(menuItemClearSet);	
		
		
		propertiesMenu=new JMenu("Properties");
		menuBar.add(propertiesMenu);

		menuItemChangeWait=new JMenuItem("Set wait time");
		menuItemChangeWait.addActionListener(this);
		propertiesMenu.add(menuItemChangeWait);

		menuItemChangeAllowedDist= new JMenuItem("Set allowed distance");
		menuItemChangeAllowedDist.addActionListener(this);
		propertiesMenu.add(menuItemChangeAllowedDist);
		
		toolsMenu=new JMenu("tools");
		menuBar.add(toolsMenu);
		
		
		menuItemExtractFluo=new JMenuItem("Extract Fluorescence");
		menuItemExtractFluo.addActionListener(this);
		toolsMenu.add(menuItemExtractFluo);
		
		menuItemExtractArea=new JMenuItem("Extract Area");
		menuItemExtractArea.addActionListener(this);
		toolsMenu.add(menuItemExtractArea);
		
		menuItemRemoveProperty=new JMenuItem("Remove Property");
		menuItemRemoveProperty.addActionListener(this);
		toolsMenu.add(menuItemRemoveProperty);
		
		menuItemDisplayProperties=new JMenuItem("Display Properties");
		menuItemDisplayProperties.addActionListener(this);
		toolsMenu.add(menuItemDisplayProperties);
		
		menuItemGetTimes=new JMenuItem("Get time of stack files");
		menuItemGetTimes.addActionListener(this);
		toolsMenu.add(menuItemGetTimes);

		
		menuItemPaprikaTrack= new JMenuItem("Paprika track");
		menuItemPaprikaTrack.addActionListener(this);
		toolsMenu.add(menuItemPaprikaTrack);
		
		
		GridBagConstraints c = new GridBagConstraints();	
		panel.setBackground(SystemColor.control);
		c.fill = GridBagConstraints.HORIZONTAL;
		//c.gridwidth=GridBagConstraints.RELATIVE;
		c.gridwidth=3;
		c.weightx=0;
		c.gridx=0;
		c.gridy=0;		
		panel.add(new JLabel("cell id:"),c);

		textCellId=new JTextField("");
		textCellId.addActionListener(this);
		c.gridwidth=3;
		c.weightx=0.5;
		c.gridx=3;
		c.gridy=0;
		panel.add(textCellId,c);

		c.gridwidth=3;
		c.weightx=0;
		c.gridx=6;
		c.gridy=0;		
		panel.add(new JLabel("cell name:"),c);

		txtCellName=new JTextField("");
		txtCellName.setMinimumSize(new Dimension(20,20));
		c.gridwidth=3;
		c.weightx=0.5;
		c.gridx=9;
		c.gridy=0;
		panel.add(txtCellName,c);
		
		c.gridwidth=3;
		c.weightx=0;
		c.gridx=0;
		c.gridy=1;
		panel.add(new JLabel("mom id:"),c);

		textMomId=new JTextField("");
		c.gridwidth=3;
		c.weightx=0.5;
		c.gridx=3;
		c.gridy=1;	
		panel.add(textMomId,c);

		c.gridwidth=3;
		c.weightx=0;
		c.gridx=6;
		c.gridy=1;
		panel.add(new JLabel("mom name:"),c);

		txtMomName=new JTextField("");		
		c.gridwidth=3;
		c.weightx=0.5;
		c.gridx=9;
		c.gridy=1;
		panel.add(txtMomName,c);
		
		c.gridwidth=3;
		c.weightx=0;
		c.gridx=0;
		c.gridy=2;
		panel.add(new JLabel("remark:"),c);
		
		txtRemark = new JTextField();
		c.gridwidth=3;
		c.weightx=0.5;
		c.gridx=3;
		c.gridy=2;
		panel.add(txtRemark,c);
		txtRemark.addActionListener(this);
		
		c.gridwidth=2;
		c.weightx=0;
		c.gridx=6;
		c.gridy=2;
		panel.add(new JLabel("cell death:"),c);
		

		checkBoxCellDies = new JCheckBox();
		c.gridwidth=1;
		c.weightx=0.5;
		c.gridx=8;
		c.gridy=2;
		panel.add(checkBoxCellDies,c);
		checkBoxCellDies.addActionListener(this);
		
		
		c.gridwidth=1;
		c.weightx=0;
		c.gridx=9;
		c.gridy=2;
		panel.add(new JLabel("out:"),c);
		
		checkBoxCellOut = new JCheckBox();
		c.gridwidth=1;
		c.weightx=0.5;
		c.gridx=11;
		c.gridy=2;
		panel.add(checkBoxCellOut,c);
		checkBoxCellOut.addActionListener(this);
		
			
		c.gridwidth=2;
		c.weightx=0;
		c.gridx=0;
		c.gridy=3;
		panel.add(new JLabel("till end:"),c);
		
		checkBoxCellTillEnd = new JCheckBox();
		c.gridwidth=1;
		c.weightx=0.5;
		c.gridx=2;
		c.gridy=3;
		panel.add(checkBoxCellTillEnd,c);
		checkBoxCellTillEnd.addActionListener(this);
		
		c.gridwidth=2;
		c.weightx=0;
		c.gridx=3;
		c.gridy=3;
		panel.add(new JLabel("fuses:"),c);
		
		checkBoxCellFuses = new JCheckBox();
		c.gridwidth=1;
		c.weightx=0.5;
		c.gridx=5;
		c.gridy=3;
		panel.add(checkBoxCellFuses,c);
		checkBoxCellFuses.addActionListener(this);
		
		c.gridwidth=2;
		c.weightx=0;
		c.gridx=6;
		c.gridy=3;
		panel.add(new JLabel("div bad:"),c);
		
		checkBoxCelldivAbnormally = new JCheckBox();
		c.gridwidth=1;
		c.weightx=0.5;
		c.gridx=8;
		c.gridy=3;
		panel.add(checkBoxCelldivAbnormally,c);
		checkBoxCelldivAbnormally.addActionListener(this);
		
		c.gridwidth=2;
		c.weightx=0;
		c.gridx=9;
		c.gridy=3;
		panel.add(new JLabel("sick:"),c);
		
		checkBoxCellLooksSick = new JCheckBox();
		c.gridwidth=1;
		c.weightx=0.5;
		c.gridx=11;
		c.gridy=3;
		panel.add(checkBoxCellLooksSick,c);
		checkBoxCellLooksSick.addActionListener(this);
		
		/*cellsChecksGroup.add(checkBoxCellDies);
		cellsChecksGroup.add(checkBoxCellOut);
		cellsChecksGroup.add(checkBoxCellTillEnd);
		cellsChecksGroup.add(checkBoxCellFuses);
		cellsChecksGroup.add(checkBoxCelldivAbnormally);
		cellsChecksGroup.add(checkBoxCellLooksSick);
		*/
		
		
		butGetCell = new JButton("Get Cell");
		butGetCell.addActionListener(this);
		c.gridwidth=6;
		c.weightx=0;
		c.gridx=0;
		c.gridy=5;
		c.insets=new Insets(3, 3, 3, 3);
		panel.add(butGetCell,c);

		butAddLoc = new JButton("Add roi");		
		butAddLoc.addActionListener(this);
		c.gridwidth=6;
		c.gridx=6;
		c.gridy=5;
		panel.add(butAddLoc,c);
		
		butCellDivision = new JButton("Cell Division");
		butCellDivision.addActionListener(this);
		c.gridwidth=6;
		c.gridx=0;
		c.gridy=6;
		panel.add(butCellDivision,c);	
		
		butContMode = new JToggleButton("Cont Mode");		
		butContMode.addActionListener(this);
		c.gridwidth=6;
		c.gridx=6;
		c.gridy=6;
		panel.add(butContMode,c);
				
		butDeleteCell = new JButton("Delete Cell");
		butDeleteCell.addActionListener(this);	
		c.gridx=0;
		c.gridy=7;
		panel.add(butDeleteCell,c);
		
		butDeleteCellLocations = new JButton("Delete frame");
		butDeleteCellLocations.addActionListener(this);		
		c.gridx=6;
		c.gridy=7;
		panel.add(butDeleteCellLocations,c);
		
		butCellStart = new JButton("Cell Start");
		butCellStart.addActionListener(this);
		c.weightx=0;
		c.gridwidth=6;
		c.gridx=0;
		c.gridy=8;
		panel.add(butCellStart,c);

		butCellEnd = new JButton("Cell End");
		butCellEnd.addActionListener(this);
		c.weightx=0.1;
		c.gridwidth=6;
		c.gridx=6;
		c.gridy=8;
		panel.add(butCellEnd,c);

		butAddMom = new JButton("Add Mother");
		butAddMom.addActionListener(this);		
		c.gridx=0;
		c.gridy=9;
		panel.add(butAddMom,c);		
		
		butRemoveMom = new JButton("Remove Mother");
		butRemoveMom.addActionListener(this);		
		c.gridx=6;
		c.gridy=9;
		panel.add(butRemoveMom,c);
	
		butSwapCells = new JButton("Swap cells");
		butSwapCells.addActionListener(this);
		c.gridx=0;
		c.gridy=10;
		panel.add(butSwapCells,c);
		
		
		
		
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
//		this.origImp=imp.duplicate();		
		stack2frameMap=new TreeMap<Integer, Integer>();
		frame2timeMap=new TreeMap<Integer, Integer>();
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
			this.txtRemark.setText("");
			this.checkBoxCellDies.setSelected(false);			
			this.checkBoxCellOut.setSelected(false);
			this.checkBoxCellTillEnd.setSelected(false);
			this.checkBoxCellFuses.setSelected(false);
			this.checkBoxCelldivAbnormally.setSelected(false);			
			this.checkBoxCellLooksSick.setSelected(false);
		}
		else{			
			this.textCellId.setText(String.valueOf(curCell.getId()));
			this.txtCellName.setText(curCell.getName());
			this.checkBoxCellDies.setSelected(curCell.isDies());			
			this.checkBoxCellOut.setSelected(curCell.isOut());
			this.checkBoxCellTillEnd.setSelected(curCell.isTillEnd());
			this.checkBoxCellFuses.setSelected(curCell.isFuses());
			this.checkBoxCelldivAbnormally.setSelected(curCell.isDivAbnomally());			
			this.checkBoxCellLooksSick.setSelected(curCell.isLooksSick());
			this.txtRemark.setText(curCell.getRemark());
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
		return PathTokens.getCurFrame(imp);
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
		if (e.getSource() == checkBoxCellDies) {
			if(curCell==null){			
				setCellsGui();
				return;
			}
			else{
				SetCellFate(curCell, cellFates.DIE);
				this.setCellsGui();
			}
		}

		if (e.getSource() == checkBoxCellOut) {
			if(curCell==null){
				setCellsGui();
				return;
			}	
			else{
				SetCellFate(curCell, cellFates.OUT);
				this.setCellsGui();
			}
		}

		if (e.getSource() == checkBoxCellTillEnd) {
			if(curCell==null){			
				setCellsGui();
				return;
			}
			else{
				SetCellFate(curCell, cellFates.TILLEND);
				this.setCellsGui();
			}
			
		}

		if (e.getSource() == checkBoxCellFuses) {
			if(curCell==null){			
				setCellsGui();
				return;
			}
			else{
				SetCellFate(curCell, cellFates.FUSE);
				this.setCellsGui();
			}
		}

		if (e.getSource() == checkBoxCelldivAbnormally) {
			if(curCell==null){			
				setCellsGui();
				return;
			}
			else{			
				SetCellFate(curCell, cellFates.DIVABNORMALLY);
				this.setCellsGui();
			}
		}

		if (e.getSource() == checkBoxCellLooksSick) {
			if(curCell==null){			
				setCellsGui();
				return;
			}
			else{
				SetCellFate(curCell, cellFates.SICK);
				this.setCellsGui();
			}
		}

			
		if(e.getSource() == txtRemark){
			if(curCell==null){
				IJ.showMessage("cannot add mother: no current cell");
				return;
			}
			curCell.setRemark(txtRemark.getText());
			this.setCellsGui();
		}
		if (e.getSource() == butGetCell) {
			mouseListening=true;

		}
		if(e.getSource() ==butAddLoc){
			Roi roi =imp.getRoi();
			this.addRoiToCell(roi);
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
			cellDivision();			
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
				IJ.showMessage("cannot delete: no current cell");
				return;
			}
			cellsStruct.remove(curCell);
			monitorSet.remove(curCell);
			this.updateCurCell(null);	
		}
		
		else if(e.getSource()==menuItemDeleteSublineage){
			if(curCell==null){
				IJ.showMessage("cannot delete: no current cell");
				return;
			}
			cellsStruct.removeSublineage(curCell);
			monitorSet.remove(curCell);
			this.updateCurCell(null);	
		}
		
	
		else if (e.getSource()==butDeleteCellLocations){
			if(curCell==null){
				IJ.showMessage("cannot delete: no current cell");
				return;
			}
			GenericDialog gd= new GenericDialog("Delete cell in current frame or from a given frame forward?");
			gd.addMessage("Press yes to delete in current frame\n no to delete from a specific frame forward\n cancel to abort?");
			gd.enableYesNoCancel();
			gd.showDialog();
			if (gd.wasCanceled()){
				return;
			}
			else if (gd.wasOKed()){
				int frame=getCurFrame();
				curCell.deleteLocation(frame);
			}
			else{
				gd=new GenericDialog("Delte cell from frame");
				gd.addNumericField("What frame to start earsing from?", getCurFrame(), 0);
				gd.showDialog();
				Integer frame=(int)gd.getNextNumber();
				Integer slice=this.getSliceOfFrame(frame);
				if(slice==null){
					return;
				}
				boolean removed=curCell.deleteLocationFollowing(frame);	
			}
		}
		 
		
		else if(e.getSource()==butSwapCells){
			GenericDialog gd=new GenericDialog("Swap cells");
			int id1=-1;
			if(curCell!=null){
				id1=curCell.getId();
			}
			gd.addNumericField("Id of first cell?", id1, 4);
			gd.showDialog();
			Integer c1=(int)gd.getNextNumber();
			Cell cell1=cellsStruct.getCell(c1);
			if(cell1==null){
				IJ.showMessage("could not find cell");
				return;
			}
			gd=new GenericDialog("Swap cells");
			gd.addNumericField("Id of second cell?", -1, 0);
			gd.showDialog();
			Integer c2=(int)gd.getNextNumber();	
			Cell cell2=cellsStruct.getCell(c2);
			if(cell2==null){
				IJ.showMessage("could not find cell");
				return;
			}
			gd=new GenericDialog("Delte cell from frame");
			gd.addNumericField("What frame to start earsing from?", getCurFrame(), 4);
			gd.showDialog();
			Integer frame=(int)gd.getNextNumber();			
			cellsStruct.swapCellLocations(cell1, cell2, frame);		
			if(!cellsStruct.contains(curCell)){				
				if(cellsStruct.contains(cell1)){
					curCell=cell1;
				}
				else if(cellsStruct.contains(cell2)){
					curCell=cell2;
				}
			}
			this.updateCurCell(curCell);
			
			
		}

		else if(e.getSource()==butAddSis){
			IJ.showMessage("Not set yet go to mother....");
		}

		else if(e.getSource()==butContMode){
			contMode();

		}	

		else if(e.getSource()==menuItemSaveStruct){
			this.saveCellStructure();				 
		}
		else if(e.getSource()==menuItemLoadStruct){
			cellsStruct=this.loadCellsStruct();
		}
		else if (e.getSource()==menuItemExportStruct){
			String path=getSavePath();				
			CellsWriter.writeStructure(path, cellsStruct);
		}

		else if(e.getSource()==menuItemAdd2Set){
			this.addToMonitorSet();
		}

		else if(e.getSource()==menuItemRemoveFromSet){
			this.removeFromMonitorSet();
		}
		else if(e.getSource()==menuItemClearSet){
			this.clearMonitorSet();
		}
		
		else if(e.getSource()==menuItemDisplayMoms){
			if(curCell==null){
				IJ.showMessage("no cell is selected");
			}
			else{
				String res="mother ids:";
				Set<Cell> moms=curCell.getMothers();
				if(moms!=null){
					for(Cell mom:moms){
						res+=" "+mom.getId();
					}
				}
				IJ.showMessage(res);
			}
		}
		else if(e.getSource()==menuItemDisplayDaughters){
			if(curCell==null){
				IJ.showMessage("no cell is selected");
			}
			else{
				String res="daughters ids:";
				Set<Cell> daughts=curCell.getDaughters();
				if(daughts!=null){
					for(Cell d:daughts){
						res+=" "+d.getId();
					}
				}
				IJ.showMessage(res);
			}
		}
		
		else if(e.getSource()==menuItemShowSet){
			String res="cells in set are: ";
			for(Cell cell: monitorSet){
				res+=cell.getId()+" ";
			}
			IJ.showMessage(res);
			
			
		}
		else if(e.getSource()==menuItemGetTimes){
			this.createFrameTimeMapping();
		}

		else if(e.getSource()==menuItemExtractFluo){
			extractFluo();
		}
		else if(e.getSource()==menuItemExtractArea){
			extractArea();
		}
		else if(e.getSource()==menuItemRemoveProperty){
			removePoperty();
		}
		else if(e.getSource()==menuItemDisplayProperties){
			displayProperties();
		}
		
		
		else if(e.getSource()==menuItemChangeWait){
			GenericDialog gd=new GenericDialog("Change ContMode wait time");
			gd.addNumericField("Enter new wait in millisecond?", waitmill, 4);
			gd.showDialog();
			waitmill=(int)gd.getNextNumber();		
		}
		else if(e.getSource()==menuItemChangeAllowedDist){
			GenericDialog gd=new GenericDialog("Change allowed distance of a cell between consecutive frames");
			gd.addNumericField("Enter new distance:", allowedDist , 4);
			gd.showDialog();
			allowedDist=(int)gd.getNextNumber();		
		}
		else if(e.getSource()==menuItemPaprikaTrack){
			if(!(cellsStruct instanceof PropertiesCells)){				
				IJ.showMessage("no area property in the current structure aborting");
			}
			Cell cell=cellsStruct.getCell(1);
			
			PaprikaTrack paptrack=new PaprikaTrack(imp, (PropertiesCells)cellsStruct);
			cellsStruct=paptrack.track(monitorSet);
		}
		
		drawFrame();
	}

	/**
	 * 
	 */
	private void cellDivision() {
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

	/**
	 * 
	 */
	private void contMode() {
		if(butContMode.isSelected()){
			Thread queryThread = new Thread() {
				public void run() {
					if(monitorSet.isEmpty()){ //monitor curCell
						wandTrackCurCell();
					}
					else{
						wandTrackSet();
					}
				}
			};
			queryThread.start();
			
		}
	}
	public void itemStateChanged(ItemEvent e){
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
				Roi mroi=(Roi) curCell.getLocationInFrame(frame).getRoi().clone();
				if(this.curCell==curCell){
					mroi.setStrokeColor(Color.MAGENTA);
				}
				else if(monitorSet.contains(curCell)){
					mroi.setStrokeColor(Color.CYAN);
				}
				else{
					mroi.setStrokeColor(Color.GREEN);
				}
				mroi.setName(curCell.getName());				
				ov.add(mroi);
			}
		}

		if(menuItemDisplayNames.isSelected()){
			ov.setLabelFont(font);
			ov.setLabelColor(Color.GREEN);
			ov.drawNames(true);
		}	
		if(!menuItemDisplayRois.isSelected()){
			imp.setOverlay(null);
		}
		else{
			imp.setOverlay(ov);
		}
		
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
		String path=getSavePath();
		this.saveCellsStruct(path);
	}
	
	public String getSavePath(){
		SaveDialog sd = new SaveDialog("Save cell structure", "cell Struct", "");
		String name = sd.getFileName();
		if (name==null){
			return null;  
		}
		String directory = sd.getDirectory();         
		String path = directory+name;
		return path;
	}


	public Cells loadCellsStruct(){
		String path=getFilePathDlg(1);
		return loadCellsStruct(path);
	}


	private static String getFilePathDlg(int type){
		OpenDialog od = new OpenDialog("Choose a cells structure file (note this will erase current structure)", null);  
		String dir = od.getDirectory();  
		if (null == dir){ 
			return null; // dialog was canceled  
		}
		dir = dir.replace('\\', '/'); // Windows safe  
		if (!dir.endsWith("/")){
			dir += "/";  
		}
		String filename=od.getFileName();
		if(type==1){
			return dir+filename;
		}
		else if(type==2){
			return dir;
		}
		else if(type==3){
			return filename;
		}
		else{
			return null;
		}
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
		else{// check if there's a current or previous mark of this cell if so and its far ask the user what to do
			PolyProperty polyprop=curCell.getLocationInFrame(frame);
			if(polyprop==null && slice>1){//look at the previous frame
				filename=stack.getSliceLabel(slice-1);
				pt=new PathTokens(filename);
				int prevframe=pt.frame;
				polyprop=curCell.getLocationInFrame(prevframe);
			}
			if(polyprop!=null){
				double dist=Math.sqrt(Math.pow((polyprop.getRoi().getBounds().x-roi.getBounds().x),2)+Math.pow((polyprop.getRoi().getBounds().y-roi.getBounds().y),2));
				if(dist>allowedDist){
					GenericDialog gd = new GenericDialog("Found ROI too far");
					gd.addMessage("press yes to counitnue, no for starting a new cell, cancel to abort");
					gd.enableYesNoCancel();
					gd.showDialog();
					if (gd.wasCanceled()){
						return;
					}			 
					else if(!gd.wasOKed()){
						Cell newCell= cellsStruct.addNewCell();	
						updateCurCell(newCell);
					}
				}
			}
			
			
		}
		//check if there's a mother of this cell exists in this frame or later
		Set<Cell> moms=curCell.getMothers();		
		if(moms!=null){
			Iterator<Cell> miter=moms.iterator();
			while(miter.hasNext()){
				Cell mom=miter.next();
				int maxFrame=cellsStruct.getCl().getMaxFrame(mom);
				if(maxFrame>=frame){
					IJ.showMessage("a mother of this cell exists in a later frame");
					return;				

				}
			}
		}
		//check if there's a daughter of this cell exists in this frame or earlier
		Set<Cell> daughts=curCell.getDaughters();		
		if(daughts!=null){
			Iterator<Cell> diter=daughts.iterator();
			while(diter.hasNext()){
				Cell daught=diter.next();
				int minFrame=cellsStruct.getCl().getMinFrame(daught);
				if(minFrame<=frame){
					IJ.showMessage("a daughter of this cell exists in a previous frame");
					return;				

				}
			}
		}
		curCell.addLocation(frame, roi);

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
		else if(key.getKeyChar()=='g'){
			mouseListening=true;
		}	
		else if(key.getKeyChar()=='l'){
			this.addToMonitorSet();
		}	
		else if(key.getKeyChar()=='r'){
			this.removeFromMonitorSet();
		}	
		else if(key.getKeyChar()=='c'){
			if(butContMode.isSelected()){
				butContMode.setSelected(false);
			}
			else{
				butContMode.setSelected(true);
				this.contMode();
			}
		}
		else if(key.getKeyChar()=='d'){
			this.cellDivision();
		}
		else if(key.getKeyChar()=='m'){
			if(curCell==null){
				return;
			}
			Set<Cell> moms= curCell.getMothers();
			if(moms==null ||moms.isEmpty()){
				return;
			}
			curCell=moms.iterator().next();			
		}
		
		drawFrame();
		this.setCellsGui();
	}

	void wandTrackCurCell(){		
		Roi roi= imp.getRoi();
		if(roi==null){
			butContMode.setSelected(false);
			return;			
		}
		this.addRoiToCell(roi);
		if(curCell==null){
			IJ.showMessage("cannot track: no current cell");
			return;
		}
		int prevFrame=this.getCurFrame();
		boolean gotNext=nextSlice();

		while(butContMode.isSelected() & gotNext){	
			gotNext=wandTrack(curCell,prevFrame);	
			if(!gotNext){
				butContMode.setSelected(false);
				drawFrame();
				this.setCellsGui();
				return;			
			}
			drawFrame();
			this.setCellsGui();
			try {
				Thread.sleep(waitmill);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			prevFrame=this.getCurFrame();
			gotNext=nextSlice();
		}
		butContMode.setSelected(false);
		drawFrame();
		this.setCellsGui();

	}		

	void wandTrackSet(){
		Set<Cell> missedCells=new HashSet<Cell>();
		int prevFrame=this.getCurFrame();
		boolean gotNext=nextSlice();		

		while(butContMode.isSelected()&& gotNext && missedCells.isEmpty()){
			Iterator<Cell> citer=monitorSet.iterator();
			while(citer.hasNext()){
				Cell curCell=citer.next();			
				if(!wandTrack(curCell,prevFrame)){
					missedCells.add(curCell);
				}				
			}
			drawFrame();
			this.setCellsGui();
			try {
				Thread.sleep(waitmill);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!missedCells.isEmpty()){
				butContMode.setSelected(false);
				drawFrame();
				this.curCell=missedCells.iterator().next();
				this.setCellsGui();
				return;	
			}
			prevFrame=this.getCurFrame();
			gotNext=nextSlice();			
		}
		butContMode.setSelected(false);
	
		drawFrame();
		this.setCellsGui();
	}

	/*
	 * wandTrack the given cell in the current frame according to the ROI of the cell in prevFrame
	 */
	private boolean wandTrack(Cell cell, int prevFrame){
		PolyProperty prevPoly=cell.getLocationInFrame(prevFrame);
		if(prevPoly==null){
			return false;
		}
		Roi roi = prevPoly.getRoi();				
		double tolerance=0;

		//TODO - check about the offScreen if needed here...
		int x =(int) roi.getBounds().getCenterX();
		int y =(int) roi.getBounds().getCenterY();
		int dots=IJ.doWand(x, y, tolerance,  "8-connected" ); //looks like this returns the number of points
		if(dots==0){		
			return false;
		}
		curCell=cell;
		//check if this roi is not too far from the previous
		Roi prevRoi=roi;
		roi= imp.getRoi();
		double dist=Math.sqrt(Math.pow((prevRoi.getBounds().x-roi.getBounds().x),2)+Math.pow((prevRoi.getBounds().y-roi.getBounds().y),2));
		if(dist>allowedDist){
			GenericDialog gd = new GenericDialog("YesNoCancel");
			gd.addMessage("Found ROI too far- counitnue?");
			gd.enableYesNoCancel();
			gd.showDialog();
			if (gd.wasCanceled()||!gd.wasOKed()){
				return false;
			}			 
		}
		//check if this roi is much smaller than the last frame- indicating a problem with threshold or a division
		double prevArea=prevRoi.getBounds().getHeight()*prevRoi.getBounds().getWidth();
		double roiArea=roi.getBounds().getHeight()*roi.getBounds().getWidth();
		if(roiArea/prevArea<consecAreaRatio){
			return false;
		}
		
		//check if this roi is not allready overlapping with an existing cell		
		int frame=PathTokens.getCurFrame(imp);		
		Cell overlaping=cellsStruct.overlapingLocation(roi, frame, overlapingRatio);
		if(overlaping!=null){
			if(cell.getId()!=overlaping.getId())
				return false;
		}
		this.addRoiToCell(roi);
		return true;
	}

	 
 
	
	
	
	public void createFrameTimeMapping(){
		PathTokens pt=new PathTokens(imp.getImageStack(),1);
		String path=pt.getPre()+pt.getSite()+"_";
		String dir=JoyDivision_.getFilePathDlg(2);
		SortedSet<Integer> sortedFrameNums=new TreeSet<Integer>();
		sortedFrameNums.addAll(this.stack2frameMap.values());
		Iterator<Integer> fiter=sortedFrameNums.iterator();
		long init=-1;
		while(fiter.hasNext()){
			int frameNum=fiter.next();
			String fullpath=dir+path+frameNum+".tif";
			File file = new File(fullpath);			
			long modifiedTime = file.lastModified();
			if(init<0){
				init=modifiedTime;
			}
			int totTime=(int)(modifiedTime-init)/60000;
			frame2timeMap.put(frameNum, totTime);
			IJ.showMessage("file: "+fullpath+" created at: "+totTime);

		}
	}

	/**
	 * The imagePlus opened is taken and for each frame the relative roi from each cell is taken 
	 * for which the mean values in the relative location in the current ImagePlus is calculated and added 
	 */
	public void extractFluo(){
		GenericDialog gd = new GenericDialog("YesNoCancel");
		gd.addMessage("Is the current image contains the fluorescence wished to be extracted?");
		gd.enableYesNoCancel();
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		else if (!gd.wasOKed())
			return;
		gd=new GenericDialog("What is the name of the fluoresence");
		gd.addStringField("fluoName", "GFP");	
		gd.showDialog();
		TextField txt=(TextField)gd.getStringFields().firstElement();
		String fluoname=txt.getText();		

		if(!(cellsStruct instanceof PropertiesCells)){
			PropertiesCells newCellsStruct=new PropertiesCells(cellsStruct);
			cellsStruct=newCellsStruct;			
		}
		else{
			Collection<String> props=((PropertiesCells) cellsStruct).getPropertyNames();
			if(props!=null){
				if(props.contains(fluoname)){
					IJ.showMessage("Structure already contains Property "+fluoname);
					return;
				}
			}
		}
		//now we are ready to start:
		((PropertiesCells) cellsStruct).addProperty(fluoname);			
		//we will go over all frames and update their ROI to be PolyProperty and hold the correct value-
		//here- we calculate the mean fluo of the each cell in each frame
		ImagePlus fimp=IJ.getImage();
		for (int curSlice=1; curSlice<=fimp.getStackSize();curSlice++){
			fimp.setSlice(curSlice);
			int curFrame=PathTokens.getCurFrame(fimp);
			Set<Cell> frameCells=cellsStruct.getCellsInFrame(curFrame);
			if(frameCells!=null){
				Iterator<Cell> citer=frameCells.iterator();
				while(citer.hasNext()){
					Cell curCell=citer.next();
					PolyProperty curRoi=curCell.getLocationInFrame(curFrame);								
					//find the mean of this Roi
					fimp.setRoi(curRoi.getRoi());
					ImageStatistics stats= ImageStatistics.getStatistics(fimp.getProcessor(), Measurements.MEAN,fimp.getCalibration());
					PropertiesCells propCells=(PropertiesCells)cellsStruct;
					int propId=propCells.getPropertyId(fluoname);
					curRoi.setProperty(propId, stats.mean);
					curCell.addLocation(curFrame, curRoi);				
				}
			}   
		}		
	}

	
	/**
	 * The imagePlus opened is taken and for each frame the relative roi from each cell is taken 
	 * for which the mean values in the relative location in the current ImagePlus is calculated and added 
	 */
	public void extractArea(){
		GenericDialog gd = new GenericDialog("YesNoCancel");
		gd.addMessage("Is the current image contains the needed area marks?");
		gd.enableYesNoCancel();
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		else if (!gd.wasOKed())
			return;
	
		String propName="Area";		

		if(!(cellsStruct instanceof PropertiesCells)){
			PropertiesCells newCellsStruct=new PropertiesCells(cellsStruct);
			cellsStruct=newCellsStruct;			
		}
		else{
			Collection<String> props=((PropertiesCells) cellsStruct).getPropertyNames();
			if(props!=null){
				if(props.contains(propName)){
					IJ.showMessage("Structure already contains Property "+propName);
					return;
				}
			}
		}
		//now we are ready to start:
		((PropertiesCells) cellsStruct).addProperty(propName);			
		//we will go over all frames and update their ROI to be PolyProperty and hold the correct value-
		//here- we calculate the mean fluo of the each cell in each frame
		ImagePlus fimp=IJ.getImage();
		for (int curSlice=1; curSlice<=fimp.getStackSize();curSlice++){
			fimp.setSlice(curSlice);
			int curFrame=PathTokens.getCurFrame(fimp);
			Set<Cell> frameCells=cellsStruct.getCellsInFrame(curFrame);
			if(frameCells!=null){
				Iterator<Cell> citer=frameCells.iterator();
				while(citer.hasNext()){
					Cell curCell=citer.next();
					PolyProperty curRoi=curCell.getLocationInFrame(curFrame);								
					//find the mean of this Roi
					fimp.setRoi(curRoi.getRoi());
					ImageStatistics stats= ImageStatistics.getStatistics(fimp.getProcessor(), Measurements.AREA	,fimp.getCalibration());
					PropertiesCells propCells=(PropertiesCells)cellsStruct;
					int propId=propCells.getPropertyId(propName);
					curRoi.setProperty(propId, stats.area);
					curCell.addLocation(curFrame, curRoi);				
				}
			}   
		}		
	}

	
	/*
	 * Remove the properties with the given name from the cellstruct
	 */
	public void removePoperty(){
		if(!(cellsStruct instanceof PropertiesCells)){
			IJ.showMessage("no properties in current structure");
			return;
		}
		GenericDialog gd = new GenericDialog("Remove property");
		gd=new GenericDialog("What is the name of the property?");
		gd.addStringField("property name:", "GFP");	
		gd.showDialog();
		TextField txt=(TextField)gd.getStringFields().firstElement();
		String propName=txt.getText();	
		PropertiesCells cells=(PropertiesCells)cellsStruct;
		int propId=cells.getPropertyId(propName);
		if(propId==-1){
			IJ.showMessage("no property "+propName+" in current structure");
			return;
		}
		for(Cell cell: cells.values()){
			if(cell.getFrames()!=null){			
				for(Integer frame: cell.getFrames()){
					PolyProperty poly=cell.getLocationInFrame(frame);
					poly.removeProperty(propId);
				}
			}
		}
		cells.propId2NameMapping.remove(propId);		
	}
	
	
	public void displayProperties(){
		if(!(cellsStruct instanceof PropertiesCells)){
			IJ.showMessage("no properties in current structure");
			return;
		}
		PropertiesCells cells=(PropertiesCells)cellsStruct;
		String props="";
		for(int propId: cells.propId2NameMapping.keySet()){
			props=propId+" "+cells.propId2NameMapping.get(propId)+"\n";			
		}
		IJ.showMessage(props);		
	}
	
	
	private void addToMonitorSet(){
		if(curCell!=null){
			monitorSet.add(curCell);
		}
	}
	private void removeFromMonitorSet(){
		monitorSet.remove(curCell);
	}
	private void clearMonitorSet(){
		monitorSet.clear();
	}

	private boolean nextSlice(){
		boolean res=false;
		int curSlice=imp.getCurrentSlice();
		if(curSlice<imp.getImageStackSize()){
			imp.setSlice(++curSlice);
			res=true;
		}
		return res;
	}
	
	private void SetCellFate(Cell cell,cellFates fate){
		if(fate==cellFates.DIE){
			if(cell.isDies()){
				cell.setDies(false);
				checkBoxCellDies.setSelected(false);
			}
			else{
				//SetCellNoFate(cell);
				cell.setDies(true);
				checkBoxCellDies.setSelected(true);
			}
			
		}
		
		if(fate==cellFates.DIVABNORMALLY){
			if(cell.isDivAbnomally()){
				cell.setDivAbnormally(false);
				checkBoxCelldivAbnormally.setSelected(false);
			}
			else{
				//SetCellNoFate(cell);
				cell.setDivAbnormally(true);
				checkBoxCelldivAbnormally.setSelected(true);
			}
		}
			if(fate==cellFates.FUSE){
				if(cell.isFuses()){
					cell.setFuses(false);
					checkBoxCellFuses.setSelected(false);
				}
				else{
					//SetCellNoFate(cell);
					cell.setFuses(true);
					checkBoxCellFuses.setSelected(true);
				}
				
			}
			if(fate==cellFates.OUT){
				if(cell.isOut()){
					cell.setOut(false);
					checkBoxCellOut.setSelected(false);
				}
				else{
					//SetCellNoFate(cell);
					cell.setOut(true);
					checkBoxCellOut.setSelected(true);
				}
				
			}
			if(fate==cellFates.SICK){
				if(cell.isLooksSick()){
					cell.setLooksSick(false);
					checkBoxCellLooksSick.setSelected(false);
				}
				else{
					//SetCellNoFate(cell);
					cell.setLooksSick(true);
					checkBoxCellLooksSick.setSelected(true);
				}
				
			}	
			
			if(fate==cellFates.TILLEND){
				if(cell.isTillEnd()){
					cell.setTillEnd(false);
					checkBoxCellTillEnd.setSelected(false);
				}
				else{
					//SetCellNoFate(cell);
					cell.setTillEnd(true);
					checkBoxCellTillEnd.setSelected(true);
				}
				
			}
		
		
		
	}
	
	private void SetCellNoFate(Cell cell){
		cell.setDies(false);
		checkBoxCellDies.setSelected(false);
		cell.setDivAbnormally(false);
		checkBoxCelldivAbnormally.setSelected(false);
		cell.setFuses(false);
		checkBoxCellFuses.setSelected(false);
		cell.setLooksSick(false);
		checkBoxCellLooksSick.setSelected(false);
		cell.setOut(false);
		checkBoxCellOut.setSelected(false);
		cell.setTillEnd(false);
		checkBoxCellTillEnd.setSelected(false);
	}

	@Override
	public void keyPressed(KeyEvent key) {
	}



	@Override
	public void keyReleased(KeyEvent key) {


	}
	@Override
	public void imageOpened(ImagePlus imp) {
	}

	@Override
	public void imageClosed(ImagePlus imp) {
		// TODO Auto-generated method stub

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
