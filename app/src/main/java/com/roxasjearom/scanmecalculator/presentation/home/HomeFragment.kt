package com.roxasjearom.scanmecalculator.presentation.home

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.roxasjearom.scanmecalculator.BuildConfig
import com.roxasjearom.scanmecalculator.R
import com.roxasjearom.scanmecalculator.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private var imageUri: Uri? = null

    @Inject
    lateinit var recognizer: TextRecognizer

    private val takeImageFromCameraResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUri?.let { imageUri ->
                    createInputImage(imageUri)?.let { inputImage ->
                        getTextResult(inputImage)
                    }
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_capture_image_cancelled), Toast.LENGTH_SHORT).show()
            }
        }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri ->
            createInputImage(imageUri)?.let { inputImage ->
                getTextResult(inputImage)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addInputButton.text = getButtonLabel()
        binding.addInputButton.setOnClickListener {
            val imageFile = createImageFile()
            imageUri = getImageUri(imageFile)

            if (BuildConfig.FLAVOR_input == "camera") {
                takeImageFromCameraResult.launch(imageUri)
            } else {
                selectImageFromGalleryResult.launch("image/*")
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeUiState.collect { uiState ->
                    binding.inputTextView.text = uiState.input
                    binding.resultTextView.text = uiState.result
                }
            }
        }
    }

    private fun getTextResult(inputImage: InputImage) {
        recognizer.process(inputImage)
            .addOnSuccessListener { result ->
                if(result.textBlocks.isEmpty()) {
                    showNoResultFound()
                    return@addOnSuccessListener
                }
                for (block in result.textBlocks) {
                    val textLines = block.lines.map { it.text }
                    viewModel.validateLines(textLines)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                showNoResultFound()
            }
    }

    private fun showNoResultFound() {
        binding.inputTextView.text = getString(R.string.message_no_valid_input_found)
        binding.resultTextView.text = getString(R.string.message_no_result_to_show)
    }

    private fun getButtonLabel(): String {
        return if (BuildConfig.FLAVOR_input == "camera") {
            getString(R.string.label_take_picture)
        } else {
            getString(R.string.label_select_image)
        }
    }

    private fun createInputImage(fileUri: Uri): InputImage? {
        return try {
            InputImage.fromFilePath(requireContext(), fileUri)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}",
            ".jpg",
            storageDir
        )
    }

    private fun getImageUri(file: File): Uri {
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            file
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
