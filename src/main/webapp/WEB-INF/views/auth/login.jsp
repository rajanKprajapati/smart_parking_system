<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Sign In - Smart Parking"/>
</jsp:include>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="card p-4 p-md-5 border-0 shadow rounded-4 bg-white">
                <div class="text-center mb-4">
                    <div class="rounded-circle bg-primary bg-opacity-10 text-primary d-inline-flex align-items-center justify-content-center mb-2" style="width: 60px; height: 60px;">
                        <i class="bi bi-box-arrow-in-right fs-2"></i>
                    </div>
                    <h3 class="fw-bold mb-1">Welcome Back</h3>
                    <p class="text-muted small">Sign in to manage reservations or parking facilities</p>
                </div>

                <c:if test="${param.registered != null}">
                    <div class="alert alert-success small mb-3">
                        <i class="bi bi-check-circle-fill me-1"></i>Registration successful! Please sign in with your credentials.
                    </div>
                </c:if>

                <c:if test="${param.logged_out != null}">
                    <div class="alert alert-info small mb-3">
                        <i class="bi bi-info-circle-fill me-1"></i>You have been logged out safely.
                    </div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/login">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="redirect" value="${redirect != null ? redirect : param.redirect}">

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-secondary">Email Address</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-envelope text-muted"></i></span>
                            <input type="email" name="email" id="loginEmail" class="form-control border-start-0" placeholder="name@example.com" value="${email}" required autofocus>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label small fw-semibold text-secondary">Password</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-lock text-muted"></i></span>
                            <input type="password" name="password" id="loginPassword" class="form-control border-start-0" placeholder="••••••••" required>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold rounded-3 shadow-sm mb-3">
                        Sign In <i class="bi bi-arrow-right ms-1"></i>
                    </button>
                </form>

                <!-- Demo Credentials Quick Selector for Testing & Evaluation -->
                <div class="bg-light p-3 rounded-3 mt-3 border">
                    <span class="small fw-bold text-dark d-block mb-2"><i class="bi bi-lightning-charge-fill text-warning me-1"></i>Quick Test Accounts:</span>
                    <div class="d-flex flex-wrap gap-2">
                        <button type="button" class="btn btn-sm btn-outline-primary"
                                onclick="fillCreds('customer@smartparking.com', 'Password@123')">
                            Driver Demo
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-success"
                                onclick="fillCreds('provider@smartparking.com', 'Password@123')">
                            Provider Demo
                        </button>
                    </div>
                </div>

                <div class="text-center mt-4">
                    <span class="small text-muted">Don't have an account? </span>
                    <a href="${pageContext.request.contextPath}/register" class="small fw-semibold text-primary text-decoration-none">
                        Create one now
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
function fillCreds(email, pass) {
    document.getElementById('loginEmail').value = email;
    document.getElementById('loginPassword').value = pass;
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
