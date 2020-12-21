package challenges.tdb_owling;

public class BowlingGame
{
    private static final int SPARE = 10;
    private static final int STRIKE = 10;
    private static final int NUMBER_OF_FRAMES = 10;
    private static final int MAX_ROLLS = 21;

    private int score = 0;
    private int currentRoll = 0;
    private final int[] rolls = new int[MAX_ROLLS];

    public void roll(final int pins)
    {
        rolls[currentRoll++] = pins;
    }

    public int score()
    {
        int rollNumber = 0;
        for (int frame = 0; frame < NUMBER_OF_FRAMES; frame++)
        {
            int firstRoll = rolls[rollNumber];
            int secondRoll = rolls[rollNumber+1];
            if (firstRoll == STRIKE)
            {
                score += firstRoll + secondRoll + rolls[rollNumber+2] + rolls[rollNumber+3];
                rollNumber++;
            }
            else if (firstRoll + secondRoll == SPARE)
            {
                score += firstRoll + secondRoll + rolls[rollNumber+2];
                rollNumber = rollNumber + 2;
            }
            else
            {
                score += firstRoll + secondRoll;
                rollNumber = rollNumber + 2;
            }
        }
        return score;
    }
}
