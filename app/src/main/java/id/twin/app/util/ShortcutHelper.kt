package id.twin.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import id.twin.app.ShortcutActivity
import id.twin.app.data.CloneItem

object ShortcutHelper {

    fun pinShortcut(context: Context, clone: CloneItem): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val launchIntent = Intent(context, ShortcutActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra(ShortcutActivity.EXTRA_PKG, clone.packageName)
            putExtra(ShortcutActivity.EXTRA_USER_ID, clone.userId)
        }

        val iconBitmap = clone.rawBitmap?.let { buildBadgedBitmap(it, clone.instanceNumber) }
        val iconCompat = if (iconBitmap != null) {
            IconCompat.createWithBitmap(iconBitmap)
        } else {
            IconCompat.createWithResource(context, context.applicationInfo.icon)
        }

        val info = ShortcutInfoCompat.Builder(context, "twin_${clone.userId}_${clone.packageName}")
            .setIntent(launchIntent)
            .setShortLabel(clone.displayName)
            .setLongLabel(clone.displayName)
            .setIcon(iconCompat)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }

    private fun buildBadgedBitmap(source: Bitmap, number: Int): Bitmap {
        val size = 144
        val scaled = Bitmap.createScaledBitmap(source, size, size, true)
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawBitmap(scaled, 0f, 0f, null)

        val badgeSize = 46f
        val margin = 4f
        val rect = RectF(
            size - badgeSize - margin,
            size - badgeSize - margin,
            size - margin,
            size - margin
        )

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawOval(
            RectF(rect.left - 3f, rect.top - 3f, rect.right + 3f, rect.bottom + 3f),
            borderPaint
        )

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3D6AF2")
            style = Paint.Style.FILL
        }
        canvas.drawOval(rect, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val textY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(number.toString(), rect.centerX(), textY, textPaint)
        return out
    }
}
