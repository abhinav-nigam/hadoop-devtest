package com.talentica.platform;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.talentica.platform.common.CommonConfig;
import com.talentica.platform.common.MarketPlaceFactory;
import com.talentica.platform.common.MarketplaceBase;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Created by AmitG on 20-03-2014.
 */
public class Main {
    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        DOMConfigurator.configure("log4j.xml");
        Main main = new Main();
        CommonConfig commonConfig = main.loadConfiguration(args);
        if (commonConfig != null) {
            MarketplaceBase marketplace = MarketPlaceFactory.getTargetMarketplace(commonConfig.getMarketplaceName());
            marketplace.crawl(commonConfig);
        }
    }

    /**
     * This method loads the configuration passed to the program
     *
     * @param args
     * @return
     */
    private CommonConfig loadConfiguration(String[] args) {
        CommonConfig config = new CommonConfig();
        Yaml yaml = new Yaml();
        try {
            InputStream in = Files.newInputStream(Paths.get(args[0]));
            config = yaml.loadAs(in, CommonConfig.class);
        } catch (IOException e) {
            System.out.print("Error in opening the file");
        }
        return config;
    }
}
