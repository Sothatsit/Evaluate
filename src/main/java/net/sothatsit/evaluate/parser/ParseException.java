package net.sothatsit.evaluate.parser;

public class ParseException extends RuntimeException {

    public final StringStream stream;
    public final int fromIndex;
    public final int toIndex;

    public ParseException(String reason, StringStream stream, int index) {
        this(reason, stream, index, index);
    }

    public ParseException(String reason, StringStream stream, int fromIndex, int toIndex) {
        super(reason);

        this.stream = stream;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    public void printError() {
        System.err.println(printErrorString());
    }

    public String printErrorString() {
        StringBuilder error = new StringBuilder();

        error.append("ParseException: ").append(getMessage()).append("\n");
        error.append(getLocationString("Found at: ")).append("\n");

        return error.toString();
    }

    public String getLocationString() {
        return getLocationString("");
    }

    public String getLocationString(String prefix) {
        StringBuilder builder = new StringBuilder();

        builder.append(prefix).append(stream.input).append('\n');

        int fromIndex = prefix.length() + this.fromIndex;
        int toIndex = prefix.length() + this.toIndex;

        if(toIndex > fromIndex) {
            for(int index = 0; index < fromIndex; ++index) {
                builder.append(' ');
            }

            for(int index = fromIndex; index < toIndex; ++index) {
                if(index == fromIndex || index == toIndex - 1) {
                    builder.append('^');
                } else {
                    builder.append('~');
                }
            }
        } else {
            for(int index = 0; index < fromIndex - 1; ++index) {
                builder.append(' ');
            }

            builder.append("^^");
        }

        return builder.toString();
    }
}
