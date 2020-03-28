package io.jenkins.plugins.tools;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.request.OapiRobotSendRequest.Btns;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import io.jenkins.plugins.DingTalkRobotConfig;
import io.jenkins.plugins.model.BuildJobModel;
import io.jenkins.plugins.model.RobotConfigModel;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * 消息发送器
 *
 * @author liuwei
 * @date 2019/12/26 10:07
 */
public class DingTalkSender {

  /**
   * 消息类型
   */
  public static final String MSG_TYPE = "actionCard";

  private RobotConfigModel robotConfigModel;

  /**
   * 在标题里包含关键字验证
   */
  private String title;

  public DingTalkSender(DingTalkRobotConfig robot) {
    this.robotConfigModel = RobotConfigModel.of(robot);
    String keys = robotConfigModel.getKeys();
    this.title = "Jenkins 构建通知";
    if (keys != null) {
      this.title += "关键字：" + keys;
    }
  }


  public String send(BuildJobModel buildJobModel) {

    List<Btns> btnList = new ArrayList<>();
    String changeLog = buildJobModel.getChangeLog();
    String console = buildJobModel.getConsole();
    String parameters = buildJobModel.getParameters();

    if (!StringUtils.isEmpty(changeLog)) {
      Btns changeLogBtn = new Btns();
      changeLogBtn.setTitle("更改记录");
      changeLogBtn.setActionURL(changeLog);
      btnList.add(changeLogBtn);
    }

    if (!StringUtils.isEmpty(console)) {
      Btns consoleBtn = new Btns();
      consoleBtn.setTitle("控制台");
      consoleBtn.setActionURL(console);
      btnList.add(consoleBtn);
    }

    if (!StringUtils.isEmpty(parameters)) {
      Btns parametersBtn = new Btns();
      parametersBtn.setTitle("构建参数");
      parametersBtn.setActionURL(parameters);
      btnList.add(parametersBtn);
    }

    OapiRobotSendRequest.Actioncard actionCard = new OapiRobotSendRequest.Actioncard();
    actionCard.setTitle(title);
    actionCard.setText(buildJobModel.getText());
    actionCard.setBtnOrientation("1");
    actionCard.setBtns(btnList);

    OapiRobotSendRequest request = new OapiRobotSendRequest();
    request.setMsgtype(MSG_TYPE);
    request.setActionCard(actionCard);
    request.setAt(buildJobModel.getAt());

    try {
      OapiRobotSendResponse response = new DefaultDingTalkClient(robotConfigModel.getServer())
          .execute(request);
      if (!response.isSuccess()) {
        return response.getErrmsg();
      }
    } catch (ApiException e) {
      return e.getMessage();
    }
    return null;
  }
}
