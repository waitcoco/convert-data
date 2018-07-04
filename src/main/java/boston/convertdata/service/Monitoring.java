package boston.convertdata.service;

import boston.convertdata.controller.ConvertController;
import boston.convertdata.model.structured.Video;
import boston.convertdata.model.task.AvailableTaskResponse;
import boston.convertdata.repository.HdfsRepository;
import boston.convertdata.repository.TaskRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class Monitoring {

    private AvailableTaskResponse currentAvailableTaskResponse = null;
    private final int heartbeatSleepMillis = 10000;
    private final int downloadJsonSleepMillis = 10000;

    private final TaskRepository taskRepository;
    private final HdfsRepository hdfsRepository;
    private final ConvertController convertController;

    @Autowired
    public Monitoring(TaskRepository taskRepository, HdfsRepository hdfsRepository, ConvertController convertController) {
        this.taskRepository = taskRepository;
        this.hdfsRepository = hdfsRepository;
        this.convertController = convertController;
    }

    public void start() {
        new Thread(this::heartbeatUpdating).start();
        new Thread(this::getStructuredVideoInfo).start();
    }

    // 获取任务
    private void getStructuredVideoInfo() {
        while (true) {
            try {
                currentAvailableTaskResponse = taskRepository.findAvailableTask();
                if (currentAvailableTaskResponse.isFound()) {
                    // 获取resultPath (json文件在HDFS里的路径), 并下载
                    Video video = hdfsRepository.convertHdfsFile2Video(currentAvailableTaskResponse.getVideo().getResultPath());
                    log.info("get video info of id: " + video.getVideoId());
                    // 获取到的结构化结果进行封装并上传es
                    convertController.convert(video);
                    // complete task, 将任务status改为2
                    taskRepository.completeTask(currentAvailableTaskResponse.getTask().getId(), currentAvailableTaskResponse.getTask().getExecutorId());
                    currentAvailableTaskResponse.setFound(false);
                } else {
                    log.info("no task found");
                }
            } catch (Exception e) {
                currentAvailableTaskResponse = null;
                log.error("failed to get structured video information", e);
            }

            try {
                Thread.sleep(downloadJsonSleepMillis);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    // 更新心跳
    private void heartbeatUpdating() {
        while (true) {
            try {
                if (currentAvailableTaskResponse != null && currentAvailableTaskResponse.isFound()) {
                    taskRepository.updateHeartbeat(currentAvailableTaskResponse.getTask().getId(), currentAvailableTaskResponse.getTask().getExecutorId());
                    log.info("update heartbeat succeeded");
                } else {
                    log.info("didn't update heartbeat: no task for now");
                }
            } catch (Exception e) {
                log.error("failed to update heartbeat", e);
            }

            try {
                Thread.sleep(heartbeatSleepMillis);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

}
