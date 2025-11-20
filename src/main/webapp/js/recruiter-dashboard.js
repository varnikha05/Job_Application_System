'use strict';

(function() {
    const stats = {
        totalJobs: document.getElementById('totalJobs'),
        totalApplications: document.getElementById('totalApplications'),
        activeJobs: document.getElementById('activeJobs'),
        todayApplications: document.getElementById('todayApplications'),
        avgApplications: document.getElementById('avgApplications'),
        selectionRate: document.getElementById('selectionRate'),
        avgTimeToHire: document.getElementById('avgTimeToHire'),
        topSkill: document.getElementById('topSkill')
    };

    const tables = {
        myJobs: document.getElementById('jobsTable'),
        applications: document.getElementById('applicationsTable'),
        recentApplications: document.getElementById('recentApplicationsTable')
    };

    const charts = {};

    document.addEventListener('DOMContentLoaded', init);

    function init() {
        showFlashMessage();
        wireNav();
        loadDashboard();
        initCharts();
        refreshJobs();
        refreshApplications();
        refreshRecentApplications();
    }

    function showFlashMessage() {
        const box = document.getElementById('dashboardMessage');
        if (!box) {
            return;
        }
        const params = new URLSearchParams(window.location.search);
        if (params.has('posted')) {
            box.innerHTML = '<div class="alert alert-success" role="alert"><i class="fas fa-check-circle me-2"></i>Job posted successfully.</div>';
        } else if (params.has('error')) {
            box.innerHTML = '<div class="alert alert-danger" role="alert"><i class="fas fa-exclamation-triangle me-2"></i>Unable to load recruiter data. Please log in again.</div>';
        }
        if (params.toString()) {
            window.history.replaceState({}, document.title, window.location.pathname);
        }
    }

    function wireNav() {
        document.querySelectorAll('[data-dashboard-nav]').forEach(link => {
            link.addEventListener('click', event => {
                event.preventDefault();
                const target = link.getAttribute('data-dashboard-nav');
                showTab(target);
            });
        });
    }

    function showTab(tabId) {
        const tabPane = document.getElementById(tabId);
        if (!tabPane) {
            return;
        }
        document.querySelectorAll('.tab-pane').forEach(pane => {
            pane.classList.remove('show', 'active');
        });
        tabPane.classList.add('show', 'active');
        if (tabId === 'my-jobs') {
            refreshJobs();
        } else if (tabId === 'applications') {
            refreshApplications();
        }
    }

    function loadDashboard() {
        fetch('RecruiterStatsServlet')
            .then(handleJson)
            .then(updateStatsCards)
            .catch(() => clearStats());
    }

    function updateStatsCards(data) {
        if (!data) {
            clearStats();
            return;
        }
        stats.totalJobs.textContent = safeNumber(data.totalJobs);
        stats.totalApplications.textContent = safeNumber(data.totalApplications);
        stats.activeJobs.textContent = safeNumber(data.activeJobs);
        stats.todayApplications.textContent = safeNumber(data.todayApplications);
        stats.avgApplications.textContent = formatDecimal(data.avgApplications);
        stats.selectionRate.textContent = `${formatDecimal(data.selectionRate)}%`;
        stats.avgTimeToHire.textContent = safeNumber(data.avgTimeToHire);
        stats.topSkill.textContent = data.topSkill || 'N/A';
        updateCharts(data);
    }

    function clearStats() {
        Object.values(stats).forEach(node => {
            if (node) {
                node.textContent = '0';
            }
        });
        if (stats.topSkill) {
            stats.topSkill.textContent = 'N/A';
        }
    }

    function refreshJobs() {
        if (!tables.myJobs) {
            return;
        }
        const statusFilter = document.getElementById('jobFilter');
        const status = statusFilter ? statusFilter.value : '';
        const requestUrl = status ? `RecruiterJobsServlet?status=${encodeURIComponent(status)}` : 'RecruiterJobsServlet';
        fetch(requestUrl)
            .then(handleJson)
            .then(renderJobs)
            .catch(renderEmptyJobs);
    }

    function renderJobs(payload) {
        const tbody = tables.myJobs.querySelector('tbody');
        if (!tbody) {
            return;
        }
        tbody.innerHTML = '';
    const jobs = payload && Array.isArray(payload.jobs) ? payload.jobs : [];
        if (!jobs.length) {
            renderEmptyJobs();
            return;
        }
        jobs.forEach(job => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><strong>${escapeHtml(job.title)}</strong></td>
                <td>${escapeHtml(job.location)}</td>
                <td><span class="badge bg-info">${safeNumber(job.applicationCount)}</span></td>
                <td>${formatDate(job.postedDate)}</td>
                <td>${renderDeadline(job.closingDate)}</td>
                <td><span class="badge bg-${statusBadge(job.status)}">${escapeHtml(job.status || '')}</span></td>
                <td>
                    <div class="btn-group" role="group">
                        <button class="btn btn-sm btn-outline-primary" data-job-view="${job.jobId}"><i class="fas fa-eye"></i></button>
                    </div>
                </td>`;
            tbody.appendChild(row);
        });
    }

    function renderDeadline(value) {
        const formatted = formatDate(value);
    return formatted || '<span class="text-muted">No deadline</span>';
    }

    function renderEmptyJobs() {
        const tbody = tables.myJobs.querySelector('tbody');
        if (!tbody) {
            return;
        }
        tbody.innerHTML = `<tr>
            <td colspan="7" class="text-center text-muted py-4">
                <i class="fas fa-briefcase me-2"></i>No jobs found. <a href="post-job.html">Post your first job</a>
            </td>
        </tr>`;
    }

    function refreshApplications() {
        if (!tables.applications) {
            return;
        }
        const status = getValue('applicationFilter');
        const dateRange = getValue('dateFilter');
        const params = new URLSearchParams();
        if (status) {
            params.append('status', status);
        }
        if (dateRange) {
            params.append('dateRange', dateRange);
        }
        const requestUrl = params.toString() ? `RecruiterApplicationsServlet?${params.toString()}` : 'RecruiterApplicationsServlet';
        fetch(requestUrl)
            .then(handleJson)
            .then(renderApplications)
            .catch(renderEmptyApplications);
    }

    function renderApplications(payload) {
        const tbody = tables.applications.querySelector('tbody');
        if (!tbody) {
            return;
        }
        tbody.innerHTML = '';
        const applications = payload && Array.isArray(payload.applications) ? payload.applications : [];
        if (!applications.length) {
            renderEmptyApplications();
            return;
        }
        applications.forEach(app => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><strong>${escapeHtml(app.applicantName)}</strong><br><small class="text-muted">${escapeHtml(app.applicantEmail)}</small></td>
                <td>${escapeHtml(app.jobTitle)}</td>
                <td>${formatDate(app.appliedDate)}</td>
                <td>${escapeHtml(app.applicantSkills || '')}</td>
                <td><span class="badge bg-${statusBadge(app.status)}">${escapeHtml(app.status || '')}</span></td>
                <td>${renderResume(app.resumePath)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" data-application-view="${app.applicationId}"><i class="fas fa-eye"></i></button>
                </td>`;
            tbody.appendChild(row);
        });
    }

    function renderEmptyApplications() {
        const tbody = tables.applications.querySelector('tbody');
        if (!tbody) {
            return;
        }
        tbody.innerHTML = `<tr>
            <td colspan="7" class="text-center text-muted py-4">
                <i class="fas fa-user-clock me-2"></i>No applications found.
            </td>
        </tr>`;
    }

    function refreshRecentApplications() {
        if (!tables.recentApplications) {
            return;
        }
        fetch('RecruiterApplicationsServlet?limit=5')
            .then(handleJson)
            .then(renderRecent)
            .catch(renderEmptyRecent);
    }

    function renderRecent(payload) {
        const tbody = tables.recentApplications.querySelector('tbody');
        if (!tbody) {
            return;
        }
        tbody.innerHTML = '';
        const applications = payload && Array.isArray(payload.applications) ? payload.applications : [];
        if (!applications.length) {
            renderEmptyRecent();
            return;
        }
        applications.forEach(app => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><strong>${escapeHtml(app.applicantName)}</strong><br><small class="text-muted">${escapeHtml(app.applicantEmail)}</small></td>
                <td>${escapeHtml(app.jobTitle)}</td>
                <td>${formatDate(app.appliedDate)}</td>
                <td><span class="badge bg-${statusBadge(app.status)}">${escapeHtml(app.status || '')}</span></td>
                <td><button class="btn btn-sm btn-outline-primary" data-application-view="${app.applicationId}"><i class="fas fa-eye"></i> View</button></td>
				`;
            tbody.appendChild(row);
        });
    }

    function renderEmptyRecent() {
        const tbody = tables.recentApplications.querySelector('tbody');
        if (!tbody) {
            return;
        }
        tbody.innerHTML = `<tr>
            <td colspan="5" class="text-center text-muted py-4">
                <i class="fas fa-inbox me-2"></i>No recent applications.
            </td>
        </tr>`;
    }

    function initCharts() {
        const trendsCanvas = document.getElementById('applicationTrendsChart');
        const statusCanvas = document.getElementById('jobStatusChart');
        if (window.Chart && trendsCanvas) {
            charts.trends = new Chart(trendsCanvas.getContext('2d'), {
                type: 'line',
                data: { labels: [], datasets: [{ label: 'Applications', data: [], borderColor: '#89ba16', backgroundColor: 'rgba(137,186,22,0.15)', fill: true }] },
                options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true } } }
            });
        }
        if (window.Chart && statusCanvas) {
            charts.status = new Chart(statusCanvas.getContext('2d'), {
                type: 'doughnut',
                data: { labels: ['Active', 'Closed', 'Draft'], datasets: [{ data: [0, 0, 0], backgroundColor: ['#89ba16', '#28a745', '#ffc107'] }] },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }
    }

    function updateCharts(data) {
        if (charts.trends && Array.isArray(data.monthlyApplications)) {
            charts.trends.data.labels = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
            charts.trends.data.datasets[0].data = data.monthlyApplications;
            charts.trends.update();
        }
        if (charts.status && data.jobStatusDistribution) {
            charts.status.data.datasets[0].data = [
                safeNumber(data.jobStatusDistribution.active),
                safeNumber(data.jobStatusDistribution.closed),
                safeNumber(data.jobStatusDistribution.draft)
            ];
            charts.status.update();
        }
    }

    function handleJson(response) {
        if (!response.ok) {
            throw new Error('Request failed');
        }
        return response.json();
    }

    function safeNumber(value) {
        return Number.isFinite(value) ? value : 0;
    }

    function formatDecimal(value) {
        const number = Number.parseFloat(value);
        return Number.isFinite(number) ? number.toFixed(1) : '0.0';
    }

    function formatDate(value) {
        if (!value) {
            return '';
        }
        try {
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) {
                return '';
            }
            return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
        } catch (err) {
            return '';
        }
    }

    function statusBadge(status) {
        switch ((status || '').toLowerCase()) {
            case 'active':
                return 'success';
            case 'closed':
                return 'danger';
            case 'draft':
                return 'warning';
            case 'pending':
                return 'warning';
            case 'reviewed':
                return 'info';
            case 'shortlisted':
                return 'primary';
            case 'selected':
                return 'success';
            case 'rejected':
                return 'danger';
            default:
                return 'secondary';
        }
    }

    function renderResume(path) {
        if (!path) {
            return '<span class="text-muted">Not uploaded</span>';
        }
        return `<a class="btn btn-sm btn-outline-primary" href="${encodeURI(path)}" target="_blank"><i class="fas fa-file"></i> View</a>`;
    }

    function escapeHtml(value) {
        if (!value) {
            return '';
        }
        return value
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function getValue(controlId) {
        const control = document.getElementById(controlId);
        return control ? control.value : '';
    }
	
})();
