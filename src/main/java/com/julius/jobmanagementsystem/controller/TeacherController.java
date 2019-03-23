package com.julius.jobmanagementsystem.controller;

import com.alibaba.fastjson.JSONObject;
import com.julius.jobmanagementsystem.domain.entity.Result;
import com.julius.jobmanagementsystem.domain.entity.Student;
import com.julius.jobmanagementsystem.domain.entity.Task;
import com.julius.jobmanagementsystem.service.ResultService;
import com.julius.jobmanagementsystem.service.StudentService;
import com.julius.jobmanagementsystem.service.TaskService;
import com.julius.jobmanagementsystem.utils.Config;
import com.julius.jobmanagementsystem.utils.FileUtils;
import com.julius.jobmanagementsystem.utils.UploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class TeacherController {
    @Autowired
    private ResultService resultService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private TaskService taskService;

    @RequestMapping("/query")
    public String queryResult(Integer taskId, Integer curPage, Model model) {
        Integer pageSize = 30;
        Integer total = 0;
        try {
            total = resultService.findResultByTaskId(taskId).size();
        } catch (Exception e) {
            model.addAttribute("flag", false);
            System.out.println("error!!!!!" + taskId);
            return "/queryResult";
        }
        Integer totalPage = (total % pageSize) == 0 ? total / pageSize : (total / pageSize) + 1;
        if (curPage == null || curPage <= 1)
            curPage = 1;
        if (curPage > totalPage)
            curPage = totalPage;
        System.out.println("curOpage" + curPage);
        List<Result> list = new ArrayList<Result>();
        List<String> stuNameList = new ArrayList<String>();
        System.out.println("taskId" + taskId);
        try {
            list = resultService.findResultByTaskId(taskId, (curPage - 1) * pageSize, pageSize);
            for (Result result : list) {
                String stuId = result.getStuId();
                Student stu = new Student();
                stu = studentService.findStudentInfoByStuId(stuId);
                stuNameList.add(stu.getStuName());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<Task> taskList = new ArrayList<Task>();
        try {
            taskList = taskService.findAllTasks();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            list = null;
            e.printStackTrace();
        }
        model.addAttribute("taskList", taskList);
        model.addAttribute("resultList", list);
        model.addAttribute("stuNameList", stuNameList);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("curPage", curPage);
        model.addAttribute("taskId", taskId);
        return "/queryResult";
    }

    @RequestMapping(value = "/updateResult", method = RequestMethod.POST)
    public void updateResult(@RequestBody JSONObject json, HttpServletResponse response) {
        String stuId = json.getString("stuId");
        Integer taskId = json.getInteger("taskId");
        Integer score = json.getInteger("score");
        Result result = new Result();
        result.setStuId(stuId);
        result.setTaskId(taskId);
        result.setScore(score);
        int flag = 0;
        try {
            flag = resultService.updateResult(result);

        } catch (Exception e) {
            flag = 0;
            e.printStackTrace();
        }
        try {
            response.getWriter().print(flag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 教师更新作业
     *
     * @param request    请求信息
     * @param taskId     作业编号
     * @param taskName   作业名称
     * @param datetime   更新日期
     * @param uploadfile 上传文件的流对象
     * @return
     */
    @RequestMapping("/updatetask")
    public String updateTask(
            final HttpServletRequest request,
            final @RequestParam(value = "taskid") Integer taskId,
            final @RequestParam(value = "taskname") String taskName,
            final @RequestParam(value = "datetime") String datetime,
            final @RequestParam(value = "uploadfile", required = false)
                    MultipartFile[] uploadfile) {
        Task task = new Task();
        String oldUrl = "";
        String newUrl = "";
        String oldName = "";
        String newName = "";
        boolean flag = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            task = taskService.findTaskByTaskId(taskId);
            oldName = task.getTaskDownloadName();
            newName = uploadfile[0].getOriginalFilename();
            oldUrl = Config.title + taskId;
            newUrl = Config.title + taskId;
            task.setTaskName(taskName);
            task.setTaskExpiry(sdf.parse(datetime));
            task.setTaskDownloadName(newName);
            taskService.updateTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File oldDir = new File(oldUrl);
        if (uploadfile[0].getSize() == 0) {//未选择上传文件,只修改文件夹名字
            flag = oldDir.renameTo(new File(newUrl, newName));
            if (flag) {
                System.out.println("文件名修改成功");
            }
        } else {
            //选择上传文件，删除原来的文件，新建文件并写入上传的文件
            FileUtils fu = new FileUtils();
            File file = new File(oldDir, oldName);
            fu.deleteFile(file);
            UploadUtils up = new UploadUtils();
            up.uploadUtils(uploadfile, newUrl);
        }
        //获取修改成功后的作业信息
//        Task t = Common.getTaskByName(taskName, taskService);
        //获取评分类对象
//        Rating rate = new Rating(t.getTaskName(), t.getTaskId());
        //获取当前活跃的线程信息，找到要修改的作业之前开启的线程并关闭
        //获取当前活跃的线程组
//        ThreadGroup group = Thread.currentThread().getThreadGroup();
//        Thread thread = null;
//        String threadName = oldName;
        //找到指定名字的线程，即获取要停止运行的线程
//        while (group != null) {
//            Thread[] threads = new Thread[(int) (group.activeCount() * 1.2)];
//            int count = group.enumerate(threads, true);
//            for (int i = 0; i < count; i++) {
//                System.out.println("线程名字：" + threads[i].getName());
//                if (threadName.equals(threads[i].getName())) {
//                    thread = threads[i];
//                    break;
//                }
//            }
//            group = group.getParent();
//        }
//        //如果要停止运行的线程存在，即仍在运行，则打断线程
//        if (thread != null) {
//            thread.interrupt();
//            System.out.println(oldName + "线程已被打断");
//        } else {
//            System.out.println("找不到线程！！！！");
//        }
//        //以新的作业信息开启新线程
//        new Thread(new AutoCheckThread(rate, t.getTaskExpiry(), resultService), t.getTaskName()).start();
        return "redirect:/managejob";
    }

    /**
     * 教师新增作业
     *
     * @param taskName    作业名称
     * @param datetime    作业最迟提交日期
     * @param uploadFiles 上传文件流对象
     * @return
     */
    @RequestMapping(value = "/uploadtask", method = RequestMethod.POST)
    public String upload(@RequestParam(value = "taskname") String taskName,
                         @RequestParam(value = "datetime") String datetime,
                         @RequestParam(value = "uploadfile", required = false)
                                 MultipartFile[] uploadFiles) {
        //查询数据表已经存在的作业记录,在其基础上自增
        List<Task> tasks = taskService.findAllTasks();
        Task task = new Task();
        Integer taskId = 0;
        if (tasks.size() > 0) {
            taskId = tasks.get(0).getTaskId();
            taskId++;
            task.setTaskId(taskId);
        }
        //文件存放路径,绝对路径+作业id
        String road = Config.title + task.getTaskId();

        UploadUtils up = new UploadUtils();
        if (up.uploadUtils(uploadFiles, road)) {
            //设置作业名称
            task.setTaskName(taskName);
            //文件下载名称
            task.setTaskDownloadName(uploadFiles[0].getOriginalFilename());
            // 日期转换
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date date = sdf.parse(datetime);
                task.setTaskExpiry(date);
                // 把作业记录添加到数据库中
                taskService.addTask(task);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

            // 在result中添加记录，作业和所有的学生
            // 获取当前的所有学生ID
            List<Student> students = new ArrayList<Student>();
            try {
                students = studentService.findAllStudent();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 获取作业ID
            try {
                task = taskService.findTaskByTaskName(taskName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (Student student : students) {
                Result result = new Result();
                result.setStuId(student.getStuId());
                result.setTaskId(task.getTaskId());
                try {
                    resultService.addResult(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // 开起线程到规定时间后开启自动评分
//        Task task = Common.getTaskByName(taskName, taskService);
//        //获取评分类对象
//        Rating rate = new Rating(task.getTaskName(), task.getTaskId());
//        //开启自动评分线程
//        new Thread(new AutoCheckThread(rate, task.getTaskExpiry(), resultService), task.getTaskName()).start();
        return "redirect:/managejob";
    }

    @RequestMapping("/taskIsExist")
    public void taskIsExist(String taskname, HttpServletResponse response) {
        String flag = "true";
        List<Task> tasks = new ArrayList<Task>();
        // 存放所有的作业
        try {
            tasks = taskService.findAllTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Task task : tasks) {
            if (task.getTaskName().equals(taskname)) {
                flag = "false";
            }
        }
        JSONObject json = new JSONObject();
        json.put("getdata", flag);
        try {
            response.getWriter().print(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
