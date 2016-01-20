package cz.destil.moodsync.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;
import cz.destil.moodsync.R;

/**
 * Created by benediktboss on 19/01/16.
 */
public class AlertWizardDialog {
  private ProgressDialog pdialog;
  private static AlertWizardDialog dialogs;

  private AlertWizardDialog() {

  }

  public static synchronized AlertWizardDialog getInstance() {
    if (dialogs == null) {
      dialogs = new AlertWizardDialog();
    }
    return dialogs;
  }

  public static void showErrorDialog(Context activityContext, String msg, int btnNameResId) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
    builder.setTitle(R.string.title_error).setMessage(msg).setPositiveButton(btnNameResId, null);
    AlertDialog alert = builder.create();
    alert.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    if (! ((Activity) activityContext).isFinishing()) {
      alert.show();
    }

  }

  /**
   * Stops running progress-bar
   */
  public void closeProgressDialog() {

    if (pdialog != null) {
      pdialog.dismiss();
      pdialog = null;
    }
  }

  /**
   * Shows progress-bar
   *
   * @param resID
   * @param act
   */
  public void showProgressDialog(int resID, Context ctx) {
    String message = ctx.getString(resID);
    pdialog = ProgressDialog.show(ctx, null, message, true, true);
    pdialog.setCancelable(false);

  }

  /**
   *
   * @param activityContext
   * @param msg
   * @param btnNameResId
   */
  public static void showAuthenticationErrorDialog(
      final Activity activityContext, String msg, int btnNameResId) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
    builder.setTitle(R.string.title_error).setMessage(msg)
        .setPositiveButton(btnNameResId, new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            activityContext.finish();

          }
        });
    AlertDialog alert = builder.create();
    alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    alert.show();
  }

}
