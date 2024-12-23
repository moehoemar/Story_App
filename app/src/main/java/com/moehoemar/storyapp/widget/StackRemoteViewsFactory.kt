package com.moehoemar.storyapp.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.lifecycle.ViewModelProvider
import com.moehoemar.storyapp.R
import com.moehoemar.storyapp.data.di.Injection
import com.moehoemar.storyapp.data.remote.response.story.ListStoryItem
import com.moehoemar.storyapp.utils.loadWidgetImage
import kotlinx.coroutines.runBlocking

internal class StackRemoteViewsFactory(
    private val mContext: Context,
) : RemoteViewsService.RemoteViewsFactory {

    private var storiesList = mutableListOf<ListStoryItem>()
    private lateinit var userToken: String
    private lateinit var widgetViewModel: WidgetViewModel

    override fun onCreate() {
        widgetViewModel = ViewModelProvider.AndroidViewModelFactory
            .getInstance(mContext.applicationContext as android.app.Application)
            .create(WidgetViewModel::class.java)
        userToken = Injection.provideUserToken(mContext)
    }

    override fun onDataSetChanged() {
        runBlocking {
            try {
                val stories = widgetViewModel.getStories(userToken)
                storiesList.clear()
                if (!stories.isNullOrEmpty()) {
                    storiesList.addAll(stories)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        storiesList.clear()
    }

    override fun getCount(): Int = storiesList.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= storiesList.size) {
            return RemoteViews(mContext.packageName, R.layout.item_widget)
        }

        val story = storiesList[position]
        val views = RemoteViews(mContext.packageName, R.layout.item_widget).apply {
            setTextViewText(R.id.story_user_name, story.name)
            val imageBitmap = story.photoUrl?.let { loadWidgetImage(it) }
            imageBitmap?.let { setImageViewBitmap(R.id.story_image, it) }
        }

        val intent = Intent().apply {
            putExtra(StoryWidget.EXTRA_ITEM, story.id)
        }
        views.setOnClickFillInIntent(R.id.item_widget, intent)
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(i: Int): Long = i.toLong()
    override fun hasStableIds(): Boolean = true
}
