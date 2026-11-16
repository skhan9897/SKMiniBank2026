<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Enterprise Account Opening | SK Mini Bank</title>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">

<style>
    body {
        margin: 0;
        padding: 0;
        min-height: 100vh;
        background: #0f0c29;
        font-family: 'Segoe UI', sans-serif;
        color: white;
    }

    .bg-container {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
        z-index: -1;
    }

    /* SVG RGB Waves */
    .wave-container {
        position: fixed;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 10vh;
        z-index: 0;
    }

    .waves {
        width: 100%;
        height: 100%;
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

    .reg-container {
        width: 100%;
        max-width: 920px;
        margin: 40px auto;
        z-index: 10;
        position: relative;
        padding: 0 20px;
    }

    .glass-card {
        background: rgba(255, 255, 255, 0.04);
        backdrop-filter: blur(25px);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 35px;
        padding: 45px;
        box-shadow: 0 40px 100px rgba(0,0,0,0.5);
    }

    /* Wizard Progress Bar */
    .wizard-progress {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 35px;
        position: relative;
        padding: 0 10px;
    }

    .step-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        position: relative;
        z-index: 2;
        cursor: pointer;
    }

    .step-circle {
        width: 38px;
        height: 38px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.1);
        border: 2px solid rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 13px;
        color: rgba(255, 255, 255, 0.5);
        transition: 0.4s;
    }

    .step-item.active .step-circle {
        background: #00d2ff;
        border-color: #00d2ff;
        color: #0f0c29;
        box-shadow: 0 0 20px rgba(0, 210, 255, 0.6);
    }

    .step-item.completed .step-circle {
        background: #20c997;
        border-color: #20c997;
        color: white;
    }

    .step-label {
        font-size: 10px;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin-top: 6px;
        color: rgba(255, 255, 255, 0.4);
    }

    .step-item.active .step-label { color: #00d2ff; }
    .step-item.completed .step-label { color: #20c997; }

    .section-header {
        color: #00d2ff;
        font-weight: 800;
        font-size: 13px;
        text-transform: uppercase;
        letter-spacing: 2px;
        margin-bottom: 25px;
        padding-bottom: 8px;
        border-bottom: 1px solid rgba(0, 210, 255, 0.2);
    }

    .form-control, .form-select {
        background: rgba(255, 255, 255, 0.05) !important;
        border: 1px solid rgba(255, 255, 255, 0.1) !important;
        border-radius: 14px;
        color: white !important;
        padding: 14px;
        transition: 0.3s;
    }

    .form-control:focus, .form-select:focus {
        background: rgba(255, 255, 255, 0.1) !important;
        border-color: #00d2ff !important;
        box-shadow: 0 0 20px rgba(0, 210, 255, 0.2);
    }

    .form-label {
        font-weight: 700;
        font-size: 11px;
        color: rgba(255,255,255,0.6);
        margin-bottom: 8px;
        text-transform: uppercase;
    }

    .btn-premium {
        background: linear-gradient(135deg, #00d2ff 0%, #3a7bd5 100%);
        border: none;
        border-radius: 14px;
        padding: 15px 30px;
        font-weight: 800;
        letter-spacing: 1.5px;
        box-shadow: 0 10px 25px rgba(0, 210, 255, 0.3);
        transition: 0.3s;
    }

    .btn-premium:hover {
        transform: translateY(-2px);
        box-shadow: 0 15px 35px rgba(0, 210, 255, 0.5);
    }

    .btn-outline-custom {
        background: transparent;
        border: 1px solid rgba(255, 255, 255, 0.2);
        color: rgba(255,255,255,0.7);
        border-radius: 14px;
        padding: 15px 30px;
        font-weight: 700;
    }

    .btn-outline-custom:hover {
        border-color: #00d2ff;
        color: white;
        background: rgba(255, 255, 255, 0.05);
    }

    .logo-area { text-align: center; margin-bottom: 30px; }
    .logo-area i { font-size: 55px; color: #00d2ff; margin-bottom: 10px; filter: drop-shadow(0 0 15px rgba(0, 210, 255, 0.4)); }
    .logo-area h2 { font-weight: 900; letter-spacing: 2px; margin-bottom: 4px; }

    .wizard-step-content { display: none; }
    .wizard-step-content.active-step { display: block; animation: fadeIn 0.4s ease; }

    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

    /* Branded Loading Overlay */
    #bankLoaderOverlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(15, 12, 41, 0.88);
        backdrop-filter: blur(15px);
        z-index: 9999;
        display: none;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }

    .loader-spinner {
        width: 75px;
        height: 75px;
        border: 4px solid rgba(0, 210, 255, 0.15);
        border-top-color: #00d2ff;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-bottom: 20px;
    }

    @keyframes spin { to { transform: rotate(360deg); } }

    .loader-text {
        font-size: 16px;
        font-weight: 700;
        letter-spacing: 2px;
        color: #00d2ff;
        text-transform: uppercase;
    }

    .status-badge {
        font-size: 11px;
        font-weight: 700;
        padding: 5px 12px;
        border-radius: 12px;
        display: inline-block;
    }
    .status-pending { background: rgba(255, 193, 7, 0.2); color: #ffc107; border: 1px solid rgba(ff, 193, 7, 0.4); }
    .status-verified { background: rgba(32, 201, 151, 0.2); color: #20c997; border: 1px solid rgba(32, 201, 151, 0.4); }
</style>
</head>
<body>

    <div class="bg-container"></div>
    <div class="wave-container">
        <svg class="waves" xmlns="http://www.w3.org/2000/svg" viewBox="0 24 150 28" preserveAspectRatio="none">
            <defs><path id="w" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z" /></defs>
            <g class="parallax">
                <use xlink:href="#w" x="48" y="0" />
                <use xlink:href="#w" x="48" y="3" />
                <use xlink:href="#w" x="48" y="5" />
                <use xlink:href="#w" x="48" y="7" />
            </g>
        </svg>
    </div>

    <!-- SK MINI BANK BRANDED LOADING OVERLAY -->
    <div id="bankLoaderOverlay">
        <div class="logo-area mb-3">
            <i class="fas fa-university"></i>
        </div>
        <div class="loader-spinner"></div>
        <div class="loader-text" id="loaderMessage">Processing...</div>
        <div class="text-white-50 small mt-2">SK MINI BANK SECURE DIGITAL ENGINE</div>
    </div>

    <div class="reg-container">
        <div class="glass-card shadow-lg">
            <div class="logo-area">
                <i class="fas fa-university"></i>
                <h2>JOIN SK MINI BANK</h2>
                <p class="text-white-50 small">10-Step Enterprise Account Opening Wizard</p>
            </div>

            <!-- Wizard Progress Indicator Bar -->
            <div class="wizard-progress">
                <div class="step-item active" id="stepIndicator1" onclick="jumpToStep(1)">
                    <div class="step-circle"><i class="fa fa-mobile-alt"></i></div>
                    <div class="step-label">Mobile</div>
                </div>
                <div class="step-item" id="stepIndicator2" onclick="jumpToStep(2)">
                    <div class="step-circle"><i class="fa fa-envelope"></i></div>
                    <div class="step-label">Email</div>
                </div>
                <div class="step-item" id="stepIndicator3" onclick="jumpToStep(3)">
                    <div class="step-circle"><i class="fa fa-user"></i></div>
                    <div class="step-label">Personal</div>
                </div>
                <div class="step-item" id="stepIndicator4" onclick="jumpToStep(4)">
                    <div class="step-circle"><i class="fa fa-id-card"></i></div>
                    <div class="step-label">Aadhaar</div>
                </div>
                <div class="step-item" id="stepIndicator5" onclick="jumpToStep(5)">
                    <div class="step-circle"><i class="fa fa-credit-card"></i></div>
                    <div class="step-label">PAN</div>
                </div>
                <div class="step-item" id="stepIndicator6" onclick="jumpToStep(6)">
                    <div class="step-circle"><i class="fa fa-upload"></i></div>
                    <div class="step-label">Upload</div>
                </div>
                <div class="step-item" id="stepIndicator7" onclick="jumpToStep(7)">
                    <div class="step-circle"><i class="fa fa-users"></i></div>
                    <div class="step-label">Nominee</div>
                </div>
                <div class="step-item" id="stepIndicator8" onclick="jumpToStep(8)">
                    <div class="step-circle"><i class="fa fa-piggy-bank"></i></div>
                    <div class="step-label">Account</div>
                </div>
                <div class="step-item" id="stepIndicator9" onclick="jumpToStep(9)">
                    <div class="step-circle"><i class="fa fa-check-double"></i></div>
                    <div class="step-label">Review</div>
                </div>
                <div class="step-item" id="stepIndicator10">
                    <div class="step-circle"><i class="fa fa-flag-checkered"></i></div>
                    <div class="step-label">Status</div>
                </div>
            </div>

            <div id="alertNotice"></div>

            <!-- STEP 1: MOBILE VERIFICATION -->
            <div class="wizard-step-content active-step" id="stepContent1">
                <div class="section-header">Step 1: Mobile Number Registration (AWS SNS)</div>
                <div class="mb-4">
                    <label class="form-label">Indian Mobile Number</label>
                    <div class="input-group">
                        <span class="input-group-text bg-transparent text-white border-secondary">+91</span>
                        <input type="text" id="inputMobile" class="form-control" maxlength="10" placeholder="10-Digit Mobile Number">
                        <button type="button" class="btn btn-outline-info" id="btnSendMobileOtp" onclick="sendMobileOtp()">SEND OTP</button>
                    </div>
                </div>
                <div class="mb-4" id="grpMobileOtp" style="display:none;">
                    <label class="form-label">Enter 6-Digit Mobile OTP</label>
                    <input type="text" id="inputMobileOtp" class="form-control mb-2" maxlength="6" placeholder="6-Digit OTP">
                    <button type="button" class="btn btn-success w-100" onclick="verifyMobileOtp()">VERIFY MOBILE OTP</button>
                </div>
                <div class="d-flex justify-content-end mt-4">
                    <button type="button" class="btn btn-premium" id="btnNext1" onclick="nextStep(2)" disabled>PROCEED TO EMAIL VERIFICATION <i class="fa fa-arrow-right ms-2"></i></button>
                </div>
            </div>

            <!-- STEP 2: EMAIL VERIFICATION -->
            <div class="wizard-step-content" id="stepContent2">
                <div class="section-header">Step 2: Email Registration (AWS SES)</div>
                <div class="mb-4">
                    <label class="form-label">Email Address</label>
                    <div class="input-group">
                        <input type="email" id="inputEmail" class="form-control" placeholder="example@skminibank.com">
                        <button type="button" class="btn btn-outline-info" id="btnSendEmailOtp" onclick="sendEmailOtp()">SEND OTP</button>
                    </div>
                </div>
                <div class="mb-4" id="grpEmailOtp" style="display:none;">
                    <label class="form-label">Enter 6-Digit Email OTP</label>
                    <input type="text" id="inputEmailOtp" class="form-control mb-2" maxlength="6" placeholder="6-Digit OTP">
                    <button type="button" class="btn btn-success w-100" onclick="verifyEmailOtp()">VERIFY EMAIL OTP</button>
                </div>
                <div class="d-flex justify-content-between mt-4">
                    <button type="button" class="btn btn-outline-custom" onclick="prevStep(1)"><i class="fa fa-arrow-left me-2"></i> BACK</button>
                    <button type="button" class="btn btn-premium" id="btnNext2" onclick="nextStep(3)" disabled>PROCEED TO PERSONAL DETAILS <i class="fa fa-arrow-right ms-2"></i></button>
                </div>
            </div>

            <!-- STEP 3: PERSONAL INFORMATION -->
            <div class="wizard-step-content" id="stepContent3">
                <div class="section-header">Step 3: Personal Information</div>
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Full Name *</label>
                        <input type="text" id="fullName" class="form-control" placeholder="Rahul Sharma">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Father's Name *</label>
                        <input type="text" id="fatherName" class="form-control">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Mother's Name *</label>
                        <input type="text" id="motherName" class="form-control">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Date of Birth *</label>
                        <input type="date" id="dob" class="form-control">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Gender *</label>
                        <select id="gender" class="form-select">
                            <option value="Male">Male</option>
                            <option value="Female">Female</option>
                            <option value="Other">Other</option>
                        </select>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Marital Status</label>
                        <select id="maritalStatus" class="form-select">
                            <option value="Single">Single</option>
                            <option value="Married">Married</option>
                        </select>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Occupation</label>
                        <input type="text" id="occupation" class="form-control" placeholder="Engineer, Businessman, etc.">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Residential Address *</label>
                        <input type="text" id="address" class="form-control" placeholder="House No, Street, Landmark">
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label">City *</label>
                        <input type="text" id="city" class="form-control">
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label">State *</label>
                        <input type="text" id="state" class="form-control">
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label">Pincode *</label>
                        <input type="text" id="pincode" class="form-control" maxlength="6">
                    </div>
                </div>
                <div class="d-flex justify-content-between mt-4">
                    <button type="button" class="btn btn-outline-custom" onclick="prevStep(2)"><i class="fa fa-arrow-left me-2"></i> BACK</button>
                    <button type="button" class="btn btn-premium" onclick="savePersonalDetails()">SAVE & PROCEED TO AADHAAR <i class="fa fa-arrow-right ms-2"></i></button>
                </div>
            </div>

            <!-- STEP 4: AADHAAR VERIFICATION -->
            <div class="wizard-step-content" id="stepContent4">
                <div class="section-header">Step 4: Authorized Aadhaar Verification</div>
                <div class="mb-4">
                    <label class="form-label">12-Digit Aadhaar Card Number</label>
                    <input type="text" id="aadhaarNumber" class="form-control mb-3" maxlength="12" placeholder="12-Digit Aadhaar Number">
                    <div class="form-check mb-3">
                        <input class="form-check-input" type="checkbox" id="aadhaarConsent" checked>
                        <label class="form-check-label text-white-50 small" for="aadhaarConsent">
                            I give voluntary consent to SK Mini Bank to authenticate my Aadhaar credentials via UIDAI authorized provider.
                        </label>
                    </div>
                    <button type="button" class="btn btn-info w-100" onclick="verifyAadhaar()">VERIFY AADHAAR WITH AUTHORIZED PROVIDER</button>
                    <div id="aadhaarStatusBox" class="mt-3"></div>
                </div>
                <div class="d-flex justify-content-between mt-4">
                    <button type="button" class="btn btn-outline-custom" onclick="prevStep(3)"><i class="fa fa-arrow-left me-2"></i> BACK</button>
                    <button type="button" class="btn btn-premium" onclick="nextStep(5)">PROCEED TO PAN VERIFICATION <i class="fa fa-arrow-right ms-2"></i></button>
                </div>
            </div>

            <!-- STEP 5: PAN CARD VERIFICATION -->
            <div class="wizard-step-content" id="stepContent5">
                <div class="section-header">Step 5: Authorized PAN Verification</div>
                <div class="mb-4">
                    <label class="form-label">10-Character PAN Card Number</label>
                    <input type="text" id="panNumber" class="form-control mb-3" maxlength="10" placeholder="ABCDE1234F" style="text-transform:uppercase;">
                    <button type="button" class="btn btn-info w-100" onclick="verifyPan()">VERIFY PAN WITH AUTHORIZED PROVIDER</button>
                    <div id="panStatusBox" class="mt-3"></div>
                </div>
                <div class="d-flex justify-content-between mt-4">
                    <button type="button" class="btn btn-outline-custom" onclick="prevStep(4)"><i class="fa fa-arrow-left me-2"></i> BACK</button>
                    <button type="button" class="btn btn-premium" onclick="nextStep(6)">PROCEED TO DOCUMENT UPLOAD <i class="fa fa-arrow-right ms-2"></i></button>
                </div>
            </div>

            <!-- STEP 6: DOCUMENT UPLOAD -->
            <div class="wizard-step-content" id="stepContent6">
                <div class="section-header">Step 6: Document Upload (AWS S3 Encrypted)</div>
                <div class="row">
                    <div class="col-md-6 mb-4">
                        <label class="form-label">Profile Photograph (JPG/PNG)</label>
                        <input type="file" id="filePhoto" class="form-control mb-2" accept="image/*">
                        <button type="button" class="btn btn-sm btn-outline-info w-100" onclick="uploadDoc('PHOTO', 'filePhoto')">Upload Photo</button>
                    </div>
                    <div class="col-md-6 mb-4">
                        <label class="form-label">Aadhaar Document (JPG/PNG/PDF)</label>
                        <input type="file" id="fileAadhaar" class="form-control mb-2" accept="image/*,application/pdf">
                        <button type="button" class="btn btn-sm btn-outline-info w-100" onclick="uploadDoc('AADHAAR', 'fileAadhaar')">Upload Aadhaar</button>
                    </div>
                    <div class="col-md-6 mb-4">
                        <label class="form-label">PAN Card (JPG/PNG/PDF)</label>
                        <input type="file" id="filePan" class="form-control mb-2" accept="image/*,application/pdf">
                        <button type="button" class="btn btn-sm btn-outline-info w-100" onclick="uploadDoc('PAN', 'filePan')">Upload PAN</button>
                    </div>
                    <div class="col-md-6 mb-4">
                        <label class="form-label">Specimen Signature (JPG/PNG)</label>
                        <input type="file" id="fileSig" class="form-control mb-2" accept="image/*">
                        <button type="button" class="btn btn-sm btn-outline-info w-100" onclick="uploadDoc('SIGNATURE', 'fileSig')">Upload Signature</button>
                    </div>
                </div>
                <div class="d-flex justify-content-between mt-4">
                    <button type="button" class="btn btn-outline-custom" onclick="prevStep(5)"><i class="fa fa-arrow-left me-2"></i> BACK</button>
                    <button type="button" class="btn btn-premium" onclick="nextStep(7)">PROCEED TO NOMINEE DETAILS <i class="fa fa-arrow-right ms-2"></i></button>
                </div>
            </div>

            <!-- STEP 7: NOMINEE DETAILS -->
            <div class="wizard-step-content" id="stepContent7">
                <div class="section-header">Step 7: Nominee Details</div>
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Nominee Name *</label>
                        <input type="text" id="nomineeName" class="form-control" placeholder="Priya Sharma">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Relationship *</label>
                        <select id="nomineeRel" class="form-select">
                            <option value="Spouse">Spouse</option>
                            <option value="Parent">Parent</option>
                            <option value="Child">Child</option>
                            <option value="Sibling">Sibling</option>
                        </select>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Nominee Mobile</label>
                        <input type="text" id="nomineeMobile" class="form-control" maxlength="10">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Nominee Date of Birth</label>
                        <input type="date" id="nomineeDob" class="form-control">
                    </div>
                </div>
                <div class="d-flex justify-content-between mt-4">
                    <button type="button" class="btn btn-outline-custom" onclick="prevStep(6)"><i class="fa fa-arrow-left me-2"></i> BACK</button>
                    <button type="button" class="btn btn-premium" onclick="nextStep(8)">PROCEED TO ACCOUNT DETAILS <i class="fa fa-arrow-right ms-2"></i></button>
                </div>
            </div>

            <!-- STEP 8: ACCOUNT TYPE & SELECTION -->
            <div class="wizard-step-content" id="stepContent8">
                <div class="section-header">Step 8: Account Details & Branch Selection</div>
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Account Type *</label>
                        <select id="accountType" class="form-select">
                            <option value="Savings Account">Savings Account (Zero Initial Deposit)</option>
                            <option value="Current Account">Current Account (Business Account)</option>
                        </select>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Select Home Branch *</label>
                        <select id="branch" class="form-select">
                            <option value="Bareilly Main Digital Branch">Bareilly Main Digital Branch (SKMB0001001)</option>
                            <option value="Delhi Central Branch">Delhi Central Branch (SKMB0001002)</option>
                            <option value="Mumbai Fort Branch">Mumbai Fort Branch (SKMB0001003)</option>
                        </select>
                    </div>
                </div>
                <div class="alert alert-info bg-transparent border-info text-info rounded-3 mt-3">
                    <i class="fa fa-info-circle me-2"></i> <b>Zero Initial Balance Notice:</b> Initial account balance starts at <b>₹0.00</b>. Balance only updates upon verified customer deposit transaction after Admin KYC approval.
                </div>
                <div class="d-flex justify-content-between mt-4">
                    <button type="button" class="btn btn-outline-custom" onclick="prevStep(7)"><i class="fa fa-arrow-left me-2"></i> BACK</button>
                    <button type="button" class="btn btn-premium" onclick="nextStep(9)">PROCEED TO FINAL REVIEW <i class="fa fa-arrow-right ms-2"></i></button>
                </div>
            </div>

            <!-- STEP 9: FINAL REVIEW & SUBMISSION -->
            <div class="wizard-step-content" id="stepContent9">
                <div class="section-header">Step 9: Final Application Review</div>
                <div class="p-4 rounded-4" style="background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.1);">
                    <div class="row">
                        <div class="col-md-6 mb-2"><b>Applicant Name:</b> <span id="revName">-</span></div>
                        <div class="col-md-6 mb-2"><b>Mobile Number:</b> <span id="revMobile">-</span></div>
                        <div class="col-md-6 mb-2"><b>Email Address:</b> <span id="revEmail">-</span></div>
                        <div class="col-md-6 mb-2"><b>Account Type:</b> <span id="revAccType">-</span></div>
                        <div class="col-md-6 mb-2"><b>Aadhaar Status:</b> <span id="revAadhaar">PENDING_CONFIG</span></div>
                        <div class="col-md-6 mb-2"><b>PAN Status:</b> <span id="revPan">PENDING_CONFIG</span></div>
                    </div>
                </div>
                <div class="form-check mt-4 mb-3">
                    <input class="form-check-input" type="checkbox" id="termsConsent" checked>
                    <label class="form-check-label text-white-50 small" for="termsConsent">
                        I declare that the information provided is accurate and I agree to the SK Mini Bank terms and conditions.
                    </label>
                </div>
                <div class="d-flex justify-content-between mt-4">
                    <button type="button" class="btn btn-outline-custom" onclick="prevStep(8)"><i class="fa fa-arrow-left me-2"></i> BACK</button>
                    <button type="button" class="btn btn-success btn-premium" onclick="submitApplication()">SUBMIT FINAL APPLICATION <i class="fa fa-paper-plane ms-2"></i></button>
                </div>
            </div>

            <!-- STEP 10: CONFIRMATION & LIVE TRACKING -->
            <div class="wizard-step-content" id="stepContent10">
                <div class="text-center py-4">
                    <i class="fa fa-circle-check text-success display-1 mb-3"></i>
                    <h3 class="fw-bold">APPLICATION SUBMITTED SUCCESSFULLY!</h3>
                    <p class="text-white-50">Your application reference has been generated and dispatched via SMS & Email.</p>

                    <div class="p-4 rounded-4 my-4" style="background: rgba(0, 210, 255, 0.1); border: 1px dashed #00d2ff;">
                        <span class="text-uppercase small text-info d-block mb-1">Application Reference Number</span>
                        <h2 class="fw-bold text-white mb-0" id="dispAppRef">SKM-APP-000000</h2>
                    </div>

                    <div class="mb-4">
                        <span class="status-badge status-pending" id="dispAppStatus">STATUS: PENDING_KYC_APPROVAL</span>
                    </div>

                    <p class="small text-white-50">Our Bank Administrators are reviewing your submitted documents. Upon approval, your account number will be generated and notified to you.</p>

                    <a href="login.jsp" class="btn btn-premium mt-3"><i class="fa fa-sign-in-alt me-2"></i> PROCEED TO CUSTOMER LOGIN</a>
                </div>
            </div>

        </div>
    </div>

    <script>
        let currentStepNum = 1;
        let verifiedMobile = '';
        let verifiedEmail = '';

        function showLoader(msg) {
            document.getElementById('loaderMessage').innerText = msg || 'Processing...';
            document.getElementById('bankLoaderOverlay').style.display = 'flex';
        }

        function hideLoader() {
            document.getElementById('bankLoaderOverlay').style.display = 'none';
        }

        function showAlert(msg, type) {
            const alertBox = document.getElementById('alertNotice');
            alertBox.innerHTML = '<div class="alert alert-' + type + ' border-0 bg-' + type + ' text-white rounded-3 mb-4 text-center" style="--bs-bg-opacity: .4;"><i class="fa fa-info-circle me-2"></i> ' + msg + '</div>';
        }

        function jumpToStep(step) {
            if (step > currentStepNum && step > 2 && (!verifiedMobile || !verifiedEmail)) {
                showAlert('Mobile and Email verifications are compulsory before proceeding.', 'danger');
                return;
            }
            document.querySelectorAll('.wizard-step-content').forEach(el => el.classList.remove('active-step'));
            document.querySelectorAll('.step-item').forEach((el, idx) => {
                if (idx + 1 < step) el.className = 'step-item completed';
                else if (idx + 1 === step) el.className = 'step-item active';
                else el.className = 'step-item';
            });
            document.getElementById('stepContent' + step).classList.add('active-step');
            currentStepNum = step;
        }

        function nextStep(step) { jumpToStep(step); }
        function prevStep(step) { jumpToStep(step); }

        function sendMobileOtp() {
            const mob = document.getElementById('inputMobile').value.trim();
            if (mob.length !== 10) { showAlert('Please enter a valid 10-digit mobile number.', 'danger'); return; }

            showLoader('Sending Mobile OTP via AWS SNS...');
            fetch('<%=request.getContextPath()%>/api/registration/mobile/send-otp', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'mobile=' + encodeURIComponent(mob)
            })
            .then(res => res.json())
            .then(data => {
                hideLoader();
                showAlert(data.message, data.status === 'success' ? 'success' : 'danger');
                if (data.status === 'success') {
                    document.getElementById('grpMobileOtp').style.display = 'block';
                }
            })
            .catch(() => { hideLoader(); showAlert('Error sending Mobile OTP.', 'danger'); });
        }

        function verifyMobileOtp() {
            const mob = document.getElementById('inputMobile').value.trim();
            const otp = document.getElementById('inputMobileOtp').value.trim();
            showLoader('Verifying Mobile OTP...');
            fetch('<%=request.getContextPath()%>/api/registration/mobile/verify-otp', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'mobile=' + encodeURIComponent(mob) + '&otp=' + encodeURIComponent(otp)
            })
            .then(res => res.json())
            .then(data => {
                hideLoader();
                showAlert(data.message, data.status === 'success' ? 'success' : 'danger');
                if (data.status === 'success') {
                    verifiedMobile = mob;
                    document.getElementById('btnNext1').disabled = false;
                }
            })
            .catch(() => { hideLoader(); showAlert('Error verifying Mobile OTP.', 'danger'); });
        }

        function sendEmailOtp() {
            const email = document.getElementById('inputEmail').value.trim();
            if (!email.includes('@')) { showAlert('Please enter a valid email address.', 'danger'); return; }

            showLoader('Sending Email OTP via AWS SES...');
            fetch('<%=request.getContextPath()%>/api/registration/email/send-otp', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'email=' + encodeURIComponent(email)
            })
            .then(res => res.json())
            .then(data => {
                hideLoader();
                showAlert(data.message, data.status === 'success' ? 'success' : 'danger');
                if (data.status === 'success') {
                    document.getElementById('grpEmailOtp').style.display = 'block';
                }
            })
            .catch(() => { hideLoader(); showAlert('Error sending Email OTP.', 'danger'); });
        }

        function verifyEmailOtp() {
            const email = document.getElementById('inputEmail').value.trim();
            const otp = document.getElementById('inputEmailOtp').value.trim();
            showLoader('Verifying Email OTP...');
            fetch('<%=request.getContextPath()%>/api/registration/email/verify-otp', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'email=' + encodeURIComponent(email) + '&otp=' + encodeURIComponent(otp)
            })
            .then(res => res.json())
            .then(data => {
                hideLoader();
                showAlert(data.message, data.status === 'success' ? 'success' : 'danger');
                if (data.status === 'success') {
                    verifiedEmail = email;
                    document.getElementById('btnNext2').disabled = false;
                }
            })
            .catch(() => { hideLoader(); showAlert('Error verifying Email OTP.', 'danger'); });
        }

        function savePersonalDetails() {
            const fn = document.getElementById('fullName').value.trim();
            if (!fn) { showAlert('Please enter your full name.', 'danger'); return; }

            showLoader('Saving Personal Information...');
            const body = new URLSearchParams();
            body.append('fullName', fn);
            body.append('fatherName', document.getElementById('fatherName').value.trim());
            body.append('motherName', document.getElementById('motherName').value.trim());
            body.append('dob', document.getElementById('dob').value);
            body.append('gender', document.getElementById('gender').value);
            body.append('maritalStatus', document.getElementById('maritalStatus').value);
            body.append('occupation', document.getElementById('occupation').value.trim());
            body.append('address', document.getElementById('address').value.trim());
            body.append('city', document.getElementById('city').value.trim());
            body.append('state', document.getElementById('state').value.trim());
            body.append('pincode', document.getElementById('pincode').value.trim());

            fetch('<%=request.getContextPath()%>/api/registration/personal-details', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: body.toString()
            })
            .then(res => res.json())
            .then(data => {
                hideLoader();
                if (data.status === 'success') {
                    nextStep(4);
                } else {
                    showAlert(data.message, 'danger');
                }
            })
            .catch(() => { hideLoader(); showAlert('Error saving personal details.', 'danger'); });
        }

        function verifyAadhaar() {
            const num = document.getElementById('aadhaarNumber').value.trim();
            showLoader('Verifying Aadhaar via Authorized Provider...');
            fetch('<%=request.getContextPath()%>/api/registration/aadhaar/verify', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'aadhaarNumber=' + encodeURIComponent(num)
            })
            .then(res => res.json())
            .then(data => {
                hideLoader();
                document.getElementById('aadhaarStatusBox').innerHTML = '<div class="alert alert-warning bg-transparent border-warning text-warning rounded-3 small"><i class="fa fa-info-circle me-1"></i> ' + data.message + '</div>';
            })
            .catch(() => { hideLoader(); });
        }

        function verifyPan() {
            const num = document.getElementById('panNumber').value.trim();
            showLoader('Verifying PAN via Authorized Provider...');
            fetch('<%=request.getContextPath()%>/api/registration/pan/verify', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'panNumber=' + encodeURIComponent(num) + '&fullName=' + encodeURIComponent(document.getElementById('fullName').value.trim())
            })
            .then(res => res.json())
            .then(data => {
                hideLoader();
                document.getElementById('panStatusBox').innerHTML = '<div class="alert alert-warning bg-transparent border-warning text-warning rounded-3 small"><i class="fa fa-info-circle me-1"></i> ' + data.message + '</div>';
            })
            .catch(() => { hideLoader(); });
        }

        function uploadDoc(type, fileInputId) {
            const input = document.getElementById(fileInputId);
            if (!input.files || !input.files[0]) { showAlert('Please select a file to upload.', 'danger'); return; }

            showLoader('Uploading Document to AWS S3 Private Storage...');
            const formData = new FormData();
            formData.append('documentType', type);
            formData.append('file', input.files[0]);

            fetch('<%=request.getContextPath()%>/api/registration/documents/upload', {
                method: 'POST',
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                hideLoader();
                showAlert(data.message, data.status === 'success' ? 'success' : 'danger');
            })
            .catch(() => { hideLoader(); showAlert('Document upload failed.', 'danger'); });
        }

        function submitApplication() {
            showLoader('Submitting Final Application & Publishing Outbox Event...');
            const body = new URLSearchParams();
            body.append('fullName', document.getElementById('fullName').value.trim());
            body.append('fatherName', document.getElementById('fatherName').value.trim());
            body.append('motherName', document.getElementById('motherName').value.trim());
            body.append('dob', document.getElementById('dob').value);
            body.append('gender', document.getElementById('gender').value);
            body.append('maritalStatus', document.getElementById('maritalStatus').value);
            body.append('occupation', document.getElementById('occupation').value.trim());
            body.append('aadhaar', document.getElementById('aadhaarNumber').value.trim());
            body.append('pan', document.getElementById('panNumber').value.trim());
            body.append('address', document.getElementById('address').value.trim());
            body.append('city', document.getElementById('city').value.trim());
            body.append('state', document.getElementById('state').value.trim());
            body.append('pincode', document.getElementById('pincode').value.trim());
            body.append('accountType', document.getElementById('accountType').value);
            body.append('branch', document.getElementById('branch').value);

            fetch('<%=request.getContextPath()%>/api/registration/submit', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: body.toString()
            })
            .then(res => res.json())
            .then(data => {
                hideLoader();
                if (data.status === 'success') {
                    document.getElementById('dispAppRef').innerText = data.applicationRef;
                    jumpToStep(10);
                } else {
                    showAlert(data.message, 'danger');
                }
            })
            .catch(() => { hideLoader(); showAlert('Error submitting application.', 'danger'); });
        }
    </script>

</body>
</html>
