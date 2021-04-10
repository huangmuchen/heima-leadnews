package com.heima.admin.service.impl;

import com.heima.admin.dao.CommonDao;
import com.heima.admin.filter.BaseCommonFilter;
import com.heima.admin.service.ICommonService;
import com.heima.common.constans.AdminConstans;
import com.heima.model.admin.dtos.CommonDto;
import com.heima.model.admin.dtos.CommonWhereDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.threadlocal.AdminThreadLocalUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: HuangMuChen
 * @date: 2021/2/9 10:48
 * @version: V1.0
 * @Description: 通用业务层实现类
 */
@Service
public class CommonServiceImpl implements ICommonService {
    @Autowired
    private CommonDao commonDao;
    @Autowired
    ApplicationContext context;

    /**
     * 加载通用的数据列表：无条件查询，无条件统计，有条件的查询，有条件的统计
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult list(CommonDto dto) {
        // 参数校验
        if (dto == null || dto.getName() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 分页参数校验
        dto.checkParam();
        // 校验查询权限
        if (!dto.getName().isList()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        // 获取表名
        String tableName = dto.getName().name().toLowerCase();
        // 获取查询条件
        String where = getWhere(dto);
        // 获取分页查询起始位置
        int start = (dto.getPage() - 1) * dto.getSize();
        // 初始记录行的偏移量是0(而不是1)
        if (start < 0) {
            start = 0;
        }
        // 查询结果集
        List<?> list;
        // 查询总记录数
        int total;
        // 判断是条件查询 or 非条件查询
        if (StringUtils.isEmpty(where)) {
            // 非条件查询
            list = this.commonDao.list(tableName, start, dto.getSize());
            total = this.commonDao.listCount(tableName);
        } else {
            // 条件查询
            list = this.commonDao.listForWhere(tableName, where, start, dto.getSize());
            total = this.commonDao.listCountForWhere(tableName, where);
        }
        // 新建一个Map
        Map<String, Object> map = new HashMap<>();
        // 添加数据
        map.put("list", list);
        map.put("total", total);
        //后处理的bean
        doFilter(dto, AdminConstans.FILTER_TYPE_LIST);
        // 返回查询结果
        return ResponseResult.okResult(map);
    }

    /**
     * 新增/修改通用的数据列表：通过dto中的model来判断，选择使用新增或修改
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult update(CommonDto dto) {
        // 参数校验
        if (dto == null || dto.getName() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 表名
        String tableName = dto.getName().name();
        // 更新条件
        String where = getWhere(dto);
        // 根据操作模式执行相应操作
        if (AdminConstans.FILTER_TYPE_ADD.equals(dto.getModel())) {
            // 新增，条件必须为空
            if (StringUtils.isNotEmpty(where)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "新增数据不能设置条件");
            } else {
                return addData(dto, tableName);
            }
        } else {
            // 更新，必须有条件
            if (StringUtils.isEmpty(where)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "修改条件不能为空");
            } else {
                return updateData(dto, tableName, where);
            }
        }
    }

    /**
     * 删除通用的数据列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult delete(CommonDto dto) {
        // 参数校验
        if (dto == null || dto.getName() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 校验删除权限
        if (!dto.getName().isDelete()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        // 获取表名
        String tableName = dto.getName().name().toLowerCase();
        // 获取查询条件
        String where = getWhere(dto);
        // 删除必须有条件
        if (StringUtils.isEmpty(where)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "删除条件不合法");
        }
        // 调用dao进行删除
        int rows = this.commonDao.delete(tableName, where);
        // 如果删除成功，则进行后处理操作
        if (rows > 0) {
            doFilter(dto, AdminConstans.FILTER_TYPE_DELETE);
        }
        // 返回删除结果
        return ResponseResult.okResult(rows);
    }

    /**
     * 拼接查询条件：where name = "zhangsan"
     *
     * @param dto
     * @return
     */
    private String getWhere(CommonDto dto) {
        StringBuffer sb = new StringBuffer();
        // 取出条件集合
        List<CommonWhereDto> where = dto.getWhere();
        // 判断
        if (!CollectionUtils.isEmpty(where)) {
            // 遍历集合
            where.forEach(w -> {
                // 参数校验
                if (StringUtils.isNotEmpty(w.getFiled()) && StringUtils.isNotEmpty(w.getValue()) && !w.getFiled().equalsIgnoreCase(w.getValue())) {
                    // 取出字段
                    String tempF = parseValue(w.getFiled());
                    // 取出字段值
                    String tempV = parseValue(w.getValue());
                    // \d*表示0个或多个0到9的数字
                    if (!tempF.matches("\\d*") && !tempF.equalsIgnoreCase(tempV)) {
                        // 精确匹配
                        if (AdminConstans.WHERE_TYPE_EQUALS.equals(w.getType())) {
                            sb.append(" and ").append(tempF).append("='").append(tempV).append("'");
                        }
                        // 模糊匹配
                        if (AdminConstans.WHERE_TYPE_LIKE.equals(w.getType())) {
                            sb.append(" and ").append(tempF).append("like '%").append(tempV).append("%'");
                        }
                        // 区间匹配
                        if (AdminConstans.WHERE_TYPE_BETWEEN.equals(w.getType())) {
                            String[] temp = tempV.split(",");
                            sb.append(" and ").append(tempF).append("between").append(temp[0]).append(" and ").append(temp[1]);
                        }
                    }
                }
            });
        }
        // 返回拼接结果
        return sb.toString();
    }

    /**
     * 拼接更新语句：name="zhangsan",age=18
     *
     * @param dto
     * @return
     */
    private String getSets(CommonDto dto) {
        StringBuffer sb = new StringBuffer();
        // 取出sets集合
        List<CommonWhereDto> sets = dto.getSets();
        // 原子计数器
        AtomicInteger count = new AtomicInteger();
        // 非空判断
        if (!CollectionUtils.isEmpty(sets)) {
            // 遍历集合,此处也可以使用sets.stream().forEach(s->{})遍历集合，因为更新sql中无所谓顺序
            sets.forEach(s -> {
                if (StringUtils.isEmpty(s.getValue())) {
                    // 自增 ++
                    count.incrementAndGet();
                } else {
                    // 取出字段
                    String tempF = parseValue(s.getFiled());
                    // 取出字段值
                    String tempV = parseValue(s.getValue());
                    // \d*表示0个或多个0到9的数字
                    if (!tempF.matches("\\d*") && !tempF.equalsIgnoreCase(tempV)) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(tempF).append("='").append(tempV).append("'");
                    }
                }
            });
        }
        // 判断是否有字段值为空的
        if (count.get() > 0) {
            return null;
        }
        return sb.toString();
    }

    /**
     * 拼接插入语句：insert into ${tableName} (${fileds}) values (${values})
     *
     * @param dto
     * @return
     */
    private String[] getInsertSql(CommonDto dto) {
        StringBuffer fileds = new StringBuffer();
        StringBuffer values = new StringBuffer();
        // 取出sets集合
        List<CommonWhereDto> sets = dto.getSets();
        // 原子计数器
        AtomicInteger count = new AtomicInteger();
        if (!CollectionUtils.isEmpty(sets)) {
            sets.forEach(s -> {
                if (StringUtils.isEmpty(s.getValue())) {
                    // 自增 ++
                    count.incrementAndGet();
                } else {
                    // 取出字段
                    String tempF = parseValue(s.getFiled());
                    // 取出字段值
                    String tempV = parseValue(s.getValue());
                    // \d*表示0个或多个0到9的数字
                    if (!tempF.matches("\\d*") && !tempF.equalsIgnoreCase(tempV)) {
                        if (fileds.length() > 0) {
                            fileds.append(",");
                            values.append(",");
                        }
                        fileds.append(tempF);
                        values.append("'").append(tempV).append("'");
                    }
                }
            });
        }
        // 判断是否有字段值为空的
        if (count.get() > 0) {
            return null;
        }
        // 返回拼接结果
        return new String[]{fileds.toString(), values.toString()};
    }

    /**
     * 为了防止sql注入，替换一些敏感的符号
     *
     * @param filed
     * @return
     */
    private String parseValue(String filed) {
        if (StringUtils.isNotEmpty(filed)) {
            return filed.replaceAll(".*([';#%]+|(--)+).*", "");
        }
        return filed;
    }

    /**
     * 根据名称获取后置处理器实现类
     *
     * @param dto
     * @return
     */
    private BaseCommonFilter findFilter(CommonDto dto) {
        // 获取bean的名称[表名的大写，但是可以作为bean的名称，比如@bean（"AP_ARTICLE"）]
        String name = dto.getName().name();
        // 判断
        if (context.containsBean(name)) {
            // getBean(String name,Class type)这种获取bean的方式比较适合当类型不唯一时，再通过id或者name来获取bean
            return context.getBean(name, BaseCommonFilter.class);
        }
        return null;
    }

    /**
     * 后置处理方法
     *
     * @param dto
     * @param name 查询(list)、更新(update)、删除(delete)、新增(add)
     */
    private void doFilter(CommonDto dto, String name) {
        // 获取后置处理器
        BaseCommonFilter filter = findFilter(dto);
        // 判断
        if (filter != null) {
            // 获取用户信息
            AdUser user = AdminThreadLocalUtils.getUser();
            // 查询后处理
            if (AdminConstans.FILTER_TYPE_LIST.equals(name)) {
                filter.doListAfter(user, dto);
            }
            // 更新后处理
            if (AdminConstans.FILTER_TYPE_UPDATE.equals(name)) {
                filter.doUpdateAfter(user, dto);
            }
            // 删除后处理
            if (AdminConstans.FILTER_TYPE_DELETE.equals(name)) {
                filter.doDeleteAfter(user, dto);
            }
            // 新增后处理
            if (AdminConstans.FILTER_TYPE_ADD.equals(name)) {
                filter.doInsertAfter(user, dto);
            }
        }
    }

    /**
     * 插入一条数据
     *
     * @param dto
     * @param tableName
     * @return
     */
    private ResponseResult addData(CommonDto dto, String tableName) {
        // 拼接插入语句
        String[] sql = getInsertSql(dto);
        // 校验新增权限
        if (!dto.getName().isAdd()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        // 校验参数
        if (sql == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "传入的参数值不能为空");
        }
        // 调用dao层进行插入
        int rows = this.commonDao.insert(tableName, sql[0], sql[1]);
        // 如果插入成功，调用后处理方法
        if (rows > 0) {
            doFilter(dto, AdminConstans.FILTER_TYPE_ADD);
        }
        // 返回插入结果
        return ResponseResult.okResult(rows);
    }

    /**
     * 更新一条数据
     *
     * @param dto
     * @param tableName
     * @param where
     * @return
     */
    private ResponseResult updateData(CommonDto dto, String tableName, String where) {
        // 拼接更新语句
        String sets = getSets(dto);
        // 校验更新权限
        if (!dto.getName().isUpdate()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        // 校验参数
        if (StringUtils.isEmpty(sets)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "修改的参数值不能为空");
        }
        // 调用dao层进行更新
        int rows = this.commonDao.update(tableName, where, sets);
        // 如果更新成功，调用后处理方法
        if (rows > 0) {
            doFilter(dto, AdminConstans.FILTER_TYPE_UPDATE);
        }
        // 返回更新结果
        return ResponseResult.okResult(rows);
    }
}
