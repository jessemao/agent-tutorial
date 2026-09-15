package com.acme.training.wms.count;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="wms_count_approval_preview",uniqueConstraints=@UniqueConstraint(name="uk_count_approval_preview_token",columnNames="token"))
class CountApprovalPreview {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=64) private String token;
    @Column(name="count_id",nullable=false) private Long countId;
    @Column(name="count_version",nullable=false) private long countVersion;
    @Column(name="facts_fingerprint",nullable=false,length=64) private String factsFingerprint;
    @Column(name="created_by",nullable=false,length=100) private String createdBy;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected CountApprovalPreview(){}
    CountApprovalPreview(String token,Long countId,long countVersion,String factsFingerprint,String createdBy){this.token=token;this.countId=countId;this.countVersion=countVersion;this.factsFingerprint=factsFingerprint;this.createdBy=createdBy;this.createdAt=Instant.now();}
    boolean matches(Long requestedCountId,long requestedVersion){return countId.equals(requestedCountId)&&countVersion==requestedVersion;}
    String getFactsFingerprint(){return factsFingerprint;}
}
