# Application Routes, APIs & Workflows

## 1. Controller Routing Reference

### Public Routes
| Route | Method | Controller | Description |
| :--- | :--- | :--- | :--- |
| `/` or `/home` | `GET` | `HomeServlet` | Landing page, hero search, featured facilities showcase |
| `/browse` | `GET` | `BrowseParkingServlet` | Browse facilities, filter by keyword and maximum price |
| `/parking-details` | `GET` | `BrowseParkingServlet` | Facility profile, hours, real-time bay availability search |
| `/login` | `GET` / `POST` | `AuthServlet` | User authentication form & session initialization |
| `/register` | `GET` / `POST` | `AuthServlet` | Driver & Provider registration form & account creation |
| `/logout` | `GET` / `POST` | `AuthServlet` | Invalidates session and clears security cookies |

### Customer / Driver Routes (Protected by `AuthFilter`)
| Route | Method | Controller | Description |
| :--- | :--- | :--- | :--- |
| `/customer/dashboard` | `GET` | `CustomerServlet` | Overview of recent reservations and quick facility links |
| `/customer/book` | `GET` / `POST` | `CustomerServlet` | Reservation form, live fee estimator, and booking creation |
| `/customer/confirmation` | `GET` | `CustomerServlet` | Printable confirmation receipt with unique reference code |
| `/customer/bookings` | `GET` | `CustomerServlet` | Reservation history with status tabs (`ALL`, `CONFIRMED`, `COMPLETED`, `EXPIRED`, `CANCELLED`) |
| `/customer/cancel-booking` | `POST` | `CustomerServlet` | Cancels an upcoming reservation and frees the bay |
| `/customer/profile` | `GET` / `POST` | `CustomerServlet` | Updates customer profile and password |
| `/customer/notifications` | `GET` | `CustomerServlet` | Customer notification history (Email & SMS alerts) |

### Provider Routes (Protected by `AuthFilter`)
| Route | Method | Controller | Description |
| :--- | :--- | :--- | :--- |
| `/provider/dashboard` | `GET` | `ProviderServlet` | Live analytics: bays, availability, bookings, and booking valuation |
| `/provider/areas` | `GET` / `POST` | `ProviderServlet` | Create/edit parking facilities and toggle active status |
| `/provider/slots` | `GET` / `POST` | `ProviderServlet` | Configure bays, slot IDs, vehicle types, and active toggles |
| `/provider/bookings` | `GET` | `ProviderServlet` | View customer reservations across facilities with status filter |
| `/provider/booking-details`| `GET` | `ProviderServlet` | Comprehensive booking audit, customer contact & timestamps |
| `/provider/profile` | `GET` / `POST` | `ProviderServlet` | Manage company profile and change password |
| `/provider/notifications` | `GET` | `ProviderServlet` | Facility alert log (automated customer slot expiry alerts) |

### System & Retry Routes
| Route | Method | Controller | Description |
| :--- | :--- | :--- | :--- |
| `/notifications/retry` | `POST` | `NotificationRetryServlet` | Allows safe manual retry of eligible failed notifications |

---

## 2. Core Workflows

### Workflow 1: Customer Slot Reservation
1. **Browse**: Customer accesses `/browse`, inputs keyword "Downtown", and sets max price "$6.00/hr".
2. **Inspect**: Customer clicks facility "Downtown Central Plaza" (`/parking-details?id=1`).
3. **Availability Check**: Customer inputs arrival `2026-11-01T10:00` and departure `2026-11-01T12:00`.
4. **Server Validation**: `ParkingSlotDao` executes interval exclusion query, returning unreserved bays.
5. **Fee Preview**: `FeeCalculator` computes 2.00 hours $\times$ $5.00/hr = $10.00 estimated fee.
6. **Submission**: Customer selects Bay `CP-A01` and submits form with anti-CSRF token to `/customer/book`.
7. **Transactional Concurrency**:
   * `BookingService` starts transaction `conn.setAutoCommit(false)`.
   * Acquires row lock via `SELECT 1 FROM bookings WHERE slot_id=1 ... FOR UPDATE`.
   * Verifies no race condition occurred; inserts booking record with unique reference `BK-20261101-1042`.
   * Commits transaction.
8. **Confirmation & Dispatch**: Redirects to `/customer/confirmation?ref=BK-20261101-1042`. Dispatches confirmation Email and SMS to driver.

### Workflow 2: Automated Background Expiry
1. `ExpiryScheduler` executes periodically (every 60s).
2. Executes query for bookings where `booking_status = 'CONFIRMED'` and `end_time <= NOW()`.
3. Finds booking `BK-20261101-1042`.
4. Updates status to `EXPIRED`.
5. Dispatches 4 automated notifications:
   * **Customer Email**: "Your parking booking #BK-20261101-1042 reached its scheduled end time. Please vacate slot CP-A01."
   * **Customer SMS**: "Smart Parking: Booking #BK-20261101-1042 at Downtown Central Plaza has expired."
   * **Provider Email**: "Customer booking #BK-20261101-1042 for slot CP-A01 reached its scheduled end time."
   * **Provider SMS**: "Provider Alert: Booking #BK-20261101-1042 for slot CP-A01 expired."
6. Records all 4 dispatches in MySQL `notifications` table with status `SIMULATED` (or `SENT` if SMTP/Twilio credentials are configured).
