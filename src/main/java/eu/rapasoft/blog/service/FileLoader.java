package eu.rapasoft.blog.service;

import eu.rapasoft.blog.model.BlogEntry;
import eu.rapasoft.blog.util.ApplicationProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Pavol Rajzak
 */
@Slf4j
@Service
public class FileLoader {

	@Autowired
	private BlogEntryParser blogEntryParser;

	@SneakyThrows
	private Map<String, File> availableFiles() {
		File directory = Paths.get(ApplicationProperties.INSTANCE.getMDFilesPath().toURI()).toFile();

		if (!directory.isDirectory()) {
			log.error("Directory {} does not exist or is not an directory.", directory.getAbsolutePath());
			throw new RuntimeException("Wrong directory path");
		}

		FilenameFilter filter = (dir, name) -> name.endsWith(".md") || name.endsWith(".jpeg") || name.endsWith(".jpg");

		return Arrays.asList(directory.listFiles(filter))
				.parallelStream()
				.collect(Collectors.toMap(File::getName, Function.<File>identity()));
	}

	@SneakyThrows
	public byte[] loadJpgFile(final String requestedFile) {
		File jpg = getFile(requestedFile);
		return Files.readAllBytes(jpg.toPath());
	}

	public BlogEntry loadMdFile(final String requestedFile) {
		return blogEntryParser.parse(getFile(requestedFile));
	}

	public List<BlogEntry> listMdFiles() {
		return availableFiles().values()
				.parallelStream()
				.filter(file -> file.getName().endsWith(".md"))
				.map(blogEntryParser::parse)
				.sorted((o1, o2) -> o2.getPublished().compareTo(o1.getPublished()))
				.map(BlogEntry::perexifyContent)
				.collect(Collectors.toList());
	}

	private File getFile(String requestedFile) {
		return availableFiles().values()
				.parallelStream()
				.filter(file -> file.getName().contains(requestedFile))
				.findFirst()
				.get();
	}

}
