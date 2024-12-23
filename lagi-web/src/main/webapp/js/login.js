$(document).ready(function () {
    authLoginCookie();
    document.getElementById('login-form').addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            submitLogin();
        }
    });
    document.getElementById('register-form').addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            submitRegister();
        }
    });
});

document.addEventListener('click', function (event) {
    const userMenu = document.getElementById('userMenu');
    const userMenu_sm = document.getElementById('userMenu-sm');
    const button = document.getElementById('headlessui-menu-button-:rc:');
    if (!button.contains(event.target)) {
        userMenu.classList.add('login-hidden');
        userMenu_sm.classList.add('login-hidden');
    }
});

function toggleUserMenu() {
    let auth = getCookie('lagi-auth');
    // auth = true;
    if (!auth) {
        openModal();
    } else {
        const userMenu = document.getElementById('userMenu');
        const userMenu_sm = document.getElementById('userMenu-sm');
        userMenu.classList.toggle('login-hidden');
        userMenu_sm.classList.toggle('login-hidden');
    }
}

function openModal() {
    document.getElementById('overlay').style.display = 'flex';
    showLoginPage();
}

function closeModal() {
    document.getElementById('overlay').style.display = 'none';
    resetForms();
}

function resetForms() {
    document.getElementById('login-username').value = '';
    document.getElementById('login-password').value = '';
    document.getElementById('login-captcha').value = '';
    document.getElementById('register-username').value = '';
    document.getElementById('register-password').value = '';
    document.getElementById('register-confirm-password').value = '';
    document.getElementById('register-captcha').value = '';
    document.getElementById('login-error').style.display = 'none';
    document.getElementById('register-error').style.display = 'none';
}

function showRegisterPage() {
    updateCaptcha(document.querySelector('.register-captcha-image'));
    document.getElementById('login-form').classList.add('login-hidden');
    document.getElementById('register-form').classList.remove('login-hidden');
}

function showLoginPage() {
    updateCaptcha(document.querySelector('.login-captcha-image'));
    document.getElementById('register-form').classList.add('login-hidden');
    document.getElementById('login-form').classList.remove('login-hidden');
}

function updateCaptcha(imageElement) {
    let codeHeight = Math.floor($('#login-captcha').innerHeight());
    if (codeHeight < 0) {
        codeHeight = Math.floor($('#register-captcha').innerHeight());
    }
    imageElement.src = '/user/getCaptcha?height=' + codeHeight + '&width=' + codeHeight * 2 +
        '&fontSize=22&t=' + new Date().getTime();
}

function authLoginCookie() {
    const auth = getCookie('lagi-auth');
    if (auth) {
        $.ajax({
            type: "POST",
            contentType: "application/json;charset=utf-8",
            url: "/user/authLoginCookie",
            data: JSON.stringify({"cookieValue": auth}),
            success: function (data) {
                if (data.status === 'success') {
                    $('#user_box').html(data.data.username);
                }
            },
            error: function (data) {
            }
        });
    }
}

function submitLogin() {
    const errorDiv = document.getElementById('login-error');
    const username = $('#login-username').val();
    const password = $('#login-password').val();
    const captcha = $('#login-captcha').val();
    if (!username || !password) {
        errorDiv.style.display = 'block';
        errorDiv.textContent = '请输入用户名和密码';
    } else if (captcha.length !== 4) {
        errorDiv.style.display = 'block';
        errorDiv.textContent = '请输入正确的验证码';
    } else {
        errorDiv.style.display = 'none';
        errorDiv.textConten = '登录中...';
    }
    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: "/user/login",
        data: JSON.stringify({"username": username, "password": password, "captcha": captcha}),
        success: function (data) {
            if (data.status === 'success') {
                $('#user_box').html(data.data.username);
                closeModal();
            } else {
                errorDiv.style.display = 'block';
                if (data.msg !== undefined) {
                    errorDiv.textContent = data.msg;
                } else {
                    errorDiv.textContent = '登录失败！';
                }
                updateCaptcha(document.querySelector('.login-captcha-image'));
            }
        },
        error: function (data) {
            errorDiv.style.display = 'block';
            errorDiv.textContent = '登录失败！';
            updateCaptcha(document.querySelector('.login-captcha-image'));
        }
    });
}

function deleteCookie(cname) {
    document.cookie = cname + '=; Max-Age=0; path=/; domain=' + location.hostname;
}

function logout() {
    deleteCookie('lagi-auth');
    deleteCookie('userId');
    window.location.reload();
}

function validateRegisterForm() {
    const username = document.getElementById('register-username').value.trim();
    const password = document.getElementById('register-password').value;
    const confirmPassword = document.getElementById('register-confirm-password').value;
    const captcha = document.getElementById('register-captcha').value.trim();
    const errorMessage = document.getElementById('register-error');

    errorMessage.style.display = 'none';

    const usernameRegex = /^[a-zA-Z0-9_-]+$/;
    if (username === '') {
        showRegisterError('用户名不能为空');
        return false;
    }
    if (username.length < 3 || username.length > 20) {
        showRegisterError('用户名长度必须在 3 到 20 个字符之间');
        return false;
    }
    if (!usernameRegex.test(username)) {
        showRegisterError('用户名只能包含大小写字母、数字、"-" 和 "_"');
        return false;
    }

    if (password === '') {
        showRegisterError('密码不能为空');
        return false;
    }
    if (password.length < 6) {
        showRegisterError('密码长度必须至少为 6 个字符');
        return false;
    }

    if (confirmPassword !== password) {
        showRegisterError('确认密码与密码不匹配');
        return false;
    }

    if (captcha === '') {
        showRegisterError('验证码不能为空');
        return false;
    }
    return true;
}

function showRegisterError(message) {
    const errorMessage = document.getElementById('register-error');
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
}

function submitRegister() {
    if (!validateRegisterForm()) {
        return;
    }
    const errorDiv = document.getElementById('register-error');
    const registerData = {
        "username": $('#register-username').val(),
        "password": $('#register-password').val(),
        "captcha": $('#register-captcha').val()
    };
    $.ajax({
        type: "POST",
        contentType: "application/json;charset=utf-8",
        url: "/user/register",
        data: JSON.stringify(registerData),
        success: function (data) {
            if (data.status === 'success') {
                authLoginCookie();
                closeModal();
            } else {
                errorDiv.style.display = 'block';
                errorDiv.textContent = data.msg;
                updateCaptcha(document.querySelector('.login-captcha-image'));
            }
        },
        error: function (data) {
            errorDiv.style.display = 'block';
            errorDiv.textContent = '注册失败！';
            updateCaptcha(document.querySelector('.login-captcha-image'));
        }
    });
}