<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Provider Dashboard - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
        <div>
            <span class="badge bg-dark px-3 py-1 rounded-pill mb-1 text-uppercase">Facility Management Portal</span>
            <h2 class="fw-bold mb-0">Provider Dashboard</h2>
            <p class="text-muted small m-0">Live operations, inventory capacity, booking lifecycle, and financial valuation.</p>
        </div>
        <div class="mt-3 mt-md-0 d-flex gap-2">
            <a href="${pageContext.request.contextPath}/provider/areas" class="btn btn-outline-primary">
                <i class="bi bi-building me-1"></i>My Facilities
            </a>
            <a href="${pageContext.request.contextPath}/provider/slots" class="btn btn-primary">
                <i class="bi bi-plus-circle me-1"></i>Manage Slots
            </a>
        </div>
    </div>

    <!-- Analytics Stat Cards Grid (Section 12) -->
    <div class="row g-3 mb-4">
        <!-- Total Slots -->
        <div class="col-6 col-md-3">
            <div class="card p-3 border-0 shadow-sm sp-stat-card bg-white">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-muted small d-block">Total Slots</span>
                        <h3 class="fw-bold mb-0 text-dark">${stats.totalSlots}</h3>
                    </div>
                    <div class="rounded-circle bg-primary bg-opacity-10 text-primary p-3">
                        <i class="bi bi-grid-3x3-gap-fill fs-4"></i>
                    </div>
                </div>
                <div class="mt-2 small text-muted">Across ${stats.totalParkingAreas} facilities</div>
            </div>
        </div>

        <!-- Available Slots -->
        <div class="col-6 col-md-3">
            <div class="card p-3 border-0 shadow-sm sp-stat-card bg-white">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-muted small d-block">Available Now</span>
                        <h3 class="fw-bold mb-0 text-success">${stats.availableSlots}</h3>
                    </div>
                    <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
                        <i class="bi bi-check-circle-fill fs-4"></i>
                    </div>
                </div>
                <div class="mt-2 small text-muted">Ready for instant booking</div>
            </div>
        </div>

        <!-- Active Bookings -->
        <div class="col-6 col-md-3">
            <div class="card p-3 border-0 shadow-sm sp-stat-card bg-white">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-muted small d-block">Active Currently</span>
                        <h3 class="fw-bold mb-0 text-info">${stats.activeBookings}</h3>
                    </div>
                    <div class="rounded-circle bg-info bg-opacity-10 text-info p-3">
                        <i class="bi bi-car-front-fill fs-4"></i>
                    </div>
                </div>
                <div class="mt-2 small text-muted">Parked in facility</div>
            </div>
        </div>

        <!-- Upcoming Bookings -->
        <div class="col-6 col-md-3">
            <div class="card p-3 border-0 shadow-sm sp-stat-card bg-white">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-muted small d-block">Upcoming Bookings</span>
                        <h3 class="fw-bold mb-0 text-primary">${stats.upcomingBookings}</h3>
                    </div>
                    <div class="rounded-circle bg-primary bg-opacity-10 text-primary p-3">
                        <i class="bi bi-clock-history fs-4"></i>
                    </div>
                </div>
                <div class="mt-2 small text-muted">Future reservations</div>
            </div>
        </div>

        <!-- Completed Bookings -->
        <div class="col-6 col-md-3">
            <div class="card p-3 border-0 shadow-sm sp-stat-card bg-white">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-muted small d-block">Completed</span>
                        <h3 class="fw-bold mb-0 text-secondary">${stats.completedBookings}</h3>
                    </div>
                    <div class="rounded-circle bg-secondary bg-opacity-10 text-secondary p-3">
                        <i class="bi bi-flag-fill fs-4"></i>
                    </div>
                </div>
                <div class="mt-2 small text-muted">Past finished sessions</div>
            </div>
        </div>

        <!-- Expired Bookings -->
        <div class="col-6 col-md-3">
            <div class="card p-3 border-0 shadow-sm sp-stat-card bg-white">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-muted small d-block">Auto-Expired</span>
                        <h3 class="fw-bold mb-0 text-warning">${stats.expiredBookings}</h3>
                    </div>
                    <div class="rounded-circle bg-warning bg-opacity-10 text-warning p-3">
                        <i class="bi bi-alarm-fill fs-4"></i>
                    </div>
                </div>
                <div class="mt-2 small text-muted">Alerts sent to driver & provider</div>
            </div>
        </div>

        <!-- Cancelled Bookings -->
        <div class="col-6 col-md-3">
            <div class="card p-3 border-0 shadow-sm sp-stat-card bg-white">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-muted small d-block">Cancelled</span>
                        <h3 class="fw-bold mb-0 text-danger">${stats.cancelledBookings}</h3>
                    </div>
                    <div class="rounded-circle bg-danger bg-opacity-10 text-danger p-3">
                        <i class="bi bi-x-octagon-fill fs-4"></i>
                    </div>
                </div>
                <div class="mt-2 small text-muted">Released slots</div>
            </div>
        </div>

        <!-- Total Eligible Booking Value -->
        <div class="col-6 col-md-3">
            <div class="card p-3 border-0 shadow-sm sp-stat-card bg-white border-start border-4 border-success">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-muted small d-block">Eligible Booking Value</span>
                        <h3 class="fw-bold mb-0 text-success">$${stats.totalEligibleBookingValue}</h3>
                    </div>
                    <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
                        <i class="bi bi-cash-stack fs-4"></i>
                    </div>
                </div>
                <div class="mt-2 small text-muted">Calculated booking volume</div>
            </div>
        </div>
    </div>

    <!-- Revenue / Booking Value Transparency Disclaimer (Section 12 mandatory requirement) -->
    <div class="alert alert-light border small text-muted mb-4 d-flex align-items-center gap-2">
        <i class="bi bi-info-circle-fill text-primary"></i>
        <span>
            <strong>Accounting Distinction:</strong> <em>Eligible Booking Value</em> represents the calculated fee total of confirmed, completed, and expired bookings. Because this application version operates without an integrated payment gateway, it reflects reservation valuation rather than cleared merchant revenue.
        </span>
    </div>

    <!-- Customer Search Section (Feature 2) -->
    <div class="card p-4 border-0 shadow-sm sp-card mb-4" id="customer-search-section">
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
            <div>
                <h5 class="fw-bold mb-1">
                    <i class="bi bi-search text-primary me-2"></i>Search Customers & Bookings
                </h5>
                <p class="text-muted small mb-0">
                    Search driver reservations by customer name, email address, phone number, or booking reference/ID.
                </p>
            </div>
            <c:if test="${not empty searchQuery}">
                <a href="${pageContext.request.contextPath}/provider/dashboard" class="btn btn-sm btn-outline-secondary mt-2 mt-md-0">
                    <i class="bi bi-x-circle me-1"></i>Clear Search
                </a>
            </c:if>
        </div>

        <form action="${pageContext.request.contextPath}/provider/dashboard#customer-search-section" method="get" class="mb-3">
            <div class="input-group shadow-sm">
                <span class="input-group-text bg-white border-end-0 text-muted"><i class="bi bi-person-search"></i></span>
                <input type="text" name="query" value="${searchQuery}" class="form-control border-start-0"
                       placeholder="Enter customer name, email (e.g. customer@example.com), phone (+1-555...), or booking ID..."
                       aria-label="Search Customers" required>
                <button type="submit" class="btn btn-primary px-4 fw-semibold">
                    <i class="bi bi-search me-1"></i>Search
                </button>
            </div>
            <div class="form-text small text-muted mt-1">
                <i class="bi bi-shield-check text-success me-1"></i>Restricted to your managed facilities only. Customer passwords and sensitive credentials are never exposed.
            </div>
        </form>

        <!-- Search Results Table -->
        <c:if test="${searchQuery != null}">
            <div class="mt-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <h6 class="fw-bold text-dark m-0">
                        Matching Results for <span class="text-primary font-monospace">"${searchQuery}"</span>
                        <span class="badge bg-secondary rounded-pill ms-2">${searchResults != null ? searchResults.size() : 0} found</span>
                    </h6>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-muted text-uppercase">
                            <tr>
                                <th>Booking ID / Ref</th>
                                <th>Customer Name</th>
                                <th>Customer Email</th>
                                <th>Phone Number</th>
                                <th>Facility & Slot</th>
                                <th>Schedule</th>
                                <th>Booking Status</th>
                                <th>Payment Status</th>
                                <th>Amount</th>
                                <th class="text-end">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty searchResults}">
                                    <c:forEach var="sb" items="${searchResults}">
                                        <tr>
                                            <td>
                                                <span class="fw-bold font-monospace text-dark">#${sb.bookingId}</span>
                                                <div class="small text-muted font-monospace">${sb.bookingReference}</div>
                                            </td>
                                            <td>
                                                <strong class="text-dark">${sb.customerName}</strong>
                                            </td>
                                            <td>
                                                <a href="mailto:${sb.customerEmail}" class="text-decoration-none small">${sb.customerEmail}</a>
                                            </td>
                                            <td>
                                                <span class="small text-muted">${not empty sb.customerPhone ? sb.customerPhone : 'N/A'}</span>
                                            </td>
                                            <td>
                                                <strong class="d-block text-dark">${sb.parkingName}</strong>
                                                <span class="badge bg-dark small">Slot ${sb.slotNumber}</span>
                                            </td>
                                            <td class="small">
                                                <div><span class="text-muted">In:</span> ${sb.startTime}</div>
                                                <div><span class="text-muted">Out:</span> ${sb.endTime}</div>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${sb.confirmed}"><span class="badge badge-confirmed">CONFIRMED</span></c:when>
                                                    <c:when test="${sb.cancelled}"><span class="badge badge-cancelled">CANCELLED</span></c:when>
                                                    <c:when test="${sb.completed}"><span class="badge badge-completed">COMPLETED</span></c:when>
                                                    <c:when test="${sb.expired}"><span class="badge badge-expired">EXPIRED</span></c:when>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${sb.paid}"><span class="badge bg-success">PAID</span></c:when>
                                                    <c:when test="${sb.paymentFailed}"><span class="badge bg-danger">FAILED</span></c:when>
                                                    <c:otherwise><span class="badge bg-warning text-dark">PENDING</span></c:otherwise>
                                                </c:choose>
                                                <c:if test="${not empty sb.paymentReference}">
                                                    <div class="small text-muted font-monospace" style="font-size: 0.72rem;">${sb.paymentReference}</div>
                                                </c:if>
                                            </td>
                                            <td class="fw-bold text-success">$${sb.calculatedFee}</td>
                                            <td class="text-end">
                                                <a href="${pageContext.request.contextPath}/provider/booking-details?id=${sb.bookingId}"
                                                   class="btn btn-sm btn-outline-primary" title="View Full Details & Payment Verification">
                                                    <i class="bi bi-eye me-1"></i>View
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="10" class="text-center py-4 text-muted">
                                            <i class="bi bi-person-x fs-3 d-block text-secondary mb-2"></i>
                                            No customer reservations matched your search query <strong>"${searchQuery}"</strong> for your facilities.
                                            <div class="small text-muted mt-1">Please verify the name, email address, phone number, or booking reference.</div>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>
    </div>

    <!-- Recent Customer Bookings -->
    <div class="card p-4 border-0 shadow-sm sp-card">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0"><i class="bi bi-journal-text text-primary me-2"></i>Recent Customer Reservations</h5>
            <a href="${pageContext.request.contextPath}/provider/bookings" class="small fw-semibold text-primary text-decoration-none">
                View All Customer Bookings &rarr;
            </a>
        </div>

        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light small text-muted text-uppercase">
                    <tr>
                        <th>Reference</th>
                        <th>Facility & Slot</th>
                        <th>Customer</th>
                        <th>Schedule</th>
                        <th>Fee</th>
                        <th>Status</th>
                        <th>Payment</th>
                        <th class="text-end">Details</th>
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
                                        <span class="badge bg-dark small">Slot ${b.slotNumber}</span>
                                    </td>
                                    <td>
                                        <div class="fw-semibold text-dark">${b.customerName}</div>
                                        <div class="small text-muted">${b.customerPhone}</div>
                                    </td>
                                    <td class="small">
                                        <div><span class="text-muted">In:</span> ${b.startTime}</div>
                                        <div><span class="text-muted">Out:</span> ${b.endTime}</div>
                                    </td>
                                    <td class="fw-bold text-primary">$${b.calculatedFee}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${b.confirmed}"><span class="badge badge-confirmed">CONFIRMED</span></c:when>
                                            <c:when test="${b.cancelled}"><span class="badge badge-cancelled">CANCELLED</span></c:when>
                                            <c:when test="${b.completed}"><span class="badge badge-completed">COMPLETED</span></c:when>
                                            <c:when test="${b.expired}"><span class="badge badge-expired">EXPIRED</span></c:when>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${b.paid}"><span class="badge bg-success">PAID</span></c:when>
                                            <c:when test="${b.paymentFailed}"><span class="badge bg-danger">FAILED</span></c:when>
                                            <c:otherwise><span class="badge bg-warning text-dark">PENDING</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/provider/booking-details?id=${b.bookingId}"
                                           class="btn btn-sm btn-outline-primary" title="View Full Details">
                                            <i class="bi bi-eye"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" class="text-center py-4 text-muted">
                                    No customer bookings recorded for your parking areas yet.
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
