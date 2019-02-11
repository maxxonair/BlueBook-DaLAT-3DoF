package Model;

import static java.lang.Math.*;


public class GravityModel  {

	
	   public static double[][] DATA_GRAVITY = { // RM (average radius) [m] || Gravitational Parameter [m3/s2] || Rotational speed [rad/s] || Average Collision Diameter [m]
				{1.9554537E-3,0,0}, 						// Earth
				{9.0880835E-5,0,0},				// Moon (Earth) GRAIL data
				{0,0,0},									// Mars
				{0,0,0,0},											// Venus
		};
	
	
	static double       J2    = 0;             // First Jeofreys? Constant                 [??]
    static double       J3    = 0;                        // Second ...
    static double       J4    = 0;                        // Third ...
    
	public static void SET_Constants(int TARGET){
	    J2   = DATA_GRAVITY[TARGET][0];    	// Average collision diameter (CO2)         [m]
	    J3   = DATA_GRAVITY[TARGET][1];    	// Standard gravitational constant (Mars)   [m3/s2]
	    J4   = DATA_GRAVITY[TARGET][2];    	// Planets average radius                   [m]
}
    
    public static double get_gr(double r, double lat, double rm, double mu, int TARGET)                          // Gravity acceleration in radial direction [m/s�]
    {
    	SET_Constants(TARGET);
    	
    double phi = PI/2-lat;             // Phi                                      [rad]
    double gr;                    // Gravitational acceleration in radial -> _r and north-south -> _n direction
    gr = mu * ( 1 - 1.5 * J2 * ( 3 * cos(phi) * cos(phi) - 1) * (rm/r) * (rm/r) - 2 * J3 * cos(phi) * (5 * cos(phi) * cos(phi) - 3) * (rm/r) * (rm/r) * (rm/r) - (5/8) * J4 * (35 * cos(phi) * cos(phi) * cos(phi) * cos(phi) - 30 * cos(phi) * cos(phi) + 3) * (rm/r) * (rm/r) * (rm/r) * (rm/r) )/(r * r);
    //System.out.println(J2 + "|" + J3 + "|" + J4 + "|" + mu + "|" + rm+ "|" + gr);
    return gr;
    }
    public static double get_gn(double r, double lat, double rm,double mu, int TARGET)        // Gravity in north-south direction [m/s�]
    {
    	SET_Constants(TARGET);
    double phi = PI/2-lat;             // Phi                                      [rad]
    double gn;                    // Gravitational acceleration in radial -> _r and north-south -> _n direction
    gn = -3 * mu * sin(phi) * cos(phi) * (rm/r) * (rm/r) * (J2 + 0.5 * J3 * ( 5*cos(phi) *cos(phi) -1) * (rm/r)/cos(phi) + (5/6) * J4 * ( 7 * cos(phi) * cos(phi) - 1) * (rm/r) * (rm/r) ) /(r * r);
    return gn;
    }
    public static double get_gw(double r, double lat, double rm,double mu, int TARGET)
    {
    	SET_Constants(TARGET);
       double gw = 0;
        return gw;
    }
    
}