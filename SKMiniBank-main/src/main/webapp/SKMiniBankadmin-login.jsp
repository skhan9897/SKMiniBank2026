<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Admin Security Portal | SK Mini Bank</title>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">

<style>
    body {
        margin: 0;
        padding: 0;
        width: 100%;
        height: 100vh;
        display: flex;
        justify-content: center;
        align-items: center;
        background: #0f0c29;
        overflow: hidden;
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }

    .bg-container {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
        z-index: -1;
    }

    /* SVG RGB Waves */
    .wave-container {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 15vh;
        z-index: 1;
    }

    .waves {
        position: relative;
        width: 100%;
        height: 15vh;
        margin-bottom: -7px;
        min-height: 100px;
        max-height: 150px;
    }

    .parallax > use {
        animation: move-forever 25s cubic-bezier(.55, .5, .45, .5) infinite;
    }
    .parallax > use:nth-child(1) { animation-delay: -2s; animation-duration: 7s; fill: rgba(255, 255, 255, 0.1); }
    .parallax > use:nth-child(2) { animation-delay: -3s; animation-duration: 10s; fill: rgba(0, 210, 255, 0.1); }
    .parallax > use:nth-child(3) { animation-delay: -4s; animation-duration: 13s; fill: rgba(58, 123, 213, 0.1); }
    .parallax > use:nth-child(4) { animation-delay: -5s; animation-duration: 20s; fill: rgba(0, 210, 255, 0.3); }

    @keyframes move-forever {
        0% { transform: translate3d(-90px, 0, 0); }
        100% { transform: translate3d(85px, 0, 0); }
    }

    /* Glass Login Box */
    .login-box {
        width: 480px;
        background: rgba(255, 255, 255, 0.03);
        backdrop-filter: blur(25px);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 35px;
        padding: 45px 38px;
        box-shadow: 0 25px 60px rgba(0,0,0,0.6);
        color: white;
        z-index: 10;
        animation: slideUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
        text-align: center;
    }

    @keyframes slideUp {
        from { opacity: 0; transform: translateY(50px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .logo-area i {
        font-size: 55px;
        background: linear-gradient(135deg, #00d2ff, #3a7bd5);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        margin-bottom: 12px;
        filter: drop-shadow(0 0 10px rgba(0, 210, 255, 0.5));
    }

    .logo-area h2 {
        font-weight: 900;
        letter-spacing: 2px;
        margin-bottom: 5px;
        text-transform: uppercase;
    }

    .logo-area p {
        color: rgba(255,255,255,0.5);
        font-size: 13px;
        text-transform: uppercase;
        letter-spacing: 3px;
        margin-bottom: 25px;
    }

    .form-label {
        font-weight: 700;
        font-size: 11px;
        color: #00d2ff;
        text-transform: uppercase;
        letter-spacing: 1px;
        display: block;
        text-align: left;
        margin-bottom: 6px;
    }

    .input-wrapper {
        position: relative;
        margin-bottom: 20px;
    }

    .form-control {
        background: rgba(255, 255, 255, 0.05) !important;
        border: 1px solid rgba(255, 255, 255, 0.1) !important;
        height: 52px;
        border-radius: 15px;
        color: white !important;
        padding-left: 48px;
        transition: 0.4s;
    }

    .form-control::placeholder {
        color: rgba(255,255,255,0.3);
    }

    .form-control:focus {
        background: rgba(255, 255, 255, 0.1) !important;
        border-color: #00d2ff !important;
        box-shadow: 0 0 20px rgba(0, 210, 255, 0.2);
    }

    .input-icon {
        position: absolute;
        left: 18px;
        top: 50%;
        transform: translateY(-50%);
        color: rgba(255,255,255,0.4);
        font-size: 17px;
        z-index: 5;
    }

    .btn-login {
        height: 52px;
        background: linear-gradient(to right, #00d2ff, #3a7bd5);
        border: none;
        border-radius: 15px;
        font-weight: 800;
        font-size: 15px;
        letter-spacing: 1.5px;
        margin-top: 10px;
        box-shadow: 0 10px 25px rgba(58, 123, 213, 0.4);
        transition: 0.3s;
        text-transform: uppercase;
    }

    .btn-login:hover {
        transform: translateY(-2px);
        box-shadow: 0 15px 35px rgba(58, 123, 213, 0.6);
        filter: brightness(1.1);
    }

    .btn-home {
        background: transparent;
        border: 1px solid rgba(255,255,255,0.15);
        color: rgba(255,255,255,0.7);
        border-radius: 15px;
        height: 45px;
        font-weight: 600;
        margin-top: 15px;
        transition: 0.3s;
        text-decoration: none;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .btn-home:hover {
        background: rgba(255, 255, 255, 0.05);
        color: #fff;
        border-color: #00d2ff;
    }

    .mfa-badge {
        background: rgba(0, 210, 255, 0.15);
        border: 1px solid rgba(0, 210, 255, 0.4);
        color: #00d2ff;
        font-size: 12px;
        font-weight: 700;
        letter-spacing: 1.5px;
        padding: 6px 14px;
        border-radius: 20px;
        display: inline-block;
        margin-bottom: 20px;
        text-transform: uppercase;
    }

    .dest-info {
        font-size: 11px;
        color: rgba(255,255,255,0.6);
        text-align: left;
        margin-bottom: 4px;
    }

    .dest-info b {
        color: #fff;
    }

    .btn-resend {
        background: transparent;
        border: none;
        color: #00d2ff;
        font-size: 11px;
        font-weight: 600;
        cursor: pointer;
        padding: 0;
        margin-top: 4px;
        text-decoration: underline;
    }

    .btn-resend:disabled {
        color: rgba(255,255,255,0.3);
        cursor: not-allowed;
        text-decoration: none;
    }

    .footer-credit {
        position: absolute;
        bottom: 25px;
        width: 100%;
        text-align: center;
        color: rgba(255,255,255,0.2);
        font-size: 12px;
        letter-spacing: 2px;
        text-transform: uppercase;
        font-weight: 700;
    }

    .footer-credit b {
        color: rgba(0, 210, 255, 0.6);
    }
</style>
</head>
<body>

    <div class="bg-container"></div>

    <div class="wave-container">
        <svg class="waves" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
        viewBox="0 24 150 28" preserveAspectRatio="none" shape-rendering="auto">
        <defs>
        <path id="gentle-wave" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z" />
        </defs>
        <g class="parallax">
        <use xlink:href="#gentle-wave" x="48" y="0" />
        <use xlink:href="#gentle-wave" x="48" y="3" />
        <use xlink:href="#gentle-wave" x="48" y="5" />
        <use xlink:href="#gentle-wave" x="48" y="7" />
        </g>
        </svg>
    </div>

    <div class="login-box shadow">
        <div class="logo-area">
            <i class="fas fa-university"></i>
            <h2>SK MINI BANK</h2>
            <p>Admin Command Center</p>
        </div>

        <div id="alertBox">
        <%
        String error=(String)request.getAttribute("error");
        if(error!=null){
        %>
        <div class="alert alert-danger border-0 bg-danger text-white text-center rounded-3 mb-3" style="--bs-bg-opacity: .3; font-size: 13px;">
            <i class="fa fa-exclamation-triangle me-2"></i> <%=error%>
        </div>
        <%
        }
        %>
        </div>

        <%
        Boolean isMfaStep = Boolean.TRUE.equals(request.getAttribute("mfaStep"));
        if (!isMfaStep) {
        %>
        <!-- STEP 1: PASSWORD AUTHENTICATION -->
        <form id="loginForm" action="<%=request.getContextPath()%>/AdminLoginServlet" method="post">
            <div class="text-start mb-1">
                <label class="form-label">Admin ID / Username</label>
            </div>
            <div class="input-wrapper">
                <i class="fa fa-user-shield input-icon"></i>
                <input type="text" name="adminId" class="form-control" placeholder="Enter Admin ID" required autocomplete="username">
            </div>

            <div class="text-start mb-1">
                <label class="form-label">Admin Password</label>
            </div>
            <div class="input-wrapper">
                <i class="fa fa-lock input-icon"></i>
                <input type="password" name="password" class="form-control" placeholder="••••••••" required autocomplete="current-password">
            </div>

            <button type="submit" class="btn btn-primary w-100 btn-login shadow">
                VERIFY PASSWORD & PROCEED
            </button>

            <a href="SKMiniBank.jsp" class="btn btn-home w-100">
                <i class="fa fa-house-chimney me-2"></i> BACK TO TERMINAL
            </a>
        </form>
        <%
        } else {
            String maskedEmail = (String) request.getAttribute("maskedEmail");
            String maskedMobile = (String) request.getAttribute("maskedMobile");
        %>
        <!-- STEP 2: DUAL-OTP MULTI-FACTOR AUTHENTICATION -->
        <div class="mfa-badge mb-3">
            <i class="fa fa-shield-alt me-1"></i> STEP 2: MULTI-FACTOR AUTH (DUAL OTP)
        </div>

        <form id="mfaForm" onsubmit="handleMfaVerify(event)">
            <!-- Email OTP -->
            <div class="d-flex justify-content-between align-items-center mb-1">
                <label class="form-label mb-0">1. Email OTP</label>
                <span class="dest-info">Sent to: <b><%=maskedEmail != null ? maskedEmail : "***@***.com"%></b></span>
            </div>
            <div class="input-wrapper mb-2">
                <i class="fa fa-envelope-open-text input-icon"></i>
                <input type="text" id="emailOtp" maxlength="6" pattern="[0-9]{6}" class="form-control" placeholder="6-Digit Email OTP" required autocomplete="off">
            </div>
            <div class="text-end mb-3">
                <button type="button" id="btnResendEmail" class="btn-resend" onclick="resendOtp('email')">Resend Email OTP (<span id="timerEmail">60</span>s)</button>
            </div>

            <!-- Mobile OTP -->
            <div class="d-flex justify-content-between align-items-center mb-1">
                <label class="form-label mb-0">2. Mobile SMS OTP</label>
                <span class="dest-info">Sent to: <b><%=maskedMobile != null ? maskedMobile : "******"%></b></span>
            </div>
            <div class="input-wrapper mb-2">
                <i class="fa fa-comment-sms input-icon"></i>
                <input type="text" id="mobileOtp" maxlength="6" pattern="[0-9]{6}" class="form-control" placeholder="6-Digit Mobile OTP" required autocomplete="off">
            </div>
            <div class="text-end mb-3">
                <button type="button" id="btnResendMobile" class="btn-resend" onclick="resendOtp('mobile')">Resend Mobile OTP (<span id="timerMobile">60</span>s)</button>
            </div>

            <button type="submit" id="btnVerify" class="btn btn-primary w-100 btn-login shadow mt-2">
                VERIFY & ENTER ADMIN COMMAND CENTER
            </button>

            <a href="<%=request.getContextPath()%>/AdminLogoutServlet" class="btn btn-home w-100">
                <i class="fa fa-arrow-left me-2"></i> CANCEL & BACK TO LOGIN
            </a>
        </form>

        <script>
            let emailTimer = 60;
            let mobileTimer = 60;
            let emailInterval, mobileInterval;

            function startTimers() {
                document.getElementById('btnResendEmail').disabled = true;
                document.getElementById('btnResendMobile').disabled = true;

                emailInterval = setInterval(() => {
                    if (emailTimer <= 0) {
                        clearInterval(emailInterval);
                        document.getElementById('btnResendEmail').disabled = false;
                        document.getElementById('btnResendEmail').innerHTML = 'Resend Email OTP';
                    } else {
                        document.getElementById('timerEmail').innerText = --emailTimer;
                    }
                }, 1000);

                mobileInterval = setInterval(() => {
                    if (mobileTimer <= 0) {
                        clearInterval(mobileInterval);
                        document.getElementById('btnResendMobile').disabled = false;
                        document.getElementById('btnResendMobile').innerHTML = 'Resend Mobile OTP';
                    } else {
                        document.getElementById('timerMobile').innerText = --mobileTimer;
                    }
                }, 1000);
            }

            function resendOtp(type) {
                const btn = type === 'email' ? document.getElementById('btnResendEmail') : document.getElementById('btnResendMobile');
                btn.disabled = true;
                btn.innerHTML = 'Sending...';

                fetch('<%=request.getContextPath()%>/api/admin/otp/' + type + '/send', { method: 'POST' })
                    .then(res => res.json())
                    .then(data => {
                        showAlert(data.message, data.status === 'success' ? 'success' : 'danger');
                        if (type === 'email') {
                            emailTimer = 60;
                            document.getElementById('btnResendEmail').innerHTML = 'Resend Email OTP (<span id="timerEmail">60</span>s)';
                            emailInterval = setInterval(() => {
                                if (emailTimer <= 0) {
                                    clearInterval(emailInterval);
                                    document.getElementById('btnResendEmail').disabled = false;
                                    document.getElementById('btnResendEmail').innerHTML = 'Resend Email OTP';
                                } else {
                                    document.getElementById('timerEmail').innerText = --emailTimer;
                                }
                            }, 1000);
                        } else {
                            mobileTimer = 60;
                            document.getElementById('btnResendMobile').innerHTML = 'Resend Mobile OTP (<span id="timerMobile">60</span>s)';
                            mobileInterval = setInterval(() => {
                                if (mobileTimer <= 0) {
                                    clearInterval(mobileInterval);
                                    document.getElementById('btnResendMobile').disabled = false;
                                    document.getElementById('btnResendMobile').innerHTML = 'Resend Mobile OTP';
                                } else {
                                    document.getElementById('timerMobile').innerText = --mobileTimer;
                                }
                            }, 1000);
                        }
                    })
                    .catch(err => {
                        showAlert('Error sending OTP. Please try again.', 'danger');
                        btn.disabled = false;
                    });
            }

            function handleMfaVerify(e) {
                e.preventDefault();
                const emailOtp = document.getElementById('emailOtp').value.trim();
                const mobileOtp = document.getElementById('mobileOtp').value.trim();
                const btn = document.getElementById('btnVerify');

                if (emailOtp.length !== 6 || mobileOtp.length !== 6) {
                    showAlert('Please enter valid 6-digit OTPs for both Email and Mobile.', 'danger');
                    return;
                }

                btn.disabled = true;
                btn.innerHTML = '<i class="fa fa-spinner fa-spin me-2"></i> VERIFYING OTPS...';

                const body = new URLSearchParams();
                body.append('emailOtp', emailOtp);
                body.append('mobileOtp', mobileOtp);

                fetch('<%=request.getContextPath()%>/api/admin/otp/verify', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: body.toString()
                })
                .then(res => res.json())
                .then(data => {
                    if (data.status === 'success') {
                        showAlert('Dual MFA Verified! Redirecting to Command Center...', 'success');
                        setTimeout(() => {
                            window.location.href = data.redirect || '<%=request.getContextPath()%>/DashboardServlet';
                        }, 1000);
                    } else {
                        showAlert(data.message || 'OTP verification failed.', 'danger');
                        btn.disabled = false;
                        btn.innerHTML = 'VERIFY & ENTER ADMIN COMMAND CENTER';
                    }
                })
                .catch(err => {
                    showAlert('Network error during verification.', 'danger');
                    btn.disabled = false;
                    btn.innerHTML = 'VERIFY & ENTER ADMIN COMMAND CENTER';
                });
            }

            function showAlert(msg, type) {
                const box = document.getElementById('alertBox');
                box.innerHTML = '<div class="alert alert-' + type + ' border-0 bg-' + type + ' text-white text-center rounded-3 mb-3" style="--bs-bg-opacity: .3; font-size: 13px;"><i class="fa fa-info-circle me-2"></i> ' + msg + '</div>';
            }

            window.onload = startTimers;
        </script>
        <%
        }
        %>
    </div>

    <div class="footer-credit">
        © 2026 SK MINI BANK | Developed By <b>Sajid Khan</b>
    </div>

</body>
</html>
