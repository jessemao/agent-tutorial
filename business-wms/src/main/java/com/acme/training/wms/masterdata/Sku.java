package com.acme.training.wms.masterdata;

import javax.persistence.*;

@Entity
@Table(name = "wms_sku")
public class Sku {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 32)
    private String code;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false)
    private boolean enabled;

    protected Sku() { }

    public Sku(String code, String name) {
        this.code = code;
        this.name = name;
        this.enabled = true;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }
}
