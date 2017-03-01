package com.baifendian.swordfish.dao.mysql.model;

import com.baifendian.swordfish.dao.mysql.enums.FlowStatus;
import com.baifendian.swordfish.dao.mysql.enums.FlowType;

import java.util.Date;
import java.util.List;

/**
 *
 * <p> 查询运行的任务列表情况的查询信息
 *
 * @author : wenting.wang
 * @date : 2016年8月30日
 */
public class MaintainQuery {

    /**	项目 id**/
    private int projectId;

    /**查询的时间范围-起始时间**/
    private Date startTime;

    /**查询的时间范围-截止时间**/
    private Date endTime;

    /**是查询自己的任务还是全部任务**/
    private Boolean isMyself;

    /**用户id**/
    private int userId;

    /**根据任务状态进行查询**/
    private List<FlowStatus> taskStatus;

    /**根据任务类型进行查询**/
    private List<FlowType> flowTypes;

    /**	workflow或者即席查询任务名称**/
    private String name;

    /**起始页**/
    private Integer start;

    /**一页返回的最大长度**/
    private Integer length;

    private Long execId;

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Boolean getMyself() {
        return isMyself;
    }

    public void setMyself(Boolean myself) {
        isMyself = myself;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<FlowStatus> getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(List<FlowStatus> taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getExecId() {
        return execId;
    }

    public void setExecId(Long execId) {
        this.execId = execId;
    }

    public List<FlowType> getFlowTypes() {
        return flowTypes;
    }

    public void setFlowTypes(List<FlowType> flowTypes) {
        this.flowTypes = flowTypes;
    }
}