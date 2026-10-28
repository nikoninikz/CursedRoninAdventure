package RogueLike;

import javax.swing.JFrame;

public class Game {

	public static final String TITLE = "Cursed Ronin";

    public void start() {
        JFrame window = new JFrame(TITLE);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        window.setResizable(false);                            

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack();                     
        window.setLocationRelativeTo(null); 
        window.setVisible(true);

        gamePanel.startGameThread();       
    }
}