package spaghetti.gui.swing;

import spaghetti.game.Board;
import spaghetti.game.BoardListener;
import spaghetti.game.Move;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

import static java.lang.Math.max;

public class GraphicalBoard extends JComponent implements BoardListener, MouseInputListener, Page {
    protected Board board;
    public final SpaghettiInterface parent;
    public final int
            gridLineWidth = 1, boardOffsetX = 40, boardOffsetY = 40,
            thick = 4, sampleTypeSize = 30, sampleSpacing = 10, turnCircleSize = 22, turnCircleOverlap = 6;
    protected Point highlight = null, sample = null; // sample is bad self made button
    protected char sampleHighlight = '\0';
    public Font font = new Font("Consolas", Font.PLAIN, 20);

    public GraphicalBoard(SpaghettiInterface parent) {
        this.parent = parent;
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setBoard(Board board) {
        if (this.board != null) this.board.removeBoardListener(this);
        this.board = board;
        if (board != null) board.addBoardListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (board == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(parent.colorPalette.get(-1));
        g2.translate(boardOffsetX, boardOffsetY);
        g2.clearRect(0, 0, getBoardWidth(), getBoardHeight());

        // Alphabet:
        int charWidth = g2.getFontMetrics(font).charWidth('a');
        int charHeight = charWidth * 2;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(font);
        g2.setColor(parent.colorPalette.get(0));
        for (int i = 0; i < max(board.width, board.height); ++i) {
            char c = (char) (i + 'a');
            if (i < board.width) {
                g2.drawChars(new char[]{c},
                        0, 1,
                        boardOffsetX + i * tileSize() - charWidth,
                        -(boardOffsetY / 2));
            }
            if (i < board.height) {
                g2.drawChars(new char[]{c},
                        0, 1,
                        -(boardOffsetX / 2) - charWidth,
                        boardOffsetY + i * tileSize());
            }
        }

        // Score:
        String scoreBlue = "" + board.scores[0], scoreRed = "" + board.scores[1];
        g2.setColor(parent.colorPalette.get(1));
        g2.drawString(scoreBlue,
                -(boardOffsetX / 2) - scoreBlue.length() * charWidth / 2,
                board.height * tileSize() + boardOffsetY - charHeight / 2);
        g2.setColor(parent.colorPalette.get(2));
        g2.drawString(scoreRed,
                board.width * tileSize() + (boardOffsetX / 2) - scoreRed.length() * charWidth / 2,
                board.height * tileSize() + boardOffsetY - charHeight / 2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        for (int row = 0; row < board.height; ++row) for (int col = 0; col < board.width; ++col) {
            drawTile(g2, row, col);
        }

        // Plus Dots:
        g2.setColor(parent.colorPalette.get(0));
        for (int row = 0; row < board.height + 1; ++row) for (int col = 0; col < board.width + 1; ++col) {
            int x = col * tileSize();
            int y = row * tileSize();
            int plusSize = 10;
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x - plusSize / 2, y, x + plusSize / 2, y);
            g2.drawLine(x, y - plusSize / 2, x, y + plusSize / 2);
        }

        // Borders:
        g2.setStroke(new BasicStroke(thick * 2));
        g2.setColor(parent.colorPalette.get(5));
        g2.drawLine(-thick, -thick, getBoardWidth()+thick, -thick);
        g2.drawLine(-thick, getBoardHeight()+thick, getBoardWidth()+thick, getBoardHeight()+thick);

        g2.setColor(parent.colorPalette.get(1));
        g2.drawLine(-thick, -thick, -thick, getBoardHeight()+thick);

        g2.setColor(parent.colorPalette.get(2));
        g2.drawLine(getBoardWidth()+thick, -thick, getBoardWidth()+thick, getBoardHeight()+thick);

        // Turn Circles:
        if (board.isGameStarted()) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (board.getTurn()) {
                g2.setColor(parent.colorPalette.get(1));
                g2.fillArc(getBoardWidth() / 2 - turnCircleSize + turnCircleOverlap, getBoardHeight() + boardOffsetY / 2 - turnCircleSize / 2, turnCircleSize, turnCircleSize, 0, 360);
                g2.setColor(parent.colorPalette.get(2));
                g2.fillArc(getBoardWidth() / 2 - turnCircleOverlap, getBoardHeight() + boardOffsetY / 2 - turnCircleSize / 2, turnCircleSize, turnCircleSize, 0, 360);
            } else {
                g2.setColor(parent.colorPalette.get(2));
                g2.fillArc(getBoardWidth() / 2 - turnCircleOverlap, getBoardHeight() + boardOffsetY / 2 - turnCircleSize / 2, turnCircleSize, turnCircleSize, 0, 360);
                g2.setColor(parent.colorPalette.get(1));
                g2.fillArc(getBoardWidth() / 2 - turnCircleSize + turnCircleOverlap, getBoardHeight() + boardOffsetY / 2 - turnCircleSize / 2, turnCircleSize, turnCircleSize, 0, 360);
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        if (!board.isGameStarted()) {

        }

        // Sample:
        if (sample != null) drawSample(g2);
    }

    protected void drawSample(Graphics2D g2) {
        if (sample.x + getSampleWidth() > getTotalWidth()) sample.x -= sample.x + getSampleWidth() - getTotalWidth();
        int x = sample.x - boardOffsetX, y = sample.y - boardOffsetY;
        Color c1 = parent.colorPalette.get(0), c2 = parent.colorPalette.get(9), c3 = parent.colorPalette.get(6);
        g2.setColor(parent.colorPalette.get(8));
        g2.fillRect(x, y, getSampleWidth(), getSampleHeight());
        g2.setColor(c1);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(x, y, getSampleWidth(), getSampleHeight());

        g2.drawRect(x + sampleSpacing, y + sampleSpacing, sampleTypeSize, sampleTypeSize);
        drawTileType(g2, 'l', x + sampleSpacing, y + sampleSpacing, sampleTypeSize, c1, c1, sampleHighlight == 'l'? c3: c2);
        g2.drawRect(x + sampleSpacing * 2 + sampleTypeSize, y + sampleSpacing, sampleTypeSize, sampleTypeSize);
        drawTileType(g2, 's', x + sampleSpacing * 2 + sampleTypeSize, y + sampleSpacing, sampleTypeSize, c1, c1, sampleHighlight == 's'? c3: c2);
        g2.drawRect(x + sampleSpacing * 3 + sampleTypeSize * 2, y + sampleSpacing, sampleTypeSize, sampleTypeSize);
        drawTileType(g2, 'r', x + sampleSpacing * 3 + sampleTypeSize * 2, y + sampleSpacing, sampleTypeSize, c1, c1, sampleHighlight == 'r'? c3: c2);
    }

    protected boolean inSample(Point p) {
        if (sample == null) return false;
        return (p.x >= sample.x && p.x <= sample.x + getSampleWidth() && p.y >= sample.y && p.y <= sample.y + getSampleHeight());
    }

    protected Point getRelativeSamplePosition(Point p) {
        return new Point(p.x - sample.x, p.y - sample.y);
    }

    protected char getTypeFromSample(Point p) {
        Point r = getRelativeSamplePosition(p);
        r.x -= sampleSpacing;
        r.y -= sampleSpacing;
        if (r.y < 0 || r.y > sampleTypeSize || r.x < 0) return '\0';
        if (r.x <= sampleTypeSize) return 'l';
        if (sampleTypeSize + sampleSpacing <= r.x && r.x <= sampleTypeSize * 2 + sampleSpacing) return 's';
        if (sampleTypeSize * 2 + sampleSpacing * 2 <= r.x && r.x <= sampleTypeSize * 3 + sampleSpacing * 2) return 'r';
        return '\0';
    }

    public int getSampleWidth() {
        return sampleSpacing * 4 + sampleTypeSize * 3;
    }

    public int getSampleHeight() {
        return sampleSpacing * 2 + sampleTypeSize;
    }

    public int tileSize() {
        return 74;
    }

    public int getBoardWidth() {
        return tileSize() * board.width;
    }

    public int getBoardHeight() {
        return tileSize() * board.height;
    }

    public int getTotalWidth() {
        return getBoardWidth() + boardOffsetX * 2 + 16;
    }

    public int getTotalHeight() {
        return getBoardHeight() + boardOffsetY * 2 + 39;
    }

    public static void drawTileType(Graphics2D g2, char type, int x, int y, int box, Color c0, Color c1, Color bg) {
        Stroke restoredStroke = g2.getStroke();
        Shape restoredClip = g2.getClip();
        Color restoredColor = g2.getColor();

        g2.setClip(x-1, y-1, box+2, box+2);
        g2.setColor(bg);
        g2.fillRect(x, y, box, box);
        g2.setStroke(new BasicStroke(box / 8f));

        switch (type) {
            case 'l':
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c0);
                g2.drawArc(x-box/2, y-box/2, box, box, 270, 90);
                g2.setColor(c1);
                g2.drawArc(x + box/2, y + box/2, box, box, 90, 90);
                break;
            case 's':
                int bridge = box/6, up = box/12, extra = box/8;
                g2.setColor(c0);
                g2.drawLine(x + box/2, y, x + box/2, y + box);
                g2.setColor(bg);
                g2.fillRect(x + box/4, y + box/2 - 2*up - extra, box/2, (int)(2.5*extra));

                // SVG Path:
                // "M " + x + " ," + (y+box/2) + " h " + (box-4*bridge)/2 + " q " + bridge*0.5 + " 0, " +
                // bridge + " -" + up + " t " + bridge + " -" + up + " t " + bridge + " " + up + " t " + bridge + " " + up + " h " + (box-4*bridge)/2

                GeneralPath p = new GeneralPath();
                float X, Y, pX, pY;
                p.moveTo(X = x, Y=(y+box/2f));// M
                p.lineTo(X+=((box-4*bridge)/2f), Y);// h
                p.quadTo(pX = (X + bridge/2f), pY = Y, X+=bridge, Y-=up);// q

                p.quadTo(pX = X * 2 - pX, pY = Y * 2 - pY, X+=bridge, Y-=up);// t
                p.quadTo(pX = X * 2 - pX, pY = Y * 2 - pY, X+=bridge, Y+=up);// t
                p.quadTo(X * 2 - pX, Y * 2 - pY, X+=bridge, Y+=up);// t

                p.lineTo(X + (box-4*bridge)/2f, Y);// h

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c1);
                g2.draw(p);
                break;
            case 'r':
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c0);
                g2.drawArc(x - box/2 + box, y-box/2, box, box, 180, 90);
                g2.setColor(c1);
                g2.drawArc(x - box/2, y - box/2 + box, box, box, 0, 90);
                break;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g2.setClip(restoredClip);
        g2.setStroke(restoredStroke);
        g2.setColor(restoredColor);
    }

    protected void drawTile(Graphics2D g2, int row, int col) {
        Color   color0 = parent.colorPalette.get(board.tiles[row][col].color[0]),
                color1 = parent.colorPalette.get(board.tiles[row][col].color[1]);
        char type = board.tiles[row][col].type;

        int tileSize = tileSize();

        int x = col * tileSize;
        int y = row * tileSize;
        int x0 = x + gridLineWidth, y0 = y + gridLineWidth, s = tileSize - gridLineWidth * 2;

        Point p = new Point(row, col);
        if (p.equals(highlight)) {
            g2.setColor(parent.colorPalette.get(6));
            g2.fillRect(x, y, s + gridLineWidth * 2, s + gridLineWidth * 2);
        } else if (sample != null && p.equals(getBoardPosition(sample.x, sample.y))) {
            g2.setColor(parent.colorPalette.get(board.getTurn()? 4: 3));
            g2.fillRect(x, y, s + gridLineWidth * 2, s + gridLineWidth * 2);
        } else {
            drawTileType(g2, type, x0, y0, s, color0, color1, parent.colorPalette.get(-1));
        }

        g2.setStroke(new BasicStroke(gridLineWidth));

        g2.setColor(Color.BLACK);
        if (type != '\0') g2.drawRect(x, y, tileSize-gridLineWidth, tileSize-gridLineWidth);
    }

    public Point getBoardPosition(int x, int y) {
        x -= boardOffsetX;
        y -= boardOffsetY;
        if (x < 0 || y < 0) return null;
        Point p = new Point(y / tileSize(), x / tileSize());
        if (p.x >= board.height || p.y >= board.width) return null;
        return p;
    }

    @Override
    public void registerMove(Move m, BoardListener l) {
        revalidate();
        repaint();
    }

    @Override
    public void start() {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isStartHandler() {
        return false;
    }

    @Override
    public String getControllerName() {
        return "GUI Player";
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (board == null || !board.isGameStarted() || board.getControllerTurn() != this) return;
        if (inSample(e.getPoint())) {
            char t = getTypeFromSample(e.getPoint());
            Point s = getBoardPosition(sample.x, sample.y);
            if (t != '\0') board.play(new Move(s.x, s.y, t), this);
            sampleHighlight = '\0';
            sample = null;
            repaint();
            return;
        }
        sampleHighlight = '\0';
        sample = null;
        Point p = getBoardPosition(e.getX(), e.getY());
        if (p != null && (board.isOccupied(p.x, p.y) || !p.equals(highlight))) p = null;
        highlight = null;
        if (p != null) {
            sample = e.getPoint();
        }
        mouseMoved(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (inSample(e.getPoint())) {
            highlight = null;
            sampleHighlight = getTypeFromSample(e.getPoint());
            repaint();
            return;
        }
        sampleHighlight = '\0';
        highlight = getBoardPosition(e.getX(), e.getY());
        if (highlight != null && (board.isOccupied(highlight.x, highlight.y) ||
                (sample != null && highlight.equals(getBoardPosition(sample.x, sample.y))))) highlight = null;
        repaint();
    }

    @Override
    public void enablePage(JFrame frame) {
        frame.getContentPane().add(this);
        repaint();
        revalidate();
    }

    @Override
    public void disablePage(JFrame frame) {
        frame.getContentPane().removeAll();
    }
}
