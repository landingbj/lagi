$(document).ready(function() {
    $("#registerForm").submit(function(event) {
        event.preventDefault();

        var username = $("#username").val().trim();
        var company = $("#company").val().trim();
        var phoneNumber = $("#phoneNumber").val().trim();
        var code = $("#code").val().trim();
        if (username === '' || company === '' ||phoneNumber === '' ||code === '') {
    		phoneError.textContent = '输入内容不能为空！';
    		phoneError.style.display = 'block';
    		return;
        }
        
        var formData = {
            username: $("#username").val().trim(),
            company: $("#company").val(),
            phoneNumber: $("#phoneNumber").val(),
            code: $("#code").val()        };

        $.ajax({
            type: "POST",
            url: "account/register",
            data: JSON.stringify(formData),
            contentType: "application/json",
            dataType: "json",
            success: function(response) {
            	if (response.status === 'success') {
            		location.href = 'integrate/report.jsp?categoryName=demoll2&channelId=437';
            	} else {
                	const phoneError = document.getElementById('phoneError');
            		phoneError.textContent = response.msg;
            		phoneError.style.display = 'block';
            	}
            },
            error: function(error) {
            }
        });
    });
    
    document.getElementById('code').addEventListener('input', function (e) {
    	e.target.value = e.target.value.replace(/[^0-9]/g, '');
    });
    
    document.getElementById('phoneNumber').addEventListener('input', function (e) {
    	e.target.value = e.target.value.replace(/[^0-9]/g, '');
    });
});


function sendVerificationCode(e) {
	var phoneNumber = document.getElementById('phoneNumber').value;
	const phoneError = document.getElementById('phoneError');
	
	const phonePattern = /^1[3-9]\d{9}$/;
	
	if (!phonePattern.test(phoneNumber)) {
		e.preventDefault();
		phoneError.textContent = '请输入有效的中国手机号码';
		phoneError.style.display = 'block';
		return;
	} else {
		phoneError.style.display = 'none';
	}
	
    const sendCodeBtn = document.getElementById('sendCodeBtn');
    sendCodeBtn.disabled = true;
    var originalText = sendCodeBtn.innerHTML;
    sendCodeBtn.innerHTML = "60s";
    var seconds = 60;
    const countdown = setInterval(function () {
        seconds--;
        sendCodeBtn.innerHTML = seconds + 's';

        if (seconds <= 0) {
            clearInterval(countdown);
            sendCodeBtn.disabled = false;
            sendCodeBtn.innerHTML = originalText;  // 倒计时结束后恢复按钮原始文本
        }
    }, 1000);
    
    phoneNumber = $("#phoneNumber").val();
    var formData = {
		'phoneNumber': phoneNumber,
        'type': 1,      
    };
    
    $.ajax({
        type: "POST",
        url: "account/sendSms",
        data: JSON.stringify(formData),
        contentType: "application/json",
        dataType: "json",
        success: function(response) {
        	console.log(response);
        	if (response.status === 'success') {
        	} else {
        		const phoneError = document.getElementById('phoneError');
        		phoneError.textContent = response.msg;
        		phoneError.style.display = 'block';
        	}
        },
        error: function(error) {
        }
    });
}
