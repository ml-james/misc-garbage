package miscellany.concXecution;

import java.time.Duration;

public class TaskFactory
{
    private static final long DEFAULT_TIMEOUT = Duration.ofSeconds(10).toMillis();

    public Runnable createSuccessfulTask(int succeedAfter)
    {
        return new Thread(() ->
        {
            try
            {
                System.out.println("Aha, a successful thread!");
                Thread.sleep(succeedAfter);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        });
    }

    public Runnable createFailingTask(int failAfter)
    {
        return new Thread(() ->
        {
            try
            {
                System.out.println("Aha, a failing thread!");
                Thread.sleep(failAfter);
                throw new FailException();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        });
    }

    public Runnable createTimeoutTask()
    {
        return new Thread(() ->
        {
            try
            {
                System.out.println("Aha a timeout thread!");
                Thread.sleep(DEFAULT_TIMEOUT * 2);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        });
    }
}
