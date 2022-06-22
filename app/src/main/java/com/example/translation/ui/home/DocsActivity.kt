package com.example.translation.ui.home

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.util.Base64
import android.util.Log
import android.webkit.WebSettings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.translation.R
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import kotlinx.android.synthetic.main.activity_docs.*
import java.io.*


class DocsActivity : AppCompatActivity() {
    private lateinit var webSettings: WebSettings
    companion object{
        private const val PDF_SELECTION_CODE = 99
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs)

        val progressDialog : ProgressDialog = ProgressDialog(this)
        progressDialog.setMessage("PDF 구성중...")
        progressDialog.setCancelable(true)
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
        progressDialog.show()

        val handler : Handler = Handler()
        handler.postDelayed(Runnable {
            val intent2 = intent
            val pdf_dir = intent2.getStringExtra("pdf_dir")
            val pdf_dirs = intent2.getStringExtra("pdf_dirs")
            val pdf_name = intent2.getStringExtra("pdf_name")
            val pdf_names = intent2.getStringExtra("pdf_names")

            // PDF -> Bitmap
            val images: List<Bitmap>? = renderToBitmap(applicationContext,pdf_dir)

            // Bitmap 번역
            val bitArray = Array<Bitmap>(images!!.size) { images[0] }
            for(i in images.indices){
                val j = i+1
                if (pdf_names != null) {
                    saveBitmapToJpeg(images[i],"$pdf_names-$j")
                }

                if(!Python.isStarted()){
                    Python.start(AndroidPlatform(this))
                }

                val target_Language = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("image_targetLanguage", "").toString()

                val py : Python = Python.getInstance()
                val pyo : PyObject = py.getModule("test")
                val imageStr = pyo.callAttr("translate",target_Language, "$cacheDir/","$pdf_names-$j.png","").toString()

                val bytePlainOrg = Base64.decode(imageStr,0)
                val inStream : ByteArrayInputStream = ByteArrayInputStream(bytePlainOrg)
                val bm : Bitmap = BitmapFactory.decodeStream(inStream)

                bitArray[i] = bm
            }

            // 번역한 Bitmap -> PDF 변환
            val document = android.graphics.pdf.PdfDocument()
            for(i in bitArray.indices){
                val pageinfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(bitArray[i].width,bitArray[i].height,i+1).create()
                val page = document.startPage(pageinfo)
                val canvas = page.canvas
                val paint = Paint()
                paint.color = Color.parseColor("#ffffff")
                canvas.drawPaint(paint)
                paint.color = Color.BLUE
                canvas.drawBitmap(bitArray[i], Rect(0,0,bitArray[i].width,bitArray[i].height), Rect(0,0,bitArray[i].width,bitArray[i].height),null)
                document.finishPage(page)
            }


            // 파일 저장
            val savePath = "$cacheDir/$pdf_names-translate.pdf"
            val filePath = File(savePath)

            try {
                document.writeTo(FileOutputStream(filePath))
                document.close()
                Toast.makeText(applicationContext,"파일 저장 완료",Toast.LENGTH_SHORT).show()
            }catch (e: FileNotFoundException){
                e.printStackTrace()
            }

            // PDF 표시
            pdfView.fromFile(File("$cacheDir/$pdf_names-translate.pdf"))
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                .spacing(0)
                .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
                .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
                .fitEachPage(true) // fit each page to the view, else smaller pages are scaled relative to largest page.
                .pageSnap(false) // snap pages to screen boundaries
                .pageFling(false) // make a fling change only a single page like ViewPager
                .nightMode(false) // toggle night mode
                .load();
            progressDialog.dismiss()
        },2000)
    }

    // PDF Bitmap 변환 함수
    fun renderToBitmap(context: Context?, filePath: String?): List<Bitmap>? {
        val images: MutableList<Bitmap> = ArrayList()
        val pdfiumCore = PdfiumCore(context)
        try {
            val f = File(filePath)
            val fd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfDocument: PdfDocument = pdfiumCore.newDocument(fd)
            val pageCount = pdfiumCore.getPageCount(pdfDocument)
            for (i in 0 until pageCount) {
                pdfiumCore.openPage(pdfDocument, i)
                val width = 1960
                val height = 1960
                Log.d("Value-Test","$width\n$height")
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                pdfiumCore.renderPageBitmap(pdfDocument, bmp, i, 0, 0, width, height)
                images.add(bmp)
            }
            pdfiumCore.closeDocument(pdfDocument)
        } catch (e: Exception) {
            //todo with exception
        }
        return images
    }

    // Bitmap을 jpg로 변환
    private fun saveBitmapToJpeg(bitmap: Bitmap, name: String) {

        //내부저장소 캐시 경로를 받아옵니다.
        val storage = cacheDir

        //저장할 파일 이름
        val fileName = "$name.png"

        //storage 에 파일 인스턴스를 생성합니다.
        val tempFile = File(storage, fileName)
        try {

            // 자동으로 빈 파일을 생성합니다.
            tempFile.createNewFile()

            // 파일을 쓸 수 있는 스트림을 준비합니다.
            val out = FileOutputStream(tempFile)

            // compress 함수를 사용해 스트림에 비트맵을 저장합니다.
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)

            // 스트림 사용후 닫아줍니다.
            out.close()
        } catch (e: FileNotFoundException) {
            Log.e("MyTag", "FileNotFoundException : " + e.message)
        } catch (e: IOException) {
            Log.e("MyTag", "IOException : " + e.message)
        }
    }

    // 캐시 삭제
    private fun clearCache(){
        val cacheDirFile : File = this.cacheDir
        if(cacheDirFile.isDirectory){
            clearSubCacheFiles(cacheDirFile)
        }
    }

    private fun clearSubCacheFiles(cacheDirFile : File){
        if(cacheDirFile.isFile){
            return
        }
        for (cacheFile in cacheDirFile.listFiles()!!) {
            if (cacheFile.isFile) {
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
            } else {
                clearSubCacheFiles(cacheFile)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearCache()
    }
}