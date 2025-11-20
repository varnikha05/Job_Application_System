(function ($) {
  const jobsEndpoint = 'RecruiterJobsServlet';
  const applicationsEndpoint = 'RecruiterApplicationsServlet';

  const $jobsContainer = $('#recruiterJobsContainer');
  const $applicationsContainer = $('#recruiterApplicationsContainer');
  const $jobsEmptyState = $('#recruiterJobsEmptyState');
  const $applicationsEmptyState = $('#recruiterApplicationsEmptyState');
  const $toast = $('#recruiterFeedback');

  const statusOptions = ['Applied', 'Under Review', 'Shortlisted', 'Interview Scheduled', 'Selected', 'Rejected'];

  $(document).ready(function () {
    loadJobs();
    loadApplications();
    wireEvents();
  });

  function wireEvents() {
    $('#refreshRecruiterJobs').on('click', function () {
      loadJobs(true);
    });
    $('#refreshRecruiterApplications').on('click', function () {
      loadApplications(true);
    });
    $applicationsContainer.on('click', '[data-update-application]', function () {
      const applicationId = Number($(this).data('update-application'));
      submitApplicationUpdate(applicationId, $(this).closest('[data-application-row]'));
    });
  }

  function loadJobs(showLoader) {
    if (showLoader) {
      $jobsContainer.addClass('loading');
    }
    $.getJSON(jobsEndpoint)
      .done(function (payload) {
        const jobs = Array.isArray(payload.jobs) ? payload.jobs : [];
        renderJobs(jobs);
      })
      .fail(function (xhr) {
        if (xhr.status === 401) {
          showToast('Please log in as a recruiter to view your jobs.', 'error');
        } else {
          showToast('Unable to load your job postings.', 'error');
        }
      })
      .always(function () {
        $jobsContainer.removeClass('loading');
      });
  }

  function loadApplications(showLoader) {
    if (showLoader) {
      $applicationsContainer.addClass('loading');
    }
    $.getJSON(applicationsEndpoint)
      .done(function (payload) {
        const applications = Array.isArray(payload.applications) ? payload.applications : [];
        renderApplications(applications);
      })
      .fail(function (xhr) {
        if (xhr.status === 401) {
          showToast('Please log in as a recruiter to view applications.', 'error');
        } else {
          showToast('Unable to load applications. Try again later.', 'error');
        }
      })
      .always(function () {
        $applicationsContainer.removeClass('loading');
      });
  }

  function renderJobs(jobs) {
    $jobsContainer.empty();
    if (!jobs.length) {
      $jobsEmptyState.removeClass('hidden');
      return;
    }
    $jobsEmptyState.addClass('hidden');

    jobs.forEach(function (job) {
      const postedDate = job.postedDate || '';
      const closingDate = job.closingDate || 'Not set';
      const badge = pickStatusBadge(job.status);
      const description = job.description ? truncate(job.description, 160) : 'No description provided yet.';
      $jobsContainer.append(`
        <article class="bg-white rounded-xl shadow-sm p-6 card-hover">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="font-semibold text-xl">${escapeHtml(job.title || 'Untitled role')}</h3>
              <p class="text-slate-500 mt-2">${escapeHtml(job.companyName || 'Company Confidential')}</p>
            </div>
            <span class="badge ${badge}">${escapeHtml(job.status || 'Active')}</span>
          </div>
          <div class="mt-4 flex flex-wrap gap-3 text-sm text-slate-500">
            <span>📍 ${escapeHtml(job.location || 'Remote / Flexible')}</span>
            <span>🗓️ Posted ${escapeHtml(postedDate)}</span>
            <span>⏰ Deadline ${escapeHtml(closingDate)}</span>
            <span>📝 Applications ${escapeHtml(job.applicationCount || 0)}</span>
          </div>
          <p class="text-slate-600 text-sm mt-4">${escapeHtml(description)}</p>
        </article>
      `);
    });
  }

  function renderApplications(applications) {
    $applicationsContainer.empty();
    if (!applications.length) {
      $applicationsEmptyState.removeClass('hidden');
      return;
    }
    $applicationsEmptyState.addClass('hidden');

    applications.forEach(function (application) {
      const interviewDate = application.interviewDate || '';
      const remarks = application.remarks || '';
      const resumeLink = application.resumePath ? `<a class="resume-link" href="${escapeHtml(application.resumePath)}" target="_blank" rel="noopener">Download resume</a>` : '<span class="text-slate-500">No resume uploaded</span>';
      $applicationsContainer.append(`
        <article class="application-row" data-application-row data-application-id="${application.applicationId}">
          <div class="flex items-center justify-between flex-wrap gap-3">
            <div>
              <h3 class="font-semibold text-lg">${escapeHtml(application.applicantName || 'Candidate')}</h3>
              <p class="text-slate-500 text-sm mt-1">${escapeHtml(application.applicantEmail || 'Email not available')}</p>
              <p class="text-slate-500 text-sm mt-1">Applied for <strong>${escapeHtml(application.jobTitle || 'Job')}</strong> at ${escapeHtml(application.companyName || 'your company')}</p>
            </div>
            <span class="status-pill" data-status="${escapeHtml(application.status || 'Applied')}">${escapeHtml(application.status || 'Applied')}</span>
          </div>
          <div class="mt-3 text-sm text-slate-600">
            <p>Skills: ${escapeHtml(application.applicantSkills || 'Not provided')}</p>
            <p class="mt-2">${resumeLink}</p>
            <p class="mt-2">Interview: ${interviewDate ? `<strong>${escapeHtml(interviewDate)}</strong>` : '<span class="text-slate-500">Not scheduled</span>'}</p>
          </div>
          <div class="mt-4 grid md:grid-cols-3 gap-4">
            <label>
              <span class="text-sm font-medium text-slate-600">Update status</span>
              <select name="status">
                ${statusOptions.map(option => `<option ${option === (application.status || 'Applied') ? 'selected' : ''} value="${option}">${option}</option>`).join('')}
              </select>
            </label>
            <label>
              <span class="text-sm font-medium text-slate-600">Interview date</span>
              <input type="datetime-local" name="interviewDate" value="${formatForInput(interviewDate)}" />
            </label>
            <label>
              <span class="text-sm font-medium text-slate-600">Notes</span>
              <textarea name="remarks" rows="2" placeholder="Optional notes for this applicant">${escapeHtml(remarks)}</textarea>
            </label>
          </div>
          <div class="table-actions mt-4">
            <button class="btn-primary" type="button" data-update-application="${application.applicationId}">Save update</button>
          </div>
        </article>
      `);
    });
  }

  function submitApplicationUpdate(applicationId, $row) {
    if (!$row.length) {
      showToast('We could not read the selected application.', 'error');
      return;
    }
    const status = $row.find('select[name="status"]').val();
    const interviewDate = $row.find('input[name="interviewDate"]').val();
    const remarks = $row.find('textarea[name="remarks"]').val();

    $.ajax({
      url: applicationsEndpoint,
      method: 'POST',
      data: {
        action: 'updateStatus',
        applicationId,
        status,
        interviewDate,
        remarks
      }
    }).done(function (payload) {
      const message = payload && payload.message ? payload.message : 'Application updated successfully.';
      showToast(message, 'success');
      loadApplications();
    }).fail(function (xhr) {
      let message = 'Unable to update application. Please try again.';
      if (xhr.responseJSON && xhr.responseJSON.message) {
        message = xhr.responseJSON.message;
      }
      showToast(message, 'error');
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
        return 'badge-slate';
    }
  }

  function truncate(text, max) {
    if (!text) {
      return '';
    }
    return text.length > max ? text.substring(0, max - 1) + '…' : text;
  }

  function formatForInput(value) {
    if (!value) {
      return '';
    }
    const normalized = value.includes(' ') ? value.replace(' ', 'T') : value;
    const date = new Date(normalized);
    if (Number.isNaN(date.getTime())) {
      return '';
    }
    const pad = num => String(num).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
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

  function showToast(message, variant) {
    if (!$toast.length) {
      return;
    }
    const typeClass = variant === 'success' ? 'alert-success' : variant === 'error' ? 'alert-error' : 'alert-info';
    $toast
      .removeClass('hidden')
      .attr('class', `alert ${typeClass} mt-4`)
      .html(escapeHtml(message));
  }
})(jQuery);
