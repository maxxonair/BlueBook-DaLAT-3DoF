package GUI.Dashboard;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStreamReader;

import javax.imageio.IIOException;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import GUI.BlueBookVisual;
import GUI.RotatedIcon;
import GUI.TextIcon;
import GUI.DataStructures.InputFileSet;

public class PlotElement {
	
	 public static double PI = 3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808;
		@SuppressWarnings("unused")
		private final static double deg2rad = PI/180.0; 		//Convert degrees to radians
		private final static double rad2deg = 180/PI; 		//Convert radians to degrees
    private JButton yAxisIndicator, xAxisIndicator;
    private VariableList2 variableListY, variableListX;
    
    private List<InputFileSet> resultFile = new ArrayList<InputFileSet>(); 
    
    private XYSeriesCollection resultSet = new XYSeriesCollection();
    
    private Crosshair xCrosshair, yCrosshair;
    
    private List<String> variableList = new ArrayList<String>();
    
    private ChartSetting chartSetting;
    
    private int crosshairIndx = 0;
    
    private ChartPanel chartPanel;
    
    private int ID;
    private Color backgroundColor;
    
    public PlotElement(int ID, List<String> variableList, List<InputFileSet> analysisFile) {
    	this.variableList = variableList;
    	this.resultFile = analysisFile; 
    	this.ID = ID;
    	this.chartSetting = DashboardPlotArea.getChartSettings().get(ID);
    }
	
	public JPanel createPlotElement(PlotElement plotElement) {
		
		chartSetting = DashboardPlotArea.getChartSettings().get(ID);
		
		backgroundColor = BlueBookVisual.getBackgroundColor();
		
        JPanel panel = new JPanel();
        panel.setLocation(0, 0);
        panel.setPreferredSize(new Dimension(400, 400));
        panel.setLayout(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setForeground(BlueBookVisual.getLabelColor());
        
	    
	    JPanel FlexibleChartControlPanel = new JPanel();
	    FlexibleChartControlPanel.setLayout(new BorderLayout());
	    FlexibleChartControlPanel.setBackground(backgroundColor);
	    //FlexibleChartContentPanel.add(FlexibleChartControlPanel, BorderLayout.PAGE_START);
	    
	    JPanel xControlPanel = new JPanel();
	    xControlPanel.setLayout(new BorderLayout());
	    //xControlPanel.setPreferredSize(new Dimension(1000, 25));
	    xControlPanel.setBackground(backgroundColor);
	    panel.add(xControlPanel, BorderLayout.PAGE_END);
	    
	    JPanel yControlPanel = new JPanel();
	    yControlPanel.setLayout(new BorderLayout());
	    //yControlPanel.setPreferredSize(new Dimension(400, 25));
	    yControlPanel.setBackground(backgroundColor);
	    panel.add(yControlPanel, BorderLayout.LINE_START);

	      
	       yAxisIndicator = new JButton();
	       variableListY =  new VariableList2(yAxisIndicator, "y",plotElement);
	       yAxisIndicator.setBackground(backgroundColor);
	       yAxisIndicator.setForeground(BlueBookVisual.getLabelColor());
	       yAxisIndicator.setOpaque(true);
	       yAxisIndicator.setBorderPainted(false);
	       try {
	      TextIcon t1 = new TextIcon(yAxisIndicator, variableList.get(plotElement.getChartSetting().y), TextIcon.Layout.HORIZONTAL);
	      RotatedIcon r1 = new RotatedIcon(t1, RotatedIcon.Rotate.UP);
	      t1.setFont(BlueBookVisual.getSmall_font());
	      yAxisIndicator.addActionListener(new ActionListener() {

	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				variableListY.getVariableList(variableList);
			}
	    	  
	      });
	      yAxisIndicator.setIcon( r1 );
	      variableListY.setSelectedIndx(chartSetting.y);
	       } catch(Exception e) {
	    	   
	       }
	       
	       xAxisIndicator = new JButton();
	       variableListX =  new VariableList2(xAxisIndicator, "x",plotElement);
	       if(ID==0 && chartSetting.x==0) {
		       xAxisIndicator.setForeground(Color.BLUE);
	       } else {
		       xAxisIndicator.setForeground(BlueBookVisual.getLabelColor());
	       }
	       xAxisIndicator.setBackground(backgroundColor);
	       xAxisIndicator.setOpaque(true);
	       xAxisIndicator.setBorderPainted(false);
	       try {
	       TextIcon t1 = new TextIcon(xAxisIndicator, variableList.get(chartSetting.x), TextIcon.Layout.HORIZONTAL);
	       RotatedIcon r1 = new RotatedIcon(t1, RotatedIcon.Rotate.ABOUT_CENTER);
	      xAxisIndicator.addActionListener(new ActionListener() {

	
			@Override
			public void actionPerformed(ActionEvent arg1) {
				variableListX.getVariableList(variableList);
			}
	    	  
	      });
	      xAxisIndicator.setIcon( r1 );
	       } catch (Exception e) {
	    	   
	       }

	      xAxisIndicator.setPreferredSize(new Dimension(25,25));
	      yAxisIndicator.setPreferredSize(new Dimension(25,25));
	      xAxisIndicator.setMinimumSize(new Dimension(25,25));
	      yAxisIndicator.setMinimumSize(new Dimension(25,25));
	      xControlPanel.add(xAxisIndicator, BorderLayout.CENTER);
	      yControlPanel.add(yAxisIndicator, BorderLayout.CENTER);
        
	      
	      try {
			chartPanel = createChartPanel();
		      panel.add(chartPanel, BorderLayout.CENTER);
			} catch (IOException e) {
				System.err.println("ERROR: Chart Panel could not be created.");
			}
        
		return panel;
	}
	
	public ChartPanel createChartPanel() throws IOException {
		//result1.removeAllSeries();
		try {
		resultSet = addDataSet(chartSetting.x,chartSetting.y, resultSet, variableList);
		} catch(FileNotFoundException | ArrayIndexOutOfBoundsException eFNF2) {
			
		}
	    //-----------------------------------------------------------------------------------
		JFreeChart chart = ChartFactory.createScatterPlot("", "", "", resultSet, PlotOrientation.VERTICAL, false, false, false); 
		XYPlot plot = (XYPlot)chart.getXYPlot(); 
	    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
		Font font3 = new Font("Dialog", Font.PLAIN, 12);
	    double size = 2.0;
	    double delta = size / 2.0;
		Shape dot = new Ellipse2D.Double(-delta, -delta, size, size);
		//plotColor = removeAllPlotColor(plotColor);
	    for(int i=0;i<resultFile.size();i++) {
	    plot.setRenderer(i, renderer);
		plot.getDomainAxis().setLabelFont(font3);
		plot.getRangeAxis().setLabelFont(font3);
		plot.getRangeAxis().setLabelPaint(BlueBookVisual.getLabelColor());
		plot.getDomainAxis().setLabelPaint(BlueBookVisual.getLabelColor());
		plot.setForegroundAlpha(0.5f);
		plot.setBackgroundPaint(backgroundColor);
		plot.setDomainGridlinePaint(BlueBookVisual.getLabelColor());
		plot.setRangeGridlinePaint(BlueBookVisual.getLabelColor()); 
	    renderer.setSeriesPaint( i , BlueBookVisual.getLabelColor());
		renderer.setSeriesShape(i, dot);
	    }
		chart.setBackgroundPaint(backgroundColor); 	
		//chart.getLegend().setBackgroundPaint(backgroundColor);
		//chart.getLegend().setItemPaint(labelColor);
		try {
			if(!resultFile.get(0).isLegend()) {
				chart.removeLegend();
			}
		} catch (Exception e) {
			System.err.println("ERROR: PlotElement/Loading Legend Failed");
		}
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		//final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		//domainAxis.setInverted(true);
		//Shape cross = ShapeUtilities.createDiagonalCross(1, 1) ;

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMaximumDrawHeight(50000);
		chartPanel.setMaximumDrawWidth(50000);
		chartPanel.setMinimumDrawHeight(0);
		chartPanel.setMinimumDrawWidth(0);
		chartPanel.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				if(arg0.getButton()==2) {
				//System.out.println("clicked")	;
				updateCrosshairIndx();
				}
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		chartPanel.setMouseWheelEnabled(true);
		//chartPanel.setPreferredSize(new Dimension(900, page1_plot_y));
		chartPanel.addChartMouseListener(new ChartMouseListener() {
	        @Override
	        public void chartMouseClicked(ChartMouseEvent event) {
	            // ignore
	        }
	
	        @Override
	        public void chartMouseMoved(ChartMouseEvent event) {
	            Rectangle2D dataArea = chartPanel.getScreenDataArea();
	            JFreeChart chart = event.getChart();
	            XYPlot plot = (XYPlot) chart.getPlot();
	            ValueAxis xAxis = plot.getDomainAxis();
	            double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea, 
	                    RectangleEdge.BOTTOM);
	            try {
	            double y = DatasetUtilities.findYValue(plot.getDataset(), crosshairIndx, x);
	            xCrosshair.setValue(x);
	            yCrosshair.setValue(y);
	            } catch (Exception e) {
	            	
	            }
	            double max = xAxis.getUpperBound();
	            double min = xAxis.getLowerBound();
	            int indx = (int) ( (x/(max-min))*DashboardPlotArea.getResultSet().size());
	            
		            if(indx>0 && indx<DashboardPlotArea.getResultSet().size()) {
	            		try {
	            			updateDashboardValues(indx);
                		} catch (Exception exp ) {
                			
                		}
		            }
	        }
	});
	    CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
	    xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
	    xCrosshair.setLabelVisible(true);
	    yCrosshair = new Crosshair(Double.NaN, Color.RED, new BasicStroke(0f));
	    yCrosshair.setLabelVisible(true);
	    //if(false) {
		    xCrosshair.setLabelBackgroundPaint(BlueBookVisual.getLabelColor());
		    yCrosshair.setLabelBackgroundPaint(BlueBookVisual.getLabelColor());
	   // }
	    crosshairOverlay.addDomainCrosshair(xCrosshair);
	    crosshairOverlay.addRangeCrosshair(yCrosshair);
	    chartPanel.addOverlay(crosshairOverlay);

	   return chartPanel;
	}
	
	private void updateDashboardValues(int indx){
		
		//System.out.println(BlueBookVisual.decf.format(Math.toDegrees(DashboardPlotArea.getResultSet().get(indx).getLongitude())));
		DashboardLeftPanel.INDICATOR_LONG.setText(BlueBookVisual.decf.format(Math.toDegrees(DashboardPlotArea.getResultSet().get(indx).getLongitude())));
		DashboardLeftPanel.INDICATOR_LAT.setText(BlueBookVisual.decf.format(Math.toDegrees(DashboardPlotArea.getResultSet().get(indx).getLatitude())));		
		DashboardLeftPanel.INDICATOR_ALT.setText(BlueBookVisual.decf.format(DashboardPlotArea.getResultSet().get(indx).getAltitude()));
		
		DashboardLeftPanel.INDICATOR_VEL.setText(BlueBookVisual.decf.format(DashboardPlotArea.getResultSet().get(indx).getVelocity()));
		DashboardLeftPanel.INDICATOR_FPA.setText(BlueBookVisual.decf.format(Math.toDegrees(DashboardPlotArea.getResultSet().get(indx).getFpa())));
		DashboardLeftPanel.INDICATOR_AZI.setText(BlueBookVisual.decf.format(Math.toDegrees(DashboardPlotArea.getResultSet().get(indx).getAzi())));
	
		DashboardLeftPanel.INDICATOR_M0.setText(BlueBookVisual.decf.format(DashboardPlotArea.getResultSet().get(indx).getSCMass()));
	
		DashboardLeftPanel.INDICATOR_PRIMTANKFIL.setText(BlueBookVisual.decf.format(DashboardPlotArea.getResultSet().get(indx).getPrimTankfillingLevelPerc()));
		DashboardLeftPanel.INDICATOR_SECMTANKFIL.setText(BlueBookVisual.decf.format(DashboardPlotArea.getResultSet().get(indx).getSecTankfillingLevelPerc()));
	}
	
	public void updateChart(){
    	resultSet.removeAllSeries();
    	try {
   // 	resultSet = addDataSet(variableListX.getSelectedIndx(),variableListY.getSelectedIndx(), 
    	//		resultSet, variableList);
    	resultSet = addDataSet(chartSetting.x,chartSetting.y, 
    			resultSet, variableList);
   //	chart.getXYPlot().getDomainAxis().setAttributedLabel(String.valueOf(axis_chooser.getSelectedItem()));
    //	chart.getXYPlot().getRangeAxis().setAttributedLabel(String.valueOf(axis_chooser2.getSelectedItem()));
    	} catch(ArrayIndexOutOfBoundsException | IOException eFNF2) {
    	}
}
	
	public XYSeriesCollection addDataSet(int x, int y, XYSeriesCollection XYSeriesCollection, List<String> variableList2) throws IOException , IIOException, FileNotFoundException, ArrayIndexOutOfBoundsException{
	for(int i=0;i<resultFile.size();i++) {  
					XYSeries xySeries = new XYSeries(""+i+""+resultFile.get(i).getInputDataFileName(), false, true); 
			        FileInputStream fstream = null;
					try{ fstream = new FileInputStream(resultFile.get(i).getInputDataFilePath());} catch(IOException eIO) { System.out.println(eIO);}
			        DataInputStream in = new DataInputStream(fstream);
			        BufferedReader br = new BufferedReader(new InputStreamReader(in));
			        String strLine;
			        try {
				                  while ((strLine = br.readLine()) != null )   {
							            String[] tokens = strLine.split(" ");
							            double xx=0; double yy=0; 
							            if(containsIllegalCharacter(tokens[x])) {
							            	xx = 0;
							            	System.err.println("ERROR: Illegal character in x values detected. Value index ignored.");
							            } else {
							            	xx = Double.parseDouble(tokens[x]);
							            }
							            if(containsIllegalCharacter(tokens[y])) {
							            	yy = 0;
							            	System.err.println("ERROR: Illegal character in y values detected. Value index ignored.");
							            } else {
							            	yy = Double.parseDouble(tokens[y]);
							            }
							            
							            	 @SuppressWarnings("static-access")
											String x_axis_label = variableList2.get(variableListX.getSelectedIndx());
							            	 boolean isangle = x_axis_label.indexOf("[deg]") !=-1? true: false;
							            	 boolean isangle2 = x_axis_label.indexOf("[deg/s]") !=-1? true: false;
							            	 if(isangle || isangle2) {xx = xx*rad2deg;} 
			
							            	 @SuppressWarnings("static-access")
											String y_axis_label = variableList2.get(variableListY.getSelectedIndx());
							            	 boolean isangle3 = y_axis_label.indexOf("[deg]") !=-1? true: false;
							            	 boolean isangle4 = y_axis_label.indexOf("[deg/s]") !=-1? true: false;
							            	 if(isangle3 || isangle4) {yy = yy*rad2deg;} 
							             
							            	 xySeries.add(xx , yy);
						           }
			 in.close();
			 XYSeriesCollection.addSeries(xySeries); 
			        } catch (NullPointerException | NumberFormatException eNPE) { 
			      	  System.err.println("ERROR: Error occurred during file import. PlotElement>>addDataset failed.");
			      	  }
	}
	return XYSeriesCollection;
	}

	public ChartSetting getChartSetting() {
		return chartSetting;
	}

	public void setChartSetting(ChartSetting chartSetting) {
		this.chartSetting = chartSetting;
		DashboardPlotArea.getChartSettings().set(ID, chartSetting);
		
		List<ChartSetting> settings = DashboardPlotArea.getChartSettings();
		settings.set(ID, chartSetting);
		DashboardPlotArea.setChartSettings(settings);
		
	       if(ID==0 && chartSetting.x==0) {
		       xAxisIndicator.setForeground(Color.BLUE);
	       } else {
		       xAxisIndicator.setForeground(BlueBookVisual.getLabelColor());
	       }
		updateChart();
	}

	public int getID() {
		return ID;
	}
	
	private void updateCrosshairIndx() {
		/*
		crosshairIndx++;
		if(crosshairIndx>BlueBookVisual.getInputFileSet().size()-1) {
			crosshairIndx=0;
		}
		*/
	}
	

	private boolean containsIllegalCharacter(String teststring) {
		boolean result=false; 
        try {
            @SuppressWarnings("unused")
			Double num = Double.parseDouble(teststring);
        } catch (NumberFormatException e) {
        	result = true;
        }
		return result;
	}

	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	public VariableList2 getVariableListY() {
		return variableListY;
	}

	public VariableList2 getVariableListX() {
		return variableListX;
	}
	
	

}
