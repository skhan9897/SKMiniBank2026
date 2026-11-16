<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Universal Multi-Bank Transfer - SK Bank of Bareilly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body class="bg-light">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow">
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0">Universal Multi-Bank, Mobile & UPI Transfer</h4>
                    </div>
                    <div class="card-body">
                        <form id="transferForm">
                            <div class="mb-3">
                                <label class="form-label">Transfer / Verification Mode</label>
                                <select class="form-select" id="transferType" name="transferType">
                                    <option value="ACCOUNT">Bank Account & IFSC</option>
                                    <option value="MOBILE">Mobile Number & Bank</option>
                                    <option value="UPI">UPI ID (VPA)</option>
                                </select>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Select Recipient Bank</label>
                                <select class="form-select" id="recipientBankId" name="recipientBankId" required>
                                    <option value="">Loading supported banks...</option>
                                </select>
                            </div>

                            <div id="accountSection">
                                <div class="mb-3">
                                    <label class="form-label">Recipient Account Number</label>
                                    <input type="text" class="form-control" id="accountNumber" placeholder="Enter account number">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">IFSC Code</label>
                                    <input type="text" class="form-control" id="ifsc" placeholder="e.g. SKB0000001">
                                </div>
                                <button type="button" class="btn btn-secondary mb-3" id="verifyAccountBtn">Verify Account & Bank</button>
                            </div>

                            <div id="mobileSection" style="display: none;">
                                <div class="mb-3">
                                    <label class="form-label">Recipient Mobile Number</label>
                                    <input type="text" class="form-control" id="mobileNumber" placeholder="Enter 10-digit mobile number">
                                </div>
                                <button type="button" class="btn btn-secondary mb-3" id="verifyMobileBtn">Verify Mobile & Bank</button>
                            </div>

                            <div id="upiSection" style="display: none;">
                                <div class="mb-3">
                                    <label class="form-label">UPI ID (VPA)</label>
                                    <input type="text" class="form-control" id="upiId" placeholder="username@bank">
                                </div>
                                <button type="button" class="btn btn-secondary mb-3" id="verifyUpiBtn">Verify UPI ID</button>
                            </div>

                            <div id="verificationResult" class="alert alert-info d-none"></div>

                            <div class="mb-3">
                                <label class="form-label">Resolved Account / VPA</label>
                                <input type="text" class="form-control" id="resolvedAccountOrVpa" readonly>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Recipient Verified Name</label>
                                <input type="text" class="form-control" id="recipientName" readonly required>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Amount (₹)</label>
                                <input type="number" step="0.01" class="form-control" id="amount" required placeholder="Enter amount">
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Idempotency Key (Unique Transaction Token)</label>
                                <input type="text" class="form-control" id="idempotencyKey" value="IDEMP-<%= System.currentTimeMillis() %>">
                            </div>

                            <button type="submit" class="btn btn-primary w-100" id="submitTransferBtn" disabled>Proceed & Pay Securely</button>
                        </form>

                        <div id="transferResult" class="mt-3"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        $(document).ready(function() {
            // Load supported banks
            $.get('/api/master/banks', function(data) {
                if (data.success && data.banks) {
                    let options = '<option value="">Select Recipient Bank</option>';
                    data.banks.forEach(function(bank) {
                        options += '<option value="' + bank.bank_id + '">' + bank.bank_name + ' (' + bank.routing_prefix + ')</option>';
                    });
                    $('#recipientBankId').html(options);
                }
            });

            $('#transferType').change(function() {
                let val = $(this).val();
                $('#accountSection, #mobileSection, #upiSection').hide();
                if (val === 'ACCOUNT') {
                    $('#accountSection').show();
                } else if (val === 'MOBILE') {
                    $('#mobileSection').show();
                } else if (val === 'UPI') {
                    $('#upiSection').show();
                }
                $('#verificationResult').addClass('d-none');
                $('#recipientName').val('');
                $('#resolvedAccountOrVpa').val('');
                $('#submitTransferBtn').prop('disabled', true);
            });

            $('#verifyAccountBtn').click(function() {
                let bankId = $('#recipientBankId').val();
                let accNo = $('#accountNumber').val();
                let ifsc = $('#ifsc').val();

                if (!bankId || !accNo) {
                    alert('Please select recipient bank and enter account number.');
                    return;
                }

                $.ajax({
                    url: '/api/beneficiaries/verify-account',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify({ bank_id: bankId, account_number: accNo, ifsc: ifsc }),
                    success: function(resp) {
                        let alertBox = $('#verificationResult');
                        alertBox.removeClass('d-none alert-danger alert-success');
                        if (resp.success) {
                            alertBox.addClass('alert-success').text('Verified Account in ' + bankId + ': ' + resp.beneficiary_name);
                            $('#recipientName').val(resp.beneficiary_name);
                            $('#resolvedAccountOrVpa').val(accNo);
                            $('#submitTransferBtn').prop('disabled', false);
                        } else {
                            alertBox.addClass('alert-danger').text('Verification Failed: ' + resp.message);
                            $('#submitTransferBtn').prop('disabled', true);
                        }
                    }
                });
            });

            $('#verifyMobileBtn').click(function() {
                let bankId = $('#recipientBankId').val();
                let mobile = $('#mobileNumber').val();

                if (!bankId || !mobile) {
                    alert('Please select recipient bank and enter mobile number.');
                    return;
                }

                $.ajax({
                    url: '/api/beneficiaries/verify-mobile',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify({ bank_id: bankId, mobile: mobile }),
                    success: function(resp) {
                        let alertBox = $('#verificationResult');
                        alertBox.removeClass('d-none alert-danger alert-success');
                        if (resp.success) {
                            alertBox.addClass('alert-success').text('Verified Mobile Account in ' + bankId + ': ' + resp.beneficiary_name + ' (A/C: ' + resp.account_number + ')');
                            $('#recipientName').val(resp.beneficiary_name);
                            $('#resolvedAccountOrVpa').val(resp.account_number);
                            $('#submitTransferBtn').prop('disabled', false);
                        } else {
                            alertBox.addClass('alert-danger').text('Mobile Verification Failed: ' + resp.message);
                            $('#submitTransferBtn').prop('disabled', true);
                        }
                    }
                });
            });

            $('#verifyUpiBtn').click(function() {
                let upiId = $('#upiId').val();
                if (!upiId) {
                    alert('Please enter UPI ID.');
                    return;
                }

                $.ajax({
                    url: '/api/beneficiaries/verify-upi',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify({ upi_id: upiId }),
                    success: function(resp) {
                        let alertBox = $('#verificationResult');
                        alertBox.removeClass('d-none alert-danger alert-success');
                        if (resp.success) {
                            alertBox.addClass('alert-success').text('Verified UPI Payee: ' + resp.payee_name);
                            $('#recipientName').val(resp.payee_name);
                            $('#resolvedAccountOrVpa').val(upiId);
                            $('#submitTransferBtn').prop('disabled', false);
                        } else {
                            alertBox.addClass('alert-danger').text('UPI Verification Failed: ' + resp.message);
                            $('#submitTransferBtn').prop('disabled', true);
                        }
                    }
                });
            });

            $('#transferForm').submit(function(e) {
                e.preventDefault();
                let transferType = $('#transferType').val();
                let payload = {
                    transfer_type: transferType,
                    recipient_bank_id: $('#recipientBankId').val(),
                    recipient_account_or_vpa: $('#resolvedAccountOrVpa').val(),
                    recipient_name: $('#recipientName').val(),
                    amount: parseFloat($('#amount').val()),
                    idempotency_key: $('#idempotencyKey').val()
                };

                $.ajax({
                    url: '/api/payments/transfer',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(payload),
                    success: function(resp) {
                        let resBox = $('#transferResult');
                        if (resp.success) {
                            resBox.html('<div class="alert alert-success">Transfer Successful!<br>Reference: ' + resp.transaction_reference + '<br>Status: ' + resp.status + '<br>Remaining Balance: ₹' + resp.remaining_balance + '</div>');
                        } else {
                            resBox.html('<div class="alert alert-danger">Transfer Failed: ' + resp.message + '</div>');
                        }
                    }
                });
            });
        });
    </script>
</body>
</html>
