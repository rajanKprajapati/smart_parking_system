// Smart Parking Management System - Client Script
document.addEventListener('DOMContentLoaded', function () {

    // Auto-dismiss alerts after 6 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 6000);
    });

    // Real-time Booking Fee Estimator
    const startInput = document.getElementById('startTime');
    const endInput = document.getElementById('endTime');
    const priceInput = document.getElementById('pricePerHour');
    const feeDisplay = document.getElementById('liveFeeDisplay');
    const durationDisplay = document.getElementById('liveDurationDisplay');

    function updateFeeEstimate() {
        if (!startInput || !endInput || !priceInput || !feeDisplay) return;

        const startVal = startInput.value;
        const endVal = endInput.value;
        const priceVal = parseFloat(priceInput.value);

        if (!startVal || !endVal || isNaN(priceVal)) {
            feeDisplay.innerText = '$0.00';
            if (durationDisplay) durationDisplay.innerText = '0.00 hrs';
            return;
        }

        const start = new Date(startVal);
        const end = new Date(endVal);

        if (end <= start) {
            feeDisplay.innerText = 'Invalid Interval (End must be after Start)';
            if (durationDisplay) durationDisplay.innerText = '0 hrs';
            return;
        }

        const diffMinutes = (end - start) / (1000 * 60);
        let hours = diffMinutes / 60.0;
        if (hours < 0.5) hours = 0.5; // Minimum 30 min billing policy

        const totalFee = (hours * priceVal).toFixed(2);
        feeDisplay.innerText = '$' + totalFee;
        if (durationDisplay) durationDisplay.innerText = hours.toFixed(2) + ' hrs';
    }

    if (startInput && endInput) {
        startInput.addEventListener('change', updateFeeEstimate);
        endInput.addEventListener('change', updateFeeEstimate);
        updateFeeEstimate();
    }
});

// Helper for action confirmation dialogs
function confirmAction(message) {
    return confirm(message || 'Are you sure you want to proceed with this action?');
}
