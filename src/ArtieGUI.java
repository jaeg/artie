import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import artie.ai.Artie;
import artie.utilities.Logger;

public class ArtieGUI extends JPanel {

	private static final long serialVersionUID = 1L;

	private JFileChooser saveChoice;
	private JTextField entryField;
	public static JTextArea logWindow;
	private JButton save;
	private JButton submit;
	private JButton showLog;
	private Artie artie;

	public static void main(String[] args) {
		JFrame frame = new JFrame("ARTIE");
		frame.getContentPane().add(new ArtieGUI());
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}

	public ArtieGUI() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		try {
			BufferedImage logo = ImageIO.read(new File("ARTIEHEADER.gif"));
			JLabel logoLabel = new JLabel(new ImageIcon(logo));
			logoLabel.setAlignmentX(0.5f);
			add(logoLabel);
		} catch (Exception ex) {
			System.out.println(ex);
		}
		new Logger();
		JPanel optionsPanel = new JPanel();
		JPanel conversationPanel = new JPanel();
		JPanel submissionPanel = new JPanel();
		artie = new Artie();

		save = new JButton("Save Chat");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveChoice.addChoosableFileFilter(new FileFilter() {
					String description = "Text File (*.txt)";
					String extension = "txt";

					public String getDescription() {
						return description;
					}

					public boolean accept(File f) {
						if (f == null)
							return false;
						if (f.isDirectory())
							return true;
						return f.getName().toLowerCase().endsWith(extension);
					}
				});
				if (saveChoice.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
					try {
						FileWriter fstream = new FileWriter(saveChoice
								.getSelectedFile());
						BufferedWriter out = new BufferedWriter(fstream);
						out.write(logWindow.getText());
						out.close();
						JOptionPane.showMessageDialog(null,
								"Your chat log has been saved.");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null,
								"Error saving file!");
					}

				}
			}
		});
		showLog = new JButton("Show Log");

		showLog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.toggleLog();
			}
		});

		optionsPanel.add(save);
		optionsPanel.add(showLog);
		add(optionsPanel);

		this.setBorder(new EmptyBorder(50, 50, 50, 50));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		conversationPanel.setLayout(new BoxLayout(conversationPanel,
				BoxLayout.X_AXIS));
		submissionPanel.setLayout(new BoxLayout(submissionPanel,
				BoxLayout.X_AXIS));
		conversationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		logWindow = new JTextArea();
		logWindow.setWrapStyleWord(true);
		logWindow.setLineWrap(true);
		logWindow.setEditable(false);

		JScrollPane conversationWindow = new JScrollPane(logWindow);

		add(conversationWindow);

		conversationWindow.getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						logWindow.select(logWindow.getHeight() + 100000, 0);
					}
				});
		conversationPanel.add(conversationWindow);
		add(conversationPanel);

		conversationWindow.getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						logWindow.select(logWindow.getHeight() + 100000, 0);
					}
				});
		conversationPanel.setPreferredSize(new Dimension(300, 300));

		submit = new JButton("Submit");
		entryField = new JTextField();

		submissionPanel.add(submit);
		submissionPanel.add(entryField);
		add(submissionPanel);

		this.setBackground(new Color(227, 25, 42));
		optionsPanel.setBackground(new Color(227, 25, 42));
		conversationPanel.setBackground(new Color(227, 25, 42));
		submissionPanel.setBackground(new Color(227, 25, 42));

		entryField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					// submitAction();
				}
			}
		});
	}
	/*
	 * private void submitAction() { Logger.log("\n"); String userInput =
	 * entryField.getText(); Logger.log("User sent: " + userInput + "\n");
	 * 
	 * artie.response(userInput); String lastResponse =
	 * artie.response().toString(); artie.learn(userInput);
	 * entryField.setText(""); }
	 */
}