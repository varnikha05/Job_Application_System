(function ($) {
  function toggleModal($modal, show) {
    if (!$modal.length) {
      return;
    }
    if (show) {
      $modal.removeClass('hidden');
      $modal.attr('aria-hidden', 'false');
      $('body').addClass('modal-open');
    } else {
      $modal.addClass('hidden');
      $modal.attr('aria-hidden', 'true');
      $('body').removeClass('modal-open');
    }
  }

  $.fn.simpleModal = function (action) {
    return this.each(function () {
      const $modal = $(this);
      if (action === 'show') {
        toggleModal($modal, true);
      } else if (action === 'hide') {
        toggleModal($modal, false);
      }
    });
  };

  $(document).on('click', '[data-modal-target]', function (event) {
    event.preventDefault();
    const target = $(this).data('modal-target');
    if (!target) {
      return;
    }
    const $modal = $(target.startsWith('#') ? target : '#' + target);
    toggleModal($modal, true);
  });

  $(document).on('click', '[data-modal-close]', function (event) {
    event.preventDefault();
    const target = $(this).data('modalClose');
    if (target) {
      const $modal = $(target.startsWith('#') ? target : '#' + target);
      toggleModal($modal, false);
    } else {
      toggleModal($(this).closest('.modal-overlay'), false);
    }
  });

  $(document).on('click', '.modal-overlay', function (event) {
    if ($(event.target).is('.modal-overlay')) {
      toggleModal($(this), false);
    }
  });
})(jQuery);
