package org.mskcc.cbio.oncokb.exporter;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class SchemaExporter {
    public static void main(String[] args) {
        try {
            Configuration cfg = new Configuration();
            
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.Gene.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.Alteration.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.Article.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.PortalAlteration.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.Evidence.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.Treatment.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.VariantConsequence.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.TreatmentDrug.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.Geneset.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.TumorType.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.Info.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.Drug.class);
            cfg.addAnnotatedClass(org.mskcc.cbio.oncokb.model.TumorTypeSynonym.class);
            
            cfg.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            cfg.setProperty("hibernate.show_sql", "false");
            cfg.setProperty("hibernate.format_sql", "false");
            cfg.setProperty("hibernate.connection.useUnicode", "true");
            cfg.setProperty("hibernate.connection.characterEncoding", "utf8");
            cfg.setProperty("hibernate.connection.charSet", "utf8");
            cfg.setProperty("hibernate.current_session_context_class", "thread");
            cfg.setProperty("hibernate.connection.release_mode", "after_statement");
            cfg.setProperty("hibernate.hbm2ddl.auto", "create");
            cfg.setProperty("hibernate.cache.use_second_level_cache", "false");
            cfg.setProperty("hibernate.cache.use_query_cache", "false");
            
            String username = getPropertyOrEnv("db.username", "DB_USERNAME");
            String password = getPropertyOrEnv("db.password", "DB_PASSWORD");
            String url = getPropertyOrEnv("db.url", "DB_URL");


            if (username == null || password == null || url == null) {
                System.exit(1);
            }
            cfg.setProperty("hibernate.connection.username", username);
            cfg.setProperty("hibernate.connection.password", password);
            cfg.setProperty("hibernate.connection.url", url);

            try {
                cfg.buildSessionFactory();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            SchemaExport export = new SchemaExport(cfg);
            export.setOutputFile("src/main/resources/schema-export.sql");
            export.setDelimiter(";");
            export.setFormat(true);
            
            export.create(true, false);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String getPropertyOrEnv(String sysPropKey, String envKey) {
        String val = System.getProperty(sysPropKey);
        if (val == null) {
            val = System.getenv(envKey);
        }
        return val;
    }
}