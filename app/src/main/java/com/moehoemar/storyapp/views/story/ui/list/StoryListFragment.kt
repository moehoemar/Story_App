package com.moehoemar.storyapp.views.story.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.moehoemar.storyapp.R
import com.moehoemar.storyapp.data.preferences.StoryAppPreferences
import com.moehoemar.storyapp.data.preferences.dataStore
import com.moehoemar.storyapp.databinding.FragmentListStoryBinding
import com.moehoemar.storyapp.views.story.adapter.LoadingStateAdapter
import com.moehoemar.storyapp.views.story.adapter.StoryListAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


class StoryListFragment : Fragment() {
    private var _binding: FragmentListStoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvStory: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var preferences: StoryAppPreferences
    private lateinit var userName: String
    private lateinit var listStoryAdapter: StoryListAdapter
    private lateinit var storyListViewModel: StoryListViewModel
    private lateinit var layoutManager: GridLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = StoryAppPreferences.getInstance(requireActivity().dataStore)

        setupViews()
        getData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViews() {
        userName = runBlocking { preferences.getName().first() }
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.welcome) + " " + userName

        storyListViewModel = ViewModelProvider(
            this@StoryListFragment,
            ViewModelFactory(requireContext())
        )[StoryListViewModel::class.java]
        rvStory = binding.rvStory
        swipeRefreshLayout = binding.swipeRefresh
        layoutManager = GridLayoutManager(requireContext(), 1)
        rvStory.layoutManager = layoutManager
        rvStory.setHasFixedSize(true)
        listStoryAdapter = StoryListAdapter()
        rvStory.adapter = listStoryAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                listStoryAdapter.retry()
            }
        )

        swipeRefreshLayout.setOnRefreshListener {
            listStoryAdapter.refresh()
        }
    }

    private fun getData() {
        storyListViewModel.stories.observe(viewLifecycleOwner) {
            listStoryAdapter.submitData(lifecycle, it)
        }

        listStoryAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner) { loadState ->
            swipeRefreshLayout.isRefreshing = loadState.refresh is LoadState.Loading
            if (loadState.refresh is LoadState.NotLoading &&
                loadState.append.endOfPaginationReached &&
                listStoryAdapter.itemCount < 1) {
                scrollToTop()
            }
        }
    }

    private fun scrollToTop() {
        rvStory.post {
            rvStory.smoothScrollToPosition(0)
        }
    }
}
