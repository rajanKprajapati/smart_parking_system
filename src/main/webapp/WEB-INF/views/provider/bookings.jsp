<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Customer Bookings - Provider Portal"/>
</jsp:include>

<div class="container py-4">
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1"><i class="bi bi-journal-text text-primary me-2"></i>Customer Reservations</h2>
            <p class="text-muted small m-0">Monitor all customer bookings reserved across your parking facilities.</p>
        </div>
    </div>

    <!-- Status Tabs -->
    <ul class="nav nav-pills mb-4 bg-white p-2 rounded-3 shadow-sm border">
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'ALL' ? 'active' : ''}" href="${pageContext.request.contextPath}/provider/bookings?status=ALL">
                All Bookings
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'CONFIRMED' ? 'active' : ''}" href="${pageContext.request.contextPath}/provider/bookings?status=CONFIRMED">
                Confirmed / Active
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'COMPLETED' ? 'active' : ''}" href="${pageContext.request.contextPath}/provider/bookings?status=COMPLETED">
                Completed
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'EXPIRED' ? 'active' : ''}" href="${pageContext.request.contextPath}/provider/bookings?status=EXPIRED">
                Expired
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'CANCELLED' ? 'active' : ''}" href="${pageContext.request.contextPath}/provider/bookings?status=CANCELLED">
                Cancelled
            </a>
        </li>
    </ul>

    <!-- Bookings Table -->
    <div class="card p-4 border-0 shadow-sm sp-card">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light small text-muted text-uppercase">
                    <tr>
                        <th>Booking Ref</th>
                        <th>Facility & Slot</th>
                        <th>Customer Details</th>
                        <th>Arrival (Start)</th>
                        <th>Departure (End)</th>
                        <th>Duration / Fee</th>
                        <th>Booking Status</th>
                        <th>Payment</th>
                        <th class="text-end">Details</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty bookings}">
                            <c:forEach var="b" items="${bookings}">
                                <tr>
                                    <td>
                                        <span class="font-monospace fw-bold text-dark">${b.bookingReference}</span>
                                        <div class="small text-muted">${b.createdAt}</div>
                                    </td>
                                    <td>
                                        <strong class="text-dark d-block">${b.parkingName}</strong>
                                        <span class="badge bg-dark">Slot ${b.slotNumber}</span>
                                        <span class="badge bg-light text-dark border">${b.vehicleType}</span>
                                    </td>
                                    <td>
                                        <div class="fw-semibold text-dark">${b.customerName}</div>
                                        <div class="small text-muted"><i class="bi bi-envelope me-1"></i>${b.customerEmail}</div>
                                        <div class="small text-muted"><i class="bi bi-phone me-1"></i>${b.customerPhone}</div>
                                    </td>
                                    <td class="small">${b.startTime}</td>
                                    <td class="small">${b.endTime}</td>
                                    <td>
                                        <div class="fw-bold text-primary">$${b.calculatedFee}</div>
                                        <div class="small text-muted">${b.durationHours} hrs</div>
                                    </td>
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
                                        <c:if test="${not empty b.paymentReference}">
                                            <div class="small text-muted font-monospace" style="font-size: 0.72rem;">${b.paymentReference}</div>
                                        </c:if>
                                    </td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/provider/booking-details?id=${b.bookingId}"
                                           class="btn btn-sm btn-outline-primary" title="View Full Booking Audit">
                                            <i class="bi bi-eye me-1"></i>Details
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="9" class="text-center py-5 text-muted">
                                    <i class="bi bi-inbox display-6 d-block mb-2"></i>
                                    No customer bookings found for the selected status filter.
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
