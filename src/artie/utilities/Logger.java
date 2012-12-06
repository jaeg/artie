package artie.utilities;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Logger extends JPanel
{
	private static final long serialVersionUID = 1L;
	static JTextArea log = new JTextArea();
	private static JFrame logFrame;
	private static boolean currentState = false;
	
	public Logger()
	{
		logFrame = new JFrame("Log Window");
		logFrame.getContentPane().add(this);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JScrollPane logPane = new JScrollPane(log);
		logPane.setPreferredSize(new Dimension(370,500));
		log.setEditable(false);

		logPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
			public void adjustmentValueChanged(AdjustmentEvent e){
				log.select(log.getHeight()+100000, 0);
			}});
		
		add(logPane);
		
		logFrame.pack();
		logFrame.setVisible(true);
		currentState = true;
		logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
	}
	
	public static void log(String message)
	{
		log.append(message);
	}
	
	public static void toggleLog()
	{
		logFrame.setVisible(!currentState);
		currentState = !currentState;
	}

	
	
}

