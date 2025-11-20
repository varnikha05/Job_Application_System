/**
 * Chart Diagnostic Script
 * Add this to your admin_home.html temporarily to diagnose issues
 */

console.log("=== CHART DIAGNOSTIC START ===");

// 1. Check jQuery
if (typeof $ !== 'undefined') {
    console.log("✅ jQuery loaded: v" + $.fn.jquery);
} else {
    console.error("❌ jQuery NOT loaded!");
}

// 2. Check Chart.js
if (typeof Chart !== 'undefined') {
    console.log("✅ Chart.js loaded: v" + Chart.version);
} else {
    console.error("❌ Chart.js NOT loaded!");
}

// 3. Check canvas element
$(document).ready(function() {
    const canvas = document.getElementById('applicationsChart');
    if (canvas) {
        console.log("✅ Canvas element found");
        console.log("   - Width:", canvas.width);
        console.log("   - Height:", canvas.height);
        console.log("   - Parent:", canvas.parentElement);
        
        const ctx = canvas.getContext('2d');
        if (ctx) {
            console.log("✅ Canvas context obtained");
        } else {
            console.error("❌ Cannot get canvas context!");
        }
    } else {
        console.error("❌ Canvas element NOT found!");
    }
    
    // 4. Test AJAX call
    console.log("Testing AJAX call to AdminApplicationsServlet...");
    $.ajax({
        url: "/JOB_APPLICATION_SYSTEM/AdminApplicationsServlet",
        method: "GET",
        data: { period: "week" },
        success: function(data) {
            console.log("✅ AJAX Success!");
            console.log("   - Data type:", typeof data);
            console.log("   - Data:", data);
            
            if (typeof data === 'string') {
                try {
                    data = JSON.parse(data);
                    console.log("   - Parsed JSON successfully");
                } catch(e) {
                    console.error("   - JSON parse error:", e);
                }
            }
            
            if (data.trend) {
                console.log("   - Trend array length:", data.trend.length);
                console.log("   - First item:", data.trend[0]);
                console.log("   - Last item:", data.trend[data.trend.length - 1]);
            } else {
                console.error("   - No 'trend' property in data!");
            }
        },
        error: function(xhr, status, error) {
            console.error("❌ AJAX Error!");
            console.error("   - Status:", status);
            console.error("   - Error:", error);
            console.error("   - Response:", xhr.responseText);
        }
    });
    
    // 5. Test simple chart creation
    setTimeout(function() {
        console.log("Testing simple chart creation...");
        try {
            const canvas = document.getElementById('applicationsChart');
            const ctx = canvas.getContext('2d');
            
            const testChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: ['A', 'B', 'C'],
                    datasets: [{
                        label: 'Test',
                        data: [1, 2, 3],
                        borderColor: 'rgb(78, 115, 223)',
                        backgroundColor: 'rgba(78, 115, 223, 0.2)'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false
                }
            });
            
            console.log("✅ Test chart created successfully!");
            console.log("   - Chart instance:", testChart);
            
            // Destroy test chart after 3 seconds
            setTimeout(function() {
                testChart.destroy();
                console.log("Test chart destroyed, loading real data...");
            }, 3000);
            
        } catch(e) {
            console.error("❌ Test chart creation failed!");
            console.error("   - Error:", e.message);
            console.error("   - Stack:", e.stack);
        }
    }, 2000);
});

console.log("=== CHART DIAGNOSTIC END ===");
