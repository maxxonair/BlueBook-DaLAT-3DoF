//	FLIGHT.m --  Generic 6-DOF Trim, Linear Model, and Flight Path Simulation
//   Euler Angle/Quaternion Option for Rotation (Direction Cosine) Matrix
//   MAE 331 Homework 7 Edition --- Input Spec Altered

//	December 12, 2018  
//	===============================================================
//	Copyright 2006-2018 by ROBERT F. STENGEL.  All rights reserved.

	clear
	global GEAR CONHIS SPOIL u x V uInc tuHis deluHis TrimHist SMI MODEL RUNNING

    disp('**=========================**')
    disp('** 6-DOF FLIGHT Simulation **')
    disp('**=========================**')
    disp(' ')
    disp(['Date and Time are ', num2str(datestr(now))]);
    disp('===============================')

    
//	This is the SCRIPT FILE.  It contains the Main Program, which:
//		Defines initial conditions
//		Contains aerodynamic data tables (if required)
//		Calculates longitudinal trim condition
//		Calculates stability-and-control matrices for linear model
//		Simulates flight path using nonlinear equations of motion
		
//	Functions used by FLIGHT:
//		AeroModelAlpha.m    High-Alpha, Low-Mach aerodynamic coefficients of the aircraft, 
//                           thrust model, and geometric and inertial properties
//		AeroModelMach.m     Low-Alpha, High-Mach aerodynamic coefficients of the aircraft, 
//                           thrust model, and geometric and inertial properties
//       AeroModelUser.m     User-defined aerodynamic coefficients of the aircraft, 
//                           thrust model, and geometric and inertial properties
//		Atmos.m             Air density, sound speed
//		DCM.m               Direction-cosine (Rotation) matrix from Euler Angles
//       RMQ.m               Direction-cosine (Rotation) matrix from Quaternions
//		EoM.m               Equations of motion for integration (Euler Angles)
//		EoMQ.m              Equations of motion for integration (Quaternions)
//		LinModel.m          Equations of motion for defining linear-model
//                           (F & G) matrices via central differences
//		TrimCost.m          Cost function for trim solution
//		WindField.m         Wind velocity components

//	DEFINITION OF THE STATE VECTOR
//   With Euler Angle DCM option (QUAT = 0):
//		x(1)    = 		Body-axis x inertial velocity, ub, m/s
//		x(2)    =		Body-axis y inertial velocity, vb, m/s
//		x(3)    =		Body-axis z inertial velocity, wb, m/s
//		x(4)    =		North position of center of mass WRT Earth, xe, m
//		x(5)    =		East position of center of mass WRT Earth, ye, m
//		x(6)    =		Negative of c.m. altitude WRT Earth, ze = -h, m
//		x(7)    =		Body-axis roll rate, pr, rad/s
//		x(8)    =		Body-axis pitch rate, qr, rad/s
//		x(9)    =		Body-axis yaw rate, rr,rad/s
//		x(10)   =		Roll angle of body WRT Earth, phir, rad
//		x(11)   =		Pitch angle of body WRT Earth, thetar, rad
//		x(12)   =		Yaw angle of body WRT Earth, psir, rad
//   With Quaternion DCM option (QUAT = 1):
//		x(1)    = 		Body-axis x inertial velocity, ub, m/s
//		x(2)    =		Body-axis y inertial velocity, vb, m/s
//		x(3)    =		Body-axis z inertial velocity, wb, m/s
//		x(4)    =		North position of center of mass WRT Earth, xe, m
//		x(5)    =		East position of center of mass WRT Earth, ye, m
//		x(6)    =		Negative of c.m. altitude WRT Earth, ze = -h, m
//		x(7)    =		Body-axis roll rate, pr, rad/s
//		x(8)    =		Body-axis pitch rate, qr, rad/s
//		x(9)    =		Body-axis yaw rate, rr,rad/s
//		x(10)   =		q1, x Component of quaternion
//		x(11)   =		q2, y Component of quaternion
//		x(12)   =		q3, z Component of quaternion
//		x(13)   =		q4, cos(Euler) Component of quaternion
	
//	DEFINITION OF THE CONTROL VECTOR
//		u(1)    = 		Elevator, dEr, rad, positive: trailing edge down
//		u(2)    = 		Aileron, dAr, rad, positive: left trailing edge down
//		u(3)    = 		Rudder, dRr, rad, positive: trailing edge left
//		u(4)    = 		Throttle, dT, //
//		u(5)    =		Asymmetric Spoiler, dASr, rad
//		u(6)    =		Flap, dFr, rad
//		u(7)    =		Stabilator, dSr, rad

//   ======================================================================
//	USER INPUTS
//   ===========
//   --  Flags define which analyses or IC/Inputs will be engaged
//	======================================================
//	FLIGHT Flags (1 = ON, 0 = OFF)

    MODEL   =   2;      // Aerodynamic model selection
                        // 0: Incompressible flow, high angle of attack
                        // 1: Compressible flow, low angle of attack
                        // 2: User-Defined model
    QUAT    =   0;      // 0: Rotation Matrix (DCM) from Euler Angles
                        // 1: Rotation Matrix (DCM) from Quaternion
	TRIM    = 	1;		// Trim flag (= 1 to calculate trim @ I.C.)
	LINEAR  = 	1;		// Linear model flag (= 1 to calculate and store F and G)
	SIMUL   =	1;		// Flight path flag (= 1 for nonlinear simulation)
	CONHIS  =	1;		// Control history ON (= 1) or OFF (= 0)
	RUNNING =   0;      // internal flag, -
    
	GEAR    = 	0;		// Landing gear DOWN (= 1) or UP (= 0)
	SPOIL   =	0;		// Symmetric Spoiler DEPLOYED (= 1) or CLOSED (= 0)
	dF      = 	0;		// Flap setting, deg
    
    disp('  ')
    disp(['MODEL = ',num2str(MODEL),'   QUAT = ',num2str(QUAT)])
    disp(['TRIM =  ',num2str(TRIM),'   LINEAR = ',num2str(LINEAR)])
    disp(['SIMUL = ',num2str(SIMUL)])
    disp('  ')
    if MODEL == 0
        disp('[AeroModel = AeroModelAlpha]')
    end
    if MODEL == 1
        disp('[AeroModel = AeroModelMach]')
    end
    if MODEL == 2
        disp('[AeroModel = AeroModelUser]')
    end
    
//   Starting and Final Times for Simulation
//   =======================================
	ti      = 	0;      // Initial time for simulation, sec
	tf      =	60;    // Final time for simulation, sec
    disp('  ')
    disp('Simulation Interval')
    disp(['Initial Time = ',num2str(ti),' s, Final Time = ',num2str(tf),' s'])
    
//	Initial Altitude (ft), Indicated Airspeed (kt)
//   ==============================================
//   These values specify the level-flight trim condition
//   They also specify initial conditions unless overwritten

//   HW 7 Specific I.C.
    
    MIC     =   0.75;    //   Initial Mach Number
    hm      =   10500;    //   Initial Altitude, m
    disp('  ')
    disp('HW Part 1')
    disp('=========')
    disp(['Mach = ',num2str(MIC),', Altitude, m = ',num2str(hm)])

    [airDens,airPres,temp,soundSpeed] = Atmos(hm);
    disp('  ')
    disp(['Air Density     = ',num2str(airDens),' kg/m^3, Air Pressure = ', ...
        num2str(airPres),' N/m^2'])
    disp(['Air Temperature = ',num2str(temp),' K,         Sound Speed  = ', ...
        num2str(soundSpeed),' m/s'])
    
    hft     =   hm/0.3048;       //   Initial Altitude, ft
    VIC     =   MIC*soundSpeed;  //   Initial True Airspeed, m/s
    VTASkt  =   VIC*1.94384;     //   Initial True Airspeed, kt
    VKIAS   =   VTASkt*sqrt(airDens/1.225);  //   Initial Indicated Airspeed, kt
    disp('  ')
    disp(['hft = ',num2str(hft),', True Airspeed, m/s = ',num2str(VIC)])
    disp(['VTASkt = ',num2str(VTASkt),', VKIAS = ',num2str(VKIAS)])  

//     hft         =   34450;   // Altitude above Sea Level, ft
//     VKIAS       =   244;     // Indicated Airspeed, kt
    
    hm          =   hft * 0.3048;    // Altitude above Sea Level, m
    VmsIAS      =   VKIAS * 0.514444;  // Indicated Airspeed, m/s
    disp('  ')
    disp('Initial Conditions')
    disp('==================')
    disp(['Altitude           = ',num2str(hft),' ft,   = ',num2str(hm),' m'])
    disp(['Indicated Airspeed = ',num2str(VKIAS),' kt, = ',num2str(VmsIAS),' m/s'])
    
//   US Standard Atmosphere, 1976, Table Lookup for I.C.    
    [airDens,airPres,temp,soundSpeed] = Atmos(hm);
    disp('  ')
    disp(['Air Density     = ',num2str(airDens),' kg/m^3, Air Pressure = ', ...
        num2str(airPres),' N/m^2'])
    disp(['Air Temperature = ',num2str(temp),' K,         Sound Speed  = ', ...
        num2str(soundSpeed),' m/s'])
        
//   Dynamic Pressure (N/m^2), and True Airspeed (m/s)
    qBarSL  =   0.5*1.225*VmsIAS^2;  // Dynamic Pressure at sea level, N/m^2
    V       =   sqrt(2*qBarSL/airDens);	// True Airspeed, TAS, m/s
    TASms   =   V;
    disp('  ')
    disp(['Dynamic Pressure = ',num2str(qBarSL),' N/m^2, True Airspeed = ', ...
        num2str(V),' m/s'])
    
    D2R =   pi/180;
    R2D =   180/pi;

//	Alphabetical List of Additional Initial Conditions
//   ==================================================
//   These valuees subsume the hft and VKIAS entered above

	alpha   =	0;      // Angle of attack, deg (relative to air mass)
	beta    =	0;      // Sideslip angle, deg (relative to air mass)
	dA      =	0;      // Aileron angle, deg
	dAS     =	0;      // Asymmetric spoiler angle, deg
	dE      =	0;      // Elevator angle, deg
	dR      =	0;      // Rudder angle, deg
	dS      = 	0;      // Stabilator setting, deg
	dT      = 	0;      // Throttle setting, // / 100
	hdot    =	0;      // Altitude rate, m/s
	p       =	0;      // Body-axis roll rate, deg/s
	phi     =	0;      // Body roll angle wrt earth, deg
	psi     =	0;      // Body yaw angle wrt earth, deg
	q       =	0;      // Body-axis pitch rate, deg/sec
	r       =	0;      // Body-axis yaw rate, deg/s
	SMI     =	0;      // Static margin increment due to center-of-mass variation 
                        // from reference, ///100    
	theta   =	alpha;  // Body pitch angle wrt earth, deg [theta = alpha if hdot = 0]
	xe      =	0;      // Initial longitudinal position, m
	ye      = 	0;      // Initial lateral position, m
	ze      = 	-hm;    // Initial vertical position, m [h: + up, z: + down]

//	Initial Conditions Derived from Prior Initial Conditions
	gamma	=	R2D * atan(hdot / sqrt(V^2 - hdot^2));
						// Inertial Vertical Flight Path Angle, deg
	qbar	= 	0.5 * airDens * V^2;	
						// Dynamic Pressure, N/m^2
	IAS		=	sqrt(2 * qbar / 1.225);
						// Indicated Air Speed, m/s
	Mach	= 	V / soundSpeed;	
						// Mach Number
	disp(['Mach number      = ',num2str(Mach), ...
        ', Flight Path Angle = ',num2str(gamma),' deg'])										
    disp('  ')
    uInc    =   [];
    
//   AeroModel Selected by MODEL Flag
//   ================================
    if MODEL == 0
        disp('<<Low-Mach, High-Alpha Model, AeroModelAlpha>>')
    end
    if MODEL == 1
        disp('<<High-Mach, Low-Alpha Aerodynamic Model, AeroModelMach>>')
    end
    if MODEL == 2
        disp('<<User-Defined Aerodynamic Model, AeroModelUser>>')
    end
    disp('  ======================================')
    
//   Test Inputs at Initial Condition
//   ================================
//	Initial Control Perturbation (Test Inputs: rad or percent)			
	delu	=	[0;0;0;0;0;0;0];
//	Initial State Perturbation (Test Inputs: m, m/s, rad, or rad/s)
	delx	=	[0;0;0
				0;0;0
                0;0;0
				0;0;0];
    disp(' ')
    disp('Initial Perturbations to Trim for Step Response')
    disp('===============================================')
        
    disp('Control Vector')
    disp('--------------')
    disp(['Elevator   = ',num2str(delu(1)),' rad, Aileron = ', ...
        num2str(delu(2)),' rad, Rudder = ',num2str(delu(3)),' rad'])
    disp(['Throttle   = ',num2str(delu(4)),' x 100//, Asymm Spoiler = ', ...
        num2str(delu(5)),' rad, Flap = ',num2str(delu(6)),' rad'])
    disp(['Stabilator = ',num2str(delu(7)),' rad'])

    disp('  ')
    disp('State Vector')
    disp('------------')
    disp(['u   = ',num2str(delx(1)),' m/s, v = ',num2str(delx(2)),' m/s, w = ', ...
        num2str(delx(3)),' m/s'])
    disp(['x   = ',num2str(delx(4)),' m, y = ',num2str(delx(5)),' m, z = ', ...
        num2str(delx(6)),' m'])
    disp(['p   = ',num2str(delx(7)),' rad/s, q = ',num2str(delx(8)),' rad/s, r = ', ...
        num2str(delx(9)),' rad/s'])
    disp(['Phi = ',num2str(delx(10)),' rad, Theta = ',num2str(delx(11)),' rad, Psi = ', ...
        num2str(delx(12)),' rad'])
            
//	Control Perturbation History (Test Inputs: rad or 100//)
//   =======================================================
//   Each control effector represented by a column
//	Each row contains control increment delta-u(t) at time t:
    disp('  ')
    disp('Control Vector Time History Table')
    disp('=================================')
    disp('Time, sec: t0, t1, t2, ...')
	tuHis	=	[0 0.5 0.501 100]
    
    disp('Columns:  Elements of the Control Vector')
    disp('Rows:     Value at time, t0, t1, ...')
	deluHis	=	[0 0.0174533 0 0 0 0 0 ...
				0 0.0174533 0 0 0 0 0 ...
			    0 0 0 0 0 0 0 ...
			    0 0 0 0 0 0 0]

//	State Vector and Control Initialization, rad
	phir	=	phi * D2R;
	thetar	=	theta * D2R;
	psir	=	psi * D2R;

	windb	=	WindField(-ze,phir,thetar,psir);
	alphar	=	alpha * D2R;
	betar	=	beta * D2R;

	x	=	[V * cos(alphar) * cos(betar) - windb(1)
			V * sin(betar) - windb(2)
			V * sin(alphar) * cos(betar) - windb(3)
			xe
			ye
			ze
			p * D2R
			q * D2R
			r * D2R
			phir
			thetar
			psir];
	
	u	=	[dE * D2R
			dA * D2R
			dR * D2R
			dT
			dAS * D2R
			dF * D2R
			dS * D2R];

//	Trim Calculation (for Steady Level Flight at Initial V and h)
//   =============================================================
//   Euler Angles used in trim calculation
//   Trim Parameter Vector (OptParam):
//		1 = Stabilator, rad
//		2 = Throttle, //
//		3 = Pitch Angle, rad

	if TRIM >= 1
        disp('  ')
		disp('TRIM Stabilator, Thrust, and Pitch Angle')
        disp('========================================')
        OptParam        =   [];
        TrimHist        =   [];
//       Arbitrary starting values (user-selected, e.g., best guess at solution)
		InitParam		=	[0.0369;0.1892;0.0986]; 
        
//       Application of 'fminsearch' to minimize Trim Cost        
        options =   optimset('TolFun',1e-10);
		[OptParam,J,ExitFlag,Output]  =	fminsearch('TrimCost',InitParam,options);

        disp(['Trim Cost = ',num2str(J),', Exit Flag = ',num2str(ExitFlag)])
        Output
//		Optimizing Trim Error Cost with respect to dSr, dT, and Theta
        TrimHist;
        Index=  [1:length(TrimHist)];
        TrimStabDeg     =   R2D*OptParam(1);
		TrimThrusPer    =   100*OptParam(2);
        TrimPitchDeg    =   R2D*OptParam(3);
        TrimAlphaDeg    =   TrimPitchDeg - gamma;
        disp(['Stabilator  = ',num2str(TrimStabDeg),' deg, Thrust = ', ...
            num2str(TrimThrusPer),' x 100//'])
        disp(['Pitch Angle = ',num2str(TrimPitchDeg),' deg, Angle of Attack = ',...
            num2str(TrimAlphaDeg),' deg'])
        
//		Insert trim values in nominal control and state vectors
        disp(' ')
        disp('Trimmed Initial Control and State Vectors')
        disp('=========================================')
		u	=	[u(1)
				u(2)
				u(3)
				OptParam(2)
				u(5)
				u(6)
				OptParam(1)];
		format long			
		x	=	[V * cos(OptParam(3))
				x(2)
				V * sin(OptParam(3))
				x(4)
				x(5)
				x(6)
				x(7)
				x(8)
				x(9)
				x(10)
				OptParam(3)
				x(12)];
        disp('Control Vector')
        disp('--------------')
        disp(['Elevator   = ',num2str(u(1)),' rad, Aileron = ',num2str(u(2)),...
            ' rad, Rudder = ',num2str(u(3)),' rad'])
        disp(['Throttle   = ',num2str(u(4)),' x 100//, Asymm Spoiler = ',...
            num2str(u(5)),' rad, Flap = ',num2str(u(6)),' rad'])
        disp(['Stabilator = ',num2str(u(7)),' rad'])

        disp('  ')
        disp('State Vector')
        disp('------------')
        disp(['u   = ',num2str(x(1)),' m/s, v = ',num2str(x(2)),' m/s, w = ',...
            num2str(x(3)),' m/s'])
        disp(['x   = ',num2str(x(4)),' m, y = ',num2str(x(5)),' m, z = ',...
            num2str(x(6)),' m'])
        disp(['p   = ',num2str(x(7)),' rad/s, q = ',num2str(x(8)),' rad/s, r = ',...
            num2str(x(9)),' rad/s'])
        disp(['Phi = ',num2str(x(10)),' rad, Theta = ',num2str(x(11)),' rad, Psi = ',...
            num2str(x(12)),' rad'])
        disp('  ')
		format short
    end
    
    figure
	subplot(1,2,1)
	plot(Index,TrimHist(1,:),'b',Index,TrimHist(2,:),'g',Index,TrimHist(3,:),'r'),
        grid, legend('Stabilator', 'Thrust', 'Pitch Angle'),xlabel('Iterations'), 
        ylabel('Stabilator(blue), Thrust(green), Pitch Angle(red)'),...
        
    title('Trim Parameters'), legend('Stabilator, rad', 'Thrust, 100//',...
        'Pitch Angle, rad')
	subplot(1,2,2)
	semilogy(Index,TrimHist(4,:))
	xlabel('Iterations'), ylabel('Trim Cost'), grid
    title('Trim Cost')
        
//	Stability-and-Control Derivative Calculation
//   ============================================
   	if LINEAR >= 1
		disp('  ')
        disp('Generate and Save LINEAR MODEL')
        disp('==============================')
		thresh	=	[.1;.1;.1;.1;.1;.1;.1;.1;.1;.1;.1;.1;.1;.1;.1;.1;.1;.1;.1];
		xj		=	[x;u];
        uTemp   =   u;  // 'numjac' modifies 'u'; reset 'u' after the call
		xdotj		=	LinModel(ti,xj);
		[dFdX,fac]	=	numjac('LinModel',ti,xj,xdotj,thresh,[],0);
        u           =   uTemp;
        Fmodel		=	dFdX(1:12,1:12)
		Gmodel		=	dFdX(1:12,13:19)
		save ('FmodelFile','Fmodel','TASms','hm','TrimAlphaDeg')
		save ('GmodelFile','Gmodel')
    end

//	Flight Path Simulation, with Quaternion Option
//   ==============================================
    if SIMUL >= 1
        RUNNING =   1;  
		tspan	=	[ti tf];
		xo		=	x + delx;
		u		=	u + delu;
        
        disp('Trimmed Initial Control and State Vectors PLUS Test Inputs')
        disp('==========================================================')
        disp('Control Vector')
        disp('--------------')
        disp(['Elevator   = ',num2str(u(1)),' rad, Aileron = ',num2str(u(2)),...
            ' rad, Rudder = ',num2str(u(3)),' rad'])
        disp(['Throttle   = ',num2str(u(4)),' x 100//, Asymm Spoiler = ',...
            num2str(u(5)),' rad, Flap = ',num2str(u(6)),' rad'])
        disp(['Stabilator = ',num2str(u(7)),' rad'])

        disp('  ')
        disp('State Vector')
        disp('------------')
        disp(['u   = ',num2str(xo(1)),' m/s, v = ',num2str(xo(2)),' m/s, w = ',...
            num2str(xo(3)),' m/s'])
        disp(['x   = ',num2str(xo(4)),' m, y = ',num2str(xo(5)),' m, z = ',...
            num2str(xo(6)),' m'])
        disp(['p   = ',num2str(xo(7)),' rad/s, q = ',num2str(xo(8)),' rad/s, r = ',...
            num2str(xo(9)),' rad/s'])
        disp(['Phi = ',num2str(xo(10)),' rad, Theta = ',num2str(xo(11)),...
            ' rad, Psi = ',num2str(xo(12)),' rad'])
        disp('  ')
        
//       Choice of Euler Angles or Quaternions to define Rotation Matrix
//       in Equations of Motion for Simulation
//       ==================================================================
switch QUAT
    case 0
//       Trajectory Calculation using Euler Angles
//       ========================================
        disp('Use Euler Angles to form Rotation Matrix')
        disp('========================================')
        
        options =   odeset('Events',@event,'RelTol',1e-7,'AbsTol',1e-7);
		tic
        [t,x]	=	ode45(@EoM,tspan,xo,options);
		toc
        kHis	=	length(t);
        disp(['// of Time Steps = ',num2str(kHis)])
        disp('  ')
        
    case 1
//       Trajectory Calculation using Quaternions
//       ========================================
        disp('Use Quaternion to form Rotation Matrix')
        disp('======================================')
        Ho      =   DCM(xo(10), xo(11), xo(12));
        q4o     =   0.5*sqrt(1 + Ho(1,1) + Ho(2,2) + Ho(3,3));
        q1o     =   (Ho(2,3) - Ho(3,2)) / (4*q4o);
        q2o     =   (Ho(3,1) - Ho(1,3)) / (4*q4o);
        q3o     =   (Ho(1,2) - Ho(2,1)) / (4*q4o);
        xoQ     =   [xo(1:9); q1o; q2o; q3o; q4o];
        
        options =   odeset('Events',@event,'RelTol',1e-7,'AbsTol',1e-7);
        
        tic
        [tQ,xQ] =	ode15s(@EoMQ,tspan,xoQ,options);  
		toc
        kHisQ	=	length(tQ);
        disp(['// of Time Steps = ',num2str(kHisQ)])
        disp('  ')
        q1s     =   xQ(:,10);
        q2s     =   xQ(:,11);
        q3s     =   xQ(:,12);
        q4s     =   xQ(:,13);
        Phi     =   (atan2(2*(q1s.*q4s + q2s.*q3s),(1 - 2*(q1s.^2 + q2s.^2))))*180/pi;
        PhiR    =   Phi*pi/180;
        Theta   =   (asin(2*(q2s.*q4s - q1s.*q3s)))*180/pi;
        ThetaR  =   Theta*pi/180;
        Psi     =   (atan2(2*(q3s.*q4s + q1s.*q2s),(1 - 2*(q2s.^2 + q3s.^2))))*180/pi;
        PsiR    =   Psi*pi/180;
        qMag    =   sqrt(q1s.^2 + q2s.^2 + q3s.^2 + q4s.^2);
        
        t       =   tQ;
        x       =   [];
        x       =   [xQ(:,1:9),PhiR(:),ThetaR(:),PsiR(:)];
        kHis    =   kHisQ;
end
        
//       Plot Control History
        figure
        subplot(2,2,1)
        plot(tuHis,R2D*deluHis(:,1) + R2D*delu(1), tuHis, R2D*deluHis(:,7) + R2D*delu(7))    
        xlabel('Time, s'), ylabel('Elevator (blue), Stabilator (green), deg'), grid
        title('Pitch Test Inputs'), legend('Elevator, dE', 'Stabilator, dS')
        subplot(2,2,2)
        plot(tuHis,R2D*deluHis(:,2) + R2D*delu(2), tuHis, R2D*deluHis(:,3) + R2D*delu(3), tuHis, R2D*deluHis(:,5) + R2D*delu(5))    
        xlabel('Time, s'), ylabel('Aileron (blue), Rudder (green), Asymmetric Spoiler (red), deg'), grid
        title('Lateral-Directional Test Inputs'), legend('Aileron, dA', 'Rudder, dR', 'Asymmetric Spoiler, dAS')
        subplot(2,2,3)
        plot(tuHis, deluHis(:,4) + delu(4))    
        xlabel('Time, s'), ylabel('Throttle Setting'), grid
        title('Throttle Test Inputs')
        subplot(2,2,4)
        plot(tuHis,R2D*deluHis(6) + R2D*delu(6))    
        xlabel('Time, s'), ylabel('Flap, deg'), grid
        title('Flap Test Inputs')
        
//       Plot State History
		figure
		subplot(2,2,1)
		plot(t,x(:,1))
		xlabel('Time, s'), ylabel('Axial Velocity (u), m/s'), grid
        title('Forward Body-Axis Component of Inertial Velocity, u')
		subplot(2,2,2)
		plot(t,x(:,2))
		xlabel('Time, s'), ylabel('Side Velocity (v), m/s'), grid
        title('Side Body-Axis Component of Inertial Velocity, v')
		subplot(2,2,3)
		plot(t,x(:,3))
		xlabel('Time, s'), ylabel('Normal Velocity (w), m/s'), grid
        title('Normal Body-Axis Component of Inertial Velocity, z')
		subplot(2,2,4)
		plot(t,x(:,1),t,x(:,2),t,x(:,3))
		xlabel('Time, s'), ylabel('u (blue), v (green), w (red), m/s'), grid
        title('Body-Axis Component of Inertial Velocity') 
        legend('Axial velocity, u', 'Side velocity, v', 'Normal velocity, w') 
        
		figure
        subplot(3,2,1)
		plot(t,x(:,4))
		xlabel('Time, s'), ylabel('North (x), m'), grid
        title('North Location, x')
        
		subplot(3,2,2)
		plot(t,x(:,5))
		xlabel('Time, s'), ylabel('East (y), m'), grid
        title('East Location, y')
        
		subplot(3,2,3)
		plot(t,-x(:,6))
		xlabel('Time, s'), ylabel('Altitude (-z), m'), grid
        title('Altitude, -z')
        
		subplot(3,2,4)
		plot((sqrt(x(:,4).*x(:,4) + x(:,5).*x(:,5))),-x(:,6))
		xlabel('Ground Range, m'), ylabel('Altitude, m'), grid
        title('Altitude vs. Ground Range')
        
        subplot(3,2,5)
        plot(x(:,4),x(:,5))
		xlabel('North, m'), ylabel('East, m'), grid
        title('Ground Track, North vs. East')
        
        subplot(3,2,6)
        plot3(x(:,4),x(:,5),-x(:,6))
		xlabel('North, m'), ylabel('East, m'), zlabel('Altitude, m'), grid
        title('3D Flight Path')
        
        figure
		subplot(2,2,1)
		plot(t,x(:,7) * R2D)
		xlabel('Time, s'), ylabel('Roll Rate (p), deg/s'), grid
        title('Body-Axis Roll Component of Inertial Rate, p')
		subplot(2,2,2)
		plot(t,x(:,8) * R2D)
		xlabel('Time, s'), ylabel('Pitch Rate (q), deg/s'), grid
        title('Body-Axis Pitch Component of Inertial Rate, q')
		subplot(2,2,3)
		plot(t,x(:,9) * R2D)
		xlabel('Time, s'), ylabel('Yaw Rate (r), deg/s'), grid
        title('Body-Axis Yaw Component of Inertial Rate, r')
        subplot(2,2,4)
		plot(t,x(:,7) * R2D,t,x(:,8) * R2D,t,x(:,9) * R2D)
		xlabel('Time, s'), ylabel('p (blue), q (green), r (red), deg/s'), grid
        title('Body-Axis Inertial Rate Vector Components')
        legend('Roll rate, p', 'Pitch rate, q', 'Yaw rate, r')
        
        figure
		subplot(2,2,1)
		plot(t,x(:,10) * R2D)
		xlabel('Time, s'), ylabel('Roll Angle (phi), deg'), grid
        title('Earth-Relative Roll Attitude')
		subplot(2,2,2)
		plot(t,x(:,11) * R2D)
		xlabel('Time, s'), ylabel('Pitch Angle (theta), deg'), grid
        title('Earth-Relative Pitch Attitude')
		subplot(2,2,3)
		plot(t,x(:,12) * R2D)
		xlabel('Time, s'), ylabel('Yaw Angle (psi, deg'), grid
        title('Earth-Relative Yaw Attitude')
		subplot(2,2,4)
		plot(t,x(:,10) * R2D,t,x(:,11) * R2D,t,x(:,12) * R2D)
		xlabel('Time, s'), ylabel('phi (blue), theta (green), psi (red), deg'), grid
        title('Euler Angles')
        legend('Roll angle, phi', 'Pitch angle, theta', 'Yaw angle, psi')
        
        VAirRel         =   [];
        vEarth          =   [];
        AlphaAR         =   [];
        BetaAR          =   [];
        windBody        =   [];
        airDensHis      =   [];
        soundSpeedHis   =   [];
        qbarHis         =   [];
        GammaHis        =   [];
        XiHis           =   [];
        
        for i = 1:kHis
            windb           =	WindField(-x(i,6),x(i,10),x(i,11),x(i,12));
            windBody        =   [windBody windb];
            [airDens,airPres,temp,soundSpeed] = Atmos(-x(i,6));
            airDensHis      =   [airDensHis airDens];
            soundSpeedHis   =   [soundSpeedHis soundSpeed];
        end
        
        vBody           =   [x(:,1) x(:,2) x(:,3)]';
        vBodyAir        =   vBody + windBody;

        for i = 1:kHis
            vE          =   DCM(x(i,10),x(i,11),x(i,12))' * [vBody(1,i);vBody(2,i);vBody(3,i)];
            VER         =   sqrt(vE(1)^2 + vE(2)^2 + vE(3)^2);
            VAR         =   sqrt(vBodyAir(1,i)^2 + vBodyAir(2,i)^2 + vBodyAir(3,i)^2);
            VARB        =   sqrt(vBodyAir(1,i)^2 + vBodyAir(3,i)^2);

            if vBodyAir(1,i) >= 0
                Alphar      =	asin(vBodyAir(3,i) / VARB);
            else
                Alphar      =	pi - asin(vBodyAir(3,i) / VARB);
            end
            
            AlphaAR     =   [AlphaAR Alphar];
            Betar       = 	asin(vBodyAir(2,i) / VAR);
            BetaAR      =   [BetaAR Betar];
            vEarth      =   [vEarth vE];
            Xir         =   asin(vEarth(2,i) / sqrt((vEarth(1,i))^2 + (vEarth(2,i))^2));
            if vEarth(1,i) <= 0 && vEarth(2,i) <= 0
                Xir     = -pi - Xir;
            end
            if vEarth(1,i) <= 0 && vEarth(2,i) >= 0
                Xir     = pi - Xir;
            end
            Gammar      =   asin(-vEarth(3,i) / VER);
            GammaHis    =   [GammaHis Gammar];           
            XiHis       =   [XiHis Xir];
            VAirRel     =   [VAirRel VAR];
        end

        MachHis         =   VAirRel ./ soundSpeedHis;
        AlphaDegHis 	=	R2D * AlphaAR;
        BetaDegHis      =	R2D * BetaAR;
        qbarHis         =	0.5 * airDensHis .* VAirRel.*VAirRel;
        GammaDegHis     =   R2D * GammaHis;
        XiDegHis        =   R2D * XiHis;
        
        figure
        subplot(3,1,1)
        plot(t, VAirRel')    
        xlabel('Time, s'), ylabel('Air-relative Speed, m/s'), grid
        title('True AirSpeed, Vair')
        subplot(3,1,2)
        plot(t, MachHis')    
        xlabel('Time, s'), ylabel('M'), grid
        title('Mach Number, M')
        subplot(3,1,3)
        plot(t, qbarHis')    
        xlabel('Time, s'), ylabel('qbar, N/m^2'), grid
        title('Dynamic Pressure, qbar')
        
        figure
        subplot(2,1,1)
        plot(t, AlphaDegHis', t, BetaDegHis')    
        xlabel('Time, s'), ylabel('Angle of Attack, deg (blue), Sideslip Angle, deg (green)'), grid
        title('Aerodynamic Angles'), legend('Angle of Attack, alpha', 'Sideslip Angle, beta')
        subplot(2,1,2)
        plot(t, GammaDegHis', t, XiDegHis')    
        xlabel('Time, s'), ylabel('Vertical, deg (blue), Horizontal, deg (green)'), grid
        title('Flight Path Angles'), legend('Flight Path Angle, gamma', 'Heading Angle, psi')
        
    'End of FLIGHT Simulation'
	end
