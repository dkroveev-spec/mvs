package com.duckduckgram.duckerer.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import app.exteraless.icons.IconPacksConfig;
import app.exteraless.icons.IconShapeHelper;

/**
 * Плашка mvs в настройках.
 *
 * Вместо прежней светлой шапки с логотипом — графитовая карточка Ink: знак,
 * словесный знак mvs. серифом, девиз, версия сборки и метка издания. Плашка
 * заодно служит заголовком хаба: под ней лежит всё, что mvs добавил поверх
 * форка, ниже — остальные экраны.
 *
 * Долгое нажатие по карточке (или по знаку) переключает маску иконки, как
 * раньше: на прошивке со сквирклом логотип должен совпадать с рабочим столом.
 */
public class DdgHeaderCell extends LinearLayout {

    /** Бумага и терракота Ink. */
    private static final int PAPER = 0xFFF5F1E8;
    private static final int ACCENT = 0xFFD9552F;

    private Path shapePath;

    private static int dp(int value) {
        return AndroidUtilities.dp(value);
    }

    private Path shapePath() {
        if (shapePath == null) {
            shapePath = IconShapeHelper.getFinalIconShapePath(46, 46, 13);
        }
        return shapePath;
    }

    public DdgHeaderCell(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(dp(16), dp(8), dp(16), dp(8));

        final boolean dark = Theme.getActiveTheme() != null && Theme.getActiveTheme().isDark();
        final int cardColor = dark ? 0xFF1D1C19 : 0xFF151412;
        final int muted = 0xB3F0EBE1;

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(VERTICAL);
        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(cardColor);
        cardBackground.setCornerRadius(dp(22));
        if (dark) {
            cardBackground.setStroke(dp(1), 0xFF3A3730);
        }
        card.setBackground(cardBackground);
        card.setPadding(dp(18), dp(18), dp(18), dp(14));
        card.setOnLongClickListener(this::toggleIconShape);
        addView(card, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout head = new LinearLayout(context);
        head.setOrientation(HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(head, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ImageView logo = new ImageView(context) {
            @Override
            public void draw(Canvas canvas) {
                canvas.save();
                canvas.clipPath(shapePath());
                super.draw(canvas);
                canvas.restore();
            }
        };
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setBackgroundColor(cardColor);
        logo.setImageResource(R.drawable.duckduckgram_icon);
        logo.setOnLongClickListener(this::toggleIconShape);
        head.addView(logo, new LinearLayout.LayoutParams(dp(46), dp(46)));

        LinearLayout text = new LinearLayout(context);
        text.setOrientation(VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(14);
        head.addView(text, textParams);

        String mark = LocaleController.getString(R.string.DuckDuckGramPreferences);
        SpannableString wordmark = new SpannableString(mark);
        if (mark.endsWith(".")) {
            wordmark.setSpan(new ForegroundColorSpan(ACCENT),
                    mark.length() - 1, mark.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        TextView title = new TextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        title.setTypeface(AndroidUtilities.serif());
        title.setTextColor(PAPER);
        title.setIncludeFontPadding(false);
        title.setSingleLine(true);
        title.setText(wordmark);
        text.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tagline = new TextView(context);
        tagline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        tagline.setTextColor(muted);
        tagline.setText(LocaleController.getString(R.string.DuckDuckGramPreferencesInfo));
        tagline.setMaxLines(2);
        tagline.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams taglineParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        taglineParams.topMargin = dp(4);
        text.addView(tagline, taglineParams);

        View divider = new View(context);
        divider.setBackgroundColor(0x26F5F1E8);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dividerParams.topMargin = dp(16);
        card.addView(divider, dividerParams);

        LinearLayout footer = new LinearLayout(context);
        footer.setOrientation(HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        footerParams.topMargin = dp(10);
        card.addView(footer, footerParams);

        TextView version = new TextView(context);
        version.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        version.setTextColor(muted);
        version.setSingleLine(true);
        version.setText(buildVersionString());
        footer.addView(version, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView edition = new TextView(context);
        edition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        edition.setTextColor(ACCENT);
        edition.setLetterSpacing(0.14f);
        edition.setGravity(Gravity.END);
        edition.setText(LocaleController.getString(R.string.DDGInkEdition));
        footer.addView(edition, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    }

    private boolean toggleIconShape(View view) {
        IconPacksConfig.toggleSystemIconShape();
        IconShapeHelper.invalidate();
        shapePath = null;
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        view.invalidate();
        return true;
    }

    /** "12.9.2 (1258)" — версия плюс versionCode из PackageInfo, как в оригинале. */
    private static String buildVersionString() {
        StringBuilder sb = new StringBuilder(BuildVars.BUILD_VERSION_STRING);
        try {
            PackageInfo info = ApplicationLoader.applicationContext.getPackageManager()
                    .getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            if (info != null) {
                sb.append(" (").append(info.versionCode).append(")");
            }
        } catch (Exception ignore) {
        }
        if (!TextUtils.isEmpty(BuildVars.BUILD_COMMIT_ID)) {
            sb.append(" · ").append(BuildVars.BUILD_COMMIT_ID);
        }
        return sb.toString();
    }
}
