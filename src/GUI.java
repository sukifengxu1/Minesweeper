import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI extends JFrame {
	private Date startDate = new Date();
	private Date endDate;
	
	private boolean reset = false;
	private boolean flag = false;
	
	public boolean isReset() {
		return reset;
	}

	private int spacing = 5;
	
	private int mx = -100;
	private int my = -100;
	
	private Random rand = new Random();
	
	private int smileyX = 605;
	private int smileyY = 5;
	private boolean happy = true;
	private int smileyCenterX = smileyX + 35;
	private int smileyCenterY = smileyY + 35;
	
	private int flagX = 445;
	private int flagY = 5;
	private int flagCenterX = flagX + 35;
	private int flagCenterY = flagY + 35;
	
	private int spacingX = 90;
	private int spacingY = 10;
	private int minusX = spacingX + 160;
	private int minusY = spacingY;
	private int plusX = spacingX + 240;
	private int plusY = spacingY;
	
	private boolean won = false;
	private boolean lost = false;
	
	private int timeX = 1130;
	private int timeY = 5;
	private int sec = 0;
	
	private int messageX = 740;
	private int messageY = -50;
	private String message = "Nothing yet!";
	
	boolean[][] mines = new boolean[16][9];
	int[][] neighbors = new int[16][9];
	boolean[][] revealed = new boolean[16][9];
	boolean[][] flagged = new boolean[16][9];
	
	public GUI() {
		setTitle("Minesweeper");
		setSize(1280, 1000);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setResizable(false);
		
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 9; j++) {
				mines[i][j] = rand.nextInt(100) < 20? true : false;
				revealed[i][j] = false;
			}
		}
		
		int neighs;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 9; j++) {
				neighs = 0;
				for (int m = i - 1; m < i + 2; m++) {
					for (int n = j - 1; n < j + 2; n++) {
						if (m != i || n != j) {
							if (isN(i, j, m, n)) neighs++;
						}
					}
				}
				neighbors[i][j] = neighs;
			}
		}
		
		Board board = new Board();
		setContentPane(board);
		
		Move move = new Move();
		addMouseMotionListener(move);
		
		Click click = new Click();
		addMouseListener(click);
	}
	
	public void checkWon() {
		if (!lost) {
			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 9; j++) {
					if (mines[i][j] && revealed[i][j]) {
						lost = true;
						happy = false;
						endDate = new Date();
					}
				}
			}
		}
		
		if(!won && totalBoxesRevealed() + totalMines() == 144) {
			won = true;
			endDate = new Date();
		}
	}
	
	public int totalMines() {
		int total = 0;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 9; j++) {
				if (mines[i][j]) total++;
			}
		}
		return total;
	}
	
	public int totalBoxesRevealed() {
		int total = 0;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 9; j++) {
				if (revealed[i][j]) total++;
			}
		}
		return total;
	}
	
	public void resetAll() {
		reset = true;
		flag = false;
		startDate = new Date();
		messageY = -50;
		message = "Nothing yet!";
		happy = true;
		won = false;
		lost = false;
		
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 9; j++) {
				mines[i][j] = rand.nextInt(100) < 20? true : false;
				revealed[i][j] = false;
				flagged[i][j] = false;
			}
		}
		
		int neighs;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 9; j++) {
				neighs = 0;
				for (int m = i - 1; m < i + 2; m++) {
					for (int n = j - 1; n < j + 2; n++) {
						if (m != i || n != j) {
							if (isN(i, j, m, n)) neighs++;
						}
					}
				}
				neighbors[i][j] = neighs;
			}
		}
		
		reset = false;
	}
	
	public boolean inSmiley() {
		int x = Math.abs(mx - smileyCenterX);
		int y = Math.abs(my - 25 - smileyCenterY);
		int dis = (int)Math.sqrt(x * x + y * y);
		return dis < 35;
	}
	
	public boolean inFlag() {
		int x = Math.abs(mx - flagCenterX);
		int y = Math.abs(my - 25 - flagCenterY);
		int dis = (int)Math.sqrt(x * x + y * y);
		return dis < 35;
	}
	
	public int inBoxX() {
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 9; j++) {
				if (mx >= spacing + i * 80 && mx < i * 80 + 80 - spacing && my >= spacing + j * 80 + 80 + 26 && my < j * 80 + 26 + 80 + 80 - spacing) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public int inBoxY() {
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 9; j++) {
				if (mx >= spacing + i * 80 && mx < i * 80 + 80 - spacing && my >= spacing + j * 80 + 80 + 26 && my < j * 80 + 26 + 80 + 80 - spacing) {
					return j;
				}
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * @param mX main box
	 * @param mY
	 * @param cX current box
	 * @param cY
	 * @return if current box contains mine and is a neighbor of main box
	 */
	public boolean isN(int mX, int mY, int cX, int cY) {
		// We know that (m != i || n != j)
		if (cX >= 0 && cX < 16 && cY >= 0 && cY < 9 && mines[cX][cY] && Math.abs(mX - cX) < 2 && Math.abs(mY - cY) < 2) {
			return true;
		}
		return false;
	}
	
	public class Board extends JPanel {
		
		public void paintComponent(Graphics g) {
//			g.setColor(Color.DARK_GRAY);
//			g.fillRect(0, 0, 1280, 800);
			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 9; j++) {
					g.setColor(Color.GRAY);
//					if (mines[i][j]) {
//						g.setColor(Color.YELLOW);
//					}
					if (revealed[i][j]) {
						g.setColor(Color.WHITE);
						if (mines[i][j]) {
							g.setColor(Color.RED);
						}
					}
					if (mx >= spacing + i * 80 && mx < i * 80 + 80 - spacing && my >= spacing + j * 80 + 80 + 26 && my < j * 80 + 26 + 80 + 80 - spacing) {
						g.setColor(Color.LIGHT_GRAY);
					}
					g.fillRect(spacing + i * 80, spacing + j * 80 + 80, 80 - 2 * spacing, 80 - 2 * spacing);
					if (revealed[i][j]) {
						g.setColor(Color.BLACK);
						if (!mines[i][j] && neighbors[i][j] != 0) {
							switch (neighbors[i][j]) {
								case 1: g.setColor(Color.BLUE);
										break;
								case 2: g.setColor(Color.GREEN);
										break;
								case 3: g.setColor(Color.RED);
										break;
								case 4: g.setColor(new Color(0, 0, 128)); // Navy
										break;
								case 5: g.setColor(new Color(178, 34, 34)); // Firebrick
										break;
								case 6: g.setColor(new Color(72, 209, 204)); // Medium Turquoise
										break;
								case 8: g.setColor(Color.DARK_GRAY);
										break;
							}
							g.setFont(new Font("Tahoma", Font.BOLD, 40));
							g.drawString(Integer.toString(neighbors[i][j]), i * 80 + 27, j * 80 + 80 + 55);
						} else if (mines[i][j]){ // draw bomb
							g.fillRect(i * 80 + 10 + 20, j * 80 + 80 + 20, 20, 40);
							g.fillRect(i * 80 + 20, j * 80 + 80 + 10 + 20, 40, 20);
							g.fillRect(i * 80 + 5 + 20, j * 80 + 80 + 5 + 20, 30, 30);
							g.fillRect(i * 80 + 38, j * 80 + 80 + 15, 4, 50);
							g.fillRect(i * 80 + 15, j * 80 + 80 + 38, 50, 4);
						}
					}
					// flags
					if (flagged[i][j]) {
						g.setColor(Color.BLACK);
						g.fillRect(i * 80 + 32 + 5, j * 80 + 80 + 15 + 5, 5, 40);
						g.fillRect(i * 80 + 20 + 5, j * 80 + 80 + 50 + 5, 30, 10);
						g.setColor(Color.RED);
						g.fillRect(i * 80 + 16 + 5, j * 80 + 80 + 15 + 5, 20, 15);
						g.setColor(Color.BLACK);
						g.drawRect(i * 80 + 16 + 5, j * 80 + 80 + 15 + 5, 20, 15);
						g.drawRect(i * 80 + 17 + 5, j * 80 + 80 + 16 + 5, 18, 13);
						g.drawRect(i * 80 + 18 + 5, j * 80 + 80 + 17 + 5, 16, 11);
					}
				}
			}
			//spacing minus-plus painting
			g.setColor(Color.BLACK);
			g.fillRect(spacingX, spacingY, 300, 60);
			g.setColor(Color.WHITE);
			g.fillRect(minusX + 5, minusY + 10, 40, 40);
			g.fillRect(plusX + 5, plusY + 10, 40, 40);
			
			g.setFont(new Font("Tahoma", Font.PLAIN, 35));
			g.drawString("Spacing", spacingX + 20, spacingY + 45);
			g.setFont(new Font("Tahoma", Font.PLAIN, 30));
			if (spacing < 10) {
				g.drawString("0" + Integer.toString(spacing), minusX + 49, minusY + 40);
			} else {
				g.drawString(Integer.toString(spacing), minusX + 49, minusY + 40);
			}
			
			g.setColor(Color.BLACK);
			g.fillRect(minusX + 15, minusY + 27, 20, 6);
			g.fillRect(plusX + 15, plusY + 27, 20, 6);
			g.fillRect(plusX + 22, plusY + 20, 6, 20);
			
			// smiley painting
			g.setColor(Color.YELLOW);
			g.fillOval(smileyX, smileyY, 70, 70);
			
			g.setColor(Color.BLACK);
			g.fillOval(smileyX + 15, smileyY + 20, 10, 10);
			g.fillOval(smileyX + 45, smileyY + 20, 10, 10);
			
			if (happy) {
				g.fillRect(smileyX + 20, smileyY + 50, 30, 5);
				g.fillRect(smileyX + 17, smileyY + 45, 5, 5);
				g.fillRect(smileyX + 48, smileyY + 45, 5, 5);
			} else {
				g.fillRect(smileyX + 20, smileyY + 45, 30, 5);
				g.fillRect(smileyX + 17, smileyY + 50, 5, 5);
				g.fillRect(smileyX + 48, smileyY + 50, 5, 5);
			}
			
			//Flag painting
			g.setColor(Color.BLACK);
			g.fillRect(flagX + 32, flagY + 15, 5, 40);
			g.fillRect(flagX + 20, flagY + 50, 30, 10);
			g.setColor(Color.RED);
			g.fillRect(flagX + 16, flagY + 15, 20, 15);
			g.setColor(Color.BLACK);
			g.drawRect(flagX + 16, flagY + 15, 20, 15);
			g.drawRect(flagX + 17, flagY + 16, 18, 13);
			g.drawRect(flagX + 18, flagY + 17, 16, 11);
			
			if (flag) {
				g.setColor(Color.RED);
			}
			
			g.drawOval(flagX, flagY, 70, 70);
			g.drawOval(flagX + 1, flagY + 1, 68, 68);
			g.drawOval(flagX + 2, flagY + 2, 66, 66);
			
			// Time counter painting
			g.setColor(Color.BLACK);
			g.fillRect(timeX, timeY, 140, 70);
			if (!won && !lost) {
				sec = (int)((new Date().getTime() - startDate.getTime()) / 1000);
			}
			if (sec > 999) sec = 999;
			g.setColor(Color.WHITE);
			if (won) {
				g.setColor(Color.GREEN);
			} else if (lost) {
				g.setColor(Color.RED);
			}
			g.setFont(new Font("Tahoma", Font.PLAIN, 80));
			if (sec < 10) {
				g.drawString("00" + Integer.toString(sec), timeX, timeY + 65);
			} else if (sec < 100) {
				g.drawString("0" + Integer.toString(sec), timeX, timeY + 65);
			} else {
				g.drawString(Integer.toString(sec), timeX, timeY + 65);
			}
			
			// Won message painting
			if (won) {
				g.setColor(Color.GREEN);
				message = "YOU WIN";
			} else if (lost) {
				g.setColor(Color.RED);
				message = "YOU LOSE";
			}
			if (won || lost) {
				messageY = -50 + (int)((new Date().getTime() - endDate.getTime()) / 10);
				if (messageY > 67) messageY = 67;
				g.setFont(new Font("Tahoma", Font.PLAIN, 70));
				g.drawString(message, messageX, messageY);
			}
		}
	}
	
	public class Move implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mx = e.getX();
			my = e.getY();
		}
		
	}
	
	public class Click implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			mx = e.getX();
			my = e.getY();
			
			if (mx > minusX + 5 && mx < minusX + 45 && my - 25 > minusY + 10 && my - 25 < minusY + 50 && spacing > 1) {
				spacing--;
			} else if (mx > plusX + 5 && mx < plusX + 45 && my - 25 > plusY + 10 && my - 25 < plusY + 50 && spacing < 15) {
				spacing++;
			}
			System.out.println(mx + ", " + my);
			if (inBoxX() != -1 && inBoxY() != -1) {
				
				if (flag && !revealed[inBoxX()][inBoxY()]) {
					flagged[inBoxX()][inBoxY()] = !flagged[inBoxX()][inBoxY()];
				} else {
					if (!flagged[inBoxX()][inBoxY()]) {
						revealed[inBoxX()][inBoxY()] = true;
					}
				}
				
				System.out.println("click in " + inBoxX() + " and " + inBoxY());
			} else {
				System.out.println("not in boxes");
			}
			
			if (inSmiley()) {
				resetAll();
				System.out.println("click in smiley");
			} else {
				System.out.println("not smiley");
			}
			
			if (inFlag()) {
				flag = !flag;
				System.out.println("click in flag");
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
