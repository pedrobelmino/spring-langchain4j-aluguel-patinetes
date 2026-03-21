// Update: Normalize booking ID to uppercase for case-insensitive search
Optional<Booking> bookingOpt = bookingService.findById(bookingId.toUpperCase());