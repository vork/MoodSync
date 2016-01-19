package cz.destil.moodsync.light;

import android.graphics.Bitmap;
import android.support.annotation.IntDef;
import android.support.v7.graphics.Palette;

import cz.destil.moodsync.R;
import cz.destil.moodsync.core.App;
import cz.destil.moodsync.core.Config;
import cz.destil.moodsync.util.SleepTask;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Periodically extracts color from a bitmap.
 *
 * @author David Vávra (david@vavra.me)
 */
public class ColorExtractor {

    @IntDef({COLOR_EXTRACT_ALL, COLOR_EXTRACT_CENTER, COLOR_EXTRACT_LEFT, COLOR_EXTRACT_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ColorExtractMode {}

    public static final int COLOR_EXTRACT_ALL = 0;
    public static final int COLOR_EXTRACT_CENTER = 1;
    public static final int COLOR_EXTRACT_LEFT = 2;
    public static final int COLOR_EXTRACT_RIGHT = 3;

    private static ColorExtractor sInstance;
    private boolean mRunning = true;

    public static ColorExtractor get() {
        if (sInstance == null) {
            sInstance = new ColorExtractor();
        }
        return sInstance;
    }

    public void start(final MirroringHelper mirroring, final Listener listener, @ColorExtractMode final int extractMode) {
        mRunning = true;
        new SleepTask(Config.INITIAL_DELAY, new SleepTask.Listener() {
            @Override
            public void awoken() {
                extractBitmap(mirroring, listener, extractMode);
            }
        }).start();
    }

    private void extractBitmap(final MirroringHelper mirroring, final Listener listener, @ColorExtractMode
    final int extractMode) {
        if (mRunning) {
            mirroring.getLatestBitmap(new MirroringHelper.Listener() {
                @Override
                public void onBitmapAvailable(final Bitmap bitmap) {
                    Bitmap usedBitmap;
                    if(extractMode == COLOR_EXTRACT_CENTER) {
                        usedBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 4, 0, bitmap.getWidth() / 4 * 3, bitmap.getHeight());
                    } else if (extractMode == COLOR_EXTRACT_LEFT) {
                        usedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() / 4, bitmap.getHeight());
                    } else if (extractMode == COLOR_EXTRACT_RIGHT) {
                        usedBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 4 * 3, 0, bitmap.getWidth(), bitmap.getHeight());
                    } else {
                        usedBitmap = bitmap;
                    }

                    final Bitmap bmap = usedBitmap;

                    Palette.generateAsync(bmap, 25, new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            bmap.recycle();
                            bitmap.recycle();
                            int defaultColor = App.get().getResources().getColor(R.color.not_recognized);
                            final int color = palette.getVibrantColor(defaultColor);
                            listener.onColorExtracted(color);
                            new SleepTask(Config.FREQUENCE_OF_SCREENSHOTS, new SleepTask.Listener() {
                                @Override
                                public void awoken() {
                                    extractBitmap(mirroring, listener, extractMode);
                                }
                            }).start();
                        }
                    });
                }
            });
        }
    }

    public void stop() {
        mRunning = false;
    }

    public interface Listener {
        public void onColorExtracted(int color);
    }


}
