<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Scan & Pay - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/browse">Browse</a></li>
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/parking-details?id=${area.parkingAreaId}">${area.parkingName}</a></li>
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/customer/book?areaId=${area.parkingAreaId}&slotId=${slot.slotId}&startTime=${startTime}&endTime=${endTime}">Reserve</a></li>
            <li class="breadcrumb-item active" aria-current="page">QR Payment</li>
        </ol>
    </nav>

    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card p-4 p-md-5 border-0 shadow-sm sp-card rounded-4">
                <div class="d-flex align-items-center justify-content-between border-bottom pb-3 mb-4">
                    <div>
                        <span class="badge bg-primary px-3 py-1 rounded-pill mb-1">Step 2 of 2: UPI Payment</span>
                        <h3 class="fw-bold mb-0"><i class="bi bi-qr-code-scan text-primary me-2"></i>UPI QR Code Payment</h3>
                    </div>
                    <div class="text-end">
                        <span class="text-muted small d-block">Amount Due:</span>
                        <strong class="fs-4 text-success">$${totalFee}</strong>
                    </div>
                </div>

                <!-- Reservation Summary Box -->
                <div class="bg-light p-3 rounded-3 mb-4 border">
                    <div class="row g-2 align-items-center">
                        <div class="col-md-7">
                            <h5 class="fw-bold mb-1">${area.parkingName}</h5>
                            <p class="text-muted small mb-1"><i class="bi bi-geo-alt-fill text-danger me-1"></i>${area.location}</p>
                            <span class="badge bg-dark me-1">Slot ${slot.slotNumber} (${slot.vehicleType})</span>
                            <span class="badge bg-info text-dark">${durationHours} hrs @ $${area.pricePerHour}/hr</span>
                        </div>
                        <div class="col-md-5 text-md-end small text-muted">
                            <div><i class="bi bi-calendar-event me-1"></i>From: <strong>${formattedStart}</strong></div>
                            <div><i class="bi bi-calendar-check me-1"></i>To: <strong>${formattedEnd}</strong></div>
                            <div><i class="bi bi-person me-1"></i>Provider: <strong>${area.providerName}</strong></div>
                        </div>
                    </div>
                </div>

                <!-- Dynamic QR Code Display -->
                <div class="text-center py-3 px-2 bg-white rounded-3 border mb-4">
                    <span class="badge bg-light text-dark border px-3 py-1 rounded-pill mb-2">
                        <i class="bi bi-phone me-1"></i>Scan with any UPI App (GPay / PhonePe / Paytm / BHIM)
                    </span>

                    <div class="my-3 d-inline-block p-3 bg-white rounded shadow-sm border" style="max-width: 260px;">
                        ${qrSvg}
                    </div>

                    <div class="small text-muted mt-1">
                        <div>Payee VPA: <strong class="text-dark font-monospace">${payeeVpa}</strong></div>
                        <div>Merchant: <strong class="text-dark">${payeeName}</strong></div>
                        <div>Amount: <strong class="text-success fs-5 fw-bold">$${totalFee} (${currency})</strong></div>
                    </div>

                    <!-- Mobile Direct UPI Intent Button -->
                    <div class="mt-3 d-md-none">
                        <a href="${upiUri}" class="btn btn-outline-success btn-sm rounded-pill px-4">
                            <i class="bi bi-arrow-up-right-square me-1"></i>Open Directly in UPI App
                        </a>
                    </div>
                </div>

                <!-- Payment Verification Form -->
                <form method="post" action="${pageContext.request.contextPath}/customer/confirm-booking">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="areaId" value="${area.parkingAreaId}">
                    <input type="hidden" name="slotId" value="${slot.slotId}">
                    <input type="hidden" name="startTime" value="${startTime}">
                    <input type="hidden" name="endTime" value="${endTime}">
                    <input type="hidden" name="paymentMethod" value="UPI_QR">

                    <div class="mb-3">
                        <label class="form-label fw-semibold">
                            UPI Transaction ID / UTR Reference Number
                            <span class="text-muted fw-normal small">(From your payment confirmation screen)</span>
                        </label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-receipt text-primary"></i></span>
                            <input type="text" name="paymentReference" class="form-control font-monospace"
                                   placeholder="e.g. 430198765432 or UPI-TXN-12345" maxlength="50">
                        </div>
                        <div class="form-text small">
                            Entering your UTR reference helps the parking operator quickly match your settlement.
                        </div>
                    </div>

                    <!-- Payment Verification State Disclaimer (Mandatory Requirement) -->
                    <div class="alert alert-warning small d-flex align-items-start gap-2 mb-4">
                        <i class="bi bi-shield-lock-fill fs-5 text-warning flex-shrink-0 mt-1"></i>
                        <div>
                            <strong>Verification Policy:</strong> Scanning the QR code initiates a direct peer-to-merchant UPI transfer.
                            In compliance with secure transaction standards, your booking bay will be reserved immediately with payment status
                            <span class="badge bg-warning text-dark">PENDING</span> until verified against the merchant banking ledger.
                            Never share your UPI PIN or banking passwords with anyone.
                        </div>
                    </div>

                    <div class="d-grid gap-2">
                        <button type="submit" class="btn btn-success btn-lg fw-semibold rounded-3 shadow-sm py-3">
                            <i class="bi bi-check-circle-fill me-1"></i>Confirm Booking & Submit Payment
                        </button>
                        <a href="${pageContext.request.contextPath}/customer/book?areaId=${area.parkingAreaId}&slotId=${slot.slotId}&startTime=${startTime}&endTime=${endTime}"
                           class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left me-1"></i>Back to Change Time Window
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
