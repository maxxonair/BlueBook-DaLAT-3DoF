package GUI.Dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import GUI.BlueBookVisual;
import GUI.BlueBookPlot.main.BlueBookPlot;
import GUI.Dashboard.Console.ConsoleClass;
import GUI.DataStructures.InputFileSet;
import Simulator_main.DataSets.RealTimeResultSet;
import utils.GuiReadInput;
import utils.ReadInput;
import utils.WriteInput;

public class DashboardPlotArea {
	//-------------------------------------------------------------------------------------------------------------
	
	private static JPanel mainPanel;
	//-------------------------------------------------------------------------------------------------------------
	// Formatting valules (Fonts, Borders, decimalLayouts etc):	
	
    private Color backgroundColor;
    private Color labelColor;
    
    private int numberOfCharts = 4;
    //-------------------------------------------------------------------------------------------------------------
    // Global GUI components:

	//-------------------------------------------------------------------------------------------------------------
    // Content Lists 
    private static List<DashboardPlotPanel> contentPanelList;	
    static List<RealTimeResultSet> resultSet;
    private static List<ChartSetting> chartSettings;
    //-------------------------------------------------------------------------------------------------------------
    // Class Values:
    private static List<InputFileSet> analysisFile = new ArrayList<InputFileSet>();
    private static  int targetIndx=1;
    private static String Model3DFilePath=System.getProperty("user.dir") + "/resourcs/models3D/millenium-falcon.obj";

    
    private static ConsoleClass masterConsole;


	public DashboardPlotArea(ConsoleClass console) {
		
		masterConsole = console;
		
		backgroundColor = BlueBookVisual.getBackgroundColor();
		labelColor = BlueBookVisual.getLabelColor();
	    resultSet  = GuiReadInput.READ_ResultSet();
		
		try {
			analysisFile = GuiReadInput.readResultFileList(System.getProperty("user.dir") + "/results.txt" );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		contentPanelList = new ArrayList<>(numberOfCharts);
		chartSettings = initList();
		
		
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(900, 900-100));
        mainPanel.setBackground(backgroundColor);
        mainPanel.setForeground(labelColor);

        //-------------------------------------------------------------------------------

        
        /**
         *  Draw all 4 areas 
         */
        updateDashboardPlotArea(contentPanelList);
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	public static void updateDashboardPlotArea(List<DashboardPlotPanel> contentPanelList) {
		try {
			mainPanel.removeAll();
		} catch (Exception exception) {
			
		}
		
		updateResultSet();
		
		JSplitPane horizontalSplitUp = SplitPane.getSplitPane("horizontal");
        JSplitPane splitPane2 = SplitPane.getSplitPane("vertical");
        JSplitPane horizontalSplitDown = SplitPane.getSplitPane("horizontal");
        splitPane2.add(horizontalSplitUp,JSplitPane.TOP);
        splitPane2.add(horizontalSplitDown,JSplitPane.BOTTOM);
		for(int i=0;i<contentPanelList.size();i++) {
			
			try {
				contentPanelList.get(i).refresh();
			} catch (NullPointerException npe) {
				System.out.println("Error contentPanelList/refresh failed.");
			}

			if(i==0) {
				horizontalSplitUp.add(contentPanelList.get(i).getMainPanel(), JSplitPane.LEFT);
			} else if (i==1) {
				horizontalSplitDown.add(contentPanelList.get(i).getMainPanel(), JSplitPane.LEFT);
			} else if (i==2) {
				horizontalSplitDown.add(contentPanelList.get(i).getMainPanel(), JSplitPane.RIGHT);
			} else if (i==3) {
				horizontalSplitUp.add(contentPanelList.get(i).getMainPanel(), JSplitPane.RIGHT);
			}
		}
		horizontalSplitUp.setDividerLocation(0.5);
        splitPane2.setDividerLocation(370);
        horizontalSplitDown.setDividerLocation(0.5);
        mainPanel.add(splitPane2);
        
	}

	public static List<InputFileSet> getAnalysisFile() {
		return analysisFile;
	}

	public static void setAnalysisFile(List<InputFileSet> analysisFile) {
		DashboardPlotArea.analysisFile = analysisFile;
	}
	
	
	public static List<DashboardPlotPanel> getContentPanelList() {
		return contentPanelList;
	}

	public static void setContentPanelList(List<DashboardPlotPanel> contentPanelList) {
		DashboardPlotArea.contentPanelList = contentPanelList;
		DashboardPlotArea.updateDashboardPlotArea(contentPanelList);
	}	
	
	public static int getTargetIndx() {
		return targetIndx;
	}

	public static void setTargetIndx(int targetIndx) {
		DashboardPlotArea.targetIndx = targetIndx;
		DashboardPlotArea.updateDashboardPlotArea(contentPanelList);
	}

	public static ConsoleClass getMasterConsole() {
		return masterConsole;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Component Tester");
		frame.setSize(400,400);
		frame.setLayout(new BorderLayout());

		DashboardPlotArea dataplot = new DashboardPlotArea(new ConsoleClass());
		frame.add(dataplot.getMainPanel(), BorderLayout.CENTER);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
		frame.pack();
	}

	public static  String getModel3DFilePath() {
		return Model3DFilePath;
	}

	public static void setModel3DFilePath(String model3dFilePath) {
		Model3DFilePath = model3dFilePath;
		DashboardPlotArea.updateDashboardPlotArea(contentPanelList);
	}

	public static List<RealTimeResultSet> getResultSet() {
		return resultSet;
	}
	
	public static void updateResultSet() {
		resultSet = GuiReadInput.READ_ResultSet();
	}

	public static List<ChartSetting> getChartSettings() {
		return chartSettings;
	}

	public static void setChartSettings(List<ChartSetting> chartSettings) {
		DashboardPlotArea.chartSettings = chartSettings;
		WriteInput.writeDashboradSetting(chartSettings);
		updateDashboardPlotArea(contentPanelList);
	}

	private List<ChartSetting> initList(){
		/**
		 *  Read Chart layout from file 
		 */
		chartSettings = new ArrayList<>();

		try {
			chartSettings = ReadInput.readChartLayout(numberOfCharts);	
		} catch(Exception readExcp) {
			System.out.println("Error: Reading chart layout failed");
			System.out.println(readExcp);
		}
		if( chartSettings.isEmpty() ) { // chartLayout not initialized
		chartSettings = new ArrayList<ChartSetting>(numberOfCharts);
		
			for(int i=0;i<numberOfCharts;i++) {
				ChartSetting chartSetting = new ChartSetting();
				if(i==0) {
					chartSetting.x = 0;
					chartSetting.y = 4;
				} else {
					chartSetting.x = 6;
					chartSetting.y = 4;
				}
				chartSettings.add(chartSetting);
			}
			chartSettings.get(0).type =0;
			chartSettings.get(1).type =0;
			chartSettings.get(2).type =2;
			chartSettings.get(3).type =1;
	        contentPanelList.add( (new Data2DPlot(0, analysisFile)) );
	        contentPanelList.add( (new Data2DPlot(1, analysisFile)) );
	        //contentPanelList.add( (new JPanel() ));
	        contentPanelList.add( (new AttitudeView(Model3DFilePath)));
	        contentPanelList.add( (new Planet3DView(resultSet)) );
		} else {
				for(int i=0;i<numberOfCharts;i++) {
					if(chartSettings.get(i).type==0) {
				        contentPanelList.add( (new Data2DPlot(i, analysisFile)) );
					} else if (chartSettings.get(i).type == 1) {
				        contentPanelList.add( (new Planet3DView(resultSet)) );
					} else if (chartSettings.get(i).type == 2) {
				        contentPanelList.add( (new AttitudeView(Model3DFilePath)));
					} else if (chartSettings.get(i).type == 3) {
						ConsoleClass dashboardConsole = new ConsoleClass();				
					    dashboardConsole.setDoc(masterConsole.getDoc()); // Link output stream to main console
				        contentPanelList.add( dashboardConsole );
					} else if (chartSettings.get(i).type == 4) {			
				        contentPanelList.add( new BlueBookPlot() );
					}
				}
		}
		return chartSettings; 
	}
}
