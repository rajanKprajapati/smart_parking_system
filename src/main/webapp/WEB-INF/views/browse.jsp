<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Browse Parking Facilities - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <!-- Header & Search Breadcrumb -->
    <div class="row align-items-center mb-4">
        <div class="col-md-6">
            <h2 class="fw-bold mb-1"><i class="bi bi-geo-alt-fill text-primary me-2"></i>Browse Parking Facilities</h2>
            <p class="text-muted small m-0">Find available parking garages, compare rates, and reserve bays.</p>
        </div>
        <div class="col-md-6 text-md-end mt-3 mt-md-0">
            <span class="badge bg-secondary fs-6">
                Found ${areas != null ? areas.size() : 0} Facilities
            </span>
        </div>
    </div>

    <!-- Filter Card -->
    <div class="card p-4 border-0 shadow-sm sp-card mb-4">
        <form action="${pageContext.request.contextPath}/browse" method="get" class="row g-3">
            <div class="col-md-6">
                <label class="form-label small fw-semibold text-muted">Search by Facility Name or Location</label>
                <div class="input-group">
                    <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                    <input type="text" name="keyword" class="form-control" placeholder="e.g. Downtown, Terminal, Airport..." value="${keyword}">
                </div>
            </div>

            <div class="col-md-4">
                <label class="form-label small fw-semibold text-muted">Max Price Per Hour ($)</label>
                <div class="input-group">
                    <span class="input-group-text bg-light"><i class="bi bi-currency-dollar"></i></span>
                    <input type="number" step="0.5" name="maxPrice" class="form-control" placeholder="e.g. 10.00" value="${maxPrice}">
                </div>
            </div>

            <div class="col-md-2 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100 py-2">
                    <i class="bi bi-funnel me-1"></i>Filter
                </button>
            </div>
        </form>
    </div>

    <!-- Parking Facilities Grid -->
    <div class="row g-4">
        <c:choose>
            <c:when test="${not empty areas}">
                <c:forEach var="area" items="${areas}">
                    <div class="col-md-6 col-lg-4">
                        <div class="card h-100 border-0 shadow-sm sp-card">
                            <div class="card-body p-4">
                                <div class="d-flex justify-content-between align-items-start mb-2">
                                    <h5 class="card-title fw-bold text-dark mb-1">${area.parkingName}</h5>
                                    <span class="badge bg-primary fs-6 fw-bold">
                                        $${area.pricePerHour}<span class="small fw-normal">/hr</span>
                                    </span>
                                </div>

                                <p class="text-muted small mb-2">
                                    <i class="bi bi-geo-alt-fill text-danger me-1"></i>${area.location}
                                </p>

                                <p class="card-text text-secondary small mb-3" style="min-height: 48px;">
                                    ${area.description != null ? area.description : 'Convenient, secure parking facility.'}
                                </p>

                                <div class="bg-light p-3 rounded-3 small mb-3">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span class="text-muted"><i class="bi bi-clock me-1"></i>Hours:</span>
                                        <span class="fw-semibold text-dark">${area.operatingDetails}</span>
                                    </div>
                                    <div class="d-flex justify-content-between mb-1">
                                        <span class="text-muted"><i class="bi bi-p-square me-1"></i>Capacity:</span>
                                        <span class="fw-semibold text-dark">${area.totalSlots} Slots</span>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span class="text-muted"><i class="bi bi-person me-1"></i>Provider:</span>
                                        <span class="fw-semibold text-dark">${area.providerName != null ? area.providerName : 'Verified Partner'}</span>
                                    </div>
                                </div>
                            </div>

                            <div class="card-footer bg-white border-0 p-3 pt-0 text-end">
                                <a href="${pageContext.request.contextPath}/parking-details?id=${area.parkingAreaId}"
                                   class="btn btn-primary w-100 py-2 rounded-3">
                                    <i class="bi bi-calendar2-range me-1"></i>Check Slots & Reserve
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="col-12 py-5 text-center">
                    <div class="card p-5 border-0 shadow-sm rounded-4">
                        <i class="bi bi-search text-muted display-4 mb-3"></i>
                        <h4 class="fw-bold">No Parking Facilities Match Your Search</h4>
                        <p class="text-muted">Try adjusting your search keywords or price filter, or view all available lots.</p>
                        <div class="mt-3">
                            <a href="${pageContext.request.contextPath}/browse" class="btn btn-outline-primary">
                                Clear Filters
                            </a>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
