let currentPage = 1;
const limit = 10;
let totalJobs = 0;
let totalPages = 0;
let isLoading = false;

document.addEventListener('DOMContentLoaded', function() {
  loadJobs();
  

  // Next button functionality
  document.getElementById('pj-next').addEventListener('click', function(e) {
    e.preventDefault();
    if (currentPage < totalPages && !isLoading) {
      currentPage++;
      loadJobs();
    }
  });

  // Previous button functionality
  document.getElementById('pj-prev').addEventListener('click', function(e) {
    e.preventDefault();
    if (currentPage > 1 && !isLoading) {
      currentPage--;
      loadJobs();
    }
  });

  // Search button functionality
  document.getElementById('searchBtn').addEventListener('click', function(e) {
    e.preventDefault();
    currentPage = 1;
    loadJobs();
  });

  // Allow Enter key to trigger search
  document.getElementById('searchTitle').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
      e.preventDefault();
      currentPage = 1;
      loadJobs();
    }
  });
});

function loadStats() {
  fetch('/JOB_APPLICATION_SYSTEM/PublicStatsServlet')
    .then(response => response.json())
    .then(data => {
      const elements = document.querySelectorAll('.number');
      elements.forEach(el => {
        const caption = el.parentNode.parentNode.querySelector('.caption');
        if (caption) {
          const text = caption.textContent.trim();
          if (text === 'Candidates') el.textContent = data.totalCandidates;
          if (text === 'Jobs Posted') el.textContent = data.totalJobs;
          if (text === 'Jobs Filled') el.textContent = data.jobsFilled;
          if (text === 'Companies') el.textContent = data.totalCompanies;
        }
      });
    })
    .catch(err => console.error('Stats error:', err));
}

function loadJobs() {
  if (isLoading) return;

  isLoading = true;
  showLoading();

  const title = document.getElementById('searchTitle').value.trim() || "";
  const locationSelect = document.getElementById('searchLocation');
  const location = locationSelect && locationSelect.value ? locationSelect.value : "";
  const jobTypeSelect = document.getElementById('searchJobType');
 
//  const companyName = job.company_name ? job.company_name : 'Company not specified';
  //const jobType = job.job_type ? job.job_type : 'Full Time';

  const params = new URLSearchParams({
    limit: limit,
    page: currentPage,
    title: title,
    location: location,
   // jobType: jobType
  });

  fetch(`/JOB_APPLICATION_SYSTEM/PublicJobsServlet?${params.toString()}`)
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      totalJobs = data.totalJobs || 0;
      totalPages = Math.ceil(totalJobs / limit);
      renderJobs(data.jobs || []);
      updatePagination();
      updateJobCounter();
    })
    .catch(err => {
      console.error('Error loading jobs:', err);
      showError();
    })
    .finally(() => {
      isLoading = false;
    });
}

function showLoading() {
  const list = document.getElementById('publicJobsList');
  list.innerHTML = `
    <li class="text-center py-5" id="publicJobsLoading">
      <div class="spinner-border text-primary" role="status">
        <span class="sr-only">Loading...</span>
      </div>
      <p class="mt-3 mb-0 text-muted">Loading jobs...</p>
    </li>
  `;
}

function showError() {
  const list = document.getElementById('publicJobsList');
  list.innerHTML = `
    <li class="text-center py-4">
      <div class="alert alert-danger">
        <h5>Error Loading Jobs</h5>
        <p>Unable to load jobs at the moment. Please try again later.</p>
        <button class="btn btn-outline-danger btn-sm" onclick="loadJobs()">Retry</button>
      </div>
    </li>
  `;
}

// Example: jobs = [{title, company, location, salary, type, posted, id}]
function renderJobs(jobs) {
  const jobList = document.getElementById('publicJobsList');
  jobList.innerHTML = '';

  jobs.forEach(job => {
    // Use companyName and jobType from backend
    const companyName = job.companyName && job.companyName.trim() ? job.companyName : 'Company not specified';
    const jobType = job.jobType || 'Full Time';

    const li = document.createElement('li');
    li.className = 'job-card-outline';

    li.innerHTML = `
      <div class="job-card-main">
        <div class="job-card-title-row">
          <div onclick="viewDetails(${job.jobId})" style="cursor:pointer;">
            <div class="job-title-ref">${job.title}</div>
            <div class="job-company-ref">${companyName}</div>
          </div>
          <div class="job-card-action">
            <button class="btn-apply-ref" onclick="applyJob(${job.jobId})">Apply</button>
          </div>
        </div>
        <div class="job-card-meta-row">
		
          <span class="badge-ref">${jobType}</span>
          <span class="salary-ref">${job.salary ? job.salary + ' LPA' : ''}</span>
          <span>${job.location}</span>
          <span>${job.posted ? job.posted : ''}</span>
        </div>
      </div>
    `;

    jobList.appendChild(li);
  });
}

function applyJob(jobId) {
  window.location.href = "job-details.html?jobId=" + jobId;
}


function viewDetails(jobId) {
  window.location.href = "user-job-details.html?jobId=" + jobId;
}



function updatePagination() {
  const start = totalJobs > 0 ? (currentPage - 1) * limit + 1 : 0;
  const end = Math.min(currentPage * limit, totalJobs);
  const pageInfo = totalJobs > 0 ? `Showing ${start}-${end} of ${totalJobs} jobs` : 'No jobs found';

  document.querySelector('.page-info').textContent = pageInfo;

  const prevBtn = document.getElementById('pj-prev');
  const nextBtn = document.getElementById('pj-next');

  prevBtn.classList.toggle('disabled', currentPage <= 1);
  nextBtn.classList.toggle('disabled', currentPage >= totalPages || totalJobs === 0);

  updatePaginationNumbers();
}

function updatePaginationNumbers() {
  const paginationDiv = document.querySelector('.custom-pagination .d-inline-block');
  if (!paginationDiv) return;

  paginationDiv.innerHTML = '';

  if (totalPages <= 1) {
    const pageLink = document.createElement('a');
    pageLink.href = '#';
    pageLink.textContent = '1';
    pageLink.className = 'active';
    paginationDiv.appendChild(pageLink);
    return;
  }

  const maxVisiblePages = 5;
  let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
  let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

  if (endPage - startPage < maxVisiblePages - 1) {
    startPage = Math.max(1, endPage - maxVisiblePages + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    const pageLink = document.createElement('a');
    pageLink.href = '#';
    pageLink.textContent = i;
    pageLink.className = i === currentPage ? 'active' : '';
    pageLink.addEventListener('click', function(e) {
      e.preventDefault();
      if (i !== currentPage && !isLoading) {
        currentPage = i;
        loadJobs();
      }
    });
    paginationDiv.appendChild(pageLink);
  }
}

function updateJobCounter() {
  const counterElement = document.getElementById('jobsPostedCounter');
  if (counterElement) {
    counterElement.setAttribute('data-number', totalJobs);
    counterElement.textContent = totalJobs;
  }

  const headingElement = document.getElementById('jobCountHeading');
  if (headingElement) {
    headingElement.textContent = totalJobs > 0 ? `Latest Jobs (${totalJobs} available)` : 'Latest Jobs';
  }
}
