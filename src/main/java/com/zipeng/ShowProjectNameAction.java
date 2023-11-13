package com.zipeng;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : liang.zi.peng
 * @create 2023-11-13 10:43
 */
public class ShowProjectNameAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Project project = e.getProject();
        Messages.showMessageDialog("项目名称为:" + project.getName(), "这是标题", Messages.getInformationIcon());
    }
}
