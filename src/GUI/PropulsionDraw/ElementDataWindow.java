package GUI.PropulsionDraw;

import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import GUI.PropulsionDraw.ComponentMetaFileTypes.MetaDataLine;

public class ElementDataWindow {
	private static JFrame frame;
	private static int elementHeight=35;
	private static int frameWidth=300;
	@SuppressWarnings("unused")
	private  Canvas canvas;
	private BoxElement boxElement;
	public ElementDataWindow(BoxElement boxElement, Canvas canvas) {
		this.boxElement=boxElement;
		this.canvas= canvas;
	}
	
	public  void showWindow() {
	     frame = new JFrame(""+boxElement.getName());	
	    List<MetaDataLine> elementMetaList = boxElement.getMetaFile().getElementMetaList();
	    
	    for(MetaDataLine line : elementMetaList) {
		    	JPanel panel = CreateLine(line.name, ""+line.value);
		    	frame.add(panel);
	    }
		frame.setSize(frameWidth, elementMetaList.size()*elementHeight);
		frame.setLayout(new GridLayout(elementMetaList.size(),1));
	    //f.pack();
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    Point location = MouseInfo.getPointerInfo().getLocation(); 
	    int x = (int) location.getX();
	    int y = (int) location.getY();
	    frame.setLocation(x, y);
	    frame.setResizable(false);
	    frame.setVisible(true);

	}
	

		public  JPanel CreateLine(String description, String value) {
			JPanel panel = new JPanel();
			panel.setLayout(null);
			panel.setSize(300, 30);
			
			JLabel label = new JLabel(""+description);
			label.setSize(200, 25);
			label.setHorizontalAlignment(JLabel.LEFT);
			label.setLocation(0, 1);
			panel.add(label);
			
			JTextField text = new JTextField(""+value);
			text.setSize(100, 25);
			text.setLocation(200,1);
			text.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
						if(description.equals("Name")) {
							boxElement.setName(text.getText());
							boxElement.getMetaFile().setName(text.getText());
						} else {
							boxElement.getMetaFile().getMetaListUpdate(label.getText(), text.getText());
						}
						canvas.getStatsPanel().updatePanel();
						frame.dispose();
						
				}
				
			});
			text.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(FocusEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					if(description.equals("Name")) {
						boxElement.setName(text.getText());
						boxElement.getMetaFile().setName(text.getText());
						boxElement.getCanvas().getStatsPanel().updatePanel();
					} else {
						boxElement.getMetaFile().getMetaListUpdate(label.getText(), text.getText());
					}
					canvas.getStatsPanel().updatePanel();
				}
				
			});
			panel.add(text);
			
			return panel;
		}


}
