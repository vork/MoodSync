package cz.destil.moodsync.light;

import cz.destil.moodsync.core.App;
import cz.destil.moodsync.core.BaseAsyncTask;
import cz.destil.moodsync.event.SuccessEvent;

/**
 * Controller which controls LIFX lights.
 *
 * @author David Vávra (david@vavra.me)
 */
public abstract class LightsController {
    protected static final int TIMEOUT = 5000;
    protected boolean mWorkingFine;
    protected boolean mDisconnected;
    protected int mPreviousColor = -1;

    public abstract void changeColor(int color);

    public void init() {
        mWorkingFine = false;
        mDisconnected = false;
    }

    protected void startRocking() {
        App.bus().post(new SuccessEvent());
        mWorkingFine = true;
    }

    public abstract void start();

    public void stop() {
        mDisconnected = true;
    }

    public abstract void signalStop();

    public abstract class TimeoutTask extends BaseAsyncTask {

        @Override
        public void inBackground() {
            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
