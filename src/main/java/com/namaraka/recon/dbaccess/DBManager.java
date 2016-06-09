package com.namaraka.recon.dbaccess;

import com.namaraka.recon.ApplicationPropertyLoader;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import com.namaraka.recon.constants.LinkType;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.ReconTransactionsTable;
import com.namaraka.recon.model.v1_0.ReportDetails;
import com.namaraka.recon.utilities.AuditTrailInterceptor;
import com.namaraka.recon.utilities.CallBack;
import com.namaraka.recon.utilities.FileProcessDeterminants;
import com.namaraka.recon.utilities.GlobalAttributes;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.Criteria;
import org.hibernate.EmptyInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionException;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

/**
 *
 * @author smallgod
 */
public final class DBManager {

    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);

    private static SessionFactory getSessionFactory() {
        return ApplicationPropertyLoader.loadInstance().getSessionFactory();
    }

    private static Session getSession() {

        Session session = null;
        EmptyInterceptor interceptor = new AuditTrailInterceptor();

        try {

            session = getSessionFactory().getCurrentSession();

            if (!session.isOpen()) {
                session = getSessionFactory().openSession();
            }

        } catch (HibernateException he) {
            logger.error("Hibernate exception: " + he.getMessage());

        }
        return session;
    }

    private static StatelessSession getStatelessSession() {

        StatelessSession statelessSession;
        EmptyInterceptor interceptor = new AuditTrailInterceptor();
        try {
            statelessSession = getSessionFactory().openStatelessSession();
            logger.debug("openned stateless session");
        } catch (HibernateException he) {
            logger.error("Hibernate exception openning stateless session: " + he.getMessage());
            throw new NullPointerException("Couldnot create open a statelesssession");
        }
        return statelessSession;
    }

    public static void closeSession(Session tempSession) {

        if (tempSession != null) {
            try {

                if (tempSession.isConnected()) {
                    tempSession.disconnect();
                }

                if (tempSession.isOpen()) {
                    tempSession.close();
                }

                tempSession = null;

            } catch (HibernateException hbe) {
                logger.error("Couldn't close Session: " + hbe.getMessage());
            }
        }
    }

    public static void closeSession(StatelessSession tempSession) {

        if (tempSession != null) {
            tempSession.close();
        }
    }

    /**
     * *
     * Call back function to insert or update record
     *
     * @param <T>
     * @param callBack
     * @param reportFileDetails
     * @param noOfRecordsInDB
     * @return
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static <T> int insertOrUpdateRecord(CallBack callBack, ReportDetails reportFileDetails, long noOfRecordsInDB) throws MyCustomException {

        StatelessSession tempSession = getStatelessSession();
        Transaction transaction = null;
        int recordsIterated = 0;

        LinkType linkType = reportFileDetails.getLinkType();

        try {

            transaction = tempSession.beginTransaction();

            if (linkType == LinkType.LINKED) {
                recordsIterated = callBack.readRecordsFromDB(reportFileDetails, tempSession, noOfRecordsInDB);
            } else {
                recordsIterated = callBack.readRecordsFromFile(reportFileDetails, tempSession, noOfRecordsInDB);
            }

            transaction.commit();

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception inserting/updating: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception saving object list: " + e.getCause().getMessage());
        } finally {
            closeSession(tempSession);
        }

        return recordsIterated;
    }

    public static <T> int persistTempRecords(CallBack callBack, ReportDetails reportFileDetails) throws MyCustomException {

        StatelessSession tempSession = getStatelessSession();
        Transaction transaction = null;
        int recordsIterated = 0;

        try {

            transaction = tempSession.beginTransaction();

            recordsIterated = callBack.processLinkFileRecords(reportFileDetails, tempSession);

            transaction.commit();

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception inserting/updating: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            logger.error("General exception saving object list: " + e.getCause().getMessage());
        } finally {
            closeSession(tempSession);
        }

        return recordsIterated;
    }

    /**
     * Save object to database
     *
     * @param <T>
     * @param dbObject
     * @return Database ID of saved object
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static <T> long persistDatabaseModel(T dbObject) throws MyCustomException {

        long dbObjectId = 0;
        Session tempSession = getSession();
        Transaction transaction = null;

        try {

            transaction = tempSession.beginTransaction();
            dbObjectId = (Long) tempSession.save(dbObject);
            transaction.commit();

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new MyCustomException("Hibernate Error saving object to DB", ErrorCode.PROCESSING_ERR, "Error saving: " + he.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new MyCustomException("Error saving object to DB", ErrorCode.PROCESSING_ERR, "Error saving: " + e.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {
            closeSession(tempSession);
        }

        return dbObjectId;
    }

    public static <T> long persistDatabaseModel(String entityName, T dbObject) {

        long dbObjectId = 0;
        Session tempSession = getSession();
        Transaction transaction = null;

        try {

            transaction = tempSession.beginTransaction();
            dbObjectId = (Long) tempSession.save(entityName, dbObject);
            transaction.commit();

            logger.debug(">>>> Object saved to DB");

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception saving DB object: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception saving DB object: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return dbObjectId;
    }

    /**
     * *
     * Check if record exists using property name and classEntity
     *
     * @param <T>
     * @param properyName
     * @param propertyValue
     * @param classEntity
     * @return true if record exists in database entity
     */
    public static <T> boolean checkIfRecordExists(String properyName, T propertyValue, Class<T> classEntity) {

        StatelessSession tempSession = getStatelessSession();
        boolean isExistent = false;

        try {

            Criteria criteria = tempSession.createCriteria(classEntity);
            criteria.add(Restrictions.eq(properyName, propertyValue).ignoreCase());
            criteria.setProjection(Projections.rowCount());
            Long rowCount = (Long) criteria.uniqueResult();

            if (rowCount > 0) {
                isExistent = true;
            }

        } catch (HibernateException he) {
            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {
            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return isExistent;
    }

    /**
     * Bulk insertion using a statelessSession to improve performance
     *
     * @param <T>
     * @param dbObjectList
     * @return
     */
    public static <T> boolean bulkInsert(List<T> dbObjectList) {

        StatelessSession tempSession = getStatelessSession();
        Transaction transaction = null;
        boolean committed = false;

//        ScrollableResults customers = tempSession.getNamedQuery("GetCustomers").scroll(ScrollMode.FORWARD_ONLY);
//
//        while (customers.next()) {
//            Customer customer = (Customer) customers.get(0);
//            customer.updateStuff(
//            ....);
//            tempSession.update(customer);
//        }
//
//        transaction.commit();
//        tempSession.close();
        try {

            transaction = tempSession.beginTransaction();
            for (T dbObject : dbObjectList) {
                tempSession.insert(dbObject);
            }
            transaction.commit();
            committed = true;

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return committed;
    }

    public static <T> boolean bulkInsertBatch(List<T> dbObjectList) throws MyCustomException {

        int i = 0;

        Session tempSession = getSession();
        Transaction transaction = null;
        boolean committed = false;

        try {

            transaction = tempSession.beginTransaction();
            for (T dbObject : dbObjectList) {

                tempSession.save(dbObject);

                if (i % GlobalAttributes.HIBERNATE_JDBC_BATCH == 0) { // Same as the JDBC batch size
                    //flush a batch of inserts and release memory:
                    tempSession.flush();
                    tempSession.clear();
                }
            }

            transaction.commit();
            committed = true;

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception saving object list: " + e.getMessage());
        } finally {

            closeSession(tempSession);
        }

        return committed;
    }

    /**
     *
     * @param <T>
     * @param dbObjectList
     * @return
     */
    public static <T> boolean bulkUpdate(Set<T> dbObjectList) {

        StatelessSession tempSession = getStatelessSession();
        Transaction transaction = null;
        boolean committed = false;

        try {

            transaction = tempSession.beginTransaction();
            for (T dbObject : dbObjectList) {
                tempSession.update(dbObject);
            }
            transaction.commit();
            committed = true;

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return committed;
    }

    /**
     * *
     * Bulk fetch reconTable IDs
     *
     * @param entityName
     * @return
     */
    public static Set<String> bulkIDFetch(String entityName) {

        StatelessSession tempSession = getStatelessSession();
        Set<String> results = new HashSet<>();

        try {

            Criteria criteria = tempSession.createCriteria(entityName);
            //criteria.addOrder(Order.asc(propertyName));
            //criteria.add(Restrictions.eq(propertyName, propertyValue));

            ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);
            ReconTransactionsTable reconTable;

            int count = 0;
            while (scrollableResults.next()) {
                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }

                reconTable = (ReconTransactionsTable) scrollableResults.get()[0];
                results.add(reconTable.getIDValue());
            }

        } catch (HibernateException he) {

            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {

            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return results;
    }

    /**
     *
     * @param <T>
     * @param persistentClassType
     * @return
     */
    public static Set bulkFetch(Class persistentClassType) {

        logger.debug("going to bulkFetch records");

        StatelessSession tempSession = getStatelessSession();
        Set<Object> results = new HashSet<>();

        try {

            Criteria criteria = tempSession.createCriteria(persistentClassType);
            //criteria.addOrder(Order.asc(propertyName));
            //criteria.add(Restrictions.eq(propertyName, propertyValue));

            ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);

            int count = 0;
            while (scrollableResults.next()) {
                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }
                results.add(scrollableResults.get()[0]);

            }

            logger.debug("BulkFetched num of records: " + results.size());

        } catch (HibernateException he) {

            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {

            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return results;
    }

    /**
     *
     * @param <T>
     * @param returnType
     * @param method
     * @param target
     * @param argument
     * @param expectedReturn
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static <T> void foo(Class<T> returnType, Method method, String target, Object argument, T expectedReturn) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        T actualReturn = returnType.cast(method.invoke(target, argument));
        System.out.print(actualReturn.equals(expectedReturn));
    }

    /**
     * fetch records by property name & class type
     *
     * @param persistentClassType
     * @param propertyName
     * @param propertyValue
     * @return
     */
    public static Set bulkFetchByPropertyName(Class persistentClassType, String propertyName, Object propertyValue) {

        StatelessSession tempSession = getStatelessSession();
        Set<Object> results = new HashSet<>();

        try {

            Criteria criteria = tempSession.createCriteria(persistentClassType);
            //criteria.addOrder(Order.asc(propertyName));
            criteria.add(Restrictions.eq(propertyName, propertyValue));

            ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);

            int count = 0;
            while (scrollableResults.next()) {
                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }
                results.add(scrollableResults.get()[0]);

            }
        } catch (HibernateException he) {

            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {

            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return results;
    }

    public static Set bulkFetchByPropertyName(Class persistentClassType, String propertyName, Object propertyValue, StatelessSession tempSession) {

        Set<Object> results = new HashSet<>();

        try {

            Criteria criteria = tempSession.createCriteria(persistentClassType);
            //criteria.addOrder(Order.asc(propertyName));
            criteria.add(Restrictions.eq(propertyName, propertyValue));

            ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);

            int count = 0;
            while (scrollableResults.next()) {
                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }
                results.add(scrollableResults.get()[0]);

            }
        } catch (HibernateException he) {
            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {
            logger.error("General exception saving object list: " + e.getMessage());
        }
        return results;
    }

    /**
     * Fetch records from database in bulk using entity name
     *
     * @param <T>
     * @param entityName
     * @return
     */
    public static Set<Object> bulkFetch(String entityName) {

        StatelessSession tempSession = getStatelessSession();
        Set<Object> results = new HashSet<>();

        try {

            Criteria criteria = tempSession.createCriteria(entityName);
            //criteria.addOrder(Order.asc(propertyName));
            //criteria.add(Restrictions.eq(propertyName, propertyValue));

            ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);

            int count = 0;
            while (scrollableResults.next()) {
                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }
                results.add(scrollableResults.get()[0]);

            }

        } catch (HibernateException he) {

            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {

            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return results;
    }

    /**
     * Only Fetch one column from the result set
     *
     * @param <T>
     * @param classType
     * @param columToFetch
     * @param restrictToPropertyName
     * @param restrictionValue
     * @return
     */
    public static <T> List<T> fetchOnlyColumn(Class classType, String columToFetch, String restrictToPropertyName, Object restrictionValue) {

        StatelessSession tempSession = getStatelessSession();
        List<T> results = new ArrayList<>();

        try {

            //Criteria.forClass(bob.class.getName())
            Criteria criteria = tempSession.createCriteria(classType);
            //criteria.add(Restrictions.gt("id", 10));
            criteria.setProjection(Projections.property(columToFetch));
            criteria.add(Restrictions.eq(restrictToPropertyName, restrictionValue)); //transactions should belong to the same group
            //criteria.addOrder(Order.asc(propertyName));

            ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);

            int count = 0;
            while (scrollableResults.next()) {
                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }
                results.add((T) scrollableResults.get()[0]);

            }

        } catch (HibernateException he) {

            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {

            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return results;
    }

    /**
     *
     * @param <T>
     * @param classType
     * @param columToFetch
     * @param restrictToPropertyName
     * @param restrictionValue
     * @return
     */
    public static <T> List<T> fetchOnlyColumnWithCollection(Class classType, String columToFetch, String restrictToPropertyName, Collection<?> restrictionValue) {

        StatelessSession tempSession = getStatelessSession();
        List<T> results = new ArrayList<>();

        try {

            //Criteria.forClass(bob.class.getName())
            Criteria criteria = tempSession.createCriteria(classType);
            //criteria.add(Restrictions.gt("id", 10));
            criteria.setProjection(Projections.property(columToFetch));
            criteria.add(Restrictions.in(restrictToPropertyName, restrictionValue));
            //criteria.addOrder(Order.asc(propertyName));

            ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);

            int count = 0;
            while (scrollableResults.next()) {
                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }
                results.add((T) scrollableResults.get()[0]);

            }

        } catch (HibernateException he) {

            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {

            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return results;
    }

    /**
     * Only Fetch one column from the result set
     *
     * @param <T>
     * @param classType
     * @param columToFetch
     * @param restrictToPropertyName
     * @param fileID
     * @param fileCount
     * @return
     */
    public static <T> List<T> bulkFetchExceptions(Class classType, String columToFetch, String restrictToPropertyName, String fileID, int fileCount) {

        StatelessSession tempSession = getStatelessSession();
        List<T> results = new ArrayList<>();

        Type intType = IntegerType.INSTANCE;

        try {

            String hqlQuery = "SELECT rec.rowDetails FROM TemporaryRecords  as rec WHERE rec.fileID=:fileID and rec.generatedID NOT IN "
                    + "(SELECT rec.generatedID FROM TemporaryRecords where rec.generatedID Like '%F' or rec.generatedID like '%S' GROUP BY rec.generatedID HAVING COUNT(rec.generatedID) =:filecount)";

            String sqlQuery = "SELECT row_details FROM temporary_records WHERE file_id=:fileID AND generated_id NOT IN "
                    + "(SELECT generated_id FROM temporary_records WHERE generated_id Like '%F' OR generated_id LIKE '%S' GROUP BY generated_id HAVING COUNT(generated_id) =:filecount)";

//            String sqlQuery = "SELECT row_details FROM temporary_records WHERE generated_id NOT IN "
//                    + "(SELECT generated_id FROM temporary_records where generated_id Like '%F' or generated_id like '%S' GROUP BY generated_id HAVING COUNT(generated_id) =:filecount)";
            //Query query = tempSession.createQuery(hqlQuery);
            Query query = tempSession.createSQLQuery(sqlQuery);

            query.setParameter("fileID", fileID);
            query.setParameter("filecount", fileCount);

            //SELECT generated_id, row_details FROM temporary_records WHERE generated_id IN  (SELECT generated_id FROM temporary_records GROUP BY generated_id HAVING COUNT(generated_id) =2)
            //Criteria.forClass(bob.class.getName())
            //Criteria criteria = tempSession.createCriteria(classType);
            //criteria.add(Restrictions.or(Property.forName("col3").eq("value3"), Property.forName("col4").eq("value3")));       
            //we only want successful & Failed transactions (rest are exceptions)
            /*criteria.setProjection(Projections.property(columToFetch));
             criteria.add(
             //Restrictions.not(
                    
             Restrictions.or(
             Restrictions.ilike(restrictToPropertyName, "S", MatchMode.END),
             Restrictions.ilike(restrictToPropertyName, "F", MatchMode.END)
             )
             )
             .add(Restrictions.sqlRestriction("generated_id having count(generated_id) = ?", fileCount, intType));*/
            logger.debug("Query size :: " + query.list().size());

            ScrollableResults scrollableResults = query.scroll(ScrollMode.FORWARD_ONLY);

            int count = 0;
            while (scrollableResults.next()) {

                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }
                results.add((T) scrollableResults.get()[0]);
            }

        } catch (HibernateException he) {

            logger.error("hibernate exception while extracting exceptions: " + he.getMessage());
        } catch (Exception e) {

            logger.error("General exception while extracting exceptions: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return results;
    }

    /**
     * Get Selected columns from Temporary Records Table
     *
     * @param <T>
     * @param classType
     * @param fileID
     * @return
     */
    public static <T> List<T> bulkFetchSelectedColumns(Class classType) {

        StatelessSession tempSession = getStatelessSession();
        List<T> results = new ArrayList<>();

        try {

            Criteria criteria = tempSession.createCriteria(classType)
                    //.add(Restrictions.eq("fileID", fileID))
                    .setProjection(Projections.projectionList()
                            .add(Projections.property("generatedID"), "generatedID")
                            .add(Projections.property("isFailedOrSuccessful"), "isFailedOrSuccessful")
                            .add(Projections.property("fileID"), "fileID")
                            .add(Projections.property("rowDetails"), "rowDetails"))
                    .setResultTransformer(Transformers.aliasToBean(classType));

            //List list = criteria.list();
            ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);

            int count = 0;
            while (scrollableResults.next()) {

                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }
                results.add((T) scrollableResults.get()[0]);
            }

        } catch (HibernateException he) {

            logger.error("hibernate exception while extracting exceptions: " + he.getMessage());
        } catch (Exception e) {

            logger.error("General exception while extracting exceptions: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return results;
    }

    /**
     *
     * @param <T>
     * @param classType
     * @param columToFetch
     * @param propName1
     * @param propValue1
     * @param propName2
     * @param propValue2
     * @return
     */
    public static <T> List<T> fetchOnlyColumn(Class classType, String columToFetch, String propName1, Object propValue1, String propName2, Object propValue2) {

        StatelessSession tempSession = getStatelessSession();
        List<T> results = new ArrayList<>();

        try {

            //Criteria.forClass(bob.class.getName())
            Criteria criteria = tempSession.createCriteria(classType);
            //criteria.add(Restrictions.gt("id", 10));
            criteria.setProjection(Projections.property(columToFetch));
            criteria.add(Restrictions.eq(propName1, propValue1)); //transactions should belong to the same group
            criteria.add(Restrictions.eq(propName2, propValue2));
            //criteria.addOrder(Order.asc(propertyName));

            ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);

            int count = 0;
            while (scrollableResults.next()) {
                if (++count > 0 && count % 10 == 0) {
                    logger.debug("Fetched " + count + " entities");
                }
                results.add((T) scrollableResults.get()[0]);

            }

        } catch (HibernateException he) {

            logger.error("hibernate exception saving object list: " + he.getMessage());
        } catch (Exception e) {

            logger.error("General exception saving object list: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return results;
    }

    /**
     * Update Obect in database
     *
     * @param <T>
     * @param dbObject
     */
    protected static <T> void updateDatabaseModel1(T dbObject) {

        Session tempSession = getSession();
        Transaction transaction = null;

        try {
            transaction = tempSession.beginTransaction();
            tempSession.update(dbObject);
            //tempSession.update(tempSession.merge(dbObject));
            //tempSession.flush();
            transaction.commit();

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception saving DB object: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception saving DB object: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }
    }

    public static void updateDatabaseModel(Object dbObject) {

        Session tempSession = getSession();
        Transaction transaction = null;

        try {
            transaction = tempSession.beginTransaction();
            tempSession.update(dbObject);
            //retrievedDatabaseModel = (T)getSession().get(persistentClass, objectId);

            //retrievedDatabaseModel = (T)tempSession.merge(object);
            //tempSession.update(retrievedDatabaseModel);
            //retrievedDatabaseModel = dbObject;
            //tempSession.update(tempSession.merge(retrievedDatabaseModel));
            //tempSession.update(retrievedDatabaseModel);
            tempSession.flush();
            transaction.commit();

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception updating DB object: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception updating DB object: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }
    }

    /**
     * Read object from database by Id
     *
     * @param <T>
     * @param persistentClass
     * @param objectId
     * @return <code> DBMSXMLObject </code>
     * @throws NullPointerException
     */
    public static <T> T retrieveDatabaseModel(Class<T> persistentClass, long objectId) throws NullPointerException {

        Session tempSession = getSession();
        Transaction transaction = null;
        T retrievedDatabaseModel = null;

        try {

            transaction = tempSession.beginTransaction();
            retrievedDatabaseModel = (T) getSession().get(persistentClass, objectId);
            transaction.commit();

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception reading DB object: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception reading DB object: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        if (retrievedDatabaseModel == null) {
            throw new NullPointerException("Got Null retrieving DB object");
        }

        return retrievedDatabaseModel;
    }

    /**
     * *
     * retrieve all records and order by propertyName
     *
     * @param <T>
     * @param persistentClassType
     * @param propertyName
     * @return
     */
    public static <T> List<T> retrieveAllDatabaseRecords(Class<T> persistentClassType, String propertyName) {

        Session tempSession = getSession();
        Transaction transaction = null;
        List<T> results = null;

        try {

            transaction = tempSession.beginTransaction();
            Criteria criteria = tempSession.createCriteria(persistentClassType);
            criteria.addOrder(Order.asc(propertyName));
            //criteria.add(Restrictions.eq(propertyName, propertyValue));
            results = criteria.list();

            /*for (Iterator iterator = results.iterator();
             iterator.hasNext();) {
             Employee employee = (Employee) iterator.next();
             System.out.print("First Name: " + employee.getFirstName());
             System.out.print("  Last Name: " + employee.getLastName());
             System.out.println("  Salary: " + employee.getSalary());
             }*/
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception querying DB: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception querying DB: " + e.getMessage());
        } finally {
            closeSession(tempSession);
            //tempSession.clear();
        }

        return results;
    }

    /**
     * *
     * Get all records that correspond to a given property name
     *
     * @param <T>
     * @param persistentClassType
     * @param propertyName
     * @param propertyValue
     * @return
     */
    public static <T> List<T> retrieveAllDatabaseRecords(Class<T> persistentClassType, String propertyName, String propertyValue) {

        Session tempSession = getSession();
        Transaction transaction = null;
        List<T> results = null;

        try {

            transaction = tempSession.beginTransaction();
            Criteria criteria = tempSession.createCriteria(persistentClassType);
            //criteria.addOrder(Order.asc(propertyName));
            criteria.add(Restrictions.eq(propertyName, propertyValue));
            results = criteria.list();

            /*for (Iterator iterator = results.iterator();
             iterator.hasNext();) {
             Employee employee = (Employee) iterator.next();
             System.out.print("First Name: " + employee.getFirstName());
             System.out.print("  Last Name: " + employee.getLastName());
             System.out.println("  Salary: " + employee.getSalary());
             }*/
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception querying DB: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception querying DB: " + e.getMessage());
        } finally {
            closeSession(tempSession);
            //tempSession.clear();
        }

        return results;
    }

    /**
     *
     * @param <T>
     * @param persistentClassType
     * @param propertyName
     * @param propertyValues
     * @return
     */
    public static <T> List<T> retrieveAllDatabaseRecords(Class<T> persistentClassType, String propertyName, Collection<?> propertyValues) {

        Session tempSession = getSession();
        Transaction transaction = null;
        List<T> results = null;

        try {

            transaction = tempSession.beginTransaction();
            Criteria criteria = tempSession.createCriteria(persistentClassType);
            //criteria.addOrder(Order.asc(propertyName));
            criteria.add(Restrictions.in(propertyName, propertyValues));
            results = criteria.list();

            /*for (Iterator iterator = results.iterator();
             iterator.hasNext();) {
             Employee employee = (Employee) iterator.next();
             System.out.print("First Name: " + employee.getFirstName());
             System.out.print("  Last Name: " + employee.getLastName());
             System.out.println("  Salary: " + employee.getSalary());
             }*/
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception querying DB: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception querying DB: " + e.getMessage());
        } finally {
            closeSession(tempSession);
            //tempSession.clear();
        }

        return results;
    }

    public static <T> List<T> retrieveAllDatabaseRecords(Session tempSession, Class<T> persistentClassType, String propertyName, Collection<?> propertyValues) {

        Transaction transaction = null;
        List<T> results = null;

        try {

            transaction = tempSession.beginTransaction();
            Criteria criteria = tempSession.createCriteria(persistentClassType);
            //criteria.addOrder(Order.asc(propertyName));
            criteria.add(Restrictions.in(propertyName, propertyValues));
            results = criteria.list();

            /*for (Iterator iterator = results.iterator();
             iterator.hasNext();) {
             Employee employee = (Employee) iterator.next();
             System.out.print("First Name: " + employee.getFirstName());
             System.out.print("  Last Name: " + employee.getLastName());
             System.out.println("  Salary: " + employee.getSalary());
             }*/
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception querying DB: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception querying DB: " + e.getMessage());
        } finally {
            //closeSession(tempSession);
            //tempSession.clear();
        }

        return results;
    }

    /**
     * *
     * retrieve all records without ordering them
     *
     * @param <T>
     * @param persistentClassType
     * @return
     */
    public static List<?> retrieveAllDatabaseRecords(Class<?> persistentClassType) {

        Session tempSession = getSession();
        Transaction transaction = null;

        List<?> results = null;

        try {

            transaction = tempSession.beginTransaction();
            Criteria criteria = tempSession.createCriteria(persistentClassType);
            //criteria.addOrder(Order.asc(propertyName));
            //criteria.add(Restrictions.eq(propertyName, propertyValue));
            results = criteria.list();
            transaction.commit();

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception  querying DB: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception querying DB: " + e.getMessage());
        } finally {
            closeSession(tempSession);
            //tempSession.clear();
        }

        return results;
    }

    /**
     * Count table records using class name
     *
     * @param <T>
     * @param persistentClass
     * @return
     * @throws NullPointerException
     */
    public static <T> long countRecords(Class<T> persistentClass) throws NullPointerException {

        StatelessSession tempSession = getStatelessSession();
        long count = -1L;

        try {

            count = (Long) tempSession.createCriteria(persistentClass).setProjection(Projections.rowCount()).uniqueR‌esult();

        } catch (HibernateException he) {
            logger.error("hibernate exception counting DB objects: " + he.getMessage());
        } catch (Exception e) {
            logger.error("General exception counting DB objects: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return count;
    }

    /**
     *
     * @param <T>
     * @param persistentClass
     * @param propertyName
     * @param propertyValue
     * @return
     * @throws NullPointerException
     */
    public static <T> long countRecords(Class<T> persistentClass, String propertyName, Object propertyValue) throws NullPointerException {

        StatelessSession tempSession = getStatelessSession();
        long count = -1L;

        try {

            Criteria criteria = tempSession.createCriteria(persistentClass);
            criteria.add(Restrictions.eq(propertyName, propertyValue));
            count = (Long) criteria.setProjection(Projections.rowCount()).uniqueR‌esult();

        } catch (HibernateException he) {
            logger.error("hibernate exception counting DB objects: " + he.getMessage());
        } catch (Exception e) {
            logger.error("General exception counting DB objects: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        return count;
    }

    public static <T> long countRecords(Class<T> persistentClass, String propertyName1, Object propertyValue1, String propertyName2, Object propertyValue2) throws NullPointerException, MyCustomException {

        StatelessSession tempSession = getStatelessSession();
        long count = -1L;

        try {

            Criteria criteria = tempSession.createCriteria(persistentClass);
            criteria.add(Restrictions.eq(propertyName1, propertyValue1));
            criteria.add(Restrictions.eq(propertyName2, propertyValue2));

            count = (Long) criteria.setProjection(Projections.rowCount()).uniqueR‌esult();

        } catch (HibernateException he) {

            throw new MyCustomException("Hibernate Error counting", ErrorCode.PROCESSING_ERR, "Error counting: " + he.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (Exception e) {

            throw new MyCustomException("Error saving counting", ErrorCode.PROCESSING_ERR, "Error counting: " + e.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {
            closeSession(tempSession);
        }

        return count;
    }

    /**
     *
     * @param <T>
     * @param persistentClass
     * @param propertyName
     * @param propertyValues
     * @return
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static <T> int countRecords(Class<T> persistentClass, String propertyName, Collection<?> propertyValues) throws MyCustomException {

        StatelessSession tempSession = getStatelessSession();
        int count = -1;

        try {

            Criteria criteria = tempSession.createCriteria(persistentClass);
            criteria.add(Restrictions.in(propertyName, propertyValues));
            count = Integer.parseInt(String.valueOf(criteria.setProjection(Projections.rowCount()).uniqueR‌esult()));

        } catch (HibernateException he) {

            throw new MyCustomException("Hibernate Error counting", ErrorCode.PROCESSING_ERR, "Error counting: " + he.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (Exception e) {

            throw new MyCustomException("Error counting", ErrorCode.PROCESSING_ERR, "Error counting: " + e.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {
            closeSession(tempSession);
        }

        return count;
    }

    /**
     * *
     * Count table records using entity name
     *
     * @param entityName
     * @return
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     *
     */
    public static long countRecords(String entityName) throws MyCustomException {

        logger.debug("counting records from entityName:" + entityName);

        //Session tempSession = getSession();
        StatelessSession tempSession = getStatelessSession();
        long count = -1L;

        try {

            //change to entity name from class
            count = (Long) tempSession.createCriteria(entityName).setProjection(Projections.rowCount()).uniqueR‌esult();

        } catch (HibernateException he) {

            throw new MyCustomException("Hibernate Error counting", ErrorCode.PROCESSING_ERR, "Error counting: " + he.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (Exception e) {

            throw new MyCustomException("Error counting", ErrorCode.PROCESSING_ERR, "Error counting: " + e.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {
            closeSession(tempSession);
        }

        return count;
    }

    /**
     * *
     * Delete all records using entityName
     *
     * @param entityName
     * @return
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     *
     */
    public static int deleteAllRecords(String entityName) throws MyCustomException {

        Session tempSession = getSession();
        int recordsDeleted = -1;

        try {

            String deleteHQLquery = String.format("DELETE FROM %s", entityName);
            Query query = tempSession.createQuery(deleteHQLquery);
            recordsDeleted = query.executeUpdate();

        } catch (HibernateException he) {

            throw new MyCustomException("Hibernate Error deleting", ErrorCode.PROCESSING_ERR, "Error deleting: " + he.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (Exception e) {

            throw new MyCustomException("Error deleting object", ErrorCode.PROCESSING_ERR, "Error deleting: " + e.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {
            closeSession(tempSession);
        }

        return recordsDeleted;
    }

    /**
     * Read object from database by Id
     *
     * @param <T>
     * @param persistentClass
     * @param id
     * @return
     * @throws NullPointerException
     */
    public static <T> T retrieveDatabaseModel2(Class<T> persistentClass, int id) throws NullPointerException {

        Session tempSession = getSession();
        Transaction transaction = null;
        T retrievedDatabaseModel = null;

        try {

            transaction = tempSession.beginTransaction();
            retrievedDatabaseModel = persistentClass.cast(getSession().get(persistentClass, id));
            transaction.commit();

        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception reading DB object: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception reading DB object: " + e.getMessage());
        } finally {
            closeSession(tempSession);
        }

        if (retrievedDatabaseModel == null) {
            throw new NullPointerException("Got Null retrieving DB object");
        }

        return retrievedDatabaseModel;
    }

    /**
     * Delete object from database
     *
     * @param <T>
     * @param persistentClass
     * @param objectId
     */
    public static <T> void deleteDatabaseModel(Class<T> persistentClass, long objectId) throws NullPointerException {

        Session tempSession = getSession();
        Transaction transaction = null;
        T retrievedDatabaseModel;

        try {

            transaction = tempSession.beginTransaction();
            //tempSession.buildLockRequest(LockOptions.NONE).lock(dbObject);
            retrievedDatabaseModel = (T) getSession().get(persistentClass, objectId);

            if (retrievedDatabaseModel == null) {
                throw new NullPointerException("Delete object NOT found");
            }
            tempSession.delete(retrievedDatabaseModel);
            //tempSession.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception deleting DB object: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception deleting DB object: " + e.getMessage());
        } finally {
            closeSession(tempSession);
            //tempSession.clear();
        }
    }

    /*public String getPassword(String userName) {
     Transaction trns = null;
     Session session = HibernateUtil.getSessionFactory().openSession();
     try {
     trns = session.beginTransaction();
     List result = session.createQuery("select password from UserDetails where userName=:userName")
     .setParameter("userName", user.getUserName())
     .list();
     trns.commit();
     } catch (RuntimeException e) {
     e.printStackTrace();
     s} finally {
     session.flush();
     session.close();
     }
     return getPassword(user.getUserName());
     }
     }*/
    /**
     * Get all Records whose property value corresponding to propertyName is
     * given
     *
     * @param <T>
     * @param classType
     * @param propertyName
     * @param propertyValue
     * @return A list of records whose property value corresponds to the
     * propertyName given
     */
    public static <T> List<T> getRecordsEqualToPropertyValue(Class<T> classType, String propertyName, Object propertyValue) {

        Session tempSession = getSession();
        Transaction transaction = null;
        List<T> results = null;

        try {

            transaction = tempSession.beginTransaction();
            Criteria criteria = tempSession.createCriteria(classType);
            criteria.add(Restrictions.eq(propertyName, propertyValue));
            results = criteria.list();

            if (results.size() > Integer.MAX_VALUE) {
                logger.warn("records in DB more than what integer can hold");
            }

            /*for (Iterator iterator = results.iterator();
             iterator.hasNext();) {
             Employee employee = (Employee) iterator.next();
             System.out.print("First Name: " + employee.getFirstName());
             System.out.print("  Last Name: " + employee.getLastName());
             System.out.println("  Salary: " + employee.getSalary());
             }*/
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception querying DB: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception querying DB: " + e.getMessage());
        } finally {
            closeSession(tempSession);
            //tempSession.clear();
        }

        return results;
    }

    /**
     *
     * @param <T>
     * @param classType
     * @param propertyName1
     * @param propertyValue1
     * @param propertyName2
     * @param propertyValue2
     * @return
     */
    public static <T> List<T> getRecordsEqualToPropertyValue(Class<T> classType, String propertyName1, Object propertyValue1, String propertyName2, Object propertyValue2) {

        Session tempSession = getSession();
        Transaction transaction = null;
        List<T> results = null;

        try {

            transaction = tempSession.beginTransaction();
            Criteria criteria = tempSession.createCriteria(classType);
            criteria.add(Restrictions.eq(propertyName1, propertyValue1));
            criteria.add(Restrictions.eq(propertyName2, propertyValue2));

            results = criteria.list();

            if (results.size() > Integer.MAX_VALUE) {
                logger.warn("records in DB more than what integer can hold");
            }

            /*for (Iterator iterator = results.iterator();
             iterator.hasNext();) {
             Employee employee = (Employee) iterator.next();
             System.out.print("First Name: " + employee.getFirstName());
             System.out.print("  Last Name: " + employee.getLastName());
             System.out.println("  Salary: " + employee.getSalary());
             }*/
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception querying DB: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception querying DB: " + e.getMessage());
        } finally {
            closeSession(tempSession);
            //tempSession.clear();
        }

        return results;
    }

    public static <T> List<T> getMasterFileRecordByReconGroupID(Class<T> classType, String reconGroupIDPropertyName, Object propertyValue) {

        Session tempSession = getSession();
        Transaction transaction = null;
        List<T> results = null;

        try {

            transaction = tempSession.beginTransaction();
            Criteria criteria = tempSession.createCriteria(classType);

            criteria.add(Restrictions.eq(reconGroupIDPropertyName, propertyValue));
            criteria.add(Restrictions.eq("isMaster", Boolean.TRUE));

            results = criteria.list();

            if (results.size() > Integer.MAX_VALUE) {
                logger.warn("records in DB more than what integer can hold");
            }

            logger.debug(">>>> number of records in db list: " + results.size());

            /*for (Iterator iterator = results.iterator();
             iterator.hasNext();) {
             Employee employee = (Employee) iterator.next();
             System.out.print("First Name: " + employee.getFirstName());
             System.out.print("  Last Name: " + employee.getLastName());
             System.out.println("  Salary: " + employee.getSalary());
             }*/
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception querying DB: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception querying DB: " + e.getMessage());
        } finally {
            closeSession(tempSession);
            //tempSession.clear();
        }
        return results;
    }

    public static <T> List<T> getRecordsEqualToPropertyValue(String entityName, String propertyName, Object propertyValue) {

        logger.debug("entityName inside getRecordsEqualToproperty:" + entityName);
        Session tempSession = getSession();
        Transaction transaction = null;
        List<T> results = null;

        try {

            transaction = tempSession.beginTransaction();
            Criteria criteria = tempSession.createCriteria(entityName);
            criteria.add(Restrictions.eq(propertyName, propertyValue));
            results = criteria.list();

            if (results.size() > Integer.MAX_VALUE) {
                logger.warn("records in DB more than what integer can hold");
            }

            logger.debug(">>>> number of records in db list: " + results.size());

            /*for (Iterator iterator = results.iterator();
             iterator.hasNext();) {
             Employee employee = (Employee) iterator.next();
             System.out.print("First Name: " + employee.getFirstName());
             System.out.print("  Last Name: " + employee.getLastName());
             System.out.println("  Salary: " + employee.getSalary());
             }*/
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("hibernate exception querying DB: " + he.getMessage());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("General exception querying DB: " + e.getMessage());
        } finally {
            closeSession(tempSession);
            //tempSession.clear();
        }

        return results;
    }

    /*Query query=  session.createQuery("from Account where name=?");
     Account user=(Account)session.setString(0,user.getName()).uniqueResult();
     if(user!=null){
     //Do whatever you want to do
     }else{
     //Insert user
     }
    
     List sales = session.createCriteria(Sale.class)
     .add(Expression.ge("date",startDate);
     .add(Expression.le("date",endDate);
     .addOrder( Order.asc("date") )
     .setFirstResult(0)
     .setMaxResults(10)
     .list();    
     */
//    private <T> T load(T t, Session tempSession) {
//        tempSession.buildLockRequest(LockOptions.NONE).lock(t);
//        return t;
//    }
//
//   
//
//    //String readableDate = DateUtilities.stringFormatJodaDates(sub.getDateRegistered(), DateUtilities.dateTimeFormat);
//    //total number of records
//    public int getTotalNoOfRecords(String tableName) {
//
//        //capitalise first letter to match class name
//        String tableNameWithCapital = tableName.substring(0, 1).toUpperCase() + tableName.substring(1);
//
//        Session session = getSessionFactory().getCurrentSession();
//        int totalNoOfRecords = 0;
//        try {
//
//            session.beginTransaction();
//            totalNoOfRecords = ((Long) session.createQuery("select recordsDeleted(*) from " + tableName).iterate().next()).intValue();
//            System.out.println(">>> total no. of records : " + totalNoOfRecords);
//            session.getTransaction().commit();
//        } catch (RuntimeException e) {
//            session.getTransaction().rollback();
//            throw e; // or display error message
//        }
//        return totalNoOfRecords;
//    }
//
//    /*	public ArrayList<DataBaseObject> getClient(String clientName) {
//
//     //Session session = getSessionFactory().openSession();
//     Session session = getSessionFactory().openSession();
//     String temp = clientName + "%"; // �%�+title+�%�;
//     ArrayList<DataBaseObject> connectionsList = null;
//     Transaction tx = null;
//		
//     try{
//     Query query = session.createQuery("from Client client where str(client.clientName) like ?");
//     query.setString(0, temp);
//			
//     tx = session.beginTransaction();
//			
//     List<DataBaseObject> listOfObjects = (List<DataBaseObject>) query.list();
//     connectionsList = CollectionsConverter.listToArrayList(listOfObjects);
//     //session.getTransaction().commit();
//     tx.commit();
//	
//     logger.debug(">>> size of array retrieved is: " + connectionsList.size());
//     }
//		
//     catch (RuntimeException e) {
//     if (tx != null) tx.rollback();
//     throw e; // or display error message
//     }
//     finally {
//     session.close();
//     }
//		
//     return connectionsList;
//     }*/
//    public ArrayList<DataBaseObject> getClient(String clientName) {
//
//        String temp = clientName + "%"; // �%�+title+�%�;
//        ArrayList<DataBaseObject> connectionsList = null;
//
//        try {
//
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery("from Client client where str(client.clientName) like ?");
//            query.setString(0, temp);
//
//            List<DataBaseObject> listOfObjects = (List<DataBaseObject>) query.list();
//            connectionsList = CollectionsConverter.listToArrayList(listOfObjects);
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//
//            logger.debug(">>> size of array retrieved is: " + connectionsList.size());
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            throw e; // or display error message
//        }
//        return connectionsList;
//    }
//
//    public ArrayList<DataBaseObject> getTodaySMSJobList(String queryString, String todayDate) {
//
//	//public static List getStudentInformation(Date sDate,Date eDate,String address,Session session){
//
//        /* 
//         * SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
//         boolean isFirstSearchCriterion = true; 
//
//         StringBuilder query = new StringBuilder(queryString);
//         query.append(" where scheduled_date = '" + date.format(sDate) + "'");
//		   
//		   
//		  
//         if(sDate!=null){
//         if(isFirstSearchCriterion){
//         query.append(" where enlisted >= '" + date.format(sDate) + "'");
//         }else{
//         query.append(" and enlisted >= '" + date.format(sDate) + "'");
//         }
//         isFirstSearchCriterion = false;
//         }
//
//         if(eDate!=null){
//         if(isFirstSearchCriterion){
//         query.append(" where enlisted <= '" + date.format(eDate) + "'");
//         }else{
//         query.append(" and enlisted <= '" + date.format(eDate) + "'");
//         }
//         isFirstSearchCriterion = false;
//         }
//
//         if(address!=null){
//         if(isFirstSearchCriterion){
//         query.append(" where address = '" + address+"'");
//         }else{
//         query.append(" and address = '" + address+"'");
//         }
//         isFirstSearchCriterion = false;
//         }
//
//         query.append(" order by date");
//         Query result = session.createQuery(query.toString());
//
//         return result.list();
//         }
//         */
//        //SELECT_SCHEDULED_SMS = "from ScheduledMessage sm where sm.scheduledDate like ?";
//        //select note from Communication n where n.subject=:subject and ((n.sentTo=:sentto and n.creator=:creator) or (n.sentTo:creator and n.creator=:sentto))
//        ArrayList<DataBaseObject> connectionsList = null;
//
//        String param1 = todayDate + "%";   //�%�+title+�%�;
//        String param2 = "pending";
//
//        try {
//
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
//
//            query.setString(0, param1);
//            query.setString(1, param2);
//
//            //billNumber is a long
//            //select b.billNumber from Bill b where str(b.billNumber) like :billNumber").setString("billNumber,your_parameter).list();
//            // Criterion c1 = Restrictions.like("stateName", "Virg", MatchMode.END);
//            //Criterion c1 = Restrictions.like("stateName", "Virg%");		 
//            // Criteria criteria = session.createCriteria(ScheduledMessage.class).add(Restrictions.like("scheduledDate", "street 1%", MatchMode.START));
//            //Criteria criteria = session.createCriteria(ScheduledMessage.class).add(Restrictions.like("scheduledDate", todayDate, MatchMode.START));
//            // List<DataBaseObject> listOfObjects = (List<DataBaseObject>)criteria.list();		   
//            List<DataBaseObject> listOfObjects = (List<DataBaseObject>) query.list();
//            connectionsList = CollectionsConverter.listToArrayList(listOfObjects);
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//
//            System.out.println("scheduled sms jobs retrieved from DB! ");
//            //session.close();
//
//            logger.debug(">>> size of array retrieved is: " + connectionsList.size() + "\n: print array: " + connectionsList.toString());
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);; // or display error message
//        }
//
//        return connectionsList;
//    }
//
//    public ArrayList<DataBaseObject> getRecordsInRange(String queryString, LocalDateTime startDate, LocalDateTime endDate, String status, int startPageIndex, int numRecordsPerPage, String jtSorting) {
//
//        String pagedQueryString = queryString + " ORDER BY " + jtSorting;
//
//        if (status.equalsIgnoreCase("all")) {
//            System.out.println(">>> keywordstatus == all");
//            status = "%";
//        }
//
//        ArrayList<DataBaseObject> connectionsList = null;
//
//        try {
//
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(pagedQueryString);
//            query.setString(0, status);
//            query.setParameter("startdate", startDate);
//            query.setParameter("enddate", endDate);
//            query.setFirstResult(startPageIndex);
//            query.setMaxResults(numRecordsPerPage);
//
//            List<DataBaseObject> listOfObjects = (List<DataBaseObject>) query.list();
//            System.out.println(">>> number of records retrieved : " + listOfObjects.size());
//            connectionsList = CollectionsConverter.listToArrayList(listOfObjects);
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);; // or display error message
//        }
//
//        System.out.println(">>>>>  number of records returned is: " + connectionsList.size());
//        return connectionsList;
//
//    }
//
//    public ArrayList<DataBaseObject> getContentRecordsInRange(String queryString, LocalDateTime startDate, LocalDateTime endDate, int startPageIndex, int numRecordsPerPage, String jtSorting) {
//
//        String pagedQueryString = queryString + " ORDER BY " + jtSorting;
//
//        ArrayList<DataBaseObject> connectionsList = null;
//
//        try {
//
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(pagedQueryString);
//            query.setParameter("startdate", startDate);
//            query.setParameter("enddate", endDate);
//            query.setFirstResult(startPageIndex);
//            query.setMaxResults(numRecordsPerPage);
//
//            List<DataBaseObject> listOfObjects = (List<DataBaseObject>) query.list();
//            System.out.println(">>> number of records retrieved : " + listOfObjects.size());
//            connectionsList = CollectionsConverter.listToArrayList(listOfObjects);
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e); // or display error message
//        }
//
//        return connectionsList;
//
//    }
//
//    public KeyWord getKeyWord(String queryString, String keyWord, String subkeyWord) {
//
//        KeyWord aKeyWord = null;
//        ArrayList<KeyWord> listOfKeyWords = null;
//
//        try {
//
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
//            query.setParameter("mainkeyword", keyWord);
//            query.setParameter("subkeyword", subkeyWord);
//
//            listOfKeyWords = (ArrayList<KeyWord>) query.list();
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);// or display error message
//        } finally {
//
//            if (listOfKeyWords != null) {
//
//                if (listOfKeyWords.size() > 0) {
//
//                    aKeyWord = (KeyWord) listOfKeyWords.get(0);
//                    System.out.println("keywordID for that keyword is : " + aKeyWord.getKeyWordID());
//
//                } else {
//                    logger.debug(">>>  records list returned is size 0");
//                }
//            } else {
//                logger.debug(">>> records list is NULL");
//            }
//        }
//
//        return aKeyWord;
//    }
//
//    public Subscriber getSubscriber(String queryString, String keyWord, String subkeyWord, String msisdn) {
//
//        Subscriber aSubscriber = null;
//        ArrayList<Subscriber> listOfSubscribers = null;
//
//        try {
//
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
//            query.setParameter("mainkeyword", keyWord);
//            query.setParameter("subkeyword", subkeyWord);
//            query.setParameter("msisdn", msisdn);
//
//            listOfSubscribers = (ArrayList<Subscriber>) query.list();
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> an errror : " + e);// or display error message
//        } finally {
//
//            if (listOfSubscribers != null) {
//
//                if (listOfSubscribers.size() > 0) {
//
//                    aSubscriber = (Subscriber) listOfSubscribers.get(0);
//
//                } else {
//                    logger.debug(">>>  records list returned is size 0");
//                }
//            } else {
//                logger.debug(">>> records list is NULL");
//            }
//        }
//
//        return aSubscriber;
//    }
//
//    public Content getContent(String queryString, KeyWord keyWord) {
//
//        ArrayList<Content> listOfContent = null;
//        Content aContent = null;
//
//        try {
//
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
//            query.setParameter("keyword", keyWord);
//
//            listOfContent = (ArrayList<Content>) query.list();
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> an errror : " + e);// or display error message
//        } finally {
//
//            if (listOfContent != null) {
//
//                if (listOfContent.size() > 0) {
//
//                    aContent = (Content) listOfContent.get(0);
//
//                } else {
//                    logger.debug(">>>  records list returned is size 0");
//                }
//            } else {
//                logger.debug(">>> records list is NULL");
//            }
//        }
//
//        return aContent;
//    }
//
//    public UnSubscriber getUnSubscriber(String queryString, Subscriber subscriber) {
//
//        ArrayList<UnSubscriber> listOfUnSubscribers = null;
//        UnSubscriber anUnSubscriber = null;
//
//        try {
//
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
//            query.setParameter("subscriber", subscriber);
//
//            listOfUnSubscribers = (ArrayList<UnSubscriber>) query.list();
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);// or display error message
//        } finally {
//
//            if (listOfUnSubscribers != null) {
//
//                if (listOfUnSubscribers.size() > 0) {
//
//                    anUnSubscriber = (UnSubscriber) listOfUnSubscribers.get(0);
//
//                } else {
//                    logger.debug(">>>  records list returned is size 0");
//                }
//            } else {
//                logger.debug(">>> records list is NULL");
//            }
//        }
//        return anUnSubscriber;
//    }
//
//    /*public ArrayList<DataBaseObject> getRecordsInRange(LocalDateTime startDate, LocalDateTime endDate, String datePropertyName, Class tableClass){
//	
//     //LocalDate dateTimeNow = DateUtilities.convertToLocalDate(DateUtilities.convertToLocalDate());
//     System.out.println("startDate: " + startDate);
//     System.out.println("endDate: " + endDate);
//	
//     Session session = getSessionFactory().openSession();
//	
//     //Criteria criteria = session.createCriteria(tableClass)
//     //.add(Restrictions.ge(datePropertyName, startDate))
//     //.add(Restrictions.le(datePropertyName, endDate));
//     //.add(Restrictions.ge(datePropertyName, "2014-01-28T18:31:54.000"))
//     //.add(Restrictions.le(datePropertyName, "2014-02-01T18:31:54.000"));
//	
//     //System.out.println("criteria string: " + criteria.toString());
//	
//	
//     String str = "from KeyWord kw where kw.localDateTimeAdded >= :startdate and kw.localDateTimeAdded <= :enddate";  
//     Query query = session.createQuery(str);  
//     query.setParameter("startdate", startDate);  
//     query.setParameter("enddate",  endDate);  
//	
//     //criteria.add(Restrictions.between("datePropertyName", startDate, endDate));
//	
//     int userAge = session.createCriteria(KeyWord.class)
//     .add(Restrictions.ge("age", a))
//     .add(Restrictions.le("age", b))
//     .setProjection(Property.forName("age"))
//     .uniqueResult();
//	
//     session.beginTransaction();	 
//	 
//	 
//     List list = criteria.list();
//     Iterator itr = list.iterator();
//     while (itr.hasNext()) {
//     KeyWord keyword = (KeyWord) itr.next();
//     System.out.println("UserId: " + keyword.getKeyWordID());
//     System.out.printf("\t");
//     System.out.println("UserName: " + keyword.getNoOfSubscribers());
//     System.out.printf("\t");
//     ....
//
//     }
//	   
//     List<DataBaseObject> listOfObjects = (List<DataBaseObject>)query.list();		
//     System.out.println(">>> number of records retrieved : " + listOfObjects.size());
//     //List<DataBaseObject> listOfObjects = (List<DataBaseObject>)query.list();
//     ArrayList<DataBaseObject> connectionsList = CollectionsConverter.listToArrayList(listOfObjects);
//     session.getTransaction().commit();
//	   
//     session.close();
//
//     logger.debug(">>> size of scheduled sms jobs array retrieved is: " + connectionsList.size());
//     return connectionsList;
//	
//     }*/
//    public ArrayList<JTableOptionCreator> getJTableOptionsFromDB(String queryString, String keyWordValueIfAny) {
//
//        ArrayList<JTableOptionCreator> jTableOptions = null;
//
//        try {
//
//            System.out.println(">>>>>>> ABOUT TO BEGIN TRANSACTION  ........................");
//            getSessionFactory().getCurrentSession().beginTransaction();
//            System.out.println(">>>>>>> TRANSACTION BEGUN  ........................");
//
//            System.out.println(">>>>>>> ABOUT TO CREATE QUERY  ........................");
//
//            Query query = (getSessionFactory().getCurrentSession()).createQuery(queryString);
//            System.out.println(">>>>>>> AFTER CREATING QUERY  ........................");
//
//            if (keyWordValueIfAny != null) {
//                if (!(keyWordValueIfAny.equals(""))) {
//                    query.setParameter("mainkeyword", keyWordValueIfAny);
//                }
//            }
//
//            jTableOptions = new ArrayList<JTableOptionCreator>();
//            JTableOptionCreator jTableOptionCreator = null;
//
//            Iterator iterableObjects = query.list().iterator();
//            System.out.println(">>>>>>> ABOUT TO COMMIT TRANSACTION........................");
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//            System.out.println(">>>>>>> AFTER COMMIT TRANSACTION........................");
//
//            while (iterableObjects.hasNext()) {
//
//                Object[] row = (Object[]) iterableObjects.next();
//                int objectID = (Integer) row[0];
//                String displayText = (String) row[1];
//
//                //jTableOptionCreator = new JTableOptionCreator(displayText, String.valueOf(objectID));
//                jTableOptionCreator = new JTableOptionCreator(displayText, displayText);
//
//                jTableOptions.add(jTableOptionCreator);
//            }
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);// or display error message
//        }
//
//        return jTableOptions;
//
//        /*Iterator results = sess.createQuery(
//         "select cat.color, min(cat.birthdate), recordsDeleted(cat) from Cat cat " +
//         "group by cat.color")
//         .list()
//         .iterator();
//
//         while ( iterableObjects.hasNext() ) {
//         Object[] row = (Object[]) results.next();
//         Color type = (Color) row[0];
//         Date oldest = (Date) row[1];
//         Integer recordsDeleted = (Integer) row[2];
//         .....
//         }*/
//    }
//
//    public ArrayList<DataBaseObject> getClientNamesFromDB(String queryString) {
//
//        ArrayList<DataBaseObject> clientNamesList = null;
//
//        try {
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
//
//            List<DataBaseObject> listOfObjects = (List<DataBaseObject>) query.list();
//            clientNamesList = CollectionsConverter.listToArrayList(listOfObjects);
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);// or display error message
//        }
//
//        System.out.println(">>> clientnames arraysize = " + clientNamesList.size());
//        return clientNamesList;
//
//    }
//
//    public ArrayList<DataBaseObject> getTodaySMSJobList(String queryString, LocalDate todayDate) {
//
//        //SELECT_SCHEDULED_SMS = "from ScheduledMessage sm where sm.scheduledDate like ?";
//        //select note from Communication n where n.subject=:subject and ((n.sentTo=:sentto and n.creator=:creator) or (n.sentTo:creator and n.creator=:sentto))
//        String temp = todayDate + "%";   //�%�+title+�%�;
//        ArrayList<DataBaseObject> connectionsList = null;
//
//        try {
//
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery("from ScheduledMessage sm where str(sm.scheduledDate) like ? and (sm.status=?)");
//            //Query query = session.createQuery("from ScheduledMessage sm where str(sm.scheduledDate) like ?");	
//
//            query.setString(0, temp);
//            query.setString(1, "pending");
//
//            logger.debug(">>>>    query created is: " + query.toString());
//
//            //billNumber is a long
//            //select b.billNumber from Bill b where str(b.billNumber) like :billNumber").setString("billNumber,your_parameter).list();
//            // Criterion c1 = Restrictions.like("stateName", "Virg", MatchMode.END);
//            //Criterion c1 = Restrictions.like("stateName", "Virg%");		 
//            // Criteria criteria = session.createCriteria(ScheduledMessage.class).add(Restrictions.like("scheduledDate", "street 1%", MatchMode.START));
//            //Criteria criteria = session.createCriteria(ScheduledMessage.class).add(Restrictions.like("scheduledDate", todayDate, MatchMode.START));	   
//            // List<DataBaseObject> listOfObjects = (List<DataBaseObject>)criteria.list();		   
//            List<DataBaseObject> listOfObjects = (List<DataBaseObject>) query.list();
//            connectionsList = CollectionsConverter.listToArrayList(listOfObjects);
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//            logger.debug(">>> size of scheduled sms jobs array retrieved is: " + connectionsList.size());
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);// or display error message
//        }
//        return connectionsList;
//    }
//
//    public int selectAndInsert(String queryString) {
//
//        int rowsAffected = 0;
//
//        try {
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
//            rowsAffected = query.executeUpdate();
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);// or display error message
//        }
//
//        return rowsAffected;
//    }
//
//    /*public ArrayList<DataBaseObject> readFromDB(String queryString) {
//		
//     ArrayList<DataBaseObject> connectionsList = null;
//		
//     try{
//     getSessionFactory().getCurrentSession().beginTransaction();
//			
//     Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
//     connectionsList = CollectionsConverter.listToArrayList((List<DataBaseObject>)query.list());
//     getSessionFactory().getCurrentSession().getTransaction().commit();
//     }
//     catch (RuntimeException e) {
//     getSessionFactory().getCurrentSession().getTransaction().rollback();
//     logger.error(">>> errror : " + e);// or display error message
//     }
//     System.out.println("db objects retrieved! ");
//
//     return connectionsList;
//     }*/
//    public ArrayList<DataBaseObject> readFromDB(String queryString, int jtStartIndex, int jtPageSize, String orderBy) {
//
//        String pagedQueryString = queryString + " ORDER BY " + orderBy;
//        System.out.println("query : " + pagedQueryString);
//        //SELECT * FROM Students ORDER BY Name ASC LIMIT 20,10
//
//        ArrayList<DataBaseObject> connectionsList = null;
//
//        try {
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            Query query = getSessionFactory().getCurrentSession().createQuery(pagedQueryString);
//            query.setFirstResult(jtStartIndex);
//            query.setMaxResults(jtPageSize);
//
//            connectionsList = CollectionsConverter.listToArrayList((List<DataBaseObject>) query.list());
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);// or display error message
//        }
//        System.out.println("db objects retrieved! ");
//
//        return connectionsList;
//    }
//
//    /* Method to CREATE an record in the database */
//    /* Method to DELETE a record from the database */
//    public boolean deleteObjectFromDB(int dbObjectID, String tablename) {
//
//        boolean deleted = false;
//
//        try {
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            if (tablename.equalsIgnoreCase(Subscriber.getTableName())) {
//
//                Subscriber aSubscriber = (Subscriber) getSessionFactory().getCurrentSession().get(Subscriber.class, new Integer(dbObjectID));
//                getSessionFactory().getCurrentSession().delete(aSubscriber);
//
//                deleted = true;
//
//            } else if (tablename.equalsIgnoreCase(Content.getTableName())) {
//
//                Content aContent = (Content) getSessionFactory().getCurrentSession().get(Content.class, new Integer(dbObjectID));
//                getSessionFactory().getCurrentSession().delete(aContent);
//
//                deleted = true;
//
//            } else if (tablename.equalsIgnoreCase(KeyWord.getTableName())) {
//
//                KeyWord aKeyword = (KeyWord) getSessionFactory().getCurrentSession().get(KeyWord.class, new Integer(dbObjectID));
//                getSessionFactory().getCurrentSession().delete(aKeyword);
//
//                deleted = true;
//
//            } else if (tablename.equalsIgnoreCase(Client.getTableName())) {
//
//                Client aClient = (Client) getSessionFactory().getCurrentSession().get(Client.class, new Integer(dbObjectID));
//                getSessionFactory().getCurrentSession().delete(aClient);
//
//                deleted = true;
//            }
//
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//            System.out.println(">>> object deleted with ID: " + dbObjectID);
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror deleting : " + e);// or display error message
//        } catch (Exception e) {
//            logger.error(">> exception : " + e);
//        }
//
//        return deleted;
//    }
//
//    //need get the object to update first before
//    public DataBaseObject getDBObjectToUpdate(int dbObjectID, String tablename) {
//
//        DataBaseObject dbObject = null;
//        System.out.println(">>>>   tablename : " + tablename);
//
//        if (tablename.equalsIgnoreCase(Subscriber.getTableName())) {
//
//            System.out.println(">>>>   tablename subscriber : " + tablename);
//            getSessionFactory().getCurrentSession().beginTransaction();
//            dbObject = (Subscriber) getSessionFactory().getCurrentSession().get(Subscriber.class, new Integer(dbObjectID));
//
//        } else if (tablename.equalsIgnoreCase(ScheduledMessage.getTableName())) {
//
//            System.out.println(">>>>   tablename scheduledmsg: " + tablename);
//            getSessionFactory().getCurrentSession().beginTransaction();
//            dbObject = (ScheduledMessage) getSessionFactory().getCurrentSession().get(ScheduledMessage.class, new Integer(dbObjectID));
//
//        }
//
//        /*tx = session.beginTransaction();
//         Employee employee = (Employee)session.get(Employee.class, EmployeeID); 
//         employee.setSalary( salary );
//         session.update(employee); 
//         tx.commit();*/
//        nowUpdateObjectInDB(dbObject, tablename, getSessionFactory().getCurrentSession());
//        return dbObject;
//    }
//
//    //after getting the object to update, edit it and then update it calling this method
//    public void nowUpdateObjectInDB(DataBaseObject dbObject, String tablename, Session session) {
//
//        try {
//
//            if (tablename.equalsIgnoreCase(Subscriber.getTableName())) {
//                Subscriber aSubscriber = (Subscriber) dbObject;
//                session.update(aSubscriber);
//            } else if (tablename.equalsIgnoreCase(ScheduledMessage.getTableName())) {
//                ScheduledMessage aScheduledMessage = (ScheduledMessage) dbObject;
//                session.update(aScheduledMessage);
//            }
//            session.getTransaction().commit();
//
//        } catch (RuntimeException e) {
//            session.getTransaction().rollback();
//            logger.error(">>> errror : " + e);// or display error message
//        }
//    }
//
//    public void updateAnObject(DataBaseObject dbObject, String tablename) {
//
//        try {
//            getSessionFactory().getCurrentSession().beginTransaction();
//
//            if (tablename.equalsIgnoreCase(Subscriber.getTableName())) {
//
//                Subscriber aSubscriber = (Subscriber) dbObject;
//                getSessionFactory().getCurrentSession().update(aSubscriber);
//                System.out.println("object updated successfuly!! ");
//
//            } else if (tablename.equalsIgnoreCase(ScheduledMessage.getTableName())) {
//
//                ScheduledMessage aScheduledMessage = (ScheduledMessage) dbObject;
//                getSessionFactory().getCurrentSession().update(aScheduledMessage);
//            }
//            getSessionFactory().getCurrentSession().getTransaction().commit();
//
//        } catch (RuntimeException e) {
//            getSessionFactory().getCurrentSession().getTransaction().rollback();
//            logger.error(">>> errror : " + e);// or display error message
//        }
//    }
    /*public void DeleteUser(int id) {
     try {
	 
     System.out.println(id);
     Session s = getSession();
     Transaction tx = s.beginTransaction();
     Student u = (Student) s.load(Student.class, new Long(id));// difference between get() and load()
     s.delete(u);
     tx.commit();
     System.out.println("\n\n Record Deleted \n");
     } catch (HibernateException e) {
     System.out.println(e.getMessage());
     }
     }*/

    /*public Integer persistObjectToDB(DataBaseObject dbObject) {
     int dbObjectID;
     Transaction transaction = null;
     Session tempSession = getSessionFactory().openSession();
     transaction = tempSession.beginTransaction();
     dbObjectID= (Integer) tempSession.save(dbObject);
     tempSession.getTransaction().commit();
     tempSession.close();
     //getSessionFactory().close();

     return dbObjectID;

     }*/
    /* Method to UPDATE salary for an employee 
     public void updateEmployee(Integer EmployeeID, int salary ){
     Session session = factory.openSession();
     Transaction tx = null;
     try{
     tx = session.beginTransaction();
     Employee employee = 
     (Employee)session.get(Employee.class, EmployeeID); 
     employee.setSalary( salary );
     session.update(employee); 
     tx.commit();
     }catch (HibernateException e) {
     if (tx!=null) tx.rollback();
     e.printStackTrace(); 
     }finally {
     session.close(); 
     }
     }*/
}
