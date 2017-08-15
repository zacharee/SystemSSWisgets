package tk.zacharywander.systemsswisgets.misc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Arrays;

public class Util
{
    /**
     * Open app by specified package name/ID
     * @param context caller's context
     * @param packageName desired app's package name
     * @return success
     */
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new PackageManager.NameNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean isToggleShown(Context context, String key) {
        return Settings.Global.getInt(context.getContentResolver(), key + "_shown", 1) == 1;
    }

    public static void setToggleShown(Context context, String key, boolean shown) {
        Settings.Global.putInt(context.getContentResolver(), key + "_shown", shown ? 1 : 0);
    }

    public static ArrayList<String> parseSavedToggleOrder(Context context, ArrayList<String> def) {
        String load = Settings.Global.getString(context.getContentResolver(), "toggle_order");

        if (isEmptyNull(load)) return def;

        return new ArrayList<>(Arrays.asList(load.split("[,]")));
    }

    public static void saveNewToggleOrder(Context context, ArrayList<String> order) {
        StringBuilder builder = new StringBuilder(order.get(0));

        for (int i = 1; i < order.size(); i++) {
            builder.append(",").append(order.get(i));
        }

        Settings.Global.putString(context.getContentResolver(), "toggle_order", builder.toString());
    }

    /**
     * Whether input String is empty/null
     * @param load input
     * @return is empty or null
     */
    public static boolean isEmptyNull(String load) {
        return load == null || load.isEmpty();
    }
}
