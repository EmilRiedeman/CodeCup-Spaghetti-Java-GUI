package spaghetti.gui.swing;

import spaghetti.game.Board;
import spaghetti.game.BoardController;
import spaghetti.game.BoardListener;
import spaghetti.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(-10, 0, -10, 0));

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
                                        .addComponent(prePlayedMovesLabel)
                                        .addComponent(boardSizeLabel)

                                )
                                .addGap(30)
                                .addGroup(submitLayout.createParallelGroup()
                                        .addComponent(darkModeCheckBox)
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
                        .addGroup(submitLayout.createParallelGroup()
                                .addComponent(darkModeLabel, 20, 20, 20)
                                .addComponent(darkModeCheckBox, 20, 20, 20)
                        )
                        .addGap(20)
                        .addGroup(submitLayout.createParallelGroup()
                                .addComponent(prePlayedMovesLabel, 20, 20, 20)
                                .addComponent(prePlayedMovesCheckBox, 20, 20, 20)
                        )
                        .addGroup(submitLayout.createParallelGroup()
                                .addComponent(boardSizeLabel, 20, 20, 20)
                                .addComponent(boardWidthField, 20, 20, 20)
                                .addComponent(boardHeightField, 20, 20, 20)
                        )
                        .addComponent(submitButton).addGap(10)
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

        for (Component c : submitPanel.getComponents()) {
            if (!(c instanceof JButton))
                c.setFont(c.getFont().deriveFont(11f));
        }
    }

    public void updateSettings() {
        boolean humanOnly = selectors[0].isLocalHuman() && selectors[1].isLocalHuman();
        prePlayedMovesCheckBox.setEnabled(humanOnly);
        boardHeightField.setEnabled(humanOnly);
        boardWidthField.setEnabled(humanOnly);
        if (!humanOnly) {
            prePlayedMovesCheckBox.setSelected(true);
            boardWidthField.setText("7");
            boardHeightField.setText("9");
        }
    }

    private void onSubmit(ActionEvent actionEvent) {
        Board board;
        boolean human = selectors[0].isLocalHuman() && selectors[1].isLocalHuman();
        if (human) {
            try {
                board = new Board(Math.min(Integer.parseInt(boardWidthField.getText()), 50), Math.min(Integer.parseInt(boardHeightField.getText()), 50));
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
                board.start(prePlayedMovesCheckBox.isSelected() || !human, c1, c2); // todo side
            }
            board.announceControllers(c1, c2);
        } else {
            if (c1 != null) c1.close();
            if (c2 != null) c2.close();
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
