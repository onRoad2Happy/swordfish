/*
 * Copyright (C) 2017 Baifendian Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baifendian.swordfish.dao.mapper;

import com.baifendian.swordfish.dao.enums.FlowStatus;
import com.baifendian.swordfish.dao.mapper.utils.EnumFieldUtil;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.jdbc.SQL;

public class StreamingResultMapperProvider {

  private static final String TABLE_NAME = "streaming_result";

  public String insert(Map<String, Object> parameter) {
    return new SQL() {
      {
        INSERT_INTO(TABLE_NAME);

        VALUES("`worker`", "#{result.worker}");
        VALUES("`streaming_id`", "#{result.streamingId}");
        VALUES("`parameter`", "#{result.parameter}");
        VALUES("`user_defined_params`", "#{result.userDefinedParams}");
        VALUES("`submit_user`", "#{result.submitUserId}");
        VALUES("`submit_time`", "#{result.submitTime}");
        VALUES("`queue`", "#{result.queue}");
        VALUES("`proxy_user`", "#{result.proxyUser}");
        VALUES("`schedule_time`", "#{result.scheduleTime}");
        VALUES("`start_time`", "#{result.startTime}");
        VALUES("`end_time`", "#{result.endTime}");
        VALUES("`status`", EnumFieldUtil.genFieldStr("result.status", FlowStatus.class));
        VALUES("`app_links`", "#{result.appLinks}");
        VALUES("`job_links`", "#{result.jobLinks}");
        VALUES("`job_id`", "#{result.jobId}");
      }
    }.toString();
  }

  /**
   * 根据执行 id 查询执行结果
   *
   * @param parameter
   * @return
   */
  public String selectById(Map<String, Object> parameter) {
    return constructCommonSimpleSQL().
            WHERE("r.id=#{execId}").toString();
  }

  /**
   * 查询某流任务最新的一条结果记录
   * for example: select * from table where id=(select max(id) from table where field=xxx limit 1);
   *
   * @param parameter
   * @return
   */
  public String findLatestDetailByStreamingId(Map<String, Object> parameter) {
    String subSql = new SQL() {
      {
        SELECT("max(id)");

        FROM(TABLE_NAME);

        WHERE("streaming_id = #{streamingId}");
      }
    }.toString() + " limit 1";

    return constructCommonDetailSQL().
            WHERE("r.id = " + "(" + subSql + ")").
            toString();
  }

  /**
   * 查找没有完成的 job
   *
   * @param parameter
   * @return
   */
  public String findNoFinishedJob(Map<String, Object> parameter) {
    return constructCommonSimpleSQL().
            WHERE("status <= " + FlowStatus.RUNNING.ordinal()).OR().WHERE("status = " + FlowStatus.INACTIVE.ordinal()).toString();
  }

  /**
   * 通过 id 更新
   *
   * @param parameter
   * @return sql 语句
   */
  public String updateResult(Map<String, Object> parameter) {
    return new SQL() {
      {
        UPDATE(TABLE_NAME);

        SET("`worker` = #{result.worker}");
        SET("`parameter` = #{result.parameter}");
        SET("`user_defined_params` = #{result.userDefinedParams}");
        SET("`submit_user` = #{result.submitUserId}");
        SET("`submit_time` = #{result.submitTime}");
        SET("`queue` = #{result.queue}");
        SET("`proxy_user` = #{result.proxyUser}");
        SET("`schedule_time` = #{result.scheduleTime}");
        SET("`start_time` = #{result.startTime}");
        SET("`end_time` = #{result.endTime}");
        SET("`status` = " + EnumFieldUtil.genFieldStr("result.status", FlowStatus.class));
        SET("`app_links` = #{result.appLinks}");
        SET("`job_links` = #{result.jobLinks}");
        SET("`job_id` = #{result.jobId}");

        WHERE("id = #{result.execId}");
      }
    }.toString();
  }

  /**
   * 查询项目的 id
   *
   * @param parameter
   * @return
   */
  public String queryProject(Map<String, Object> parameter) {
    return new SQL() {
      {
        SELECT("p.*");

        FROM("project p");

        JOIN("streaming_job s on p.id = s.project_id");
        JOIN(TABLE_NAME + " as r on s.id = r.streaming_id");

        WHERE("r.id=#{execId}");
      }
    }.toString();
  }

  /**
   * 根据执行 id 查询详细信息
   *
   * @param parameter
   * @return
   */
  public String findDetailByExecId(Map<String, Object> parameter) {
    return constructCommonDetailSQL().
            WHERE("r.id=#{execId}")
            .toString();
  }

  /**
   * 根据项目和名称查询
   *
   * @param parameter
   * @return
   */
  public String findLatestDetailByProjectAndNames(Map<String, Object> parameter) {
    List<String> nameList = (List<String>) parameter.get("nameList");

    String subSql = new SQL() {
      {
        SELECT("max(id)");

        FROM(TABLE_NAME);

        WHERE("p.id=#{projectId}");

        if (CollectionUtils.isNotEmpty(nameList)) {
          String names = "\"" + String.join("\", \"", nameList) + "\"";
          WHERE("s.name in (" + names + ")");
        }

        GROUP_BY("streaming_id");
      }
    }.toString();

    return constructCommonDetailSQL().
            WHERE("r.id in " + "(" + subSql + ")").
            toString();
  }

  /**
   * 根据多个条件一起组合查询
   *
   * @param parameter
   * @return
   */
  public String findByMultiCondition(Map<String, Object> parameter) {
    List<Integer> status = (List<Integer>) parameter.get("status");
    String name = (String) parameter.get("name");

    Date startDate = (Date) parameter.get("startDate");
    Date endDate = (Date) parameter.get("endDate");

    SQL sql = constructCommonDetailSQL().
            WHERE("s.project_id = #{projectId}");

    if (startDate != null) {
      sql = sql.WHERE("schedule_time >= #{startDate}");
    }

    if (endDate != null) {
      sql = sql.WHERE("schedule_time < #{endDate}");
    }

    // 模糊匹配
    if (StringUtils.isNotEmpty(name)) {
      sql = sql.WHERE("s.name like '" + name + "%'");
    }

    if (CollectionUtils.isNotEmpty(status)) {
      sql = sql.WHERE("`status` in ("+StringUtils.join(status,",")+")");
    }

    String subClause = sql.toString();

    String sqlClause = new SQL() {
      {
        SELECT("*");

        FROM("(" + subClause + ") e_f");
      }
    }.toString() + " order by schedule_time DESC limit #{start},#{limit}";

    return sqlClause;
  }

  /**
   * 查询数目
   *
   * @param parameter
   * @return
   */
  public String findCountByMultiCondition(Map<String, Object> parameter) {
    List<Integer> status = (List<Integer>) parameter.get("status");
    String name = (String) parameter.get("name");

    Date startDate = (Date) parameter.get("startDate");
    Date endDate = (Date) parameter.get("endDate");

    return new SQL() {
      {
        SELECT("count(0)");

        FROM(TABLE_NAME + " r");

        JOIN("streaming_job s on r.streaming_id = s.id");

        if (startDate != null) {
          WHERE("schedule_time >= #{startDate}");
        }

        if (endDate != null) {
          WHERE("schedule_time < #{endDate}");
        }

        WHERE("s.project_id=#{projectId}");

        // 模糊匹配
        if (StringUtils.isNotEmpty(name)) {
          WHERE("s.name like '" + name + "%'");
        }

        if (CollectionUtils.isNotEmpty(status)) {
          WHERE("`status` in ("+StringUtils.join(status,",")+")");
        }

      }
    }.toString();
  }

  /**
   * 构造简单的 sql 语句, 主要做了简单的连接
   *
   * @return
   */
  private SQL constructCommonSimpleSQL() {
    return new SQL() {{
      SELECT("submit_user as submit_user_id");
      SELECT("s.owner as owner_id");
      SELECT("s.project_id");
      SELECT("s.name");
      SELECT("s.`desc`");
      SELECT("s.create_time");
      SELECT("s.modify_time");
      SELECT("s.notify_type");
      SELECT("s.notify_mails");
      SELECT("s.type");
      SELECT("p.name as project_name");
      SELECT("r.*");

      FROM(TABLE_NAME + " as r");

      JOIN("streaming_job s on r.streaming_id = s.id");
      JOIN("project p on s.project_id = p.id");
    }};
  }

  /**
   * 构造一个比较通用的 sql, 主要做了详细的关联
   *
   * @return
   */
  private SQL constructCommonDetailSQL() {
    return new SQL() {{
      SELECT("r.submit_user as submit_user_id");
      SELECT("s.owner as owner_id");
      SELECT("s.project_id");
      SELECT("s.name");
      SELECT("s.`desc`");
      SELECT("s.create_time");
      SELECT("s.modify_time");
      SELECT("s.type");
      SELECT("s.notify_type");
      SELECT("s.notify_mails");
      SELECT("u2.name as submit_user_name");
      SELECT("u1.name as owner_name");
      SELECT("p.name as project_name");
      SELECT("r.*");

      FROM(TABLE_NAME + " as r");

      JOIN("streaming_job s on r.streaming_id = s.id");
      JOIN("project p on s.project_id = p.id");
      JOIN("user u1 on s.owner = u1.id");
      JOIN("user u2 on r.submit_user = u2.id");
    }};
  }
}
