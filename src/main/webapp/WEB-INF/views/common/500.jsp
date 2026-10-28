<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="500 - Server Error"/>
</jsp:include>

<div class="container py-5 text-center">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card p-5 border-0 shadow-sm rounded-4">
                <i class="bi bi-exclamation-octagon-fill text-danger display-1 mb-3"></i>
                <h1 class="fw-bold">500 - Internal Processing Error</h1>
                <p class="text-muted lead">An unexpected exception occurred while fulfilling your request. Technical logs have recorded the occurrence safely.</p>
                <div class="mt-4">
                    <a href="${pageContext.request.contextPath}/" class="btn btn-primary px-4 py-2 me-2">
                        <i class="bi bi-house-door me-1"></i>Return Home
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
