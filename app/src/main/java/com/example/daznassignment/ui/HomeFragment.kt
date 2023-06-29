package com.example.daznassignment.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.daznassignment.R
import com.example.daznassignment.adapters.VideoListAdapter
import com.example.daznassignment.data.VideoDataItem
import com.example.daznassignment.databinding.FragmentHomeBinding
import com.example.daznassignment.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"
    private var _binding:FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VideoListAdapter

    private val viewModel by viewModels<VideoViewModel>()



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
    private fun onVideoClicked(video: VideoDataItem){
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