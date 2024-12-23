package com.moehoemar.storyapp

import com.moehoemar.storyapp.data.remote.response.story.ListStoryItem

object DataDummy {
    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..20) {
            val story = ListStoryItem(
                id = i.toString(),
                name = "Story $i",
                description = "StoryDesc $i",
                createdAt = "2024-10-$i",
                photoUrl = "https://pbs.twimg.com/profile_images/1544955480816369664/tbdn5Cc8_400x400.jpg",
                lat = -6.23,
                lon = 106.75,
            )
            items.add(story)
        }
        return items
    }
}