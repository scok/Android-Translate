package com.example.translation.ui.webtranslate

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.example.translation.MainActivity
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.*
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder

class ApiTranslateNmt(translationText: String) : AsyncTask<String, Void, String>() {
    var translationtext = translationText
    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: String?): String {
        val clientId : String = "ucbpb641nm"
        val clientSecret : String = "cSoECdb0oHi3fOuuLm5MCvGc3UBJVctC9Sfdw0vS"
        val apiURL = "https://naveropenapi.apigw.ntruss.com/nmt/v1/translation"

        val text: String = try{
            URLEncoder.encode(translationtext, "UTF-8")
        }catch (e: UnsupportedEncodingException){
            throw RuntimeException("인코딩 실패",e)
        }

        val requestHeaders : MutableMap<String, String> = HashMap()
        requestHeaders["X-NCP-APIGW-API-KEY-ID"] = clientId
        requestHeaders["X-NCP-APIGW-API-KEY"] = clientSecret

        fun connect(apiUrl: String): HttpURLConnection {
            return try {
                val url = URL(apiUrl)
                url.openConnection() as HttpURLConnection
            } catch (e: MalformedURLException) {
                throw RuntimeException("API URL이 잘못되었습니다. : $apiUrl", e)
            } catch (e: IOException) {
                throw RuntimeException("연결이 실패했습니다. : $apiUrl", e)
            }
        }

        fun readBody(body: InputStream): String {
            val streamReader = InputStreamReader(body)
            try {
                BufferedReader(streamReader).use { lineReader ->
                    val responseBody = StringBuilder()
                    var line: String?
                    while (lineReader.readLine().also { line = it } != null) {
                        responseBody.append(line)
                    }
                    return responseBody.toString()
                }
            } catch (e: IOException) {
                throw RuntimeException("API 응답을 읽는데 실패했습니다.", e)
            }
        }

        fun post(apiUrl: String, requestHeaders: Map<String, String>, text: String): String {
            val con: HttpURLConnection = connect(apiUrl)
            val postParams =
                "source=$originalLanguage&target=$targetLanguage&text=${text}" //원본언어: 한국어 (ko) -> 목적언어: 영어 (en)
            try {
                con.requestMethod = "POST"

                for(header : Map.Entry<String, String> in requestHeaders.entries){
                    con.setRequestProperty(header.key, header.value)
                }
                con.doOutput = true
                DataOutputStream(con.outputStream).use { wr ->
                    wr.write(postParams.toByteArray())
                    wr.flush()
                }
                val responseCode = con.responseCode
                return if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 응답
                    readBody(con.inputStream)
                } else {  // 에러 응답
                    readBody(con.errorStream)
                }
            } catch (e: IOException) {
                throw RuntimeException("API 요청과 응답 실패", e)
            } finally {
                con.disconnect()
            }
        }

        val responseBody: String = post(apiURL, requestHeaders, text)
        println(responseBody)

        val parser : JsonParser = JsonParser()
        val element : JsonElement = parser.parse(responseBody)
        var data : String = ""

        if(element.asJsonObject.get("errorMessage") != null){
            Log.e("번역 오류","번역 오류가 발생했습니다." + "[오류 코드: "+element.asJsonObject.get("errorCode").asString + "]")
            data = "A: 오류입니다."
        }else if(element.asJsonObject.get("message") != null){
            data = element.asJsonObject.get("message").asJsonObject.get("result")
                .asJsonObject.get("translatedText").asString
            Log.e("번역 성공", "번역되었습니다. $data")
        }

        return data
    }
}