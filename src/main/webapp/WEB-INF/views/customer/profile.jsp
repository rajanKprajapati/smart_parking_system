<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="My Profile - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <h2 class="fw-bold mb-4"><i class="bi bi-person-gear text-primary me-2"></i>Profile & Account Settings</h2>

            <!-- Personal Information Card -->
            <div class="card p-4 border-0 shadow-sm sp-card mb-4 rounded-4">
                <h5 class="fw-bold mb-3">Personal Details</h5>
                <form method="post" action="${pageContext.request.contextPath}/customer/profile">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="action" value="update_profile">

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-muted">Email Address (Account Identifier)</label>
                        <input type="email" class="form-control bg-light" value="${user.email}" readonly>
                        <div class="form-text small">Email cannot be modified directly.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Full Name</label>
                        <input type="text" name="fullName" class="form-control" value="${user.fullName}" required>
                    </div>

                    <div class="mb-4">
                        <label class="form-label small fw-semibold">Phone Number (SMS Alert Target)</label>
                        <input type="tel" name="phoneNumber" class="form-control" value="${user.phoneNumber}" required>
                    </div>

                    <button type="submit" class="btn btn-primary px-4">
                        <i class="bi bi-save me-1"></i>Save Profile
                    </button>
                </form>
            </div>

            <!-- Password Change Card -->
            <div class="card p-4 border-0 shadow-sm sp-card rounded-4">
                <h5 class="fw-bold mb-3">Change Password</h5>
                <form method="post" action="${pageContext.request.contextPath}/customer/profile">
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
