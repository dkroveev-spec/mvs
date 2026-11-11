package com.duckduckgram.duckerer.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import app.exteraless.icons.IconPacksConfig;
import app.exteraless.icons.IconShapeHelper;

/**
 * Плашка mvs в настройках.
 *
 * Графитовая карточка Ink: знак, словесный знак mvs. серифом с терракотовой
 * точкой, девиз, версия сборки и метка издания с пульсирующей точкой. Плашка
 * заодно служит заголовком хаба: под ней лежит всё, что mvs добавил поверх
 * форка, ниже — остальные экраны.
 *
 * Движение: появление снизу, нажатие уменьшает карточку, долгое нажатие
 * переключает маску иконки, тап открывает «О mvs».
 */
public class DdgHeaderCell extends LinearLayout {

    /** Бумага и терракота Ink. */
    private static final int PAPER = 0xFFF5F1E8;
    private static final int ACCENT = 0xFFD9552F;

    private Path shapePath;
    private ObjectAnimator pulse;

    private static int dp(int value) {
        return AndroidUtilities.dp(value);
    }

    private Path shapePath() {
        if (shapePath == null) {
            shapePath = IconShapeHelper.getFinalIconShapePath(46, 46, 13);
        }
        return shapePath;
    }

    private final View dot;

    public DdgHeaderCell(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(dp(16), dp(8), dp(16), dp(8));

        final boolean dark = Theme.getActiveTheme() != null && Theme.getActiveTheme().isDark();
        final int cardColor = dark ? 0xFF1D1C19 : 0xFF151412;
        final int muted = 0xB3F0EBE1;

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(VERTICAL);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(cardColor);
        shape.setCornerRadius(dp(22));
        if (dark) {
            shape.setStroke(dp(1), 0xFF3A3730);
        }
        card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x1FF5F1E8), shape, null));
        card.setPadding(dp(18), dp(18), dp(18), dp(14));
        card.setOnClickListener(v -> showAbout());
        card.setOnLongClickListener(this::toggleIconShape);
        card.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.985f).scaleY(0.985f).setDuration(90).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
            }
            return v.onTouchEvent(event);
        });
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
        head.addView(logo, new LinearLayout.LayoutParams(dp(46), dp(46)));

        LinearLayout text = new LinearLayout(context);
        text.setOrientation(VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(14);
        head.addView(text, textParams);

        // Словесный знак не переводим: mvs. — это логотип, а не подпись экрана.
        String mark = LocaleController.getString(R.string.DDGWordmark);
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

        LinearLayout editionRow = new LinearLayout(context);
        editionRow.setOrientation(HORIZONTAL);
        editionRow.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        footer.addView(editionRow, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        dot = new View(context);
        GradientDrawable dotShape = new GradientDrawable();
        dotShape.setShape(GradientDrawable.OVAL);
        dotShape.setColor(ACCENT);
        dot.setBackground(dotShape);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(6), dp(6));
        dotParams.rightMargin = dp(6);
        editionRow.addView(dot, dotParams);

        TextView edition = new TextView(context);
        edition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        edition.setTextColor(ACCENT);
        edition.setLetterSpacing(0.14f);
        edition.setText(LocaleController.getString(R.string.DDGInkEdition));
        editionRow.addView(edition, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setAlpha(0f);
        setTranslationY(dp(10));
        animate().alpha(1f).translationY(0f).setDuration(320).start();
        pulse = ObjectAnimator.ofFloat(dot, View.ALPHA, 0.3f, 1f);
        pulse.setDuration(1300);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (pulse != null) {
            pulse.cancel();
            pulse = null;
        }
        super.onDetachedFromWindow();
    }

    private void showAbout() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        new AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.DDGWordmark))
                .setMessage(buildVersionString() + "\n\n" + LocaleController.getString(R.string.DDGPlateAbout))
                .setPositiveButton(LocaleController.getString(R.string.OK), null)
                .show();
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
