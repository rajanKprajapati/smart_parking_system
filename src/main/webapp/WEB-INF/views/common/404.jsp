<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="404 - Page Not Found"/>
</jsp:include>

<div class="container py-5 text-center">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card p-5 border-0 shadow-sm rounded-4">
                <i class="bi bi-geo-alt-fill text-warning display-1 mb-3"></i>
                <h1 class="fw-bold">404 - Slot Not Found</h1>
                <p class="text-muted lead">The page or resource you requested could not be located on this parking network.</p>
                <div class="mt-4">
                    <a href="${pageContext.request.contextPath}/" class="btn btn-primary px-4 py-2 me-2">
                        <i class="bi bi-house-door me-1"></i>Return Home
                    </a>
                    <a href="${pageContext.request.contextPath}/browse" class="btn btn-outline-secondary px-4 py-2">
                        <i class="bi bi-search me-1"></i>Browse Facilities
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
