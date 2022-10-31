package utils;


import cli.CliRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

public final class ThreadUtils {

    private final static Logger LOG = LogFactory.logger(CliRequest.class);

    private ThreadUtils() throws IllegalAccessException {
        throw new IllegalAccessException("Utility class : static access only.");
    }

    public static <T> List<T> runParallel(List<? extends Callable<T>> callables) throws ExecutionException, InterruptedException {
        assert callables != null && !callables.isEmpty();
        List<T> result = new ArrayList<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> c : callables) {
            futures.add(executorService.submit(c));
        }
        int l = futures.size();
        for (Future<T> f : futures) {
            result.add(f.get());
            LOG.fine(String.format("runParallel %d / %d", result.size(), l));
        }
        executorService.shutdown();
        return result;
    }

    public static <T> List<T> runMergeParallel(List<? extends Callable<List<T>>> callables) throws ExecutionException, InterruptedException {
        assert callables != null && !callables.isEmpty();
        List<T> result = new ArrayList<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<List<T>>> futures = new ArrayList<>();
        for (Callable<List<T>> c : callables) {
            futures.add(executorService.submit(c));
        }
        int l = futures.size();
        for (Future<List<T>> f : futures) {
            result.addAll(f.get());
            LOG.fine(String.format("runParallel %d / %d", result.size(), l));
        }
        executorService.shutdown();
        return result;
    }

}


