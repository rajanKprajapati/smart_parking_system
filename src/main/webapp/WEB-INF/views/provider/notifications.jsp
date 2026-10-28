<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Provider Alerts & Notifications - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1"><i class="bi bi-bell-fill text-primary me-2"></i>Provider Alert Log</h2>
            <p class="text-muted small m-0">History of automated alerts delivered regarding customer booking expiries and facility events.</p>
        </div>
        <div>
            <span class="badge bg-secondary fs-6">${notifications != null ? notifications.size() : 0} Total Logged</span>
        </div>
    </div>

    <!-- Info banner -->
    <div class="alert alert-info border-0 shadow-sm rounded-3 mb-4 d-flex align-items-center gap-3">
        <i class="bi bi-info-circle-fill fs-3 text-info"></i>
        <div class="small">
            <strong>Automated Notification Architecture:</strong> When a customer's reserved parking duration reaches scheduled end time, our background scheduler dispatches notifications to both the driver and you via Email & SMS. Unconfigured messaging channels log safely in development mode as <span class="badge badge-simulated">SIMULATED</span>.
        </div>
    </div>

    <!-- Table -->
    <div class="card p-4 border-0 shadow-sm sp-card">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light small text-muted text-uppercase">
                    <tr>
                        <th>Alert Type</th>
                        <th>Booking Ref</th>
                        <th>Channel</th>
                        <th>Contact Target</th>
                        <th>Delivery Status</th>
                        <th>Attempts</th>
                        <th>Timestamp</th>
                        <th class="text-end">Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty notifications}">
                            <c:forEach var="n" items="${notifications}">
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${n.notificationType == 'BOOKING_EXPIRY'}">
                                                <span class="badge bg-danger text-uppercase"><i class="bi bi-alarm me-1"></i>Slot Expiry Alert</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary text-uppercase">${n.notificationType}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="font-monospace fw-bold text-dark">${n.bookingReference}</span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${n.channel == 'EMAIL'}">
                                                <span class="badge bg-light text-dark border"><i class="bi bi-envelope text-primary me-1"></i>Email</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-light text-dark border"><i class="bi bi-chat-dots text-success me-1"></i>SMS</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="small text-muted">${n.recipientContact}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${n.deliveryStatus == 'SENT'}"><span class="badge badge-sent">SENT</span></c:when>
                                            <c:when test="${n.deliveryStatus == 'SIMULATED'}"><span class="badge badge-simulated">SIMULATED</span></c:when>
                                            <c:when test="${n.deliveryStatus == 'FAILED'}"><span class="badge badge-failed">FAILED</span></c:when>
                                            <c:when test="${n.deliveryStatus == 'RETRYING'}"><span class="badge badge-retrying">RETRYING</span></c:when>
                                            <c:otherwise><span class="badge bg-secondary">${n.deliveryStatus}</span></c:otherwise>
                                        </c:choose>
                                        <c:if test="${n.safeErrorSummary != null}">
                                            <div class="small text-danger mt-1 fst-italic" style="max-width: 250px;">${n.safeErrorSummary}</div>
                                        </c:if>
                                    </td>
                                    <td class="small">${n.attemptCount} / ${n.maxRetries}</td>
                                    <td class="small text-muted">${n.sentAt != null ? n.sentAt : n.createdAt}</td>
                                    <td class="text-end">
                                        <c:if test="${n.eligibleForRetry}">
                                            <form method="post" action="${pageContext.request.contextPath}/notifications/retry" class="d-inline">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="notificationId" value="${n.notificationId}">
                                                <input type="hidden" name="returnUrl" value="/provider/notifications">
                                                <button type="submit" class="btn btn-sm btn-outline-warning" title="Retry Delivery">
                                                    <i class="bi bi-arrow-clockwise me-1"></i>Retry
                                                </button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" class="text-center py-5 text-muted">
                                    <i class="bi bi-bell-slash display-6 d-block mb-2"></i>
                                    No alerts logged for your account yet.
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
