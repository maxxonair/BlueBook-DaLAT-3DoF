package Simulator_main;

import Model.Atmosphere;

public class CoordinateTransformation {

	
	public  double[][] C_A2NED 	  = {{0,0,0},{0,0,0},{0,0,0}}; 			// Rotational matrix Aerodynamic to NED system			// Rotational matrix body fixed to NED system
	public  double[][] C_ECEF2NED = {{0,0,0},{0,0,0},{0,0,0}}; 			// Rotational matrix ECEF to NED system
	public  double[][] C_A2B 	  = {{0,0,0},{0,0,0},{0,0,0}}; 			// Rotational matrix Bodyfixed to Aerodynamic
	public  double[][] C_ECEF2ECI = {{0,0,0},{0,0,0},{0,0,0}}; 
	public  double[][] C_ECI2ECEF = {{0,0,0},{0,0,0},{0,0,0}}; 			// Rotational matrix Bodyfixed to Aerodynamic
	public  double[][] C_GC2NED   = {{0,0,0},{0,0,0},{0,0,0}};
	public  double[][] C_GC2B 	  = {{0,0,0},{0,0,0},{0,0,0}};
	public  double[][] C_NED2B 	  = {{0,0,0},{0,0,0},{0,0,0}};
	public  double[][] C_B2NED 	  = {{0,0,0},{0,0,0},{0,0,0}};
	
	
	public CoordinateTransformation() {
		super();
	}


	public  void initializeTranformationMatrices(double[] x, double t, double omega, Atmosphere atmosphere, double[][] euler_angle, 
			double[][] q_vector, double[] r_ECEF_spherical,double[] V_NED_ECEF_spherical) {
		//-------------------------------------------------------------------------------------------
		//              Aerodynamic frame to North-East-Down
		//-------------------------------------------------------------------------------------------
		C_A2NED[0][0] =  Math.cos(V_NED_ECEF_spherical[2])*Math.cos(V_NED_ECEF_spherical[1]);
		C_A2NED[1][0] =  Math.sin(V_NED_ECEF_spherical[2])*Math.cos(V_NED_ECEF_spherical[1]);
		C_A2NED[2][0] = -Math.sin(V_NED_ECEF_spherical[1]);
		
		C_A2NED[0][1] = -Math.sin(V_NED_ECEF_spherical[1])*Math.cos(atmosphere.getBankAngle()) - Math.cos(V_NED_ECEF_spherical[2])*Math.sin(V_NED_ECEF_spherical[1])*Math.sin(atmosphere.getBankAngle());
		C_A2NED[1][1] =  Math.cos(V_NED_ECEF_spherical[2])*Math.cos(atmosphere.getBankAngle()) - Math.sin(V_NED_ECEF_spherical[2])*Math.sin(V_NED_ECEF_spherical[1])*Math.sin(atmosphere.getBankAngle());
		C_A2NED[2][1] = -Math.cos(V_NED_ECEF_spherical[1])*Math.sin(atmosphere.getBankAngle());
		
		C_A2NED[0][2] = -Math.sin(V_NED_ECEF_spherical[2])*Math.sin(atmosphere.getBankAngle()) + Math.cos(V_NED_ECEF_spherical[2])*Math.sin(V_NED_ECEF_spherical[1])*Math.cos(atmosphere.getBankAngle());
		C_A2NED[1][2] =  Math.cos(V_NED_ECEF_spherical[2])*Math.sin(atmosphere.getBankAngle()) + Math.sin(V_NED_ECEF_spherical[2])*Math.sin(V_NED_ECEF_spherical[1])*Math.cos(atmosphere.getBankAngle());
		C_A2NED[2][2] =  Math.cos(V_NED_ECEF_spherical[1])*Math.cos(atmosphere.getBankAngle());
		//-------------------------------------------------------------------------------------------
		//             Body fixed frame to North-East-Down
		//-------------------------------------------------------------------------------------------
		// Euler Angle Representation: 
		/*
		C_NED2B[0][0] =  Math.cos(euler_angle[2])*Math.cos(euler_angle[1]); 
		C_NED2B[1][0] =  Math.sin(euler_angle[2])*Math.cos(euler_angle[1]);
		C_NED2B[2][0] = -Math.sin(euler_angle[1]);
		
		C_NED2B[0][1] = Math.cos(euler_angle[2])*Math.sin(euler_angle[1])*Math.sin(euler_angle[0])-Math.sin(euler_angle[2])*Math.cos(euler_angle[0]); 
		C_NED2B[1][1] = Math.cos(euler_angle[2])*Math.cos(euler_angle[0])+Math.sin(euler_angle[2])*Math.sin(euler_angle[1])*Math.sin(euler_angle[0]);
		C_NED2B[2][1] = Math.cos(euler_angle[1])*Math.sin(euler_angle[0]);
		
		C_NED2B[0][2] = Math.sin(euler_angle[2])*Math.sin(euler_angle[0])+Math.cos(euler_angle[2])*Math.sin(euler_angle[1])*Math.cos(euler_angle[0]); 
		C_NED2B[1][2] = Math.sin(euler_angle[2])*Math.sin(euler_angle[1])*Math.cos(euler_angle[0])-Math.cos(euler_angle[2])*Math.sin(euler_angle[0]);
		C_NED2B[2][2] = Math.cos(euler_angle[1])*Math.cos(euler_angle[0]);
		*/

		//-----------------------------------------------------------------------
		// Quaternion Representation: 
		
		C_NED2B[0][0] =    (q_vector[0][0]*q_vector[0][0] + q_vector[1][0]*q_vector[1][0] - q_vector[2][0]*q_vector[2][0] - q_vector[3][0]*q_vector[3][0]); 
		C_NED2B[1][0] =  2*(q_vector[1][0]*q_vector[2][0] - q_vector[0][0]*q_vector[3][0]);
		C_NED2B[2][0] =  2*(q_vector[1][0]*q_vector[3][0] + q_vector[0][0]*q_vector[2][0]);
	
		C_NED2B[0][1] =  2*(q_vector[1][0]*q_vector[2][0] + q_vector[0][0]*q_vector[3][0]); 
		C_NED2B[1][1] =    (q_vector[0][0]*q_vector[0][0] - q_vector[1][0]*q_vector[1][0] + q_vector[2][0]*q_vector[2][0] - q_vector[3][0]*q_vector[3][0]);
		C_NED2B[2][1] =  2*(q_vector[2][0]*q_vector[3][0] - q_vector[0][0]*q_vector[1][0]);
		
		C_NED2B[0][2] =  2*(q_vector[1][0]*q_vector[3][0] - q_vector[0][0]*q_vector[2][0]); 
		C_NED2B[1][2] =  2*(q_vector[0][0]*q_vector[1][0] + q_vector[2][0]*q_vector[3][0]);
		C_NED2B[2][2] =    (q_vector[0][0]*q_vector[0][0] - q_vector[1][0]*q_vector[1][0] - q_vector[2][0]*q_vector[2][0] + q_vector[3][0]*q_vector[3][0]);
		
		
		//C_GC2B = Mathbox.Multiply_Matrices(C_GC2NED, C_NED2B);
		
		C_B2NED[0][0] =    (q_vector[0][0]*q_vector[0][0] + q_vector[1][0]*q_vector[1][0] - q_vector[2][0]*q_vector[2][0] - q_vector[3][0]*q_vector[3][0]); 
		C_B2NED[1][0] =  2*(q_vector[1][0]*q_vector[2][0] + q_vector[0][0]*q_vector[3][0]);
		C_B2NED[2][0] =  2*(q_vector[1][0]*q_vector[3][0] - q_vector[0][0]*q_vector[2][0]);
	
		C_B2NED[0][1] =  2*(q_vector[1][0]*q_vector[2][0] - q_vector[0][0]*q_vector[3][0]); 
		C_B2NED[1][1] =    (q_vector[0][0]*q_vector[0][0] - q_vector[1][0]*q_vector[1][0] + q_vector[2][0]*q_vector[2][0] - q_vector[3][0]*q_vector[3][0]);
		C_B2NED[2][1] =  2*(q_vector[2][0]*q_vector[3][0] + q_vector[0][0]*q_vector[1][0]);
		
		C_B2NED[0][2] =  2*(q_vector[1][0]*q_vector[3][0] + q_vector[0][0]*q_vector[2][0]); 
		C_B2NED[1][2] =  2*(q_vector[2][0]*q_vector[3][0] - q_vector[0][0]*q_vector[1][0]);
		C_B2NED[2][2] =    (q_vector[0][0]*q_vector[0][0] - q_vector[1][0]*q_vector[1][0] - q_vector[2][0]*q_vector[2][0] + q_vector[3][0]*q_vector[3][0]);
		//-------------------------------------------------------------------------------------------
		//             ECEF frame to North-East-Down
		//-------------------------------------------------------------------------------------------
		C_ECEF2NED[0][0] = -Math.cos(r_ECEF_spherical[0])*Math.sin(r_ECEF_spherical[1]);
		C_ECEF2NED[1][0] = -Math.sin(r_ECEF_spherical[0]);
		C_ECEF2NED[2][0] = -Math.cos(r_ECEF_spherical[0])*Math.cos(r_ECEF_spherical[1]);
		
		C_ECEF2NED[0][1] = -Math.sin(r_ECEF_spherical[0])*Math.sin(r_ECEF_spherical[1]);
		C_ECEF2NED[1][1] =  Math.cos(r_ECEF_spherical[0]);
		C_ECEF2NED[2][1] = -Math.sin(r_ECEF_spherical[0])*Math.cos(r_ECEF_spherical[1]);
		
		C_ECEF2NED[0][2] =  Math.cos(r_ECEF_spherical[1]);
		C_ECEF2NED[1][2] =  0;
		C_ECEF2NED[2][2] = -Math.sin(r_ECEF_spherical[1]);
		//-------------------------------------------------------------------------------------------
		//             Bodyfixed frame to Aerodynamic frame
		//-------------------------------------------------------------------------------------------
		C_A2B[0][0] =  Math.cos(atmosphere.getAngleOfAttack())*Math.cos(atmosphere.getAngleOfSideslip());
		C_A2B[1][0] =  Math.sin(atmosphere.getAngleOfSideslip());
		C_A2B[2][0] =  Math.sin(atmosphere.getAngleOfAttack())*Math.cos(atmosphere.getAngleOfSideslip());
		
		C_A2B[0][1] =  -Math.cos(atmosphere.getAngleOfAttack())*Math.sin(atmosphere.getAngleOfSideslip());
		C_A2B[1][1] =   Math.cos(atmosphere.getAngleOfSideslip());
		C_A2B[2][1] =  -Math.sin(atmosphere.getAngleOfAttack())*Math.sin(atmosphere.getAngleOfSideslip());
		
		C_A2B[0][2] =  -Math.sin(atmosphere.getAngleOfAttack());
		C_A2B[1][2] =  0;
		C_A2B[2][2] =   Math.cos(atmosphere.getAngleOfAttack());
		//-------------------------------------------------------------------------------------------
		//             ECEF to ECI
		//-------------------------------------------------------------------------------------------
		C_ECEF2ECI[0][0] = Math.cos(omega*t);
		C_ECEF2ECI[1][0] = Math.sin(omega*t);
		C_ECEF2ECI[2][0] = 0;
		
		C_ECEF2ECI[0][1] = -Math.sin(omega*t);
		C_ECEF2ECI[1][1] =  Math.cos(omega*t);
		C_ECEF2ECI[2][1] = 0;
		
		C_ECEF2ECI[0][2] = 0;
		C_ECEF2ECI[1][2] = 0;
		C_ECEF2ECI[2][2] = 1;
		//-------------------------------------------------------------------------------------------
		//             ECI to ECEF
		//-------------------------------------------------------------------------------------------
		C_ECI2ECEF[0][0] =  Math.cos(omega*t);
		C_ECI2ECEF[1][0] = -Math.sin(omega*t);
		C_ECI2ECEF[2][0] = 0;
		
		C_ECI2ECEF[0][1] =  Math.sin(omega*t);
		C_ECI2ECEF[1][1] =  Math.cos(omega*t);
		C_ECI2ECEF[2][1] = 0;
		
		C_ECI2ECEF[0][2] = 0;
		C_ECI2ECEF[1][2] = 0;
		C_ECI2ECEF[2][2] = 1;
		//-------------------------------------------------------------------------------------------
		//             B 2 NED
		//-------------------------------------------------------------------------------------------
		//C_B2NED = Mathbox.Multiply_Matrices(C_ECEF2NED, Mathbox.Multiply_Matrices(C_ECI2ECEF, C_B2NED));
	}


	public  double[][] getC_A2NED() {
		return C_A2NED;
	}




	public  double[][] getC_ECEF2NED() {
		return C_ECEF2NED;
	}


	public  double[][] getC_A2B() {
		return C_A2B;
	}


	public  double[][] getC_ECEF2ECI() {
		return C_ECEF2ECI;
	}


	public  double[][] getC_ECI2ECEF() {
		return C_ECI2ECEF;
	}


	public  double[][] getC_GC2NED() {
		return C_GC2NED;
	}


	public  double[][] getC_GC2B() {
		return C_GC2B;
	}


	public  double[][] getC_NED2B() {
		return C_NED2B;
	}
	
	public  double[][] getC_B2NED() {
		return C_B2NED;
	}	
}