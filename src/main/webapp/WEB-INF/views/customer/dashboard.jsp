<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Customer Dashboard - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <!-- Header -->
    <div class="card p-4 border-0 shadow-sm sp-card bg-primary text-white mb-4 rounded-4">
        <div class="row align-items-center">
            <div class="col-md-8">
                <span class="badge bg-light text-primary px-3 py-1 rounded-pill mb-2 fw-semibold">Customer Dashboard</span>
                <h2 class="fw-bold mb-1">Hello, ${sessionScope.loggedUser.fullName}!</h2>
                <p class="mb-0 opacity-75 small">Manage your current parking reservations, view receipts, and discover nearby parking slots.</p>
            </div>
            <div class="col-md-4 text-md-end mt-3 mt-md-0">
                <a href="${pageContext.request.contextPath}/browse" class="btn btn-light text-primary fw-semibold rounded-pill px-4">
                    <i class="bi bi-search me-1"></i>Find Parking Now
                </a>
            </div>
        </div>
    </div>

    <!-- Quick Stat / Nav Cards -->
    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="card p-3 border-0 shadow-sm sp-card d-flex flex-row align-items-center gap-3">
                <div class="rounded-circle bg-primary bg-opacity-10 text-primary p-3">
                    <i class="bi bi-calendar-check fs-3"></i>
                </div>
                <div>
                    <span class="text-muted small">My Reservations</span>
                    <h4 class="fw-bold mb-0">${recentBookings != null ? recentBookings.size() : 0} Total</h4>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card p-3 border-0 shadow-sm sp-card d-flex flex-row align-items-center gap-3">
                <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
                    <i class="bi bi-p-square fs-3"></i>
                </div>
                <div>
                    <span class="text-muted small">Available Facilities</span>
                    <h4 class="fw-bold mb-0">${availableAreas != null ? availableAreas.size() : 0} Open</h4>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card p-3 border-0 shadow-sm sp-card d-flex flex-row align-items-center gap-3">
                <div class="rounded-circle bg-warning bg-opacity-10 text-warning p-3">
                    <i class="bi bi-bell-fill fs-3"></i>
                </div>
                <div>
                    <span class="text-muted small">Expiry Tracking</span>
                    <h4 class="fw-bold mb-0 text-success">Active</h4>
                </div>
            </div>
        </div>
    </div>

    <!-- Recent Bookings Table -->
    <div class="card p-4 border-0 shadow-sm sp-card mb-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0"><i class="bi bi-clock-history text-primary me-2"></i>My Recent Reservations</h5>
            <a href="${pageContext.request.contextPath}/customer/bookings" class="small fw-semibold text-primary text-decoration-none">
                View All Bookings &rarr;
            </a>
        </div>

        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light small text-muted text-uppercase">
                    <tr>
                        <th>Reference</th>
                        <th>Facility & Slot</th>
                        <th>Start Time</th>
                        <th>End Time</th>
                        <th>Est. Fee</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty recentBookings}">
                            <c:forEach var="b" items="${recentBookings}">
                                <tr>
                                    <td>
                                        <span class="font-monospace fw-bold text-dark">${b.bookingReference}</span>
                                    </td>
                                    <td>
                                        <strong class="d-block text-dark">${b.parkingName}</strong>
                                        <span class="badge bg-secondary small">Slot: ${b.slotNumber}</span>
                                        <span class="badge bg-light text-dark border small">${b.vehicleType}</span>
                                    </td>
                                    <td class="small">${b.startTime}</td>
                                    <td class="small">${b.endTime}</td>
                                    <td class="fw-bold text-primary">$${b.calculatedFee}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${b.confirmed}"><span class="badge badge-confirmed">CONFIRMED</span></c:when>
                                            <c:when test="${b.cancelled}"><span class="badge badge-cancelled">CANCELLED</span></c:when>
                                            <c:when test="${b.completed}"><span class="badge badge-completed">COMPLETED</span></c:when>
                                            <c:when test="${b.expired}"><span class="badge badge-expired">EXPIRED</span></c:when>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/customer/confirmation?ref=${b.bookingReference}"
                                           class="btn btn-sm btn-outline-primary me-1" title="View Confirmation Receipt">
                                            <i class="bi bi-receipt"></i>
                                        </a>
                                        <c:if test="${b.eligibleForCancellation}">
                                            <form method="post" action="${pageContext.request.contextPath}/customer/cancel-booking"
                                                  class="d-inline" onsubmit="return confirmAction('Cancel this reservation? Your slot will be released.');">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="bookingId" value="${b.bookingId}">
                                                <input type="hidden" name="reason" value="Cancelled by driver">
                                                <button type="submit" class="btn btn-sm btn-outline-danger" title="Cancel Reservation">
                                                    <i class="bi bi-x-circle"></i>
                                                </button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" class="text-center py-4 text-muted">
                                    You have no parking reservations yet. <a href="${pageContext.request.contextPath}/browse">Browse parking facilities</a> to reserve a bay.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
