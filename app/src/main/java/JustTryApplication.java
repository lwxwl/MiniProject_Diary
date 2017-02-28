import android.app.Application;

import timber.log.Timber;

/**
 * Created by lwxwl on 2017/2/9.
 */

public class JustTryApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
//使用Timber框架记录时光日记

