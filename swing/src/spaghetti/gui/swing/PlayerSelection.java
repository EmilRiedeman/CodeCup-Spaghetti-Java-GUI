package spaghetti.gui.swing;

import spaghetti.game.Board;
import spaghetti.game.BoardController;
import spaghetti.networking.client.ServerConnection;
import spaghetti.utils.*;
import spaghetti.BotProgram;
import spaghetti.game.BoardListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.file.Paths;

public class PlayerSelection extends JPanel implements ItemListener {
    public static final Font font1 = new Font(Font.MONOSPACED, Font.BOLD, 20);
    //public static final Font font2 = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    public final PlayerSelectionPage parent;
    public final GroupLayout[] layouts = new GroupLayout[3]; // Player, Server, Bot
    public final JComboBox<String> typeSelection = new JComboBox<String>(new String[]{"Player", "Server", "Bot"}){{
        addItemListener(PlayerSelection.this);
    }};

    public final JLabel title = new JLabel() {{
        setFont(font1);
    }};

    public final JLabel nameLabel = new JLabel("Name:");
    public final JTextField nameField = new JTextField("Player");

    // Server:
    public final JLabel serverAddressLabel = new JLabel("Server Address:");
    public final JTextField serverAddressField = new JTextField("localhost");
    public final JLabel serverPortLabel = new JLabel("Server Port:");
    public final JTextField serverPortField = new JTextField("12345");

    // Bot:
    //new JRadioButtonMenuItem
    public final JRadioButton javaRadio = new JRadioButton();
    public final JRadioButton execRadio = new JRadioButton();
    public final JRadioButton otherRadio = new JRadioButton();
    public final JLabel javaLabel = new JLabel("Java");
    public final JLabel execLabel = new JLabel("Executable");
    public final JLabel otherLabel = new JLabel("Other");
    public final ButtonGroup runCommandGroup = new ButtonGroup() {{
        add(execRadio);
        add(javaRadio);
        add(otherRadio);
    }};

    public final JButton execSearchFileButton = new JButton("Search File");
    public final JLabel execFileLabel = new JLabel("-");
    public final JFileChooser execFileChooser = new JFileChooser(Paths.get(".").toFile());
    public File fileExec = null;

    public final JButton javaSearchFileButton = new JButton("Search File") {{
        setEnabled(false);
    }};
    public final JLabel javaFileLabel = new JLabel("-");
    public final JFileChooser javaFileChooser = new JFileChooser(Paths.get(".").toFile());
    public File fileJava = null;

    public final JTextField otherField = new JTextField();

    public PlayerSelection(String text, PlayerSelectionPage p) {
        this.parent = p;
        this.title.setText("Select " + text);
        this.nameField.setText(text);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        GroupLayout.Group[] horizontalGroups = new GroupLayout.Group[3];
        GroupLayout.Group[] verticalGroups = new GroupLayout.Group[3];
        for (int i = 0; i < 3; ++i) {
            layouts[i] = new GroupLayout(this);
            layouts[i].setAutoCreateGaps(true);
            layouts[i].setAutoCreateContainerGaps(true);
            horizontalGroups[i] = (layouts[i].createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(title)
                    .addComponent(typeSelection)
                    .addGroup(layouts[i].createSequentialGroup()
                        .addComponent(nameLabel)
                        .addComponent(nameField)
                    )
            );
            verticalGroups[i] = (layouts[i].createSequentialGroup()
                    .addComponent(title)
                    .addComponent(typeSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layouts[i].createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    ).addGap(5, 25, 25)
            );
        }

        // Server:
        ((GroupLayout.ParallelGroup)horizontalGroups[1])
                .addGroup(layouts[1].createParallelGroup()
                        .addGroup(layouts[1].createSequentialGroup()
                                .addComponent(serverAddressLabel)
                                .addComponent(serverAddressField))
                        .addGroup(layouts[1].createSequentialGroup()
                                .addComponent(serverPortLabel)
                                .addComponent(serverPortField)));

        ((GroupLayout.SequentialGroup)verticalGroups[1])
                .addGroup(layouts[1].createSequentialGroup()
                        .addGroup(layouts[1].createParallelGroup()
                                .addComponent(serverAddressLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(serverAddressField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layouts[1].createParallelGroup()
                                .addComponent(serverPortLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(serverPortField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

        // Bot:
        ActionListener radioListener = e -> {
            execSearchFileButton.setEnabled(e.getSource() == execRadio);
            javaSearchFileButton.setEnabled(e.getSource() == javaRadio);
        };
        execRadio.addActionListener(radioListener);
        javaRadio.addActionListener(radioListener);
        otherRadio.addActionListener(radioListener);
        execRadio.setSelected(true);

        execSearchFileButton.addActionListener(e -> execFileChooser.showOpenDialog(null));
        execFileChooser.addActionListener(e -> {
            fileExec = execFileChooser.getSelectedFile();
            if (fileExec != null) execFileLabel.setText(fileExec.getName());
            else execFileLabel.setText("-");
        });
        javaSearchFileButton.addActionListener(e -> javaFileChooser.showOpenDialog(null));
        javaFileChooser.addActionListener(e -> {
            fileJava = javaFileChooser.getSelectedFile();
            String extension = Utils.getFileExtension(fileJava);

            if (!(extension.equals("class") || extension.equals("jar"))) fileJava = null;
            if (fileJava != null) javaFileLabel.setText(fileJava.getName());
            else javaFileLabel.setText("-");
        });
        ((GroupLayout.ParallelGroup)horizontalGroups[2])
                .addGroup(layouts[2].createSequentialGroup()
                        .addGroup(layouts[2].createParallelGroup()
                                .addComponent(execLabel)
                                .addComponent(javaLabel)
                                .addComponent(otherLabel)
                        )
                        .addGroup(layouts[2].createParallelGroup()
                                .addComponent(execRadio)
                                .addComponent(javaRadio)
                                .addComponent(otherRadio)
                        )
                        .addGroup(layouts[2].createParallelGroup()
                                .addGroup(layouts[2].createSequentialGroup()
                                        .addComponent(execSearchFileButton)
                                        .addComponent(execFileLabel, 20, 20, Short.MAX_VALUE)
                                ).addGroup(layouts[2].createSequentialGroup()
                                        .addComponent(javaSearchFileButton)
                                        .addComponent(javaFileLabel, 20, 20, Short.MAX_VALUE)
                                ).addGroup(layouts[2].createSequentialGroup()
                                        .addComponent(otherField, 20, 20, Short.MAX_VALUE)
                                )
                        )
                );

        ((GroupLayout.SequentialGroup)verticalGroups[2])
                .addGroup(layouts[2].createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(execLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(execRadio)
                        .addComponent(execSearchFileButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(execFileLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                )
                .addGroup(layouts[2].createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(javaLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(javaRadio)
                        .addComponent(javaSearchFileButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(javaFileLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                )
                .addGroup(layouts[2].createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(otherLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(otherRadio)
                        .addComponent(otherField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

        for (int i = 0; i < 3; ++i) {
            layouts[i].setHorizontalGroup(horizontalGroups[i]);
            layouts[i].setVerticalGroup(verticalGroups[i]);
        }

        setLayout(layouts[0]);
    }

    public BoardController create(GraphicalBoard gb, Board board) {
        BoardController r = null;
        String name = nameField.getText();
        switch (typeSelection.getSelectedIndex()) {
            case 0:
                r = new MouseInputBoardController(name, gb);
                break;
            case 1:
                try {
                    r = new ServerConnection(serverAddressField.getText(), Integer.parseInt(serverPortField.getText()),
                            board);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                if (execRadio.isSelected()) {
                    if (fileExec != null && fileExec.exists())
                        r = new BotProgram(name, fileExec.getAbsolutePath(), board);
                } else if (javaRadio.isSelected())
                    if (fileJava != null && fileJava.exists()) {
                        if (Utils.getFileExtension(fileJava).equals("class"))
                            r = new BotProgram(name, "" + '"' + System.getProperty("java.home")
                                    + "\\bin\\java.exe\" -classpath \"" + fileJava.getParent() +
                                    "\" " + fileJava.getName().replaceFirst("[.][^.]+$", ""), board);
                        else if (Utils.getFileExtension(fileJava).equals("jar"))
                            r = new BotProgram(name, "" + '"' + System.getProperty("java.home")
                                    + "\\bin\\java.exe\" -jar \"" + fileJava.getAbsolutePath() + "\"", board);
                    }
                break;
        }
        return r;
    }

    public boolean isFlexible() {
        return typeSelection.getSelectedIndex() == 0;
    }

    public boolean isStartHandler() {
        return typeSelection.getSelectedIndex() == 1;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            parent.updateSettings();
            for (Component c : getComponents())
                c.setVisible(false);
            setLayout(layouts[typeSelection.getSelectedIndex()]);
            typeSelection.setVisible(true);
            nameField.setVisible(true);
            nameField.setEnabled(true);
            nameLabel.setVisible(true);
            title.setVisible(true);
            switch (typeSelection.getSelectedIndex()) {
                case 1:
                    serverAddressLabel.setVisible(true);
                    serverAddressField.setVisible(true);

                    serverPortLabel.setVisible(true);
                    serverPortField.setVisible(true);
                    nameField.setEnabled(false);
                    break;
                case 2:
                    execFileLabel.setVisible(true);
                    execSearchFileButton.setVisible(true);
                    execRadio.setVisible(true);
                    execLabel.setVisible(true);

                    javaFileLabel.setVisible(true);
                    javaSearchFileButton.setVisible(true);
                    javaRadio.setVisible(true);
                    javaLabel.setVisible(true);

                    otherRadio.setVisible(true);
                    otherLabel.setVisible(true);
                    otherField.setVisible(true);
                    break;
            }
            revalidate();
        }
    }
}
