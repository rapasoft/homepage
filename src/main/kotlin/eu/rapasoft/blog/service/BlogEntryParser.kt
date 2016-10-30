package eu.rapasoft.blog.service

import eu.rapasoft.blog.model.BlogEntry
import java.util.*

/**
 * Created by Lenovo on 26. 10. 2016.
 */
class BlogEntryParser {

    companion object {
        val TITLE_LINE = 0;
        val AUTHOR_LINE = 1;
        val PUBLISH_DATE_LINE = 3;
        val CATEGORY_LINE = 4;
        val CONTENT_START_LINE = 7;
        val instance: BlogEntryParser = BlogEntryParser();
    }

    fun parse(lines: List<String>, fileName: String): BlogEntry {
        return BlogEntry(
                getLine(lines, TITLE_LINE),
                fetchContent(lines, CONTENT_START_LINE, lines.size),
                Arrays.asList(*getLine(lines, CATEGORY_LINE).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()),
                getLine(lines, PUBLISH_DATE_LINE),
                fileName.substring(0, fileName.indexOf(".")))
    }

    private fun getLine(lines: List<String>, position: Int): String {
        return lines[position].replace("^[\\-|#]\\s*".toRegex(), "").trim { it <= ' ' }
    }

    private fun fetchContent(lines: List<String>, contentStartLine: Int, size: Int): String {
        val stringBuilder = StringBuilder()
        for (i in contentStartLine..size - 1) {
            stringBuilder.append(lines[i]).append("\n")
        }
        return stringBuilder.toString()
    }

}