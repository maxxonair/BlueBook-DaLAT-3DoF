package Simulator_main; 

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import Controller.PID_01;
import Model.AtmosphereModel;
import Model.GravityModel;
import Model.atm_dataset;

public class EquationsOfMotion_3DOF implements FirstOrderDifferentialEquations {
	//----------------------------------------------------------------------------------------------------------------------------
	//............................................                                       .........................................
	//
    //	                                                         Constants
	//
	//----------------------------------------------------------------------------------------------------------------------------
	   public static double[][] DATA_MAIN = { // RM (average radius) [m] || Gravitational Parameter [m3/s2] || Rotational speed [rad/s] || Average Collision Diameter [m]
				{6371000,3.9860044189E14,7.2921150539E-5,1.6311e-9}, 	// Earth
				{1737000,4903E9,2.661861E-6,320},						// Moon (Earth)
				{3389500,4.2838372E13,7.0882711437E-5,1.6311e-9},		// Mars
				{0,0,0,0},												// Venus
		};
	   	public static String[] str_target = {"Earth","Moon","Mars", "Venus"};
	  //----------------------------------------------------------------
		   public static double input_cmd=0;
		   public static double tminus=0;
		   public static double cmd_min = 0;
		   public static double cmd_max = 20000;
		   public static boolean cntrl_on = false; 
	  //----------------------------------------------------------------
			public static double Lt = 0;    		// Average collision diameter (CO2)         [m]
			public static double mu    = 0;    	    // Standard gravitational constant (Mars)   [m3/s2]
			public static double rm    = 0;    	    // Planets average radius                   [m]
			public static double omega = 0 ;        // Planets rotational rate                  [rad/sec]
			public static double g0 = 9.81;         // For normalized ISP 			
			public static double gn = 0;
			public static double gr = 0;
			public static double D = 0;
			public static double L = 0;
			public static double Ty = 0; 
		    public static double qinf=0;
		    public static double S =0;
		    public static double bank =0;
		    public static double CdPar=0;
		    public static double Thrust=0;
		    public static double Cl=0;
		    public static double Cd=0;
		    public static double Spar=0;
		    public static double rho =0;
		    public static double gamma =0;
		    public static double R=0;
		    public static double Ma=0;
		    public static double T=0;
		    public static double CdC=0;
		    public static double ISP = 10 ; 
		    public static double P = 0 ;      // static pressure [Pa]
		    public static double cp = 0;
		    public static int flowzone=0; // Flow zone continuum - transitional - free molecular flwo
		    public static double Cdm = 0; // Drag coefficient in contiuum flow; 
		    public static int TARGET=0;
	        private static List<atm_dataset> ATM_DATA = new ArrayList<atm_dataset>(); 
    
	    public int getDimension() {
	        return 7;
	    }
	    
		public static void SET_Constants(int TARGET) throws IOException{
		    Lt    = DATA_MAIN[TARGET][3];    	// Average collision diameter (CO2)         [m]
		    mu    = DATA_MAIN[TARGET][1];    	// Standard gravitational constant (Mars)   [m3/s2]
		    rm    = DATA_MAIN[TARGET][0];    	// Planets average radius                   [m]
		    omega = DATA_MAIN[TARGET][2];		// Planets roational speed     				[rad/s]
		    //--------------------------------------------------------------------
		    // 			Write env.inp : 
			 PrintWriter writer = new PrintWriter(new File("env.inp"), "UTF-8");
			for (int i = 0; i < 6; i++) {
				if(i==0) {
				writer.println(""+TARGET);
				} else if (i<5){
				writer.println(""+DATA_MAIN[TARGET][i-1]);
				}
			}
			writer.close();
		}

	

    public void computeDerivatives(double t, double[] x, double[] dxdt) {
    	gr = GravityModel.get_gr( x[2],  x[1],  rm,  mu, TARGET);
    	gn = GravityModel.get_gn(x[2], x[1],  rm,  mu, TARGET); 
    	Thrust =0 ; 
    	if(cntrl_on) {
    	Thrust = PID_01.FlightController_001(input_cmd, x[2]-rm,1, cmd_max, cmd_min);
    	} else {
    	Thrust = 0; 
    	}
    	System.out.println("Altitude: "+ (x[2]-rm));
    	if (x[2]-rm>200000 || TARGET == 1){
	    		rho = 0; 
	    		qinf= 0;
	    		T= 0 ; 
	    		gamma = 0 ; 
	    		R= 0; 
	    		Ma = 0; 
	       //-----------------------------------------------------------------------------------------------
	      	  D  =  - Thrust;            // Aerodynamic drag Force [N]
	      	  L  =        0 ;                            // Aerodynamic lift Force [N]
	      	  Ty =        0 ;                            // Aerodynamic side Force [N]
	      	//----------------------------------------------------------------------------------------------
    	} else {
	    	 rho    = AtmosphereModel.atm_read(1, x[2] - rm ) ;                    	             // density                         [kg/m≥]
	    	 qinf   = 0.5 * rho * ( x[3] * x[3]) ;               		         // Dynamic pressure                [Pa]
	    	 T      = AtmosphereModel.atm_read(2, x[2] - rm) ;                   		         // Temperature                     [K]
	    	 gamma  = AtmosphereModel.atm_read(4, x[2] - rm) ;              	                 //
	    	 R      = AtmosphereModel.atm_read(3,  x[2] - rm ) ;                                 // Gas Constant                    [Si]
	    	 P      = rho * R * T;
	    	 Ma     = x[3] / Math.sqrt( T * gamma * R);                  		 // Mach number                     [-]
    	     //CdPar  = load_Cdpar( x[3], qinf, Ma, x[2] - rm);   		             // Parachute Drag coefficient      [-]
    	     // Thrust = load_Thrust(x[3], qinf, Ma, x[2] - rm, Th_t1, x[6] );       // Thrust                          [N]
    	     CdC    = AtmosphereModel.get_CdC(x[2]-rm,0);                       // Continuum flow drag coefficient [-]
	    	 Cd 	= AtmosphereModel.load_Drag(x[3], x[2]-rm, P, T, CdC, Lt, R);    // Lift coefficient                [-]
	    	 S 		= 4;	
	     	//-----------------------------------------------------------------------------------------------
	    	  D  = - qinf * S * Cd  - Thrust ;//- qinf * Spar * CdPar;        // Aerodynamic drag Force [N]
	    	  L  =   qinf * S * Cl * cos( bank ) ;                            // Aerodynamic lift Force [N]
	    	  Ty =   qinf * S * Cl * sin( bank ) ;                            // Aerodynamic side Force [N]
	    	//----------------------------------------------------------------------------------------------
	    	  // System.out.println(x[2]+"|"+rm+"|"+(x[2]-rm)+"|"+rho+"|"+D);
	    	  // S      =  x[6] / ( 1.55 * read_data(file_cship,1));
	    	  flowzone = AtmosphereModel.calc_flowzone(x[3], x[2]-rm, P, T, Lt);
    	}
    	
    dxdt[0] = x[3] * cos( x[4] ) * sin( x[5] ) / ( x[2] * cos( x[1] ) );
    dxdt[1] = x[3] * cos( x[4] ) * cos( x[5] ) / x[2] ;
    dxdt[2] = x[3] * sin( x[4] );

    dxdt[3] = -gr * sin( x[4] ) + gn * cos( x[5] ) * cos( x[4] ) + D / x[6] + omega * omega * x[2] * cos( x[1] ) * ( sin( x[4] ) * cos( x[1] ) - cos( x[1] ) * cos( x[5] ) * sin( x[1] ) ) ;
    dxdt[4] = ( x[3] / x[2] - gr/ x[3] ) * cos( x[4] ) - gn * cos( x[5] ) * sin( x[4] ) / x[3] + L / ( x[3] * x[6] ) + 2 * omega * sin( x[5] ) * cos( x[1] ) + omega * omega * x[2] * cos( x[1] ) * ( cos( x[4] ) * cos( x[1] ) + sin( x[4] ) * cos( x[5] ) * sin( x[1] ) ) / x[3] ;
    dxdt[5] = x[3] * sin( x[5] ) * tan( x[1] ) * cos( x[4] ) / x[2] - gn * sin( x[5] ) / x[3] - Ty / ( x[3] - cos( x[4] ) * x[6] ) - 2 * omega * ( tan( x[4] ) * cos( x[5] ) * cos( x[1] ) - sin( x[1] ) ) + omega * omega * x[2] * sin( x[5] ) * sin( x[1] ) * cos( x[1] ) / ( x[3] * cos( x[4] ) ) ;

    dxdt[6] = - Thrust/(ISP*g0) ;                    // EDL vehicle mass [kg]
}
    
public static void Launch_Integrator( int INTEGRATOR, int target, double x0, double x1, double x2, double x3, double x4, double x5, double x6, double t){
//----------------------------------------------------------------------------------------------
		try {
			SET_Constants(target);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
//----------------------------------------------------------------------------------------------
		FirstOrderIntegrator dp853;
		if (INTEGRATOR == 1) {
	         dp853 = new ClassicalRungeKuttaIntegrator(1.0E-3);
		} else if (INTEGRATOR == 0) {
	         dp853 = new DormandPrince853Integrator(1.0e-8, 1.0, 1.0e-10, 1.0e-10);
		} else if (INTEGRATOR ==2){
			double minStep = 1.0e-8;
			double maxStep = 1.0; 
			double scalAbsoluteTolerance = 1.0e-10;
			double scalRelativeTolerance = 1.0e-10;
			dp853 = new GraggBulirschStoerIntegrator( minStep,  maxStep,  scalAbsoluteTolerance,  scalRelativeTolerance);
		} else if (INTEGRATOR == 3){
			int nSteps = 100; 
			double minStep = 1.0e-8;
			double maxStep = 1.0; 
			double scalAbsoluteTolerance = 1.0e-10;
			double scalRelativeTolerance = 1.0e-10;
			dp853 = new AdamsBashforthIntegrator( nSteps,  minStep,  maxStep,  scalAbsoluteTolerance,  scalRelativeTolerance);
		} else {
			// Default Value
			 dp853 = new DormandPrince853Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);
		}
//----------------------------------------------------------------------------------------------
	        FirstOrderDifferentialEquations ode = new EquationsOfMotion_3DOF();
	        //------------------------------
	        ATM_DATA.removeAll(ATM_DATA);
	        try {
				ATM_DATA = AtmosphereModel.INITIALIZE_ATM_DATA(target);
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
  			for (int i = 0;i<ATM_DATA.size();i++){
				//System.out.println(ATM_DATA.get(i).get_altitude() + " | " + ATM_DATA.get(i).get_density());
			}
//----------------------------------------------------------------------------------------------
  			// 						Result vector setup - do not touch
	        double[] y = new double[7]; // Result vector
	// Position 
	        y[0] = x0;
	        y[1] = x1;
	        y[2] = x2;
	// Velocity
	        y[3] = x3;
	        y[4] = x4;
	        y[5] = x5;
	// S/C Mass        
	        y[6] = x6;
//----------------------------------------------------------------------------------------------
	        StepHandler stepHandler = new StepHandler() {

	            ArrayList<String> steps = new ArrayList<String>();

	            public void init(double t0, double[] y0, double t) {

	            }
	            

	            public void handleStep(StepInterpolator interpolator, boolean isLast) {
	                double   t = interpolator.getCurrentTime();
	                double[] y = interpolator.getInterpolatedState();
	                double[] ymo = interpolator.getInterpolatedDerivatives();
	                double g_total = Math.sqrt(gr*gr+gn*gn);
	                double E_total = y[6]*(g_total*(y[2]-rm)+0.5*y[3]*y[3]);
	                if( t > steps.size() )
	
	                    steps.add(t + " " + y[0] + " " + y[1] + " " + y[2] + " " + y[3]+ " " + y[4] + " " + y[5] + " " + rho + " " + D + " " +L + " " +Ty + " " +gr + " " +gn + " " +g_total + " " +T+ " " +Ma+ " " +cp+ " " +R+ " " +P+ " " +Cd+ " " +Cl+ " " +bank+ " " +flowzone+ " " +qinf+ " " +CdC+ " " +Thrust+ " " +Cdm+ " " +y[6]+ " " +ymo[3]/9.81+ " " +E_total);
	//System.out.println(t + "  " + y[2] + "  |  " + y[3] +" | " + (t-steps.size()));
	                if(isLast) {
	                    try{
	        	        	//DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	        	        	//Date date = new Date();
	        	            String time = "" ;//+ dateFormat.format(date) ; 
	                        PrintWriter writer = new PrintWriter(new File("results" + time + ".txt"), "UTF-8");
	                        for(String step: steps) {
	                            writer.println(step);
	                        }
	                        writer.close();
	                    } catch(Exception e) {};
	                }
	            }
	            
	        };
	        
	        EventHandler EventHandler = new EventHandler() {
				@Override
				public double g(double t, double[] y) {
					// TODO Auto-generated method stub
					return y[2] - rm; // Altitude = 0 -> integration stop 
					//return 0;
				}


				public Action eventOccurred(double t, double[] y, boolean increasing) {
					  return Action.STOP;
					}


				@Override
				public void init(double arg0, double[] arg1, double arg2) {
					// TODO Auto-generated method stub
					
				}


				@Override
				public void resetState(double arg0, double[] arg1) {
					// TODO Auto-generated method stub
					
				}
	        	
	        };
	        dp853.addStepHandler(stepHandler);
	        dp853.addEventHandler(EventHandler,1,1.0e-3,30);
	        dp853.integrate(ode, 0.0, y, t, y);

	       System.out.println("Sucess. Integrator stop. ");      
		}
	

}