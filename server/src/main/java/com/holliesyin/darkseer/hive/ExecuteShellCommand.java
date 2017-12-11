package com.holliesyin.darkseer.hive;

import com.holliesyin.darkseer.hive.exception.HiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class ExecuteShellCommand {
    private final static Logger LOG = LoggerFactory.getLogger(ExecuteShellCommand.class);

    public static final String HIVE_TMP = "/tmp/hive";
    public static final byte[] CLI_PRINT_HEADER = "set hive.cli.print.header=true;\n".getBytes(Charset.forName(HiveProxyConsts.UTF8));

    public static String executeHiveRequest(String processId) throws Exception {
        long start = System.currentTimeMillis();
        String requestFile = genRequestFilePath(processId);
        String responseFile = genResponseFilePath(processId);

        //输出写入到文件
        try {
            LOG.info("[execute.hive] start run process,requestFile:{},responseFile:{}", requestFile, responseFile);
            ProcessBuilder.Redirect outputRedirect = ProcessBuilder.Redirect.appendTo(new File(responseFile));
            Process process = new ProcessBuilder(PlatForm.HIVE_HOME, "-S", "-f", requestFile).redirectOutput(outputRedirect).start();
            return responseFileModifiedOrProcessExit(requestFile, responseFile, process);
        } finally {
            FileUtils.deleteQuietly(new File(requestFile));
            FileUtils.deleteQuietly(new File(responseFile));
            LOG.info("[execute.hive] cost:{}ms", System.currentTimeMillis() - start);
        }
    }

    private static String responseFileModifiedOrProcessExit(final String requestFile, final String responseFile, final Process process) throws InterruptedException, java.util.concurrent.ExecutionException {
        try {
            //监听响应文件或查看进程状态
            ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("shell-demo-%d").build();
            ExecutorService pool = new ThreadPoolExecutor(1,2,0L,TimeUnit.HOURS,new LinkedBlockingQueue<Runnable>(10000),tf,new ThreadPoolExecutor.AbortPolicy());
            List<Callable<String>> monitorTasks = new ArrayList<Callable<String>>();

            monitorTasks.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    int exitValue = process.waitFor();
                    if (exitValue != 0 && process.getErrorStream() != null) {
                        LOG.error("[execute.hive] response error.requestFile:{}", requestFile);
                        throw new HiveException("执行Hive异常.");
                    }
                    return StringUtils.EMPTY;
                }
            });
            pool.invokeAny(monitorTasks);
        } finally {
            if (process != null) {
                process.destroy();
            }
            String ret = null;
            try {
                ret = FileUtils.readFileToString(new File(responseFile));
            } catch (IOException e) {
                LOG.error("[execute.hive] response error,requestFile:{}", requestFile, e);
            }
            LOG.info("[execute.hive] response success,requestFile:{},response:{}", requestFile, ret);
            return ret;
        }
    }

    public static String executeHiveSql(String sql) throws Exception {
        //组装可执行文件
        String processId = RandomStringUtils.randomNumeric(5);

        File dirFile = new File(HIVE_TMP);

        if (!dirFile.exists()) {
            dirFile.setReadable(true);
            dirFile.setWritable(true);
            dirFile.setExecutable(true);
            dirFile.mkdir();
        }

        String requestFilePath = genRequestFilePath(processId);
        File requestFile = new File(requestFilePath);

        if (!requestFile.createNewFile()) {
            throw new HiveException("创建请求文件失败");
        }

        if (!new File(genResponseFilePath(processId)).createNewFile()) {
            throw new HiveException("创建响应文件失败");
        }

        OutputStream os = null;
        try {
            os = new FileOutputStream(requestFile);
            os.write(CLI_PRINT_HEADER);
            os.write(sql.getBytes(HiveProxyConsts.UTF8));
            return executeHiveRequest(processId);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private static String genRequestFilePath(String processId) {
        return HIVE_TMP + "/tmp_hive_req_" + processId + ".sql";
    }

    private static String genResponseFilePath(String processId) {
        return HIVE_TMP + "/tmp_hive_resp_" + processId + ".txt";
    }
}