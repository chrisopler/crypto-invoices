package com.chrisopler.cryptoinvoices.server.service;

import com.chrisopler.cryptoinvoices.server.generated.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {}
