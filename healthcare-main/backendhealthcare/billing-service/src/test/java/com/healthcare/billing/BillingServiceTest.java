package com.healthcare.billing;

import com.healthcare.billing.model.Bill;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {

    private BillingService billingService;


    @BeforeEach
    void setUp() {
        billingService = new BillingService();
    }


    // =========================================================
    // createBill() TEST CASES
    // =========================================================


    // TC-BS-01: Create bill with all values provided
    @Test
    void shouldCreateBillSuccessfully() {

        Bill bill = new Bill(
                10L,
                201L,
                "John Doe",
                "Dr. Smith",
                100.0,
                "PAID"
        );

        Bill result = billingService.createBill(bill);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(201L, result.getAppointmentId());
        assertEquals("PAID", result.getPaymentStatus());

        Optional<Bill> savedBill =
                billingService.getBillById(10L);

        assertTrue(savedBill.isPresent());
    }


    // TC-BS-02: Generate ID when bill ID is null
    @Test
    void shouldGenerateIdWhenBillIdIsNull() {

        Bill bill = new Bill(
                null,
                202L,
                "Alice",
                "Dr. Brown",
                80.0,
                "PAID"
        );

        Bill result = billingService.createBill(bill);

        assertNotNull(result.getId());

        Optional<Bill> savedBill =
                billingService.getBillById(result.getId());

        assertTrue(savedBill.isPresent());
    }


    // TC-BS-03: Set default consultation fee when null
    @Test
    void shouldSetDefaultConsultationFee() {

        Bill bill = new Bill();

        bill.setId(20L);
        bill.setAppointmentId(203L);
        bill.setConsultationFee(null);

        Bill result = billingService.createBill(bill);

        assertEquals(
                50.0,
                result.getConsultationFee()
        );
    }


    // TC-BS-04: Calculate tax and total amount
    @Test
    void shouldCalculateTaxAndTotalAmount() {

        Bill bill = new Bill();

        bill.setId(21L);
        bill.setAppointmentId(204L);
        bill.setConsultationFee(100.0);

        Bill result = billingService.createBill(bill);

        assertEquals(
                5.0,
                result.getTaxAmount()
        );

        assertEquals(
                105.0,
                result.getTotalAmount()
        );
    }


    // TC-BS-05: Set default payment status
    @Test
    void shouldSetDefaultPaymentStatus() {

        Bill bill = new Bill();

        bill.setId(22L);
        bill.setAppointmentId(205L);
        bill.setConsultationFee(100.0);
        bill.setPaymentStatus(null);

        Bill result = billingService.createBill(bill);

        assertEquals(
                "PAID",
                result.getPaymentStatus()
        );
    }


    // =========================================================
    // getAllBills() TEST CASES
    // =========================================================


    // TC-BS-06: Get all bills
    @Test
    void shouldGetAllBillsSuccessfully() {

        List<Bill> bills =
                billingService.getAllBills();

        // Three sample bills are created in constructor
        assertTrue(bills.size() >= 3);
    }


    // =========================================================
    // getBillById() TEST CASES
    // =========================================================


    // TC-BS-07: Get bill by existing ID
    @Test
    void shouldGetBillByIdSuccessfully() {

        Optional<Bill> result =
                billingService.getBillById(1L);

        assertTrue(result.isPresent());
        assertEquals(
                1L,
                result.get().getId()
        );
    }


    // TC-BS-08: Return empty when bill ID does not exist
    @Test
    void shouldReturnEmptyWhenBillNotFound() {

        Optional<Bill> result =
                billingService.getBillById(9999L);

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // getBillByAppointmentId() TEST CASES
    // =========================================================


    // TC-BS-09: Get bill by appointment ID
    @Test
    void shouldGetBillByAppointmentIdSuccessfully() {

        Optional<Bill> result =
                billingService.getBillByAppointmentId(101L);

        assertTrue(result.isPresent());

        assertEquals(
                101L,
                result.get().getAppointmentId()
        );
    }


    // =========================================================
    // getRevenueSummary() TEST CASE
    // =========================================================


    // TC-BS-10: Generate revenue summary correctly
    @Test
    void shouldGenerateRevenueSummary() {

        Map<String, Object> summary =
                billingService.getRevenueSummary();

        assertNotNull(summary);

        assertTrue(
                summary.containsKey("totalRevenue")
        );

        assertTrue(
                summary.containsKey("totalInvoices")
        );

        assertTrue(
                summary.containsKey("paidInvoices")
        );

        assertEquals(
                "USD ($)",
                summary.get("currency")
        );
    }


    // TC-BS-11: Revenue summary should ignore unpaid bills
    @Test
    void shouldIgnoreUnpaidBillsInRevenue() {

        Bill unpaidBill = new Bill(
                50L,
                500L,
                "Test Patient",
                "Test Doctor",
                100.0,
                "UNPAID"
        );

        billingService.createBill(unpaidBill);

        Map<String, Object> summary =
                billingService.getRevenueSummary();

        assertEquals(
                3L,
                summary.get("paidInvoices")
        );
    }
}