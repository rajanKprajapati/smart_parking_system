<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="${area.parkingName} - Parking Details"/>
</jsp:include>

<div class="container py-4">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/browse">Browse</a></li>
            <li class="breadcrumb-item active" aria-current="page">${area.parkingName}</li>
        </ol>
    </nav>

    <!-- Facility Overview Card -->
    <div class="card p-4 border-0 shadow-sm sp-card mb-4">
        <div class="row g-4 align-items-center">
            <div class="col-lg-8">
                <div class="d-flex align-items-center gap-3 mb-2">
                    <h2 class="fw-bold mb-0">${area.parkingName}</h2>
                    <span class="badge ${area.active ? 'bg-success' : 'bg-danger'} text-uppercase">
                        ${area.activeStatus}
                    </span>
                </div>
                <p class="text-muted mb-3">
                    <i class="bi bi-geo-alt-fill text-danger me-1"></i>${area.location}
                </p>
                <p class="text-secondary">${area.description}</p>
                <div class="d-flex flex-wrap gap-4 small text-muted border-top pt-3">
                    <div><i class="bi bi-clock-history text-primary me-1"></i>Hours: <strong class="text-dark">${area.operatingDetails}</strong></div>
                    <div><i class="bi bi-p-square text-primary me-1"></i>Total Capacity: <strong class="text-dark">${area.totalSlots} Slots</strong></div>
                    <div><i class="bi bi-person text-primary me-1"></i>Managed by: <strong class="text-dark">${area.providerName}</strong></div>
                </div>
            </div>

            <div class="col-lg-4 text-lg-end">
                <div class="bg-light p-4 rounded-4 border text-center">
                    <span class="text-muted text-uppercase small fw-semibold">Hourly Parking Rate</span>
                    <h1 class="display-5 fw-bold text-primary mb-1">$${area.pricePerHour}</h1>
                    <span class="text-muted small">per billable hour</span>
                </div>
            </div>
        </div>
    </div>

    <!-- Real-time Availability & Slot Reservation Selector -->
    <div class="card p-4 border-0 shadow-sm sp-card mb-4">
        <h4 class="fw-bold mb-3"><i class="bi bi-calendar-check text-primary me-2"></i>Check Slot Availability for Your Time Window</h4>
        <p class="text-muted small mb-4">Select your arrival and departure date/time. The system calculates availability dynamically by excluding overlapping confirmed reservations.</p>

        <form action="${pageContext.request.contextPath}/parking-details" method="get" class="row g-3">
            <input type="hidden" name="id" value="${area.parkingAreaId}">
            <input type="hidden" id="pricePerHour" value="${area.pricePerHour}">

            <div class="col-md-4">
                <label class="form-label small fw-semibold">Arrival Date & Time</label>
                <input type="datetime-local" class="form-control" name="startTime" id="startTime" value="${startTime}" required>
            </div>

            <div class="col-md-4">
                <label class="form-label small fw-semibold">Departure Date & Time</label>
                <input type="datetime-local" class="form-control" name="endTime" id="endTime" value="${endTime}" required>
            </div>

            <div class="col-md-2">
                <label class="form-label small fw-semibold">Vehicle Type</label>
                <select name="vehicleType" class="form-select">
                    <option value="ANY" ${vehicleType == 'ANY' ? 'selected' : ''}>All Types</option>
                    <option value="CAR" ${vehicleType == 'CAR' ? 'selected' : ''}>Car</option>
                    <option value="SUV" ${vehicleType == 'SUV' ? 'selected' : ''}>SUV</option>
                    <option value="EV" ${vehicleType == 'EV' ? 'selected' : ''}>Electric Vehicle (EV)</option>
                    <option value="BIKE" ${vehicleType == 'BIKE' ? 'selected' : ''}>Motorcycle / Bike</option>
                </select>
            </div>

            <div class="col-md-2 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100 py-2">
                    <i class="bi bi-search me-1"></i>Check Slots
                </button>
            </div>
        </form>

        <c:if test="${intervalError != null}">
            <div class="alert alert-warning mt-3 mb-0">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>${intervalError}
            </div>
        </c:if>

        <c:if test="${startTime != null && endTime != null && intervalError == null}">
            <div class="bg-primary bg-opacity-10 border border-primary border-opacity-25 p-3 rounded-3 mt-4 d-flex justify-content-between align-items-center">
                <div>
                    <span class="small text-muted d-block">Selected Interval:</span>
                    <strong class="text-dark">${startTime} &rarr; ${endTime}</strong>
                </div>
                <div class="text-end">
                    <span class="small text-muted d-block">Estimated Fee:</span>
                    <span class="fs-4 fw-bold text-primary" id="liveFeeDisplay">$${estimatedFee}</span>
                </div>
            </div>
        </c:if>
    </div>

    <!-- Parking Bays Grid -->
    <div class="card p-4 border-0 shadow-sm sp-card">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h4 class="fw-bold mb-0"><i class="bi bi-grid-3x3-gap-fill text-primary me-2"></i>Available Parking Bays</h4>
            <div class="d-flex gap-3 small text-muted">
                <span><i class="bi bi-square-fill text-success me-1"></i>Available</span>
                <span><i class="bi bi-square-fill text-secondary me-1"></i>Inactive / Booked</span>
            </div>
        </div>

        <div class="row g-3">
            <c:choose>
                <c:when test="${not empty slots}">
                    <c:forEach var="slot" items="${slots}">
                        <div class="col-6 col-sm-4 col-md-3 col-lg-2">
                            <div class="slot-bay ${slot.active ? 'available' : 'inactive'} h-100 d-flex flex-column justify-content-between p-3">
                                <div>
                                    <span class="badge bg-dark small mb-1">${slot.vehicleType}</span>
                                    <h5 class="fw-bold text-dark my-1">${slot.slotNumber}</h5>
                                </div>

                                <div class="mt-3">
                                    <c:choose>
                                        <c:when test="${slot.active}">
                                            <a href="${pageContext.request.contextPath}/customer/book?areaId=${area.parkingAreaId}&slotId=${slot.slotId}&startTime=${startTime}&endTime=${endTime}"
                                               class="btn btn-sm btn-success w-100 rounded-pill">
                                                Reserve
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <button class="btn btn-sm btn-secondary w-100 rounded-pill" disabled>
                                                Unavailable
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="col-12 py-4 text-center">
                        <p class="text-muted">No parking slots currently available for this facility or selected time window.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
