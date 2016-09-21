package eu.rapasoft.blog.controller;

import eu.rapasoft.blog.service.FileLoader;
import eu.rapasoft.blog.util.MDFileConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pavol Rajzak
 */
@RestController
@RequestMapping(value = MDFileConstants.IMAGE, method = RequestMethod.GET)
public class ImageResource {

	@Autowired
	private FileLoader fileLoader;

	@RequestMapping(value = "{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	private ResponseEntity<byte[]> getImageFile(@PathVariable String id) {
		return ResponseEntity.ok(fileLoader.loadJpgFile(id + ".jpg"));
	}

}
