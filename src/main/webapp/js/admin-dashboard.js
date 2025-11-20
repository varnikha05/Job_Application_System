// chart instance
let applicationsChartInstance = null;

$(document).ready(function () {
    // Check if Chart.js is loaded
    if (typeof Chart === 'undefined') {
        console.error("Chart.js library not loaded!");
        showAlert("Chart library failed to load. Please refresh the page.", "danger");
    } else {
        console.log("Chart.js version:", Chart.version);
    }
    
    loadDashboardStats();
    loadJobsTable();
    setupNavigationTabs();
    
	const BASE = "/JOB_APPLICATION_SYSTEM";

    loadApplicationsOverview("week");
    window.changeChartPeriod = function (p) {
        // Update button states
        $('.btn-group button').removeClass('active');
        event.target.classList.add('active');
        loadApplicationsOverview(p);
    };
    
  
    loadQuickStats();
    loadRecentActivity();
});

function loadDashboardStats() {
    $.ajax({
        url: "/JOB_APPLICATION_SYSTEM/AdminStatsServlet",
        method: "GET",
        success: function (data) {
            if (data.status === "success") {
                $("#totalUsers").text(data.totalUsers);
                $("#totalRecruiters").text(data.totalRecruiters);
                $("#totalJobs").text(data.totalJobs);
                $("#totalApplications").text(data.totalApplications);
            } else {
                console.error("Stats Error:", data.message);
            }
        },
        error: function (xhr) {
            console.error("Dashboard stats failed", xhr.responseText);
        }
    });
}


function setupNavigationTabs() {
    $(".dashboard-nav .nav-link").on("click", function (e) {
        e.preventDefault();

        $(".dashboard-nav .nav-link").removeClass("active");
        $(this).addClass("active");

        $(".dashboard-section").hide();

        let sectionId = $(this).data("section");
        $("#" + sectionId).show();

      
        if (sectionId === "manage-recruiters-content") {
            loadRecruitersTable();
        }
        if (sectionId === "manage-admins-content") {
            loadAdminsTable();
        }
        if (sectionId === "manage-applications-content") {
            loadApplicationsTable();
        }
        if (sectionId === "dashboard-content") {
            loadDashboardStats();
        }
    });
}

function loadJobsTable() {
    $.ajax({
        url: "/JOB_APPLICATION_SYSTEM/AdminJobsServlet",
        method: "GET",
        success: function (response) {
            const data = (typeof response === "string") ? JSON.parse(response) : response;
            const tbody = $("#jobsTableBody");
            tbody.empty();

            const jobs = data.jobs;

            if (!jobs || jobs.length === 0) {
                tbody.append(`<tr><td colspan="6" class="text-center">No jobs found</td></tr>`);
                return;
            }

            jobs.forEach(job => {
                tbody.append(`
                    <tr>
                        <td>${job.recruiter_name || "-"}</td>
                        <td>${job.title || "-"}</td>
                        <td>${job.posted_date ? new Date(job.posted_date).toLocaleDateString() : "-"}</td>
                        <td>
                            <span class="badge badge-info">${job.application_count || 0}</span>
                        </td>
                        <td>
                            <span class="badge badge-${job.status === 'Active' ? 'success' : 'secondary'}">${job.status || "-"}</span>
                        </td>
                        <td>
                            <button class="btn btn-sm btn-primary" onclick="viewJobDetails(${job.job_id})">
                                <i class="icon-eye mr-1"></i>View
                            </button>
                        </td>
                    </tr>
                `);
            });

            if ($.fn.DataTable.isDataTable("#jobsTable")) {
                $("#jobsTable").DataTable().destroy();
            }

            $("#jobsTable").DataTable({
                pageLength: 10,
                autoWidth: false
            });
        },
        error: function (xhr) {
            console.error("Jobs load error:", xhr.responseText);
        }
    });
}

// View Job Details Modal
function viewJobDetails(jobId) {
    $.ajax({
        url: "/JOB_APPLICATION_SYSTEM/AdminJobsServlet",
        method: "GET",
        data: { jobId: jobId },
        success: function (response) {
            const jobs = response.jobs;
            if (jobs && jobs.length > 0) {
                const job = jobs.find(j=>j.job_id == jobId);
                showJobDetailsModal(job);
            } else {
                showAlert("Job not found", "error");
            }
        },
        error: function () {
            showAlert("Error loading job details", "error");
        }
    });
}

function showJobDetailsModal(job) {//view job details
    const modalContent = `
        <div class="modal fade" id="jobViewModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header bg-primary text-white">
                        <h5 class="modal-title">Job Details</h5>
                        <button type="button" class="close text-white" data-dismiss="modal">
                            <span>&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-8">
                                <h6 class="text-primary">${escapeHtml(job.title)}</h6>
                                <p class="text-muted mb-3">
                                    <i class="icon-briefcase mr-2"></i>${escapeHtml(job.company_name)}
                                    <span class="ml-3"><i class="icon-map-marker mr-1"></i>${escapeHtml(job.location)}</span>
                                </p>
                            </div>
                            <div class="col-md-4 text-right">
                                
                                <div class="mt-2">
                                    <small class="text-muted">Applications: </small>
                                    <span class="badge badge-info">${job.application_count}</span>
                                </div>
                            </div>
                        </div>
                        
                        <hr>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <p><strong>Experience Required:</strong></p>
                                <p class="text-muted">${escapeHtml(job.experience || 'Not specified')}</p>
                            </div>
                            <div class="col-md-6">
                                <p><strong>Salary:</strong></p>
                                <p class="text-muted">${escapeHtml(job.salary || 'Not disclosed')}</p>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <p><strong>Posted Date:</strong></p>
                                <p class="text-muted">${new Date(job.posted_date).toLocaleDateString()}</p>
                            </div>
                            <div class="col-md-6">
                                <p><strong>Recruiter Contact:</strong></p>
                                <p class="text-muted">${escapeHtml(job.recruiter_email)}</p>
                            </div>
                        </div>
                        
                        <hr>
              
                       <p><strong>Job Description:</strong></p>
					<div class="border p-2 bg-light">
    ${job.description ? job.description : 'No description provided'}
</div>
                        
                        <p class="mt-3"><strong>Required Skills:</strong></p>
                        <div class="border p-2 bg-light">
                            ${escapeHtml(job.skills || 'Not specified')}
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                       
                    </div>
                </div>
            </div>
        </div>
    `;

    // Remove existing modal and add new one
    $('#jobViewModal').remove();
    $('body').append(modalContent);
    $('#jobViewModal').modal('show');
}

// Loading sidebar quick stats
function loadQuickStats() {
    $.ajax({
        url: "/JOB_APPLICATION_SYSTEM/AdminStatsServlet",
        method: "GET",
        success: function (data) {
            if (data.status === "success") {
                $("#quickPendingRecruiters").text(data.pendingRecruiters || 0);
                $("#quickTodayJobs").text(data.jobsToday || 0);
                $("#quickTodayApplications").text(data.applicationsToday || 0);
                $("#quickActiveSessions").text(data.activeSessions || 0);
            }
        },
        error: function () {
            $("#quickPendingRecruiters").text("-");
            $("#quickTodayJobs").text("-");
            $("#quickTodayApplications").text("-");
            $("#quickActiveSessions").text("-");
        }
    });
}

// Updated Recent Activity - Gets real data from database
function loadRecentActivity() {
    $.ajax({
        url: "/JOB_APPLICATION_SYSTEM/RecentActivityServlet",
        method: "GET",
        success: function (data) {
            const container = $("#recentActivityContainer");
            container.empty();

            if (data.status !== "success" || !data.activities || data.activities.length === 0) {
                container.html(`
                    <div class="text-center text-muted py-3">
                        <i class="icon-info"></i><br>
                        No recent activity found
                    </div>
                `);
                return;
            }

            //  real activities from database
            data.activities.forEach(activity => {
                container.append(`
                    <div class="d-flex align-items-center mb-3 p-2 border-left border-${activity.color}">
                        <div class="mr-3">
                            <i class="${activity.icon} text-${activity.color}"></i>
                        </div>
                        <div class="flex-grow-1">
                            <div class="font-weight-bold text-sm">${activity.action}</div>
                            <small class="text-muted">${activity.time}</small>
                        </div>
                    </div>
                `);
            });
        },
        error: function (xhr) {
            console.error("Recent activity error:", xhr.responseText);
            $("#recentActivityContainer").html(`
                <div class="text-center text-muted py-3">
                    <i class="icon-alert-circle"></i><br>
                    Failed to load activities
                </div>
            `);
        }
    });
}

// Export Functions 
function exportUsersData() {
    showAlert("Preparing users data export...", "info");
    
    // Create form for file download
    const form = $('<form>', {
        'method': 'POST',
        'action': '/JOB_APPLICATION_SYSTEM/ExcelExportServlet'
    });
    
    form.append($('<input>', {
        'type': 'hidden',
        'name': 'dataType',
        'value': 'users'
    }));
    
    $('body').append(form);
    form.submit();
    form.remove();
    
    showAlert("Users data export started. Download will begin shortly.", "success");
}

function exportRecruitersData() {
    showAlert("Preparing recruiters data export...", "info");
    
    const form = $('<form>', {
        'method': 'POST',
        'action': '/JOB_APPLICATION_SYSTEM/ExcelExportServlet'
    });
    
    form.append($('<input>', {
        'type': 'hidden',
        'name': 'dataType',
        'value': 'recruiters'
    }));
    
    $('body').append(form);
    form.submit();
    form.remove();
    
    showAlert("Recruiters data export started. Download will begin shortly.", "success");
}

function exportApplicationsData() {
    showAlert("Preparing applications data export...", "info");
    
    const form = $('<form>', {
        'method': 'POST',
        'action': '/JOB_APPLICATION_SYSTEM/ExcelExportServlet'
    });
    
    form.append($('<input>', {
        'type': 'hidden',
        'name': 'dataType',
        'value': 'applications'
    }));
    
    $('body').append(form);
    form.submit();
    form.remove();
    
    showAlert("Applications data export started. Download will begin shortly.", "success");
}

function clearSystemLogs() {
    if (confirm("Are you sure you want to clear system logs? This action cannot be undone.")) {
        showAlert("System logs cleared successfully", "success");
    }
}




function loadRecruitersTable() {
    showAlert("Recruiter management will load recruiter data here", "info");
}

function loadAdminsTable() {
    showAlert("Admin management will load admin data here", "info");
}

function loadApplicationsTable() {
    showAlert("Application management will load application data here", "info");
}

// Test function with sample data (for debugging)
function loadApplicationsOverviewWithData(period, data) {
    renderChart(period, data);
}

function loadApplicationsOverview(period) {
    $.ajax({
        url: "/JOB_APPLICATION_SYSTEM/AdminApplicationsServlet",
        method: "GET",
        data: { period: period || "week" },
        success: function (data) {
            console.log("Chart data received:", data);
            console.log("Period:", period);
            renderChart(period, data);
        },
        error: function (xhr, status, error) {
            console.error("AJAX Error:", status, error);
            console.error("Response:", xhr.responseText);
            $("#applicationsChart").parent().html(
                "<div class='text-center text-danger mt-5'>" +
                "<i class='icon-alert-circle' style='font-size: 2rem;'></i><br><br>" +
                "<p>Failed to load chart data</p>" +
                "<small>Status: " + status + "</small>" +
                "</div>"
            );
        }
    });
}

function renderChart(period, data) {
            console.log("Rendering chart...");
            console.log("Period:", period);
            
            try {
                // Parse if string
                if (typeof data === 'string') {
                    data = JSON.parse(data);
                }
                
                const trend = data.trend || [];
                console.log("Trend array length:", trend.length);
                console.log("Trend data:", trend);
                
                // Check if we have data
                if (!trend || trend.length === 0) {
                    console.warn("No trend data available");
                    $("#applicationsChart").parent().html(
                        "<div class='text-center text-muted mt-5'>" +
                        "<i class='icon-info-circle' style='font-size: 2rem;'></i><br><br>" +
                        "<p>No application data available for this period</p>" +
                        "<p class='text-sm'>Try adding some applications or selecting a different time period</p>" +
                        "</div>"
                    );
                    return;
                }
                
                // Check if all values are zero
                const hasNonZeroData = trend.some(p => p.count > 0);
                if (!hasNonZeroData) {
                    console.warn("All application counts are zero");
                }
                
                // Extract labels and data
                const labels = trend.map(p => {
                    const date = new Date(p.date);
                    if (period === 'today') {
                        // Show hour format: 09 AM, 10 AM, etc.
                        return date.toLocaleTimeString('en-US', { hour: '2-digit', hour12: true }).replace(':00', '');
                    } else if (period === 'year') {
                        // Show month names: Jan, Feb, etc.
                        return date.toLocaleDateString('en-US', { month: 'short', year: '2-digit' });
                    } else if (period === 'month') {
                        // Show date with month: Nov 1, Nov 2, etc.
                        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                    } else {
                        // Week view: Show day name + date
                        return date.toLocaleDateString('en-US', { weekday: 'short', day: 'numeric' });
                    }
                });
                const counts = trend.map(p => p.count);
                
                console.log("Chart labels:", labels);
                console.log("Chart counts:", counts);
                
                // Calculate statistics
                const totalApps = counts.reduce((a, b) => a + b, 0);
                const avgApps = counts.length > 0 ? (totalApps / counts.length).toFixed(1) : 0;
                const maxApps = Math.max(...counts, 0);
                
                console.log("Statistics - Total:", totalApps, "Avg:", avgApps, "Max:", maxApps);
                
                
                if (applicationsChartInstance) {
                    applicationsChartInstance.destroy();
                    applicationsChartInstance = null;
                }
                
                // Check Chart.js
                if (typeof Chart === 'undefined') {
                    console.error("Chart.js is not loaded!");
                    $("#applicationsChart").parent().html(
                        "<div class='text-center text-warning mt-5'>" +
                        "<i class='icon-alert-triangle' style='font-size: 2rem;'></i><br><br>" +
                        "<p>Chart library not available</p>" +
                        "<button class='btn btn-primary btn-sm' onclick='location.reload()'>Reload Page</button>" +
                        "</div>"
                    );
                    return;
                }
                
       
                const canvas = document.getElementById('applicationsChart');
                if (!canvas) {
                    console.error("Canvas element not found!");
                    return;
                }
                
               
                const ctx = canvas.getContext('2d');
                console.log("Canvas context obtained:", ctx);
                
           
                const gradient = ctx.createLinearGradient(0, 0, 0, 300);
                let borderColor, pointColor;
                
                // Different colors for different periods
                if (period === 'today') {
                    gradient.addColorStop(0, 'rgba(54, 185, 204, 0.5)');
                    gradient.addColorStop(1, 'rgba(54, 185, 204, 0.05)');
                    borderColor = 'rgb(54, 185, 204)';
                    pointColor = 'rgb(54, 185, 204)';
                } else if (period === 'week') {
                    gradient.addColorStop(0, 'rgba(78, 115, 223, 0.5)');
                    gradient.addColorStop(1, 'rgba(78, 115, 223, 0.05)');
                    borderColor = 'rgb(78, 115, 223)';
                    pointColor = 'rgb(78, 115, 223)';
                } else if (period === 'month') {
                    gradient.addColorStop(0, 'rgba(28, 200, 138, 0.5)');
                    gradient.addColorStop(1, 'rgba(28, 200, 138, 0.05)');
                    borderColor = 'rgb(28, 200, 138)';
                    pointColor = 'rgb(28, 200, 138)';
                } else {
                    gradient.addColorStop(0, 'rgba(246, 194, 62, 0.5)');
                    gradient.addColorStop(1, 'rgba(246, 194, 62, 0.05)');
                    borderColor = 'rgb(246, 194, 62)';
                    pointColor = 'rgb(246, 194, 62)';
                }
                
                // Create new Chart.js chart with enhanced styling
                applicationsChartInstance = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'Applications',
                            data: counts,
                            backgroundColor: gradient,
                            borderColor: borderColor,
                            borderWidth: 3,
                            pointRadius: 5,
                            pointHoverRadius: 8,
                            pointBackgroundColor: pointColor,
                            pointBorderColor: '#fff',
                            pointBorderWidth: 3,
                            pointHoverBackgroundColor: pointColor,
                            pointHoverBorderColor: '#fff',
                            pointHoverBorderWidth: 3,
                            fill: true,
                            tension: 0.4,
                            shadowOffsetX: 3,
                            shadowOffsetY: 3,
                            shadowBlur: 10,
                            shadowColor: 'rgba(0, 0, 0, 0.1)'
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                display: true,
                                position: 'top',
                                labels: {
                                    usePointStyle: true,
                                    padding: 15,
                                    font: {
                                        size: 12,
                                        weight: '500'
                                    }
                                }
                            },
                            tooltip: {
                                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                                padding: 12,
                                titleFont: {
                                    size: 14,
                                    weight: 'bold'
                                },
                                bodyFont: {
                                    size: 13
                                },
                                displayColors: true,
                                callbacks: {
                                    title: function(context) {
                                        return context[0].label;
                                    },
                                    label: function(context) {
                                        return ' ' + context.parsed.y + ' Application' + (context.parsed.y !== 1 ? 's' : '');
                                    },
                                   
                                }
                            }
                        },
                        scales: {
                            x: {
                                grid: {
                                    display: false
                                },
                                ticks: {
                                    font: {
                                        size: 11
                                    },
                                    maxRotation: 45,
                                    minRotation: 0
                                }
                            },
                            y: {
                                beginAtZero: true,
                                grid: {
                                    color: 'rgba(0, 0, 0, 0.05)',
                                    drawBorder: false
                                },
                                ticks: {
                                    font: {
                                        size: 11
                                    },
                                    stepSize: Math.ceil(maxApps / 5),
                                    callback: function(value) {
                                        return value.toFixed(0);
                                    }
                                }
                            }
                        },
                        interaction: {
                            intersect: false,
                            mode: 'index'
                        },
                        animation: {
                            duration: 1000,
                            easing: 'easeInOutQuart'
                        }
                    }
                });
                
                console.log("Chart created successfully!", applicationsChartInstance);
                console.log(`Period: ${period} | Total: ${totalApps} | Avg: ${avgApps} | Max: ${maxApps}`);
                
                // Update the info text with statistics
                const periodText = {
                    'today': 'today',
                    'week': 'the last 7 days',
                    'month': 'the last 30 days',
                    'year': 'the last 365 days'
                }[period] || 'the selected period';
                
              
                
            } catch (e) {
                console.error("Chart rendering error:", e);
                console.error("Error stack:", e.stack);
                
                // Fallback: Show data in table format
                let fallbackHtml = "<div class='text-center'>" +
                    "<p class='text-warning'><i class='icon-alert-triangle'></i> Chart rendering failed</p>" +
                    "<small class='text-muted'>Error: " + e.message + "</small><br><br>";
                
                if (trend && trend.length > 0) {
                    fallbackHtml += "<table class='table table-sm table-bordered'>" +
                        "<thead><tr><th>Date</th><th>Applications</th></tr></thead><tbody>";
                    
                    trend.forEach(function(item) {
                        fallbackHtml += "<tr><td>" + item.date + "</td><td>" + item.count + "</td></tr>";
                    });
                    
                    fallbackHtml += "</tbody></table>";
                }
                
                fallbackHtml += "<button class='btn btn-primary btn-sm' onclick='location.reload()'>Reload Page</button></div>";
                
                $("#applicationsChart").parent().html(fallbackHtml);
            }
}

// Utility Functions
function escapeHtml(s) {
    return (s == null ? "" : String(s)).replace(/[&<>"']/g, c => (
        { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]
    ));
}

function showAlert(message, type = 'info') {
    $('.alert-admin').remove();
    
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show alert-admin" role="alert">
            ${message}
            <button type="button" class="close" data-dismiss="alert">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
    `;
    
    $('.container-fluid').prepend(alertHtml);
    
    setTimeout(() => {
        $('.alert-admin').fadeOut();
    }, 5000);
}


//end 
// Placeholder functions
function addNewUser() {
    showAlert("Add New User functionality - opens user creation form", "info");
}

function addNewRecruiter() {
    showAlert("Add New Recruiter functionality - opens recruiter creation form", "info");
}

function addNewAdmin() {
    showAlert("Add New Admin functionality - opens admin creation form", "info");
}

function viewJobApplications(jobId) {
    showAlert("This will show all applications for job ID: " + jobId, "info");
}