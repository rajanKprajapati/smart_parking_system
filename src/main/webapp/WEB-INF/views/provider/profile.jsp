<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Provider Profile - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <h2 class="fw-bold mb-4"><i class="bi bi-gear-wide-connected text-primary me-2"></i>Provider Account & Settings</h2>

            <!-- Profile Info Form -->
            <div class="card p-4 border-0 shadow-sm sp-card mb-4 rounded-4">
                <h5 class="fw-bold mb-3">Company / Provider Details</h5>
                <form method="post" action="${pageContext.request.contextPath}/provider/profile">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="action" value="update_profile">

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-muted">Business Email (Login Identifier)</label>
                        <input type="email" class="form-control bg-light" value="${user.email}" readonly>
                        <div class="form-text small">Email cannot be modified directly.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Company / Provider Name</label>
                        <input type="text" name="fullName" class="form-control" value="${user.fullName}" required>
                    </div>

                    <div class="mb-4">
                        <label class="form-label small fw-semibold">Contact Phone (SMS Alert Target)</label>
                        <input type="tel" name="phoneNumber" class="form-control" value="${user.phoneNumber}" required>
                    </div>

                    <button type="submit" class="btn btn-primary px-4">
                        <i class="bi bi-save me-1"></i>Save Changes
                    </button>
                </form>
            </div>

            <!-- Change Password Form -->
            <div class="card p-4 border-0 shadow-sm sp-card rounded-4">
                <h5 class="fw-bold mb-3">Security & Password</h5>
                <form method="post" action="${pageContext.request.contextPath}/provider/profile">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="action" value="change_password">

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Current Password</label>
                        <input type="password" name="currentPassword" class="form-control" required>
                    </div>

                    <div class="row g-3 mb-4">
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">New Password</label>
                            <input type="password" name="newPassword" class="form-control" required minlength="6">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Confirm New Password</label>
                            <input type="password" name="confirmNewPassword" class="form-control" required minlength="6">
                        </div>
                    </div>

                    <button type="submit" class="btn btn-outline-danger px-4">
                        <i class="bi bi-key me-1"></i>Update Password
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
