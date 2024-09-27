package daoPackage;

import voPackage.Id;
import voPackage.VOGenerator;

import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class DAOGenerator {

    protected Connection connection;

    public DAOGenerator(Connection connection) {
        this.connection = connection;
    }

    private String getTableName(VOGenerator voGenerator){
        return voGenerator.getClass().getSimpleName().substring(2);
    }

    private Map<String,Object> getAllFields(VOGenerator voGenerator){

        Map<String,Object> allFileds = new LinkedHashMap<>();

        try {
            String voName = "VO" + getTableName(voGenerator);

            if(voName.equals(voGenerator.getClass().getSimpleName())){
                Field[] fields = voGenerator.getClass().getDeclaredFields();

                for(Field field : fields){
                    String fieldName = field.getName().toUpperCase();
                    field.setAccessible(true);
                    Object fieldValue = field.get(voGenerator);

                    allFileds.put(fieldName,fieldValue);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return allFileds;
    }

    private String getInsertStatement(VOGenerator voGenerator){

        Map<String,Object> mappaVO = getAllFields(voGenerator);

        StringBuilder builder = new StringBuilder();

        builder.append("INSERT INTO ").append(getTableName(voGenerator)).append(" (");

        Iterator<Map.Entry<String, Object>> iterator = mappaVO.entrySet().iterator();
        List<Map.Entry<String, Object>> list = new ArrayList<>();

        while (iterator.hasNext()){
            list.add(iterator.next());
        }

        for(int i = 0; i < list.size(); i++){
            builder.append(list.get(i).getKey());
            if(i < list.size() - 1){
                builder.append(",");
            }
        }

        builder.append(") ")
                .append("VALUES (");

        for(int i = 0; i < list.size(); i++){
            Object value = list.get(i).getValue();

            if(value instanceof String){
                builder.append("'").append(value).append("'");

            }else if(value == null){
                builder.append("NULL");
            }else {
                builder.append(value);
            }

            if(i < list.size() - 1){
                builder.append(",");
            }
        }
        builder.append(")");

        return builder.toString();
    }

    private Map<String,Object> getPKFields(VOGenerator voGenerator){

        Map<String,Object> pkFields = new LinkedHashMap<>();

        String voName = "VO" + getTableName(voGenerator);

        try {
            if (voName.equals(voGenerator.getClass().getSimpleName())) {
                Field[] fields = voGenerator.getClass().getDeclaredFields();

                for (Field field : fields) {
                    if (field.isAnnotationPresent(Id.class)) {
                        String nameOfPk = field.getName();
                        field.setAccessible(true);
                        Object fieldValue = field.get(voGenerator);

                        pkFields.put(nameOfPk,fieldValue);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return pkFields;
    }

    private String getReadByPrimaryKeyStatement(VOGenerator voGenerator){

        Map<String,Object> pkFields = getPKFields(voGenerator);

        StringBuilder builder = new StringBuilder();

        builder.append("SELECT * FROM ")
                .append(getTableName(voGenerator))
                .append(" WHERE ");

        Iterator<Map.Entry<String, Object>> iterator = pkFields.entrySet().iterator();
        List<Map.Entry<String, Object>> list = new ArrayList<>();

        while (iterator.hasNext()){
            list.add(iterator.next());
        }

        for(int i = 0; i < list.size(); i++){
            String fieldName = list.get(i).getKey();
            builder.append(fieldName).append(" = ");

            Object fieldValue = list.get(i).getValue();

            if(fieldValue instanceof String){
                builder.append("'").append(fieldValue).append("'");

            }else {
                builder.append(fieldValue);
            }

            if(i < list.size() - 1){
                builder.append(" AND ");
            }
        }

        return builder.toString();
    }

    private String getDeleteStatement(VOGenerator voGenerator){

        Map<String,Object> pkFields = getPKFields(voGenerator);

        StringBuilder builder = new StringBuilder();

        builder.append("DELETE FROM ")
                .append(getTableName(voGenerator))
                .append(" WHERE ");

        Iterator<Map.Entry<String, Object>> iterator = pkFields.entrySet().iterator();
        List<Map.Entry<String, Object>> list = new ArrayList<>();

        while (iterator.hasNext()){
            list.add(iterator.next());
        }

        for(int i = 0; i < list.size(); i++){
            String fieldName = list.get(i).getKey();
            builder.append(fieldName).append(" = ");

            Object fieldValue = list.get(i).getValue();

            if(fieldValue instanceof String){
                builder.append("'").append(fieldValue).append("'");

            }else {
                builder.append(fieldValue);
            }

            if(i < list.size() - 1){
                builder.append(" AND ");
            }
        }

        return builder.toString();
    }

    private String getUpdateStatement(VOGenerator voGenerator){

        Map<String,Object> allFields = getAllFields(voGenerator);
        Iterator<Map.Entry<String,Object>> iteratorAllFields = allFields.entrySet().iterator();
        List<Map.Entry<String,Object>> listAllFields = new ArrayList<>();

        Map<String,Object> pkFields = getPKFields(voGenerator);
        Iterator<Map.Entry<String,Object>> iteratorPkFileds = pkFields.entrySet().iterator();
        List<Map.Entry<String,Object>> listPkFields = new ArrayList<>();

        StringBuilder builder = new StringBuilder();

        builder.append("UPDATE ")
                .append(getTableName(voGenerator)).
                append(" SET ");

        while (iteratorAllFields.hasNext()){
            listAllFields.add(iteratorAllFields.next());
        }

        while (iteratorPkFileds.hasNext()){
            listPkFields.add(iteratorPkFileds.next());
        }

        StringBuilder whereCondition = new StringBuilder();
        whereCondition.append(" WHERE ");

        for(int i = 0; i < listAllFields.size(); i++){
            String fieldAllName = listAllFields.get(i).getKey();
            Object fieldAllValue = listAllFields.get(i).getValue();

            builder.append(fieldAllName)
                    .append(" = ");

            if(fieldAllValue instanceof String){
                builder.append("'").append(fieldAllValue).append("'");

            }else {
                builder.append(fieldAllValue);
            }

            if(i < listAllFields.size() - 1){
                builder.append(",");
            }
        }

        for(int k = 0; k < listPkFields.size(); k++){
            String fieldPkName = listPkFields.get(k).getKey();
            Object fieldPkVaue = listPkFields.get(k).getValue();

            whereCondition.append(fieldPkName)
                    .append(" = ");

            if(fieldPkVaue instanceof String){
                whereCondition.append("'")
                        .append(fieldPkVaue)
                        .append("'");

            }else {
                whereCondition.append(fieldPkVaue);

            }
            if(k < listPkFields.size() - 1){
                whereCondition.append(" AND ");
            }
        }

        builder.append(whereCondition.toString());

        return builder.toString();
    }

    private List<String> getALlFieldName(VOGenerator voGenerator){

        List<String> getALlFieldName = new ArrayList<>();

        Field[] fields = voGenerator.getClass().getDeclaredFields();

        for(Field field : fields){
            getALlFieldName.add(field.getName());
        }

        return getALlFieldName;
    }

    public <T extends VOGenerator> T readByPrimaryKey(T voGenerator) {

        String sql = getReadByPrimaryKeyStatement(voGenerator);
        Field[] fields = voGenerator.getClass().getDeclaredFields();

        try{
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(sql);

            if(rs != null && rs.next()){
                for (Field field : fields){
                    field.setAccessible(true);
                    field.set(voGenerator,rs.getObject(field.getName()));
                }

                connection.commit();
                rs.close();
                st.close();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return voGenerator;
    }

    public int update(VOGenerator voGenerator) {

        String sql = getUpdateStatement(voGenerator);

        int rs = 0;

        try{
            Statement st = connection.createStatement();

            rs = st.executeUpdate(sql);

            connection.commit();
            st.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        return rs;
    }

    public int delete(VOGenerator voGenerator) {

        String sql = getDeleteStatement(voGenerator);
        int rs = 0;

        try{
            Statement st = connection.createStatement();

            rs = st.executeUpdate(sql);

            connection.commit();
            st.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        return rs;
    }

    public int create(VOGenerator voGenerator){

        String sql = getInsertStatement(voGenerator);
        int rs = 0;

        try{
            Statement st = connection.createStatement();

            rs = st.executeUpdate(sql);

            connection.commit();

            st.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        return rs;
    }

}