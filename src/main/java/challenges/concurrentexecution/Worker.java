package challenges.concurrentexecution;

import java.util.*;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;

public class Worker
{
    public static class ExecutedTasks
    {
        public List<Runnable> successful = new ArrayList<>();
        public Set<Runnable> failed = new HashSet<>();
        public Set<Runnable> timedOut = new HashSet<>();

        public void addSuccessful(final Runnable runnable)
        {
            successful.add(runnable);
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
        long startTime = System.currentTimeMillis();

        ExecutorService executorService = new ThreadPoolExecutor(actions.size(), actions.size(), timeoutMillis,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        HashMap<Runnable, Future<?>> runnableResults = new HashMap<>();
        for (Runnable action : actions)
        {
            Future<?> future = executorService.submit(action);
            runnableResults.put(action, future);
        }

        await().until(() -> runnableResults.values().stream().allMatch(Future::isDone) ||
                timeoutMillis < System.currentTimeMillis() - startTime);

        ExecutedTasks result = new ExecutedTasks();
        for (Map.Entry<Runnable, Future<?>> runnableResult : runnableResults.entrySet())
        {
            try
            {
                if (runnableResult.getValue().isDone())
                {
                    runnableResult.getValue().get();
                    result.addSuccessful(runnableResult.getKey());
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
        executorService.shutdown();
        return result;
    }
}
