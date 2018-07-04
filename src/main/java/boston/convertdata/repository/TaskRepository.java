package boston.convertdata.repository;

import boston.convertdata.model.task.AvailableTaskResponse;
import boston.convertdata.utils.OkHttpUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.hadoop.conf.Configured;
import org.apache.http.client.utils.URIBuilder;

import java.util.UUID;

@Log4j2
public class TaskRepository extends Configured {

    static private final String uploadTypeCode = "2";
    private final String taskBaseUrl;

    public TaskRepository(String taskBaseUrl) {
        this.taskBaseUrl = taskBaseUrl;
    }

    /**
     * 获取type为2的task, 即压缩等待上传es的数据
     *
     * @return AvailableResponse
     */
    public AvailableTaskResponse findAvailableTask() {
        String executorId = UUID.randomUUID().toString();
        log.info(String.format("try to get task with executorId=%s", executorId));
        return OkHttpUtil.getResponseBodyStringWithPost(
                new URIBuilder().setPath(taskBaseUrl + "/api/task/executeAvailableTask").setParameter("executorId", executorId).setParameter("taskType", uploadTypeCode),
                null,
                AvailableTaskResponse.class);
    }

    /**
     * 更新心跳
     *
     * @param taskId     任务id
     * @param executorId 执行人id
     */
    public void updateHeartbeat(String taskId, String executorId) {
        log.info(String.format("try to update heart beat: taskId=%s, executorId=%s", taskId, executorId));
        OkHttpUtil.getResponseBodyStringWithPost(
                new URIBuilder().setPath(taskBaseUrl + "/api/task/updateHeartbeat").setParameter("executorId", executorId).setParameter("taskId", taskId),
                null,
                Object.class);
    }


    public void completeTask (String taskId, String executorId) {
        log.info(String.format("try to complete task: taskId=%s, executorId=%s", taskId, executorId));
        OkHttpUtil.getResponseBodyStringWithPost(
                new URIBuilder().setPath(taskBaseUrl + "/api/task/completeTask").setParameter("executorId", executorId).setParameter("taskId", taskId),
                null,
                Object.class);
    }


}
