package GUI.PropulsionDraw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import GUI.PropulsionDraw.Relationship;
import GUI.PropulsionDraw.ComponentMetaFileTypes.ComponentMetaFile;
import GUI.PropulsionDraw.ComponentMetaFileTypes.MainEngineMetaFile;
import GUI.PropulsionDraw.ComponentMetaFileTypes.MetaDataLine;
import GUI.PropulsionDraw.ComponentMetaFileTypes.TankMetaFile;
import GUI.PropulsionDraw.ComponentMetaFileTypes.ThrusterPodMetaFile;


public class Canvas extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	private List<Relationship> relationships;
	private List<BoxElement> canvasElements;
	
	private Point backgroundLocation = new Point();
	
	private Image backgroundImage;
	
	private ReadWrite readWrite;
	
	@SuppressWarnings("unused")
	private PartsCatalogue partsCatalogue;
	
	private String backgroundImagePath = "images/propulsionElements/blueprintBackground.png";
	
	static Font smallFont  = new Font("Verdana", Font.LAYOUT_LEFT_TO_RIGHT, 10);
	
	private StatisticsPanel statsPanel;
	
	public Canvas() {
		relationships = new ArrayList<>();
		canvasElements = new ArrayList<>();
		statsPanel = new StatisticsPanel(this);
	    
	    ImageIcon imageIn;
			imageIn = new ImageIcon(backgroundImagePath,"");
		Image	image = getScaledImage(imageIn.getImage(),(int) getSize().getWidth(), (int) getHeight());
	         setBackgroundImage(image);

        setPreferredSize(new Dimension(500,500));
	}
	/*
    protected List<BoxElement> getShapes() {
        ArrayList<BoxElement> shapes = new ArrayList<>();
       // shapes.addAll(mainEngineElements);
        return shapes;
    }
    
	*/
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        if (getBackgroundImage() != null) {
            g.drawImage(getBackgroundImage(), backgroundLocation.x, backgroundLocation.y, this);
        }

        for (Relationship relationship : relationships) {

            Point2D p1 = new Point2D.Double(relationship.getParent().getElement().getBounds().getCenterX(), relationship.getParent().getElement().getBounds().getCenterY());
            Point2D p2 = new Point2D.Double(relationship.getChild().getElement().getBounds().getCenterX(), relationship.getChild().getElement().getBounds().getCenterY());
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.WHITE);
            Line2D line = new Line2D.Float(p1, p2);
            
            g2.draw(line);

        }


        g2.dispose();
    }

    public void addTankElement(BoxElement Tank, int x, int y) {
	    add(Tank.getElement());
	    addCanvasElement(Tank);
		Tank.getElement().setLocation(x, y);
		revalidate();
	    repaint();
    }

    public void addElement(BoxElement boxElement, int x, int y) {	
	    add(boxElement.getElement());
	    addCanvasElement(boxElement);
	    boxElement.getElement().setLocation(x, y);
		revalidate();
	    repaint();
    }
    
    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        repaint();
    }
    
    public Point getBackgroundLocation() {
        return backgroundLocation;
    }

    public void setBackgroundLocation(Point backgroundLocation) {
        this.backgroundLocation = backgroundLocation;
        repaint();
    }
    
	
	public void addCanvasElement(BoxElement element) {
		canvasElements.add(element);
	    repaint();
	}
	
	public List<BoxElement> getCanvasElements() {
		return canvasElements;
	}
	
	protected void relate(BoxElement parent, BoxElement child) {
		if(parent.getMetaFile().getID()!=child.getMetaFile().getID()) {
		    relationships.add(new Relationship(parent, child));
		    readWrite.writeFile();
		}
	}
	
	public void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}

	public List<Relationship> getRelationships() {
		return relationships;
	}

    public void linkReadWrite(ReadWrite readWrite) {
    	this.readWrite = readWrite;
    }
    
    public void linkPartsCatalogue(PartsCatalogue partsCatalogue) {
    	this.partsCatalogue=partsCatalogue;
    }
    
    public void resizeBackgroundImage() {
	    ImageIcon imageIn;
			imageIn = new ImageIcon(backgroundImagePath,"");
		Image	image = getScaledImage(imageIn.getImage(),(int) getSize().getWidth(), (int) getHeight());
	         setBackgroundImage(image);
         revalidate();
         repaint();
    }
    
    public BoxElement addElement(ReadWrite readWrite, int type) { // new element -> new ID
  	  BoxElement newElement = new BoxElement(partsCatalogue.getList().get(type).getName(), 
  			  partsCatalogue.getList().get(type).getLogoFilePath(), this, readWrite);
  	checkCustomResize( newElement,  type);
  	  newElement.setName(partsCatalogue.getList().get(type).getName());
  	  ComponentMetaFile metaFile =  createMetaFile(type, readWrite);
  	  metaFile.setID(UUID.randomUUID());
  	  metaFile.setName(partsCatalogue.getList().get(type).getName());
  	  newElement.setMetaFile(metaFile);
  	addElement(newElement, 50, 50);
    		repaint();
    		return newElement;
    }
    
    public BoxElement addElement(ReadWrite readWrite, int type, UUID ID) { // exisiting element with given ID
    	  BoxElement newElement = new BoxElement(partsCatalogue.getList().get(type).getName(), 
    			  partsCatalogue.getList().get(type).getLogoFilePath(), this, readWrite);
    	  		checkCustomResize( newElement,  type);
    	  	 newElement.setName(partsCatalogue.getList().get(type).getName());
      	  ComponentMetaFile metaFile = createMetaFile(type, readWrite);
      	  metaFile.setID(UUID.randomUUID());
      	 metaFile.setName(partsCatalogue.getList().get(type).getName());
    	      newElement.setMetaFile(metaFile);
    	      addElement(newElement, 50, 50);
      		repaint();
      		return newElement;
      }
    
    public BoxElement cloneElement(ReadWrite readWrite, int type, BoxElement boxElement) { // new element -> new ID
    	
      String name = boxElement.getMetaFile().getName()+" Clone";
	  int newFileX = boxElement.getElement().getX()+100;
	  int newFileY = boxElement.getElement().getY();
	  
	  
    	  BoxElement newElement = new BoxElement(name, 
    			  partsCatalogue.getList().get(type).getLogoFilePath(), this, readWrite);
    	  
    	  checkCustomResize( newElement,  type);
    	  //----------------------------------------------------------------------------------
    	  ComponentMetaFile metaFile =  createMetaFile(type, readWrite);
    	  metaFile.setID(UUID.randomUUID());
    	 // metaFile.setName(name);
    	  metaFile.setPositionX(newFileX);
    	  metaFile.setPositionY(newFileY);
    	  List<MetaDataLine> elementMetaList = new ArrayList<>();
    	  for(MetaDataLine line :boxElement.getMetaFile().getElementMetaList() ) {
    		  String description = line.name;
    		  String value = line.value;
    		  MetaDataLine liner = new MetaDataLine(description, value);
    		  elementMetaList.add(liner);
    	  }
    	  metaFile.setElementMetaList(elementMetaList);
    	  newElement.setMetaFile(metaFile);
    	  metaFile.setName(name);
    	  //------------------------------------------------------------------------------------
    	  addElement(newElement, newFileX, newFileY);
    	  newElement.setName(name);
    	  newElement.getElement().repaint();
      repaint();
      
      return newElement;
      }
    
    
    public ReadWrite getReadWrite() {
		return readWrite;
	}
    
    public BoxElement checkCustomResize(BoxElement element, int type) {
    	
    	if(type==16) {// Joint
    		 ((ComponentElement) element.getElement()).resizeElement(30, 30);
    	} else if (type == 4) { // tank 
    		//((ComponentElement) element.getElement()).resizeElement(150, 150);
    	}
    	
    	return element;
    }

	static Image getScaledImage(Image srcImg, int w, int h){
    	try {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    	} catch (IllegalArgumentException e) {
    		//System.out.println("ERROR/getScaledImage > Illegal Argument");
    		return srcImg;
    	}
    }

	public void setBackgroundImagePath(String backgroundImagePath) {
		this.backgroundImagePath = backgroundImagePath;
		repaint();
	} 
    
	public ComponentMetaFile createMetaFile(int type, ReadWrite readWrite) { 
		if(type==0 || type ==1) {
			return  new MainEngineMetaFile(type, readWrite);
		} else if ( type==2 || type==3) {
			return new ThrusterPodMetaFile(type, readWrite);
		} else if (type == 4) {
			return new TankMetaFile(type, readWrite);
		} else {
			return new ComponentMetaFile(type, readWrite);
		}
		
	
	}
	
	public void deleteAllContent() {
		for(int i=getCanvasElements().size()-1;i>=0;i--) {
			remove(getCanvasElements().get(i).getElement());
			getCanvasElements().remove(i);
		}
		for(int i=getRelationships().size()-1;i>=0;i--) {
			getRelationships().remove(i);
		}
		readWrite.writeFile();
		repaint();
	}
	public StatisticsPanel getStatsPanel() {
		return statsPanel;
	}
	
	

}
