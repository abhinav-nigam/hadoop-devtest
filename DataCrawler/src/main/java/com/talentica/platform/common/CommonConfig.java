package com.talentica.platform.common;

/**
 * Created by AmitG on 20-03-2014.
 */
public class CommonConfig {

    private String marketplaceName;
    private String strategyName;
    private String processingDir;

    public String getMarketplaceName() {
        return marketplaceName;
    }

    public void setMarketplaceName(String marketplaceName) {
        this.marketplaceName = marketplaceName;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getProcessingDir() {
        return processingDir;
    }

    public void setProcessingDir(String processingDir) {
        this.processingDir = processingDir;
    }
}
