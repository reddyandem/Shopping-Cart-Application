function updateIsActive() {
        var checkbox = document.getElementById("isActiveCheckbox");
        var hiddenInput = document.getElementById("isActiveHidden");
        var isChecked = checkbox.checked;
        hiddenInput.value = isChecked ? "true" : "false";
        console.log("Checkbox state changed - isActive: " + hiddenInput.value); // Debug log
    }

    document.addEventListener("DOMContentLoaded", function() {
        var form = document.querySelector("form");
        form.onsubmit = function(event) {
            updateIsActive(); // Ensure value is updated
            var hiddenInput = document.getElementById("isActiveHidden");
            console.log("Form submitting - isActive: " + hiddenInput.value); // Debug log before submission
            // Add a small delay to ensure the value is set
            setTimeout(function() {
                console.log("Delayed check - isActive: " + hiddenInput.value);
            }, 100);
            return true; // Allow form submission
        };

        // Update value on checkbox change
        var checkbox = document.getElementById("isActiveCheckbox");
        checkbox.onchange = updateIsActive;
    });
function calculateRedeemedTotal() {
    console.log('calculateRedeemedTotal called');
    var redeemCheckbox = document.getElementById('redeemCheckbox');
    var totalAfterDiscountValue = document.getElementById('totalAfterDiscountValue');
    var loyaltyPointsValue = document.getElementById('loyaltyPointsValue');
    var redeemAmountDisplay = document.getElementById('redeemAmountDisplay');
    var finalTotalDisplay = document.getElementById('finalTotalDisplay');

	
	
    if (!redeemCheckbox || !totalAfterDiscountValue || !loyaltyPointsValue || 
        !redeemAmountDisplay || !finalTotalDisplay) {
        console.error('One or more elements are not found in the DOM');
        return;
    }

    try {
        var totalAfterDiscount = parseFloat(totalAfterDiscountValue.value) || 0;
        var totalPoints = parseFloat(loyaltyPointsValue.value) || 0;
        var redeemAmount = 0;
        var finalTotal = totalAfterDiscount;

        console.log('Initial Total Order Price:', totalAfterDiscount, 'Total Points:', totalPoints);

        if (redeemCheckbox.checked && totalPoints >= 2000) {
            redeemAmount = (Math.min(totalPoints * 0.1, totalAfterDiscount)) * 0.25;
            finalTotal = totalAfterDiscount - redeemAmount;
            finalTotal = Math.max(0, finalTotal);
            console.log('Checkbox checked - Redeem Amount:', redeemAmount, 'Final Total:', finalTotal);
        } else {
            console.log('Checkbox unchecked or insufficient points');
        }

        redeemAmountDisplay.textContent = redeemAmount > 0 ? `-$${redeemAmount.toFixed(2)}` : '$0.00';
        finalTotalDisplay.textContent = `$${finalTotal.toFixed(2)}`;
    } catch (e) {
        console.error('Error in calculateRedeemedTotal:', e);
    }
}
// Retry logic for dynamic checkbox availability
function retryForCheckbox() {
    var redeemCheckbox = document.getElementById('redeemCheckbox');
    
    // If the checkbox is found, stop retrying and call the calculation
    if (redeemCheckbox) {
        clearInterval(retryInterval);  // Stop retrying
        redeemCheckbox.addEventListener('change', calculateRedeemedTotal); // Attach change event listener
        console.log("Redeem Checkbox found and event listener added.");
        calculateRedeemedTotal(); // Perform an initial calculation
    }
}

// Retry every 100ms to check if the checkbox is available
var retryInterval = setInterval(retryForCheckbox, 100);


document.addEventListener('DOMContentLoaded', function() {
    // Check if the form exists on the page (to avoid errors on other pages)
    const form = document.querySelector('form[action="/admin/save-order-item-limit"]');
    if (!form) return;

    // Get today's date (April 22, 2025, for reference, but dynamically calculated)
    const today = new Date();
    const todayString = today.toISOString().split('T')[0]; // Format: YYYY-MM-DD

    // Set the minimum start date to today (disables past dates)
    const startDateInput = document.getElementById('startDate');
    if (startDateInput) {
        startDateInput.setAttribute('min', todayString);

        // Dynamically update the end date's minimum value based on the start date
        startDateInput.addEventListener('change', function() {
            const startDate = new Date(this.value);
            if (startDate) {
                const endDateInput = document.getElementById('endDate');
                // Set the minimum end date to the day after the start date
                const minEndDate = new Date(startDate);
                minEndDate.setDate(startDate.getDate() + 1);
                endDateInput.setAttribute('min', minEndDate.toISOString().split('T')[0]);
                
                // Reset the end date if it's before the new start date
                const currentEndDate = new Date(endDateInput.value);
                if (currentEndDate <= startDate) {
                    endDateInput.value = '';
                }
            }
        });
    }

    // Validate the end date when changed
    const endDateInput = document.getElementById('endDate');
    if (endDateInput) {
        endDateInput.addEventListener('change', function() {
            const startDate = new Date(startDateInput.value);
            const endDate = new Date(this.value);
            if (startDate && endDate <= startDate) {
                alert('End date must be after the start date.');
                this.value = '';
            }
        });
    }

    // Validate product ID (must be > 0)
    const productIdInput = document.getElementById('productId');
    const productIdError = document.getElementById('productIdError');
    if (productIdInput && productIdError) {
        productIdInput.addEventListener('input', function() {
            const value = parseInt(this.value);
            if (isNaN(value) || value <= 0) {
                productIdError.style.display = 'block';
                this.value = '';
            } else {
                productIdError.style.display = 'none';
            }
        });
    }

    // Validate quantity input (must be > 0)
    const limitQuantityInput = document.getElementById('limitQuantity');
    const quantityError = document.getElementById('quantityError');
    if (limitQuantityInput && quantityError) {
        limitQuantityInput.addEventListener('input', function() {
            const value = parseInt(this.value);
            if (isNaN(value) || value <= 0) {
                quantityError.style.display = 'block';
                this.value = '';
            } else {
                quantityError.style.display = 'none';
            }
        });
    }

    // Prevent form submission if product ID or quantity is invalid
    if (form) {
        form.addEventListener('submit', function(event) {
            const productId = parseInt(productIdInput.value);
            const quantity = parseInt(limitQuantityInput.value);
            if (isNaN(productId) || productId <= 0) {
                event.preventDefault();
                alert('Please enter a Product ID greater than 0.');
            }
            if (isNaN(quantity) || quantity <= 0) {
                event.preventDefault();
                alert('Please enter a quantity greater than 0.');
            }
        });
    }
});

document.addEventListener('DOMContentLoaded', function() {
    // Item Limit Form Validations
    const itemLimitForm = document.querySelector('form[action="/admin/save-order-item-limit"]');
    if (itemLimitForm) {
        // Get today's date (April 22, 2025, for reference, but dynamically calculated)
        const today = new Date();
        const todayString = today.toISOString().split('T')[0]; // Format: YYYY-MM-DD

        // Set the minimum start date to today (disables past dates)
        const startDateInput = document.getElementById('startDate');
        if (startDateInput) {
            startDateInput.setAttribute('min', todayString);

            // Dynamically update the end date's minimum value based on the start date
            startDateInput.addEventListener('change', function() {
                const startDate = new Date(this.value);
                if (startDate) {
                    const endDateInput = document.getElementById('endDate');
                    // Set the minimum end date to the day after the start date
                    const minEndDate = new Date(startDate);
                    minEndDate.setDate(startDate.getDate() + 1);
                    endDateInput.setAttribute('min', minEndDate.toISOString().split('T')[0]);
                    
                    // Reset the end date if it's before the new start date
                    const currentEndDate = new Date(endDateInput.value);
                    if (currentEndDate <= startDate) {
                        endDateInput.value = '';
                    }
                }
            });
        }

        // Validate the end date when changed
        const endDateInput = document.getElementById('endDate');
        if (endDateInput) {
            endDateInput.addEventListener('change', function() {
                const startDate = new Date(startDateInput.value);
                const endDate = new Date(this.value);
                if (startDate && endDate <= startDate) {
                    alert('End date must be after the start date.');
                    this.value = '';
                }
            });
        }

        // Validate product ID (must be > 0)
        const productIdInput = document.getElementById('productId');
        const productIdError = document.getElementById('productIdError');
        if (productIdInput && productIdError) {
            productIdInput.addEventListener('input', function() {
                const value = parseInt(this.value);
                if (isNaN(value) || value <= 0) {
                    productIdError.style.display = 'block';
                    this.value = '';
                } else {
                    productIdError.style.display = 'none';
                }
            });
        }

        // Validate quantity input (must be > 0)
        const limitQuantityInput = document.getElementById('limitQuantity');
        const quantityError = document.getElementById('quantityError');
        if (limitQuantityInput && quantityError) {
            limitQuantityInput.addEventListener('input', function() {
                const value = parseInt(this.value);
                if (isNaN(value) || value <= 0) {
                    quantityError.style.display = 'block';
                    this.value = '';
                } else {
                    quantityError.style.display = 'none';
                }
            });
        }

        // Prevent form submission if product ID or quantity is invalid
        itemLimitForm.addEventListener('submit', function(event) {
            const productId = parseInt(productIdInput.value);
            const quantity = parseInt(limitQuantityInput.value);
            if (isNaN(productId) || productId <= 0) {
                event.preventDefault();
                alert('Please enter a Product ID greater than 0.');
            }
            if (isNaN(quantity) || quantity <= 0) {
                event.preventDefault();
                alert('Please enter a quantity greater than 0.');
            }
        });
    }

    // Sale Form Validations
    const saleForm = document.querySelector('form[action="/admin/save-sale"]');
    if (saleForm) {
        // Get today's date (April 22, 2025, for reference, but dynamically calculated)
        const today = new Date();
        const todayString = today.toISOString().split('T')[0]; // Format: YYYY-MM-DD

        // Set the minimum start date to today (disables past dates)
        const startDateInput = document.getElementById('startDate');
        if (startDateInput) {
            startDateInput.setAttribute('min', todayString);

            // Dynamically update the end date's minimum value based on the start date
            startDateInput.addEventListener('change', function() {
                const startDate = new Date(this.value);
                if (startDate) {
                    const endDateInput = document.getElementById('endDate');
                    // Set the minimum end date to the day after the start date
                    const minEndDate = new Date(startDate);
                    minEndDate.setDate(startDate.getDate() + 1);
                    endDateInput.setAttribute('min', minEndDate.toISOString().split('T')[0]);
                    
                    // Reset the end date if it's before the new start date
                    const currentEndDate = new Date(endDateInput.value);
                    if (currentEndDate <= startDate) {
                        endDateInput.value = '';
                    }
                }
            });
        }

        // Validate the end date when changed
        const endDateInput = document.getElementById('endDate');
        if (endDateInput) {
            endDateInput.addEventListener('change', function() {
                const startDate = new Date(startDateInput.value);
                const endDate = new Date(this.value);
                if (startDate && endDate <= startDate) {
                    alert('End date must be after the start date.');
                    this.value = '';
                }
            });
        }

        // Validate discount percentage (must be > 0)
        const discountInput = document.getElementById('discountPercentage');
        const discountError = document.getElementById('discountError');
        if (discountInput && discountError) {
            discountInput.addEventListener('input', function() {
                const value = parseFloat(this.value);
                if (isNaN(value) || value <= 0) {
                    discountError.style.display = 'block';
                    this.value = '';
                } else {
                    discountError.style.display = 'none';
                }
            });
        }

        // Prevent form submission if discount percentage is invalid
        saleForm.addEventListener('submit', function(event) {
            const discount = parseFloat(discountInput.value);
            if (isNaN(discount) || discount <= 0) {
                event.preventDefault();
                alert('Please enter a discount percentage greater than 0.');
            }
        });
    }
});

$(function(){

// User Register validation

	var $userRegister=$("#userRegister");

	$userRegister.validate({
		
		rules:{
			name:{
				required:true,
				lettersonly:true
			}
			,
			email: {
				required: true,
				space: true,
				email: true
			},
			mobileNumber: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 10,
				maxlength: 12

			},
			password: {
				required: true,
				space: true

			},
			confirmpassword: {
				required: true,
				space: true,
				equalTo: '#pass'

			},
			address: {
				required: true,
				all: true

			},

			city: {
				required: true,
				space: true

			},
			state: {
				required: true,


			},
			pincode: {
				required: true,
				space: true,
				numericOnly: true

			}, img: {
				required: true,
			}
			
		},
		messages:{
			name:{
				required:'name required',
				lettersonly:'invalid name'
			},
			email: {
				required: 'email is required',
				space: 'space not allowed',
				email: 'Invalid email'
			},
			mobileNumber: {
				required: 'mob Number is required',
				space: 'space not allowed',
				numericOnly: 'invalid mob no',
				minlength: 'min 10 digit',
				maxlength: 'max 12 digit'
			},

			password: {
				required: 'password is required',
				space: 'space not allowed'

			},
			confirmpassword: {
				required: 'confirm password must match',
				space: 'space not allowed',
				equalTo: 'password mismatch'

			},
			address: {
				required: 'address is required',
				all: 'invalid'

			},

			city: {
				required: 'city is required',
				space: 'space not allowed'

			},
			state: {
				required: 'state is required',
				space: 'space not allowed'

			},
			pincode: {
				required: 'pincode is required',
				space: 'space not allowed',
				numericOnly: 'invalid pincode'

			},
			img: {
				required: 'image required',
			}
		}
	})
	
	
// Orders Validation

var $orders=$("#orders");

$orders.validate({
		rules:{
			firstName:{
				required:true,
				lettersonly:true
			},
			lastName:{
				required:true,
				lettersonly:true
			}
			,
			email: {
				required: true,
				space: true,
				email: true
			},
			mobileNo: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 10,
				maxlength: 12

			},
			address: {
				required: true,
				all: true

			},

			city: {
				required: true,
				space: true

			},
			state: {
				required: true,


			},
			pincode: {
				required: true,
				space: true,
				numericOnly: true

			},
			paymentType:{
			required: true
			}
		},
		messages:{
			firstName:{
				required:'first required',
				lettersonly:'invalid name'
			},
			lastName:{
				required:'last name required',
				lettersonly:'invalid name'
			},
			email: {
				required: 'email is required',
				space: 'space not allowed',
				email: 'Invalid email'
			},
			mobileNo: {
				required: 'mob is required',
				space: 'space not allowed',
				numericOnly: 'invalid mob no',
				minlength: 'min 10 digit',
				maxlength: 'max 12 digit'
			}
		   ,
			address: {
				required: 'address is required',
				all: 'invalid'

			},

			city: {
				required: 'city is required',
				space: 'space not allowed'

			},
			state: {
				required: 'state is required',
				space: 'space not allowed'

			},
			pincode: {
				required: 'pincode is required',
				space: 'space not allowed',
				numericOnly: 'invalid pincode'

			},
			paymentType:{
			required: 'select payment type'
			}
		}	
})
var $categoryForm = $("#categoryForm");

$categoryForm.validate
({
	rules :{
		name:{
			required : true
			},
		file:{
			required: true,
			extension :"jpg|jpeg|png|gif"
		}			
		},
		messages:{
		name: {
			required : "Please enter the category"
		},
		file : {
			required :"Please upload file"
		}
	}
})
var $productForm = $("#productForm"); 

$productForm.validate({
    rules: {
        title: {
            required: true,
            minlength: 3
        },
        description: {
            required: true,
            minlength: 5
        },
        category: {
            required: true
        },
        price: {
            required: true,
            number: true,
            min: 0.01
        },
        stock: {
            required: true,
            digits: true,
            min: 0
        },
        status: {
            required: true
        },
        image: {
            required: true,
            extension: "jpg|jpeg|png|gif"
        }
    },
    messages: {
        title: {
            required: "Title is required",
            minlength: "Title must be at least 3 characters long"
        },
        description: {
            required: "Description is required",
            minlength: "Description must be at least 5 characters long"
        },
        category: {
            required: "Category is required"
        },
        price: {
            required: "Price is required",
            number: "Enter a valid numeric value",
            min: "Price must be greater than 0"
        },
        stock: {
            required: "Stock quantity is required",
            digits: "Enter only whole numbers",
            min: "Please enter number only greater than -1"
        },
        status: {
            required: "Please select the product status"
        },
        image: {
            required: "Please upload an image",
            extension: "Only JPG, JPEG, PNG, and GIF formats are allowed"
        }
    },
    submitHandler: function (form) {
        alert("Form submitted successfully!");
        form.submit(); // Uncomment to enable form submission
    }
});


// Reset Password Validation

var $resetPassword=$("#resetPassword");

$resetPassword.validate({
		
		rules:{
			password: {
				required: true,
				space: true

			},
			confirmPassword: {
				required: true,
				space: true,
				equalTo: '#pass'

			}
		},
		messages:{
		   password: {
				required: 'password is required',
				space: 'space not allowed'

			},
			confirmpassword: {
				required: 'please confirm password',
				space: 'space not allowed',
				equalTo: 'password mismatch'

			}
		}	
})



jQuery.validator.addMethod('lettersonly', function(value, element) {
		return /^[^-\s][a-zA-Z_\s-]+$/.test(value);
	});
	
		jQuery.validator.addMethod('space', function(value, element) {
		return /^[^-\s]+$/.test(value);
	});

	jQuery.validator.addMethod('all', function(value, element) {
		return /^[^-\s][a-zA-Z0-9_,.\s-]+$/.test(value);
	});


	jQuery.validator.addMethod('numericOnly', function(value, element) {
		return /^[0-9]+$/.test(value);
	});
})