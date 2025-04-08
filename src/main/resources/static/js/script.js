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
            redeemAmount = Math.min(totalPoints * 0.1, totalAfterDiscount);
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