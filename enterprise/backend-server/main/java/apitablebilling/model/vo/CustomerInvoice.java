package com.apitable.enterprise.apitablebilling.model.vo;

import com.stripe.model.Invoice;
import lombok.Data;

/**
 * invoice vo.
 *
 * @author Shawn Deng
 */
@Data
public class CustomerInvoice {

    private String invoiceId;

    private Long invoiceDate;

    private long amount;

    private String status;

    private String invoicePdf;


    /**
     * new instance from stripe invoice.
     *
     * @param invoice stripe invoice object
     * @return CustomerInvoice
     */
    public static CustomerInvoice fromStripeInvoice(Invoice invoice) {
        CustomerInvoice customerInvoice = new CustomerInvoice();
        customerInvoice.setInvoiceId(invoice.getId());
        customerInvoice.setInvoiceDate(invoice.getCreated());
        customerInvoice.setAmount(invoice.getTotal());
        customerInvoice.setStatus(invoice.getStatus());
        customerInvoice.setInvoicePdf(invoice.getInvoicePdf());
        return customerInvoice;
    }
}
