import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import pan.cache.loader.Cache;

public class Pidgeon extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private Visualizer vis;
	private JLabel lPoints;
	private JLabel lQuestion;
	private boolean started = false;
	private String answer = null;
	private long startTime = 0;
	private int points, question;
	private boolean waiting = false;
	private boolean finished = false;
	private int highscore = Integer.MIN_VALUE;
	private JLabel lHighscore;
	
	private void onAction(String input) {
		if (finished && !input.equals("restart")) return;
		if (input.isEmpty()) {
			if (waiting) {
				vis.setMessage(Util.randomMessage());
				nextQuestion();
				return;
			} else {
				return;
			}
		}
		switch (input) {
		case "start":
			if (started) {
				vis.setMessage("Already started. Enter 'restart' to restart.");
			} else {
				start();
			}
			break;
		case "restart":
			if (started) {
				restart();
			} else {
				vis.setMessage("Nothing has started yet. Enter 'start' to start.");
			}
			break;
		default:
			if (waiting) {
				vis.setMessage("Press [ENTER] to go to the next question.");
				textField.setText("");
				return;
			}
			if (started) {
				check(input);
			} else {
				vis.setMessage("Nothing has started yet. Enter 'start' to start.");
			}
		}
		textField.setText("");
	}
	
	private void clearVars() {
		setPoints(0);
		setQuestion(1);
		started = false;
		answer = null;
		startTime = 0;
		points = 0;
		question = 0;
		waiting = false;
		finished = false;
	}
	
	private void start() {
		Cache<Integer> hs = new Cache<>(Cache.getFolderInHome("chemgen"), "highscore");
		if (hs.exists()) {
			this.highscore = hs.loadCache();
			setHighscore(highscore);
		}
		started = true;
		nextQuestion();
		vis.setMessage("Name this molecule. Points are calculated using time and accuracy of answers.");
	}
	
	private void restart() {
		clearVars();
		start();
	}
	
	private void nextQuestion() {
		if (question == 20) {
			double average = points / 20.0;
			vis.setMessage("Finished 20. Enter 'restart' to restart. Avg: "+Util.round(average, 2));
			finished = true;
			return;
		}
		waiting = false;
		++question;
		setQuestion(question);
		answer = Util.generate();
		vis.setImage(answer);
		startTime = System.currentTimeMillis();
		
		if (points > highscore) {
			Cache<Integer> hs = new Cache<>(Cache.getFolderInHome("chemgen"), "highscore");
			highscore = points;
			setHighscore(highscore);
			hs.saveCache(highscore);
		}
	}
	
	private void check(String input) {
		double time = System.currentTimeMillis() - startTime;
		double index = Util.cosineSimilarity(input, answer);
		double timescore = 40.0 - time / 1000.0;
		double pass = 0.9;
		double multiplier = (index - pass)/(1.0-pass);
		double score = multiplier > 0 ? multiplier * timescore : multiplier * (40.0 - timescore);
		if (index == 1) {
			vis.setMessage("Correct! Press [ENTER] to go to the next question.");
			vis.setColorType(Visualizer.CORRECT);
		}
		else if (index >= 0.9) {
			vis.setMessage("Almost! Answer: "+answer);
			vis.setColorType(Visualizer.INCORRECT);
		}
		else {
			vis.setMessage("Not quite... Answer: "+answer);
			vis.setColorType(Visualizer.INCORRECT);
		}
		points += (int) (score);
		setPoints(points);
		waiting = true;
	}
	
	private void setPoints(int points) {
		lPoints.setText("Points: "+points);
		repaint();
	}
	
	private void setQuestion(int question) {
		lQuestion.setText("Question "+question);
		repaint();
	}
	
	private void setHighscore(int highscore) {
		lHighscore.setText("Highscore: "+highscore);
		repaint();
	}
	
	public Pidgeon() {
		lPoints = new JLabel("Points: 0");
		lPoints.setFont(new Font("Segoe UI Light", Font.PLAIN, 16));
		
		textField = new JTextField();
		vis = new Visualizer();
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onAction(textField.getText().trim().toLowerCase());
			}
		});
		textField.setFont(new Font("Segoe UI Light", Font.PLAIN, 18));
		textField.setColumns(10);
		
		lQuestion = new JLabel("Question 1");
		lQuestion.setFont(new Font("Segoe UI Light", Font.PLAIN, 16));
		
		lHighscore = new JLabel("Highscore: 0");
		lHighscore.setFont(new Font("Segoe UI Light", Font.PLAIN, 16));
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(vis, GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
						.addComponent(textField, GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lQuestion)
							.addPreferredGap(ComponentPlacement.RELATED, 588, Short.MAX_VALUE)
							.addComponent(lHighscore)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lPoints)))
					.addGap(18))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lQuestion, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addComponent(lPoints)
						.addComponent(lHighscore))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(vis, GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
					.addContainerGap())
		);
		setLayout(groupLayout);
	}
}
