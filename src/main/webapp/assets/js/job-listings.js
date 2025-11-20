(function ($) {
  const jobsEndpoint = 'PublicJobsServlet';
  const applicationsEndpoint = 'UserApplicationsServlet';
  const applyEndpoint = 'ApplyJobServlet';

  const $jobsContainer = $('#jobsContainer');
  const $emptyState = $('#jobsEmptyState');
  const $applyModal = $('#applyModal');
  const $applyForm = $('#applyForm');
  const $applyJobTitle = $('#applyJobTitle');
  const $applyJobCompany = $('#applyJobCompany');
  const $applyJobLocation = $('#applyJobLocation');
  const $feedbackBox = $('#feedbackBox');
  const $applicationsList = $('#applicationsList');

  const applicationsByJobId = new Map();
  let currentJobs = [];

  $(document).ready(function () {
    loadJobs();
    loadApplications();
    wireUpEvents();
  });

  function wireUpEvents() {
    $jobsContainer.on('click', '[data-apply]', function () {
      const jobId = Number($(this).data('apply'));
      const job = currentJobs.find(j => j.jobId === jobId);
      if (!job) {
        showFeedback('We could not load that job. Please refresh and try again.', 'error');
        return;
      }
      $applyForm[0].reset();
      $applyForm.find('input[name="jobId"]').val(job.jobId);
      $applyJobTitle.text(job.title || 'Job Opportunity');
      $applyJobCompany.text(job.company || 'Company Confidential');
      $applyJobLocation.text(job.location || 'Location not specified');
      $applyModal.simpleModal('show');
    });

    $applyForm.on('submit', function (event) {
      event.preventDefault();
      submitApplication();
    });

    $('#refreshJobsButton').on('click', function () {
      loadJobs(true);
    });

    $('#refreshApplicationsButton').on('click', function () {
      loadApplications(true);
    });
  }

  function loadJobs(showLoader) {
    if (showLoader) {
      $jobsContainer.addClass('loading');
    }
    $.getJSON(jobsEndpoint)
      .done(function (payload) {
        const jobs = Array.isArray(payload.jobs) ? payload.jobs : [];
        currentJobs = jobs;
        renderJobs(jobs);
      })
      .fail(function () {
        showFeedback('Unable to load job listings right now. Please try again later.', 'error');
      })
      .always(function () {
        $jobsContainer.removeClass('loading');
      });
  }

  function loadApplications(showLoader) {
    if (showLoader) {
      $('#applicationsSection').addClass('loading');
    }
    $.getJSON(applicationsEndpoint)
      .done(function (payload) {
        applicationsByJobId.clear();
        const applications = Array.isArray(payload.applications) ? payload.applications : [];
        applications.forEach(application => {
          applicationsByJobId.set(Number(application.jobId), application);
        });
        renderApplications(applications);
        markAppliedJobs();
      })
      .fail(function (xhr) {
        if (xhr.status === 401) {
          renderApplications([]);
          showFeedback('Log in to track your applications and apply quickly.', 'info');
        } else {
          showFeedback('We could not load your applications.', 'error');
        }
      })
      .always(function () {
        $('#applicationsSection').removeClass('loading');
      });
  }

  function renderJobs(jobs) {
    $jobsContainer.empty();
    if (!jobs.length) {
      $emptyState.removeClass('hidden');
      return;
    }
    $emptyState.addClass('hidden');

    jobs.forEach(function (job) {
      const applied = applicationsByJobId.has(job.jobId);
      const statusInfo = applicationsByJobId.get(job.jobId);
      const summary = job.summary && job.summary.length ? job.summary : 'Description not provided yet.';
      const badgeClass = pickStatusBadge(job.status);
      const posted = job.postedDate ? formatDate(job.postedDate) : 'Recently posted';
      const skills = job.skills ? job.skills.split(',').map(s => s.trim()).filter(Boolean) : [];

      const card = `
        <article class="bg-white shadow-sm rounded-xl p-6 card-hover job-card" data-job-id="${job.jobId}">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="font-semibold text-xl">${escapeHtml(job.title || 'Job Opportunity')}</h3>
              <p class="text-slate-500 mt-2">${escapeHtml(job.company || 'Company Confidential')}</p>
            </div>
            <span class="badge ${badgeClass}">${escapeHtml(job.status || 'Active')}</span>
          </div>
          <div class="meta mt-4">
            <span><svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 20.25c4.556 0 8.25-3.694 8.25-8.25S16.556 3.75 12 3.75 3.75 7.444 3.75 12c0 1.548.409 3.002 1.125 4.257L12 20.25z" /></svg>${escapeHtml(job.location || 'Remote friendly')}</span>
            <span><svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8.25 6.75h8.25M8.25 12h5.25M14.25 17.25h-6"/></svg>${escapeHtml(job.jobType || 'Full-time')}</span>
            <span><svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3.75 4.5h16.5v14.25H3.75z" /></svg>${escapeHtml(posted)}</span>
          </div>
          <p class="text-slate-600 mt-4">${escapeHtml(summary)}</p>
          ${skills.length ? `<div class="flex flex-wrap gap-2 mt-4">${skills.map(skill => `<span class="badge badge-slate">${escapeHtml(skill)}</span>`).join('')}</div>` : ''}
          <div class="flex items-center justify-between mt-6">
            <span class="text-sm text-slate-500">Posted ${escapeHtml(posted)}</span>
            ${renderApplyButton(job.jobId, applied, statusInfo)}
          </div>
        </article>`;

      $jobsContainer.append(card);
    });
  }

  function renderApplyButton(jobId, applied, statusInfo) {
    if (applied && statusInfo) {
      const status = escapeHtml(statusInfo.status || 'Applied');
      return `<span class="status-pill" data-status="${status}">Already ${status}</span>`;
    }
    return `<button class="btn-primary" type="button" data-apply="${jobId}">Apply Now</button>`;
  }

  function renderApplications(applications) {
    $applicationsList.empty();
    if (!applications.length) {
      $applicationsList.append('<li class="text-sm text-slate-500">No applications yet. Apply to track progress here.</li>');
      return;
    }
    applications.forEach(function (application) {
      const interview = application.interviewDate ? `Interview: <strong>${escapeHtml(application.interviewDate)}</strong>` : 'Interview: <span class="text-slate-500">Not scheduled</span>';
      const remarks = application.remarks ? `<p class="text-slate-500 mt-2">Notes: ${escapeHtml(application.remarks)}</p>` : '';
      $applicationsList.append(`
        <li class="application-row">
          <div class="flex items-center justify-between flex-wrap gap-3">
            <div>
              <h4 class="font-semibold text-lg">${escapeHtml(application.jobTitle || 'Job Opportunity')}</h4>
              <p class="text-slate-500 text-sm mt-1">${escapeHtml(application.companyName || 'Company Confidential')} • ${escapeHtml(application.jobLocation || 'Remote friendly')}</p>
            </div>
            <span class="status-pill" data-status="${escapeHtml(application.status || 'Applied')}">${escapeHtml(application.status || 'Applied')}</span>
          </div>
          <p class="text-slate-600 text-sm mt-3">Applied on ${escapeHtml(application.appliedDate || 'recently')}</p>
          <p class="text-slate-600 text-sm mt-1">${interview}</p>
          ${remarks}
        </li>
      `);
    });
  }

  function markAppliedJobs() {
    $jobsContainer.find('[data-apply]').each(function () {
      const $button = $(this);
      const jobId = Number($button.data('apply'));
      if (applicationsByJobId.has(jobId)) {
        const statusInfo = applicationsByJobId.get(jobId);
        const status = escapeHtml(statusInfo.status || 'Applied');
        $button.replaceWith(`<span class="status-pill" data-status="${status}">${status}</span>`);
      }
    });
  }

  function submitApplication() {
    const formData = new FormData($applyForm[0]);
    const jobId = formData.get('jobId');
    if (!jobId) {
      showFeedback('We could not determine which job you are applying for.', 'error');
      return;
    }

    $.ajax({
      url: applyEndpoint,
      method: 'POST',
      data: formData,
      processData: false,
      contentType: false,
      success: function (payload) {
        showFeedback(payload && payload.message ? payload.message : 'Application submitted successfully!', 'success');
        $applyModal.simpleModal('hide');
        loadApplications();
      },
      error: function (xhr) {
        let message = 'We could not submit your application. Please try again.';
        if (xhr.responseJSON && xhr.responseJSON.message) {
          message = xhr.responseJSON.message;
        }
        if (xhr.status === 401) {
          message = 'Please log in before applying for a role.';
        }
        showFeedback(message, 'error');
      }
    });
  }

  function pickStatusBadge(status) {
    switch ((status || '').toLowerCase()) {
      case 'active':
        return 'badge-green';
      case 'draft':
        return 'badge-amber';
      case 'closed':
        return 'badge-red';
      default:
        return 'badge-blue';
    }
  }

  function formatDate(value) {
    if (!value) {
      return '';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return '';
    }
    return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
  }

  function escapeHtml(value) {
    if (value === null || value === undefined) {
      return '';
    }
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function showFeedback(message, variant) {
    if (!$feedbackBox.length || !message) {
      return;
    }
    const typeClass = variant === 'success' ? 'alert-success' : variant === 'info' ? 'alert-info' : 'alert-error';
    $feedbackBox
      .removeClass('hidden')
      .attr('class', `alert ${typeClass} mt-4`)
      .html(escapeHtml(message));
  }
})(jQuery);
