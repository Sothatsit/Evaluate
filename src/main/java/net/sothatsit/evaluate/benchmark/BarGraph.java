package net.sothatsit.evaluate.benchmark;

public class BarGraph {

    private static final char EMPTY = ' ';
    private static final char FULL = '\u2588';
    private static final char[] BLOCKS = {
            EMPTY,
            '\u2581',
            '\u2582',
            '\u2583',
            '\u2584',
            '\u2585',
            '\u2586',
            '\u2587',
            FULL
    };

    private static char getBlock(double fullness) {
        if(fullness < 0)
            return EMPTY;
        if(fullness > 1)
            return FULL;

        int index = (int) Math.round(fullness * (BLOCKS.length - 1));

        return BLOCKS[index];
    }

    public static String createBarGraph(String title, double[] columnHeights, int height) {
        StringBuilder builder = new StringBuilder();

        builder.append("^ ");

        int spaces = (columnHeights.length - title.length()) / 2;

        if(spaces < 0) {
            builder.append(title.substring(columnHeights.length));
        } else {
            for(int i=0; i < spaces; ++i) {
                builder.append(' ');
            }

            builder.append(title);
        }

        builder.append('\n');

        for(int row = height - 1; row >= 0; --row) {
            builder.append("| ");

            for(double column : columnHeights) {
                builder.append(getBlock(column * height - row));
            }

            builder.append('\n');
        }

        builder.append(" -");

        for(int i=0; i < columnHeights.length; ++i) {
            builder.append('-');
        }

        builder.append(">\n");

        return builder.toString();
    }
}
