$(document).ready(function() {
	var solrURL,
		nextStart = 0,
		visibleDocs = [];

	/*
		Init options tooltip and options event listener
	*/
	$('#options-toggler').tooltip();

	$(document).mouseup(function(e) {
		var optionsToggler = $('#options-toggler'),
			options = $('#options');

		if(optionsToggler.is(e.target) && options.is(':hidden')) {
			options.show();
		} else if(!options.is(e.target) && (options.has(e.target).length == 0)) {
			options.hide();
		}
	});



	/*
		Initialize options sliders
	*/
	$('.slider').slider();


	
	/*
		Declares event listeners
	*/
	$('#search-btn').on('click', function() {
		searchSolr();
	});
	$('#input-search-string').keypress(function(e) {
		var keyCode = (e.keyCode ? e.keyCode : e.which);

		if(keyCode == '13') {
			searchSolr();
		}
	});



	/*
		Handler for images
	*/
	$(document).on('click', '.image-container', function() {
		var parentRow = $(this).parent(),
			id = $(this).attr('id').split('-')[1],
			doc = visibleDocs[id];

		if($('#info-' + id).length != 0) {
			$('#info-' + id).hide(1000, function() {
				$(this).remove();
			});
			return;
		}

		var infoRow = $('<div class="row info-row well" id="info-' + id + '" style="display: none;"></div>'),
			imageColumn = $('<div class="col-md-6 img-column"><img src="http:' + doc.url + '" /></div>'),
			infoColumn = $('<div class="col-md-6 info-column"></div>');

		infoColumn.append('<h2><b>Image Information</b></h2>');
		infoColumn.append('<p><b>URL</b>: ' + doc.url + '</p>');
		infoColumn.append('<p><b>Filename</b>: ' + doc.filename + '</p>');
		infoColumn.append('<p><b>Alt-tag</b>: ' + doc.alt + '</p>');
		infoColumn.append('<p><b>Description</b>: ' + doc.description + '</p>');
		infoColumn.append('<p><b>Context</b>: ' + doc.context + '</p>');
		infoColumn.append('<p><b>Title</b>: ' + doc.title + '</p>');
		infoColumn.append('<p><b>Subtitle</b>: ' + doc.subtitle + '</p>');
		infoColumn.append('<p><b>Score</b>: ' + doc.score + '</p>');

		infoRow.append(imageColumn);
		infoRow.append(infoColumn);
		parentRow.after(infoRow);
		infoRow.show(1000);
		$('html, body').animate({
			scrollTop: infoRow.offset().top - 400
		}, 500);
	});
	$(document).on('click', '.info-row', function() {
		$(this).hide(1000, function() {
			$(this).remove();
		});
	});



	/*
		Paging
	*/
	$('#extends-results-link').on('click', function() {
		var scrollTo = $(this).offset().top;
		searchSolr(true);

		$('html, body').animate({
			scrollTop: scrollTo
		}, 500);

		return false;
	});
	
	/*
		Sends a search request to Solr
	*/
	function searchSolr(reuseSolrUrl) {
		if(!reuseSolrUrl) {
			var searchString = $('#input-search-string').val(),
				urlWeight = $('#url-weight').slider('getValue').val(),
				filenameWeight = $('#filename-weight').slider('getValue').val(),
				altWeight = $('#alt-weight').slider('getValue').val(),
				descriptionWeight = $('#description-weight').slider('getValue').val(),
				contextWeight = $('#context-weight').slider('getValue').val(),
				titleWeight = $('#title-weight').slider('getValue').val(),
				subtitleWeight = $('#subtitle-weight').slider('getValue').val();
			
			var weightsString = '&defType=edismax&qf=url^' + urlWeight + ' filename^' + filenameWeight + ' alt^' + altWeight + ' description^' + descriptionWeight + ' context^' + contextWeight 
							+ ' title^' + titleWeight + ' subtitle^' + subtitleWeight;

			var rows = 10;

			solrURL = 'http://localhost:8983/solr/images/select?q=' + searchString + weightsString + ' &fl=*,score&sort_desc=score&rows=' + rows + '&wt=json';
			nextStart = 0;

			$('#results').empty();
		}

		$.ajax({
			url: solrURL + '&start=' + nextStart,
			crossDomain: true,
			dataType: 'jsonp',
			jsonp: 'json.wrf'
		}).success(function(data) {
			$('#feedback').hide();

			var response = data.response;
			$('#nr-results').text(response.numFound);

			var results = $('#results'),
				row;

			if(results.children().length == 0) {
				row = $('<div class="row" style="margin-top: 20px;"></div>');
				results.append(row);
			} else {
				row = results.children().last()
			}

			for(var i = 0; i < response.docs.length; i++) {
				if(row.children().length == 6) {
					row = $('<div class="row" style="margin-top: 20px;"></div>');
					results.append(row);
				}

				var imgContainer = $('<div class="col-md-2 image-container" id="doc-' + (visibleDocs.length + i) + '"><img src="http:' + response.docs[i].url + '" /></div>');
				row.append(imgContainer);
			}

			visibleDocs = visibleDocs.concat(response.docs);
			console.log(visibleDocs);

			if(response.numFound > (nextStart + 10)) {
				$('#extend-results-p').show();
				nextStart += 10;
			} else {
				$('#extend-results-p').hide();
			}

			$('#results-container').show();
		}).fail(function(xhr, textStatus, error) {
			$('#results-container').hide();
			$('#feedback').html('Ooops! Something went terribly <a href="http://dictionary.reference.com/browse/wrong" target="_blank">wrong</a>...');
			$('#feedback').show();

			console.log('Solr request failed.');
			console.log('URL: ' + $(this).attr('url'));
			console.log('Status: ' + xhr.status + ' ' + textStatus);
		});
	}
});