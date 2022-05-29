package com.example.translation.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import com.developer.filepicker.model.DialogConfigs
import com.developer.filepicker.model.DialogProperties
import com.developer.filepicker.view.FilePickerDialog
import com.example.translation.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File


class HomeFragment : Fragment(), LifecycleObserver{

    private var _binding: FragmentHomeBinding? = null
    lateinit var webSettings: WebSettings
    lateinit var root: File
    lateinit var assetManager: AssetManager
    var text: String = ""
    private val binding get() = _binding!!
    lateinit var customProgressDialog: ProgressDialog

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

        customProgressDialog = ProgressDialog(requireContext())
        customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))


        webBtn1.setOnClickListener {
            val properties : DialogProperties = DialogProperties()
            properties.selection_mode = DialogConfigs.SINGLE_MODE
            properties.selection_type = DialogConfigs.FILE_SELECT
            properties.root = File(DialogConfigs.DEFAULT_DIR)
            properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
            properties.offset = File(DialogConfigs.DEFAULT_DIR)
            properties.extensions = arrayOf("png","jpg")
            properties.show_hidden_files = true

            val dialog = FilePickerDialog(context,properties)
            dialog.setTitle("Select a Image File")

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

                customProgressDialog.show()
                if(extensions == "docx"){
                    val intent = Intent(requireContext(),DocsActivity::class.java)
                    startActivity(intent)
                    customProgressDialog.dismiss();
                }
                else {
                    customProgressDialog.show()
                    val intent = Intent(requireContext(),TImageActivity::class.java)
                    intent.putExtra("file_dirs",file_dirs)
                    intent.putExtra("file_name",file_name)
                    intent.putExtra("file_names",file_names)
                    startActivity(intent)
                    customProgressDialog.dismiss();
                }
            }
            dialog.show()
        }

        webBtn2.setOnClickListener {

            val properties2 : DialogProperties = DialogProperties()
            properties2.selection_mode = DialogConfigs.SINGLE_MODE
            properties2.selection_type = DialogConfigs.FILE_SELECT
            properties2.root = File(DialogConfigs.DEFAULT_DIR)
            properties2.error_dir = File(DialogConfigs.DEFAULT_DIR)
            properties2.offset = File(DialogConfigs.DEFAULT_DIR)
            properties2.extensions = arrayOf("pdf")
            properties2.show_hidden_files = true

            val dialog = FilePickerDialog(context,properties2)
            dialog.setTitle("Select a PDF File")

            dialog.setDialogSelectionListener {
                val text = it.contentDeepToString()
                val tmp = text.substring(1,text.length-1)
                var file_dir : String = ""
                var file_dirs : String = ""
                var file_dirs2 : String = ""
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
                    file_dirs = file_dir.substring(4,file_dir.length)
                    index = file_dir.lastIndexOf("/")
                    file_dirs2 = file_dir.substring(0,index+1)
                }

                val intent = Intent(requireContext(),DocsActivity::class.java)
                intent.putExtra("pdf_dir",file_dirs)
                intent.putExtra("pdf_dirs",file_dirs2)
                intent.putExtra("pdf_name",file_name)
                intent.putExtra("pdf_names",file_names)
                startActivity(intent)
            }
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}