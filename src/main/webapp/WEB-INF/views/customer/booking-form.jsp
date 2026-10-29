<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Confirm Booking - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/browse">Browse</a></li>
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/parking-details?id=${area.parkingAreaId}">${area.parkingName}</a></li>
            <li class="breadcrumb-item active" aria-current="page">Reserve Slot</li>
        </ol>
    </nav>

    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card p-4 p-md-5 border-0 shadow-sm sp-card rounded-4">
                <div class="d-flex align-items-center justify-content-between border-bottom pb-3 mb-4">
                    <div>
                        <span class="badge bg-primary px-3 py-1 rounded-pill mb-1">Reservation Details</span>
                        <h3 class="fw-bold mb-0">Confirm Parking Reservation</h3>
                    </div>
                    <div class="text-end">
                        <span class="text-muted small d-block">Hourly Rate:</span>
                        <strong class="fs-5 text-primary">$${area.pricePerHour}/hr</strong>
                    </div>
                </div>

                <!-- Facility Summary Box -->
                <div class="bg-light p-3 rounded-3 mb-4 border">
                    <div class="row g-2">
                        <div class="col-md-7">
                            <h5 class="fw-bold mb-1">${area.parkingName}</h5>
                            <p class="text-muted small mb-0"><i class="bi bi-geo-alt-fill text-danger me-1"></i>${area.location}</p>
                        </div>
                        <div class="col-md-5 text-md-end small text-muted">
                            <div><i class="bi bi-clock me-1"></i>${area.operatingDetails}</div>
                            <div><i class="bi bi-person me-1"></i>Provider: <strong>${area.providerName}</strong></div>
                        </div>
                    </div>
                </div>

                <!-- Booking Form -->
                <form method="post" action="${pageContext.request.contextPath}/customer/book">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="areaId" value="${area.parkingAreaId}">
                    <input type="hidden" id="pricePerHour" value="${area.pricePerHour}">

                    <!-- Slot Selector -->
                    <div class="mb-4">
                        <label class="form-label fw-semibold">Select Parking Slot / Bay</label>
                        <select name="slotId" class="form-select form-select-lg" required>
                            <option value="">-- Choose an available slot --</option>
                            <c:forEach var="s" items="${availableSlots}">
                                <option value="${s.slotId}" ${slotId == s.slotId ? 'selected' : ''}>
                                    Slot ${s.slotNumber} (${s.vehicleType})
                                </option>
                            </c:forEach>
                        </select>
                        <div class="form-text small">Only active slots without overlapping reservations are listed.</div>
                    </div>

                    <!-- Time Interval -->
                    <div class="row g-3 mb-4">
                        <div class="col-md-6">
                            <label class="form-label fw-semibold">Arrival Date & Time (Start)</label>
                            <input type="datetime-local" class="form-control" name="startTime" id="startTime" value="${startTime}" required>
                            <div class="form-text small">Cannot be in the past.</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-semibold">Departure Date & Time (End)</label>
                            <input type="datetime-local" class="form-control" name="endTime" id="endTime" value="${endTime}" required>
                            <div class="form-text small">Must be later than arrival time.</div>
                        </div>
                    </div>

                    <!-- Dynamic Live Billing Calculation Display -->
                    <div class="card p-3 bg-primary bg-opacity-10 border-primary border-opacity-25 rounded-3 mb-4">
                        <div class="row align-items-center">
                            <div class="col-6">
                                <span class="small text-muted d-block">Estimated Billable Duration:</span>
                                <strong class="fs-6 text-dark" id="liveDurationDisplay">--</strong>
                            </div>
                            <div class="col-6 text-end">
                                <span class="small text-muted d-block">Estimated Parking Fee:</span>
                                <span class="fs-3 fw-bold text-primary" id="liveFeeDisplay">$${estimatedFee != null ? estimatedFee : '0.00'}</span>
                            </div>
                        </div>
                        <div class="mt-2 pt-2 border-top border-primary border-opacity-10 text-muted small">
                            <i class="bi bi-info-circle me-1"></i>
                            Billing policy: Calculated at $${area.pricePerHour}/hr (minimum 30 minutes). Fees are finalized by backend verification.
                        </div>
                    </div>

                    <!-- Important Disclaimers -->
                    <div class="alert alert-secondary small mb-4">
                        <div class="fw-semibold mb-1"><i class="bi bi-bell-fill text-warning me-1"></i>Automated Expiry Alerts:</div>
                        Upon reaching your scheduled departure time, the system will automatically dispatch SMS and Email alerts to both you and the parking provider to vacate or extend your slot.
                    </div>

                    <div class="d-grid gap-2">
                        <button type="submit" class="btn btn-primary btn-lg fw-semibold rounded-3 shadow-sm py-3">
                            <i class="bi bi-qr-code-scan me-1"></i>Proceed to QR Payment & Confirmation
                        </button>
                        <a href="${pageContext.request.contextPath}/parking-details?id=${area.parkingAreaId}"
                           class="btn btn-outline-secondary">
                            Cancel & Return
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
