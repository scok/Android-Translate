package com.example.translation.ui.home

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.translation.R
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import kotlinx.android.synthetic.main.activity_docs.*
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.lang.Runnable


class DocsActivity : AppCompatActivity() {

    private var mDownloadManager: DownloadManager? = null
    var mDownloadQueueId : Long? = null

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs)

        val target_Language = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("image_targetLanguage", "").toString()

        var list = ArrayList<Bitmap>()
        val intent2 = intent
        val pdf_url = intent2.getStringExtra("pdf_url")
        val pdf_dir = intent2.getStringExtra("pdf_dir")
        //val pdf_dirs = intent2.getStringExtra("pdf_dirs")
        val pdf_name = intent2.getStringExtra("pdf_name")
        val pdf_names = intent2.getStringExtra("pdf_names")

        val outputFilepath2 : String = "${externalCacheDir}/$pdf_name"
        val uri = Uri.parse(pdf_url)
        URLDownloading(uri,outputFilepath2)

        val progressDialog : ProgressDialog = ProgressDialog(this)
        progressDialog.setMessage("PDF 구성중...")
        progressDialog.setCancelable(true)
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
        progressDialog.show()

        val handler : Handler = Handler()
        handler.postDelayed(Runnable {
            // PDF -> Bitmap
            val images: MutableList<Bitmap> = renderToBitmap(applicationContext,pdf_dir)
            Log.d("Test-Sample","images : $images")

            list = images as ArrayList<Bitmap>

            Log.d("Test-Sample", images.indices.toString())
            // Bitmap 번역
            for(i in 0 until images.size){
                val j = i+1
                if (pdf_names != null) {
                    saveBitmapToJpeg(images[i],"$pdf_names-$j")
                }

                Log.d("Test-Sample","i : $i")
                Log.d("Test-Sample","j : $j")

            }
        },1000)

        handler.postDelayed(Runnable {

            GlobalScope.launch {
                launch {
                    print("Translate Start")
                }

                val resultlist : Deferred<ArrayList<Bitmap>> = async {
                    val list2 = ArrayList<Bitmap>()
                    for (i in 0 until list.size){
                        val j = i + 1

                        Log.d("Test-Sample","i : $i")
                        Log.d("Test-Sample","j : $j")

                        val clientBuilder : OkHttpClient.Builder = OkHttpClient.Builder()
                        val loggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor()
                        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                        clientBuilder.addInterceptor(loggingInterceptor)

                        val client : Retrofit = Retrofit.Builder()
                            .baseUrl("https://naveropenapi.apigw.ntruss.com")
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(clientBuilder.build())
                            .build()

                        val partMap = HashMap<String, RequestBody>()
                        val trsource = RequestBody.create(MediaType.parse("text/plain"),target_Language)
                        val trtarget = RequestBody.create(MediaType.parse("text/plain"),"ko")
                        partMap["source"] = trsource
                        partMap["target"] = trtarget

                        val file = File("$cacheDir/$pdf_names-$j.jpg")
                        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"),file)
                        val body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestFile)

                        val service = client.create(PapagoService::class.java)
                        val res : Call<PapagoEntity> = service.transferPapago("w5lgfrssck","tct9yx0oteeuixAnAdIOETTtKiZFhixSLzNw3vvM",body,partMap)

                        val imageStr = res.execute().body()?.data?.renderedImage.toString()
                        val bytePlainOrg = Base64.decode(imageStr,0)
                        val inStream = ByteArrayInputStream(bytePlainOrg)
                        val bm : Bitmap = BitmapFactory.decodeStream(inStream)

                        list2.add(bm)

                        Log.d("Test-Sample","list2 : $list2")

                        if(i==(list.size-1)){

                            // 번역한 Bitmap -> PDF 변환
                            val document = android.graphics.pdf.PdfDocument()
                            for(k in 0 until list2.size){
                                val pageinfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(list2[k].width,list2[k].height,k+1).create()
                                val page = document.startPage(pageinfo)
                                val canvas = page.canvas
                                val paint = Paint()
                                paint.color = Color.parseColor("#ffffff")
                                canvas.drawPaint(paint)
                                paint.color = Color.BLUE
                                canvas.drawBitmap(list2[k], Rect(0,0,list2[k].width,list2[k].height), Rect(0,0,list2[k].width,list2[k].height),null)
                                document.finishPage(page)
                            }

                            // 파일 저장
                            val savePath = "$cacheDir/$pdf_names-translate.pdf"
                            val filePath = File(savePath)

                            try {
                                document.writeTo(FileOutputStream(filePath))
                                document.close()
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
                        }
                    }
                    list2
                }

            }
        },6000)

    }

    private fun URLDownloading(url: Uri, outputFilepath: String){
        if(mDownloadManager == null){
            mDownloadManager = (applicationContext.getSystemService(Context.DOWNLOAD_SERVICE)) as DownloadManager
        }
        val outputFile : File = File(outputFilepath)
        if(!outputFile.parentFile.exists()){
            outputFile.parentFile.mkdirs()
        }

        val downloadUri : Uri = url
        val request : DownloadManager.Request = DownloadManager.Request(downloadUri)
        var pathSegmentList : List<String> = downloadUri.pathSegments
        request.setTitle("이미지 다운로드")
        request.setDestinationUri(Uri.fromFile(outputFile))
        request.setAllowedOverMetered(true)

        mDownloadQueueId = mDownloadManager!!.enqueue(request)
    }

    // PDF Bitmap 변환 함수
    private fun renderToBitmap(context: Context?, filePath: String?): MutableList<Bitmap> {
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
        val fileName = "$name.jpg"

        //storage 에 파일 인스턴스를 생성합니다.
        val tempFile = File(storage, fileName)
        try {

            // 자동으로 빈 파일을 생성합니다.
            tempFile.createNewFile()

            // 파일을 쓸 수 있는 스트림을 준비합니다.
            val out = FileOutputStream(tempFile)

            // compress 함수를 사용해 스트림에 비트맵을 저장합니다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)

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

    private fun setDirEmpty(dirName : String){

        val path = Environment.getExternalStorageDirectory().toString() + "/pdf_temp/"

        val dir = File(path)
        val childFileList = dir.listFiles()

        if(dir.exists()){
            for (childFile in childFileList) {
                if(childFile.isDirectory){
                    setDirEmpty(childFile.absolutePath)
                }else{
                    childFile.delete()
                }
            }
            dir.delete()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearCache()
    }
}