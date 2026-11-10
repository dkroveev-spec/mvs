package com.duckduckgram.duckerer.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import static org.telegram.messenger.LocaleController.getString;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * mvs v1: экран «Защита» в режиме чтения.
 *
 * Показывает только факты: чем подписан установленный APK, что уже проверяется
 * и чего ещё нет. Никаких «щитов» и выдуманного процента защиты. Действия
 * (подпись плагинов, проверка обновлений, allowlist) появятся в v1.1.
 */
public class DdgSecurityActivity extends DdgCategoryActivity {

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
        rows.add(Row.value(R.string.DDGSecurityApkSignature, this::apkSignature));
        rows.add(Row.value(R.string.DDGSecurityUpdates, () -> getString(R.string.DDGSecurityPlanned)));
        rows.add(Row.value(R.string.DDGSecurityPluginSignatures, () -> getString(R.string.DDGSecurityPlanned)));
        rows.add(Row.value(R.string.DDGSecurityAllowlist, () -> getString(R.string.DDGSecurityPlanned)));
        rows.add(Row.info(R.string.DDGSecurityStateInfo));

        rows.add(Row.header(R.string.DDGSecurityPluginsHeader));
        rows.add(Row.link(R.string.DDGSecurityPlugins, R.drawable.msg_plugins,
                () -> presentFragment(app.exteraless.plugins.ui.PluginsActivity.javaInstance())));
        rows.add(Row.info(R.string.DDGSecurityPluginsInfo));

        return rows;
    }

    /**
     * Отпечаток сертификата, которым подписан установленный APK. Первые шесть
     * байт SHA-256 достаточно, чтобы сравнить сборку с той, что показывает CI,
     * и не превращать строку в нечитаемую простыню.
     */
    private String apkSignature() {
        try {
            PackageManager pm = ApplicationLoader.applicationContext.getPackageManager();
            PackageInfo info = pm.getPackageInfo(ApplicationLoader.applicationContext.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            Signature[] signatures = info.signatures;
            if (signatures == null || signatures.length == 0) {
                return "—";
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signatures[0].toByteArray());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                if (i > 0) {
                    sb.append(':');
                }
                sb.append(String.format("%02X", hash[i]));
            }
            sb.append("…");
            return sb.toString();
        } catch (Exception e) {
            return "—";
        }
    }
}
