<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="403 - Access Forbidden"/>
</jsp:include>

<div class="container py-5 text-center">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card p-5 border-0 shadow-sm rounded-4">
                <i class="bi bi-shield-lock-fill text-danger display-1 mb-3"></i>
                <h1 class="fw-bold">403 - Access Forbidden</h1>
                <p class="text-muted lead">You do not possess the required security privileges or ownership permissions to view this resource.</p>
                <div class="mt-4">
                    <a href="${pageContext.request.contextPath}/" class="btn btn-primary px-4 py-2 me-2">
                        <i class="bi bi-house-door me-1"></i>Return Home
                    </a>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-secondary px-4 py-2">
                        <i class="bi bi-box-arrow-in-right me-1"></i>Switch Account
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
