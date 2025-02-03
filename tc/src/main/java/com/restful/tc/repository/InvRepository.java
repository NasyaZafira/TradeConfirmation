package com.restful.tc.repository;

import com.restful.tc.model.Invoice;
import org.springframework.data.repository.CrudRepository;

public interface InvRepository extends CrudRepository<Invoice, Long> {
}
