<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Create Account - Smart Parking"/>
</jsp:include>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-6">
            <div class="card p-4 p-md-5 border-0 shadow rounded-4 bg-white">
                <div class="text-center mb-4">
                    <div class="rounded-circle bg-primary bg-opacity-10 text-primary d-inline-flex align-items-center justify-content-center mb-2" style="width: 60px; height: 60px;">
                        <i class="bi bi-person-plus fs-2"></i>
                    </div>
                    <h3 class="fw-bold mb-1">Create Your Account</h3>
                    <p class="text-muted small">Join our network as a Driver or Parking Facility Provider</p>
                </div>

                <form method="post" action="${pageContext.request.contextPath}/register">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-secondary">Account Type / Role</label>
                        <div class="row g-2">
                            <div class="col-6">
                                <input type="radio" class="btn-check" name="role" id="roleCustomer" value="CUSTOMER"
                                       ${role == null || role == 'CUSTOMER' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary w-100 py-2 d-flex flex-column align-items-center" for="roleCustomer">
                                    <i class="bi bi-car-front-fill fs-4 mb-1"></i>
                                    <span class="small fw-semibold">Customer / Driver</span>
                                </label>
                            </div>
                            <div class="col-6">
                                <input type="radio" class="btn-check" name="role" id="roleProvider" value="PROVIDER"
                                       ${role == 'PROVIDER' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary w-100 py-2 d-flex flex-column align-items-center" for="roleProvider">
                                    <i class="bi bi-building fs-4 mb-1"></i>
                                    <span class="small fw-semibold">Parking Provider</span>
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-secondary">Full Name</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-person text-muted"></i></span>
                            <input type="text" name="fullName" class="form-control border-start-0" placeholder="e.g. Jane Doe" value="${fullName}" required>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-secondary">Email Address</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-envelope text-muted"></i></span>
                            <input type="email" name="email" class="form-control border-start-0" placeholder="name@example.com" value="${email}" required>
                        </div>
                        <div class="form-text small">Used for account login and automated booking confirmation & expiry alerts.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-secondary">Phone Number</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-phone text-muted"></i></span>
                            <input type="tel" name="phone" class="form-control border-start-0" placeholder="+1-555-0199" value="${phone}" required>
                        </div>
                        <div class="form-text small">Required for critical SMS alerts when your parking session approaches expiry.</div>
                    </div>

                    <div class="row g-2 mb-4">
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold text-secondary">Password</label>
                            <input type="password" name="password" id="regPass" class="form-control" placeholder="At least 6 chars" required minlength="6">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold text-secondary">Confirm Password</label>
                            <input type="password" id="regConfirmPass" class="form-control" placeholder="Repeat password" required minlength="6">
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold rounded-3 shadow-sm mb-3">
                        Register Account <i class="bi bi-arrow-right ms-1"></i>
                    </button>
                </form>

                <div class="text-center mt-3">
                    <span class="small text-muted">Already registered? </span>
                    <a href="${pageContext.request.contextPath}/login" class="small fw-semibold text-primary text-decoration-none">
                        Sign In here
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
document.querySelector('form').addEventListener('submit', function (e) {
    const p1 = document.getElementById('regPass').value;
    const p2 = document.getElementById('regConfirmPass').value;
    if (p1 !== p2) {
        e.preventDefault();
        alert('Passwords do not match. Please re-enter.');
    }
});
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
