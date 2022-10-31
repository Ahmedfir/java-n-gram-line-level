package cli;

import tokenizer.line.AbstractLineTokenizer;
import tokenizer.line.UTFLineTokenizer;
import tokenizer.line.java.JavaLemmeLineTokenizer;

public enum CliArgTokenizer {
    JavaParser("JP"),
    UTF8("UTF8");


    private final String cliParam;

    CliArgTokenizer(String param) {
        this.cliParam = param;
    }

    private static CliArgTokenizer parse(String param){
        for (CliArgTokenizer value : CliArgTokenizer.values()) {
            if (value.cliParam.equals(param)){
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown tokenizer : "+param);
    }

    static AbstractLineTokenizer newTokenizer(String param){
        CliArgTokenizer te = parse(param);
        switch (te){
            case UTF8:
                return new UTFLineTokenizer();
            case JavaParser:
                return new JavaLemmeLineTokenizer();
            default:
                throw new IllegalArgumentException("Unknown tokenizer : "+param);
        }
    }
}
