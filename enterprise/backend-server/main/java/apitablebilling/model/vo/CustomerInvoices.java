package com.apitable.enterprise.apitablebilling.model.vo;

import com.stripe.model.Invoice;
import java.util.List;

/**
 * invoice page list vo.
 *
 * @author Shawn Deng
 */
public class CustomerInvoices extends DataCollection<CustomerInvoice> {

    public void addInvoices(List<Invoice> invoices) {
        invoices.forEach(invoice -> data.add(CustomerInvoice.fromStripeInvoice(invoice)));
    }
}
