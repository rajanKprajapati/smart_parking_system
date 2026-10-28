<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Manage Slots - Smart Parking"/>
</jsp:include>

<div class="container py-4">
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1"><i class="bi bi-grid-3x3-gap text-primary me-2"></i>Parking Bays & Slots</h2>
            <p class="text-muted small m-0">Add individual parking slots, designate vehicle compatibility, and toggle active bay status.</p>
        </div>
        <c:if test="${selectedArea != null}">
            <div class="mt-3 mt-md-0">
                <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#addSlotModal">
                    <i class="bi bi-plus-lg me-1"></i>Add Slot to ${selectedArea.parkingName}
                </button>
            </div>
        </c:if>
    </div>

    <!-- Facility Selector Dropdown -->
    <div class="card p-3 border-0 shadow-sm sp-card mb-4 bg-light">
        <form action="${pageContext.request.contextPath}/provider/slots" method="get" class="row g-2 align-items-center">
            <div class="col-md-3">
                <label class="form-label small fw-bold text-uppercase text-secondary m-0">Select Facility:</label>
            </div>
            <div class="col-md-6">
                <select name="areaId" class="form-select" onchange="this.form.submit()">
                    <c:forEach var="a" items="${areas}">
                        <option value="${a.parkingAreaId}" ${selectedArea != null && selectedArea.parkingAreaId == a.parkingAreaId ? 'selected' : ''}>
                            ${a.parkingName} (${a.location})
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <button type="submit" class="btn btn-outline-primary w-100">Load Slots</button>
            </div>
        </form>
    </div>

    <!-- Slots Grid / Table -->
    <c:choose>
        <c:when test="${selectedArea != null}">
            <div class="card p-4 border-0 shadow-sm sp-card">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold mb-0">Slots in: <span class="text-primary">${selectedArea.parkingName}</span></h5>
                    <span class="badge bg-secondary fs-6">${slots != null ? slots.size() : 0} Configured Slots</span>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-muted text-uppercase">
                            <tr>
                                <th>Slot Number / Bay ID</th>
                                <th>Vehicle Compatibility</th>
                                <th>Operational Status</th>
                                <th>Configured On</th>
                                <th class="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty slots}">
                                    <c:forEach var="s" items="${slots}">
                                        <tr>
                                            <td>
                                                <span class="fw-bold text-dark fs-6">${s.slotNumber}</span>
                                            </td>
                                            <td>
                                                <span class="badge bg-dark">${s.vehicleType}</span>
                                            </td>
                                            <td>
                                                <span class="badge ${s.active ? 'bg-success' : 'bg-secondary'} text-uppercase">
                                                    ${s.activeStatus}
                                                </span>
                                            </td>
                                            <td class="small text-muted">${s.createdAt}</td>
                                            <td class="text-end">
                                                <!-- Edit Trigger -->
                                                <button type="button" class="btn btn-sm btn-outline-secondary me-1"
                                                        data-bs-toggle="modal" data-bs-target="#editSlotModal${s.slotId}">
                                                    <i class="bi bi-pencil"></i>
                                                </button>

                                                <!-- Toggle Slot Status -->
                                                <form method="post" action="${pageContext.request.contextPath}/provider/slots" class="d-inline"
                                                      onsubmit="return confirmAction('Toggle slot status?');">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <input type="hidden" name="action" value="toggle_status">
                                                    <input type="hidden" name="areaId" value="${selectedArea.parkingAreaId}">
                                                    <input type="hidden" name="slotId" value="${s.slotId}">
                                                    <input type="hidden" name="status" value="${s.active ? 'INACTIVE' : 'ACTIVE'}">
                                                    <button type="submit" class="btn btn-sm ${s.active ? 'btn-outline-danger' : 'btn-outline-success'}"
                                                            title="${s.active ? 'Deactivate Bay' : 'Activate Bay'}">
                                                        <i class="bi ${s.active ? 'bi-toggle-on' : 'bi-toggle-off'}"></i>
                                                    </button>
                                                </form>

                                                <!-- Edit Slot Modal -->
                                                <div class="modal fade" id="editSlotModal${s.slotId}" tabindex="-1" aria-hidden="true">
                                                    <div class="modal-dialog modal-dialog-centered text-start">
                                                        <div class="modal-content">
                                                            <form method="post" action="${pageContext.request.contextPath}/provider/slots">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <input type="hidden" name="areaId" value="${selectedArea.parkingAreaId}">
                                                                <input type="hidden" name="slotId" value="${s.slotId}">
                                                                <div class="modal-header">
                                                                    <h5 class="modal-title fw-bold">Edit Slot ${s.slotNumber}</h5>
                                                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                                </div>
                                                                <div class="modal-body">
                                                                    <div class="mb-3">
                                                                        <label class="form-label small fw-semibold">Slot Identifier</label>
                                                                        <input type="text" name="slotNumber" class="form-control" value="${s.slotNumber}" required>
                                                                    </div>
                                                                    <div class="mb-3">
                                                                        <label class="form-label small fw-semibold">Vehicle Type</label>
                                                                        <select name="vehicleType" class="form-select">
                                                                            <option value="CAR" ${s.vehicleType == 'CAR' ? 'selected' : ''}>Standard Car</option>
                                                                            <option value="SUV" ${s.vehicleType == 'SUV' ? 'selected' : ''}>SUV / Van</option>
                                                                            <option value="EV" ${s.vehicleType == 'EV' ? 'selected' : ''}>Electric Vehicle (EV)</option>
                                                                            <option value="BIKE" ${s.vehicleType == 'BIKE' ? 'selected' : ''}>Motorcycle / Bike</option>
                                                                            <option value="ANY" ${s.vehicleType == 'ANY' ? 'selected' : ''}>Any Vehicle</option>
                                                                        </select>
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
                                        <td colspan="5" class="text-center py-5 text-muted">
                                            No slots created for this parking facility yet. Click "Add Slot" above to define parking bays.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Add Slot Modal -->
            <div class="modal fade" id="addSlotModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <form method="post" action="${pageContext.request.contextPath}/provider/slots">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="areaId" value="${selectedArea.parkingAreaId}">
                            <div class="modal-header">
                                <h5 class="modal-title fw-bold">Add Slot to ${selectedArea.parkingName}</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                            </div>
                            <div class="modal-body">
                                <div class="mb-3">
                                    <label class="form-label small fw-semibold">Slot Identifier / Number</label>
                                    <input type="text" name="slotNumber" class="form-control" placeholder="e.g. A-101, Bay-12, EV-01" required>
                                    <div class="form-text small">Must be unique within this facility.</div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label small fw-semibold">Vehicle Compatibility</label>
                                    <select name="vehicleType" class="form-select">
                                        <option value="CAR">Standard Car</option>
                                        <option value="SUV">SUV / Van</option>
                                        <option value="EV">Electric Vehicle (EV Charging Bay)</option>
                                        <option value="BIKE">Motorcycle / Two-Wheeler</option>
                                        <option value="ANY">Any Vehicle</option>
                                    </select>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                <button type="submit" class="btn btn-primary">Create Slot</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="card p-5 border-0 shadow-sm text-center">
                <h5 class="fw-bold text-muted">No Facilities Available</h5>
                <p class="text-muted small">Please create a parking facility under "Manage Facilities" before configuring slots.</p>
                <div>
                    <a href="${pageContext.request.contextPath}/provider/areas" class="btn btn-primary">Go to Facilities</a>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
