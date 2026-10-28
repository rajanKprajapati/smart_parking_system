<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Smart Parking - Find, Reserve & Manage Parking Spaces"/>
</jsp:include>

<!-- Hero Section -->
<section class="hero-section text-center">
    <div class="container py-4">
        <span class="badge bg-primary px-3 py-2 rounded-pill text-uppercase mb-3 fw-semibold">
            <i class="bi bi-shield-check me-1"></i>Centralized Parking Network
        </span>
        <h1 class="display-4 fw-bolder mb-3 text-white">Find & Reserve Parking in Seconds</h1>
        <p class="lead text-light opacity-75 mb-4 mx-auto" style="max-width: 720px;">
            Eliminate parking uncertainty. Discover verified parking areas, view real-time slot availability,
            reserve your bay in advance, and receive automatic expiry notifications via Email & SMS.
        </p>

        <!-- Search Bar -->
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <form action="${pageContext.request.contextPath}/browse" method="get" class="card p-2 p-md-3 shadow-lg border-0 rounded-pill bg-white">
                    <div class="row g-2 align-items-center">
                        <div class="col-md-5">
                            <div class="input-group">
                                <span class="input-group-text bg-transparent border-0 text-primary ps-3"><i class="bi bi-search"></i></span>
                                <input type="text" name="keyword" class="form-control border-0" placeholder="Parking area or city location...">
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="input-group">
                                <span class="input-group-text bg-transparent border-0 text-muted"><i class="bi bi-currency-dollar"></i></span>
                                <input type="number" step="0.5" name="maxPrice" class="form-control border-0" placeholder="Max $/hr">
                            </div>
                        </div>
                        <div class="col-md-3">
                            <button type="submit" class="btn btn-primary rounded-pill w-100 py-2 fw-semibold">
                                Search Slots
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</section>

<!-- How It Works Section -->
<section class="py-5">
    <div class="container">
        <div class="text-center mb-5">
            <h6 class="text-primary text-uppercase fw-bold letter-spacing-1">Effortless Workflow</h6>
            <h2 class="fw-bold">How Smart Parking Works</h2>
            <p class="text-muted">A seamless 3-step experience designed for drivers and parking operators.</p>
        </div>

        <div class="row g-4 text-center">
            <div class="col-md-4">
                <div class="card h-100 p-4 border-0 shadow-sm sp-card">
                    <div class="rounded-circle bg-primary bg-opacity-10 text-primary d-inline-flex align-items-center justify-content-center mx-auto mb-3" style="width: 70px; height: 70px;">
                        <i class="bi bi-geo-alt-fill fs-2"></i>
                    </div>
                    <h5 class="fw-bold mb-2">1. Discover & Select</h5>
                    <p class="text-muted small">Search parking garages by location, price per hour, and vehicle compatibility (Car, SUV, EV, Bike).</p>
                </div>
            </div>

            <div class="col-md-4">
                <div class="card h-100 p-4 border-0 shadow-sm sp-card">
                    <div class="rounded-circle bg-success bg-opacity-10 text-success d-inline-flex align-items-center justify-content-center mx-auto mb-3" style="width: 70px; height: 70px;">
                        <i class="bi bi-calendar-check-fill fs-2"></i>
                    </div>
                    <h5 class="fw-bold mb-2">2. Reserve Your Slot</h5>
                    <p class="text-muted small">Choose your exact arrival and departure times. Our transactional engine prevents double-booking guaranteed.</p>
                </div>
            </div>

            <div class="col-md-4">
                <div class="card h-100 p-4 border-0 shadow-sm sp-card">
                    <div class="rounded-circle bg-warning bg-opacity-10 text-warning d-inline-flex align-items-center justify-content-center mx-auto mb-3" style="width: 70px; height: 70px;">
                        <i class="bi bi-bell-fill fs-2"></i>
                    </div>
                    <h5 class="fw-bold mb-2">3. Automated Alerts</h5>
                    <p class="text-muted small">Receive immediate booking confirmation and automated Email & SMS reminders when your booking is nearing scheduled expiry.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Featured Parking Facilities -->
<section class="py-5 bg-white border-top border-bottom">
    <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-4">
            <div>
                <h6 class="text-primary text-uppercase fw-bold">Popular Locations</h6>
                <h2 class="fw-bold m-0">Featured Parking Areas</h2>
            </div>
            <a href="${pageContext.request.contextPath}/browse" class="btn btn-outline-primary rounded-pill px-4">
                View All <i class="bi bi-arrow-right ms-1"></i>
            </a>
        </div>

        <div class="row g-4">
            <c:choose>
                <c:when test="${not empty featuredAreas}">
                    <c:forEach var="area" items="${featuredAreas}">
                        <div class="col-md-6 col-lg-4">
                            <div class="card h-100 border-0 shadow-sm sp-card overflow-hidden">
                                <div class="card-body p-4">
                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                        <h5 class="card-title fw-bold mb-1">${area.parkingName}</h5>
                                        <span class="badge bg-primary fs-6 fw-bold">
                                            $${area.pricePerHour}<span class="small fw-normal">/hr</span>
                                        </span>
                                    </div>
                                    <p class="text-muted small mb-3">
                                        <i class="bi bi-geo-alt text-danger me-1"></i>${area.location}
                                    </p>
                                    <p class="card-text text-secondary small mb-3" style="min-height: 40px;">
                                        ${area.description != null ? area.description : 'Standard convenient parking.'}
                                    </p>
                                    <div class="d-flex justify-content-between align-items-center small text-muted border-top pt-3">
                                        <span><i class="bi bi-clock me-1 text-primary"></i>${area.operatingDetails}</span>
                                        <span class="badge bg-light text-dark border">
                                            <i class="bi bi-p-circle me-1 text-success"></i>${area.totalSlots} Slots
                                        </span>
                                    </div>
                                </div>
                                <div class="card-footer bg-light border-0 p-3 text-end">
                                    <a href="${pageContext.request.contextPath}/parking-details?id=${area.parkingAreaId}"
                                       class="btn btn-sm btn-primary px-3 rounded-pill">
                                        View Details & Slots <i class="bi bi-chevron-right ms-1"></i>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="col-12 text-center py-4">
                        <p class="text-muted">No parking areas listed yet. Check back shortly!</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

<!-- Call to Action for Providers -->
<section class="py-5">
    <div class="container">
        <div class="card bg-dark text-white border-0 rounded-4 p-5 shadow-lg position-relative overflow-hidden">
            <div class="row align-items-center">
                <div class="col-lg-8">
                    <span class="badge bg-warning text-dark px-3 py-1 rounded-pill mb-2 fw-semibold">For Parking Lot Owners</span>
                    <h2 class="display-6 fw-bold mb-3">Own a parking area or commercial garage?</h2>
                    <p class="text-light opacity-75 mb-0">
                        Maximize your parking space utilization, manage bays seamlessly, monitor upcoming and active reservations,
                        and receive automated expiry reports without manual paperwork.
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end mt-4 mt-lg-0">
                    <a href="${pageContext.request.contextPath}/register?role=PROVIDER" class="btn btn-primary btn-lg rounded-pill px-4">
                        Register as Provider <i class="bi bi-arrow-right ms-1"></i>
                    </a>
                </div>
            </div>
        </div>
    </div>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
