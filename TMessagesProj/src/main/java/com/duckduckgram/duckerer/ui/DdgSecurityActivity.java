package com.duckduckgram.duckerer.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.widget.Toast;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * mvs v1: экран «Защита».
 *
 * Показывает только факты: чем подписан установленный APK, что уже проверяется
 * и чего ещё нет. Подпись можно скопировать, проверку обновлений — открыть
 * вручную на странице релизов. Действия (подпись плагинов, allowlist) — v1.1.
 */
public class DdgSecurityActivity extends DdgCategoryActivity {

    private static final String RELEASES_URL = "https://github.com/dkroveev-spec/mvs/releases";

    @Override
    protected int titleRes() {
        return R.string.DDGSecurityTitle;
    }

    @Override
    protected int searchGuid() {
        return 31400;
    }

    @Override
    protected int searchIcon() {
        return R.drawable.msg_stealth_locked;
    }

    @Override
    protected int searchPrefixRes() {
        return R.string.DDGSecurityTitle;
    }

    @Override
    protected List<Row> buildRows() {
        List<Row> rows = new ArrayList<>();

        rows.add(Row.header(R.string.DDGSecurityState));
        rows.add(Row.valueAction(R.string.DDGSecurityApkSignature, this::apkSignature, this::copySignature));
        rows.add(Row.valueAction(R.string.DDGSecurityUpdates,
                () -> getString(R.string.DDGSecurityManual), this::openReleases));
        rows.add(Row.value(R.string.DDGSecurityPluginSignatures, () -> getString(R.string.DDGSecurityPlanned)));
        rows.add(Row.value(R.string.DDGSecurityAllowlist, () -> getString(R.string.DDGSecurityPlanned)));
        rows.add(Row.info(R.string.DDGSecurityStateInfo));

        rows.add(Row.header(R.string.DDGSecurityPluginsHeader));
        rows.add(Row.link(R.string.DDGSecurityPlugins, R.drawable.msg_plugins,
                () -> presentFragment(app.exteraless.plugins.ui.PluginsActivity.javaInstance())));
        rows.add(Row.info(R.string.DDGSecurityPluginsInfo));

        return rows;
    }

    private void openReleases() {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        Browser.openUrl(activity, RELEASES_URL);
    }

    private void copySignature() {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        try {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null) {
                return;
            }
            clipboard.setPrimaryClip(ClipData.newPlainText("mvs APK SHA-256", sha256()));
            Toast.makeText(activity, getString(R.string.DDGSecurityCopied), Toast.LENGTH_SHORT).show();
        } catch (Throwable ignore) {
        }
    }

    /** Короткий отпечаток для строки: шесть байт хватает, чтобы сверить сборку. */
    private String apkSignature() {
        byte[] hash = sha256Bytes();
        if (hash == null) {
            return "—";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(6, hash.length); i++) {
            if (i > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02X", hash[i]));
        }
        sb.append("…");
        return sb.toString();
    }

    private String sha256() {
        byte[] hash = sha256Bytes();
        if (hash == null) {
            return "—";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            if (i > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02X", hash[i]));
        }
        return sb.toString();
    }

    private byte[] sha256Bytes() {
        try {
            PackageManager pm = ApplicationLoader.applicationContext.getPackageManager();
            PackageInfo info = pm.getPackageInfo(ApplicationLoader.applicationContext.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            Signature[] signatures = info.signatures;
            if (signatures == null || signatures.length == 0) {
                return null;
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(signatures[0].toByteArray());
        } catch (Throwable e) {
            return null;
        }
    }
}
