package eu.rapasoft.blog.model

data class BlogEntry(
        val title: String,
        val content: String,
        val categories: List<String>,
        val published: String,
        val fileName: String
) {

    fun perexifyContent(blogEntry: BlogEntry): BlogEntry {
        return BlogEntry(
                blogEntry.title,
                blogEntry.content.substring(0, blogEntry.content.indexOf("\n")).replace("\\(.*\\)".toRegex(), "").replace("\\]|\\[".toRegex(), ""),
                blogEntry.categories,
                blogEntry.published,
                blogEntry.fileName
        )
    }

}