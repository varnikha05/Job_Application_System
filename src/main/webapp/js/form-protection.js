/**
 * 

 */

// Track if form has unsaved changes
let formChanged = false;
let formSubmitting = false;


$(document).ready(function() {
    setupFormProtection();
});


function setupFormProtection() {
    // Find all forms on the page
    $('form').each(function() {
        const form = $(this);
        
        // Skip if form already has protection
        if (form.hasClass('form-protected')) {
            return;
        }
        
       
        form.addClass('form-protected');
        
     
        form.find('input, select, textarea').on('input change', function() {
            formChanged = true;
            console.log('Form changed detected');
        });
        
 
        form.on('submit', function() {
            formSubmitting = true;
            formChanged = false;
            console.log('Form submitting - protection disabled');
        });
    });
    
    $(window).on('beforeunload', function(e) {
        if (formChanged && !formSubmitting) {
            const message = 'You have unsaved changes. Are you sure you want to leave? Your data will not be saved.';
            e.returnValue = message;
            return message;
        }
    });
    
   
    $('a, button[type="button"]').on('click', function(e) {
        const link = $(this);
        
        // Skip if it's a submit button or form button
        if (link.attr('type') === 'submit' || link.closest('form').length > 0) {
            return;
        }
        
        // Skip if no changes made
        if (!formChanged) {
            return;
        }
        
        // Prevent default action
        e.preventDefault();
        e.stopPropagation();

        // Show SweetAlert confirmation
        Swal.fire({
            icon: 'warning',
            title: 'Unsaved Changes',
            text: 'You have unsaved changes. Are you sure you want to leave? Your data will not be saved.',
            showCancelButton: true,
            confirmButtonText: 'Leave',
            cancelButtonText: 'Stay',
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#28a745'
        }).then((result) => {
            if (result.isConfirmed) {
                formChanged = false;
                // Navigate to the link
                window.location.href = link.attr('href') || '#';
            }
        });

        return false;
    });
}

/**

 */
function resetFormProtection() {
    formChanged = false;
    formSubmitting = false;
    console.log('Form protection reset');
}

/**
 *
 */
function markFormChanged() {
    formChanged = true;
}

/**
 * Check if form has unsaved changes
 */
function hasUnsavedChanges() {
    return formChanged;
}