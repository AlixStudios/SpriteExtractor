package com.alix;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import layout.TableLayout;
import layout.TableLayoutConstraints;

import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Alix Studios
 * 
 */
public class ExtractorUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 687350099072012107L;
	private static String EXT = ".png";
	private static String FORMAT = "PNG";
	double[][] size = { { 0.25D, 0.25D, 0.25D, 0.25D },
			{ 0.25D, 0.25D, 0.25D, 0.25D } };

	TableLayout layout = new TableLayout(this.size);
	JButton browseSrc;
	JButton browseDest;
	JButton convert;
	JTextField fileSrc;
	JTextField fileDst;

	/**
	 * 
	 */
	public ExtractorUI() {
		setSize(500, 200);
		getContentPane().setLayout(this.layout);
		setResizable(false);
		initializeComponents();
		setTitle("Sprite Extractor");
	}

	/**
	 * initialize layout of Extractor UI
	 */
	private void initializeComponents() {
		TableLayoutConstraints constraint = new TableLayoutConstraints();
		constraint.row1 = 0;
		constraint.col1 = 0;
		constraint.vAlign = (constraint.hAlign = -1);
		JLabel label = new JLabel("Source Directory");
		getContentPane().add(label, constraint);

		constraint.col1 = 1;
		constraint.row2 = 0;
		constraint.col2 = 2;
		constraint.row1 = 0;

		this.fileSrc = new JTextField();
		this.fileSrc.setSize(30, 100);
		getContentPane().add(this.fileSrc, constraint);

		constraint.col1 = 3;
		constraint.row2 = 0;
		constraint.col2 = 3;
		constraint.row1 = 0;

		this.browseSrc = new JButton("...");
		this.browseSrc.addActionListener(this);

		getContentPane().add(this.browseSrc, constraint);
		constraint.col1 = 0;
		constraint.row2 = 1;
		constraint.col2 = 0;
		constraint.row1 = 1;

		JLabel label2 = new JLabel("Dest. Directory");
		getContentPane().add(label2, constraint);

		constraint.col1 = 1;
		constraint.row2 = 1;
		constraint.col2 = 2;
		constraint.row1 = 1;

		this.fileDst = new JTextField();
		this.fileDst.setSize(30, 100);
		getContentPane().add(this.fileDst, constraint);

		constraint.col1 = 3;
		constraint.row2 = 1;
		constraint.col2 = 3;
		constraint.row1 = 1;

		this.browseDest = new JButton("...");
		this.browseDest.addActionListener(this);
		getContentPane().add(this.browseDest, constraint);

		constraint.col1 = 2;
		constraint.row2 = 3;
		constraint.col2 = 3;
		constraint.row1 = 3;
		this.convert = new JButton("Extract");
		this.convert.addActionListener(this);
		getContentPane().add(this.convert, constraint);
	}

	/***
	 * find all JSON's from sourceFolder and extract all files to targetFolder
	 * 
	 * @param sourceFolder
	 * @param targetFolder
	 * @return true or false based on success or failure in extraction
	 * @throws Exception
	 */
	boolean extract(String sourceFolder, String targetFolder) throws Exception {
		File file = new File(sourceFolder);
		String[] fileList = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		});

		if (fileList == null || fileList.length == 0) {
			JOptionPane.showMessageDialog(this,
					"No json file found in the source directory");
			return false;
		}
		Set<String> errors = new HashSet<String>();
		Set<String> extracted = new HashSet<String>();

		for (String fileName : fileList) {
			System.out.println("File Name:: " + fileName);
			String data = readFile(sourceFolder + "/" + fileName);
			JSONObject frameParent = new JSONObject(new JSONTokener(data));
			JSONObject frames = (JSONObject) frameParent.get("frames");
			File imageFile = new File(sourceFolder + "/"
					+ fileName.replace(".json", EXT));
			if (null == imageFile || !imageFile.exists()) {
				errors.add(fileName);
				continue;

			}
			BufferedImage image = ImageIO.read(imageFile);

			for (@SuppressWarnings("unchecked")
			Iterator<String> keys = (Iterator<String>) frames.keys(); keys
					.hasNext();) {
				String key = keys.next();
				if ("meta".equalsIgnoreCase(key)) {
					continue;
				}
				JSONObject jsImage = frames.getJSONObject(key);

				JSONObject frame = jsImage.getJSONObject("frame");
				System.out.println(" Key :: " + key);

				int width = frame.getInt("w") > 0 ? frame.getInt("w") : 1;
				int height = frame.getInt("h") > 0 ? frame.getInt("h") : 1;
				int x = frame.getInt("x");
				int y = frame.getInt("y");

				BufferedImage bimage = image.getSubimage(x, y, width, height);

				System.out.println("Frame:: " + frame + " width " + width
						+ " height:: " + height);

				ImageIO.write(
						bimage,
						FORMAT,
						new File(targetFolder + "/" + key.replace("/", "SLASH")));

			}
			extracted.add(fileName);
		}
		if (!errors.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Sprite not found for "
					+ errors.toString());
		}
		if (!extracted.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Successfully extracted "
					+ extracted.toString());
		}

		return true;
	}

	static String readFile(String filename) throws IOException {

		FileInputStream fis = new FileInputStream(filename);
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
		String line = null;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		reader.close();
		return builder.toString();
	}

	public void actionPerformed(ActionEvent actionEvent) {
		Object source = actionEvent.getSource();
		if ((source == this.browseDest) || (source == this.browseSrc)) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(1);
			int returnVal = fileChooser.showOpenDialog(this);
			JTextField targetField = source == this.browseDest ? this.fileDst
					: this.fileSrc;

			if (returnVal == 0) {
				File selectedFile = fileChooser.getSelectedFile();
				targetField.setText(selectedFile.getAbsolutePath());
			}

		} else if (source == this.convert) {
			String src = this.fileSrc.getText();
			String dst = this.fileDst.getText();
			if ((src == null) || (dst == null) || (src.isEmpty())
					|| (dst.isEmpty())) {
				JOptionPane.showMessageDialog(this,
						"Please select source folder and destination folder");
			} else {
				File srcFile = new File(src);
				if (!srcFile.exists()) {
					JOptionPane.showMessageDialog(this,
							"Source folder doesn't exist");
					this.fileSrc.requestFocusInWindow();

					return;
				}
				File dstFile = new File(dst);
				if (!dstFile.exists()) {
					JOptionPane.showMessageDialog(this,
							"Destination folder doesn't exist");
					this.fileSrc.requestFocusInWindow();

					return;
				}

				try {
					extract(src, dst);
					// if (extracted) {
					// JOptionPane.showMessageDialog(this,
					// "Sprites extracted successfully");
					// this.fileSrc.requestFocusInWindow();
					// }
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this,
							"An error occured while extracting sprites");

				}
			}
		}
	}
}
