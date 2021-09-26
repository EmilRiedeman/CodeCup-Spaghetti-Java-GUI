package spaghetti.gui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

public class SpaghettiInterface extends JFrame {
    public final GraphicalBoard board;
    public final PlayerSelectionPage playerSelection;

    public static final Map<Integer, Color> lightTheme = new HashMap<Integer, Color>(){{
        put(-2, new Color(240, 240, 240)); // BACKGROUND JFRAME
        put(-1, new Color(200, 200, 200)); // BACKGROUND BOARD
        put(0, Color.BLACK);
        put(1, Color.BLUE);
        put(2, Color.RED);
        put(3, new Color(173, 216, 230));
        put(4, new Color(221, 136, 136));
        put(5, Color.GRAY);
        put(6, new Color(255, 255, 237)); // HIGHLIGHTED
        put(7, new Color(170, 170, 170)); // LAST MOVE HIGHLIGHT
        put(8, new Color(160, 160, 160)); // SAMPLE BACKGROUND 1
        put(9, new Color(170, 170, 170)); // SAMPLE BACKGROUND 2
    }};
    public static final Map<Integer, Color> darkTheme = new HashMap<Integer, Color>(){{
        put(-2, new Color(50, 50, 50)); // BACKGROUND JFRAME
        put(-1, new Color(60, 60, 60)); // BACKGROUND BOARD
        put(0, new Color(220, 220, 220));
        put(1, new Color(50, 50, 255));
        put(2, Color.RED);
        put(3, new Color(173, 216, 230));
        put(4, new Color(221, 136, 136));
        put(5, Color.GRAY);
        put(6, new Color(200, 200, 205)); // HIGHLIGHTED
        put(7, new Color(150, 150, 150)); // LAST MOVE HIGHLIGHT
        put(8, new Color(43, 43, 44)); // SAMPLE BACKGROUND 1
        put(9, new Color(60, 63, 65)); // SAMPLE BACKGROUND 2
    }};
    protected Map<Integer, Color> colorPalette;
    protected Page currentPage;

    public void setColorPalette(Map<Integer, Color> palette) {
        colorPalette = palette;
        getContentPane().setBackground(palette.get(-2));
    }

    public void setPage(Page p) {
        if (currentPage != null) currentPage.disablePage(this);
        currentPage = p;
        currentPage.enablePage(this);
    }

    public Page getStartPage() {
        return playerSelection;
    }

    public SpaghettiInterface() {
        super("Spaghetti");
        final int WIDTH = 614, HEIGHT = 800;
        setSize(WIDTH, HEIGHT);
        Dimension size
                = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(size.width/2-WIDTH/2, size.height/2-HEIGHT/2);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (board.board != null) {
                    try {
                        board.board.close();
                    } catch (Exception ignore) {
                    }
                }
                System.exit(0);
            }
        });
        setColorPalette(lightTheme);
        setVisible(true);

        this.board = new GraphicalBoard(this);

        this.playerSelection = new PlayerSelectionPage(this);

        setPage(this.playerSelection);
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings","on");
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        new SpaghettiInterface();
    }
}
