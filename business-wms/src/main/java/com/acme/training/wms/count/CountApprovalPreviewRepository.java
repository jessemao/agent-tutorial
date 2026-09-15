package com.acme.training.wms.count;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface CountApprovalPreviewRepository extends JpaRepository<CountApprovalPreview,Long>{Optional<CountApprovalPreview> findByToken(String token);}
