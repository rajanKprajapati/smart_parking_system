<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Booking Details #${booking.bookingReference} - Provider Portal"/>
</jsp:include>

<div class="container py-4">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/provider/bookings">Customer Bookings</a></li>
            <li class="breadcrumb-item active" aria-current="page">#${booking.bookingReference}</li>
        </ol>
    </nav>

    <div class="row justify-content-center">
        <div class="col-lg-8">
            <c:if test="${param.paymentUpdated == 'true'}">
                <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>Payment status and ledger reference updated successfully.
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>
            <div class="card p-4 p-md-5 border-0 shadow-sm sp-card rounded-4">
                <div class="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
                    <div>
                        <span class="badge bg-secondary px-3 py-1 rounded-pill mb-1">Reservation Audit</span>
                        <h3 class="fw-bold mb-0">Booking #${booking.bookingReference}</h3>
                    </div>
                    <div>
                        <c:choose>
                            <c:when test="${booking.confirmed}"><span class="badge badge-confirmed fs-6">CONFIRMED</span></c:when>
                            <c:when test="${booking.cancelled}"><span class="badge badge-cancelled fs-6">CANCELLED</span></c:when>
                            <c:when test="${booking.completed}"><span class="badge badge-completed fs-6">COMPLETED</span></c:when>
                            <c:when test="${booking.expired}"><span class="badge badge-expired fs-6">EXPIRED</span></c:when>
                        </c:choose>
                    </div>
                </div>

                <!-- Customer & Slot Info -->
                <div class="row g-4 mb-4">
                    <div class="col-md-6">
                        <div class="bg-light p-3 rounded-3 h-100 border">
                            <h6 class="fw-bold text-uppercase text-muted small mb-2"><i class="bi bi-person me-1"></i>Customer Information</h6>
                            <div class="fs-5 fw-bold text-dark">${booking.customerName}</div>
                            <div class="small text-muted mb-1"><i class="bi bi-envelope me-1"></i>${booking.customerEmail}</div>
                            <div class="small text-muted"><i class="bi bi-telephone me-1"></i>${booking.customerPhone}</div>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="bg-light p-3 rounded-3 h-100 border">
                            <h6 class="fw-bold text-uppercase text-muted small mb-2"><i class="bi bi-geo-alt me-1"></i>Facility & Bay</h6>
                            <div class="fs-5 fw-bold text-dark">${booking.parkingName}</div>
                            <div class="small text-muted mb-2">${booking.parkingLocation}</div>
                            <div>
                                <span class="badge bg-dark fs-6">Slot ${booking.slotNumber}</span>
                                <span class="badge bg-secondary fs-6">${booking.vehicleType}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Schedule & Financial Valuation -->
                <div class="card border rounded-3 p-3 mb-4">
                    <h6 class="fw-bold text-uppercase text-muted small mb-3"><i class="bi bi-calendar3 me-1"></i>Schedule & Financials</h6>
                    <div class="row g-3">
                        <div class="col-6 col-sm-3">
                            <span class="small text-muted d-block">Scheduled Arrival</span>
                            <strong class="text-dark small">${booking.startTime}</strong>
                        </div>
                        <div class="col-6 col-sm-3">
                            <span class="small text-muted d-block">Scheduled Expiry</span>
                            <strong class="text-dark small">${booking.endTime}</strong>
                        </div>
                        <div class="col-6 col-sm-3">
                            <span class="small text-muted d-block">Billable Duration</span>
                            <strong class="text-dark small">${booking.durationHours} Hours</strong>
                        </div>
                        <div class="col-6 col-sm-3">
                            <span class="small text-muted d-block">Calculated Valuation</span>
                            <strong class="text-success fs-5">$${booking.calculatedFee}</strong>
                        </div>
                    </div>
                </div>

                <!-- Technical Operational Note -->
                <div class="alert alert-warning small mb-4">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    <strong>System Operational Notice:</strong> A booking reaching its scheduled end time triggers automatic expiry notifications to driver and provider. It does not certify physical vehicle departure without dedicated ground IoT hardware or on-site barrier confirmation.
                </div>

                <!-- Payment Audit & Verification Workflow Section -->
                <div class="card border rounded-3 p-3 mb-4 bg-light">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h6 class="fw-bold text-uppercase text-muted small m-0">
                            <i class="bi bi-cash-coin text-primary me-1"></i>Payment Audit & Manual Verification
                        </h6>
                        <div>
                            <c:choose>
                                <c:when test="${booking.paid}"><span class="badge bg-success fs-6"><i class="bi bi-check-circle me-1"></i>PAID</span></c:when>
                                <c:when test="${booking.paymentFailed}"><span class="badge bg-danger fs-6"><i class="bi bi-x-circle me-1"></i>FAILED</span></c:when>
                                <c:otherwise><span class="badge bg-warning text-dark fs-6"><i class="bi bi-clock-history me-1"></i>PENDING VERIFICATION</span></c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-sm-4">
                            <span class="small text-muted d-block">Payment Method:</span>
                            <span class="badge bg-white text-dark border"><i class="bi bi-qr-code me-1"></i>${booking.paymentMethod}</span>
                        </div>
                        <div class="col-sm-4">
                            <span class="small text-muted d-block">UTR / Transaction Reference:</span>
                            <strong class="font-monospace text-dark">${not empty booking.paymentReference ? booking.paymentReference : 'None provided'}</strong>
                        </div>
                        <div class="col-sm-4">
                            <span class="small text-muted d-block">Paid Timestamp:</span>
                            <span class="small text-muted">${booking.paidAt != null ? booking.paidAt : 'Not settled yet'}</span>
                        </div>
                    </div>

                    <!-- Provider Action Form -->
                    <form method="post" action="${pageContext.request.contextPath}/provider/verify-payment" class="border-top pt-3">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="bookingId" value="${booking.bookingId}">

                        <div class="row g-2 align-items-center">
                            <div class="col-md-6">
                                <label class="form-label small text-muted m-0">Update/Confirm Bank UTR Reference (Optional):</label>
                                <input type="text" name="paymentReference" value="${booking.paymentReference}"
                                       class="form-control form-control-sm font-monospace" placeholder="Bank ledger UTR reference...">
                            </div>
                            <div class="col-md-6 text-md-end mt-2 mt-md-4">
                                <button type="submit" name="action" value="approve" class="btn btn-sm btn-success me-1">
                                    <i class="bi bi-check-circle-fill me-1"></i>Verify & Mark Paid
                                </button>
                                <button type="submit" name="action" value="reject" class="btn btn-sm btn-outline-danger">
                                    <i class="bi bi-x-circle me-1"></i>Mark Failed
                                </button>
                            </div>
                        </div>
                    </form>
                </div>

                <div class="text-end">
                    <a href="${pageContext.request.contextPath}/provider/bookings" class="btn btn-secondary px-4">
                        &larr; Back to Customer Bookings
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
