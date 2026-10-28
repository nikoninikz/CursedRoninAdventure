package RogueLike;	

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
 
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            game.start();
        });
    }
}