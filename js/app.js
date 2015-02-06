jQuery(function($){
      $('a.hold').click(function(e) {
				e.preventDefault();
        $('.modal img').attr('src', 'img/screens/' + $(this).attr('data') + '.png');
				$('.modal').modal().open();
        console.debug();
			});

			// attach modal close handler
			$('.modal .close').on('click', function(e){
				e.preventDefault();
				$.modal().close();
			});
});