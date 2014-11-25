package Players;

import Logic.Location;
import Logic.PolarTTT;

import java.awt.*;
import java.awt.event.*;

public class MinimaxPlayer extends Player {


	public void newGame(PolarTTT game, boolean isMaximizer) {
		super.newGame(game, isMaximizer);
		frame = new Frame("Minimax Menu");
		frame.setSize(400, 300);
		frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		frame.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e ) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
					current_menu = (current_menu + 1) & 1;
					break;
				case KeyEvent.VK_LEFT:
					if (current_menu == 0) {
						if (1 < num_plies) {
							num_plies--;
						}
					}
					else {
						use_alpha_beta = !use_alpha_beta;
					}
					break;
				case KeyEvent.VK_RIGHT:
					if (current_menu == 0) {
						if (num_plies < 48) {
							num_plies++;
						}
					}
					else {
						use_alpha_beta = !use_alpha_beta;
					}
					break;
				case KeyEvent.VK_ENTER:
					setup = true;
					synchronized(frame) {
						frame.notifyAll();
					}
					break;
				}
				canvas.repaint();
			}
		});
		
		canvas = new MinimaxCanvas(this);
		frame.add(canvas);
		frame.setVisible(true);
		frame.toFront();
		synchronized(frame) {
			while (!setup) {
				try {
					frame.wait();
				} catch (InterruptedException e) {
					//	Finally done
				}
			}
			setup = true;
		}
		frame.setVisible(false);
	}
	
	@Override
	public Location getChoice(Location[] options) {
		
		char[][] board = game.theoreticalMove(options[0], '.');
		MinimaxNode root = new MinimaxNode(board, is_maximizer);
		
		return bestNode(root, 0).play;
	}
	

	public MinimaxNode bestNode(MinimaxNode current_node, int ply_number) {
		System.out.println("Testing ply " + ply_number + " with move " +
				(current_node.play == null
					? "null"
					: current_node.play.toString()));
		
		//	Base case- cut off search and apply the heuristic function
		if (num_plies < ply_number) {
			System.out.println("Found a move for " + current_node.play.toString());
			current_node.fitness = game.dylanFitness(current_node.board);
			return current_node;
		}
		
		//	Prepare the worst
		MinimaxNode best_node = null;
		int best_fitness = (is_maximizer ? Integer.MIN_VALUE : Integer.MAX_VALUE);
		
		//	Go through all possible moves
		for (int r = 0; r < 4; r++) {
			for (int t = 0; t < 12; t++) {
				Location potential_move = new Location(r, t);
				char[][] theory = game.theoreticalMove(
						current_node.board, potential_move,
						(current_node.is_maximizer ? PolarTTT.PLAYER1 : PolarTTT.PLAYER2));
				
				//	The move was illegal. Ignore it.
				if (theory[r][t] == '!') {
					/*
					for (int r1 = 0; r1 < 4; r1++) {
						for (int c1 = 0; c1 < 12; c1++) {
							System.out.print(theory[r1][c1]);
						}
						System.out.println();
					}
					*/
//					System.out.println("Move at location " + potential_move.toString() + " was null.");
					continue;
				}
				
				//	Now we check the potential of this node
				MinimaxNode potential_node = new MinimaxNode(theory, !current_node.is_maximizer);
				potential_node.play = potential_move;
								
				//	Compare with its best child
				MinimaxNode best_child = bestNode(potential_node, ply_number + 1);
				
				//	If the child didn't help, ignore it
				if (best_child == null) {
					continue;
				}
				
				System.out.println("Best child is not null");
				
				if (potential_node.is_maximizer ?
						best_child.fitness < best_fitness : best_fitness < best_child.fitness) {
					continue;
				}
				
				System.out.println("Found a good node " + best_child.play.toString());
				
				//	Save the best
				best_node = best_child;
				best_fitness = best_child.fitness;
			}
		}
		//	Show what we have (might be null!)
		return best_node;
	}
	
	@Override
	public String getName() {
		return "Minimax " + num_plies + "p" + (use_alpha_beta ? "+AB" : "");
	}
	
	private Frame frame;
	private MinimaxCanvas canvas;
	public int num_plies = 1;
	public boolean use_alpha_beta, setup = false;
	public String[] menues = {"Ply count", "Use Alpha-Beta pruning?"};
	public int current_menu = 0;
}

class MinimaxNode {
	public int fitness = 0;
	char[][] board;
	public Location play;
	public MinimaxNode root, best;
	public boolean is_maximizer;
	public MinimaxNode(char[][] board, boolean is_maximizer){
		this.is_maximizer = is_maximizer;
		this.board = board;
	}
}

class MinimaxCanvas extends Canvas {
	private static final long serialVersionUID = 1L;
	MinimaxPlayer p;
	MinimaxCanvas (MinimaxPlayer p ) {
		this.p = p;
		setBackground(Color.WHITE);
	}
	
	public void paint(Graphics g) {
		g.drawString(p.menues[0], 100, 50);
		g.drawString("<- " + p.num_plies + " ->", 100, 100);
		g.drawString(p.menues[1], 100, 150);
		g.drawString(p.use_alpha_beta ? "Yes Yes Yes!" : "No No No!", 100, 200);
		
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(98, p.current_menu == 0 ? 100 : 200, 50, 3);
	}
}