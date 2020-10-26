package cli;

import java.util.ArrayList;

/**
 * Port of Python Library texttable
 * Done everything in the python library texttable apart from separate datatype formatting.
 * I ask for everything as a string and just leave it as it is.
 * 
 * TODO:
 * - Improve textWrap from the python library textwrap wrap method which uses
 * regular expressions. Then have a complete library
 * - Add a feature where you can specify that all columns should have certain
 * alignments so it doesn't matter how many there are. This wouldn't have to be reset
 */
public class TextTable {

    // decor characters and whether to show them
    private char charDecoHorizontal = '-';
    private char charDecoVertical = '|';
    private char charDecoCorner = '+';
    private char charDecoHeader = '=';
    private boolean showDecoHorizontal = true;
    private boolean showDecoVertical = true;
    private boolean showDecoHeader = true;
    private boolean showDecoBorder = true;

    // widths
    private Integer maxWidth = null;
    private int[] widths;

    // alignment
    private char[] headerHorizontalAlign;
    private char[] columnHorizontalAlign;
    private char[] columnVerticalAlign;

    // data
    private Integer numFields;
    private ArrayList<String> header;
    private ArrayList<ArrayList<String>> rows;

    public TextTable() {
        this.reset();
    }

    public void reset() {
        this.rows = new ArrayList<>();
        this.header = null;
        this.numFields = null;
        this.headerHorizontalAlign = null;
        this.columnHorizontalAlign = null;
        this.columnVerticalAlign = null;
        this.widths = null;
    }

    public void setMaxWidth(int maxWidth) {
        if (maxWidth <= 0) {
            this.maxWidth = null;
        } else {
            this.maxWidth = maxWidth;
        }
    }

    public void setChars(char charDecoHorizontal, char charDecoVertical, char charDecoCorner, char charDecoHeader) {
        this.charDecoHorizontal = charDecoHorizontal;
        this.charDecoVertical = charDecoVertical;
        this.charDecoCorner = charDecoCorner;
        this.charDecoHeader = charDecoHeader;
    }

    public void setDeco(boolean showDecoHorizontal, boolean showDecoVertical, boolean showDecoBorder, boolean showDecoHeader) {
        this.showDecoHorizontal = showDecoHorizontal;
        this.showDecoVertical = showDecoVertical;
        this.showDecoBorder = showDecoBorder;
        this.showDecoHeader = showDecoHeader;
    }

    public void setHeaderHorizontalAlign(char[] alignment) {
        this.checkNumFields(alignment);
        for (char align: alignment) {
            if (align != 'l' && align != 'c' && align != 'r') {
                throw new IllegalArgumentException("All alignments must be either 'l', 'c' or 'r'");
            }
        }
        this.headerHorizontalAlign = alignment;
    }

    public void setColumnHorizontalAlign(char[] alignment) {
        this.checkNumFields(alignment);
        for (char align: alignment) {
            if (align != 'l' && align != 'c' && align != 'r') {
                throw new IllegalArgumentException("All alignments must be either 'l', 'c' or 'r'");
            }
        }
        this.columnHorizontalAlign = alignment;
    }

    public void setColumnVerticalAlign(char[] alignment) {
        this.checkNumFields(alignment);
        for (char align: alignment) {
            if (align != 't' && align != 'm' && align != 'b') {
                throw new IllegalArgumentException("All alignments must be either 't', 'm' or 'b'");
            }
        }
        this.columnVerticalAlign = alignment;
    }

    public void setColumnWidth(int[] widths) {
        this.checkNumFields(widths);
        for (int width: widths) {
            if (width <= 0) {
                throw new IllegalArgumentException("All widths must be greater than 0");
            }
        }
        this.widths = widths;
    }

    /**
     * Add a header row
     * @param header
     */
    public void addHeader(ArrayList<String> header) {
        this.checkNumFields(header);
        this.header = header;
    }

    /**
     * Add a non header row
     * @param row
     */
    public void addRow(String[] row) {

        ArrayList<String> r = new ArrayList<>();
        for (String field: row) {
            r.add(field);
        }

        this.checkNumFields(r);
        this.rows.add(r);
    }

    /**
     * Add rows - includesHeader tells us whether the first row is a header
     * @param rows
     * @param includesHeader
     */
    public void addRows(String[][] rows, boolean includesHeader) {
        if (includesHeader) {
            this.header = new ArrayList<String>();
            for (String heading: rows[0]) {
                this.header.add(heading);
            }
        }
        for (int i = 1; i < rows.length; i++) {
            this.addRow(rows[i]);
        }
    }

    public void print() {
        System.out.println(this.draw());
    }

    public String draw() {
        if (this.header == null && this.rows.isEmpty()) {
            return "";
        }
        this.checkWidths();
        this.checkAlignment();

        String out = "";

        String horizontalLineNotHeader = this.generateHorizontalLine(false);

        // top boarder
        if (this.showDecoBorder) {
            out += horizontalLineNotHeader;
        }

        // header row
        if (this.header != null) {
            out += this.drawLine(this.header, true);
            if (this.showDecoHeader) {
                out += this.generateHorizontalLine(true);
            }
        }

        // normal rows
        int length = 0;
        for (ArrayList<String> row: this.rows) {
            length++;
            out += this.drawLine(row, false);
            if (this.showDecoHorizontal && length < this.rows.size()) {
                out += horizontalLineNotHeader;
            }
        }

        // bottom boarder
        if (this.showDecoBorder) {
            out += horizontalLineNotHeader;
        }

        // remove trailing newline
        return out.substring(0, out.length() - 1);
    }

    private void checkNumFields(char[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Must have at least 1 field");
        } else if (this.numFields == null) {
            this.numFields = array.length;
        } else if (this.numFields != array.length) {
            throw new IllegalArgumentException("Number of fields should be consistent, previously had " + this.numFields + " but given " + array.length);
        }
    }

    private void checkNumFields(int[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Must have at least 1 field");
        } else if (this.numFields == null) {
            this.numFields = array.length;
        } else if (this.numFields != array.length) {
            throw new IllegalArgumentException("Number of fields should be consistent, previously had " + this.numFields + " but given " + array.length);
        }
    }

    private void checkNumFields(ArrayList<String> array) {
        if (array.isEmpty()) {
            throw new IllegalArgumentException("Must have at least 1 field");
        } else if (this.numFields == null) {
            this.numFields = array.size();
        } else if (this.numFields != array.size()) {
            throw new IllegalArgumentException("Number of fields should be consistent, previously had " + this.numFields + " but given " + array.size());
        }
    }

    private String generateHorizontalLine(boolean isHeader) {
        char chr = isHeader? this.charDecoHeader: this.charDecoHorizontal;

        String separator = "" + chr + (this.showDecoVertical? this.charDecoCorner: chr) + chr;
        
        String out = "";
        for (int width: this.widths) {
            for (int i = 0; i < width; i++) {
                out += chr;
            }
            out += separator;
        }

        out = out.substring(0, out.length() - 3);

        if (this.showDecoBorder) {
            out = "" + this.charDecoCorner + chr + out + chr + this.charDecoCorner;
        }

        return out + "\n";
    }

    private int lenCell(String cell) {
        String[] cellLines = cell.split("\n", -1);
        int maxi = 0;
        for (String line: cellLines) {
            int length = 0;
            String[] parts = line.split("\t", -1);
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                length += part.length();
                if (i + 1 < parts.length) {
                    length = (length / 8 + 1) * 8;
                }
            }
            maxi = Math.max(maxi, length);
        }
        return maxi;
    }

    private void checkWidths() {
        if (this.widths == null) {

            int[] maxi = new int[this.numFields];
            if (this.header != null) {
                for (int i = 0; i < this.numFields; i++) {
                    maxi[i] = this.lenCell(this.header.get(i));
                }
            }
            for (ArrayList<String> row: this.rows) {
                for (int i = 0; i < row.size(); i++) {
                    maxi[i] = Math.max(maxi[i], this.lenCell(row.get(i)));
                }
            }

            int contentWidth = 0;
            for (int width: maxi) {
                contentWidth += width;
            }
            int decoWidth = 3 * (this.numFields - 1) + (this.showDecoBorder? 4: 0);
            if (this.maxWidth != null && (contentWidth + decoWidth) > this.maxWidth) {
                // content too wide to fit in the expected maxWidth so recompute maximum cell width for each cell
                if (this.maxWidth < (this.numFields + decoWidth)) {
                    throw new IllegalArgumentException("maxWidth too small to fit everything in");
                }
                int availableWidth = this.maxWidth - decoWidth;
                int[] newmaxi = new int[this.numFields];
                int i = 0;
                while (availableWidth > 0) {
                    if (newmaxi[i] < maxi[i]) {
                        newmaxi[i]++;
                        availableWidth--;
                    }
                    i = (i + 1) % this.numFields;
                }
                maxi = newmaxi;
            }
            this.widths = maxi;
        }
    }

    private void checkAlignment() {
        char[] horizontalAlign = new char[this.numFields];
        char[] verticalAlign = new char[this.numFields];
        for (int i = 0; i < this.numFields; i++) {
            horizontalAlign[i] = 'l';
            verticalAlign[i] = 't';
        }

        if (this.headerHorizontalAlign == null) {
            this.headerHorizontalAlign = horizontalAlign;
        }
        if (this.columnHorizontalAlign == null) {
            this.columnHorizontalAlign = horizontalAlign;
        }
        if (this.columnVerticalAlign == null) {
            this.columnVerticalAlign = verticalAlign;
        }
    }

    private String drawLine(ArrayList<String> line, boolean isHeader) {
        ArrayList<ArrayList<String>> splitLine = this.splitIt(line, isHeader);
        String space = " ";
        String out = "";

        for (int i = 0; i < splitLine.get(0).size(); i++) {
            if (this.showDecoBorder) {
                out += this.charDecoVertical + " ";
            }
            int length = 0;
            for (int j = 0; j < splitLine.size(); j++) {
                ArrayList<String> cell = splitLine.get(j);
                int width = this.widths[j];
                char align = this.columnHorizontalAlign[j];
                length++;
                String cellLine = cell.get(i);
                int fill = width - cellLine.length();
                if (isHeader) {
                    align = this.headerHorizontalAlign[length - 1];
                }
                switch (align) {
                    case 'r':
                        for (int k = 0; k < fill; k++) {
                            out += space;
                        }
                        out += cellLine;
                        break;
                    case 'c':
                        for (int k = 0; k < fill/2; k++) {
                            out += space;
                        }
                        out += cellLine;
                        for (int k = 0; k < fill/2 + fill % 2; k++) {
                            out += space;
                        }
                        break;
                    case 'l':
                        out += cellLine;
                        for (int k = 0; k < fill; k++) {
                            out += space;
                        }
                        break;
                }
                if (length < splitLine.size()) {
                    out += space + (this.showDecoVertical? this.charDecoVertical: space) + space;
                }
            }
            out += (this.showDecoBorder? space+this.charDecoVertical: "") + "\n";
        }

        return out;
    }

    private ArrayList<ArrayList<String>> splitIt(ArrayList<String> line, boolean isHeader) {

        ArrayList<ArrayList<String>> lineWrapped = new ArrayList<>();

        for (int i = 0; i < line.size(); i++) {
            String cell = line.get(i);
            int width = this.widths[i];
            ArrayList<String> array = new ArrayList<>();
            for (String c: cell.split("\n")) {
                if ("".equals(c.strip())) {
                    array.add("");
                } else {
                    array.addAll(textWrap(c, width));
                }
            }
            lineWrapped.add(array);
        }

        int maxCellLines = 0;
        for (ArrayList<String> wrappedLine: lineWrapped) {
            if (wrappedLine.size() > maxCellLines) {
                maxCellLines = wrappedLine.size();
            }
        }

        for (int i = 0; i < lineWrapped.size(); i++) {
            ArrayList<String> cell = lineWrapped.get(i);
            char valign = this.columnVerticalAlign[i];
            if (isHeader) {
                valign = 't';
            }
            int missing = maxCellLines - cell.size();
            if (valign == 'm') {
                this.addSpaces(missing / 2, cell, true);
                this.addSpaces(missing / 2 + missing % 2, cell, false);
            } else if (valign == 'b') {
                this.addSpaces(missing, cell, true);
            } else {
                this.addSpaces(missing, cell, false);
            }
        }

        return lineWrapped;
    }

    /**
     * Own helper method for splitIt as it needs to create many empty lines
     */
    private void addSpaces(int missing, ArrayList<String> cell, boolean before) {
        for (int j = 0; j < missing; j++) {
            if (before) {
                cell.add(0, "");
            } else {
                cell.add("");
            }
        }
    }

    /**
     * Currently simply cuts off anywhere if run out of room
     * Should use the python module 'textwrap' and its 'wrap' method
     * which instantiates the class setting the attribute 'width' then
     * calls the 'wrap' method. This returns 'wrap_chunks' on 'split_chunks'
     * on the given text
     * @param text
     * @param width
     * @return
     */
    private static ArrayList<String> textWrap(String text, int width) {
        ArrayList<String> lines = new ArrayList<>();
        String currentLine = "";
        for (char chr: text.toCharArray()) {
            if (currentLine.length() < width) {
                currentLine += chr;
            } else {
                lines.add(currentLine);
                currentLine = String.valueOf(chr);
            }
        }
        lines.add(currentLine);
        return lines;
    }

    public static void main(String[] args) {
        TextTable table1 = new TextTable();
        table1.setColumnHorizontalAlign(new char[]{'l', 'r', 'c'});
        table1.setColumnVerticalAlign(new char[]{'t', 'm', 'b'});

        table1.addRows(new String[][] {
            {"Name", "Age", "Nickname"},
            {"Mr\nXavier\nHuon", "32", "Xav"},
            {"Mr\nBaptiste\nClement", "1", "Baby"},
            {"Mme\nLouise\nBourgeau", "28", "Lou\n \nLoue"}
        }, true);
        table1.print();

        System.out.println("\n\n");

        TextTable table2 = new TextTable();
        table2.setDeco(false, false, false, true);
        table2.setColumnHorizontalAlign(new char[]{'l', 'r', 'r', 'r', 'l'});
        table2.addRows(new String[][]{
            {"text", "float", "exp", "int", "auto"},
            {"abcd", "67", "654", "89", "128.001"},
            {"efghijk", "67.5434", ".654",  "89.6",  "12800000000000000000000.00023"},
            {"lmn",     "5e-78",   "5e-78", "89.4",  ".000000000000128"},
            {"opqrstu", ".023",    "5e+78", "92.",   "12800000000000000000000"}
        }, true);
        table2.print();
    }
}