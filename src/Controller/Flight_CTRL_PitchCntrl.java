package Controller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Flight_CTRL_PitchCntrl{
        public static double PI = 3.141592653589793238462643383279502884197169399375;
    	static double deg2rad = PI/180.0; 		//Convert deg2radrees to radians
    	static double rad2deg = 180.0/PI; 		//Convert radians to deg2radrees
	//--------------------------------------------------------------------------------------------------------
	//
	//		Thrust vector controller:
	//
	//			x - > Time [s] (Sequence time!)
	//			y - > Flight Path Angle [rad]
	//
	//--------------------------------------------------------------------------------------------------------
	private int     ctrl_ID;				// Controller ID					[-]
	private boolean ctrl_on;				// Switch on/off (ctrl)				[true/false]
	private double  ctr_init_x;
	private double  ctr_init_y;
	private double  ctr_end_x;
	private double  ctr_end_y;
	private double  x_is; 
	private double  y_is;
	private double  y_ideal; 
	private int     ctrl_curve;				// Control curve 					[-]
	private double  ctrl_dt;				// Delta-t controller				[s]
	private double  P_GAIN; 				// controller p-gain				[-]
	private double  I_GAIN;					// controller i-gain		   		[-]
	private double  D_GAIN;					// controller d-gain				[-]
	private double  cmd_min;				// controller output cmd min		[-]
	private double  cmd_max;				// controller output cmd max    	[-]
	private double  CTRL_ERROR;				// controller Errror				[ ]
	private double  CTRL_TIME;				// controller Time 					[s]	
	private double  CTRL_Thrust;
	private boolean engine_lost=false; 
		
	private double tvc_cmd;					// thrust vector angle cmd			[rad] 
	
	private double  tzero; 					// Sequence Time 						[s]
	private boolean tswitch =true;          // Time switch to start controller time [s]
	
	private double tvc_was =0; 
	private boolean tvc_switch=true; 
	private double tvcwas=0; 
	//-----------------------------------------------------------------------------
	static String INPUT_FILE = null; 
	public static String ControllerInputFile_1 = "/CTRL/cntrl_";
    public static String ControllerInputFile_2 = ".inp"; 
	public static String ControllerInputFile; 
	//-----------------------------------------------------------------------------
	public Flight_CTRL_PitchCntrl(int ctrl_ID, boolean ctrl_on, double ctr_init_x, double ctr_init_y, double ctr_end_x, double ctr_end_y) {
		this.ctrl_ID	  = ctrl_ID;
		this.ctrl_on 	  = ctrl_on;
		this.ctr_init_x   = ctr_init_x;
		this.ctr_init_y   = ctr_init_y;
		this.ctr_end_x    = ctr_end_x;
		this.ctr_end_y    = ctr_end_y; 
		if (ctrl_ID==0) {
			this.ctrl_on=false;
		} else {
		double[] readINP;
		try {
			readINP = READ_CTRL_INPUT(Integer.toString(ctrl_ID));
			if (readINP[0]==1) {this.ctrl_on=true;}else {this.ctrl_on=false;}
		 this.P_GAIN  = readINP[1];
		 this.I_GAIN  = readINP[2];
		 this.D_GAIN  = readINP[3];
		 this.cmd_max = readINP[4];
		 this.cmd_min = readINP[5];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
			System.out.println("ERROR: Controller 01 setting read failed.");
		}
		}
	}
	
    public static double[] READ_CTRL_INPUT(String Controller_ID) throws IOException{
        double[] readINP = new double[20];
        if(Integer.parseInt(Controller_ID)!=0) {
        String dir = System.getProperty("user.dir");
        INPUT_FILE = dir+ControllerInputFile_1+""+Controller_ID+""+ControllerInputFile_2;
       	double InitialState = 0;
   	    FileInputStream fstream = null;
       	try {
   	    fstream = new FileInputStream(INPUT_FILE);
   	    } catch(IOException eIO) { System.out.println(eIO);}
            DataInputStream in = new DataInputStream(fstream);
   			BufferedReader br = new BufferedReader(new InputStreamReader(in));
           String strLine;
           int k = 0;
           try {
           while ((strLine = br.readLine()) != null )   {
           	String[] tokens = strLine.split(" ");
           	InitialState = Double.parseDouble(tokens[0]);
           	readINP[k]= InitialState;
           	k++;
           }
           fstream.close();
           in.close();
           br.close();
           } catch(NullPointerException eNPE) { System.out.println(eNPE);}
        }
           return readINP;
       }
       
	
	public int get_ctrl_ID() {
		return ctrl_ID;
	}
	public boolean get_ctrl_on() {
		return ctrl_on;
	}

	public double get_TVC_cmd(){
    	if(ctrl_on) {
 		    //------------------------------------------------------------------------------------------------------------------
 		    //									Select Reference Landing Path 
 		    //------------------------------------------------------------------------------------------------------------------
    			boolean ContinuousBurn = false;
    		if        (ctrl_curve==0) {
		         ContinuousBurn = true;
		    } else if (ctrl_curve==1) {
 		         y_ideal =    PitchCurve.SquareRootPitchCurve(ctr_init_x, ctr_init_y, ctr_end_y, ctr_end_x, x_is);
 		    } else if (ctrl_curve==2) {
 		    		y_ideal =   0; // TBD
 		    } else if (ctrl_curve==3) {
 		    		y_ideal =   PitchCurve.LinearPitchCurve(ctr_init_x, ctr_init_y, ctr_end_y, ctr_end_x, x_is);
 		    } 
 		    //------------------------------------------------------------------------------------------------------------------
 		    //									No Reference Landing curve set -> Continuous burn 
 		    //------------------------------------------------------------------------------------------------------------------
     		    //------------------------------------------------------------------------------------------------------------------
     		    //									No Reference Landing curve set -> Continuous burn 
     		    //------------------------------------------------------------------------------------------------------------------
     		   if(ContinuousBurn){  } else {
     	 		//------------------------------------------------------------------------------------------------------------------
     	 		//									No Reference Lanidng curve set -> Continuous burn 
     	 		//------------------------------------------------------------------------------------------------------------------
     		     CTRL_ERROR = y_is - y_ideal ;
     		    // Select Controller and compute output command: 
     		     tvc_cmd = PID_01.PID_001(CTRL_ERROR,ctrl_dt, P_GAIN, I_GAIN, D_GAIN, cmd_max, cmd_min);
     		    // Check if controller provides usable values: 
     		   if(Double.isNaN(tvc_cmd)) {System.out.println("Controller Error: Returned NaN.-> Controller OFF"); tvc_cmd=0;}
     		//   if(1400<alt && alt<1500) {System.out.println(alt+" | "+target_velocity + " | "+ CTRL_ERROR + " | "+ throttle_cmd);}

     		   }
        	} else {
        		tvc_cmd = 0; // Controller off 
        	}
    		return (-tvc_cmd);
    	}
	public double get_Pitch_cmd() {
		if(ctrl_on) {
			double turn_rate_rad = ctr_end_y; 
			if(engine_lost&&turn_rate_rad>0) {turn_rate_rad=turn_rate_rad*0.3;}
			if(engine_lost&&turn_rate_rad<0) {turn_rate_rad=turn_rate_rad*2;}
			if(turn_rate_rad<0) {
				if(tvc_switch){tvcwas = tvc_was;tvc_switch=false;}} else {tvcwas=0;}
				if(engine_lost) {tvc_cmd=4.5*deg2rad;} else {tvc_cmd= tvcwas + turn_rate_rad*CTRL_TIME;}
			double fpa_is = y_is*rad2deg;
			if(fpa_is>180) {
				tvc_cmd = -5*deg2rad; 
			}
			//System.out.println(""+CTRL_TIME+" | "+tvcwas+" | "+tvc_cmd*rad2deg);
		}
		return tvc_cmd; 
	}
	
	public double get_maintain_horizontal_TVC_cmd() {
		double fpa_is = y_is*rad2deg;
	     CTRL_ERROR = 180 - fpa_is ;
	     //if(Math.abs(Math.abs(fpa_is)-180)>0.5){
	    	 tvc_cmd = PID_01.PID_001(CTRL_ERROR,ctrl_dt, P_GAIN, I_GAIN, D_GAIN, cmd_max, cmd_min)*deg2rad;
	    	// System.out.println(""+CTRL_ERROR+" | "+tvc_cmd*rad2deg+" | "+P_GAIN+" | "+I_GAIN+" | "+D_GAIN);
	   //  } else {
	  //  	 tvc_cmd = 0 ; 
	  //   }

		return tvc_cmd; 
	}

	public int get_ctrl_curve() {
		return ctrl_curve; 
	}
	public double get_ctrl_dt() {
		return ctrl_dt; 
	}
	public double get_CTRL_ERROR() {
		return CTRL_ERROR;
	}
	public double get_CTRL_TIME() {
		return CTRL_TIME; 
	}
	public void Update_Flight_CTRL(boolean ctrl_on, double[] x, double t, double ctr_init_x, double ctr_init_y, int ctrl_curve, double ctrl_dt, double tvc_was, double CTRL_Thrust, boolean engine_loss_detected) {
		this.ctrl_on 	  = ctrl_on;
		if(this.ctrl_ID==0) {this.ctrl_on=false;} // overwrite : controller off with no FC set
		this.y_is 		  = x[4];
        this.ctr_init_x   = ctr_init_x;
        this.ctr_init_y   = ctr_init_y;
		this.ctrl_curve   = ctrl_curve; 
		this.ctrl_dt	  = ctrl_dt; 
		this.tvc_was      = tvc_was; 
		if(t!=-1&&tswitch) {tzero=t;tswitch=false;}
		CTRL_TIME = t-tzero; 
		this.x_is 		  = CTRL_TIME;
		if(this.CTRL_Thrust>CTRL_Thrust&&engine_loss_detected==false) {engine_lost=true; 	} 
		else if(engine_loss_detected) {engine_lost=true;}
		this.CTRL_Thrust=CTRL_Thrust;
	}
	public void set_ctrl_on(boolean NewValue){
		ctrl_on = NewValue;
	}
	public void set_ctrl_curve(int NewValue) {
		ctrl_curve = NewValue;
	}
	public void set_ctrl_dt(double NewValue) {
		ctrl_dt = NewValue; 
	}	
	public double get_ctr_end_y() {
		return ctr_end_y; 
	}
	public boolean get_engine_lost() {
		return engine_lost;
	}
}