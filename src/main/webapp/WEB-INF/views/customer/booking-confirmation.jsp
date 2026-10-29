<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Booking Confirmed - Smart Parking"/>
</jsp:include>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="card p-4 p-md-5 border-0 shadow-sm sp-card rounded-4 text-center">
                <div class="rounded-circle bg-success bg-opacity-10 text-success d-inline-flex align-items-center justify-content-center mx-auto mb-3" style="width: 75px; height: 75px;">
                    <i class="bi bi-check-lg display-5"></i>
                </div>

                <span class="badge bg-success px-3 py-1 rounded-pill mb-2 text-uppercase">Reservation Confirmed</span>
                <h2 class="fw-bold mb-1">Your Parking Slot is Reserved!</h2>
                <p class="text-muted small mb-4">A confirmation email and SMS have been triggered with your reservation details.</p>

                <!-- Booking Reference Card -->
                <div class="bg-light p-3 rounded-3 border mb-4">
                    <span class="text-muted small text-uppercase fw-semibold d-block">Booking Reference Code</span>
                    <span class="fs-2 fw-bolder font-monospace text-primary">${booking.bookingReference}</span>
                </div>

                <!-- Reservation Details Table -->
                <div class="card border rounded-3 p-3 mb-4 text-start">
                    <div class="row g-3">
                        <div class="col-sm-6">
                            <span class="text-muted small d-block">Parking Facility</span>
                            <strong class="text-dark">${booking.parkingName}</strong>
                            <div class="text-muted small">${booking.parkingLocation}</div>
                        </div>

                        <div class="col-sm-6 text-sm-end">
                            <span class="text-muted small d-block">Assigned Slot</span>
                            <span class="badge bg-dark fs-6">${booking.slotNumber}</span>
                            <span class="badge bg-secondary fs-6">${booking.vehicleType}</span>
                        </div>

                        <div class="col-12 border-top pt-2"></div>

                        <div class="col-sm-6">
                            <span class="text-muted small d-block">Arrival Time (Start)</span>
                            <strong class="text-dark">${booking.startTime}</strong>
                        </div>

                        <div class="col-sm-6 text-sm-end">
                            <span class="text-muted small d-block">Departure Time (End)</span>
                            <strong class="text-dark">${booking.endTime}</strong>
                        </div>

                        <div class="col-12 border-top pt-2"></div>

                        <div class="col-sm-6">
                            <span class="text-muted small d-block">Billable Duration</span>
                            <strong class="text-dark">${booking.durationHours} Hours</strong>
                        </div>

                        <div class="col-sm-6 text-sm-end">
                            <span class="text-muted small d-block">Total Amount</span>
                            <strong class="fs-4 text-primary">$${booking.calculatedFee}</strong>
                        </div>

                        <div class="col-12 border-top pt-2"></div>

                        <div class="col-sm-6">
                            <span class="text-muted small d-block">Payment Method & Status</span>
                            <span class="badge bg-light text-dark border me-1"><i class="bi bi-qr-code me-1"></i>${booking.paymentMethod}</span>
                            <c:choose>
                                <c:when test="${booking.paid}"><span class="badge bg-success">PAID</span></c:when>
                                <c:when test="${booking.paymentFailed}"><span class="badge bg-danger">FAILED</span></c:when>
                                <c:otherwise><span class="badge bg-warning text-dark">PENDING VERIFICATION</span></c:otherwise>
                            </c:choose>
                        </div>

                        <div class="col-sm-6 text-sm-end">
                            <span class="text-muted small d-block">Payment Reference / UTR</span>
                            <span class="font-monospace text-dark">${not empty booking.paymentReference ? booking.paymentReference : 'Awaiting Ledger Match'}</span>
                        </div>
                    </div>
                </div>

                <!-- Buttons -->
                <div class="d-flex flex-wrap justify-content-center gap-3">
                    <button type="button" class="btn btn-outline-secondary px-4 py-2" onclick="window.print()">
                        <i class="bi bi-printer me-1"></i>Print Receipt
                    </button>
                    <a href="${pageContext.request.contextPath}/customer/bookings" class="btn btn-primary px-4 py-2">
                        <i class="bi bi-calendar-check me-1"></i>View My Bookings
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
