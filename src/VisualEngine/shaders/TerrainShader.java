package VisualEngine.shaders;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import VisualEngine.entities.Camera;
import VisualEngine.entities.Light;
import VisualEngine.entities.ThirdPersonCamera;
import VisualEngine.toolbox.Maths;

 
public class TerrainShader extends ShaderProgram{
     
     
    private static final String VERTEX_FILE = "src/VisualEngine/shaders/terrainVertexShader.txt";
    private static final String FRAGMENT_FILE = "src/VisualEngine/shaders/terrainFragmentShader.txt";
     
    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_lightPosition;
    private int location_lightColour;
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_backgroundTexture;
    private int location_rTexture;
    private int location_gTexture;
    private int location_bTexture;
    private int location_blendMap;
	private int location_skyColour;
    private int location_density;
    private int location_gradient;
 
    public TerrainShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }
 
    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");
    }
 
    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_lightPosition = super.getUniformLocation("lightPosition");
        location_lightColour = super.getUniformLocation("lightColour");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
        location_backgroundTexture = super.getUniformLocation("backgroundTexture");
        location_rTexture = super.getUniformLocation("rTexture");
        location_gTexture = super.getUniformLocation("gTexture");
        location_bTexture = super.getUniformLocation("bTexture");
        location_blendMap = super.getUniformLocation("blendMap");  
        location_skyColour = super.getUniformLocation("skyColour");
        location_density = super.getUniformLocation("density");
        location_gradient = super.getUniformLocation("gradient");
    }
    
	public void loadSkyColour(float r, float g, float b) {
		super.loadVector(location_skyColour, new Vector3f(r,g,b));
	}
    
    public void connectTextureUnits() {
    	super.loadInt(location_backgroundTexture, 0);
    	super.loadInt(location_rTexture, 1);
    	super.loadInt(location_gTexture, 2);
    	super.loadInt(location_bTexture, 3);
    	super.loadInt(location_blendMap, 4);
    }
     
    public void loadShineVariables(float damper,float reflectivity){
        super.loadFloat(location_shineDamper, damper);
        super.loadFloat(location_reflectivity, reflectivity);
    }
     
    public void loadTransformationMatrix(Matrix4f matrix){
        super.loadMatrix(location_transformationMatrix, matrix);
    }
     
    public void loadLight(Light light){
        super.loadVector(location_lightPosition, light.getPosition());
        super.loadVector(location_lightColour, light.getColour());
    }
     
    public void loadViewMatrix(Camera camera){
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        super.loadMatrix(location_viewMatrix, viewMatrix);
    }
    
    public void loadViewMatrix(ThirdPersonCamera camera){
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        super.loadMatrix(location_viewMatrix, viewMatrix);
    }
     
    public void loadProjectionMatrix(Matrix4f projection){
        super.loadMatrix(location_projectionMatrix, projection);
    }
     
    public void loadVisibility(float density, float gradient) {
    	super.loadFloat(location_density, density);
    	super.loadFloat(location_gradient, gradient);
    }
 
}
