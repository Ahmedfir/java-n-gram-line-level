package cli;

import tokenizer.AbstractTokenizer;
import tokenizer.file.UTFFileTokenizer;
import tokenizer.file.java.xml.JavaLemmeTokenizer;
import tokenizer.line.AbstractLineTokenizer;
import tokenizer.line.UTFLineTokenizer;
import tokenizer.line.java.JavaLemmeLineTokenizer;

public enum CliArgTokenizer {
    LineLevelJavaParser("JP"),
    LineLevelUTF8("UTF8"),
    FileLevelJavaParser("F_JP"),
    FileLevelUTF8("F_UTF8");


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

    static AbstractTokenizer newTokenizer(String param){
        CliArgTokenizer te = parse(param);
        switch (te){
            case LineLevelUTF8:
                return new UTFLineTokenizer();
            case LineLevelJavaParser:
                return new JavaLemmeLineTokenizer();
            case FileLevelUTF8:
                return new UTFFileTokenizer();
            case FileLevelJavaParser:
                return new JavaLemmeTokenizer();
            default:
                throw new IllegalArgumentException("Unknown tokenizer : "+param);
        }
    }
}
