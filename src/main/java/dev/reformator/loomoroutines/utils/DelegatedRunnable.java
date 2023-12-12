package dev.reformator.loomoroutines.utils;

public class DelegatedRunnable implements Runnable {
    private Runnable goal = null;

    @Override
    public void run() {
        goal.run();
    }

    public Runnable getGoal() {
        return goal;
    }

    public void setGoal(Runnable goal) {
        this.goal = goal;
    }
}
