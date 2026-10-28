<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Manage Parking Facilities - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1"><i class="bi bi-building text-primary me-2"></i>My Parking Facilities</h2>
            <p class="text-muted small m-0">Add, configure, and manage active operations for your parking locations.</p>
        </div>
        <div class="mt-3 mt-md-0">
            <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#addAreaModal">
                <i class="bi bi-plus-lg me-1"></i>Add New Facility
            </button>
        </div>
    </div>

    <!-- Facilities List -->
    <div class="card p-4 border-0 shadow-sm sp-card">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light small text-muted text-uppercase">
                    <tr>
                        <th>Facility Name</th>
                        <th>Location</th>
                        <th>Hourly Rate</th>
                        <th>Operating Hours</th>
                        <th>Capacity</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty areas}">
                            <c:forEach var="a" items="${areas}">
                                <tr>
                                    <td>
                                        <strong class="text-dark d-block">${a.parkingName}</strong>
                                        <span class="small text-secondary">${a.description}</span>
                                    </td>
                                    <td class="small text-muted"><i class="bi bi-geo-alt me-1 text-danger"></i>${a.location}</td>
                                    <td><strong class="text-primary fs-6">$${a.pricePerHour}</strong>/hr</td>
                                    <td class="small">${a.operatingDetails}</td>
                                    <td>
                                        <span class="badge bg-secondary">${a.totalSlots} Slots</span>
                                    </td>
                                    <td>
                                        <span class="badge ${a.active ? 'bg-success' : 'bg-danger'} text-uppercase">
                                            ${a.activeStatus}
                                        </span>
                                    </td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/provider/slots?areaId=${a.parkingAreaId}"
                                           class="btn btn-sm btn-outline-primary me-1" title="Manage Bays">
                                            <i class="bi bi-grid-3x3 me-1"></i>Slots
                                        </a>

                                        <!-- Edit Modal Trigger -->
                                        <button type="button" class="btn btn-sm btn-outline-secondary me-1"
                                                data-bs-toggle="modal" data-bs-target="#editAreaModal${a.parkingAreaId}" title="Edit Facility">
                                            <i class="bi bi-pencil"></i>
                                        </button>

                                        <!-- Toggle Status Form -->
                                        <form method="post" action="${pageContext.request.contextPath}/provider/areas" class="d-inline"
                                              onsubmit="return confirmAction('Toggle facility active status? Inactive facilities cannot accept new bookings.');">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="action" value="toggle_status">
                                            <input type="hidden" name="areaId" value="${a.parkingAreaId}">
                                            <input type="hidden" name="status" value="${a.active ? 'INACTIVE' : 'ACTIVE'}">
                                            <button type="submit" class="btn btn-sm ${a.active ? 'btn-outline-danger' : 'btn-outline-success'}"
                                                    title="${a.active ? 'Deactivate' : 'Activate'}">
                                                <i class="bi ${a.active ? 'bi-pause-circle' : 'bi-play-circle'}"></i>
                                            </button>
                                        </form>

                                        <!-- Edit Area Modal -->
                                        <div class="modal fade" id="editAreaModal${a.parkingAreaId}" tabindex="-1" aria-hidden="true">
                                            <div class="modal-dialog modal-dialog-centered text-start">
                                                <div class="modal-content">
                                                    <form method="post" action="${pageContext.request.contextPath}/provider/areas">
                                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                        <input type="hidden" name="areaId" value="${a.parkingAreaId}">
                                                        <div class="modal-header">
                                                            <h5 class="modal-title fw-bold">Edit Facility: ${a.parkingName}</h5>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                        </div>
                                                        <div class="modal-body">
                                                            <div class="mb-3">
                                                                <label class="form-label small fw-semibold">Facility Name</label>
                                                                <input type="text" name="parkingName" class="form-control" value="${a.parkingName}" required>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label small fw-semibold">Location / Address</label>
                                                                <input type="text" name="location" class="form-control" value="${a.location}" required>
                                                            </div>
                                                            <div class="row g-2 mb-3">
                                                                <div class="col-6">
                                                                    <label class="form-label small fw-semibold">Price Per Hour ($)</label>
                                                                    <input type="number" step="0.5" name="pricePerHour" class="form-control" value="${a.pricePerHour}" required>
                                                                </div>
                                                                <div class="col-6">
                                                                    <label class="form-label small fw-semibold">Operating Details</label>
                                                                    <input type="text" name="operatingDetails" class="form-control" value="${a.operatingDetails}" required>
                                                                </div>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label small fw-semibold">Description</label>
                                                                <textarea name="description" class="form-control" rows="3">${a.description}</textarea>
                                                            </div>
                                                        </div>
                                                        <div class="modal-footer">
                                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                            <button type="submit" class="btn btn-primary">Save Changes</button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" class="text-center py-5 text-muted">
                                    <i class="bi bi-building-add display-6 d-block mb-2"></i>
                                    You have not added any parking facilities yet. Click "Add New Facility" to get started.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Add New Area Modal -->
<div class="modal fade" id="addAreaModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${pageContext.request.contextPath}/provider/areas">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold"><i class="bi bi-plus-circle text-primary me-2"></i>Add New Parking Facility</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Facility Name</label>
                        <input type="text" name="parkingName" class="form-control" placeholder="e.g. City Center Multilevel Garage" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Location / Address</label>
                        <input type="text" name="location" class="form-control" placeholder="e.g. 500 Market St, Financial District" required>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label small fw-semibold">Hourly Rate ($)</label>
                            <input type="number" step="0.5" name="pricePerHour" class="form-control" placeholder="5.00" required>
                        </div>
                        <div class="col-6">
                            <label class="form-label small fw-semibold">Operating Hours</label>
                            <input type="text" name="operatingDetails" class="form-control" placeholder="e.g. 24/7 Operations" value="24/7 Operations" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Description</label>
                        <textarea name="description" class="form-control" rows="3" placeholder="Security features, barrier gates, covered parking details..."></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Create Facility</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
