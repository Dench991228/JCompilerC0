package jcompiler.util;

public class Pos {
    public int Row;
    public int Col;

    public Pos(int row, int col) {
        this.Row = row;
        this.Col = col;
    }

    public Pos nextCol() {
        return new Pos(Row, Col + 1);
    }

    public Pos nextRow() {
        return new Pos(Row + 1, 0);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Pos(row: ").append(Row).append(", col: ").append(Col).append(")").toString();
    }
}
