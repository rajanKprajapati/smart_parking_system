<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="csrf-token" content="${csrfToken}">
    <title>${pageTitle != null ? pageTitle : 'Smart Parking Management System'}</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <!-- Custom Stylesheet -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>

<!-- Global Navigation -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top shadow-sm py-2">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${pageContext.request.contextPath}/">
            <i class="bi bi-p-square-fill fs-3 text-primary"></i>
            <span class="fw-bold">Smart<span class="text-primary">Parking</span></span>
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent"
                aria-controls="navbarContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/browse">
                        <i class="bi bi-geo-alt me-1"></i>Browse Parking
                    </a>
                </li>

                <c:if test="${sessionScope.loggedUser != null && (sessionScope.loggedUser.isCustomer() || sessionScope.loggedUser.isAdmin())}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/customer/dashboard">
                            <i class="bi bi-speedometer2 me-1"></i>My Dashboard
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/customer/bookings">
                            <i class="bi bi-calendar-check me-1"></i>My Bookings
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/customer/notifications">
                            <i class="bi bi-bell me-1"></i>Notifications
                        </a>
                    </li>
                </c:if>

                <c:if test="${sessionScope.loggedUser != null && (sessionScope.loggedUser.isProvider() || sessionScope.loggedUser.isAdmin())}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/provider/dashboard">
                            <i class="bi bi-bar-chart-fill me-1"></i>Provider Dashboard
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/provider/areas">
                            <i class="bi bi-building me-1"></i>Manage Facilities
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/provider/slots">
                            <i class="bi bi-grid-3x3-gap me-1"></i>Manage Slots
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/provider/bookings">
                            <i class="bi bi-journal-text me-1"></i>Customer Bookings
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/provider/notifications">
                            <i class="bi bi-bell me-1"></i>Alerts
                        </a>
                    </li>
                </c:if>
            </ul>

            <div class="d-flex align-items-center gap-2">
                <c:choose>
                    <c:when test="${sessionScope.loggedUser != null}">
                        <div class="dropdown">
                            <button class="btn btn-outline-light dropdown-toggle d-flex align-items-center gap-2"
                                    type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-person-circle"></i>
                                <span>${sessionScope.loggedUser.fullName}</span>
                                <span class="badge bg-primary text-uppercase">${sessionScope.loggedUser.role}</span>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end shadow">
                                <li>
                                    <h6 class="dropdown-header">Signed in as</h6>
                                    <span class="dropdown-item-text small text-muted">${sessionScope.loggedUser.email}</span>
                                </li>
                                <li><hr class="dropdown-divider"></li>
                                <c:choose>
                                    <c:when test="${sessionScope.loggedUser.isProvider()}">
                                        <li><a class="dropdown-item" href="${pageContext.request.contextPath}/provider/profile"><i class="bi bi-gear me-2"></i>Profile & Settings</a></li>
                                    </c:when>
                                    <c:otherwise>
                                        <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/profile"><i class="bi bi-gear me-2"></i>Profile & Settings</a></li>
                                    </c:otherwise>
                                </c:choose>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <form method="post" action="${pageContext.request.contextPath}/logout" class="m-0">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <button type="submit" class="dropdown-item text-danger">
                                            <i class="bi bi-box-arrow-right me-2"></i>Sign Out
                                        </button>
                                    </form>
                                </li>
                            </ul>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-light me-2">
                            <i class="bi bi-box-arrow-in-right me-1"></i>Sign In
                        </a>
                        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary">
                            <i class="bi bi-person-plus me-1"></i>Register
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</nav>

<!-- System Flash Alerts -->
<div class="container mt-3">
    <c:if test="${errorMessage != null}">
        <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>${errorMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${successMessage != null}">
        <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>${successMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${sessionScope.authError != null}">
        <div class="alert alert-warning alert-dismissible fade show shadow-sm" role="alert">
            <i class="bi bi-shield-exclamation me-2"></i>${sessionScope.authError}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="authError" scope="session"/>
    </c:if>
</div>

<main>
