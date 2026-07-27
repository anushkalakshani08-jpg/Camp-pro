package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AppSettings
import com.example.data.model.QuotationEntity
import com.example.data.model.QuotationItemEntity
import com.example.data.model.RentalBillEntity
import com.example.data.model.RentalItemEntity
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateBillPdf(
        context: Context,
        bill: RentalBillEntity,
        items: List<RentalItemEntity>,
        settings: AppSettings
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 dimensions at 72dpi
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawPdfContent(
            context = context,
            canvas = canvas,
            documentTitle = "RENTAL INVOICE",
            docNumber = bill.billNumber,
            customerName = bill.customerName,
            customerPhone = bill.customerPhone,
            customerNic = bill.customerNic,
            startDate = bill.startDate,
            endDate = bill.endDate,
            totalDays = bill.totalDays,
            tableItems = items.map {
                PdfItemRow(it.itemName, it.itemCategory, it.dailyPrice, it.quantity, it.totalItemPrice)
            },
            subtotal = bill.subtotal,
            discountValue = bill.discountValue,
            discountType = bill.discountType,
            totalDeposit = bill.totalDeposit,
            grandTotal = bill.grandTotal,
            notes = bill.notes,
            settings = settings
        )

        pdfDocument.finishPage(page)

        val pdfDir = File(context.cacheDir, "invoices").apply { if (!exists()) mkdirs() }
        val pdfFile = File(pdfDir, "${bill.billNumber}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return pdfFile
    }

    fun generateQuotationPdf(
        context: Context,
        quote: QuotationEntity,
        items: List<QuotationItemEntity>,
        settings: AppSettings
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawPdfContent(
            context = context,
            canvas = canvas,
            documentTitle = "COST ESTIMATE & QUOTATION",
            docNumber = quote.quoteNumber,
            customerName = quote.customerName,
            customerPhone = quote.customerPhone,
            customerNic = quote.customerNic,
            startDate = quote.startDate,
            endDate = quote.endDate,
            totalDays = quote.totalDays,
            tableItems = items.map {
                PdfItemRow(it.itemName, "Gear", it.dailyPrice, it.quantity, it.totalItemPrice)
            },
            subtotal = quote.subtotal,
            discountValue = quote.discountValue,
            discountType = quote.discountType,
            totalDeposit = quote.totalDeposit,
            grandTotal = quote.grandTotal,
            notes = quote.notes,
            settings = settings
        )

        pdfDocument.finishPage(page)

        val pdfDir = File(context.cacheDir, "quotations").apply { if (!exists()) mkdirs() }
        val pdfFile = File(pdfDir, "${quote.quoteNumber}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return pdfFile
    }

    private data class PdfItemRow(
        val name: String,
        val category: String,
        val dailyPrice: Double,
        val qty: Int,
        val total: Double
    )

    private fun drawPdfContent(
        context: Context,
        canvas: Canvas,
        documentTitle: String,
        docNumber: String,
        customerName: String,
        customerPhone: String,
        customerNic: String,
        startDate: Long,
        endDate: Long,
        totalDays: Int,
        tableItems: List<PdfItemRow>,
        subtotal: Double,
        discountValue: Double,
        discountType: String,
        totalDeposit: Double,
        grandTotal: Double,
        notes: String,
        settings: AppSettings
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        // Background
        canvas.drawColor(Color.WHITE)

        // Top Accent Bar (Nature Green / Primary)
        paint.color = Color.parseColor("#1B4332")
        canvas.drawRect(0f, 0f, 595f, 15f, paint)

        var y = 45f

        // Draw Business Logo if available
        val logoBitmap: Bitmap? = try {
            if (!settings.businessLogoPath.isNullOrEmpty()) {
                BitmapFactory.decodeFile(settings.businessLogoPath)
            } else {
                BitmapFactory.decodeResource(context.resources, com.example.R.drawable.img_app_icon)
            }
        } catch (e: Exception) {
            null
        }

        if (logoBitmap != null) {
            val scaled = Bitmap.createScaledBitmap(logoBitmap, 60, 60, true)
            canvas.drawBitmap(scaled, 35f, y, paint)
        }

        // Business Header Text
        val headerX = if (logoBitmap != null) 105f else 35f
        paint.color = Color.parseColor("#1B4332")
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText(settings.businessName, headerX, y + 20f, paint)

        paint.color = Color.parseColor("#4A5568")
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText(settings.businessAddress, headerX, y + 36f, paint)
        canvas.drawText("Tel: ${settings.businessPhone} | Email: ${settings.businessEmail}", headerX, y + 50f, paint)

        // Document Title
        paint.color = Color.parseColor("#2D6A4F")
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(documentTitle, 560f, y + 20f, paint)

        paint.color = Color.parseColor("#718096")
        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText(docNumber, 560f, y + 38f, paint)

        y += 80f
        paint.textAlign = Paint.Align.LEFT

        // Divider
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawLine(35f, y, 560f, y, paint)

        y += 20f

        // Customer & Rental Period Info Card
        paint.color = Color.parseColor("#F7FAFC")
        val infoRect = RectF(35f, y, 560f, y + 75f)
        canvas.drawRoundRect(infoRect, 8f, 8f, paint)

        paint.color = Color.parseColor("#2D3748")
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("CUSTOMER DETAILS:", 45f, y + 20f, paint)

        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#4A5568")
        canvas.drawText("Name: $customerName", 45f, y + 38f, paint)
        canvas.drawText("Phone: $customerPhone | NIC: $customerNic", 45f, y + 55f, paint)

        paint.color = Color.parseColor("#2D3748")
        paint.isFakeBoldText = true
        canvas.drawText("RENTAL PERIOD:", 320f, y + 20f, paint)

        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#4A5568")
        canvas.drawText("Start: ${DateUtils.formatDate(startDate)}", 320f, y + 38f, paint)
        canvas.drawText("Return: ${DateUtils.formatDate(endDate)} ($totalDays Days)", 320f, y + 55f, paint)

        y += 95f

        // Table Header
        paint.color = Color.parseColor("#1B4332")
        val tableHeaderRect = RectF(35f, y, 560f, y + 24f)
        canvas.drawRoundRect(tableHeaderRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = true

        canvas.drawText("ITEM DESCRIPTION", 45f, y + 16f, paint)
        canvas.drawText("CATEGORY", 230f, y + 16f, paint)
        canvas.drawText("DAILY RATE", 340f, y + 16f, paint)
        canvas.drawText("QTY", 440f, y + 16f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL (LKR)", 550f, y + 16f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 28f

        // Table Rows
        tableItems.forEachIndexed { index, item ->
            if (index % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(35f, y - 4f, 560f, y + 18f, paint)
            }

            paint.color = Color.parseColor("#2D3748")
            paint.textSize = 10f
            paint.isFakeBoldText = false

            canvas.drawText(item.name, 45f, y + 10f, paint)
            canvas.drawText(item.category, 230f, y + 10f, paint)
            canvas.drawText(DateUtils.formatCurrency(item.dailyPrice), 340f, y + 10f, paint)
            canvas.drawText("${item.qty}", 445f, y + 10f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(DateUtils.formatCurrency(item.total), 550f, y + 10f, paint)
            paint.textAlign = Paint.Align.LEFT

            y += 22f
        }

        // Table Bottom Border
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(35f, y, 560f, y, paint)

        y += 20f

        // Billing Summary Box on right
        val summaryX = 330f
        paint.color = Color.parseColor("#F1F5F9")
        val summaryRect = RectF(summaryX, y, 560f, y + 110f)
        canvas.drawRoundRect(summaryRect, 6f, 6f, paint)

        var sumY = y + 20f
        paint.textSize = 10f
        paint.color = Color.parseColor("#475569")

        canvas.drawText("Subtotal ($totalDays Days):", summaryX + 15f, sumY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(DateUtils.formatCurrency(subtotal), 545f, sumY, paint)
        paint.textAlign = Paint.Align.LEFT

        if (discountValue > 0) {
            sumY += 18f
            canvas.drawText("Discount:", summaryX + 15f, sumY, paint)
            paint.textAlign = Paint.Align.RIGHT
            val discText = if (discountType == "PERCENT") "-${discountValue}%" else "-${DateUtils.formatCurrency(discountValue)}"
            canvas.drawText(discText, 545f, sumY, paint)
            paint.textAlign = Paint.Align.LEFT
        }

        sumY += 18f
        canvas.drawText("Refundable Deposit:", summaryX + 15f, sumY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(DateUtils.formatCurrency(totalDeposit), 545f, sumY, paint)
        paint.textAlign = Paint.Align.LEFT

        sumY += 22f
        paint.color = Color.parseColor("#1B4332")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("GRAND TOTAL:", summaryX + 15f, sumY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(DateUtils.formatCurrency(grandTotal), 545f, sumY, paint)
        paint.textAlign = Paint.Align.LEFT

        // Terms & Conditions on left
        paint.color = Color.parseColor("#2D3748")
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("TERMS & CONDITIONS:", 35f, y + 15f, paint)

        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#64748B")
        var termY = y + 28f
        settings.termsAndConditions.split("\n").take(4).forEach { line ->
            if (line.isNotBlank()) {
                canvas.drawText(line.trim(), 35f, termY, paint)
                termY += 14f
            }
        }

        y += 140f

        // Signatures
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(35f, y + 40f, 200f, y + 40f, paint)
        canvas.drawLine(395f, y + 40f, 560f, y + 40f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        canvas.drawText("Customer Signature", 65f, y + 54f, paint)
        canvas.drawText("Authorized Manager", 425f, y + 54f, paint)

        // Footer Bar
        paint.color = Color.parseColor("#2D6A4F")
        canvas.drawRect(0f, 825f, 595f, 842f, paint)
        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Thank you for choosing ${settings.businessName}! Wish you a great outdoor adventure.", 297f, 836f, paint)
    }

    fun sharePdfViaWhatsApp(context: Context, file: File, customerPhone: String) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard share sheet if WhatsApp not installed
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Receipt PDF"))
        }
    }
}
