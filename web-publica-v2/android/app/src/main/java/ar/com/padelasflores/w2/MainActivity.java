package ar.com.padelasflores.w2;

import android.os.Bundle;
import android.webkit.ValueCallback;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private long ultimaSalida = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        ViewCompat.getWindowInsetsController(getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        ViewCompat.getWindowInsetsController(getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, windowInsets) -> {
            Insets system = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(system.left, system.top, system.right, Math.max(system.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { resolverAtrasWeb(); }
        });
    }

    private void resolverAtrasWeb() {
        if (getBridge() == null || getBridge().getWebView() == null) { salidaProtegida(); return; }
        String script = "(()=>{try{return String(Boolean(window.__w2HandleNativeBack&&window.__w2HandleNativeBack()))}catch(e){return 'false'}})()";
        getBridge().getWebView().evaluateJavascript(script, new ValueCallback<String>() {
            @Override public void onReceiveValue(String value) {
                if ("\"true\"".equals(value)) return;
                if (getBridge().getWebView().canGoBack()) getBridge().getWebView().goBack();
                else salidaProtegida();
            }
        });
    }

    private void salidaProtegida() {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimaSalida < 2000L) { finish(); return; }
        ultimaSalida = ahora;
        Toast.makeText(this, "Volvé a presionar para salir", Toast.LENGTH_SHORT).show();
    }
}
