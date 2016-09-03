import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;

public class Origin extends JFrame {

	private static final long serialVersionUID = 1L;

	public Origin() {
		super("Organic Chemistry - Nomenclature");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		Pidgeon pidgey = new Pidgeon();
		add(pidgey);
		pidgey.setPreferredSize(new Dimension(700,500));
			
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
	}
	
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(() -> {
			new Origin();
		});
	}
	
}
