package com.heima.admin.dao;

import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author: HuangMuChen
 * @date: 2021/2/9 10:45
 * @version: V1.0
 * @Description: 通用mappper持久层接口：如果在mycat分库分表的情况下，可以提供多个方法支持不同分片算法的数据CRUD，这里演示较为常用的查询，既非复合分片的CRUD实现
 */
public interface CommonDao {

    /**
     * 分页查询列表
     *
     * @param tableName
     * @param start
     * @param size
     * @return
     */
    @Select("select * from ${tableName} limit #{start},#{size}")
    @ResultType(HashMap.class)
    List<HashMap> list(@Param("tableName") String tableName, @Param("start") int start, @Param("size") int size);

    /**
     * 查询总条数
     *
     * @param tableName
     * @return
     */
    @Select("select count(*) from ${tableName} ")
    @ResultType(Integer.class)
    int listCount(@Param("tableName") String tableName);

    /**
     * 按条件分页查询列表
     *
     * @param tableName
     * @param where
     * @param start
     * @param size
     * @return
     */
    @Select("select * from ${tableName} where 1=1 ${where} limit #{start},#{size}")
    @ResultType(HashMap.class)
    List<HashMap> listForWhere(@Param("tableName") String tableName, @Param("where") String where, @Param("start") int start, @Param("size") int size);

    /**
     * 按条件查询总条数
     *
     * @param tableName
     * @param where
     * @return
     */
    @Select("select count(*) from ${tableName} where 1=1 ${where}")
    @ResultType(Integer.class)
    int listCountForWhere(@Param("tableName") String tableName, @Param("where") String where);

    /**
     * 更新数据
     *
     * @param tableName
     * @param where
     * @param sets
     * @return
     */
    @Update("update ${tableName} set ${sets} where 1=1 ${where}")
    @ResultType(Integer.class)
    int update(@Param("tableName") String tableName, @Param("where") String where, @Param("sets") String sets);

    /**
     * 插入数据
     *
     * @param tableName
     * @param fileds
     * @param values
     * @return
     */
    @Insert("insert into ${tableName} (${fileds}) values (${values})")
    @ResultType(Integer.class)
    int insert(@Param("tableName") String tableName, @Param("fileds") String fileds, @Param("values") String values);

    /**
     * 删除数据
     *
     * @param tableName
     * @param where
     * @return
     */
    @Delete("delete from ${tableName} where 1=1 ${where} limit 1")
    @ResultType(Integer.class)
    int delete(@Param("tableName") String tableName, @Param("where") String where);
}
