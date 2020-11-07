package oo_homework;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class UML_editor extends JFrame{
	
	private JPanel  buttonPanel, diagramPanel;
	private JToggleButton buttonSelect, buttonAsso, buttonGen, buttonCom, buttonClass, buttonCase;
	private JLabel statusBar;
	private JMenu fileMenu; 
	private JMenu editMenu;
	private JMenuItem changeName;
	private JMenuItem Group;
	private JMenuItem unGroup;
	private JMenuBar menuBar;

	// graphic object
	private int drawCount = 0;
	private final int OBJECT_COUNT = 1000;
	UMLobject umlObj[] = new UMLobject[OBJECT_COUNT];
	private int lineCount = 0;
	UML_lines umlLine[] = new UML_lines[OBJECT_COUNT];
	
	private int selectedMode = 0;
	private final int TYPE_CLASS = 0;
	private final int TYPE_CASE  = 1;
	
	private final int OBJECT_WIDTH = 120;
	private final int OBJECT_HEIGHT = 100; 
	private final int PORT_WIDTH = 6; 
	
	// mode type
	private final int STILL_STATUS = 0;
	private final int SELECT_ACTIVATE = 1;
	private final int DRAW_LINE_ACTIVATE = 2;
	private boolean assoActivate = false;
	private boolean genActivate  = false;
	private boolean comActivate  = false;
	private final int CLASS_ACTIVATE = 3;
	private final int CASE_ACTIVATE  = 4;
	
	// record the point in select mode
	private int orginPointX = 0;
	private int orginPointY = 0;
	
	// Initial state of the dragged object
	private int tmp_PointXmin[] = new int[OBJECT_COUNT];
	private int tmp_PointYmin[] = new int[OBJECT_COUNT];
	private int tmp_PointXmax[] = new int[OBJECT_COUNT];
	private int tmp_PointYmax[] = new int[OBJECT_COUNT];
	private boolean isDragObj = false;
	
	// select group objects
	private boolean isSelectGroup = false;
	private int pressedPointX = 0;
	private int pressedPointY = 0;
	private int f_height = 0;
	private int f_width  = 0;
	private int lowerY  = 0;
	private int upperY  = 0;
	private int leftX   = 0;
	private int rightX  = 0;
	
	// drag the line
	private int dragLinePointX;
	private int dragLinePointY;
	private int pressLinePointX;
	private int pressLinePointY;
	private int pickObjNum = -1;
	private int tmp_line_x1[] = new int[OBJECT_COUNT];
	private int tmp_line_y1[] = new int[OBJECT_COUNT]; 
	private int tmp_line_x2[] = new int[OBJECT_COUNT];
	private int tmp_line_y2[] = new int[OBJECT_COUNT];
	
	private final int POSTION_DOWN  = 1;
	private final int POSTION_UP    = 2;
	private final int POSTION_LEFT  = 3;
	private final int POSTION_RIGHT = 4;
	
	private final int TYPE_ASSO = 1;
	private final int TYPE_GEN  = 2;
	private final int TYPE_COM  = 3;
	
	// composite object
	UMLcomObj comObj = new UMLcomObj();
	private int subObjNum = 0;
	private int tierNum = 0;
	// private boolean isGrouping = false;
	
	public UML_editor(){
		super( "UML Editor");
		
		for(int i=0; i<OBJECT_COUNT; i++){
			umlObj[i] = new UMLobject();
			umlLine[i] = new UML_lines();
			comObj.objArray[i] = -1;
			comObj.objTier[i] = -1;
		}
	
		menuBar = new JMenuBar();		
		setJMenuBar(menuBar);
		
		fileMenu = new JMenu("File");
		editMenu = new JMenu("Edit");
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		
		changeName = new JMenuItem("Change Object Name");
		Group   = new JMenuItem("Group Objects");
		unGroup = new JMenuItem("UnGroup Objects");
		editMenu.add(changeName);
		editMenu.add(Group);
		editMenu.add(unGroup);
		
		changeName.addActionListener( 
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					boolean allSelected = false;
					for(int i=0; i<=drawCount; i++){ 
						if(umlObj[i].isSelected){
							String renameObj =
									(String)JOptionPane.showInputDialog( null, "Please input the new name of "+ umlObj[i].objName, "Rename your object",
																		JOptionPane.PLAIN_MESSAGE, null, null,"object");	
							if(renameObj != null){
								umlObj[i].objName = renameObj;
								repaint();
							}
							allSelected = true;
						}
					}
					if(!allSelected){
						JOptionPane.showMessageDialog(null, "Please select an object.", "Info",JOptionPane.INFORMATION_MESSAGE); 
						allSelected = false;
					}
				}
			}
		);
		
		Group.addActionListener( 
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						boolean isAdded = false;
						boolean nothingSelected = true;
						for(int i=0; i<=drawCount; i++){ 
							if(umlObj[i].isSelected){
								for(int j=0; j<=subObjNum; j++){
									if( comObj.objArray[j] == i){
										isAdded = true;
									}
								}
								if(!isAdded){
									comObj.objArray[subObjNum] = i;
									comObj.objTier[subObjNum]  = tierNum;									
									subObjNum++;
									nothingSelected = false;
								}
							}							
						}
						if(nothingSelected){
							JOptionPane.showMessageDialog(null, "Please select at least a basic object.", "Info",JOptionPane.INFORMATION_MESSAGE); 
						}
						else{
							tierNum ++;
							
							String s = "";
							for(int i=0; i<subObjNum; i++){
								s += umlObj[comObj.objArray[i]].objName+ ", ";
							}
							statusBar.setText(String.format( "%s are composed", s  ));
						}
					}
				}
		);
		
		unGroup.addActionListener( 
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						String s = "";
						int remove_num = 0;
						for(int i=0; i<subObjNum; i++){
							if( comObj.objTier[i] == tierNum-1){
								s += umlObj[comObj.objArray[i]].objName+ ", ";
								comObj.objArray[i] = -1;
								comObj.objTier[i] = -1;	
								remove_num++;
							}
						}	
						if(remove_num == 0){
							statusBar.setText(String.format( "No composite object!" ));
						}
						else{
							tierNum --;
							subObjNum -= remove_num;
							statusBar.setText(String.format( "%sare decomposed", s  ));
						}
					}
				}
		);
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout( 6, 1, 20, 20 ));		
		
		buttonSelect = new JToggleButton("Select");		
		buttonAsso 	 = new JToggleButton("Association Line");
		buttonGen 	 = new JToggleButton("Generalization Line");		
		buttonCom 	 = new JToggleButton("Composition Line");
		buttonClass  = new JToggleButton("Class");
		buttonCase   = new JToggleButton("Use Case");
		
		buttonPanel.add( buttonSelect );
		buttonPanel.add( buttonAsso );
		buttonPanel.add( buttonGen );
		buttonPanel.add( buttonCom );
		buttonPanel.add( buttonClass );
		buttonPanel.add( buttonCase );
		
		diagramPanel = new JPanel(){
			public void paintComponent(Graphics g){	
				super.paintComponent(g);							
				for(int i=0; i<drawCount; i++){
					if( umlObj[i].type == TYPE_CLASS ){				
						//draw object
						g.setColor( new Color( 240, 240, 240) );
						g.fill3DRect(umlObj[i].xmin, umlObj[i].ymin, OBJECT_WIDTH, OBJECT_HEIGHT, true);
						for(int j =0; j<subObjNum; j++){
							if(comObj.objArray[j] == i){
								g.setColor( new Color( 255, 253, 67) );
								for(int k=0; k<=5; k++)
									g.drawRect( umlObj[i].xmin-k, umlObj[i].ymin-k, OBJECT_WIDTH+2*k, OBJECT_HEIGHT+2*k);
							}
						}
						g.setColor( new Color( 0, 0, 0) );
						g.draw3DRect( umlObj[i].xmin, umlObj[i].ymin, OBJECT_WIDTH, OBJECT_HEIGHT/3, true);
						g.draw3DRect( umlObj[i].xmin, umlObj[i].ymin + OBJECT_HEIGHT/3, OBJECT_WIDTH, OBJECT_HEIGHT/3, true);
						g.draw3DRect( umlObj[i].xmin, umlObj[i].ymin + OBJECT_HEIGHT*2/3, OBJECT_WIDTH, OBJECT_HEIGHT/3, true);
						if(umlObj[i].objName != null){
							g.setColor( new Color( 10, 20, 129) );
							g.setFont( new Font( "SansSerif", Font.BOLD, 20 ) );
							g.drawString( umlObj[i].objName, umlObj[i].xmin+25, (umlObj[i].ymin+umlObj[i].ymax)/2 - OBJECT_HEIGHT/3 + 8 );
						}
					}				
					else if( umlObj[i].type == TYPE_CASE ){						
						//draw object
						g.setColor( new Color( 240, 240, 240) );
						g.fillOval(umlObj[i].xmin, umlObj[i].ymin, OBJECT_WIDTH, OBJECT_HEIGHT);
						for(int j =0; j<subObjNum; j++){
							if(comObj.objArray[j] == i){
								g.setColor( new Color( 255, 253, 67) );
								for(int k=0; k<=5; k++)
									g.drawOval( umlObj[i].xmin-k, umlObj[i].ymin-k, OBJECT_WIDTH+2*k, OBJECT_HEIGHT+2*k);
							}
						}
						g.setColor( new Color( 0, 0, 0) );
						g.drawOval(umlObj[i].xmin, umlObj[i].ymin, OBJECT_WIDTH, OBJECT_HEIGHT);						
						if(umlObj[i].objName != null){
							g.setColor( new Color( 10, 20, 129) );
							g.setFont( new Font( "SansSerif", Font.BOLD, 20 ) );
							g.drawString( umlObj[i].objName, umlObj[i].xmin+25, (umlObj[i].ymin+umlObj[i].ymax)/2+5);
						}
					}	
					
					//draw connection ports
					if( umlObj[i].isSelected ){
						int upPointx = (umlObj[i].xmax + umlObj[i].xmin)/2 - PORT_WIDTH/2;
						int upPointy = umlObj[i].ymin - PORT_WIDTH/2;
						g.setColor( new Color( 235, 160, 200) );
						g.fillRect(upPointx, upPointy, PORT_WIDTH, PORT_WIDTH);
						
						int downPointx = (umlObj[i].xmax + umlObj[i].xmin)/2 - PORT_WIDTH/2;
						int downPointy = umlObj[i].ymax - PORT_WIDTH/2;
						g.setColor( new Color( 235, 160, 200) );
						g.fillRect(downPointx, downPointy, PORT_WIDTH, PORT_WIDTH);
						
						int leftPointx =  umlObj[i].xmin - PORT_WIDTH/2;
						int leftPointy = (umlObj[i].ymax + umlObj[i].ymin)/2 - PORT_WIDTH/2;
						g.setColor( new Color( 235, 160, 200) );
						g.fillRect(leftPointx, leftPointy, PORT_WIDTH, PORT_WIDTH);		
						
						int rightPointx =  umlObj[i].xmax - PORT_WIDTH/2;
						int rightPointy = (umlObj[i].ymax + umlObj[i].ymin)/2 - PORT_WIDTH/2;
						g.setColor( new Color( 235, 160, 200) );
						g.fillRect(rightPointx, rightPointy, PORT_WIDTH, PORT_WIDTH);	
					}
				}
				if( isSelectGroup ){
					g.setColor( new Color( 175, 114, 194) );
					g.drawRect( leftX, lowerY, f_width, f_height);	
				}
				
				for(int i=0; i<=lineCount; i++){
					if( umlLine[i].isDraggingLine ){
						g.setColor( new Color( 0, 0, 0) );
						g.drawLine(  pressLinePointX,  pressLinePointY, dragLinePointX, dragLinePointY );
					}
					else if( umlLine[i].genLine ){
						if( umlLine[i].lineType == TYPE_ASSO ){
							g.setColor( new Color( 153, 88, 176) );	
						}
						else if( umlLine[i].lineType == TYPE_GEN ){
							g.setColor( new Color( 71, 87, 200) );
							Polygon triangle = new Polygon();
							triangle.addPoint(umlLine[i].linePortX2, umlLine[i].linePortY2 );
							if( umlLine[i].portPos == POSTION_DOWN){
								triangle.addPoint(umlLine[i].linePortX2 + 5, umlLine[i].linePortY2 + 10 );	
								triangle.addPoint(umlLine[i].linePortX2 - 5, umlLine[i].linePortY2 + 10 );	
							}
							else if( umlLine[i].portPos == POSTION_UP ){
								triangle.addPoint(umlLine[i].linePortX2 + 5, umlLine[i].linePortY2 - 10 );	
								triangle.addPoint(umlLine[i].linePortX2 - 5, umlLine[i].linePortY2 - 10 );				
							}
							else if( umlLine[i].portPos == POSTION_LEFT ){
								triangle.addPoint(umlLine[i].linePortX2 - 10, umlLine[i].linePortY2 + 5 );	
								triangle.addPoint(umlLine[i].linePortX2 - 10, umlLine[i].linePortY2 - 5 );
							}
							else if( umlLine[i].portPos == POSTION_RIGHT ){
								triangle.addPoint(umlLine[i].linePortX2 + 10, umlLine[i].linePortY2 + 5 );	
								triangle.addPoint(umlLine[i].linePortX2 + 10, umlLine[i].linePortY2 - 5 );
							}
							g.fillPolygon( triangle );
						}
						else if( umlLine[i].lineType == TYPE_COM ){
							g.setColor( new Color( 66, 219, 63) );	
							Polygon rhombus = new Polygon();
							rhombus.addPoint(umlLine[i].linePortX2, umlLine[i].linePortY2 );
							if( umlLine[i].portPos == POSTION_DOWN){				
								rhombus.addPoint(umlLine[i].linePortX2 - 5, umlLine[i].linePortY2 + 7 );
								rhombus.addPoint(umlLine[i].linePortX2 , umlLine[i].linePortY2 + 14 );
								rhombus.addPoint(umlLine[i].linePortX2 + 5, umlLine[i].linePortY2 + 7 );
							}
							else if( umlLine[i].portPos == POSTION_UP ){
								rhombus.addPoint(umlLine[i].linePortX2 + 5, umlLine[i].linePortY2 - 7 );	
								rhombus.addPoint(umlLine[i].linePortX2, umlLine[i].linePortY2 - 14 );
								rhombus.addPoint(umlLine[i].linePortX2 - 5, umlLine[i].linePortY2 - 7 );
							}
							else if( umlLine[i].portPos == POSTION_LEFT ){
								rhombus.addPoint(umlLine[i].linePortX2 - 7, umlLine[i].linePortY2 - 5  );	
								rhombus.addPoint(umlLine[i].linePortX2 - 14, umlLine[i].linePortY2 );
								rhombus.addPoint(umlLine[i].linePortX2 - 7, umlLine[i].linePortY2 + 5 );
							}
							else if( umlLine[i].portPos == POSTION_RIGHT ){
								rhombus.addPoint(umlLine[i].linePortX2 + 7, umlLine[i].linePortY2 - 5 );	
								rhombus.addPoint(umlLine[i].linePortX2 + 14, umlLine[i].linePortY2 );
								rhombus.addPoint(umlLine[i].linePortX2 + 7, umlLine[i].linePortY2 + 5 );
							}	
							g.fillPolygon( rhombus );
						}
						g.drawLine(  umlLine[i].linePortX1,  umlLine[i].linePortY1, umlLine[i].linePortX2, umlLine[i].linePortY2 );
					}
				}
			}
		};
		diagramPanel.setBackground(Color.white);
		
		statusBar = new JLabel("UML Editor");
		
		add( buttonPanel, BorderLayout.WEST );
		add( diagramPanel, BorderLayout.CENTER);
		add( statusBar, BorderLayout.SOUTH);

		diagramPanel.addMouseMotionListener( new CanvasMotionHandler() );
		diagramPanel.addMouseListener( new CanvasClickHandler() );
		
		buttonSelect.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if( buttonSelect.isSelected()){							
							selectedMode = SELECT_ACTIVATE;
							unSelectedOtherButtons(selectedMode);
						}
						else
							selectedMode = STILL_STATUS;						    
					}				
				}
		);
		
		buttonAsso.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if( buttonAsso.isSelected()){
						selectedMode = DRAW_LINE_ACTIVATE;
						assoActivate = true;
						genActivate = false;
						comActivate = false;
						unSelectedOtherButtons(selectedMode);
					}
					else{
						selectedMode = STILL_STATUS;
						assoActivate = false;
					}
				}									
			}
		);
		
		buttonGen.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if( buttonGen.isSelected()){
							selectedMode = DRAW_LINE_ACTIVATE;	
							genActivate = true;
							assoActivate = false;
							comActivate = false;
							unSelectedOtherButtons(selectedMode);
						}
						else{
							selectedMode = STILL_STATUS;
							genActivate = false;
						}
					}									
				}
		);
		
		buttonCom.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if( buttonCom.isSelected()){
							selectedMode = DRAW_LINE_ACTIVATE;	
							comActivate = true;
							assoActivate = false;
							genActivate  = false;
							unSelectedOtherButtons(selectedMode);
						}
						else{
							selectedMode = STILL_STATUS;	
							comActivate = false;
						}
					}									
				}
		);		
		
		buttonClass.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if( buttonClass.isSelected()){
						selectedMode = CLASS_ACTIVATE;
						unSelectedOtherButtons(selectedMode);
					}
					else
						selectedMode = STILL_STATUS;
				}				
			}
		);
		
		buttonCase.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if( buttonCase.isSelected()){
							selectedMode = CASE_ACTIVATE;
							unSelectedOtherButtons(selectedMode);
						}
						else
							selectedMode = STILL_STATUS;
					}				
				}
		);

	}
	
	private class CanvasMotionHandler extends MouseMotionAdapter{
				
		/*public void mouseMoved( MouseEvent event){	
			statusBar.setText(String.format( "Moved at ( %d, %d )", event.getX(), event.getY() ));		
		}*/
		
		public void mouseDragged( MouseEvent event){
			if( selectedMode == SELECT_ACTIVATE ){
				if(isSelectGroup){
					checkDragFrameBound( pressedPointX, pressedPointY, event.getX(), event.getY());			
					boolean isComObj = false;
					for(int i=0; i<drawCount; i++){		
						if( umlObj[i].xmin > leftX && umlObj[i].xmin < rightX && umlObj[i].ymin>lowerY && umlObj[i].ymin<upperY ){
							// check lower left point
							umlObj[i].isSelected = true;
						}
						else if( umlObj[i].xmax > leftX && umlObj[i].xmax < rightX && umlObj[i].ymin>lowerY && umlObj[i].ymin<upperY ){
							// check lower right point
							umlObj[i].isSelected = true;
						}else if( umlObj[i].xmin > leftX && umlObj[i].xmin < rightX && umlObj[i].ymax>lowerY && umlObj[i].ymax<upperY ){
							// check upper left point
							umlObj[i].isSelected = true;
						}else if( umlObj[i].xmax > leftX && umlObj[i].xmax < rightX && umlObj[i].ymax>lowerY && umlObj[i].ymax<upperY ){
							// check upper right point
							umlObj[i].isSelected = true;
						}else{
							umlObj[i].isSelected = false;							
						}
						
						if(umlObj[i].isSelected){
							for(int j=0; j<subObjNum; j++){
								if(comObj.objArray[j] == i){
									isComObj = true;
								}
							}
	   					}
					}					
   					if(isComObj){
   						for(int j=0; j<subObjNum; j++){
   							umlObj[ comObj.objArray[j] ].isSelected = true;
   						}
   					}
				}
				else if(isDragObj){
					int displaceX = event.getX() - orginPointX;
					int displaceY = event.getY() - orginPointY;
					for(int i=0; i<drawCount; i++){		
						if(umlObj[i].isSelected){
							umlObj[i].xmin = tmp_PointXmin[i] + displaceX;
							umlObj[i].ymin = tmp_PointYmin[i] + displaceY;
							umlObj[i].xmax = tmp_PointXmax[i] + displaceX;
							umlObj[i].ymax = tmp_PointYmax[i] + displaceY;
						}
					}
					// also repaint the line
					for(int i=0; i<lineCount; i++){
						int objnum1 = umlLine[i].lineObjNum1;
						int objnum2 = umlLine[i].lineObjNum2;
						if( umlObj[objnum1].isSelected){
							umlLine[i].linePortX1 = tmp_line_x1[i] + displaceX;
							umlLine[i].linePortY1 = tmp_line_y1[i] + displaceY;
						}
						if( umlObj[objnum2].isSelected ){
							umlLine[i].linePortX2 = tmp_line_x2[i] + displaceX;
							umlLine[i].linePortY2 = tmp_line_y2[i] + displaceY;
						}
					}
				}
				repaint();	
			}
			else if( selectedMode == DRAW_LINE_ACTIVATE ){
				for(int i=0; i<=lineCount; i++){
					if( umlLine[i].isDraggingLine){
						dragLinePointX = event.getX();
						dragLinePointY = event.getY();
						repaint();
					}	
				}
			}
			statusBar.setText(String.format( "Dragged at ( %d, %d )", event.getX(), event.getY() ));
		}
	}
	
	private class CanvasClickHandler extends MouseAdapter{
				
		public void mouseClicked( MouseEvent event ){
		   	switch (selectedMode){
		   		case SELECT_ACTIVATE:
		   			boolean isComObj = false;
		   			for(int i=drawCount; i>=0; i--){		   // The object depth starts at the top of the array 
		   				if( event.getX() <= umlObj[i].xmax && event.getX() >= umlObj[i].xmin &&
		   					event.getY() <= umlObj[i].ymax && event.getY() >= umlObj[i].ymin	)
		   				{
		   					umlObj[i].isSelected = true;
		   					for(int j=0; j<subObjNum; j++){
		   						if(comObj.objArray[j] == i){
		   							isComObj = true;
		   						}
		   					}
		   					if(isComObj){
		   						for(int j=0; j<subObjNum; j++){
		   							umlObj[ comObj.objArray[j] ].isSelected = true;
		   						}
		   					}
		   					break;
		   				}
		   				else{
		   					umlObj[i].isSelected = false;	// cancel selection 	
		   				}
		   			}
		   			repaint();
		   			break;
			    
		   			case CLASS_ACTIVATE:			
		   				if( drawCount < OBJECT_COUNT){			  
		   					umlObj[drawCount].objName = 
		   							(String)JOptionPane.showInputDialog( null, "Please input the name of Class object", "Name your Class",
															JOptionPane.PLAIN_MESSAGE, null, null,"object"+(drawCount+1));
		   					if( umlObj[drawCount].objName != null ){
		   						umlObj[drawCount].type = TYPE_CLASS;					
		   						// store the boundary of the Class
		   						umlObj[drawCount].xmin = event.getX();
		   						umlObj[drawCount].ymin = event.getY(); 
		   						umlObj[drawCount].xmax = event.getX() + OBJECT_WIDTH;
		   						umlObj[drawCount].ymax = event.getY() + OBJECT_HEIGHT;  // coordinate starts at upper left;
					
		   						drawCount++;					
		   						repaint();
		   					}
		   				}					
		   				break;
				
		   	case CASE_ACTIVATE:
			 	if( drawCount < OBJECT_COUNT){
					umlObj[drawCount].objName = 
							(String)JOptionPane.showInputDialog( null, "Please input the name of Use Case object", "Name your Use Case",
																JOptionPane.PLAIN_MESSAGE, null, null,"object"+(drawCount+1));
					if( umlObj[drawCount].objName != null ){
						umlObj[drawCount].type = TYPE_CASE;					
						// store the boundary of the Class
						umlObj[drawCount].xmin = event.getX();
						umlObj[drawCount].ymin = event.getY(); 
						umlObj[drawCount].xmax = event.getX() + OBJECT_WIDTH;
						umlObj[drawCount].ymax = event.getY() + OBJECT_HEIGHT;  // coordinate starts at upper left;
					
						drawCount++;					
						repaint();
					}
				}			
				break;
				
			default:
				break;
			}
		}
		
		public void mousePressed( MouseEvent event){
			switch(selectedMode){
			   case SELECT_ACTIVATE:
				   // drag the object if pressed at object				   
				   int excludeObj = -1;
				   boolean isComObj = false;
				   boolean cancelOtherObj = false;
				   for(int i=drawCount; i>=0; i--){					
					   if( event.getX() <= umlObj[i].xmax && event.getX() >= umlObj[i].xmin &&
				    		event.getY() <= umlObj[i].ymax && event.getY() >= umlObj[i].ymin	)
					   {				
						   if(!umlObj[i].isSelected){
							   umlObj[i].isSelected = true;
							   isDragObj = true;
							   excludeObj = i;
							   cancelOtherObj = true;
							   orginPointX = event.getX();
							   orginPointY = event.getY();
							   
							   for(int j=0; j<subObjNum; j++){
			   						if(comObj.objArray[j] == i){
			   							isComObj = true;
			   						}
			   					}
								
								break;
						   }
						   else if(umlObj[i].isSelected){		
							   isDragObj = true;
							   cancelOtherObj = false;
							   orginPointX = event.getX();
							   orginPointY = event.getY();
							   
							   for(int j=0; j<subObjNum; j++){
			   						if(comObj.objArray[j] == i){
			   							isComObj = true;
			   						}
			   					}
							   break;
						   }
					   }				
				   }		
				
				   if(cancelOtherObj){
					   for(int i=0; i<drawCount; i++){		
						   if( i != excludeObj )
							   umlObj[i].isSelected = false;
					   }
				   }
				   
  					if(isComObj){
   						for(int j=0; j<subObjNum; j++){
   							umlObj[comObj.objArray[j]].isSelected = true;
   						}
   				    }
				
				   if(isDragObj){
					   for(int i=0; i<drawCount; i++){			
						   tmp_PointXmin[i] = umlObj[i].xmin;
						   tmp_PointYmin[i] = umlObj[i].ymin;
						   tmp_PointXmax[i] = umlObj[i].xmax;
						   tmp_PointYmax[i] = umlObj[i].ymax;
					   }
					   for(int i=0; i<lineCount; i++){
							tmp_line_x1[i] = umlLine[i].linePortX1;
							tmp_line_y1[i] = umlLine[i].linePortY1;
							tmp_line_x2[i] = umlLine[i].linePortX2;
							tmp_line_y2[i] = umlLine[i].linePortY2;
					   }
				   }
				
				   // pressed out of the selected range 
				   if( !isDragObj ){
					   for(int i=0; i<drawCount; i++){		
						   umlObj[i].isSelected = false;
					   }
					   pressedPointX = event.getX();
					   pressedPointY = event.getY();
					   checkDragFrameBound( pressedPointX, pressedPointY, event.getX(), event.getY() );

					   isSelectGroup = true;       // start to draw selected frame 
				   }
				   
				   statusBar.setText(String.format( "Pressed at ( %d, %d )", event.getX(), event.getY() ));	
				   repaint();
				   
				   break;	
				   
			   case DRAW_LINE_ACTIVATE:
				   for(int i=0; i<drawCount; i++){		
					   if( event.getX() <= umlObj[i].xmax && event.getX() >= umlObj[i].xmin &&
					       event.getY() <= umlObj[i].ymax && event.getY() >= umlObj[i].ymin	)
					   	{
						   	  if(assoActivate){
						   		 umlLine[lineCount].lineType = TYPE_ASSO;
						   	  }else if(genActivate){
						   		 umlLine[lineCount].lineType = TYPE_GEN;
						   	  }else if(comActivate){
						   		 umlLine[lineCount].lineType = TYPE_COM;
						   	  }
						   	  pickObjNum = i;
							  pressLinePointX = event.getX();
							  pressLinePointY = event.getY();
							  dragLinePointX = event.getX();
							  dragLinePointY = event.getY();
							  umlLine[lineCount].lineObjNum1 = i;
						   	  checkRangePort(pickObjNum, pressLinePointX, pressLinePointY, 1);
							  umlLine[lineCount].isDraggingLine = true;
							  repaint();
						   	  break;
						}
				   }				   
				   
				   break;
				   
			   default:
				   break;
			}			
		}
		
		public void mouseReleased( MouseEvent event){
			if( selectedMode == SELECT_ACTIVATE ){
				isDragObj = false;
				isSelectGroup = false;
				repaint();
			}
			else if( selectedMode == DRAW_LINE_ACTIVATE ){
				  // judge the area of the object		
			   umlLine[lineCount].isDraggingLine = false;
			   for(int i=0; i<drawCount; i++){		
				  if( event.getX() <= umlObj[i].xmax && event.getX() >= umlObj[i].xmin &&
					  event.getY() <= umlObj[i].ymax && event.getY() >= umlObj[i].ymin	)
				  {
					  if( i != pickObjNum ){	// cannot released on itself
						  umlLine[lineCount].genLine = true;	
						  umlLine[lineCount].lineObjNum2 = i;
						  checkRangePort(i, (float)event.getX(), (float)event.getY(), 2);
						  lineCount++;						  
						  break;
					  }
				  }			   	  
			   }
			   repaint();
			}		
		}
	}
	
	public void checkDragFrameBound(int dragFrameX1, int dragFrameY1, int dragFrameX2, int dragFrameY2){
		f_width = dragFrameX2 - dragFrameX1;
		leftX = dragFrameX1;
		rightX = dragFrameX2;
		if( f_width < 0 ){
			leftX = dragFrameX2;
			rightX = dragFrameX1;
			f_width = -f_width;
		}
		f_height = dragFrameY2 - dragFrameY1;
		lowerY = dragFrameY1;
		upperY = dragFrameY2;
		if( f_height < 0 ){
			lowerY = dragFrameY2;
			upperY = dragFrameY1;
			f_height = -f_height;
		}
	}
	
	public void checkRangePort(int objNum, float point_x, float point_y, int vertex){
		  float m_up   = (float)(umlObj[objNum].ymin - umlObj[objNum].ymax)/(umlObj[objNum].xmax - umlObj[objNum].xmin);
	  	  float m_down = -m_up;
	  	  float d_up   = (float)(umlObj[objNum].ymin - m_up*umlObj[objNum].xmax);
	  	  float d_down = (float)(umlObj[objNum].ymax - m_down*umlObj[objNum].xmax);
	  	  // L: y - mx -d = 0 
	  	  if( ( point_y - m_down*point_x - d_down) >= 0 && ( point_y - m_up* point_x - d_up) <= 0  ){
	  		  // left area
	  		 if( vertex == 1 ){
	  			  umlLine[lineCount].linePortX1 =  umlObj[objNum].xmin;
	  			  umlLine[lineCount].linePortY1 = (umlObj[objNum].ymin + umlObj[objNum].ymax)/2;
	  		  }
	  		  else if( vertex == 2 ){
	  			  umlLine[lineCount].linePortX2 =  umlObj[objNum].xmin;
	  			  umlLine[lineCount].linePortY2 = (umlObj[objNum].ymin + umlObj[objNum].ymax)/2;
	  		  }	
	  		  umlLine[lineCount].portPos = 3;
	  	  }
	  	  else if( ( point_y - m_down*point_x - d_down) <= 0 && ( point_y - m_up* point_x - d_up) >= 0  ){
	  		  // right area
	  	  	  if( vertex == 1 ){
	  			  umlLine[lineCount].linePortX1 =  umlObj[objNum].xmax;
	  			  umlLine[lineCount].linePortY1 = (umlObj[objNum].ymin + umlObj[objNum].ymax)/2;
	  		  }
	  		  else if( vertex == 2 ){
	  			  umlLine[lineCount].linePortX2 =  umlObj[objNum].xmax;
	  			  umlLine[lineCount].linePortY2 = (umlObj[objNum].ymin + umlObj[objNum].ymax)/2;
	  		  }	
	  	  	  umlLine[lineCount].portPos = 4;
	  	  }
	  	  else if( ( point_y - m_down*point_x - d_down) >= 0 && ( point_y - m_up* point_x - d_up) >= 0  ){
	  		  // down area
	  		  if( vertex == 1 ){
	  			  umlLine[lineCount].linePortX1 = (umlObj[objNum].xmin + umlObj[objNum].xmax)/2;
	  			  umlLine[lineCount].linePortY1 =  umlObj[objNum].ymax;
	  		  }
	  		  else if( vertex == 2 ){
	  			  umlLine[lineCount].linePortX2 = (umlObj[objNum].xmin + umlObj[objNum].xmax)/2;
	  			  umlLine[lineCount].linePortY2 =  umlObj[objNum].ymax;
	  		  }
	  		  umlLine[lineCount].portPos = 1;
	  	  }
	  	  else if( ( point_y - m_down*point_x - d_down) <= 0 && (point_y - m_up* point_x - d_up) <= 0  ){
  			 // up area
	  		  if( vertex == 1 ){
	  			  umlLine[lineCount].linePortX1 = (umlObj[objNum].xmin + umlObj[objNum].xmax)/2;
	  			  umlLine[lineCount].linePortY1 =  umlObj[objNum].ymin;
	  		  }
	  		  else if( vertex == 2 ){
	  			  umlLine[lineCount].linePortX2 = (umlObj[objNum].xmin + umlObj[objNum].xmax)/2;
	  			  umlLine[lineCount].linePortY2 =  umlObj[objNum].ymin;
	  		  }	  		  
	  		  umlLine[lineCount].portPos = 2;
	  	  }		
	}
	
	public void unSelectedOtherButtons(int selectedType){
			if(selectedType != SELECT_ACTIVATE){
				buttonSelect.setSelected(false);
			}
			if(selectedType != DRAW_LINE_ACTIVATE){
				buttonAsso.setSelected(false);	
				buttonGen.setSelected(false);
				buttonCom.setSelected(false);
				assoActivate = false;
				genActivate  = false;
				comActivate  = false;
			}
			if( !assoActivate ){
				buttonAsso.setSelected(false);			
			}
			if( !genActivate ){
				buttonGen.setSelected(false);			
			}
			if( !comActivate ){
				buttonCom.setSelected(false);			
			}
			if(selectedType != CLASS_ACTIVATE){
				buttonClass.setSelected(false);
			}
			if(selectedType != CASE_ACTIVATE){
				buttonCase.setSelected(false);
			}
	}
	
	public static void main( String args[]){
		UML_editor UMLinterface = new UML_editor();
		UMLinterface.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		UMLinterface.setLocation(300, 100);
		UMLinterface.setSize(1000, 600);
		UMLinterface.setResizable(false);
		UMLinterface.setVisible(true);			
	}
}

