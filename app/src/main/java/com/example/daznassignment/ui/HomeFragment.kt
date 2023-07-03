package com.example.daznassignment.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.daznassignment.R
import com.example.daznassignment.adapters.VideoListAdapter
import com.example.daznassignment.data.VideoDataItem
import com.example.daznassignment.databinding.FragmentHomeBinding
import com.example.daznassignment.utils.FRAGMENT_RESULT_DATA_KEY
import com.example.daznassignment.utils.FRAGMENT_RESULT_REQUEST_KEY
import com.example.daznassignment.utils.Resource
import com.example.daznassignment.utils.TAG_HOMEFRAGMENT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val TAG = TAG_HOMEFRAGMENT
    private var _binding:FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VideoListAdapter

    private val viewModel by activityViewModels<VideoViewModel>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater)
        adapter = VideoListAdapter(::onVideoClicked)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: viewmodel $viewModel")
        viewModel.videos.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Success -> {
                    adapter.submitList(it.data)
                    setupVideoRV()
                }
                is Resource.Error -> {
                    Log.d(TAG, "onViewCreated: ${it.message}")
                    Snackbar.make(requireView(),it.message!!,Snackbar.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    Snackbar.make(requireView(),"Loading",Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun onVideoClicked(selectedVideo: VideoDataItem, position:Int){
        setFragmentResult(FRAGMENT_RESULT_REQUEST_KEY, bundleOf(FRAGMENT_RESULT_DATA_KEY to position))
        findNavController().navigate(R.id.action_homeFragment_to_playbackFragment)
    }

    private fun setupVideoRV(){
        binding.videoRv.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}