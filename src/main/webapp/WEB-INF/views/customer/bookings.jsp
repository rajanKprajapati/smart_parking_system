<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="My Bookings - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1"><i class="bi bi-calendar-check text-primary me-2"></i>My Parking Reservations</h2>
            <p class="text-muted small m-0">Track upcoming, active, completed, cancelled, and expired parking bookings.</p>
        </div>
        <div class="mt-3 mt-md-0">
            <a href="${pageContext.request.contextPath}/browse" class="btn btn-primary rounded-pill px-4">
                <i class="bi bi-plus-circle me-1"></i>New Reservation
            </a>
        </div>
    </div>

    <c:if test="${param.cancelled != null}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>Reservation has been successfully cancelled. The parking bay has been released.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Status Navigation Tabs -->
    <ul class="nav nav-pills mb-4 bg-white p-2 rounded-3 shadow-sm border">
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'ALL' ? 'active' : ''}" href="${pageContext.request.contextPath}/customer/bookings?status=ALL">
                All Bookings
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'CONFIRMED' ? 'active' : ''}" href="${pageContext.request.contextPath}/customer/bookings?status=CONFIRMED">
                Confirmed / Active
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'COMPLETED' ? 'active' : ''}" href="${pageContext.request.contextPath}/customer/bookings?status=COMPLETED">
                Completed
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'EXPIRED' ? 'active' : ''}" href="${pageContext.request.contextPath}/customer/bookings?status=EXPIRED">
                Expired
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${activeStatus == 'CANCELLED' ? 'active' : ''}" href="${pageContext.request.contextPath}/customer/bookings?status=CANCELLED">
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
                        <th>Booking Reference</th>
                        <th>Facility & Location</th>
                        <th>Slot</th>
                        <th>Arrival (Start)</th>
                        <th>Departure (End)</th>
                        <th>Est. Fee</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
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
                                        <span class="small text-muted"><i class="bi bi-geo-alt me-1"></i>${b.parkingLocation}</span>
                                    </td>
                                    <td>
                                        <span class="badge bg-dark">${b.slotNumber}</span>
                                        <span class="badge bg-light text-dark border">${b.vehicleType}</span>
                                    </td>
                                    <td class="small">${b.startTime}</td>
                                    <td class="small">${b.endTime}</td>
                                    <td>
                                        <span class="fw-bold text-primary">$${b.calculatedFee}</span>
                                        <div class="small text-muted">${b.durationHours} hrs</div>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${b.confirmed}"><span class="badge badge-confirmed">CONFIRMED</span></c:when>
                                            <c:when test="${b.cancelled}"><span class="badge badge-cancelled">CANCELLED</span></c:when>
                                            <c:when test="${b.completed}"><span class="badge badge-completed">COMPLETED</span></c:when>
                                            <c:when test="${b.expired}"><span class="badge badge-expired">EXPIRED</span></c:when>
                                        </c:choose>
                                        <c:if test="${b.cancelled && b.cancellationReason != null}">
                                            <div class="small text-muted fst-italic mt-1">${b.cancellationReason}</div>
                                        </c:if>
                                    </td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/customer/confirmation?ref=${b.bookingReference}"
                                           class="btn btn-sm btn-outline-primary me-1" title="Receipt">
                                            <i class="bi bi-receipt"></i>
                                        </a>

                                        <c:if test="${b.eligibleForCancellation}">
                                            <button type="button" class="btn btn-sm btn-outline-danger"
                                                    data-bs-toggle="modal" data-bs-target="#cancelModal${b.bookingId}"
                                                    title="Cancel Reservation">
                                                <i class="bi bi-x-circle me-1"></i>Cancel
                                            </button>

                                            <!-- Cancel Modal -->
                                            <div class="modal fade" id="cancelModal${b.bookingId}" tabindex="-1" aria-hidden="true">
                                                <div class="modal-dialog modal-dialog-centered text-start">
                                                    <div class="modal-content">
                                                        <form method="post" action="${pageContext.request.contextPath}/customer/cancel-booking">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="bookingId" value="${b.bookingId}">
                                                            <div class="modal-header">
                                                                <h5 class="modal-title fw-bold">Cancel Reservation #${b.bookingReference}</h5>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                            </div>
                                                            <div class="modal-body">
                                                                <p class="text-muted small">Are you sure you want to cancel this booking? The slot will immediately become available for other drivers.</p>
                                                                <div class="mb-3">
                                                                    <label class="form-label small fw-semibold">Reason for Cancellation (Optional)</label>
                                                                    <textarea name="reason" class="form-control" rows="2" placeholder="e.g. Travel plan changed, rescheduled..."></textarea>
                                                                </div>
                                                            </div>
                                                            <div class="modal-footer">
                                                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Keep Booking</button>
                                                                <button type="submit" class="btn btn-danger">Confirm Cancellation</button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" class="text-center py-5 text-muted">
                                    <i class="bi bi-inbox display-6 d-block mb-2"></i>
                                    No reservations found matching the selected filter.
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
