package eu.rapasoft.blog.model

data class BlogEntry(
    val title: String,
    val content: String,
    val categories: List<String>,
    val published: String,
    val fileName: String
) {

  fun perexifyContent(): String =
      this.content
          .substring(0, this.content.indexOf("\n"))
          .replace("\\(.*\\)".toRegex(), "")
          .replace("\\]|\\[".toRegex(), "")

}