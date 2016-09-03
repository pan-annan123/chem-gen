import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Visualizer extends JPanel {

	private static final long serialVersionUID = 1L;
	private Image img;
	private String message = "Enter 'start' to start.";
	public static final int DEFAULT = -1;
	public static final int INCORRECT = 0;
	public static final int CORRECT = 1;
	private int color = DEFAULT;
	
	public Visualizer() {
		setBackground(Color.WHITE);
	}
	
	public void setImage(String molecule) {
		try {
			load("http://opsin.ch.cam.ac.uk/opsin/"+URLEncoder.encode(molecule,"UTF-8")+".png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setMessage(String message) {
		this.message = message;
		color = DEFAULT;
		repaint();
		revalidate();
	}
	
	public void setColorType(int type) {
		color = type;
	}
	
	private void load(String imgURL) throws IOException {
		img = ImageIO.read(new URL(imgURL));
		repaint();
		revalidate();
	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D)graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		
		if (img != null)
			g.drawImage(img, (getWidth()-img.getWidth(this))/2, (getHeight()-img.getHeight(this))/2, this);
		if (message != null) {
			g.setFont(new Font("Segoe UI Light", Font.PLAIN, 14));
			switch (color) {
			case INCORRECT:
				g.setColor(Color.RED);
				break;
			case CORRECT:
				g.setColor(Color.GREEN);
				break;
			default:
			}
			g.drawString(message, 10, 20);
		}
	}
}
