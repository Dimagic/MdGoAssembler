package mdgoassembler.utils;

import mdgoassembler.models.AssHistory;
import mdgoassembler.models.Assembly;

import mdgoassembler.models.Product;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.Statistics;

import java.util.HashMap;

public class HibernateSessionFactoryUtil implements MsgBox {
    private static final Logger LOGGER = LogManager.getLogger(HibernateSessionFactoryUtil.class.getName());
    private static SessionFactory sessionFactory;

    private HibernateSessionFactoryUtil() {
    }

    public static SessionFactory getSessionFactory() throws CustomException {
        Settings settings = Utils.getSettings();
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration()
                        .addAnnotatedClass(Assembly.class)
                        .addAnnotatedClass(Product.class)
                        .addAnnotatedClass(AssHistory.class)
                        .setProperty("hibernate.connection.url", String.format("jdbc:postgresql://%s:%s/%s",
                                settings.getAddr_db(), settings.getPort_db(), settings.getName_db()))
                        .setProperty("hibernate.connection.username", settings.getUser_db())
                        .setProperty("hibernate.connection.password", settings.getPass_db())
                        .configure();

                StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());
                Statistics stats = sessionFactory.getStatistics();
                stats.setStatisticsEnabled(true);
            } catch (Exception e) {
                throw new CustomException(e.getCause().getCause().getMessage());
            }
        }
        return sessionFactory;
    }

    public static SessionFactory restartSessionFactory() throws CustomException {
        sessionFactory = null;
        return getSessionFactory();
    }

    public static HashMap<String, String> getConnectionInfo() throws CustomException {
        HashMap<String, String> connInfoMap = new HashMap<>();
        SessionFactory sessionFactory = getSessionFactory();
        if (sessionFactory == null){
            return null;
        }
        Session session = sessionFactory.openSession();
        ConnectionInfo connectionInfo = new ConnectionInfo();
        session.doWork(connectionInfo);
        connInfoMap.put("DataBaseProductName", connectionInfo.getDataBaseProductName());
        connInfoMap.put("DataBaseUrl", connectionInfo.getDataBaseUrl());
        connInfoMap.put("DriverName", connectionInfo.getDriverName());
        connInfoMap.put("Username", connectionInfo.getUsername());
        for (String c : connInfoMap.keySet()) {
            LOGGER.info(String.format("%s : %s", c, connInfoMap.get(c)));
        }
        session.close();
        return connInfoMap;
    }
}
