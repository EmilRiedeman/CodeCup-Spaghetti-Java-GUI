package spaghetti.gui.swing;

import spaghetti.game.Board;
import spaghetti.game.BoardController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class PlayerSelectionPage implements Page {
    public final SpaghettiInterface parent;
    public final JPanel mainPanel = new JPanel();

    public final JPanel selectorPanel = new JPanel();
    public final PlayerSelection[] selectors = new PlayerSelection[] {
            new PlayerSelection("Player 1", this),
            new PlayerSelection("Player 2", this)
    };

    public final JPanel submitPanel = new JPanel();
    public final JButton submitButton = new JButton("Start");
    public final JLabel darkModeLabel = new JLabel("Dark Mode:");
    public final JCheckBox darkModeCheckBox = new JCheckBox();
    public final JLabel matchLabel = new JLabel("Match:");
    public final JComboBox<String> matchComboBox = new JComboBox<>(new String[]{"Player 1 vs Player 2", "Player 2 vs Player 1", "Random"});
    public final JLabel prePlayedMovesLabel = new JLabel("Pre Played Moves:");
    public final JCheckBox prePlayedMovesCheckBox = new JCheckBox() {{
        setSelected(true);
    }};
    public final JLabel boardSizeLabel = new JLabel("Board Size:");
    public final JTextField boardWidthField = new JTextField("7");
    public final JTextField boardHeightField = new JTextField("9");

    public final JLabel boardSidesLabel = new JLabel("Sides:");

    public PlayerSelectionPage(SpaghettiInterface parent) {
        this.parent = parent;

        mainPanel.setFocusable(true);
        selectorPanel.setLayout(new GridLayout(1, 2));
        selectorPanel.add(selectors[0]);
        selectorPanel.add(selectors[1]);

        updateSettings();
        submitButton.setPreferredSize(new Dimension(120, 40));
        submitButton.setFont(submitButton.getFont().deriveFont(20f));
        submitButton.addActionListener(this::onSubmit);
        darkModeCheckBox.addActionListener(e -> {
            if (darkModeCheckBox.isSelected()) parent.setColorPalette(SpaghettiInterface.darkTheme);
            else parent.setColorPalette(SpaghettiInterface.lightTheme);
        });

        GroupLayout submitLayout = new GroupLayout(submitPanel);
        submitLayout.setAutoCreateGaps(true);
        submitLayout.setHorizontalGroup(
                submitLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup(submitLayout.createSequentialGroup()
                                .addGroup(submitLayout.createParallelGroup()
                                        .addComponent(darkModeLabel)
                                        .addComponent(matchLabel)
                                        .addComponent(prePlayedMovesLabel)
                                        .addComponent(boardSizeLabel)
                                )
                                .addGap(30)
                                .addGroup(submitLayout.createParallelGroup()
                                        .addComponent(darkModeCheckBox)
                                        .addComponent(matchComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(prePlayedMovesCheckBox)
                                        .addGroup(submitLayout.createSequentialGroup()
                                                .addComponent(boardWidthField, 50, 50, 50)
                                                .addGap(20)
                                                .addComponent(boardHeightField, 50, 50, 50)
                                        )
                                )
                        )
                        .addComponent(submitButton)
        );

        submitLayout.setVerticalGroup(
                submitLayout.createSequentialGroup()
                        .addGap(10)
                        .addGroup(submitLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(darkModeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(darkModeCheckBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGap(20)
                        .addGroup(submitLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(matchLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(matchComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(submitLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(prePlayedMovesLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(prePlayedMovesCheckBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(submitLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(boardSizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(boardWidthField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(boardHeightField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addComponent(submitButton)
        );

        submitPanel.setLayout(submitLayout);

        GroupLayout mainLayout = new GroupLayout(mainPanel);
        mainLayout.setHorizontalGroup(
                mainLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(selectorPanel)
                        .addComponent(submitPanel)
        );
        mainLayout.setVerticalGroup(
                mainLayout.createSequentialGroup()
                        .addComponent(selectorPanel)
                        .addComponent(submitPanel)
        );

        mainPanel.setLayout(mainLayout);
    }

    public void updateSettings() {
        submitButton.setEnabled(!(selectors[0].isStartHandler() && selectors[1].isStartHandler()));
        matchComboBox.setEnabled(!(selectors[0].isStartHandler() || selectors[1].isStartHandler()));

        boolean flexible = selectors[0].isFlexible() && selectors[1].isFlexible();
        prePlayedMovesCheckBox.setEnabled(flexible);
        boardHeightField.setEnabled(flexible);
        boardWidthField.setEnabled(flexible);
        if (!flexible) {
            prePlayedMovesCheckBox.setSelected(true);
            boardWidthField.setText("7");
            boardHeightField.setText("9");
        }
    }

    private void onSubmit(ActionEvent actionEvent) {
        Board board;
        boolean flexible = selectors[0].isFlexible() && selectors[1].isFlexible();
        if (flexible) {
            try {
                board = new Board(Math.min(Integer.parseInt(boardWidthField.getText()), 26), Math.min(Integer.parseInt(boardHeightField.getText()), 26));
            } catch (Exception exception) {
                board = new Board(7, 9);
            }
        } else
            board = new Board(7, 9);
        parent.board.setBoard(board);
        BoardController
                c1 = selectors[0].create(parent.board, board),
                c2 = selectors[1].create(parent.board, board);
        if (c1 != null && c2 != null) {
            parent.setPage(parent.board);
            if (!(c1.isStartHandler() || c2.isStartHandler())) {
                BoardController blue, red;
                boolean b;
                if (matchComboBox.getSelectedIndex() == 2) b = new Random().nextBoolean();
                else b = matchComboBox.getSelectedIndex() == 0;
                blue = b? c1: c2;
                red = b? c2: c1;
                board.start(prePlayedMovesCheckBox.isSelected() || !flexible, blue, red);
            }
            board.announceControllers(c1, c2);
        } else {
            if (c1 != null) c1.close();
            if (c2 != null) c2.close();
            parent.board.setBoard(null);
        }
    }

    @Override
    public void enablePage(JFrame frame) {
        frame.getContentPane().add(mainPanel);
        frame.setTitle("Spaghetti");
        mainPanel.requestFocus();
        mainPanel.repaint();
        mainPanel.revalidate();
    }

    @Override
    public void disablePage(JFrame frame) {
        frame.getContentPane().removeAll();
    }
}
