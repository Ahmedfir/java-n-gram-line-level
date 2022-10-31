import cli.CliRequest;
import modelling.exception.TrainingFailedException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {


    /**
     * repo=repo_path
     * in=file_absolute_path
     * in=file_absolute_path
     * in=file_absolute_path
     * out=output_file_absolute_path
     *
     * @param args
     */
    public static void main(String... args) throws IOException, ExecutionException, InterruptedException, TrainingFailedException {
        CliRequest cliRequest = CliRequest.parseArgs(args);
        cliRequest.train().rank();
    }

}
