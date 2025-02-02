package GUI.PropulsionDraw;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import GUI.PropulsionDraw.ComponentMetaFileTypes.ComponentMetaFile;

class BoxElement{
	
	private ComponentElement element;
	
	private BoxElement boxElement;
	
	private ComponentMetaFile metaFile;
	
	private String name;
	
	private ElementPopUpMenu menu ;
	
	private Canvas canvas;
	
	public BoxElement(String name, String imageFilePath, Canvas canvas, ReadWrite readWrite) {
		this.name = name ; 
		this.canvas=canvas;
		boxElement = this;
		
		element = new ComponentElement(imageFilePath, canvas);
		element.addMouseListener(new PopClickListener() {
			@Override
		    public void mousePressed(MouseEvent e) {
		        if (e.isPopupTrigger())
		            doPop(e);
		    }
			@Override
		    public void mouseReleased(MouseEvent e) {
		        if (e.isPopupTrigger())
		            doPop(e);
		    }

		    private void doPop(MouseEvent e) {
		    	    menu = new ElementPopUpMenu(element, canvas, boxElement);
		    	   
		        menu.show(e.getComponent(), e.getX(), e.getY());
		    }
		});
		element.setFocusable(true);
		element.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {

				
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
	    	
	    });
		this.setName(name);
	}
	
	
	public ComponentMetaFile getMetaFile() {
		return metaFile;
	}



	public void setMetaFile(ComponentMetaFile metaFile) {
		this.metaFile=null;
		this.metaFile = metaFile;
		metaFile.readMetaList();
		setName(metaFile.getName());
	}


	public ComponentElement getElement() {
		return element;
	}



	public String getName() {
		return name;
	}

	public ElementPopUpMenu getMenu() {
		return menu; 
	}


	public void setName(String name) {
		this.name = name;
		element.setName(name);
		element.repaint();
	}



	class PopClickListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {

	    }

	    public void mouseReleased(MouseEvent e) {

	    }


	}



	public Canvas getCanvas() {
		return canvas;
	}
	
}