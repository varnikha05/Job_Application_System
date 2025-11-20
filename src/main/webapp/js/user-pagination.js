let currentPage = 1;
let limit = 10;
 

function showNext() {
    currentPage++;
    loadJobs($("#searchTitle").val(), $("#searchLocation").val(), currentPage);
}
 
function showPrev() {
    if (currentPage > 1) {
        currentPage--;
        loadJobs($("#searchTitle").val(), $("#searchLocation").val(), currentPage);
    }
}
 

$(document).ready(function () {
 
    $("#pj-next").click(function (e) {
        e.preventDefault();
        showNext();
    });
 
    $("#pj-prev").click(function (e) {
        e.preventDefault();
        showPrev();
    });
 
});/**
 * 
 */