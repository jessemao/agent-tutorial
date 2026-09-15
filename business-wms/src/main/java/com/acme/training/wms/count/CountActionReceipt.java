package com.acme.training.wms.count;

import com.acme.training.platform.error.PlatformException;
import java.io.*;
import java.util.Base64;
import javax.persistence.*;

@Entity
@Table(
        name = "wms_count_action_receipt",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_count_action_key",
                columnNames = {"action_type", "idempotency_key"}))
class CountActionReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "action_type", nullable = false, length = 24)
    private String actionType;
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;
    @Column(name = "count_id", nullable = false)
    private Long countId;
    @Column(nullable = false, length = 2000)
    private String fingerprint;
    @Column(name = "result_version", nullable = false)
    private long resultVersion;
    @Lob
    @Column(name = "result_snapshot", nullable = false)
    private String resultSnapshot;

    protected CountActionReceipt() {
    }

    CountActionReceipt(CountAction action, String key, Long countId, String fingerprint,
                       InventoryCountView result) {
        this.actionType = action.name();
        this.idempotencyKey = key;
        this.countId = countId;
        this.fingerprint = fingerprint;
        this.resultVersion = result.getVersion();
        this.resultSnapshot = serialize(result);
    }

    Long getCountId() {
        return countId;
    }

    String getFingerprint() {
        return fingerprint;
    }

    long getResultVersion() {
        return resultVersion;
    }

    InventoryCountView getResult() {
        try {
            byte[] snapshot = Base64.getDecoder().decode(resultSnapshot);
            return deserialize(snapshot);
        } catch (IOException | ClassNotFoundException | RuntimeException failure) {
            throw new PlatformException(
                    "WMS_COUNT_RECEIPT_INVALID", "盘点幂等回执损坏");
        }
    }

    private InventoryCountView deserialize(byte[] snapshot)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(snapshot))) {
            return (InventoryCountView) input.readObject();
        }
    }

    private static String serialize(InventoryCountView result) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(result);
            output.flush();
            return Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException failure) {
            throw new PlatformException(
                    "WMS_COUNT_RECEIPT_INVALID", "无法保存盘点幂等结果");
        }
    }
}
