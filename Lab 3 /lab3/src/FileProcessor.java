/**
 * Elliot Anderson
 * 09/30/2026
 * Graded Lab #3 - GUI File Processor
 *
 * Opens text file, displays its contents, and reports its line count.
 * Input: a file name entered in dialog box.
 * Output: the file contents in the text area, plus the dialog boxes.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileProcessor extends JFrame {
    /**
     * Text area that displays the contents of the opened file.
     */
    private JTextArea contentArea;

    /**
     * Label above the text area showing the name of the open file.
     */
    private JLabel fileLabel;

    /**
     * Name of the file currently displayed; null until a file is opened.
     */
    private String currentFileName = null;

    /**
     * Number of lines in the file currently displayed.
     */
    private int currentLineCount = 0;

    /**
     * Constructor -- builds the user interface:
     * menu bar, file label, text area with scroll bars, and
     * the row of buttons.
     */
    public FileProcessor() {
        super("File Processor");

        // Actions shared by the menu item and the buttons.
        Action openAction = new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        };
        openAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);

        Action summaryAction = new AbstractAction("Summary") {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSummary();
            }
        };
        summaryAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);

        Action exitAction = new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitApplication();
            }
        };
        exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);

        // ===== Menu Bar: File -> Open, Summary, Exit =====
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(new JMenuItem(openAction));
        fileMenu.add(new JMenuItem(summaryAction));
        fileMenu.add(new JMenuItem(exitAction));

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // === Label that shows the current file name ===
        fileLabel = new JLabel("File: ");
        fileLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        // === Center: "Text:" label and the scrollable text area.
        contentArea = new JTextArea(20, 40);
        contentArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        contentArea.setMargin(new Insets(5, 5, 5, 5));
        contentArea.setEditable(false); // display only
        JScrollPane scrollPane = new JScrollPane(contentArea);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 0));
        centerPanel.add(new JLabel("Text:"), BorderLayout.WEST);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // === Bottom: Open, Summary, Exit buttons in one row ===
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        buttonPanel.add(new JButton(openAction));
        buttonPanel.add(new JButton(summaryAction));
        buttonPanel.add(new JButton(exitAction));

        // === Assemble the frame ===
        setLayout(new BorderLayout());
        add(fileLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Closing the window goes through the same exit dialog.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApplication();
            }
        });

        pack();
        setLocationRelativeTo(null); // center on screen
    }

    /**
     * Prompts the user for a file name, reads the file, and displays it.
     * Shows an error dialog if the file cannot be found or read.
     */
    private void openFile() {
        String fileName = JOptionPane.showInputDialog(this,
                "Please enter a file name", "Input", JOptionPane.QUESTION_MESSAGE);

        // Do nothing if the user pressed Cancel or left the field empty.
        if (fileName == null || fileName.trim().isEmpty()) {
            return;
        }

        fileName = fileName.trim();
        File inputFile = new File(fileName);

        if (!inputFile.isFile()) {
            JOptionPane.showMessageDialog(this,
                    fileName + " doesn't exist", "File Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        readFile(inputFile, fileName);
    }

    /**
     * Reads a file line by line, counting the lines, and puts the text
     * into the text area.
     *
     * @param inputFile the file to read
     * @param fileName the name to show in the label and dialogs
     */
    private void readFile(File inputFile, String fileName) {
        StringBuilder text = new StringBuilder(); // accumulates the file contents
        int lines = 0; // counts lines read from the file

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line; // holds current line read from the file

            // read each line, append it to the displayed text, and update the line count.
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
                lines++;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read " + fileName,
                    "Read Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Only update the display state once the whole file was read.
        contentArea.setText(text.toString());
        contentArea.setCaretPosition(0); // scroll back to the top.
        fileLabel.setText("File: " + fileName);

        currentFileName = fileName;
        currentLineCount = lines;
    }

    /**
     * Displays the number of lines in the open file, or a message if no file has
     * been opened yet.
     */
    private void showSummary() {
        if (currentFileName == null) {
            JOptionPane.showMessageDialog(this,
                    "Please open a file first.",
                    "No File Open", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Total Lines = " + currentLineCount,
                "Summary for " + currentFileName,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a termination message, then closes the application.
     */
    private void exitApplication() {
        JOptionPane.showMessageDialog(this, "Closing application...",
                "Exit", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    /**
     * Creates and shows the window on the Swing event thread.
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileProcessor().setVisible(true));
    }
}
