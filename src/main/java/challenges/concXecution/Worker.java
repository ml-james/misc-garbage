package challenges.concXecution;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Worker
{
    public static class ExecutedTasks
    {
        public List<Runnable> successful = new ArrayList<>();
        public Set<Runnable> failed = new HashSet<>();
        public Set<Runnable> timedOut = new HashSet<>();

        public void addAllSuccessful(final List<Runnable> runnables)
        {
            successful.addAll(runnables);
        }

        public void addFailed(final Runnable runnable)
        {
            failed.add(runnable);
        }

        public void addTimedOut(final Runnable runnable)
        {
            timedOut.add(runnable);
        }
    }

    public ExecutedTasks execute(Collection<Runnable> actions, long timeoutMillis) throws InterruptedException
    {
        final CountDownLatch countDownLatch = new CountDownLatch(actions.size());

        ExecutorService executorService = new ThreadPoolExecutor(actions.size(), actions.size(), timeoutMillis,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        HashMap<Runnable, Future<Long>> runnableResults = new HashMap<>();
        for (Runnable action : actions)
        {
            Callable<Long> callable = () ->
            {
                try
                {
                    long scopedStartTime = System.currentTimeMillis();
                    action.run();
                    long scopedEndTime = System.currentTimeMillis();
                    return (Long) (scopedEndTime - scopedStartTime);
                }
                finally
                {
                    countDownLatch.countDown();
                }
            };
            Future<Long> future = executorService.submit(callable);
            runnableResults.put(action, future);
        }

        countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);

        ExecutedTasks result = new ExecutedTasks();
        List<SuccessfulRun> successfulRuns = new ArrayList<>();
        for (Map.Entry<Runnable, Future<Long>> runnableResult : runnableResults.entrySet())
        {
            try
            {
                if (runnableResult.getValue().isDone())
                {
                    Long timeTaken = runnableResult.getValue().get();
                    successfulRuns.add(new SuccessfulRun(runnableResult.getKey(), timeTaken));
                }
                else
                {
                    result.addTimedOut(runnableResult.getKey());
                }
            }
            catch (ExecutionException ee)
            {
                result.addFailed(runnableResult.getKey());
            }
        }

        result.addAllSuccessful(successfulRuns.stream()
                .sorted(Comparator.comparing(x -> x.timeTaken))
                .map(x -> x.runnable)
                .collect(Collectors.toList()));
        executorService.shutdown();

        return result;
    }

    private static class SuccessfulRun
    {
        private final Runnable runnable;
        private final long timeTaken;

        public SuccessfulRun(Runnable runnable, long timeTaken)
        {
            this.runnable = runnable;
            this.timeTaken = timeTaken;
        }
    }
}
