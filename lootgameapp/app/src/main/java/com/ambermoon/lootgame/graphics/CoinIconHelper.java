package com.ambermoon.lootgame.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.TypedValue;

/**
 * Replaces the Unicode coin character (\u25C8) in text with an inline
 * coin GIF sprite loaded from assets/icons/coin/coin.gif.
 */
public class CoinIconHelper {
    private static final String COIN_PATH = "icons/coin/coin.gif";
    private static final char COIN_CHAR = '\u25C8';

    /**
     * Returns a CharSequence where every occurrence of \u25C8 in the given
     * text is replaced with an inline coin icon sized to match the text.
     * Falls back to the original string if the asset is missing.
     */
    public static CharSequence withCoin(Context context, String text, float textSizeSp) {
        if (context == null || text == null) return text;

        int idx = text.indexOf(COIN_CHAR);
        if (idx == -1) return text;

        AssetLoader.ImageAsset asset = AssetLoader.load(COIN_PATH);
        if (asset == null || asset.bitmap == null) return text;

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp,
                context.getResources().getDisplayMetrics());
        int size = Math.round(px * 1.2f);

        SpannableString spannable = new SpannableString(text);
        while (idx != -1) {
            Bitmap scaled = Bitmap.createScaledBitmap(asset.bitmap, size, size, false);
            ImageSpan span = new ImageSpan(context, scaled, ImageSpan.ALIGN_BASELINE);
            spannable.setSpan(span, idx, idx + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            idx = text.indexOf(COIN_CHAR, idx + 1);
        }

        return spannable;
    }
}
