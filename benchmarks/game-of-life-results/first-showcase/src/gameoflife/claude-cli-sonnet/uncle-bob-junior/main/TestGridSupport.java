// src/test/java/life/TestGridSupport.java
package life;

final class TestGridSupport {
    private static final char ALIVE_CHAR = 'O';

    private TestGridSupport() {
    }

    static Grid gridFrom(String... rows) {
        CellState[][] cells = new CellState[rows.length][];
        for (int row = 0; row < rows.length; row++) {
            cells[row] = parseRow(rows[row]);
        }
        return new Grid(cells);
    }

    private static CellState[] parseRow(String row) {
        CellState[] cells = new CellState[row.length()];
        for (int col = 0; col < row.length(); col++) {
            cells[col] = row.charAt(col) == ALIVE_CHAR ? CellState.ALIVE : CellState.DEAD;
        }
        return cells;
    }
}
