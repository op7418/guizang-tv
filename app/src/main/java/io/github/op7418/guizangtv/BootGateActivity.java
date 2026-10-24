package io.github.op7418.guizangtv;
import android.app.Activity;
import android.os.Bundle;
/** One-shot gate for the stock launcher's package-based startup hook. */
public final class BootGateActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        try { BootStart.enter(this, getIntent(), true); } finally { finish(); }
    }
}
