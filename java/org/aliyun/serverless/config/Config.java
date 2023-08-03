package org.aliyun.serverless.config;

import java.time.Duration;

public class Config {
    private String platformHost;
    private Integer platformPort;
    private Duration gcInterval;
    private Duration idleDurationBeforeGC;
    private Boolean warmUp; // 这个作为config的参数又不适合，这里无法指定哪些实例进行预热，只能全部预热？

    public Config(String platformHost, Integer platformPort, Duration gcInterval, Duration idleDurationBeforeGC, Boolean warmUp) {
        this.platformHost = platformHost;
        this.platformPort = platformPort;
        this.gcInterval = gcInterval;
        this.idleDurationBeforeGC = idleDurationBeforeGC;
        this.warmUp = warmUp;
    }

    public String getPlatformHost() {
        return platformHost;
    }

    public void setPlatformHost(String platformHost) {
        this.platformHost = platformHost;
    }

    public Integer getPlatformPort() {
        return platformPort;
    }

    public void setPlatformPort(Integer platformPort) {
        this.platformPort = platformPort;
    }

    public Duration getGcInterval() {
        return gcInterval;
    }

    public void setGcInterval(Duration gcInterval) {
        this.gcInterval = gcInterval;
    }

    public Duration getIdleDurationBeforeGC() {
        return idleDurationBeforeGC;
    }

    public void setIdleDurationBeforeGC(Duration idleDurationBeforeGC) {
        this.idleDurationBeforeGC = idleDurationBeforeGC;
    }

    public Boolean getWarmUp() {
        return warmUp;
    }

    public void setWarmUp(Boolean warmUp) {
        this.warmUp = warmUp;
    }

    public static final Config DEFAULT_CONFIG = new Config(
            "127.0.0.1",
            50051,
            Duration.ofSeconds(10),
            Duration.ofMinutes(5),
            true
    );
}
