var className = 'post';

$.getJSON("md-file", function (blogEntries) {
	blogEntries.forEach(function (entry) {
		var blogEntry = document.createElement('section');
		var categories = '';

		entry.categories.forEach(function(category) {
			categories += '<a class="post-category">' + category + '</a>';
		});

		$(blogEntry).addClass(className)
			.html
		(
			'<header class="post-header">\n' +
			'<img class="post-avatar" width="100px" height="100px" src="image/' + entry.fileName + '" alt="' + entry.fileName + '" />\n' +
			'<h2 class="post-title"><a href="' + entry.fileName + '">' + entry.title + '</a></h2>' +
			'<p class="post-meta">' +
			'At <a class="post-author"> ' + entry.published + '</a> \n' +
			' under ' + categories +
			'</p>' +
			'</header>' +
			'<div class="post-description"><p>' + entry.content + '</p></div>\n'
		)
			.appendTo($(".posts"));
	})
});