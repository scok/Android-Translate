package com.example.translation.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import com.developer.filepicker.model.DialogConfigs
import com.developer.filepicker.model.DialogProperties
import com.developer.filepicker.view.FilePickerDialog
import com.example.translation.databinding.FragmentHomeBinding
import com.shockwave.pdfium.PdfiumCore
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File


class HomeFragment : Fragment(), LifecycleObserver{

    private var _binding: FragmentHomeBinding? = null
    lateinit var webSettings: WebSettings
    lateinit var root: File
    lateinit var assetManager: AssetManager
    var text: String = ""
    private val binding get() = _binding!!

    @SuppressLint("SdCardPath", "SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("SetJavaScriptEnabled", "SdCardPath")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        webBtn1.setOnClickListener {
            val properties : DialogProperties = DialogProperties()
            properties.selection_mode = DialogConfigs.SINGLE_MODE
            properties.selection_type = DialogConfigs.FILE_SELECT
            properties.root = File(DialogConfigs.DEFAULT_DIR)
            properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
            properties.offset = File(DialogConfigs.DEFAULT_DIR)
            properties.extensions = null
            properties.show_hidden_files = true

            val dialog = FilePickerDialog(context,properties)
            dialog.setTitle("Select a File")

            dialog.setDialogSelectionListener {
                val text = it.contentDeepToString()
                val tmp = text.substring(1,text.length-1)
                var file_dir : String = ""
                var file_dirs : String = ""
                var file_name : String = ""
                var file_names : String = ""
                var extensions : String = ""
                var index : Int = 0
                var index2 : Int = 0

                for (path in it) {
                    val file = File(path)
                    file_name = file.name
                    index2 = file_name.lastIndexOf(".")
                    file_names = file_name.substring(0,index2)
                    extensions = file_name.substring(index2+1,file_name.length)

                    file_dir = file.absolutePath
                    index = file_dir.lastIndexOf("/")
                    file_dirs = file_dir.substring(0,index+1)
                }

                if(extensions == "docx"){
                    val intent = Intent(requireContext(),DocsActivity::class.java)
                    startActivity(intent)
                }
                else {
                    val intent = Intent(requireContext(),TImageActivity::class.java)
                    intent.putExtra("file_dirs",file_dirs)
                    intent.putExtra("file_name",file_name)
                    intent.putExtra("file_names",file_names)
                    startActivity(intent)
                }

                /*
                val intent = Intent(requireContext(),TImageActivity::class.java)
                intent.putExtra("file_dir",file_dirs)
                intent.putExtra("file_name",file_name)
                intent.putExtra("file_names",file_names)
                startActivity(intent)

                 */

                /*

                val inputPdf = File(tmp)
                val outputHTML : File = pdf2htmlEX((activity as MainActivity).applicationContext).setInputPDF(inputPdf).convert()
                Toast.makeText(requireContext(),"$outputHTML",Toast.LENGTH_SHORT).show()

                val intent = Intent(requireContext(),PdfActivity::class.java)
                intent.putExtra("PdfFile",tmp)
                intent.putExtra("File",outputHTML.toString())
                startActivity(intent)

                 */

            }

            dialog.show()
        }

        webBtn2.setOnClickListener{
            val intent = Intent(requireContext(),TImageActivity::class.java)
            //intent.putExtra("PdfFile",tmp)
            //intent.putExtra("File",outputHTML.toString())
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}