package cz.destil.moodsync.light;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.IntDef;
import android.support.v7.graphics.Palette;

import cz.destil.moodsync.R;
import cz.destil.moodsync.core.App;
import cz.destil.moodsync.core.Config;
import cz.destil.moodsync.util.SleepTask;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Periodically extracts color from a bitmap.
 *
 * @author David Vávra (david@vavra.me)
 */
public class ColorExtractor {

    @IntDef({COLOR_EXTRACT_ALL, COLOR_EXTRACT_CENTER, COLOR_EXTRACT_LEFT, COLOR_EXTRACT_RIGHT, COLOR_EXTRACT_OFF})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ColorExtractMode {}

    public static final int COLOR_EXTRACT_ALL = 0;
    public static final int COLOR_EXTRACT_CENTER = 1;
    public static final int COLOR_EXTRACT_LEFT = 2;
    public static final int COLOR_EXTRACT_RIGHT = 3;
    public static final int COLOR_EXTRACT_OFF = 4;

    private static ColorExtractor sInstance;
    private boolean mRunning = true;

    public static ColorExtractor get() {
        if (sInstance == null) {
            sInstance = new ColorExtractor();
        }
        return sInstance;
    }

    public void start(final MirroringHelper mirroring, final Listener listener) {
        mRunning = true;
        new SleepTask(Config.INITIAL_DELAY, new SleepTask.Listener() {
            @Override
            public void awoken() {
                extractBitmap(mirroring, listener);
            }
        }).start();
    }

    private void extractBitmap(final MirroringHelper mirroring, final Listener listener) {
        if (mRunning) {
            mirroring.getLatestBitmap(new MirroringHelper.Listener() {
                @Override public void onBitmapAvailable(final Bitmap center, final Bitmap left, Bitmap right,
                    Bitmap all) {
                    new AsyncTask<Bitmap, Void, List<ColorGroup>>(){

                        @Override protected List<ColorGroup> doInBackground(Bitmap... params) {
                            int defaultColor = App.get().getResources().getColor(R.color.not_recognized);

                            if(params.length != 4) {
                                throw new IllegalArgumentException("Need to pass in 4 bitmaps");
                            }
                            Palette centerPalette = Palette.from(params[0]).generate();
                            Palette leftPalette = Palette.from(params[1]).generate();
                            Palette rightPalette = Palette.from(params[2]).generate();
                            Palette allPalette = Palette.from(params[3]).generate();

                            List<ColorGroup> toRet = new ArrayList<ColorGroup>();
                            toRet.add(new ColorGroup(centerPalette.getVibrantColor(defaultColor), COLOR_EXTRACT_CENTER));
                            toRet.add(new ColorGroup(leftPalette.getVibrantColor(defaultColor), COLOR_EXTRACT_LEFT));
                            toRet.add(new ColorGroup(rightPalette.getVibrantColor(defaultColor), COLOR_EXTRACT_RIGHT));
                            toRet.add(new ColorGroup(allPalette.getVibrantColor(defaultColor), COLOR_EXTRACT_ALL));
                            return toRet;
                        }

                        protected void onPostExecute(List<ColorGroup> result) {
                            listener.onColorExtracted(result);
                            new SleepTask(Config.FREQUENCE_OF_SCREENSHOTS, new SleepTask.Listener() {
                                @Override
                                public void awoken() {
                                    extractBitmap(mirroring, listener);
                                }
                            }).start();
                        }
                    }.execute(center, left, right, all);
                }
            });
        }
    }

    public void stop() {
        mRunning = false;
    }

    public interface Listener {
        public void onColorExtracted(List<ColorGroup> colorGroups);
    }

    public class ColorGroup {
        public int color;
        public @ColorExtractMode int extractMode;

        public ColorGroup(int color, int extractMode) {
            this.color = color;
            this.extractMode = extractMode;
        }
    }


}
