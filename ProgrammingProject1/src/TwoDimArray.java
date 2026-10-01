/**
 * Elliot Anderson
 * Created: 09/22/2026
 * Last Updated: 09/30/2026
 * Programming Project #1
 */

import javax.swing.*;
import java.io.*;


/**
 * A two-dimensional array of doubles loaded from a text file, with methods
 * for totals, averages, row and column statistics, standard deviation,
 * searching, and replacing values.
 *
 * The file's first line holds the number of rows and columns; each following
 * line holds one row of values.
 *
 * @author Elliot Anderson
 * @version 1.0
 */
public class TwoDimArray {

    /**
     * The backing two-dimensional array.
     * null until loadArray succeeds.
     */
    private double[][] data;

    /**
     * Name of the file the array is loaded from.
     */
    private final String fileName;

    /**
     * The value passed to the most recent call to findValue.
     */
    private double searchValue;

    /**
     * True once findValue has been called,
     * so replaceAll has a value to match.
     */
    private boolean hasSearchValue = false;

    /**
     * Row index of the match found by findValue,
     * or -1 if there is none.
     */
    private int foundRow = -1;

    /**
     * Column index of the match found by findValue,
     * or -1 if there is none.
     */
    private int foundColumn = -1;

    /**
     * Creates an array object for the given data file.
     * The file is not read until {@link #loadArray()} is called.
     * @param fileName the name of the file holding the array data.
     */
    public TwoDimArray(String fileName) {
        this.fileName = fileName;
    }

    // ========== LOADING ===========

    /**
     * Reads the array from the data file. The first non-blank line must hold
     * the row and column counts as positive whole numbers. It must be followed by exactly that
     * many rows, each with exactly that many numbers. On any issue, an error dialog is shown and the array
     * is left unloaded.
     *
     * @return true if the array was loaded successfully, false if an error occurred.
     */
    public boolean loadArray() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            // First Record: row and column counts
            String header = nextDataLine(reader);
            if (header == null) {
                showError(fileName + " is empty.");
                return false;
            }

            String[] sizes = header.trim().split("\\s+");
            if (sizes.length != 2) {
                showError("The first line must hold exactly two numbers:\n"
                + "the number of rows and the number of columns.");
                return false;
            }

            int rows; // number of rows declared in the file
            int columns; // number of columns declared in the file

            try {
                rows = Integer.parseInt(sizes[0]);
                columns = Integer.parseInt(sizes[1]);
            } catch (NumberFormatException e) {
                showError("The row and column counts must be whole numbers.\n"
                        + "First line read: \"" + header.trim() + "\"");
                return false;
            }

            if (rows <= 0 || columns <= 0) {
                showError("The row and column counts must be greater than zero.\n"
                + "First line read: \"" + header.trim() + "\"");
                return false;
            }

            // === Remaining records: one line of values per row ===
            double[][] values = new double[rows][columns];
            for (int row = 0; row < rows; row++) {
                String line = nextDataLine(reader);
                if (line == null) {
                    showError("Missing data: expected " + rows + " rows of values "
                    + "but found only " + row + ".");
                    return false;
                }

                String[] tokens = line.trim().split("\\s+");
                if (tokens.length != columns) {
                    showError("Row " + row + " has " + tokens.length + " value(s); "
                    + "expected " + columns + ".\nLine read: \"" + line.trim() + "\"");
                    return false;
                }

                // Convert each token to a number, rejecting text and NaN/Infinity.
                for (int col = 0; col < columns; col++) {
                    try {
                        values[row][col] = Double.parseDouble(tokens[col]);
                    } catch (NumberFormatException e) {
                        showError("Invalid value \"" + tokens[col] + "\" in row " + row + ", column " + col + ".");
                        return false;
                    }

                    if (!Double.isFinite(values[row][col])) {
                        showError("Invalid value \"" + tokens[col] + "\" in row " + row + ", column " + col + ".");
                        return false;
                    }
                }
            }

            // Extra data means the header does not match the file.
            if (nextDataLine(reader) != null) {
                showError("The file has more rows than the " + rows + " declared on the first line.");
                return false;
            }

            data = values;
            clearSearch();
            return true;
        } catch (FileNotFoundException e) {
            showError(fileName + " doesn't exist.");
            return false;
        } catch (IOException e) {
            showError("Error reading " + fileName + ":\n" + e.getMessage());
            return false;
        }
    }

    // ================= Accessors =================

    /**
     * Returns the name of the data file.
     *
     * @return the file name given to the constructor.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the number of rows in the array.
     *
     * @return the row count
     * @throws IllegalStateException if the array has not been loaded.
     */
    public int getNumRows() {
        checkLoaded();
        return data.length;
    }

    /**
     * Returns the number of columns in the array.
     *
     * @return the column count
     * @throws IllegalStateException if the array has not been loaded
     */
    public int getNumColumns() {
        checkLoaded();
        return data[0].length;
    }

    // =============== Array Statistics =================

    /**
     * Returns the total of all values in the array.
     *
     * @return the sum of every element.
     * @throws IllegalStateException if the array has not been loaded.
     */
    public double getTotal() {
        checkLoaded();
        double total = 0.0; // running sum of all elements.
        for (double[] row : data) {
            for (double value : row) {
                total += value;
            }
        }
        return total;
    }

    /**
     * Returns the average of all values in the array.
     *
     * @return the total divided by number of elements.
     * @throws IllegalStateException if the array has not been loaded.
     */
    public double getAverage() {
        return getTotal() / (getNumRows() * getNumColumns());
    }

    /**
     * Returns the largest value in the array.
     *
     * @return the maximum element in the array.
     * @throws IllegalStateException if the array has not been loaded.
     */
    public double getArrayMaxValue() {
        checkLoaded();
        double max = data[0][0]; // largest seen so far
        for (double[] row : data) {
            for (double value : row) {
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }

    /**
     * Returns the smallest value in the array.
     *
     * @return the minimum element.
     * @throws IllegalStateException if the array has not been loaded.
     */
    public double getArrayMinValue() {
        checkLoaded();
        double min = data[0][0]; // smallest value seen so far
        for (double[] row : data) {
            for (double value : row) {
                if (value < min) {
                    min = value;
                }
            }
        }
        return min;
    }

    /**
     * Returns the standard deviation of all values in the array:
     * the square root of the average squared distance from the mean.
     *
     * @return the standard deviation of the elements.
     * @throws  IllegalStateException if the array has not been loaded.
     */
    public double calcStdDev() {
        double mean = getAverage(); // average of all elements.
        double sumOfSquares = 0.0; // running sum of squared deviation
        for (double[] row : data) {
            for (double value : row) {
                double deviation = value - mean;
                sumOfSquares += deviation * deviation;
            }
        }

        return Math.sqrt(sumOfSquares / (getNumRows() * getNumColumns()));
    }

    // ============ Row and Column Statistics ============

    /**
     * Returns the total of the values in one row.
     *
     * @param row the row index (0-indexed).
     * @return the sum of the row's elements.
     * @throws IllegalArgumentException if the row index is out of range.
     * @throws IllegalStateException if the array has not been loaded.
     */
    public double getRowTotal(int row) {
        checkRow(row);
        double total = 0.0; // running sum for the row
        for (double value : data[row]) {
            total += value;
        }

        return total;
    }

    /**
     * Returns the total of the values in one column.
     *
     * @param column the column index (0-indexed).
     * @return the sum of the column's elements.
     * @throws IllegalArgumentException if the column index is out of range.
     * @throws IllegalStateException if the array has not been loaded.
     */
    public double getColumnTotal(int column) {
        checkColumn(column);
        double total = 0.0; // running sum of the column
        for (double[] row : data) {
            total += row[column];
        }
        return total;
    }

    /**
     * Returns the highest value in one row.
     *
     * @param row the row index (0-indexed).
     * @return the largest element in the row
     * @throws IllegalArgumentException if the row index is out of range.
     * @throws IllegalStateException if the array has not been loaded.
     */
    public double getHighestInRow(int row) {
        checkRow(row);
        double highest = data[row][0]; // largest value seen so far
        for (double value : data[row]) {
            if (value > highest) {
                highest = value;
            }
        }

        return highest;
    }

    /**
     * Returns the lowest value in one row.
     *
     * @param row the row index (0-indexed)
     * @return the smallest element in the row
     * @throws IllegalArgumentException if the row index is out of range.
     * @throws IllegalStateException if the array has not been loaded
     */
    public double getLowestInRow(int row) {
        checkRow(row);
        double lowest = data[row][0]; // smallest value seen so far
        for (double value : data[row]) {
            if (value < lowest) {
                lowest = value;
            }
        }
        return lowest;
    }

    // ========== Search and Replace =============

    /**
     * Searches the array, row by row, for a value. The value is remembered for
     * {@link #replaceAll(double)}, and the position of the first match is remembered
     * for {@link #replace(double)}.
     *
     * @param value the value to search for (exact match is required)
     * @return true if the value was found, false otherwise
     * @throws IllegalStateException if the array has not been loaded.
     */
    public boolean findValue(double value) {
        checkLoaded();
        searchValue = value;
        hasSearchValue = true;
        foundRow = -1;
        foundColumn = -1;

        for (int row = 0; row < data.length; row++) {
            for (int col = 0; col < data[row].length; col++) {
                if (data[row][col] == value) {
                    foundRow = row;
                    foundColumn = col;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the row of the match found by the last call to findValue.
     *
     * @return the row index, or  -1 if there is no current match.
     */
    public int getFoundRow() {
        return foundRow;
    }

    /**
     * Returns the column of the match found by the last call to findValue.
     *
     * @return the column index, or -1 if there is no current match.
     */
    public int getFoundColumn() {
        return foundColumn;
    }

    /**
     * Replaces the match found by the last call to {@link #findValue(double)}
     * with a new value. After the replacement there is no current match until
     * findValue is called again.
     *
     * @param newValue the value to store in place of the match
     * @return true if a value was replaced, false if there was no match
     * @throws IllegalStateException if the array has not been loaded.
     */
    public boolean replace(double newValue) {
        checkLoaded();
        if (foundRow < 0) {
            return false;
        }

        data[foundRow][foundColumn] = newValue;
        foundRow = -1;
        foundColumn = -1;
        return true;
    }

    /**
     * Replaces every element equal to the value from the last call to
     * {@link #findValue(double)} with a new value.
     *
     * @param newValue the value to store in place of each match.
     * @return the number of elements replaced (0 if findValue has not been called).
     * @throws  IllegalStateException if the array has not been loaded.
     */
    public int replaceAll(double newValue) {
        checkLoaded();
        if (!hasSearchValue) {
            return 0;
        }

        int count = 0; // number of elements replaced
        for (double[] row : data) {
            for (int col = 0; col < row.length; col++) {
                if (row[col] == searchValue) {
                    row[col] = newValue;
                    count++;
                }
            }
        }

        foundRow = -1;
        foundColumn = -1;
        return count;
    }

    // ======== Display ==========

    /**
     * Returns the array as a grid of values, one row per line, with row and
     * column indices as labels.
     *
     * @return a formatted table of the array, or a error message if it is not loaded.
     */
    @Override
    public String toString() {
        if (data == null) {
            return "(array not loaded)";
        }

        StringBuilder table = new StringBuilder(); // formatted output builder

        table.append(String.format("%8s", ""));
        for (int col = 0; col < data[0].length; col++) {
            table.append(String.format("%14s", "Col " + col));
        }
        table.append("\n");

        // one line per row
        for (int row = 0; row < data.length; row++) {
            table.append(String.format("%-8s", "Row " + row));
            for (double value : data[row]) {
                table.append(String.format("%14.4f", value));
            }
            table.append("\n");
        }
        return table.toString();
    }

    // ========== Private Helpers ==========

    /**
     * Returns the next non-blank line from the reader.
     *
     * @param reader the Reader to read from
     * @return the next line with content, or null at the end of the file.
     * @throws IOException if the file cannot be read
     */
    private static String nextDataLine(BufferedReader reader) throws IOException {
        String line; // current line read from the file
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                return line;
            }
        }
        return null;
    }

    /**
     * Clears previous search state.
     */
    private void clearSearch() {
        hasSearchValue = false;
        foundRow = -1;
        foundColumn = -1;
    }

    /**
     * Verifies that the array has been loaded.
     *
     * @throws IllegalStateException if loadArray has not succeeded.
     */
    private void checkLoaded() {
        if (data == null) {
            throw new IllegalStateException("The array has not been loaded.");
        }
    }

    /**
     * Makes sure a row index is valid and in-range.
     *
     * @param row the row index to check.
     * @throws IllegalArgumentException if the index is out of range.
     */
    private void checkRow(int row) {
        checkLoaded();
        if (row < 0 || row >= data.length) {
            throw new IllegalArgumentException("Row " + row + " is out of range (0-" + (data.length - 1) + ").");
        }
    }

    /**
     * Makes sure a column index is valid and in-range.
     *
     * @param column the column index to check.
     * @throws IllegalArgumentException if the index is out of range.
     */
    private void checkColumn(int column) {
        checkLoaded();
        if (column < 0 || column >= data[0].length) {
            throw new IllegalArgumentException("Column " + column + " is out of range (0-" + (data[0].length - 1) + ").");
        }
    }

    /**
     * Shows an error message in a dialog box.
     *
     * @param message the text to display.
     */
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Asks the user for a number in a dialog box, repeating the prompt until
     * a valid number is entered or the user cancels.
     *
     * @param prompt the message shown in the dialog
     * @param title the dialog's title
     * @return the number entered, or null if the user pressed Cancel.
     */
    private static Double promptForDouble(String prompt, String title) {
        while (true) {
            String input = JOptionPane.showInputDialog(null, prompt, title,
                    JOptionPane.QUESTION_MESSAGE);
            if (input == null) {
                return null; // User pressed Cancel
            }
            try {
                double value = Double.parseDouble(input.trim());
                if (Double.isFinite(value)) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message below
            }

            showError("\"" + input.trim() + "\" is not a valid number. Please try again.");
        }
    }

    /**
     * Prints the row, column, and whole-array statistics to standard output.
     *
     * @param array the loaded array to print the report on.
     */
    private static void printStatistics(TwoDimArray array) {
        System.out.println("Row statistics:");
        for (int row = 0; row < array.getNumRows(); row++) {
            System.out.printf("  Row %d:   total = %12.4f   highest = %12.4f   lowest = %12.4f%n",
                    row, array.getRowTotal(row), array.getHighestInRow(row), array.getLowestInRow(row));
        }

        System.out.println();
        System.out.println("Column Statistics:");
        for (int col = 0; col < array.getNumColumns(); col++) {
            System.out.printf("  Column %d:   total = %12.4f%n", col, array.getColumnTotal(col));
        }

        System.out.println();
        System.out.println("Whole-Array Statistics:");
        System.out.printf("  Total              = %12.4f%n", array.getTotal());
        System.out.printf("  Average            = %12.4f%n", array.getAverage());
        System.out.printf("  Maximum value      = %12.4f%n", array.getArrayMaxValue());
        System.out.printf("  Minimum value      = %12.4f%n", array.getArrayMinValue());
        System.out.printf("  Standard deviation = %12.4f%n", array.calcStdDev());
    }

    /**
     * Lets the user search for values and replace them, repeating until the user cancels.
     * Results are printed to standard output.
     *
     * @param array the loaded array to search.
     */
    private static void runSearchAndReplace(TwoDimArray array) {
        String[] choices = {"Replace first match", "Replace all matches", "Don't replace"};

        while (true) {
            Double target = promptForDouble("Enter a value to search for\n(Cancel to finish):",
                    "Find Value");
            if (target == null) {
                return;
            }

            System.out.println();
            if (!array.findValue(target)) {
                System.out.println("findValue(" + target + "): not found.");
                continue;
            }

            System.out.println("findValue(" + target + "): found at row "
                    + array.getFoundRow() + ", column "
                    + array.getFoundColumn() + ".");

            int choice = JOptionPane.showOptionDialog(null,
                    target + " was found at row " + array.getFoundRow()
            + ", column " + array.getFoundColumn() + ".\nReplace it?",
                    "Replace Value", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, choices, choices[0]);
            if (choice != 0 && choice != 1) {
                continue; // "Don't replace" or dialog closed.
            }

            Double newValue = promptForDouble("Enter the replacement value:", "Replacement");
            if (newValue == null) {
                continue;
            }

            if (choice == 0) {
                array.replace(newValue);
                System.out.println("replace(" + newValue + "): replaced the first match.");
            } else {
                int count = array.replaceAll(newValue);
                System.out.println("replaceAll(" + newValue + "): replaced " + count + " value(s).");
            }

            System.out.println();
            System.out.println("Updated array:");
            System.out.print(array);
            System.out.printf("New Total = %.4f, average = %.4f, std dev = %.4f%n",
                    array.getTotal(), array.getAverage(), array.calcStdDev());
        }
    }

    // ======== DRIVER =============

    /**
     * Driver: asks for a data file, loads it, prints the array and its statistics, then lets the user
     * search and replace values. Quits with an error dialog if the file is missing, empty, or invalid.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        String fileName = JOptionPane.showInputDialog(null, "Enter the name of the input file:",
                "Input File", JOptionPane.QUESTION_MESSAGE);
        if (fileName == null) {
            System.exit(0); // user pressed cancel
        }

        fileName = fileName.trim();
        File inputFile = new File(fileName); // used to check the file before reading it
        if (fileName.isEmpty()) {
            showError("No file name was entered.");
            System.exit(1);
        }
        if (!inputFile.isFile()) {
            showError(fileName + " doesn't exist.");
            System.exit(1);
        }
        if (inputFile.length() == 0) {
            showError(fileName + " is empty.");
            System.exit(1);
        }

        TwoDimArray array = new TwoDimArray(fileName);
        if (!array.loadArray()) {
            System.exit(1);  // loadArray already showed the error
        }

        System.out.println("File: " + array.getFileName());
        System.out.println("Size: " + array.getNumRows() + " rows x "
                + array.getNumColumns() + " columns");
        System.out.println();
        System.out.print(array);
        System.out.println();
        printStatistics(array);

        runSearchAndReplace(array);

        System.out.println();
        System.out.println("Done.");
        System.exit(0);  // closes the Swing thread started by the dialogs
    }
}
